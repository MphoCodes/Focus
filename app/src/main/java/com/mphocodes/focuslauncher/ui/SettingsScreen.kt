package com.mphocodes.focuslauncher.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mphocodes.focuslauncher.R
import com.mphocodes.focuslauncher.ui.theme.FocusMuted
import com.mphocodes.focuslauncher.ui.theme.LocalFocusPrimary

@Composable
internal fun SettingsScreen(
    uiState: LauncherUiState,
    onNavigate: (LauncherScreen) -> Unit,
    onCyclePauseDuration: () -> Unit,
    onCycleAppOrder: () -> Unit,
    onToggleLocalDataOnly: () -> Unit,
    onToggleBlackTheme: () -> Unit,
    onToggleGrayscale: () -> Unit,
    onToggleZeroApps: () -> Unit,
    onCycleTextSize: () -> Unit,
    onCycleThemeAccent: () -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.Settings, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                FocusText(
                    text = stringResource(R.string.settings_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(28.dp))
                SettingsSection("ONBOARDING")
                SettingRow("Setup Checklist", onClick = { onNavigate(LauncherScreen.Setup) })
                Spacer(Modifier.height(28.dp))
                SettingsSection(stringResource(R.string.settings_section_home_screen))
                SettingRow(
                    label = stringResource(R.string.settings_workspaces),
                    value = stringResource(R.string.settings_focus_mode_apps_count, uiState.settings.workspaces.sumOf { it.appIds.size }),
                    onClick = { onNavigate(LauncherScreen.Workspaces) }
                )
                SettingRow(stringResource(R.string.settings_fav_apps), onClick = { onNavigate(LauncherScreen.Favorites) })
                SettingRow(stringResource(R.string.settings_hidden_apps), onClick = { onNavigate(LauncherScreen.Hidden) })
                SettingRow(
                    label = stringResource(R.string.settings_app_disguises),
                    value = stringResource(R.string.settings_focus_mode_apps_count, uiState.settings.appDisguises.size),
                    onClick = { onNavigate(LauncherScreen.Disguises) }
                )
                SettingRow(
                    label = stringResource(R.string.settings_app_order),
                    value = uiState.settings.appOrder.value,
                    onClick = onCycleAppOrder
                )
                SettingRow(stringResource(R.string.settings_set_default), onClick = { onNavigate(LauncherScreen.DefaultHome) })
                Spacer(Modifier.height(28.dp))

                SettingsSection(stringResource(R.string.settings_section_focus))
                SettingRow(
                    label = stringResource(R.string.settings_focus_mode),
                    value = stringResource(R.string.settings_focus_mode_apps_count, uiState.settings.intentPromptAppIds.size),
                    onClick = { onNavigate(LauncherScreen.FocusMode) }
                )
                SettingRow(
                    label = stringResource(R.string.settings_app_time_quotas),
                    value = stringResource(R.string.settings_focus_mode_apps_count, uiState.settings.appDailyQuotaMinutes.size),
                    onClick = { onNavigate(LauncherScreen.Quotas) }
                )
                SettingRow(
                    label = stringResource(R.string.settings_context_profiles),
                    value = displayProfileName(uiState.activeContextProfileId),
                    onClick = { onNavigate(LauncherScreen.ContextProfiles) }
                )
                SettingRow(
                    label = stringResource(R.string.settings_notification_batching),
                    value = stringResource(R.string.settings_focus_mode_apps_count, uiState.settings.essentialNotificationAppIds.size),
                    onClick = { onNavigate(LauncherScreen.Notifications) }
                )
                SettingRow(stringResource(R.string.settings_intent_prompts), onClick = { onNavigate(LauncherScreen.FocusMode) })
                SettingRow(
                    label = stringResource(R.string.settings_pause_duration),
                    value = if (uiState.settings.focusPauseSeconds == 0) stringResource(R.string.settings_pause_duration_off) else stringResource(R.string.settings_pause_duration_sec, uiState.settings.focusPauseSeconds),
                    onClick = onCyclePauseDuration
                )
                Spacer(Modifier.height(28.dp))

                SettingsSection(stringResource(R.string.settings_section_privacy))
                SettingRow(
                    label = stringResource(R.string.settings_usage_access),
                    value = stringResource(R.string.settings_usage_access_not_used)
                )
                ToggleRow(
                    label = stringResource(R.string.settings_local_data_only),
                    selected = uiState.settings.localDataOnly,
                    onClick = onToggleLocalDataOnly
                )
                Spacer(Modifier.height(28.dp))

                SettingsSection(stringResource(R.string.settings_section_system_integration))
                ToggleRow(
                    label = stringResource(R.string.settings_grayscale),
                    selected = uiState.settings.grayscaleEnabled,
                    onClick = onToggleGrayscale
                )
                ToggleRow(
                    label = stringResource(R.string.settings_zero_apps),
                    selected = uiState.settings.zeroAppsScheduleEnabled,
                    onClick = onToggleZeroApps
                )
                Spacer(Modifier.height(28.dp))

                SettingsSection(stringResource(R.string.settings_section_appearance))
                ToggleRow(
                    label = stringResource(R.string.settings_black_theme),
                    selected = uiState.settings.blackThemeEnabled,
                    onClick = onToggleBlackTheme
                )
                SettingRow(
                    label = stringResource(R.string.settings_text_size),
                    value = uiState.settings.textSize.value,
                    onClick = onCycleTextSize
                )
                SettingRow(
                    label = stringResource(R.string.settings_theme_accent),
                    value = uiState.settings.themeAccent.value,
                    onClick = onCycleThemeAccent
                )
                Spacer(Modifier.height(28.dp))

                SettingsSection(stringResource(R.string.settings_section_about))
                SettingRow(stringResource(R.string.app_name), value = "v${appVersionText()}")
                SettingRow(stringResource(R.string.settings_privacy), onClick = { onNavigate(LauncherScreen.Privacy) })
                Spacer(Modifier.height(32.dp))
                FocusText(
                    text = stringResource(R.string.settings_about_footer),
                    color = FocusMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
