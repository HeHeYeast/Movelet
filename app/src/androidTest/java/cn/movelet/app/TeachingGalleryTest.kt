package cn.movelet.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeachingGalleryTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun everyExerciseLoadsItsOwnImageOrExplicitTextTeaching() {
        if (compose.onAllNodesWithText("了解，按身体状态调整").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithText("了解，按身体状态调整").performClick()
        compose.onNodeWithText("动作库").performClick()
        Catalog.all.forEach { exercise ->
            compose.onNodeWithText("搜索动作或肌群").performTextClearance()
            compose.onNodeWithText("搜索动作或肌群").performTextInput(exercise.name)
            compose.onNode(hasText(exercise.name) and !hasSetTextAction()).performClick()
            val photo = TeachingMedia.photos(exercise.id).firstOrNull { it.isGenerated }
            if (photo == null) {
                compose.onNodeWithText("先读步骤，再看原始资料").assertExists()
                compose.onNodeWithTag("teaching-detail").performScrollToNode(hasText("02  一步一步来"))
                compose.onNodeWithText("02  一步一步来").assertExists()
                compose.onNodeWithContentDescription("返回").performClick()
                return@forEach
            }
            compose.waitUntil(10000) {
                compose.onAllNodesWithContentDescription("${photo.title}：${photo.caption}", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("AI 动作示意").assertExists()
            if (exercise.id in listOf("burpee", "floor_burpee", "reverse_lunge")) {
                compose.onNodeWithTag("teaching-detail").performScrollToNode(hasText(photo.phaseGuide!!, substring = true))
                compose.onNodeWithText(photo.phaseGuide!!, substring = true).assertExists()
            }
            compose.onNodeWithContentDescription("返回").performClick()
        }
    }
    @Test fun offlineGalleryCanPageZoomAndReturnWithoutChangingTraining() {
        if (compose.onAllNodesWithText("了解，按身体状态调整").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithText("了解，按身体状态调整").performClick()
        compose.onNodeWithText("动作库").performClick()
        compose.onNodeWithText("搜索动作或肌群").performTextInput("死虫")
        compose.onNodeWithText("死虫式").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithTag("loaded-photo", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("AI 动作示意").assertExists()
        compose.onNodeWithText("1 / 1").assertExists()
        compose.onNodeWithText("放大查看").performClick()
        compose.onNodeWithText("放大 2 倍").performClick()
        compose.onNodeWithText("还原").assertExists()
        compose.onNodeWithContentDescription("关闭大图").performClick()
        if (!BuildConfig.PERSONAL_MEDIA) {
            compose.onNodeWithText("原图对照").assertDoesNotExist()
            return
        }
        compose.onNodeWithText("原图对照").performClick()
        compose.onNodeWithText("1 / 4").assertExists()
        compose.onNodeWithContentDescription("下一张").performClick()
        compose.waitForIdle()
        compose.onNodeWithText("2 / 4").assertExists()
        compose.onNodeWithText("放大查看").performClick()
        compose.onNodeWithTag("photo-viewer").assertIsDisplayed()
        compose.onNodeWithText("放大 2 倍").performClick()
        compose.onNodeWithText("还原").assertExists()
        compose.onNodeWithContentDescription("大图下一张").performClick()
        compose.onNodeWithText("放大 2 倍").assertExists()
        compose.onNodeWithContentDescription("关闭大图").performClick()
        compose.onNodeWithText("3 / 4").assertExists()
        compose.onNodeWithText("AI 分步示意").performClick()
        compose.onNodeWithText("1 / 1").assertExists()
        compose.onNodeWithText("AI 动作示意").assertExists()
        compose.onNodeWithContentDescription("返回").performClick()
        compose.onNodeWithText("搜索动作或肌群").performTextClearance()
        compose.onNodeWithText("搜索动作或肌群").performTextInput("踏步")
        compose.onNodeWithText("原地轻踏步").performClick()
        compose.onNodeWithText("AI 动作示意").assertExists()
        compose.onNodeWithText("原图对照").performClick()
        compose.onNodeWithText("降阶参考：坐姿抬脚").assertExists()
    }
}
