package cn.movelet.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeachingGalleryTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun offlineGalleryCanPageZoomAndReturnWithoutChangingTraining() {
        assumeTrue(BuildConfig.PERSONAL_MEDIA)
        if (compose.onAllNodesWithText("了解，按身体状态调整").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithText("了解，按身体状态调整").performClick()
        compose.onNodeWithText("动作库").performClick()
        compose.onNodeWithText("搜索动作或肌群").performTextInput("死虫")
        compose.onNodeWithText("死虫式").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithTag("loaded-photo", useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty() }
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
        compose.onNodeWithContentDescription("返回").performClick()
        compose.onNodeWithText("搜索动作或肌群").performTextClearance()
        compose.onNodeWithText("搜索动作或肌群").performTextInput("踏步")
        compose.onNodeWithText("原地轻踏步").performClick()
        compose.onNodeWithText("降阶参考：坐姿抬脚").assertExists()
    }
}
