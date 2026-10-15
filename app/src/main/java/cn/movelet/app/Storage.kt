package cn.movelet.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

@Entity(tableName = "plans")
data class PlanRow(@PrimaryKey val date: String, val json: String)
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
}
@Database(entities = [PlanRow::class, WorkoutRow::class], version = 1, exportSchema = true)
abstract class MoveletDb : RoomDatabase() { abstract fun dao(): WorkoutDao }
class MoveletApplication : Application() {
    val db by lazy { Room.databaseBuilder(this, MoveletDb::class.java, "movelet.db").build() }
}

class Repository(private val db: MoveletDb) {
    private val gson = Gson()
    private val mutex = Mutex()
    val sessions = db.dao().observe().map { rows -> rows.map { gson.fromJson(it.json, Session::class.java) } }
    val plans = db.dao().plans().map { rows -> rows.associate { it.date to gson.fromJson(it.json, DayPlan::class.java) } }
    private suspend fun save(s: Session) = db.dao().put(WorkoutRow(s.id, s.date, s.endedAt == null, s.startedAt, gson.toJson(s)))
    suspend fun savePlan(plan: DayPlan) = db.dao().putPlan(PlanRow(plan.date, gson.toJson(plan.copy(items = plan.items.map(Plans::safe)))))
    suspend fun start(plan: DayPlan) = mutex.withLock {
        db.withTransaction { if (db.dao().active() == null) save(Training.start(plan)) }
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

class MainViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = Repository((app as MoveletApplication).db)
    val sessions = repository.sessions.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val plans = repository.plans.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())
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
    fun change(transform: (Session) -> Session) = action { repository.change(transform) }
    fun pause() = change { it.copy(paused = true) }
}
