package cn.movelet.app

import java.time.LocalDate
import org.junit.Assert.*
import org.junit.Test

class PosterPlanTest {
    private val monday = LocalDate.of(2026, 10, 19)
    @Test fun posterExercisesHaveTheirOwnTeachingAndPreserveTimedTargets() {
        (0L..6L).flatMap { Plans.forDate(monday.plusDays(it)).items }.forEach { item ->
            val exercise = Catalog.get(item.exerciseId)
            assertTrue(exercise.reviewed)
            assertTrue(exercise.steps.size >= 2 && exercise.sources.isNotEmpty())
            assertEquals(item.dose, Plans.safe(item).dose)
        }
        assertNotEquals(Catalog.get("wall_slide").id, Catalog.get("wall_angel").id)
        assertNotEquals(Catalog.get("split").id, Catalog.get("reverse_lunge").id)
        assertNotEquals(Catalog.get("chin").id, Catalog.get("chin_tuck").id)
        val sunday = Training.start(Plans.forDate(monday.plusDays(6)))
        val walk = sunday.items.last().plan
        val solo = Training.start(DayPlan(monday.toString(), "walk", false, listOf(walk)))
        assertEquals(180, Training.complete(solo, 180).items.single().completed.single().dose)
        assertEquals("秒 / 侧", Catalog.get("doorway").unit.label)
    }
    @Test fun sevenDayDefaultsFollowTheRequestedPoster() {
        val base = listOf("chin_tuck:1:8", "cat:1:8", "thread:1:5", "wall_angel:1:8", "doorway:1:20")
        val main = listOf(
            listOf("pushup:2:6", "squat:2:12", "bridge:2:12", "dead_bug:2:8", "jumping_jack:2:30"),
            listOf("bird_dog:2:8", "prone_w:2:10", "calf:2:15", "jumping_jack:2:30", "walk:1:120"),
            listOf("reverse_lunge:2:8", "prone_w:2:10", "plank:2:20", "bird_dog:2:8", "floor_burpee:2:5"),
            listOf("squat:2:12", "bridge:2:12", "prone_w:2:10", "calf:2:15", "jumping_jack:2:30"),
            listOf("pushup:2:6", "squat:2:12", "bridge:2:12", "dead_bug:2:8", "jumping_jack:2:30"),
            listOf("reverse_lunge:2:8", "bird_dog:2:8", "plank:2:20", "prone_w:2:10", "jumping_jack:1:30"),
            listOf("bird_dog:1:6", "squat:1:10", "jumping_jack:1:30", "walk:1:180")
        )
        repeat(7) { i ->
            val plan = Plans.forDate(monday.plusDays(i.toLong()))
            assertEquals("day ${i + 1}", base + main[i], plan.items.map { "${it.exerciseId}:${it.sets}:${it.dose}" })
            assertEquals(5, plan.items.count { it.section == "肩背活动" })
            assertTrue(plan.items.filter { it.section == "可选有氧" }.all { it.optional && !it.enabled })
            assertEquals(plan.items.map { it.exerciseId }.size, plan.items.map { it.exerciseId }.distinct().size)
        }
    }
}
