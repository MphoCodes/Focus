package com.focuslauncher

import android.app.Application
import android.content.Context
import com.focuslauncher.data.model.InstalledApp
import com.focuslauncher.data.model.LauncherSettings
import com.focuslauncher.data.repository.AppsRepository
import com.focuslauncher.data.repository.SettingsRepository
import com.focuslauncher.ui.FocusLauncherViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuotaBlockTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockkConstructor(AppsRepository::class)
        mockkConstructor(SettingsRepository::class)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun testBlockIfQuotaExceeded_noQuota() = runTest {
        val app = mockk<Application>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        every { app.applicationContext } returns context

        val settingsFlow = MutableStateFlow(LauncherSettings())
        every { anyConstructed<SettingsRepository>().settings } returns settingsFlow
        every { anyConstructed<AppsRepository>().loadLaunchableApps() } returns emptyList()

        val viewModel = FocusLauncherViewModel(app)
        val targetApp = InstalledApp("com.test", "TestApp", "com.test", "MainActivity")

        // No quota set
        val result = viewModel.blockIfQuotaExceeded(targetApp)
        assertFalse(result)
    }

    @Test
    fun testBlockIfQuotaExceeded_hasQuota_noUsageAccess() = runTest {
        val app = mockk<Application>(relaxed = true)
        val context = mockk<Context>(relaxed = true)
        every { app.applicationContext } returns context

        val settingsFlow = MutableStateFlow(LauncherSettings(
            appDailyQuotaMinutes = mapOf("com.test" to 15)
        ))
        every { anyConstructed<SettingsRepository>().settings } returns settingsFlow
        every { anyConstructed<AppsRepository>().loadLaunchableApps() } returns emptyList()
        every { anyConstructed<AppsRepository>().hasUsageAccess() } returns false

        val viewModel = FocusLauncherViewModel(app)
        val targetApp = InstalledApp("com.test", "TestApp", "com.test", "MainActivity")

        // Wait for UI state to update
        testScheduler.advanceUntilIdle()

        val result = viewModel.blockIfQuotaExceeded(targetApp)
        assertTrue(result)
    }
}
