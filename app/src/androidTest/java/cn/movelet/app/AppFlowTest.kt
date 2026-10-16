package cn.movelet.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppFlowTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun libraryTeachingTrainingAndRecordFlow() {
        if (compose.onAllNodesWithText("了解，按身体状态调整").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithText("了解，按身体状态调整").performClick()
        compose.onNodeWithText("动作库", useUnmergedTree = false).performClick()
        compose.onNodeWithText("搜索动作或肌群").performTextInput("死虫")
        compose.onNodeWithText("死虫式").performClick()
        if (BuildConfig.PERSONAL_MEDIA) compose.onNodeWithText("动作图解").assertExists()
        else compose.onNodeWithText("先读步骤，再看原始资料").assertExists()
        compose.onNodeWithTag("teaching-detail").performScrollToNode(hasText("02  一步一步来"))
        compose.onNodeWithText("02  一步一步来").assertIsDisplayed()
        compose.onNodeWithContentDescription("返回").performClick()
        compose.onNodeWithText("今日").performClick()
        compose.onNodeWithText("开始今天的训练").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("专注训练").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("完成这一组").performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithText("跳过休息").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("跳过休息").performScrollTo().performClick()
        compose.onNodeWithText("查看完整教学（暂停）").performScrollTo().performClick()
        compose.onNodeWithContentDescription("返回").performClick()
        compose.onNodeWithText("继续训练").assertExists()
        compose.onNodeWithText("结束").performClick()
        compose.onNodeWithText("没有").performClick()
        compose.onNodeWithText("保存记录").performClick()
        compose.waitUntil(10000) { compose.onAllNodesWithText("部分完成").fetchSemanticsNodes().isNotEmpty() }
        compose.onNodeWithText("部分完成").assertExists()
        compose.onNodeWithText("查看各组实际次数").performScrollTo().performClick()
        compose.onNodeWithText("第 1 组：3 次 / 侧").performScrollTo().assertIsDisplayed()
    }
}
