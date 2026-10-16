package cn.movelet.app

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class TeachingPhoto(
    val exerciseId: String, val path: String, val title: String, val caption: String,
    val credit: String, val sourceUrl: String, val url: String, val relation: String,
    val width: Int, val height: Int, val sha256: String, val checkedOn: String,
    val rights: String, val review: String
) {
    val isGenerated: Boolean get() = path.startsWith("generated/")
}

object TeachingMedia {
    var catalog: List<TeachingPhoto> = emptyList()
        private set
    fun initialize(context: Context) {
        catalog = context.assets.open("teaching-media.json").bufferedReader().use {
            Gson().fromJson(it, Array<TeachingPhoto>::class.java).toList()
        }
    }
    fun photos(id: String) = catalog.filter { it.exerciseId == id && (it.isGenerated || BuildConfig.PERSONAL_MEDIA) }
    fun summary(id: String): String {
        val pictures = photos(id)
        return when {
            pictures.isEmpty() -> "文字步骤 · 原始资料"
            pictures.any { it.isGenerated } -> "AI 分步示意 · 可放大"
            pictures.all { it.relation == "easier" } -> "降阶参考图 · ${pictures.size} 张"
            else -> "离线图解 · ${pictures.size} 张"
        }
    }
}

private object PhotoCache {
    // Bitmap memory, not compressed file bytes. No network requests or disk cache.
    private val cache = object : LruCache<String, Bitmap>(12 * 1024 * 1024) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }
    fun load(context: Context, path: String, maxSide: Int): Bitmap? {
        val key = "$path:$maxSide"
        cache.get(key)?.let { return it }
        return runCatching {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > maxSide * 2) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            context.assets.open(path).use { BitmapFactory.decodeStream(it, null, options) }
                ?.also { cache.put(key, it) }
        }.getOrNull()
    }
}

@Composable private fun LocalPhoto(photo: TeachingPhoto, modifier: Modifier, small: Boolean = false) {
    val context = LocalContext.current.applicationContext
    var ready by remember(photo.path) { mutableStateOf(false) }
    val bitmap by produceState<Bitmap?>(null, photo.path, small) {
        ready = false
        value = withContext(Dispatchers.IO) { PhotoCache.load(context, photo.path, if (small) 240 else 1600) }
        ready = true
    }
    Box(modifier.background(Color.White), contentAlignment = Alignment.Center) {
        bitmap?.let { Image(it.asImageBitmap(), "${photo.title}：${photo.caption}", Modifier.fillMaxSize().testTag("loaded-photo"),
            contentScale = if (small && photo.isGenerated) ContentScale.Crop else ContentScale.Fit,
            alignment = if (small && photo.isGenerated) Alignment.CenterStart else Alignment.Center) }
            ?: if (ready) Text("图片暂不可用\n请查看文字步骤", color = Color(0xFF41584B))
            else CircularProgressIndicator(Modifier.size(22.dp), color = Color(0xFF276553))
    }
}

@Composable fun ExerciseThumbnail(e: Exercise) {
    val photo = TeachingMedia.photos(e.id).firstOrNull { it.relation == "main" }
    if (photo == null) CategoryTile(e.category)
    else LocalPhoto(photo, Modifier.size(width = 82.dp, height = 68.dp).clip(RoundedCornerShape(14.dp)), small = true)
}

@Composable fun TeachingGallery(e: Exercise, compact: Boolean = false) {
    val available = TeachingMedia.photos(e.id)
    var showOriginals by rememberSaveable(e.id) { mutableStateOf(false) }
    val generated = available.filter { it.isGenerated }
    val originals = available.filterNot { it.isGenerated }
    val photos = if (generated.isNotEmpty() && !showOriginals) generated else originals
    if (available.isEmpty()) {
        if (!compact) SoftCard {
            Text("先读步骤，再看原始资料", fontWeight = FontWeight.SemiBold)
            Text(if (BuildConfig.PERSONAL_MEDIA) "尚未找到与本版本一致的示范图片。可以先了解下方降阶动作。" else "本安装包提供文字教学，原始资料可在下方打开。")
        }
        return
    }
    key(e.id, showOriginals) {
        val pager = rememberPagerState { photos.size }
        val scope = rememberCoroutineScope()
        var expanded by rememberSaveable { mutableStateOf(false) }
        val current = photos[pager.currentPage]
        SoftCard {
            if (generated.isNotEmpty() && originals.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !showOriginals, onClick = { showOriginals = false }, label = { Text("AI 分步示意") })
                FilterChip(selected = showOriginals, onClick = { showOriginals = true }, label = { Text("原图对照") })
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(if (current.isGenerated) "AI 动作示意" else "动作图解", Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                Text("${pager.currentPage + 1} / ${photos.size}", style = MaterialTheme.typography.labelLarge)
            }
            HorizontalPager(pager, Modifier.fillMaxWidth().testTag("teaching-gallery")) { index ->
                LocalPhoto(photos[index], Modifier.fillMaxWidth()
                    .then(if (photos[index].isGenerated) Modifier.aspectRatio(photos[index].width.toFloat() / photos[index].height) else Modifier.height(if (compact) 180.dp else 235.dp))
                    .clip(RoundedCornerShape(16.dp)).clickable(onClickLabel = "放大图片") { expanded = true })
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(enabled = pager.currentPage > 0, onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }) { Icon(Icons.Default.ChevronLeft, "上一张") }
                TextButton(onClick = { expanded = true }) { Icon(Icons.Default.ZoomIn, null); Spacer(Modifier.width(6.dp)); Text("放大查看") }
                IconButton(enabled = pager.currentPage < photos.lastIndex, onClick = { scope.launch { pager.animateScrollToPage(pager.currentPage + 1) } }) { Icon(Icons.Default.ChevronRight, "下一张") }
            }
            Text(current.title, fontWeight = FontWeight.SemiBold)
            Text(current.caption, style = MaterialTheme.typography.bodyMedium)
            if (current.isGenerated) Text("左图：起始 · 右图：动作中\n已对照资料核对要点，非真人示范或专业签核。", style = MaterialTheme.typography.labelMedium)
            Text(current.credit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (expanded) PhotoViewer(photos, pager.currentPage, { expanded = false }) { index -> scope.launch { pager.scrollToPage(index) } }
        }
    }
}

@Composable private fun PhotoViewer(photos: List<TeachingPhoto>, initial: Int, close: () -> Unit, changed: (Int) -> Unit) {
    // Read the activity's raw insets: the surrounding Scaffold has already consumed them.
    val viewerInsets = WindowInsets.safeDrawing.asPaddingValues()
    var index by rememberSaveable { mutableIntStateOf(initial) }
    var zoom by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    fun move(delta: Int) { index = (index + delta).coerceIn(photos.indices); zoom = 1f; offset = Offset.Zero; changed(index) }
    Dialog(onDismissRequest = close, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        Surface(Modifier.fillMaxSize().testTag("photo-viewer"), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.padding(viewerInsets).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${index + 1} / ${photos.size} · ${photos[index].title}", Modifier.weight(1f), fontWeight = FontWeight.Bold)
                    IconButton(onClick = close) { Icon(Icons.Default.Close, "关闭大图") }
                }
                Box(Modifier.fillMaxWidth().weight(1f).clipToBounds().background(Color.White)
                    .pointerInput(index) { detectTransformGestures { _, pan, factor, _ ->
                        zoom = (zoom * factor).coerceIn(1f, 4f)
                        val limitX = size.width * (zoom - 1) / 2
                        val limitY = size.height * (zoom - 1) / 2
                        offset = Offset((offset.x + pan.x).coerceIn(-limitX, limitX), (offset.y + pan.y).coerceIn(-limitY, limitY))
                    } }) {
                    LocalPhoto(photos[index], Modifier.fillMaxSize().graphicsLayer { scaleX = zoom; scaleY = zoom; translationX = offset.x; translationY = offset.y })
                }
                Column(Modifier.heightIn(max = 150.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(photos[index].caption)
                    Text("${photos[index].credit}\n原图 ${photos[index].width} × ${photos[index].height} · 双指缩放、拖动查看", style = MaterialTheme.typography.bodySmall)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(enabled = index > 0, onClick = { move(-1) }) { Icon(Icons.Default.ChevronLeft, "大图上一张") }
                    TextButton(onClick = { zoom = if (zoom > 1f) 1f else 2f; offset = Offset.Zero }) { Text(if (zoom > 1f) "还原" else "放大 2 倍") }
                    IconButton(enabled = index < photos.lastIndex, onClick = { move(1) }) { Icon(Icons.Default.ChevronRight, "大图下一张") }
                }
            }
        }
    }
}
