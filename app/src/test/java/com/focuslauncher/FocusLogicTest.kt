package com.focuslauncher

import com.focuslauncher.data.model.LauncherSettings
import com.focuslauncher.data.model.ProfileId
import com.focuslauncher.notification.NotificationBatchDeliverer
import com.focuslauncher.ui.ContextSignal
import com.focuslauncher.ui.resolveActiveContextProfile
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class FocusLogicTest {

    @Test
    fun testResolveActiveContextProfile_disabled() {
        val settings = LauncherSettings(contextProfilesEnabled = false)
        val signal = ContextSignal()
        val result = resolveActiveContextProfile(settings, signal)
        assertEquals(ProfileId.DEFAULT, result)
    }

    @Test
    fun testResolveActiveContextProfile_wifiMatch() {
        val settings = LauncherSettings(
            contextProfilesEnabled = true,
            contextProfilesAutoSwitch = true,
            workProfileWifiSsid = "CorpNet"
        )
        val signal = ContextSignal(wifiSsid = "CorpNet")
        val result = resolveActiveContextProfile(settings, signal)
        assertEquals(ProfileId.WORK, result)
    }

    @Test
    fun testNotificationBatchToken() {
        // Before 12 PM -> null
        val morning = LocalDateTime.of(2023, 10, 1, 9, 0)
        assertEquals(null, NotificationBatchDeliverer.currentBatchToken(morning))

        // After 12 PM, before 6 PM -> 12
        val afternoon = LocalDateTime.of(2023, 10, 1, 14, 0)
        assertEquals("20231001-12", NotificationBatchDeliverer.currentBatchToken(afternoon))

        // After 6 PM -> 18
        val evening = LocalDateTime.of(2023, 10, 1, 20, 0)
        assertEquals("20231001-18", NotificationBatchDeliverer.currentBatchToken(evening))
    }
}
