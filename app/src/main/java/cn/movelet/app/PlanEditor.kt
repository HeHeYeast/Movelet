package cn.movelet.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import java.time.LocalDate

val WeekdayNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
val PlanSections = listOf("肩背活动", "力量训练", "轻量活动", "舒缓活动", "可选有氧", "放松走动")

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun WeeklyPlanScreen(templates: Map<Int, WeekTemplate>, back: () -> Unit, edit: (Int) -> Unit) {
    val monday = LocalDate.now().with(java.time.DayOfWeek.MONDAY)
    Scaffold(topBar = { TopAppBar(title = { Text("每周安排") }, navigationIcon = {
        IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
    }) }) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { Text("分别编辑七天的重复安排。从今天起用于未单独修改的日期；当天修改和已有训练记录不会被覆盖。") }
            items((1..7).toList()) { day ->
                val plan = templates[day]?.plan ?: Plans.forDate(monday.plusDays((day - 1).toLong()))
                Card(onClick = { edit(day) }, modifier = Modifier.fillMaxWidth().testTag("week-$day"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${WeekdayNames[day - 1]} · ${plan.title}", fontWeight = FontWeight.SemiBold)
                        Text("${plan.items.count { it.enabled }} 个动作 · ${plan.items.filter { it.enabled }.sumOf { it.sets }} 组")
                        Text("${if (templates.containsKey(day)) "自定义安排" else "默认安排"} · 点此编辑", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun PlanEditorScreen(initial: DayPlan, restore: DayPlan, weeklyDay: Int?, back: () -> Unit,
    save: (DayPlan, Boolean, (Boolean) -> Unit) -> Unit) {
    val gson = remember { Gson() }
    val initialJson = remember(initial.date, weeklyDay) { gson.toJson(initial) }
    var json by rememberSaveable(initial.date, weeklyDay) { mutableStateOf(initialJson) }
    var useWeekly by rememberSaveable(initial.date, weeklyDay) { mutableStateOf(false) }
    val draft = remember(json) { gson.fromJson(json, DayPlan::class.java) }
    var discard by remember { mutableStateOf(false) }
    var adding by rememberSaveable { mutableStateOf<String?>(null) }
    var editing by rememberSaveable { mutableStateOf<Int?>(null) }
    var busy by remember { mutableStateOf(false) }
    fun change(plan: DayPlan) { json = gson.toJson(plan); useWeekly = false }
    fun leave() { if (!busy) { if (json != initialJson || useWeekly) discard = true else back() } }
    BackHandler { if (adding != null) adding = null else if (editing != null) editing = null else leave() }
    Scaffold(topBar = { TopAppBar(title = { Text(if (weeklyDay == null) "编辑当天动作" else "编辑每${WeekdayNames[weeklyDay - 1]}") },
        navigationIcon = { IconButton(enabled = !busy, onClick = { leave() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "取消编辑") } }) },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Button(enabled = !busy && draft.title.isNotBlank(), modifier = Modifier.fillMaxWidth().navigationBarsPadding().imePadding().padding(16.dp).heightIn(min = 52.dp).testTag("save-plan"),
                    onClick = {
                        busy = true
                        save(draft.copy(title = draft.title.trim(), items = draft.items.map(Plans::safe)), useWeekly) { success ->
                            busy = false
                            if (success) back()
                        }
                    }) { Text(if (busy) "正在保存…" else if (weeklyDay == null) "保存当天安排" else "保存每周安排") }
            }
        }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(if (weeklyDay == null) "仅修改 ${initial.date}。保存后用于下一场训练。" else "每${WeekdayNames[weeklyDay - 1]}重复使用；不覆盖单独调整过的日期。", style = MaterialTheme.typography.bodyMedium)
            OutlinedTextField(draft.title, { change(draft.copy(title = it.take(40))) }, enabled = !busy, label = { Text("计划名称") }, singleLine = true, modifier = Modifier.fillMaxWidth().testTag("plan-title"))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("力量训练日", Modifier.weight(1f))
                Switch(checked = draft.strength, enabled = !busy, onCheckedChange = { change(draft.copy(strength = it)) })
            }
            Text("以力量为主时打开；休息或轻活动日关闭。它用于记录分类和加量建议。", style = MaterialTheme.typography.bodySmall)
            TextButton(enabled = !busy, onClick = { change(draft.copy(items = emptyList(), strength = false)) }) { Text("清空动作，设为休息日") }
            TextButton(enabled = !busy, onClick = { json = gson.toJson(restore.copy(date = initial.date)); useWeekly = true }) {
                Text(if (weeklyDay == null) "恢复每周安排" else "恢复默认安排")
            }
            if (draft.items.isEmpty()) Text("这一天没有动作，可以直接保存为休息日。")
            PlanSections.forEach { section ->
                val indices = draft.items.indices.filter { draft.items[it].section == section }
                SectionTitle(section)
                indices.forEachIndexed { position, index ->
                    val item = draft.items[index]
                    val e = Catalog.get(item.exerciseId)
                    SoftCard(Modifier.testTag("edit-item-${e.id}")) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ExerciseThumbnail(e)
                            Column(Modifier.weight(1f)) {
                                Text(e.name, fontWeight = FontWeight.SemiBold)
                                Text("${item.sets} 组 × ${item.dose} ${e.unit.label}", style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(enabled = !busy, onClick = { editing = index }) { Icon(Icons.Default.Tune, "调整${e.name}") }
                        }
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            if (item.optional) {
                                Text(if (item.enabled) "已加入" else "未加入", Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
                                Switch(item.enabled, enabled = !busy, onCheckedChange = { enabled -> change(draft.copy(items = draft.items.mapIndexed { i, p -> if (i == index) p.copy(enabled = enabled) else p })) })
                            } else Spacer(Modifier.weight(1f))
                            fun move(to: Int) {
                                val list = draft.items.toMutableList()
                                val other = indices[to]
                                list[index] = list[other].also { list[other] = list[index] }
                                change(draft.copy(items = list))
                            }
                            IconButton(enabled = !busy && position > 0, onClick = { move(position - 1) }) { Icon(Icons.Default.ArrowUpward, "上移${e.name}") }
                            IconButton(enabled = !busy && position < indices.lastIndex, onClick = { move(position + 1) }) { Icon(Icons.Default.ArrowDownward, "下移${e.name}") }
                            IconButton(enabled = !busy, onClick = { change(draft.copy(items = draft.items.filterIndexed { i, _ -> i != index })) }) { Icon(Icons.Default.DeleteOutline, "删除${e.name}") }
                        }
                    }
                }
                OutlinedButton(enabled = !busy, onClick = { adding = section }, modifier = Modifier.fillMaxWidth().testTag("add-$section")) {
                    Icon(Icons.Default.Add, null); Text("添加动作到$section")
                }
            }
            Text("建议保留轻量肩背活动，相同肌群的高负荷日之间安排恢复。调整后以实际动作与休息时间为准。", style = MaterialTheme.typography.bodySmall)
        }
    }
    if (discard) AlertDialog(onDismissRequest = { discard = false }, title = { Text("有尚未保存的修改") },
        text = { Text("返回后将保留原来的安排。") }, confirmButton = { TextButton(onClick = back) { Text("放弃修改") } },
        dismissButton = { TextButton(onClick = { discard = false }) { Text("继续编辑") } })
    editing?.let { index ->
        draft.items.getOrNull(index)?.let { original -> EditPlanDialog(original, { editing = null }, "修改保存在草稿中，点击页面底部按钮后生效。",
            draft.items.filterIndexed { i, _ -> i != index }.map { it.exerciseId }.toSet()) { changed ->
            change(draft.copy(items = draft.items.mapIndexed { i, p -> if (i == index) changed else p })); editing = null
        } }
    }
    adding?.let { section -> ExercisePicker(section, draft.items.map { it.exerciseId }.toSet(), { adding = null }) { e ->
        val item = PlanItem(e.id, e.sets, e.dose, section, optional = section == "可选有氧")
        change(draft.copy(items = (draft.items + item).sortedBy { PlanSections.indexOf(it.section) }))
        adding = null
    } }
}

@Composable private fun ExercisePicker(section: String, used: Set<String>, dismiss: () -> Unit, choose: (Exercise) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val choices = Catalog.all.filter { e -> e.reviewed && e.id !in used &&
        (when(section) {
            "肩背活动", "舒缓活动" -> e.category == Category.MOBILITY
            "力量训练" -> e.category in listOf(Category.UPPER, Category.LOWER, Category.CORE)
            "可选有氧" -> e.category == Category.CARDIO
            "放松走动" -> e.id == "walk" || e.id == "march"
            else -> true
        }) && (e.name.contains(query.trim(), true) || e.muscles.contains(query.trim(), true)) }
    AlertDialog(onDismissRequest = dismiss, title = { Text("添加动作") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(query, { query = it }, singleLine = true, label = { Text("搜索要添加的动作") })
            Text("已加入的动作不重复显示；候选动作暂不开放加入。", style = MaterialTheme.typography.bodySmall)
            LazyColumn(Modifier.heightIn(max = 360.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (choices.isEmpty()) item { Text("没有可添加的匹配动作") }
                items(choices, key = { it.id }) { e ->
                    TextButton(onClick = { choose(e) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.fillMaxWidth()) { Text(e.name); Text("${e.sets} 组 × ${e.dose} ${e.unit.label}", style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }, confirmButton = { TextButton(onClick = dismiss) { Text("取消") } })
}
