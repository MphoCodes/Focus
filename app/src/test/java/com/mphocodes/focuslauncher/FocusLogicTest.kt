package com.mphocodes.focuslauncher

import com.mphocodes.focuslauncher.data.model.LauncherSettings
import com.mphocodes.focuslauncher.data.model.InstalledApp
import com.mphocodes.focuslauncher.data.model.ProfileId
import com.mphocodes.focuslauncher.data.model.Workspace
import com.mphocodes.focuslauncher.data.repository.WorkspaceSerializer
import com.mphocodes.focuslauncher.notification.NotificationBatchDeliverer
import com.mphocodes.focuslauncher.ui.ContextSignal
import com.mphocodes.focuslauncher.ui.LauncherUiState
import com.mphocodes.focuslauncher.ui.resolveActiveContextProfile
import com.mphocodes.focuslauncher.ui.workspaceApps
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

    @Test
    fun testWorkspaceSerializer_roundTrip() {
        val workspaces = listOf(
            Workspace(id = "work", name = "Work", appIds = setOf("app.one/Main", "app.two/Main")),
            Workspace(id = "money", name = "Money", appIds = setOf("bank.app/Main"))
        )

        val decoded = WorkspaceSerializer.decode(WorkspaceSerializer.encode(workspaces))

        assertEquals("Work", decoded[0].name)
        assertEquals(setOf("app.one/Main", "app.two/Main"), decoded[0].appIds)
        assertEquals("Money", decoded[1].name)
        assertEquals(setOf("bank.app/Main"), decoded[1].appIds)
    }

    @Test
    fun testWorkspaceApps_hidesHiddenAppsAndSortsByDisplayLabel() {
        val camera = InstalledApp(id = "camera/Main", label = "Camera", packageName = "camera", className = "Main")
        val bank = InstalledApp(id = "bank/Main", label = "Bank", packageName = "bank", className = "Main")
        val mail = InstalledApp(id = "mail/Main", label = "Mail", packageName = "mail", className = "Main")
        val settings = LauncherSettings(
            hiddenAppIds = setOf(camera.id),
            workspaces = listOf(Workspace(id = "work", name = "Work", appIds = setOf(camera.id, bank.id, mail.id)))
        )
        val state = LauncherUiState(
            installedApps = listOf(mail, camera, bank),
            settings = settings
        )

        val result = workspaceApps(state, settings.workspaces.first())

        assertEquals(listOf(bank, mail), result)
    }
}
