package cn.movelet.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.core.content.edit
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MoveletTheme { MoveletApp() } }
    }
}

private val Pine = Color(0xFF276553)
@Composable fun MoveletTheme(content: @Composable () -> Unit) {
    val colors = if (isSystemInDarkTheme()) darkColorScheme(primary = Color(0xFFADD5BD), onPrimary = Color(0xFF12382C), secondary = Color(0xFFCDDDAA),
        background = Color(0xFF151D18), surface = Color(0xFF1E2821), primaryContainer = Color(0xFF2D493A),
        onPrimaryContainer = Color(0xFFE2EEDD), secondaryContainer = Color(0xFF334336), onSecondaryContainer = Color(0xFFE3EBD8),
        onSurface = Color(0xFFE4E9E1), onSurfaceVariant = Color(0xFFB9C5B9), surfaceVariant = Color(0xFF354139))
    else lightColorScheme(primary = Pine, onPrimary = Color.White, primaryContainer = Color(0xFFE3EBD8),
        onPrimaryContainer = Color(0xFF233E31), secondary = Color(0xFF6A7758), secondaryContainer = Color(0xFFEEF0E7), onSecondaryContainer = Color(0xFF233E31),
        background = Color(0xFFF6F7F2), surface = Color(0xFFFEFFFA), surfaceVariant = Color(0xFFEDF0E7),
        onSurface = Color(0xFF23352C), onSurfaceVariant = Color(0xFF5C6A60))
    MaterialTheme(colorScheme = colors, shapes = Shapes(medium = RoundedCornerShape(20.dp), large = RoundedCornerShape(28.dp)), content = content)
}

@Composable fun MoveletApp(vm: MainViewModel = viewModel()) {
    val sessions by vm.sessions.collectAsStateWithLifecycle()
    val plans by vm.plans.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val active = sessions.firstOrNull { it.endedAt == null }
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var focus by rememberSaveable { mutableStateOf(false) }
    var detail by rememberSaveable { mutableStateOf<String?>(null) }
    var today by remember { mutableStateOf(LocalDate.now()) }
    var selected by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val prefs = LocalContext.current.getSharedPreferences("local_preferences", 0)
    var safety by rememberSaveable { mutableStateOf(!prefs.getBoolean("safety_seen", false)) }
    val owner = LocalLifecycleOwner.current
    DisposableEffect(owner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) vm.pause()
            if (event == Lifecycle.Event.ON_START) {
                val now = LocalDate.now()
                if (today != now) { today = now; selected = now.toString() }
            }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    LaunchedEffect(Unit) { while (true) { delay(60_000); val now = LocalDate.now(); if (now != today) { today = now; selected = now.toString() } } }
    BackHandler(detail != null || focus || tab != 0) {
        when { detail != null -> detail = null; focus -> { vm.pause(); focus = false }; else -> tab = 0 }
    }
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when {
            detail != null -> ExerciseDetail(Catalog.get(detail!!), { detail = null }, { detail = it })
            focus && active != null -> FocusScreen(active, vm, { vm.pause(); focus = false }, { detail = it }, { focus = false; tab = 2 })
            else -> Scaffold(containerColor = MaterialTheme.colorScheme.background, bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    listOf("今日" to Icons.Default.Today, "动作库" to Icons.Default.GridView, "记录" to Icons.Default.BarChart).forEachIndexed { i, pair ->
                        NavigationBarItem(selected = tab == i, onClick = { tab = i }, icon = { Icon(pair.second, null) }, label = { Text(pair.first) })
                    }
                }
            }) { padding ->
                val date = LocalDate.parse(selected)
                val plan = plans[selected] ?: Plans.forDate(date)
                when(tab) {
                    0 -> TodayScreen(Modifier.padding(padding), today, date, plan, sessions, active,
                        { selected = it.toString() }, { detail = it }, vm::edit,
                        { if (active == null) vm.start(plan); focus = true }, { safety = true })
                    1 -> LibraryScreen(Modifier.padding(padding)) { detail = it }
                    2 -> HistoryScreen(Modifier.padding(padding), today, sessions)
                }
            }
        }
    }
    if (safety) AlertDialog(onDismissRequest = { safety = false }, title = { Text("从舒服、可持续开始") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("每组留 2–4 次余力，恢复日可以短一点。你的肩背疼痛尚未评估，本计划不用于诊断或治疗。")
            Text("关节痛、刺痛或症状加重：停止该动作，选择无痛替代，必要时结束训练。持续疼痛、明显无力麻木或活动受限，应寻求专业评估；突发剧痛或外伤后无法活动时及时就医。")
            Text("第一轮提供离线文字教学。高清图片/视频尚待授权和专业复核，分类图标不是动作示范。原始教学资料需联网打开。")
        }
    }, confirmButton = { TextButton(onClick = { prefs.edit { putBoolean("safety_seen", true) }; safety = false }) { Text("了解，按身体状态调整") } })
    if (error != null) AlertDialog(onDismissRequest = { vm.error.value = null }, title = { Text("本地保存提示") }, text = { Text(error!!) }, confirmButton = { TextButton(onClick = { vm.error.value = null }) { Text("知道了") } })
}

@Composable fun TitleBlock(eyebrow: String, title: String, subtitle: String? = null, action: (@Composable () -> Unit)? = null) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(eyebrow, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge, letterSpacing = 1.sp)
            Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        action?.invoke()
    }
}
@Composable fun SectionTitle(title: String, subtitle: String? = null) {
    Column(Modifier.padding(top = 6.dp, bottom = 2.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}
@Composable fun SoftCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}
@Composable fun CategoryTile(category: Category, large: Boolean = false) {
    val icon: ImageVector = when(category) { Category.MOBILITY -> Icons.Default.Spa; Category.UPPER -> Icons.Default.FitnessCenter; Category.LOWER -> Icons.Default.Terrain; Category.CORE -> Icons.Default.CenterFocusStrong; Category.CARDIO -> Icons.Default.FavoriteBorder }
    Surface(shape = RoundedCornerShape(if (large) 28.dp else 16.dp), color = MaterialTheme.colorScheme.primaryContainer) {
        Box(Modifier.size(if (large) 96.dp else 58.dp), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = "${category.label}分类图标，非动作示范", modifier = Modifier.size(if (large) 42.dp else 26.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}
@Composable fun WeekStrip(today: LocalDate, selected: LocalDate, onSelect: (LocalDate) -> Unit, dates: Set<String> = emptySet()) {
    val monday = today.minusDays((today.dayOfWeek.value - 1).toLong())
    BoxWithConstraints(Modifier.fillMaxWidth()) {
    val cellWidth = maxOf(48.dp, (maxWidth - 18.dp) / 7)
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        repeat(7) { i ->
            val date = monday.plusDays(i.toLong())
            val chosen = date == selected
            Surface(onClick = { onSelect(date) }, modifier = Modifier.width(cellWidth), shape = RoundedCornerShape(16.dp),
                color = if (chosen) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                contentColor = if (chosen) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface) {
                Column(Modifier.padding(vertical = 10.dp).heightIn(min = 62.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(listOf("一", "二", "三", "四", "五", "六", "日")[i], style = MaterialTheme.typography.labelMedium)
                    Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(if (date.toString() in dates) "•" else if (date == today) "今" else " ", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
    }
}

@Composable fun TodayScreen(modifier: Modifier, today: LocalDate, date: LocalDate, plan: DayPlan,
    sessions: List<Session>, active: Session?, select: (LocalDate) -> Unit, detail: (String) -> Unit,
    edit: (DayPlan) -> Unit, start: () -> Unit, safety: () -> Unit) {
    var editing by remember { mutableStateOf<Int?>(null) }
    val latest = sessions.firstOrNull { it.date == date.toString() }
    Column(modifier.fillMaxSize()) {
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { TitleBlock("MOVELET  /  轻练", if (date == today) "今天，动一会儿" else "为这一天留点时间", date.format(DateTimeFormatter.ofPattern("M 月 d 日 · yyyy"))) {
                IconButton(onClick = safety) { Icon(Icons.Default.Info, "训练与素材说明") }
            } }
            item { WeekStrip(today, date, select, sessions.filter { it.doneSets > 0 }.map { it.date }.toSet()) }
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(28.dp)) {
                    Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(if (plan.strength) "循序渐进 · 第一阶段" else "恢复，也是训练的一部分", style = MaterialTheme.typography.labelLarge)
                        Text(plan.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                            Text(if (plan.strength) "约 20–25 分钟" else "约 6–15 分钟", fontWeight = FontWeight.SemiBold)
                            Text("${plan.items.filter { it.enabled }.sumOf { it.sets }} 组 · 可调整")
                        }
                        Text("肩背轻活动 5–7 分钟 · 每组保留余力", style = MaterialTheme.typography.bodyMedium)
                        if (latest != null) {
                            LinearProgressIndicator(progress = { (latest.doneSets.toFloat() / latest.targetSets.coerceAtLeast(1)).coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
                            Text("最近一场：${latest.status} · ${latest.doneSets}/${latest.targetSets} 组", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            if (active != null && active.date != date.toString()) item { Text("有一场 ${active.date} 的训练待继续或结束。", color = MaterialTheme.colorScheme.primary) }
            plan.items.map { it.section }.distinct().forEach { section ->
                item { SectionTitle(section, when(section) { "肩背活动" -> "每天保留 · 在舒适范围慢慢做"; "可选有氧" -> "默认关闭 · 不影响力量训练完成"; else -> "动作不适时可调整或跳过" }) }
                plan.items.forEachIndexed { index, item ->
                    if (item.section == section) item(key = "$section-$index") {
                        val e = Catalog.get(item.exerciseId)
                        val matchesSnapshot = latest?.items?.map { it.plan } == plan.items.filter { it.enabled }
                        val work = if (matchesSnapshot && item.enabled) latest?.items?.getOrNull(plan.items.take(index).count { it.enabled }) else null
                        ExerciseRow(e, "${item.sets} 组 × ${item.dose} ${e.unit.label}",
                            if (!item.enabled) "未加入" else if (work != null) "${work.completed.size}/${work.plan.sets} 组${if (work.skipped) " · 已跳过" else ""}" else "待开始",
                            { detail(e.id) }) {
                            if (item.optional) Switch(checked = item.enabled, onCheckedChange = if (active == null) { enabled -> edit(plan.copy(items = plan.items.mapIndexed { i, p -> if (i == index) p.copy(enabled = enabled) else p })) } else null)
                            IconButton(enabled = active == null, onClick = { editing = index }) { Icon(Icons.Default.Tune, "调整${e.name}") }
                        }
                    }
                }
            }
            item { Text("痛感不是训练目标。关节痛或症状加重时停止该动作。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Surface(shadowElevation = 4.dp) {
            Button(onClick = start, enabled = active != null || date == today,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).heightIn(min = 56.dp), shape = RoundedCornerShape(18.dp)) {
                Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(8.dp))
                Text(if (active != null) "继续训练" else if (date != today) "仅可在当天开始训练" else if (latest != null) "再练一场（新增记录）" else "开始今天的训练", fontSize = 17.sp)
            }
        }
    }
    editing?.let { index -> EditPlanDialog(plan.items[index], { editing = null }) { changed ->
        edit(plan.copy(items = plan.items.mapIndexed { i, p -> if (i == index) changed else p })); editing = null
    } }
}

@Composable fun ExerciseRow(e: Exercise, subtitle: String, status: String? = null, click: () -> Unit, actions: (@Composable RowScope.() -> Unit)? = null) {
    Card(onClick = click, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(20.dp)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryTile(e.category)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(e.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                }
                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (status != null || actions != null) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(status.orEmpty(), Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                actions?.invoke(this)
            }
        }
    }
}

@Composable fun Counter(label: String, value: Int, min: Int, max: Int, change: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f))
        IconButton(enabled = value > min, onClick = { change(value - 1) }) { Icon(Icons.Default.Remove, "减少$label") }
        Text(value.toString(), fontWeight = FontWeight.Bold)
        IconButton(enabled = value < max, onClick = { change(value + 1) }) { Icon(Icons.Default.Add, "增加$label") }
    }
}
@Composable fun EditPlanDialog(original: PlanItem, dismiss: () -> Unit, save: (PlanItem) -> Unit) {
    var item by remember { mutableStateOf(original) }
    val e = Catalog.get(item.exerciseId)
    AlertDialog(onDismissRequest = dismiss, title = { Text("调整训练") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(e.name, fontWeight = FontWeight.Bold)
            Counter("组数", item.sets, 1, 5) { item = item.copy(sets = it) }
            Counter("每组${e.unit.label}", item.dose, 1, if (e.unit == DoseUnit.SECONDS) 120 else 30) { item = item.copy(dose = it) }
            Text("替换为同类动作", style = MaterialTheme.typography.labelLarge)
            Catalog.all.filter { it.category == e.category && it.reviewed }.forEach { alt ->
                TextButton(onClick = { item = Plans.substitute(item, alt.id) }, modifier = Modifier.fillMaxWidth()) {
                    Text((if (alt.id == item.exerciseId) "✓ " else "") + alt.name)
                }
            }
            Text("替换后会重设建议次数。只影响这一天；训练开始后保存为独立快照。", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton(onClick = { save(Plans.safe(item)) }) { Text("保存") } }, dismissButton = { TextButton(onClick = dismiss) { Text("取消") } })
}
