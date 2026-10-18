package cn.movelet.app

import java.time.LocalDate
import java.util.UUID
import kotlin.math.max

data class PlanItem(val exerciseId: String, val sets: Int, val dose: Int, val section: String,
    val optional: Boolean = false, val enabled: Boolean = true)
data class DayPlan(val date: String, val title: String, val strength: Boolean, val items: List<PlanItem>)
data class WeekTemplate(val weekday: Int, val effectiveFrom: String, val plan: DayPlan)
data class SetResult(val dose: Int, val unit: DoseUnit)
data class WorkItem(val plan: PlanItem, val completed: List<SetResult> = emptyList(), val skipped: Boolean = false)
data class Session(
    val id: String = UUID.randomUUID().toString(), val date: String, val title: String,
    val strength: Boolean, val items: List<WorkItem>, val startedAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null, val seconds: Int = 0, val rest: Int = 0, val paused: Boolean = false,
    val simplified: Boolean = false, val effort: String = "未记录", val pain: Boolean? = null,
    val originalSets: Int = items.sumOf { it.plan.sets }, val schemaVersion: Int = 1
) {
    val currentIndex: Int get() = items.indexOfFirst { !it.skipped && it.completed.size < it.plan.sets }
    val doneSets: Int get() = items.sumOf { it.completed.size }
    val targetSets: Int get() = items.sumOf { it.plan.sets }
    val allDone: Boolean get() = items.isNotEmpty() && items.all { (it.plan.optional && it.skipped) || (!it.skipped && it.completed.size >= it.plan.sets) }
    val status: String get() = when {
        endedAt == null -> "进行中"
        doneSets == 0 -> "未完成"
        simplified && allDone -> "简化完成"
        allDone -> "已完成"
        else -> "部分完成"
    }
}

object Plans {
    fun resolve(date: LocalDate, overrides: Map<String, DayPlan>, templates: Map<Int, WeekTemplate>): DayPlan =
        overrides[date.toString()] ?: templates[date.dayOfWeek.value]
            ?.takeIf { date.toString() >= it.effectiveFrom }?.plan?.copy(date = date.toString()) ?: forDate(date)

    fun forDate(date: LocalDate): DayPlan {
        val day = date.dayOfWeek.value
        val strength = day in listOf(1, 3, 5)
        val base = listOf(
            PlanItem("neck", 1, 3, "肩背活动"), PlanItem("twist", 1, 5, "肩背活动"),
            PlanItem("scapula", 1, 8, "肩背活动"), PlanItem("chest", 3, 15, "肩背活动")
        )
        val main = when (day) {
            1, 5 -> listOf(PlanItem("incline", 2, 6, "力量训练"), PlanItem("squat", 2, 8, "力量训练"),
                PlanItem("bridge", 2, 10, "力量训练"), PlanItem("dead_bug", 2, 5, "力量训练"))
            3 -> listOf(PlanItem("incline", 2, 6, "力量训练"), PlanItem("sit_stand", 2, 8, "力量训练"),
                PlanItem("prone_w", 2, 6, "力量训练"), PlanItem("bird_dog", 2, 5, "力量训练"))
            6 -> listOf(PlanItem("sit_stand", 1, 6, "轻量活动"), PlanItem("calf", 1, 8, "轻量活动"))
            else -> listOf(PlanItem("thread", 1, 4, "舒缓活动", true, false))
        }
        return DayPlan(date.toString(), when(day) { 1,5 -> "全身力量 A"; 3 -> "全身力量 B"; 6 -> "轻活动日"; else -> "恢复与舒展" },
            strength, base + main + PlanItem("march", 2, 30, "可选有氧", true, false))
    }
    fun safe(item: PlanItem): PlanItem {
        val e = Catalog.get(item.exerciseId)
        return item.copy(sets = item.sets.coerceIn(1, 5), dose = item.dose.coerceIn(1, if (e.unit == DoseUnit.SECONDS) 120 else 30))
    }
    fun substitute(item: PlanItem, id: String): PlanItem {
        val e = Catalog.get(id)
        require(e.reviewed)
        return item.copy(exerciseId = id, dose = e.dose, sets = minOf(item.sets, e.sets))
    }
}

object Training {
    fun start(plan: DayPlan) = Session(date = plan.date, title = plan.title, strength = plan.strength,
        items = plan.items.filter { it.enabled }.map { WorkItem(Plans.safe(it)) })
    fun tick(s: Session) = if (s.paused || s.endedAt != null || s.currentIndex < 0) s
        else s.copy(seconds = s.seconds + 1, rest = max(0, s.rest - 1))
    fun complete(s: Session, actualDose: Int): Session {
        if (s.paused || s.rest > 0 || s.endedAt != null || s.currentIndex < 0) return s
        val index = s.currentIndex
        val item = s.items[index]
        val unit = Catalog.get(item.plan.exerciseId).unit
        val dose = actualDose.coerceIn(1, if (unit == DoseUnit.SECONDS) 120 else 30)
        val items = s.items.mapIndexed { i, w -> if (i == index) w.copy(completed = w.completed + SetResult(dose, unit)) else w }
        val next = s.copy(items = items)
        return next.copy(rest = if (next.currentIndex < 0) 0 else if (item.plan.section == "力量训练") 60 else 20)
    }
    fun skip(s: Session): Session {
        if (s.endedAt != null || s.currentIndex < 0) return s
        return s.copy(items = s.items.mapIndexed { i, w -> if (i == s.currentIndex) w.copy(skipped = true) else w }, rest = 0)
    }
    fun simplify(s: Session): Session {
        if (s.endedAt != null) return s
        return s.copy(simplified = true, items = s.items.map { w ->
            if (w.skipped || w.completed.size >= w.plan.sets) w
            else if (w.plan.optional) w.copy(skipped = true)
            else w.copy(plan = w.plan.copy(sets = max(w.completed.size, 1), dose = max(1, w.plan.dose * 2 / 3)))
        }, rest = 0)
    }
    fun finish(s: Session, effort: String, pain: Boolean) = s.copy(endedAt = System.currentTimeMillis(), paused = true, rest = 0, effort = effort, pain = pain)
    fun progression(sessions: List<Session>): String {
        val recent = sessions.filter { it.strength && it.endedAt != null }.sortedByDescending { it.startedAt }.take(2)
        if (recent.any { it.pain == true }) return "最近记录有疼痛：先保持或减量，避开诱发动作；持续或加重时寻求专业评估。"
        val ready = recent.size == 2 && recent.all { it.allDone && !it.simplified && it.effort == "轻松" && it.pain == false && it.items.all { w -> w.completed.all { it.dose >= w.plan.dose } } }
        val same = ready && recent[0].items.map { it.plan } == recent[1].items.map { it.plan }
        return if (same) "最近两次同计划均轻松、完整且无痛：下次可只选一个力量动作，每组加 1 次。需你手动调整。"
        else "先保持当前量。连续两次同计划轻松、完整且无痛后，再考虑只为一个动作每组加 1 次。"
    }
}
