package cn.movelet.app

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class PlanEditorTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()
    @Test fun editTodayAndWeeklyTemplateSaveCancelAndSurviveRecreation() {
        if (compose.onAllNodesWithText("了解，按身体状态调整").fetchSemanticsNodes().isNotEmpty())
            compose.onNodeWithText("了解，按身体状态调整").performClick()
        compose.onNodeWithTag("today-list").performScrollToNode(hasText("编辑今日动作"))
        compose.onNodeWithText("编辑今日动作").performClick()
        compose.onNodeWithTag("plan-title").performTextReplacement("我的今日训练")
        compose.onNodeWithText("清空动作，设为休息日").performScrollTo().performClick()
        compose.onNodeWithTag("add-力量训练").performScrollTo().performClick()
        compose.onNodeWithText("搜索要添加的动作").performTextInput("鸟狗")
        compose.onNodeWithText("鸟狗式").performClick()
        compose.onNodeWithContentDescription("调整鸟狗式").performScrollTo().performClick()
        compose.onNodeWithContentDescription("增加组数").performClick()
        compose.onNodeWithText("保存").performClick()
        compose.onNodeWithTag("add-力量训练").performScrollTo().performClick()
        compose.onNodeWithText("搜索要添加的动作").performTextInput("死虫")
        compose.onNodeWithText("死虫式").performClick()
        compose.onNodeWithContentDescription("上移死虫式").performScrollTo().performClick()
        compose.onNodeWithTag("add-力量训练").performScrollTo().performClick()
        compose.onNodeWithText("搜索要添加的动作").performTextInput("臀桥")
        compose.onNode(hasText("臀桥") and !hasSetTextAction()).performClick()
        compose.onNodeWithContentDescription("删除臀桥").performScrollTo().performClick()
        compose.onNodeWithTag("save-plan").performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithTag("save-plan").fetchSemanticsNodes().isEmpty() }
        val repo = Repository((compose.activity.application as MoveletApplication).db)
        runBlocking {
            val saved = repo.plans.first().getValue(LocalDate.now().toString())
            assertEquals(listOf("dead_bug", "bird_dog"), saved.items.map { it.exerciseId })
            assertEquals(3, saved.items.last().sets)
        }
        compose.activityRule.scenario.recreate()
        compose.onNodeWithText("我的今日训练").assertExists()
        compose.onNodeWithTag("today-list").performScrollToNode(hasText("编辑每周安排"))
        compose.onNodeWithText("编辑每周安排").performClick()
        compose.onNodeWithTag("week-1").performScrollTo().performClick()
        compose.onNodeWithTag("plan-title").performTextReplacement("每周一的新安排")
        compose.onNodeWithTag("save-plan").performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithTag("save-plan").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithTag("week-1").performScrollTo().performClick()
        compose.onNodeWithTag("plan-title").assertTextContains("每周一的新安排")
        compose.onNodeWithTag("plan-title").performTextReplacement("不要保存这次修改")
        compose.onNodeWithContentDescription("取消编辑").performClick()
        compose.onNodeWithText("放弃修改").performClick()
        compose.onNodeWithTag("week-1").performScrollTo().performClick()
        compose.onNodeWithTag("plan-title").assertTextContains("每周一的新安排")
        // Leave defaults for the other instrumentation tests.
        compose.onNodeWithText("恢复默认安排").performScrollTo().performClick()
        compose.onNodeWithTag("save-plan").performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithTag("save-plan").fetchSemanticsNodes().isEmpty() }
        runBlocking { assertTrue(1 !in repo.templates.first()) }
        compose.onNodeWithContentDescription("返回").performClick()
        compose.onNodeWithTag("today-list").performScrollToNode(hasText("编辑今日动作"))
        compose.onNodeWithText("编辑今日动作").performClick()
        compose.onNodeWithText("清空动作，设为休息日").performScrollTo().performClick()
        compose.onNodeWithTag("save-plan").performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithTag("save-plan").fetchSemanticsNodes().isEmpty() }
        compose.onNodeWithText("今天休息 · 可添加动作").assertIsNotEnabled()
        compose.onNodeWithTag("today-list").performScrollToNode(hasText("编辑今日动作"))
        compose.onNodeWithText("编辑今日动作").performClick()
        compose.onNodeWithText("恢复每周安排").performScrollTo().performClick()
        compose.onNodeWithTag("save-plan").performClick()
        compose.waitUntil(5000) { compose.onAllNodesWithTag("save-plan").fetchSemanticsNodes().isEmpty() }
        runBlocking { assertTrue(LocalDate.now().toString() !in repo.plans.first()) }
    }
}
