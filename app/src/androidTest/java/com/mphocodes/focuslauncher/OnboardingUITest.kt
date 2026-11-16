package com.mphocodes.focuslauncher

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mphocodes.focuslauncher.data.repository.SettingsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OnboardingUITest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun onboardingNextShowsFocusInterventionStep() {
        runBlocking {
            SettingsRepository(composeTestRule.activity.applicationContext)
                .setOnboardingComplete(false)
        }
        val homeTitle = composeTestRule.activity.getString(R.string.onboarding_home_title)
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithText(homeTitle).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText(homeTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.onboarding_next)
        ).performClick()
        composeTestRule.onNodeWithText(
            composeTestRule.activity.getString(R.string.onboarding_friction_title)
        ).assertIsDisplayed()
    }
}
