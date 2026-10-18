package cn.movelet.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StorageTest {
    @Test fun resettingWeekdayRemovesCustomTemplateButKeepsOtherDays() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), MoveletDb::class.java).build()
        val repo = Repository(db)
        val day = LocalDate.of(2026, 10, 19)
        repo.saveTemplate(1, Plans.forDate(day).copy(title = "自定义周一"), day)
        repo.saveTemplate(2, Plans.forDate(day.plusDays(1)).copy(title = "自定义周二"), day)
        repo.resetTemplate(1)
        val templates = repo.templates.first()
        assertFalse(templates.containsKey(1))
        assertEquals("自定义周二", templates[2]!!.plan.title)
        assertEquals(Plans.forDate(day), Plans.resolve(day, emptyMap(), templates))
        db.close()
    }
    @Test fun emptyRestPlanCannotStartAndDateOverrideCanReturnToTemplate() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), MoveletDb::class.java).build()
        val repo = Repository(db)
        val day = LocalDate.of(2026, 10, 19)
        val rest = DayPlan(day.toString(), "休息", false, emptyList())
        repo.saveTemplate(1, rest, day)
        repo.savePlan(Plans.forDate(day))
        repo.resetPlan(day.toString())
        assertEquals(rest, Plans.resolve(day, repo.plans.first(), repo.templates.first()))
        repo.start(rest)
        assertTrue(repo.sessions.first().isEmpty())
        db.close()
    }
    @Test fun upgradeFromVersionOneKeepsPlanAndCompletedWorkout() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "upgrade-${System.nanoTime()}.db"
        val plan = Plans.forDate(LocalDate.of(2026, 10, 18)).copy(title = "升级前计划")
        val session = Training.finish(Training.complete(Training.start(plan), 3), "适中", false)
        val gson = Gson()
        context.openOrCreateDatabase(name, 0, null).use { old ->
            old.execSQL("CREATE TABLE plans (date TEXT NOT NULL PRIMARY KEY, json TEXT NOT NULL)")
            old.execSQL("CREATE TABLE workouts (id TEXT NOT NULL PRIMARY KEY, date TEXT NOT NULL, active INTEGER NOT NULL, startedAt INTEGER NOT NULL, json TEXT NOT NULL)")
            old.execSQL("INSERT INTO plans VALUES (?, ?)", arrayOf(plan.date, gson.toJson(plan)))
            old.execSQL("INSERT INTO workouts VALUES (?, ?, ?, ?, ?)", arrayOf(session.id, session.date, 0, session.startedAt, gson.toJson(session)))
            old.version = 1
        }
        val db = Room.databaseBuilder(context, MoveletDb::class.java, name).addMigrations(MIGRATION_1_2).build()
        val repo = Repository(db)
        assertEquals(plan, repo.plans.first()[plan.date])
        assertEquals(session, repo.sessions.first().single())
        assertTrue(repo.templates.first().isEmpty())
        db.close()
        context.deleteDatabase(name)
        Unit
    }
    @Test fun weeklyTemplatePersistsWithoutOverwritingDateOverrideOrSession() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "weekly-${System.nanoTime()}.db"
        var db = Room.databaseBuilder(context, MoveletDb::class.java, name).build()
        val monday = LocalDate.of(2026, 10, 19)
        val original = Plans.forDate(monday)
        var repo = Repository(db)
        repo.start(original)
        val custom = original.copy(title = "自己的周一", items = listOf(PlanItem("bird_dog", 3, 7, "力量训练")))
        repo.saveTemplate(1, custom, monday)
        repo.savePlan(original.copy(title = "只改今天"))
        db.close()
        db = Room.databaseBuilder(context, MoveletDb::class.java, name).build()
        repo = Repository(db)
        val templates = repo.templates.first()
        assertEquals("自己的周一", Plans.resolve(monday.plusWeeks(1), emptyMap(), templates).title)
        assertEquals(custom.items, Plans.resolve(monday.plusWeeks(1), emptyMap(), templates).items)
        assertEquals("只改今天", Plans.resolve(monday, repo.plans.first(), templates).title)
        assertEquals(original.items.filter { it.enabled }, repo.sessions.first().single().items.map { it.plan })
        assertEquals(Plans.forDate(monday.minusWeeks(1)), Plans.resolve(monday.minusWeeks(1), emptyMap(), templates))
        db.close()
        context.deleteDatabase(name)
        Unit
    }
    @Test fun roomPersistsPlanSessionAndFeedbackAcrossReopen() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "persistence-${System.nanoTime()}.db"
        var db = Room.databaseBuilder(context, MoveletDb::class.java, name).build()
        val plan = Plans.forDate(LocalDate.of(2026, 10, 12))
        val repo = Repository(db)
        repo.savePlan(plan)
        repo.start(plan)
        repo.change { Training.complete(it, 2) }
        repo.change { it.copy(seconds = 23, paused = true) }
        val id = repo.sessions.first().single().id
        db.close()
        db = Room.databaseBuilder(context, MoveletDb::class.java, name).build()
        val reopened = Repository(db)
        val restored = reopened.sessions.first().single()
        assertEquals(id, restored.id)
        assertEquals(1, restored.doneSets)
        assertEquals(2, restored.items.first().completed.single().dose)
        assertEquals(23, restored.seconds)
        assertTrue(restored.paused)
        assertEquals(plan, reopened.plans.first()[plan.date])
        reopened.change { Training.finish(it, "吃力", true) }
        assertNull(db.dao().active())
        assertEquals(true, reopened.sessions.first().single().pain)
        assertEquals("部分完成", reopened.sessions.first().single().status)
        db.close()
        context.deleteDatabase(name)
        Unit
    }
    @Test fun startCannotOverwriteActiveSessionOrHistorySnapshot() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), MoveletDb::class.java).build()
        val repo = Repository(db)
        val a = Plans.forDate(LocalDate.of(2026, 10, 12))
        val b = Plans.forDate(LocalDate.of(2026, 10, 14))
        repo.start(a); repo.start(b); repo.savePlan(a.copy(title = "changed"))
        val rows = repo.sessions.first()
        assertEquals(1, rows.size)
        assertEquals(a.title, rows.single().title)
        assertEquals(a.items.filter { it.enabled }, rows.single().items.map { it.plan })
        repo.change { Training.finish(it, "适中", false) }
        repo.start(b)
        assertEquals(2, repo.sessions.first().size)
        assertEquals(b.date, Gson().fromJson(db.dao().active()!!.json, Session::class.java).date)
        db.close()
    }
}
