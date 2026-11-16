package com.mphocodes.focuslauncher.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Surface
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mphocodes.focuslauncher.R
import androidx.compose.ui.graphics.graphicsLayer
import com.mphocodes.focuslauncher.ui.theme.FocusSecondary
import com.mphocodes.focuslauncher.ui.theme.FocusDivider
import com.mphocodes.focuslauncher.ui.theme.LocalFocusPrimary
import com.mphocodes.focuslauncher.ui.theme.LocalFocusBackground
import com.mphocodes.focuslauncher.ui.theme.LocalTextSizeMultiplier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect

@Composable
fun FocusLauncherApp(viewModel: FocusLauncherViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingPromptApp = uiState.pendingPromptApp
    val quotaBlock = uiState.quotaBlock
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.checkActiveAppQuotas()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val context = LocalContext.current
    LifecycleResumeEffect { viewModel.refreshSystemAccess() }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.toggleNotificationBatchingEnabled()
        }
    }
    val onToggleNotificationBatching = {
        val shouldEnable = !uiState.settings.notificationBatchingEnabled
        val needsNotificationPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

        if (shouldEnable && needsNotificationPermission) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            viewModel.toggleNotificationBatchingEnabled()
        }
    }

    val currentBackground = if (uiState.settings.blackThemeEnabled) Color(0xFF000000) else Color(0xFF151515)
    val currentTextMultiplier = when (uiState.settings.textSize) {
        com.mphocodes.focuslauncher.data.model.TextSize.SMALL -> 0.85f
        com.mphocodes.focuslauncher.data.model.TextSize.STANDARD -> 1.0f
        com.mphocodes.focuslauncher.data.model.TextSize.LARGE -> 1.15f
    }
    
    val currentAccentColor = when (uiState.settings.themeAccent) {
        com.mphocodes.focuslauncher.data.model.ThemeAccent.AMBER_MONO -> androidx.compose.ui.graphics.Color(0xFFFFB300)
        com.mphocodes.focuslauncher.data.model.ThemeAccent.FOREST_GREEN -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        else -> androidx.compose.ui.graphics.Color(0xFFE2E2E2)
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val baseModifier = if (uiState.settings.grayscaleEnabled) {
        Modifier.graphicsLayer {
            colorFilter = androidx.compose.ui.graphics.ColorFilter.colorMatrix(
                androidx.compose.ui.graphics.ColorMatrix().apply { setToSaturation(0f) }
            )
        }
    } else {
        Modifier
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalFocusBackground provides currentBackground,
        LocalTextSizeMultiplier provides currentTextMultiplier,
        LocalFocusPrimary provides currentAccentColor
    ) {
        Box(modifier = baseModifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = LocalFocusBackground.current,
                    drawerContentColor = LocalFocusPrimary.current,
                    drawerShape = RectangleShape,
                    drawerTonalElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LocalFocusBackground.current)
                            .statusBarsPadding()
                            .navigationBarsPadding()
                            .padding(horizontal = 24.dp, vertical = 24.dp)
                    ) {
                        FocusText(
                            text = stringResource(R.string.app_logo),
                            color = LocalFocusPrimary.current,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 3.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))

                        // Custom thin divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(FocusDivider)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        val navItems = listOf(
                            stringResource(R.string.nav_home) to LauncherScreen.Home,
                            stringResource(R.string.nav_apps) to LauncherScreen.Apps,
                            stringResource(R.string.nav_settings) to LauncherScreen.Settings,
                            stringResource(R.string.settings_workspaces) to LauncherScreen.Workspaces,
                            stringResource(R.string.settings_fav_apps) to LauncherScreen.Favorites,
                            stringResource(R.string.settings_hidden_apps) to LauncherScreen.Hidden,
                            stringResource(R.string.settings_app_disguises) to LauncherScreen.Disguises,
                            stringResource(R.string.settings_app_time_quotas) to LauncherScreen.Quotas,
                            stringResource(R.string.settings_context_profiles) to LauncherScreen.ContextProfiles,
                            stringResource(R.string.nav_notifications) to LauncherScreen.Notifications,
                            stringResource(R.string.nav_privacy) to LauncherScreen.Privacy,
                            stringResource(R.string.settings_focus_mode) to LauncherScreen.FocusMode,
                            stringResource(R.string.settings_set_default) to LauncherScreen.DefaultHome,
                            stringResource(R.string.nav_stats) to LauncherScreen.Stats
                        )

                        navItems.forEach { (label, screen) ->
                            FocusText(
                                text = label,
                                color = if (uiState.screen == screen) LocalFocusPrimary.current else FocusSecondary,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                fontWeight = if (uiState.screen == screen) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.navigateTo(screen)
                                        coroutineScope.launch { drawerState.close() }
                                    }
                                    .padding(vertical = 14.dp)
                            )
                        }
                    }
                }
            }
        ) {
            Surface(color = LocalFocusBackground.current, modifier = Modifier.fillMaxSize()) {
                when {
                    quotaBlock != null -> QuotaBlockScreen(
                        uiState = uiState,
                        quotaBlock = quotaBlock,
                        onOpenUsageAccess = viewModel::openUsageAccessSettings,
                        onDismiss = viewModel::dismissQuotaBlock,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } }
                    )

                    pendingPromptApp != null -> IntentPromptScreen(
                        uiState = uiState,
                        app = pendingPromptApp,
                        onContinue = viewModel::confirmPromptOpen,
                        onCancel = viewModel::cancelPromptOpen,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } }
                    )

                    !uiState.settings.onboardingComplete -> OnboardingScreen(
                        onGetStarted = viewModel::completeOnboarding,
                        onMenuClick = { coroutineScope.launch { drawerState.open() } }
                    )

                    else -> when (uiState.screen) {
                        LauncherScreen.Home -> HomeScreen(
                            uiState = uiState,
                            onSearchChange = viewModel::updateSearch,
                            onOpenApp = viewModel::requestOpenApp,
                            onOpenAppInfo = viewModel::openAppInfo,
                            onOpenWorkspace = viewModel::openWorkspace,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.Apps -> AppsScreen(
                            uiState = uiState,
                            onSearchChange = viewModel::updateSearch,
                            onOpenApp = viewModel::requestOpenApp,
                            onOpenAppInfo = viewModel::openAppInfo,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.Setup -> SetupScreen(
                            uiState = uiState,
                            onNavigate = viewModel::navigateTo,
                            onCompleteSetup = viewModel::completeOnboarding,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.Settings -> SettingsScreen(
                            uiState = uiState,
                            onNavigate = viewModel::navigateTo,
                            onCyclePauseDuration = viewModel::cyclePauseDuration,
                            onCycleAppOrder = viewModel::cycleAppOrder,
                            onToggleLocalDataOnly = viewModel::toggleLocalDataOnly,
                            onToggleBlackTheme = viewModel::toggleBlackThemeEnabled,
                            onToggleGrayscale = viewModel::toggleGrayscaleEnabled,
                            onToggleZeroApps = viewModel::toggleZeroAppsScheduleEnabled,
                            onCycleTextSize = viewModel::cycleTextSize,
                            onCycleThemeAccent = viewModel::cycleThemeAccent,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.Favorites -> AppSelectorScreen(
                            title = stringResource(R.string.favorites_selector_title),
                            description = stringResource(R.string.favorites_selector_desc),
                            uiState = uiState,
                            selectedIds = uiState.settings.favoriteAppIds,
                            onSearchChange = viewModel::updateSearch,
                            onToggleApp = viewModel::toggleFavorite,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.Workspaces -> WorkspacesScreen(
                            uiState = uiState,
                            onOpenWorkspaceApps = viewModel::openWorkspaceApps,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.WorkspaceDetail -> WorkspaceDetailScreen(
                            uiState = uiState,
                            onSearchChange = viewModel::updateSearch,
                            onOpenApp = viewModel::requestOpenApp,
                            onOpenAppInfo = viewModel::openAppInfo,
                            onEditWorkspace = viewModel::openWorkspaceApps,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.WorkspaceApps -> WorkspaceAppSelectorScreen(
                            uiState = uiState,
                            onSearchChange = viewModel::updateSearch,
                            onToggleApp = viewModel::toggleWorkspaceApp,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.Hidden -> AppSelectorScreen(
                            title = stringResource(R.string.hidden_selector_title),
                            description = stringResource(R.string.hidden_selector_desc),
                            uiState = uiState,
                            selectedIds = uiState.settings.hiddenAppIds,
                            includeHidden = true,
                            onSearchChange = viewModel::updateSearch,
                            onToggleApp = viewModel::toggleHidden,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.Disguises -> AppDisguiseScreen(
                            uiState = uiState,
                            onSearchChange = viewModel::updateSearch,
                            onDisguiseChange = viewModel::setAppDisguise,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.Quotas -> AppQuotaScreen(
                            uiState = uiState,
                            onSearchChange = viewModel::updateSearch,
                            onCycleQuota = viewModel::cycleAppDailyQuota,
                            onOpenUsageAccess = viewModel::openUsageAccessSettings,
                            onRefreshUsages = viewModel::refreshQuotaUsages,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.ContextProfiles -> ContextProfilesScreen(
                            uiState = uiState,
                            onToggleEnabled = viewModel::toggleContextProfilesEnabled,
                            onToggleAutoSwitch = viewModel::toggleContextProfilesAutoSwitch,
                            onCycleManualProfile = viewModel::cycleManualContextProfile,
                            onSetProfileWifiSsid = viewModel::setProfileWifiSsid,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.WorkProfileApps -> ProfileAppSelectorScreen(
                            title = stringResource(R.string.profiles_work_apps_title),
                            description = stringResource(R.string.profiles_work_apps_desc),
                            uiState = uiState,
                            selectedIds = uiState.settings.workProfileAppIds,
                            onSearchChange = viewModel::updateSearch,
                            onToggleApp = viewModel::toggleWorkProfileApp,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.HomeProfileApps -> ProfileAppSelectorScreen(
                            title = stringResource(R.string.profiles_home_apps_title),
                            description = stringResource(R.string.profiles_home_apps_desc),
                            uiState = uiState,
                            selectedIds = uiState.settings.homeProfileAppIds,
                            onSearchChange = viewModel::updateSearch,
                            onToggleApp = viewModel::toggleHomeProfileApp,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.WeekendProfileApps -> ProfileAppSelectorScreen(
                            title = stringResource(R.string.profiles_weekend_apps_title),
                            description = stringResource(R.string.profiles_weekend_apps_desc),
                            uiState = uiState,
                            selectedIds = uiState.settings.weekendProfileAppIds,
                            onSearchChange = viewModel::updateSearch,
                            onToggleApp = viewModel::toggleWeekendProfileApp,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.Notifications -> NotificationBatchingScreen(
                            uiState = uiState,
                            onToggleEnabled = onToggleNotificationBatching,
                            onOpenListenerSettings = viewModel::openNotificationListenerSettings,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.EssentialNotifications -> ProfileAppSelectorScreen(
                            title = stringResource(R.string.notifications_essential_title),
                            description = stringResource(R.string.notifications_essential_desc),
                            uiState = uiState,
                            selectedIds = uiState.settings.essentialNotificationAppIds,
                            onSearchChange = viewModel::updateSearch,
                            onToggleApp = viewModel::toggleEssentialNotificationApp,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.Privacy -> PrivacyScreen(
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.DataManagement -> DataManagementScreen(
                            onClearIntentLogs = viewModel::clearIntentLogs,
                            onClearStats = viewModel::clearStats,
                            onClearPendingNotifications = viewModel::clearPendingNotifications,
                            onClearAllSettings = viewModel::clearAllSettings,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.FocusMode -> FocusModeScreen(
                            uiState = uiState,
                            onSearchChange = viewModel::updateSearch,
                            onToggleApp = viewModel::toggleIntentPrompt,
                            onCyclePauseDuration = viewModel::cyclePauseDuration,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.DefaultHome -> DefaultHomeScreen(
                            onOpenSettings = viewModel::openDefaultHomeSettings,
                            onNavigate = viewModel::navigateTo,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )

                        LauncherScreen.Stats -> StatsScreen(
                            uiState = uiState,
                            onMenuClick = { coroutineScope.launch { drawerState.open() } }
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
fun LifecycleResumeEffect(onResumed: () -> Unit) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val currentOnResumed by androidx.compose.runtime.rememberUpdatedState(onResumed)

    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                currentOnResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}
