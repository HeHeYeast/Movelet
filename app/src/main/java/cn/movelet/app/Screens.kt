package cn.movelet.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import androidx.core.net.toUri

@Composable fun LibraryScreen(modifier: Modifier, detail: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("全部") }
    var beginner by rememberSaveable { mutableStateOf(false) }
    val results = Catalog.all.filter { e ->
        (category == "全部" || e.category.label == category) && (!beginner || e.difficulty == "基础") &&
            (query.isBlank() || "${e.name} ${e.english} ${e.muscles}".contains(query.trim(), ignoreCase = true))
    }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item { TitleBlock("学会动作，再慢慢加量", "动作库", "24 个动作 · 离线文字教学") }
        item { OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text("搜索动作或肌群") }, leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = { if (query.isNotEmpty()) IconButton(onClick = { query = "" }) { Icon(Icons.Default.Close, "清除搜索") } }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(20.dp)) }
        item { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (listOf("全部") + Category.entries.map { it.label }).forEach { label -> FilterChip(selected = category == label, onClick = { category = label }, label = { Text(label) }) }
        } }
        item { Row(verticalAlignment = Alignment.CenterVertically) {
            FilterChip(selected = beginner, onClick = { beginner = !beginner }, label = { Text("仅基础难度") })
            Spacer(Modifier.weight(1f)); Text("${results.size} 个结果", style = MaterialTheme.typography.labelMedium)
        } }
        if (results.isEmpty()) item { SoftCard { Text("没有找到动作"); Text("试试“肩”“核心”或英文名称，或清除筛选。") } }
        results.forEach { e -> item(key = e.id) { ExerciseRow(e, "${e.category.label} · ${e.difficulty}", if (!e.reviewed) "待复核 · 不进入默认计划" else "文字教学 · 图片待授权", { detail(e.id) }) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun ExerciseDetail(e: Exercise, back: () -> Unit, open: (String) -> Unit) {
    val context = LocalContext.current
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
        TopAppBar(title = { Text("动作教学") }, navigationIcon = { IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
    }) { padding ->
        LazyColumn(Modifier.padding(padding), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item { TitleBlock(e.english, e.name, "${e.category.label} · ${e.difficulty}") }
            item { SoftCard {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryTile(e.category, true)
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("教学图片待授权", fontWeight = FontWeight.SemiBold)
                        Text("当前提供文字步骤与专业原始资料。此图标仅标识分类。", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (!e.reviewed) Text("此条目尚待逐项复核，不加入默认计划。", color = MaterialTheme.colorScheme.error)
            } }
            item { SoftCard { Text("主要肌群", style = MaterialTheme.typography.labelLarge); Text(e.muscles); Text("初始参考：${e.sets} 组 × ${e.dose} ${e.unit.label}", color = MaterialTheme.colorScheme.primary) } }
            item { SectionTitle("01  准备姿势"); Spacer(Modifier.height(8.dp)); Text(e.setup, style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp) }
            item { SectionTitle("02  一步一步来") }
            e.steps.forEachIndexed { i, step -> item { SoftCard { Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Text("${i + 1}".padStart(2, '0'), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(step, style = MaterialTheme.typography.bodyLarge, lineHeight = 26.sp)
            } } } }
            item { SoftCard { Text("呼吸节奏", fontWeight = FontWeight.SemiBold); Text(e.breathing) } }
            item { SectionTitle("03  留意这些细节"); Spacer(Modifier.height(8.dp)); SoftCard {
                Text("常见错误", fontWeight = FontWeight.SemiBold)
                e.mistakes.forEach { Text("• $it") }
                HorizontalDivider(); Text("安全提醒", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold); Text(e.safety)
            } }
            item { SoftCard { Text("感觉吃力时", fontWeight = FontWeight.SemiBold); Text(e.easier); HorizontalDivider(); Text("稳定之后", fontWeight = FontWeight.SemiBold); Text(e.harder) } }
            item { SectionTitle("资料来源与素材状态") }
            e.sources.forEach { source -> item { SoftCard {
                Text(source.title, fontWeight = FontWeight.SemiBold); Text(source.scope, style = MaterialTheme.typography.bodyMedium)
                Text("查阅：${source.checkedOn}\n文字为中文独立编写，剂量为本 App 保守起点；未经真人专业审核。", style = MaterialTheme.typography.bodySmall)
                OutlinedButton(onClick = {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, source.url.toUri())) }
                    catch (_: ActivityNotFoundException) { Toast.makeText(context, "未找到可打开资料的浏览器", Toast.LENGTH_LONG).show() }
                }) { Icon(Icons.AutoMirrored.Filled.OpenInNew, null); Spacer(Modifier.width(8.dp)); Text("打开原始教学资料（联网）") }
                Text("素材：${e.media.license}\n本地路径：${e.media.localPath ?: "暂无"}", style = MaterialTheme.typography.bodySmall)
            } } }
            item { SectionTitle("也可以了解") }
            Catalog.all.filter { it.id != e.id && it.category == e.category && it.reviewed }.take(2).forEach { alt -> item {
                ExerciseRow(alt, alt.difficulty, click = { open(alt.id) })
            } }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun FocusScreen(session: Session, vm: MainViewModel, back: () -> Unit, teach: (String) -> Unit, finished: () -> Unit) {
    var finishDialog by rememberSaveable { mutableStateOf(false) }
    var simplifyDialog by remember { mutableStateOf(false) }
    val index = session.currentIndex
    val work = session.items.getOrNull(index)
    val e = work?.let { Catalog.get(it.plan.exerciseId) }
    var actual by remember(index, work?.completed?.size, work?.plan?.dose) { mutableIntStateOf(work?.plan?.dose ?: 1) }
    // Match the visible item before applying rapid repeated taps.
    fun mutate(transform: (Session) -> Session) = vm.change { current ->
        if (current.id == session.id && current.currentIndex == index && current.doneSets == session.doneSets) transform(current) else current
    }
    Scaffold(containerColor = MaterialTheme.colorScheme.background, topBar = {
        TopAppBar(title = { Text("专注训练") }, navigationIcon = { IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "暂停并返回") } },
            actions = { TextButton(onClick = { vm.pause(); finishDialog = true }) { Text("结束") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
    }) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(22.dp), verticalArrangement = Arrangement.spacedBy(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${session.doneSets} / ${session.targetSets} 组", style = MaterialTheme.typography.labelLarge)
                    Text("已练 ${clockText(session.seconds)}", style = MaterialTheme.typography.labelLarge)
                }; Spacer(Modifier.height(10.dp)); LinearProgressIndicator(progress = { (session.doneSets.toFloat() / session.targetSets.coerceAtLeast(1)).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth()) }
                if (work == null || e == null) {
                    item { CategoryTile(Category.MOBILITY, true); Spacer(Modifier.height(20.dp)); Text("今天的努力，记下来", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold) }
                    item { Text("实际完成 ${session.doneSets} 组。跳过的内容不会算作完成。") }
                } else {
                    item { Text(work.plan.section + if (session.simplified) " · 已简化" else "", color = MaterialTheme.colorScheme.primary); Spacer(Modifier.height(14.dp)); CategoryTile(e.category, true) }
                    item { Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(e.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                        Text("第 ${work.completed.size + 1} / ${work.plan.sets} 组", style = MaterialTheme.typography.titleMedium)
                        Text("${work.plan.dose} ${e.unit.label}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                        if (e.unit == DoseUnit.EACH_SIDE) Text("左右各做 ${work.plan.dose} 次，再完成这一组", style = MaterialTheme.typography.bodyMedium)
                        if (e.unit == DoseUnit.SECONDS) Text("时长目标，完成后确认实际秒数", style = MaterialTheme.typography.bodySmall)
                    } }
                    item { SoftCard { Text(if (session.rest > 0) "休息一下" else "动作提示", fontWeight = FontWeight.SemiBold)
                        if (session.rest > 0) {
                            Text(clockText(session.rest), style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.primary)
                            Row { TextButton(onClick = { mutate { it.copy(rest = (it.rest + 15).coerceAtMost(300)) } }) { Text("加 15 秒") }; TextButton(onClick = { mutate { it.copy(rest = 0) } }) { Text("跳过休息") } }
                        } else Text(e.steps.first(), lineHeight = 25.sp)
                        OutlinedButton(onClick = { vm.pause(); teach(e.id) }, modifier = Modifier.fillMaxWidth()) { Icon(Icons.AutoMirrored.Filled.MenuBook, null); Spacer(Modifier.width(8.dp)); Text("查看完整教学（暂停）") }
                    } }
                    if (session.rest == 0) item { SoftCard { Counter("实际${e.unit.label}", actual, 1, if (e.unit == DoseUnit.SECONDS) 120 else 30) { actual = it }; Text("少做了也如实记录，不必凑数。", style = MaterialTheme.typography.bodySmall) } }
                }
                if (session.paused && work != null) item { Text("已暂停 · 返回教学或切到后台会暂停计时", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium) }
            }
            Surface(shadowElevation = 4.dp) {
                Column(Modifier.padding(horizontal = 22.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(onClick = {
                        if (work == null) finishDialog = true
                        else if (session.paused) mutate { it.copy(paused = false) }
                        else mutate { Training.complete(it, actual) }
                    }, enabled = session.paused || session.rest == 0 || work == null, modifier = Modifier.fillMaxWidth().heightIn(min = 58.dp), shape = RoundedCornerShape(18.dp)) {
                        Text(if (work == null) "保存本次训练" else if (session.paused) "继续训练" else if (session.rest > 0) "休息中 · ${session.rest} 秒" else "完成这一组", fontSize = 18.sp)
                    }
                    if (work != null) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        if (!session.paused) TextButton(onClick = { vm.pause() }) { Text("暂停") }
                        TextButton(onClick = { simplifyDialog = true; vm.pause() }) { Text("简化训练") }
                        TextButton(onClick = { mutate(Training::skip) }) { Text("跳过动作") }
                    }
                }
            }
        }
    }
    if (simplifyDialog) AlertDialog(onDismissRequest = { simplifyDialog = false }, title = { Text("保留一点，轻松完成") }, text = { Text("未完成的必选动作减至 1 组，次数约减为原来的 2/3；可选内容跳过。已完成记录保留，记录会标记为“简化”。") },
        confirmButton = { TextButton(onClick = { mutate(Training::simplify); simplifyDialog = false }) { Text("简化剩余训练") } }, dismissButton = { TextButton(onClick = { simplifyDialog = false }) { Text("保留原计划") } })
    if (finishDialog) FinishDialog(session, { finishDialog = false }) { effort, pain ->
        vm.change { Training.finish(it, effort, pain) }; finishDialog = false; finished()
    }
}

@Composable fun FinishDialog(session: Session, dismiss: () -> Unit, save: (String, Boolean) -> Unit) {
    var effort by remember { mutableStateOf("适中") }
    var pain by remember { mutableStateOf<Boolean?>(null) }
    AlertDialog(onDismissRequest = dismiss, title = { Text("记录身体的反馈") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("已完成 ${session.doneSets} 组 · ${clockText(session.seconds)}。未做的组不会补记。")
            Text("这次感觉如何？")
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf("轻松", "适中", "吃力").forEach { label -> FilterChip(selected = effort == label, onClick = { effort = label }, label = { Text(label) }) } }
            Text("是否出现疼痛或原有不适加重？")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FilterChip(selected = pain == false, onClick = { pain = false }, label = { Text("没有") })
                FilterChip(selected = pain == true, onClick = { pain = true }, label = { Text("有") })
            }
            if (pain == true) Text("下一次先减量或避开诱发动作。持续或加重时寻求专业评估。", color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton(enabled = pain != null, onClick = { save(effort, pain!!) }) { Text("保存记录") } }, dismissButton = { TextButton(onClick = dismiss) { Text("返回（保持暂停）") } })
}

fun clockText(seconds: Int) = "%02d:%02d".format(seconds / 60, seconds % 60)

@Composable fun HistoryScreen(modifier: Modifier, today: LocalDate, sessions: List<Session>) {
    var selected by rememberSaveable { mutableStateOf(today.toString()) }
    var all by rememberSaveable { mutableStateOf(false) }
    var expanded by rememberSaveable { mutableStateOf<String?>(null) }
    val finished = sessions.filter { it.endedAt != null }
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    val week = finished.filter { it.date >= monday.toString() && it.date <= monday.plusDays(6).toString() }
    val visible = if (all) finished else finished.filter { it.date == selected }
    LazyColumn(modifier.fillMaxSize(), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { TitleBlock("每一点努力，都有记录", "训练记录", "只记录实际完成的内容") }
        item { SoftCard {
            Text("本周积累", style = MaterialTheme.typography.labelLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Stat("${week.filter { it.doneSets > 0 }.map { it.date }.distinct().size}", "训练天数")
                Stat("${week.sumOf { it.doneSets }}", "实际组数")
                Stat("${week.sumOf { it.seconds } / 60}", "分钟（取整）")
            }
        } }
        item { WeekStrip(today, LocalDate.parse(selected), { selected = it.toString(); all = false }, finished.filter { it.doneSets > 0 }.map { it.date }.toSet()) }
        item { Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(if (all) "全部记录" else selected, Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
            FilterChip(selected = all, onClick = { all = !all }, label = { Text("全部历史") })
        } }
        if (visible.isEmpty()) item { SoftCard { Icon(Icons.Default.Spa, null, tint = MaterialTheme.colorScheme.primary); Text("还没有结束的训练", style = MaterialTheme.typography.titleMedium); Text("完成或提前结束一场训练后，会在这里看到真实记录。") } }
        visible.forEach { s -> item(key = s.id) { Card(onClick = { expanded = if (expanded == s.id) null else s.id }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row { Text(s.title, Modifier.weight(1f), fontWeight = FontWeight.Bold); Text(s.status, color = MaterialTheme.colorScheme.primary) }
                Text("${s.date} · ${clockText(s.seconds)} · ${s.doneSets}/${s.targetSets} 组")
                Text("${s.effort} · ${if (s.pain == true) "有不适记录" else if (s.pain == false) "无痛反馈" else "未记录疼痛"}", style = MaterialTheme.typography.bodySmall)
                if (s.simplified) Text("原计划 ${s.originalSets} 组，已简化", style = MaterialTheme.typography.bodySmall)
                Text(if (expanded == s.id) "收起详情" else "查看各组实际次数", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                if (expanded == s.id) s.items.forEach { w ->
                    HorizontalDivider()
                    Text(Catalog.get(w.plan.exerciseId).name, fontWeight = FontWeight.Medium)
                    Text(if (w.completed.isEmpty()) (if (w.skipped) "已跳过 · 0 组" else "未完成 · 0 组") else w.completed.mapIndexed { i, set -> "第 ${i + 1} 组：${set.dose} ${set.unit.label}" }.joinToString("\n") + if (w.skipped) "\n剩余已跳过" else "", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } } }
        item { SoftCard { Text("下一步怎么练", fontWeight = FontWeight.SemiBold); Text(Training.progression(finished), lineHeight = 25.sp) } }
    }
}
@Composable private fun Stat(value: String, label: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) { Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary); Text(label, style = MaterialTheme.typography.labelMedium) }
}
