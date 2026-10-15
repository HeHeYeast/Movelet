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
