package com.mphocodes.focuslauncher.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.mphocodes.focuslauncher.R
import com.mphocodes.focuslauncher.data.model.InstalledApp
import com.mphocodes.focuslauncher.ui.theme.FocusSecondary
import com.mphocodes.focuslauncher.ui.theme.FocusMuted
import com.mphocodes.focuslauncher.ui.theme.LocalFocusPrimary

@Composable
internal fun PrivacyScreen(
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.Privacy, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                FocusText(
                    text = stringResource(R.string.privacy_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                FocusText(
                    text = stringResource(R.string.privacy_summary),
                    color = FocusSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(24.dp))

                PrivacyTextBlock(
                    heading = stringResource(R.string.privacy_section_local_data),
                    body = stringResource(R.string.privacy_local_data_body)
                )
                PrivacyTextBlock(
                    heading = stringResource(R.string.privacy_section_permissions),
                    body = stringResource(R.string.privacy_permissions_body)
                )
                PrivacyTextBlock(
                    heading = stringResource(R.string.privacy_section_no_sharing),
                    body = stringResource(R.string.privacy_no_sharing_body)
                )
                PrivacyTextBlock(
                    heading = stringResource(R.string.privacy_section_deletion),
                    body = stringResource(R.string.privacy_deletion_body)
                )
                Spacer(Modifier.height(16.dp))
                SettingRow(label = "Data Management", value = "", onClick = { onNavigate(LauncherScreen.DataManagement) })
                SettingRow(
                    label = stringResource(R.string.privacy_policy_title),
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mphocodes.github.io/Focus/privacy-policy/"))
                        context.startActivity(intent)
                    }
                )
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
internal fun FocusModeScreen(
    uiState: LauncherUiState,
    onSearchChange: (String) -> Unit,
    onToggleApp: (InstalledApp) -> Unit,
    onCyclePauseDuration: () -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.FocusMode, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        val apps = selectorApps(uiState, includeHidden = true)
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                FocusText(
                    text = stringResource(R.string.focus_mode_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                FocusText(
                    text = stringResource(R.string.focus_mode_desc),
                    color = FocusMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(24.dp))
                SearchInput(
                    value = uiState.searchQuery,
                    placeholder = stringResource(R.string.selector_search_placeholder),
                    onValueChange = onSearchChange
                )
                Spacer(Modifier.height(16.dp))
            }

            items(apps, key = { it.id }) { app ->
                CheckRow(
                    label = appDisplayLabel(app, uiState.settings),
                    selected = app.id in uiState.settings.intentPromptAppIds,
                    onClick = { onToggleApp(app) }
                )
            }

            item {
                Spacer(Modifier.height(22.dp))
                SettingsSection(stringResource(R.string.focus_mode_section_interventions))
                SettingRow(
                    label = stringResource(R.string.focus_mode_ask_before_opening),
                    value = stringResource(R.string.settings_focus_mode_apps_count, uiState.settings.intentPromptAppIds.size),
                    onClick = {}
                )
                SettingRow(
                    label = stringResource(R.string.settings_pause_duration),
                    value = if (uiState.settings.focusPauseSeconds == 0) stringResource(R.string.settings_pause_duration_off) else stringResource(R.string.settings_pause_duration_sec, uiState.settings.focusPauseSeconds),
                    onClick = onCyclePauseDuration
                )
                Spacer(Modifier.height(36.dp))
                FocusButton(text = stringResource(R.string.focus_mode_btn_save), onClick = { onNavigate(LauncherScreen.Settings) })
                Spacer(Modifier.height(12.dp))
                FocusText(
                    text = stringResource(R.string.focus_mode_footer),
                    color = FocusMuted,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
internal fun DefaultHomeScreen(
    onOpenSettings: () -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.DefaultHome, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        Column(Modifier.fillMaxSize()) {
            FocusText(
                text = stringResource(R.string.default_home_title),
                color = LocalFocusPrimary.current,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 27.sp
            )
            Spacer(Modifier.height(12.dp))
            FocusText(
                text = stringResource(R.string.default_home_desc),
                color = FocusSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
            Spacer(Modifier.height(34.dp))
            StepRow(stringResource(R.string.default_home_step_1_num), stringResource(R.string.default_home_step_1_text))
            StepRow(stringResource(R.string.default_home_step_2_num), stringResource(R.string.default_home_step_2_text))
            StepRow(stringResource(R.string.default_home_step_3_num), stringResource(R.string.default_home_step_3_text))
            Spacer(Modifier.weight(1f))
            FocusButton(text = stringResource(R.string.default_home_btn_open_settings), onClick = onOpenSettings)
            Spacer(Modifier.height(12.dp))
            GhostButton(text = stringResource(R.string.default_home_btn_later), onClick = { onNavigate(LauncherScreen.Home) })
        }
    }
}

@Composable
internal fun SetupScreen(
    uiState: LauncherUiState,
    onNavigate: (LauncherScreen) -> Unit,
    onCompleteSetup: () -> Unit,
    onMenuClick: () -> Unit
) {
    val context = LocalContext.current
    val appsRepository = remember { com.mphocodes.focuslauncher.data.repository.AppsRepository(context) }

    FocusShell(
        selected = LauncherScreen.Settings,
        selectedScreen = LauncherScreen.Setup,
        showBack = true,
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                FocusText(
                    text = "SETUP CHECKLIST",
                    color = LocalFocusPrimary.current,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                FocusText(
                    text = "Complete these steps to unlock Focus Launcher's full potential.",
                    color = FocusMuted,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(28.dp))

                val isDefault = appsRepository.isDefaultLauncher()
                SettingRow(
                    label = if (isDefault) "Done - Default Launcher" else "Set Default Launcher",
                    value = "Required for home button",
                    onClick = {
                        val intent = Intent(android.provider.Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )

                val hasUsage = appsRepository.hasUsageAccess()
                SettingRow(
                    label = if (hasUsage) "Done - Usage Access" else "Grant Usage Access",
                    value = "Required to track screen time",
                    onClick = {
                        val intent = Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )

                val hasNotificationListener = appsRepository.hasNotificationListenerAccess()
                SettingRow(
                    label = if (hasNotificationListener) "Done - Notification Listener" else "Grant Notification Listener",
                    value = "Required for notification batching",
                    onClick = {
                        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    }
                )

                val hasNotificationPerm = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED
                } else true

                SettingRow(
                    label = if (hasNotificationPerm) "Done - Notification Permission" else "Grant Notification Permission",
                    value = "Required to show alerts",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        }
                    }
                )

                val hasPrivacy = uiState.settings.onboardingComplete
                SettingRow(
                    label = if (hasPrivacy) "Done - Privacy Policy" else "Review Privacy Policy",
                    value = "Your data stays on your device.",
                    onClick = onCompleteSetup
                )
                
                Spacer(Modifier.height(36.dp))
            }
        }
    }
}
