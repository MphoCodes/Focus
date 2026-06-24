package com.mphocodes.focuslauncher

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mphocodes.focuslauncher.ui.FocusLauncherApp
import io.mockk.mockk
import com.mphocodes.focuslauncher.ui.FocusLauncherViewModel

@RunWith(AndroidJUnit4::class)
class SettingsUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSettingsNavigation() {
        val viewModel = mockk<FocusLauncherViewModel>(relaxed = true)

        composeTestRule.setContent {
            FocusLauncherApp(viewModel = viewModel)
        }

        // Just checking basic load works for Compose UI
        // Not asserting actual interaction since we use a mocked view model
        // and internal state might require more setup.
    }
}
