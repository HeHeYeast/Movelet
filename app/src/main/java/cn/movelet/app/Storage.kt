package cn.movelet.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

@Entity(tableName = "plans")
data class PlanRow(@PrimaryKey val date: String, val json: String)
@Entity(tableName = "week_templates")
data class TemplateRow(@PrimaryKey val weekday: Int, val json: String)
@Entity(tableName = "workouts")
data class WorkoutRow(@PrimaryKey val id: String, val date: String, val active: Boolean, val startedAt: Long, val json: String)
@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts ORDER BY startedAt DESC") fun observe(): Flow<List<WorkoutRow>>
    @Query("SELECT * FROM workouts WHERE active = 1 LIMIT 1") suspend fun active(): WorkoutRow?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun put(row: WorkoutRow)
    @Query("SELECT * FROM plans") fun plans(): Flow<List<PlanRow>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putPlan(row: PlanRow)
    @Query("SELECT * FROM plans WHERE date = :date") suspend fun plan(date: String): PlanRow?
    @Query("DELETE FROM plans WHERE date = :date") suspend fun deletePlan(date: String)
    @Query("SELECT * FROM week_templates") fun templates(): Flow<List<TemplateRow>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun putTemplate(row: TemplateRow)
    @Query("DELETE FROM week_templates WHERE weekday = :weekday") suspend fun deleteTemplate(weekday: Int)
}
@Database(entities = [PlanRow::class, WorkoutRow::class, TemplateRow::class], version = 2, exportSchema = true)
abstract class MoveletDb : RoomDatabase() { abstract fun dao(): WorkoutDao }
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS week_templates (weekday INTEGER NOT NULL, json TEXT NOT NULL, PRIMARY KEY(weekday))")
    }
}
class MoveletApplication : Application() {
    val db by lazy { Room.databaseBuilder(this, MoveletDb::class.java, "movelet.db").addMigrations(MIGRATION_1_2).build() }
}

class Repository(private val db: MoveletDb) {
    private val gson = Gson()
    private val mutex = Mutex()
    val sessions = db.dao().observe().map { rows -> rows.map { gson.fromJson(it.json, Session::class.java) } }
    val plans = db.dao().plans().map { rows -> rows.associate { it.date to gson.fromJson(it.json, DayPlan::class.java) } }
    val templates = db.dao().templates().map { rows -> rows.associate { it.weekday to gson.fromJson(it.json, WeekTemplate::class.java) } }
    suspend fun saveTemplate(weekday: Int, plan: DayPlan, effectiveFrom: LocalDate = LocalDate.now()) {
        require(weekday in 1..7)
        db.dao().putTemplate(TemplateRow(weekday, gson.toJson(WeekTemplate(weekday, effectiveFrom.toString(), plan.copy(items = plan.items.map(Plans::safe))))))
    }
    private suspend fun save(s: Session) = db.dao().put(WorkoutRow(s.id, s.date, s.endedAt == null, s.startedAt, gson.toJson(s)))
    suspend fun savePlan(plan: DayPlan) = db.dao().putPlan(PlanRow(plan.date, gson.toJson(plan.copy(items = plan.items.map(Plans::safe)))))
    suspend fun resetPlan(date: String) = db.dao().deletePlan(date)
    suspend fun resetTemplate(weekday: Int) = db.dao().deleteTemplate(weekday)
    suspend fun start(plan: DayPlan) = mutex.withLock {
        db.withTransaction { if (plan.items.any { it.enabled } && db.dao().active() == null) save(Training.start(plan)) }
    }
    suspend fun change(transform: (Session) -> Session) = mutex.withLock {
        db.withTransaction {
            db.dao().active()?.let { row ->
                val old = gson.fromJson(row.json, Session::class.java)
                val next = transform(old)
                if (next != old) save(next)
            }
        }
    }
}

data class HomeData(val plans: Map<String, DayPlan> = emptyMap(), val templates: Map<Int, WeekTemplate> = emptyMap(),
    val sessions: List<Session> = emptyList(), val ready: Boolean = false)

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = Repository((app as MoveletApplication).db)
    val home = combine(repository.plans, repository.templates, repository.sessions) { plans, templates, sessions ->
        HomeData(plans, templates, sessions, true)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, HomeData())
    val error = MutableStateFlow<String?>(null)
    init {
        // A process restart always restores a paused session rather than counting unattended time.
        action { repository.change { it.copy(paused = true) } }
        viewModelScope.launch { while (true) { delay(1000); safely { repository.change(Training::tick) } } }
    }
    private suspend fun safely(block: suspend () -> Unit) { try { block() } catch (e: Exception) {
        if (e is kotlinx.coroutines.CancellationException) throw e
        error.value = "保存未成功，请重试。${e.localizedMessage.orEmpty()}"
    } }
    private fun action(block: suspend () -> Unit) { viewModelScope.launch { safely(block) } }
    fun start(plan: DayPlan) { if (plan.date == LocalDate.now().toString()) action { repository.start(plan) } }
    fun edit(plan: DayPlan) = action { repository.savePlan(plan) }
    fun saveEdited(plan: DayPlan, weekday: Int?, useWeekly: Boolean, done: (Boolean) -> Unit) {
        viewModelScope.launch {
            var success = false
            safely {
                when {
                    weekday != null && useWeekly -> repository.resetTemplate(weekday)
                    weekday != null -> repository.saveTemplate(weekday, plan)
                    useWeekly -> repository.resetPlan(plan.date)
                    else -> repository.savePlan(plan)
                }
                success = true
            }
            done(success)
        }
    }
    fun change(transform: (Session) -> Session) = action { repository.change(transform) }
    fun pause() = change { it.copy(paused = true) }
}
