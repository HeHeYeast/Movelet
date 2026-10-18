package cn.movelet.app

import android.content.Context
import android.graphics.BitmapFactory
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.MessageDigest

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TeachingMediaTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    @Before fun loadCatalog() { TeachingMedia.initialize(context) }

    @Test fun allImagesHaveSourceReviewAndCorrectExerciseMapping() {
        val media = TeachingMedia.catalog
        assertEquals(60, media.size)
        assertEquals(60, media.map { it.path }.toSet().size)
        assertEquals(Catalog.all.map { it.id }.toSet() - PosterExercises.all.map { it.id }.toSet(), media.filter { it.isGenerated }.map { it.exerciseId }.toSet())
        PosterExercises.all.forEach { assertTrue(TeachingMedia.photos(it.id).isEmpty()) }
        assertEquals(24, media.filter { it.isGenerated }.size)
        assertEquals(24, media.map { it.exerciseId }.toSet().size)
        media.forEach {
            assertTrue(Catalog.all.any { e -> e.id == it.exerciseId })
            assertTrue(it.path.matches(Regex("(photos|generated)/[a-z0-9_-]+\\.(jpe?g|png)")))
            assertTrue(it.sha256.matches(Regex("[a-f0-9]{64}")))
            assertTrue(it.sourceUrl.startsWith("https://"))
            assertTrue(it.credit.isNotBlank() && it.caption.isNotBlank())
            if (it.isGenerated) {
                assertTrue(it.rights.contains("生成"))
                assertTrue(it.credit.contains("AI"))
                assertTrue(maxOf(it.width, it.height) >= 1200 && minOf(it.width, it.height) >= 800)
                assertFalse(it.phaseGuide.isNullOrBlank())
            } else assertTrue(it.rights.contains("未取得"))
            assertTrue(it.review.contains("未获真人"))
        }
        assertEquals(setOf("easier"), media.filter { it.exerciseId == "march" && !it.isGenerated }.map { it.relation }.toSet())
        assertTrue(media.first { it.exerciseId == "burpee" }.phaseGuide!!.contains("左上"))
        assertTrue(media.first { it.exerciseId == "thread" && !it.isGenerated }.caption.contains("只参考下半图"))
        assertFalse(media.filter { it.exerciseId == "bridge" }.any { it.sourceUrl.contains("acefitness") })
    }

    @Test fun packagedAssetsMatchAuditedHashesAndPublicExcludesThirdPartyPhotos() {
        TeachingMedia.catalog.forEach { photo ->
            if (BuildConfig.PERSONAL_MEDIA || photo.isGenerated) {
                val bytes = context.assets.open(photo.path).use { it.readBytes() }
                val hash = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
                assertEquals(photo.path, photo.sha256, hash)
                val size = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, size)
                assertEquals(photo.width, size.outWidth)
                assertEquals(photo.height, size.outHeight)
            } else {
                assertFalse(TeachingMedia.photos(photo.exerciseId).contains(photo))
                assertTrue(runCatching { context.assets.open(photo.path).close() }.isFailure)
            }
        }
        assertEquals(if (BuildConfig.PERSONAL_MEDIA) 36 else 0, context.assets.list("photos")?.size ?: 0)
        assertEquals(24, context.assets.list("generated")?.size ?: 0)
    }
}
