package edu.lzjtu.environment.oildepot

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
        waitForDashboard()

        composeRule.onNodeWithText("预测").performClick()
        composeRule.onNodeWithText("扩散预测").assertIsDisplayed()
        composeRule.onNodeWithText("提交预测任务").assertIsDisplayed()
    }

    @Test
    fun opensPointMapAndShowsAlarmDetails() {
        waitForDashboard()

        composeRule.onNodeWithText("点位").performClick()
        composeRule.onNodeWithTag("point-map-container").assertIsDisplayed()
        waitForMapStyle()
        composeRule.onNodeWithTag("point-filter-SOIL").assertIsDisplayed()
        composeRule.onNodeWithTag("point-card-AIR-C-01").performScrollTo().performClick()
        composeRule.onNodeWithTag("point-detail-card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("point-detail-status").assertTextEquals("预警")
        composeRule.onNodeWithText("苯", substring = true).assertIsDisplayed()
    }

    @Test
    fun filtersRefinedDepotToOfflineSoilPoint() {
        waitForDashboard()

        composeRule.onNodeWithText("点位").performClick()
        composeRule.onNodeWithText("成品油库").performClick()
        composeRule.onNodeWithTag("point-filter-SOIL").performClick()
        composeRule.onNodeWithTag("point-card-SOIL-R-01").performScrollTo().performClick()
        composeRule.onNodeWithTag("point-card-SRC-R-01").assertDoesNotExist()
        composeRule.onNodeWithTag("point-detail-card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("point-detail-status").assertTextEquals("离线")
    }

    private fun waitForDashboard() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("点位").fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForMapStyle() {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodesWithTag("point-map-ready").fetchSemanticsNodes().isNotEmpty()
        }
    }
}
