package cn.movelet.app

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate

class TrainingTest {
    private val monday = LocalDate.of(2026, 10, 12)
    private fun one(sets: Int = 2) = Training.start(DayPlan(monday.toString(), "test", true, listOf(PlanItem("dead_bug", sets, 5, "力量训练"))))
    @Test fun catalogHasCompleteUniqueEntries() {
        assertEquals(32, Catalog.all.size)
        assertEquals(32, Catalog.all.map { it.id }.distinct().size)
        Catalog.all.forEach {
            assertTrue(it.steps.size >= 2)
            assertTrue(it.name.isNotBlank() && it.english.isNotBlank() && it.setup.isNotBlank() && it.breathing.isNotBlank())
            assertTrue(it.safety.isNotBlank() && it.mistakes.isNotEmpty() && it.easier.isNotBlank() && it.harder.isNotBlank())
            assertTrue(it.sources.isNotEmpty() && it.sources.all { s -> s.url.startsWith("https://") })
        }
    }
    @Test fun weekUsesPosterDayTypesAndOnlyContentCheckedDefaults() {
        val plans = (0L..6L).map { Plans.forDate(monday.plusDays(it)) }
        assertEquals(listOf(true,false,true,false,true,true,false), plans.map { it.strength })
        plans.forEach { p ->
            assertEquals(5, p.items.count { it.section == "肩背活动" })
            assertTrue(p.items.filter { it.enabled }.all { Catalog.get(it.exerciseId).reviewed })
            assertTrue(p.items.filter { it.section == "可选有氧" }.all { !it.enabled && it.optional })
        }
    }
    @Test fun bilateralDoseIsStoredPerSide() {
        val s = Training.complete(one(), 4)
        assertEquals(1, s.doneSets)
        assertEquals(4, s.items[0].completed[0].dose)
        assertEquals(DoseUnit.EACH_SIDE, s.items[0].completed[0].unit)
        assertEquals(60, s.rest)
    }
    @Test fun repeatedCompleteDuringRestCannotCreateExtraSet() {
        val s = Training.complete(one(), 5)
        assertEquals(s, Training.complete(s, 5))
    }
    @Test fun pauseStopsTimeAndCompletion() {
        val s = one().copy(paused = true, rest = 12)
        assertEquals(s, Training.tick(s))
        assertEquals(s, Training.complete(s, 5))
    }
    @Test fun timerCountsActiveSecondsAndNeverGoesNegative() {
        var s = one().copy(rest = 1)
        repeat(3) { s = Training.tick(s) }
        assertEquals(3, s.seconds)
        assertEquals(0, s.rest)
    }
    @Test fun skipIsNeverCompletion() {
        val s = Training.finish(Training.skip(one()), "适中", false)
        assertEquals(0, s.doneSets)
        assertEquals("未完成", s.status)
        assertFalse(s.allDone)
    }
    @Test fun partialSetThenSkipKeepsActualWork() {
        val s = Training.finish(Training.skip(Training.complete(one(), 3)), "吃力", false)
        assertEquals(1, s.doneSets)
        assertEquals("部分完成", s.status)
    }
    @Test fun simplifyPreservesCompletedSetsAndOriginalTarget() {
        val first = Training.complete(one(3), 5)
        val second = Training.complete(first.copy(rest = 0), 4)
        val s = Training.simplify(second)
        assertEquals(second.items[0].completed, s.items[0].completed)
        assertEquals(2, s.targetSets)
        assertEquals(3, s.originalSets)
        assertTrue(s.simplified)
        assertEquals("简化完成", Training.finish(s, "适中", false).status)
    }
    @Test fun simplifyRemovesOptionalWorkAndKeepsRequiredMinimum() {
        val plan = Plans.forDate(monday)
        val enabled = plan.copy(items = plan.items.map { it.copy(enabled = true) })
        val s = Training.simplify(Training.start(enabled))
        assertTrue(s.items.filter { it.plan.optional }.all { it.skipped })
        assertTrue(s.items.all { it.plan.dose > 0 && it.plan.sets > 0 })
    }
    @Test fun completeFinalSetHasNoRestAndStopsTimer() {
        val s = Training.complete(one(1), 5)
        assertEquals(-1, s.currentIndex)
        assertEquals(0, s.rest)
        assertEquals(s, Training.tick(s))
        assertEquals("已完成", Training.finish(s, "轻松", false).status)
    }
    @Test fun boundsAndSubstitutionsResetDoseUnit() {
        assertEquals(1, Plans.safe(PlanItem("dead_bug", -2, -1, "力量训练")).dose)
        assertEquals(30, Plans.safe(PlanItem("dead_bug", 9, 999, "力量训练")).dose)
        assertEquals(120, Plans.safe(PlanItem("march", 9, 999, "可选有氧")).dose)
        val result = Plans.substitute(PlanItem("neck", 1, 30, "肩背活动"), "chest")
        assertEquals(15, result.dose)
        assertEquals(DoseUnit.SECONDS, Catalog.get(result.exerciseId).unit)
    }
    @Test fun progressionRequiresTwoMatchingPainlessFullEffortReports() {
        val good = Training.finish(Training.complete(one(1), 5), "轻松", false)
        assertTrue(Training.progression(listOf(good, good.copy(id = "two"))).contains("下次可"))
        assertFalse(Training.progression(listOf(good)).contains("下次可"))
        assertFalse(Training.progression(listOf(good, good.copy(simplified = true))).contains("下次可"))
        assertFalse(Training.progression(listOf(good, good.copy(effort = "吃力"))).contains("下次可"))
        assertTrue(Training.progression(listOf(good, good.copy(pain = true))).contains("最近记录有疼痛"))
    }
    @Test fun doingLessThanTargetCannotSuggestProgression() {
        val short = Training.finish(Training.complete(one(1), 3), "轻松", false)
        assertFalse(Training.progression(listOf(short, short.copy(id = "two"))).contains("下次可"))
    }
    @Test fun endedSessionsAreImmutableToTrainingActions() {
        val s = Training.finish(one(), "适中", false)
        assertEquals(s, Training.complete(s, 3))
        assertEquals(s, Training.skip(s))
        assertEquals(s, Training.simplify(s))
        assertEquals(s, Training.tick(s))
    }
    @Test fun skippedOptionalWorkDoesNotInvalidateCompletedRequiredWork() {
        val plan = DayPlan(monday.toString(), "optional", true, listOf(PlanItem("dead_bug", 1, 5, "力量训练"), PlanItem("march", 1, 30, "可选有氧", true)))
        val s = Training.skip(Training.complete(Training.start(plan), 5))
        assertTrue(s.allDone)
        assertEquals(1, s.doneSets)
    }
}
