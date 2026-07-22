package edu.lzjtu.environment.oildepot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun opensPredictionWorkflowFromBottomNavigation() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("态势").fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("预测").performClick()
        composeRule.onNodeWithText("扩散预测").assertIsDisplayed()
        composeRule.onNodeWithText("提交预测任务").assertIsDisplayed()
    }
}
