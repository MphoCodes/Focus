package com.mphocodes.focuslauncher.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mphocodes.focuslauncher.R
import com.mphocodes.focuslauncher.ui.theme.FocusSecondary
import com.mphocodes.focuslauncher.ui.theme.LocalFocusPrimary
import com.mphocodes.focuslauncher.ui.theme.LocalFocusBackground

@Composable
internal fun StatsScreen(
    uiState: LauncherUiState,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.Stats, showBack = true, onNavigate = {}, onMenuClick = onMenuClick) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                FocusText(
                    text = stringResource(R.string.stats_header),
                    color = LocalFocusPrimary.current,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                FocusText(
                    text = stringResource(R.string.stats_subheader),
                    color = LocalFocusPrimary.current,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(28.dp))
                
                SettingsSection(stringResource(R.string.stats_deflections_title))
                val deflections = uiState.settings.appDeflectionCounts
                if (deflections.isEmpty()) {
                    MutedEmptyText(stringResource(R.string.stats_no_deflections))
                } else {
                    deflections.entries.sortedByDescending { it.value }.forEach { (pkg, count) ->
                        val app = uiState.installedApps.find { it.packageName == pkg }
                        val label = app?.let { appDisplayLabel(it, uiState.settings) } ?: pkg
                        SettingRow(label = label, value = count.toString())
                    }
                }
                Spacer(Modifier.height(28.dp))
                
                SettingsSection(stringResource(R.string.stats_launches_title))
                val launches = uiState.settings.appLaunchCounts
                if (launches.isEmpty()) {
                    MutedEmptyText(stringResource(R.string.stats_no_launches))
                } else {
                    launches.entries.sortedByDescending { it.value }.forEach { (pkg, count) ->
                        val app = uiState.installedApps.find { it.packageName == pkg }
                        val label = app?.let { appDisplayLabel(it, uiState.settings) } ?: pkg
                        SettingRow(label = label, value = count.toString())
                    }
                }
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
internal fun DataManagementScreen(
    onClearIntentLogs: () -> Unit,
    onClearStats: () -> Unit,
    onClearPendingNotifications: () -> Unit,
    onClearAllSettings: () -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.DataManagement, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        var showConfirmDialog by remember { mutableStateOf<(() -> Unit)?>(null) }

        LazyColumn(Modifier.fillMaxSize()) {
            item {
                FocusText(
                    text = "Data Management",
                    color = LocalFocusPrimary.current,
                    fontSize = 22.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                FocusText(
                    text = "Manage your local data or clear it completely.",
                    color = FocusSecondary,
                    fontSize = 13.sp,
                    lineHeight = 19.sp
                )
                Spacer(Modifier.height(24.dp))

                SettingRow(label = "Clear Intent Logs", value = "", onClick = { showConfirmDialog = onClearIntentLogs })
                SettingRow(label = "Clear Stats & Quotas", value = "", onClick = { showConfirmDialog = onClearStats })
                SettingRow(label = "Clear Pending Notifications", value = "", onClick = { showConfirmDialog = onClearPendingNotifications })
                Spacer(Modifier.height(24.dp))
                SettingsSection("DANGER ZONE")
                SettingRow(label = "Clear All Settings", value = "", onClick = { showConfirmDialog = onClearAllSettings })
                Spacer(Modifier.height(28.dp))
            }
        }

        if (showConfirmDialog != null) {
            Box(
                modifier = Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.8f)),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Column(Modifier.background(LocalFocusBackground.current).padding(24.dp)) {
                    FocusText("Are you sure? This action is destructive.", color = LocalFocusPrimary.current, fontSize = 16.sp)
                    Spacer(Modifier.height(24.dp))
                    FocusButton(text = "Confirm", onClick = {
                        showConfirmDialog?.invoke()
                        showConfirmDialog = null
                    })
                    Spacer(Modifier.height(12.dp))
                    GhostButton(text = "Cancel", onClick = { showConfirmDialog = null })
                }
            }
        }
    }
}
