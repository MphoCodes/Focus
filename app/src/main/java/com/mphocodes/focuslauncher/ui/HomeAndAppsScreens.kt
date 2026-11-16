@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.mphocodes.focuslauncher.ui

import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.mphocodes.focuslauncher.R
import com.mphocodes.focuslauncher.data.model.InstalledApp
import com.mphocodes.focuslauncher.data.model.Workspace
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import com.mphocodes.focuslauncher.ui.theme.FocusSecondary
import com.mphocodes.focuslauncher.ui.theme.FocusMuted
import com.mphocodes.focuslauncher.ui.theme.LocalFocusPrimary

@Composable
internal fun HomeScreen(
    uiState: LauncherUiState,
    onSearchChange: (String) -> Unit,
    onOpenApp: (InstalledApp) -> Unit,
    onOpenAppInfo: (InstalledApp) -> Unit,
    onOpenWorkspace: (Workspace) -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Home, selectedScreen = LauncherScreen.Home, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        var now by remember { mutableStateOf(LocalDateTime.now()) }
        LaunchedEffect(Unit) {
            while (true) {
                now = LocalDateTime.now()
                delay(30_000)
            }
        }

        val isZeroAppsActive = uiState.settings.zeroAppsScheduleEnabled && isTimeInSchedule(
            now = now,
            start = uiState.settings.zeroAppsStartTime,
            end = uiState.settings.zeroAppsEndTime
        )

        Column(Modifier.fillMaxSize()) {
            FocusText(
                text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                color = LocalFocusPrimary.current,
                fontSize = 64.sp,
                fontWeight = FontWeight.Light
            )
            FocusText(
                text = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                color = FocusSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(36.dp))
            Eyebrow(stringResource(R.string.home_intent_header))
            Spacer(Modifier.height(8.dp))
            FocusText(
                text = stringResource(R.string.home_intent_title),
                color = LocalFocusPrimary.current,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(28.dp))
            Eyebrow(stringResource(R.string.home_workspaces_header))
            Spacer(Modifier.height(8.dp))
            val homeList = if (uiState.searchQuery.isBlank()) uiState.favoriteApps else uiState.visibleApps
            if (isZeroAppsActive) {
                MutedEmptyText(stringResource(R.string.zero_apps_active))
            } else if (uiState.searchQuery.isBlank()) {
                WorkspaceTileGrid(
                    workspaces = uiState.settings.workspaces,
                    onOpenWorkspace = onOpenWorkspace
                )
            } else if (homeList.isEmpty()) {
                MutedEmptyText(
                    stringResource(R.string.home_no_apps_found)
                )
            } else {
                homeList.take(7).forEach { app ->
                    AppRow(label = appDisplayLabel(app, uiState.settings), onClick = { onOpenApp(app) }, onLongClick = { onOpenAppInfo(app) })
                }
            }
            Spacer(Modifier.height(28.dp))
            SearchInput(
                value = uiState.searchQuery,
                placeholder = stringResource(R.string.home_search_placeholder),
                onValueChange = onSearchChange
            )
            Spacer(Modifier.height(12.dp))
            val isFocusActive = uiState.settings.intentPromptAppIds.isNotEmpty()
            val statusText = "● " + stringResource(if (isFocusActive) R.string.home_focus_mode_on else R.string.home_focus_mode_off)
            FocusText(
                text = statusText,
                color = FocusMuted,
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
internal fun AppsScreen(
    uiState: LauncherUiState,
    onSearchChange: (String) -> Unit,
    onOpenApp: (InstalledApp) -> Unit,
    onOpenAppInfo: (InstalledApp) -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Apps, selectedScreen = LauncherScreen.Apps, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        val now = LocalDateTime.now()
        val isZeroAppsActive = uiState.settings.zeroAppsScheduleEnabled && isTimeInSchedule(
            now = now,
            start = uiState.settings.zeroAppsStartTime,
            end = uiState.settings.zeroAppsEndTime
        )

        Column(Modifier.fillMaxSize()) {
            FocusText(
                text = stringResource(R.string.apps_title),
                color = LocalFocusPrimary.current,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(24.dp))
            SearchInput(
                value = uiState.searchQuery,
                placeholder = stringResource(R.string.apps_search_placeholder),
                onValueChange = onSearchChange
            )
            Spacer(Modifier.height(18.dp))
            val context = LocalContext.current
            LazyColumn(Modifier.fillMaxSize()) {
                if (isZeroAppsActive) {
                    item { MutedEmptyText(stringResource(R.string.zero_apps_active)) }
                } else {
                    items(uiState.visibleApps, key = { it.id }) { app ->
                        AppRow(label = appDisplayLabel(app, uiState.settings), onClick = { onOpenApp(app) }, onLongClick = { onOpenAppInfo(app) })
                    }
                    if (uiState.loading) {
                        item { MutedEmptyText(stringResource(R.string.apps_loading)) }
                    } else if (uiState.visibleApps.isEmpty()) {
                        if (uiState.searchQuery.isNotBlank()) {
                            item {
                                AppRow(label = stringResource(R.string.apps_search_web, uiState.searchQuery), onClick = {
                                    val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                                        putExtra(SearchManager.QUERY, uiState.searchQuery)
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val fallbackIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${Uri.encode(uiState.searchQuery)}"))
                                        try {
                                            context.startActivity(fallbackIntent)
                                        } catch (e2: Exception) {
                                        }
                                    }
                                })
                            }
                        } else {
                        item { MutedEmptyText(stringResource(R.string.home_no_apps_found)) }
                    }
                }
            }
        }
    }
}
}
