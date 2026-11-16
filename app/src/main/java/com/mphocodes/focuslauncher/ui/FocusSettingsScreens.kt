package com.mphocodes.focuslauncher.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mphocodes.focuslauncher.R
import com.mphocodes.focuslauncher.data.model.InstalledApp
import kotlinx.coroutines.delay
import com.mphocodes.focuslauncher.ui.theme.FocusSecondary
import com.mphocodes.focuslauncher.ui.theme.FocusMuted
import com.mphocodes.focuslauncher.ui.theme.LocalFocusPrimary

@Composable
internal fun AppDisguiseScreen(
    uiState: LauncherUiState,
    onSearchChange: (String) -> Unit,
    onDisguiseChange: (InstalledApp, String) -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.Disguises, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        Column(Modifier.fillMaxSize()) {
            FocusText(
                text = stringResource(R.string.disguises_title),
                color = LocalFocusPrimary.current,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            FocusText(
                text = stringResource(R.string.disguises_desc),
                color = FocusMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(28.dp))
            SearchInput(
                value = uiState.searchQuery,
                placeholder = stringResource(R.string.selector_search_placeholder),
                onValueChange = onSearchChange
            )
            Spacer(Modifier.height(18.dp))

            val apps = selectorApps(uiState, includeHidden = true, includeOriginalLabel = true)
            LazyColumn(Modifier.fillMaxSize()) {
                items(apps, key = { it.id }) { app ->
                    DisguiseRow(
                        app = app,
                        value = uiState.settings.appDisguises[app.id].orEmpty(),
                        onValueChange = { onDisguiseChange(app, it) }
                    )
                }
                if (apps.isEmpty()) {
                    item { MutedEmptyText(stringResource(R.string.home_no_apps_found)) }
                }
            }
        }
    }
}

@Composable
internal fun AppQuotaScreen(
    uiState: LauncherUiState,
    onSearchChange: (String) -> Unit,
    onCycleQuota: (InstalledApp) -> Unit,
    onOpenUsageAccess: () -> Unit,
    onRefreshUsages: () -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.Quotas, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            while (true) {
                onRefreshUsages()
                kotlinx.coroutines.delay(5_000)
            }
        }
        val apps = selectorApps(uiState, includeHidden = true)
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                FocusText(
                    text = stringResource(R.string.quotas_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                FocusText(
                    text = stringResource(R.string.quotas_desc),
                    color = FocusMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(24.dp))
                SettingRow(
                    label = stringResource(R.string.quotas_usage_access),
                    value = stringResource(if (uiState.usageAccessGranted) R.string.quotas_usage_access_granted else R.string.quotas_usage_access_required),
                    onClick = onOpenUsageAccess
                )
                Spacer(Modifier.height(18.dp))
                SearchInput(
                    value = uiState.searchQuery,
                    placeholder = stringResource(R.string.selector_search_placeholder),
                    onValueChange = onSearchChange
                )
                Spacer(Modifier.height(16.dp))
            }

            items(apps, key = { it.id }) { app ->
                val minutes = uiState.settings.appDailyQuotaMinutes[app.id] ?: 0
                val usedMillis = uiState.quotaUsages[app.id] ?: 0L
                QuotaRow(
                    label = appDisplayLabel(app, uiState.settings),
                    minutes = minutes,
                    usedMillis = usedMillis,
                    onClick = { onCycleQuota(app) }
                )
            }

            if (apps.isEmpty()) {
                item { MutedEmptyText(stringResource(R.string.home_no_apps_found)) }
            }
        }
    }
}

@Composable
internal fun ContextProfilesScreen(
    uiState: LauncherUiState,
    onToggleEnabled: () -> Unit,
    onToggleAutoSwitch: () -> Unit,
    onCycleManualProfile: () -> Unit,
    onSetProfileWifiSsid: (String, String) -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.ContextProfiles, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                FocusText(
                    text = stringResource(R.string.profiles_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                FocusText(
                    text = stringResource(R.string.profiles_desc),
                    color = FocusMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(24.dp))

                ToggleRow(
                    label = stringResource(R.string.profiles_enabled),
                    selected = uiState.settings.contextProfilesEnabled,
                    onClick = onToggleEnabled
                )
                ToggleRow(
                    label = stringResource(R.string.profiles_auto_switch),
                    selected = uiState.settings.contextProfilesAutoSwitch,
                    onClick = onToggleAutoSwitch
                )
                SettingRow(
                    label = stringResource(R.string.profiles_active),
                    value = displayProfileName(uiState.activeContextProfileId)
                )
                SettingRow(
                    label = stringResource(R.string.profiles_manual),
                    value = displayProfileName(uiState.settings.manualContextProfileId),
                    onClick = onCycleManualProfile
                )
                Spacer(Modifier.height(24.dp))

                SettingsSection(stringResource(R.string.profiles_section_apps))
                SettingRow(
                    label = stringResource(R.string.profiles_work),
                    value = stringResource(R.string.settings_focus_mode_apps_count, uiState.settings.workProfileAppIds.size),
                    onClick = { onNavigate(LauncherScreen.WorkProfileApps) }
                )
                SettingRow(
                    label = stringResource(R.string.profiles_home),
                    value = stringResource(R.string.settings_focus_mode_apps_count, uiState.settings.homeProfileAppIds.size),
                    onClick = { onNavigate(LauncherScreen.HomeProfileApps) }
                )
                SettingRow(
                    label = stringResource(R.string.profiles_weekend),
                    value = stringResource(R.string.settings_focus_mode_apps_count, uiState.settings.weekendProfileAppIds.size),
                    onClick = { onNavigate(LauncherScreen.WeekendProfileApps) }
                )
                Spacer(Modifier.height(24.dp))

                SettingsSection(stringResource(R.string.profiles_section_wifi))
                SettingRow(
                    label = stringResource(R.string.profiles_current_wifi),
                    value = uiState.currentWifiSsid ?: stringResource(R.string.profiles_wifi_hidden_or_disconnected)
                )
                if (uiState.currentWifiSsid == null) {
                    FocusText(
                        text = stringResource(R.string.profiles_wifi_hidden_warning),
                        color = FocusSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
                ProfileWifiField(
                    label = stringResource(R.string.profiles_work_wifi),
                    value = uiState.settings.workProfileWifiSsid,
                    onValueChange = { onSetProfileWifiSsid("work", it) }
                )
                ProfileWifiField(
                    label = stringResource(R.string.profiles_home_wifi),
                    value = uiState.settings.homeProfileWifiSsid,
                    onValueChange = { onSetProfileWifiSsid("home", it) }
                )
                ProfileWifiField(
                    label = stringResource(R.string.profiles_weekend_wifi),
                    value = uiState.settings.weekendProfileWifiSsid,
                    onValueChange = { onSetProfileWifiSsid("weekend", it) }
                )
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
internal fun ProfileAppSelectorScreen(
    title: String,
    description: String,
    uiState: LauncherUiState,
    selectedIds: Set<String>,
    onSearchChange: (String) -> Unit,
    onToggleApp: (InstalledApp) -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.ContextProfiles, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        Column(Modifier.fillMaxSize()) {
            FocusText(title, color = LocalFocusPrimary.current, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            FocusText(description, color = FocusMuted, fontSize = 12.sp, lineHeight = 17.sp)
            Spacer(Modifier.height(28.dp))
            SearchInput(
                value = uiState.searchQuery,
                placeholder = stringResource(R.string.selector_search_placeholder),
                onValueChange = onSearchChange
            )
            Spacer(Modifier.height(18.dp))
            val apps = selectorApps(uiState, includeHidden = true, includeOriginalLabel = true)
            LazyColumn(Modifier.fillMaxSize()) {
                items(apps, key = { it.id }) { app ->
                    CheckRow(
                        label = appDisplayLabel(app, uiState.settings),
                        selected = app.id in selectedIds,
                        onClick = { onToggleApp(app) }
                    )
                }
                if (apps.isEmpty()) {
                    item { MutedEmptyText(stringResource(R.string.home_no_apps_found)) }
                }
            }
        }
    }
}

@Composable
internal fun NotificationBatchingScreen(
    uiState: LauncherUiState,
    onToggleEnabled: () -> Unit,
    onOpenListenerSettings: () -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.Notifications, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                FocusText(
                    text = stringResource(R.string.notifications_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                FocusText(
                    text = stringResource(R.string.notifications_desc),
                    color = FocusMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(24.dp))
                ToggleRow(
                    label = stringResource(R.string.notifications_enabled),
                    selected = uiState.settings.notificationBatchingEnabled,
                    onClick = onToggleEnabled
                )
                SettingRow(
                    label = stringResource(R.string.notifications_listener_access),
                    value = stringResource(if (uiState.notificationListenerAccessGranted) R.string.quotas_usage_access_granted else R.string.quotas_usage_access_required),
                    onClick = onOpenListenerSettings
                )
                SettingRow(
                    label = stringResource(R.string.notifications_essential_apps),
                    value = stringResource(R.string.settings_focus_mode_apps_count, uiState.settings.essentialNotificationAppIds.size),
                    onClick = { onNavigate(LauncherScreen.EssentialNotifications) }
                )
                SettingRow(
                    label = stringResource(R.string.notifications_batch_windows),
                    value = stringResource(R.string.notifications_batch_windows_value)
                )
                Spacer(Modifier.height(24.dp))
                SettingsSection(stringResource(R.string.notifications_pending_section))
                if (uiState.settings.batchedNotificationCounts.isEmpty()) {
                    MutedEmptyText(stringResource(R.string.notifications_pending_empty))
                } else {
                    uiState.settings.batchedNotificationCounts.entries.sortedByDescending { it.value }.forEach { (pkg, count) ->
                        val app = uiState.installedApps.find { it.packageName == pkg }
                        val label = app?.let { appDisplayLabel(it, uiState.settings) } ?: pkg
                        SettingRow(label = label, value = count.toString())
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
