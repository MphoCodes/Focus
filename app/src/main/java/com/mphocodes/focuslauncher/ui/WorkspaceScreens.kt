@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.mphocodes.focuslauncher.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mphocodes.focuslauncher.R
import com.mphocodes.focuslauncher.data.model.InstalledApp
import com.mphocodes.focuslauncher.data.model.Workspace
import com.mphocodes.focuslauncher.ui.theme.FocusMuted
import com.mphocodes.focuslauncher.ui.theme.LocalFocusPrimary

@Composable
internal fun AppSelectorScreen(
    title: String,
    description: String,
    uiState: LauncherUiState,
    selectedIds: Set<String>,
    includeHidden: Boolean = false,
    onSearchChange: (String) -> Unit,
    onToggleApp: (InstalledApp) -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Apps, selectedScreen = LauncherScreen.Apps, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
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
            val apps = selectorApps(uiState, includeHidden)
            LazyColumn(Modifier.fillMaxSize()) {
                items(apps, key = { it.id }) { app ->
                    CheckRow(
                        label = appDisplayLabel(app, uiState.settings),
                        selected = app.id in selectedIds,
                        muted = app.id in uiState.settings.hiddenAppIds && !includeHidden,
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
internal fun WorkspacesScreen(
    uiState: LauncherUiState,
    onOpenWorkspaceApps: (Workspace) -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.Workspaces, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                FocusText(
                    text = stringResource(R.string.workspaces_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                FocusText(
                    text = stringResource(R.string.workspaces_desc),
                    color = FocusMuted,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(24.dp))
                SettingsSection(stringResource(R.string.workspaces_section_tiles))
            }

            items(uiState.settings.workspaces, key = { it.id }) { workspace ->
                SettingRow(
                    label = workspace.name,
                    value = stringResource(R.string.settings_focus_mode_apps_count, workspace.appIds.size),
                    onClick = { onOpenWorkspaceApps(workspace) }
                )
            }
        }
    }
}

@Composable
internal fun WorkspaceDetailScreen(
    uiState: LauncherUiState,
    onSearchChange: (String) -> Unit,
    onOpenApp: (InstalledApp) -> Unit,
    onOpenAppInfo: (InstalledApp) -> Unit,
    onEditWorkspace: (Workspace) -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    val workspace = uiState.selectedWorkspace
    if (workspace == null) {
        FocusShell(
            selected = LauncherScreen.Home,
            selectedScreen = LauncherScreen.WorkspaceDetail,
            showBack = true,
            backTarget = LauncherScreen.Home,
            onNavigate = onNavigate,
            onMenuClick = onMenuClick
        ) {
            MutedEmptyText(stringResource(R.string.workspace_missing))
        }
        return
    }

    val workspaceApps = workspaceApps(uiState, workspace)
    val shownApps = if (uiState.searchQuery.isBlank()) {
        workspaceApps
    } else {
        workspaceApps.filter { appDisplayLabel(it, uiState.settings).contains(uiState.searchQuery.trim(), ignoreCase = true) }
    }

    FocusShell(
        selected = LauncherScreen.Home,
        selectedScreen = LauncherScreen.WorkspaceDetail,
        showBack = true,
        backTarget = LauncherScreen.Home,
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        Column(Modifier.fillMaxSize()) {
            WorkspaceTile(
                workspace = workspace,
                modifier = Modifier
                    .size(82.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(Modifier.height(12.dp))
            FocusText(
                text = workspace.name,
                color = LocalFocusPrimary.current,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            SearchInput(
                value = uiState.searchQuery,
                placeholder = stringResource(R.string.workspace_search_placeholder),
                onValueChange = onSearchChange
            )
            Spacer(Modifier.height(18.dp))
            if (shownApps.isEmpty()) {
                MutedEmptyText(
                    if (workspace.appIds.isEmpty()) stringResource(R.string.workspace_no_apps) else stringResource(R.string.home_no_apps_found)
                )
                Spacer(Modifier.height(12.dp))
                GhostButton(text = stringResource(R.string.workspace_edit_apps), onClick = { onEditWorkspace(workspace) })
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    items(shownApps, key = { it.id }) { app ->
                        AppRow(
                            label = appDisplayLabel(app, uiState.settings),
                            onClick = { onOpenApp(app) },
                            onLongClick = { onOpenAppInfo(app) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun WorkspaceAppSelectorScreen(
    uiState: LauncherUiState,
    onSearchChange: (String) -> Unit,
    onToggleApp: (InstalledApp) -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    val workspace = uiState.selectedWorkspace
    val title = workspace?.name ?: stringResource(R.string.workspaces_title)
    FocusShell(
        selected = LauncherScreen.Settings,
        selectedScreen = LauncherScreen.WorkspaceApps,
        showBack = true,
        backTarget = LauncherScreen.Workspaces,
        onNavigate = onNavigate,
        onMenuClick = onMenuClick
    ) {
        Column(Modifier.fillMaxSize()) {
            FocusText(title, color = LocalFocusPrimary.current, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            FocusText(stringResource(R.string.workspace_apps_desc), color = FocusMuted, fontSize = 12.sp, lineHeight = 17.sp)
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
                        selected = workspace != null && app.id in workspace.appIds,
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
