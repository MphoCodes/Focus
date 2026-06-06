@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
package com.focuslauncher.ui

import android.Manifest
import android.app.SearchManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslauncher.R
import com.focuslauncher.data.model.InstalledApp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
private val FocusBackground = Color(0xFF000000) // Mockup has pure black backgrounds
private val LocalFocusPrimary = androidx.compose.runtime.compositionLocalOf { Color(0xFFFFFFFF) }
private val FocusSecondary = Color(0xFF8E8E8E) // Clean medium gray
private val FocusMuted = Color(0xFF4E4E4E) // Darker gray for less contrast elements
private val FocusDivider = Color(0xFF161616) // Subtle dividers
private val FocusInput = Color(0xFF0F0F0F) // Lighter box for inputs

private val LocalFocusBackground = androidx.compose.runtime.compositionLocalOf { Color(0xFF000000) }
private val LocalTextSizeMultiplier = androidx.compose.runtime.compositionLocalOf { 1.0f }

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
        "Small" -> 0.85f
        "Standard" -> 1.0f
        "Large" -> 1.15f
        else -> 1.0f
    }
    
    val currentAccentColor = when (uiState.settings.themeAccent) {
        "Amber Mono" -> Color(0xFFFFB300)
        "Forest Green" -> Color(0xFF4CAF50)
        else -> Color(0xFFE2E2E2)
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
                        Text(
                            text = "FOCUS",
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
                            "Home" to LauncherScreen.Home,
                            "Apps" to LauncherScreen.Apps,
                            "Settings" to LauncherScreen.Settings,
                            "Favourite apps" to LauncherScreen.Favorites,
                            "Hidden apps" to LauncherScreen.Hidden,
                            "App disguises" to LauncherScreen.Disguises,
                            "App time quotas" to LauncherScreen.Quotas,
                            "Context profiles" to LauncherScreen.ContextProfiles,
                            "Notifications" to LauncherScreen.Notifications,
                            "Privacy" to LauncherScreen.Privacy,
                            "Focus mode" to LauncherScreen.FocusMode,
                            "Set as default" to LauncherScreen.DefaultHome,
                            "Stats" to LauncherScreen.Stats
                        )

                        navItems.forEach { (label, screen) ->
                            Text(
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
private fun OnboardingScreen(onGetStarted: () -> Unit, onMenuClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalFocusBackground.current)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        TopMark(onMenuClick = onMenuClick)
        Spacer(Modifier.height(36.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .border(BorderStroke(1.dp, FocusSecondary)),
            contentAlignment = Alignment.Center
        ) {
            Text("F", color = LocalFocusPrimary.current, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.onboarding_title),
            color = LocalFocusPrimary.current,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 26.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.onboarding_desc_1),
            color = FocusSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(14.dp))
        Text(
            text = stringResource(R.string.onboarding_desc_2),
            color = FocusMuted,
            fontSize = 12.sp,
            lineHeight = 18.sp
        )
        Spacer(Modifier.height(36.dp))
        FocusButton(text = stringResource(R.string.onboarding_btn_get_started), onClick = onGetStarted)
        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(R.string.onboarding_already_use),
            color = FocusMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = stringResource(R.string.onboarding_footer_quote),
            color = FocusMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 8.sp,
            lineHeight = 14.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HomeScreen(
    uiState: LauncherUiState,
    onSearchChange: (String) -> Unit,
    onOpenApp: (InstalledApp) -> Unit,
    onOpenAppInfo: (InstalledApp) -> Unit,
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
            Text(
                text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                color = LocalFocusPrimary.current,
                fontSize = 64.sp,
                fontWeight = FontWeight.Light
            )
            Text(
                text = now.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")),
                color = FocusSecondary,
                fontSize = 13.sp
            )
            Spacer(Modifier.height(36.dp))
            Eyebrow(stringResource(R.string.home_intent_header))
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.home_intent_title),
                color = LocalFocusPrimary.current,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(28.dp))
            Eyebrow(stringResource(R.string.home_favorites_header))
            Spacer(Modifier.height(8.dp))
            val homeList = if (uiState.searchQuery.isBlank()) uiState.favoriteApps else uiState.visibleApps
            if (isZeroAppsActive) {
                MutedEmptyText("Zero-Apps Schedule is active")
            } else if (homeList.isEmpty()) {
                MutedEmptyText(
                    if (uiState.searchQuery.isBlank()) stringResource(R.string.home_no_favorites) else stringResource(R.string.home_no_apps_found)
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
            Text(
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
private fun AppsScreen(
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
            Text(
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
                    item { MutedEmptyText("Zero-Apps Schedule is active") }
                } else {
                    items(uiState.visibleApps, key = { it.id }) { app ->
                        AppRow(label = appDisplayLabel(app, uiState.settings), onClick = { onOpenApp(app) }, onLongClick = { onOpenAppInfo(app) })
                    }
                    if (uiState.loading) {
                        item { MutedEmptyText(stringResource(R.string.apps_loading)) }
                    } else if (uiState.visibleApps.isEmpty()) {
                        if (uiState.searchQuery.isNotBlank()) {
                            item {
                                AppRow(label = "Search Web for '${uiState.searchQuery}'", onClick = {
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

@Composable
private fun SettingsScreen(
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
                Text(
                    text = stringResource(R.string.settings_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(28.dp))
                SettingsSection(stringResource(R.string.settings_section_home_screen))
                SettingRow(stringResource(R.string.settings_fav_apps), onClick = { onNavigate(LauncherScreen.Favorites) })
                SettingRow(stringResource(R.string.settings_hidden_apps), onClick = { onNavigate(LauncherScreen.Hidden) })
                SettingRow(
                    label = stringResource(R.string.settings_app_disguises),
                    value = stringResource(R.string.settings_focus_mode_apps_count, uiState.settings.appDisguises.size),
                    onClick = { onNavigate(LauncherScreen.Disguises) }
                )
                SettingRow(
                    label = stringResource(R.string.settings_app_order),
                    value = uiState.settings.appOrder,
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

                SettingsSection("SYSTEM INTEGRATION")
                ToggleRow(
                    label = "Grayscale (E-Ink) Mode",
                    selected = uiState.settings.grayscaleEnabled,
                    onClick = onToggleGrayscale
                )
                ToggleRow(
                    label = "Zero-Apps Schedule (22:00 - 07:00)",
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
                    value = uiState.settings.textSize,
                    onClick = onCycleTextSize
                )
                SettingRow(
                    label = "Theme Accent",
                    value = uiState.settings.themeAccent,
                    onClick = onCycleThemeAccent
                )
                Spacer(Modifier.height(28.dp))

                SettingsSection(stringResource(R.string.settings_section_about))
                SettingRow(stringResource(R.string.app_name), value = "v${appVersionText()}")
                SettingRow(stringResource(R.string.settings_privacy), onClick = { onNavigate(LauncherScreen.Privacy) })
                Spacer(Modifier.height(32.dp))
                Text(
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

@Composable
private fun AppSelectorScreen(
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
            Text(title, color = LocalFocusPrimary.current, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(description, color = FocusMuted, fontSize = 12.sp, lineHeight = 17.sp)
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
private fun AppDisguiseScreen(
    uiState: LauncherUiState,
    onSearchChange: (String) -> Unit,
    onDisguiseChange: (InstalledApp, String) -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.Disguises, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.disguises_title),
                color = LocalFocusPrimary.current,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
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
private fun AppQuotaScreen(
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
                Text(
                    text = stringResource(R.string.quotas_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
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
private fun ContextProfilesScreen(
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
                Text(
                    text = stringResource(R.string.profiles_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
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
private fun ProfileAppSelectorScreen(
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
            Text(title, color = LocalFocusPrimary.current, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(description, color = FocusMuted, fontSize = 12.sp, lineHeight = 17.sp)
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
private fun NotificationBatchingScreen(
    uiState: LauncherUiState,
    onToggleEnabled: () -> Unit,
    onOpenListenerSettings: () -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.Notifications, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Text(
                    text = stringResource(R.string.notifications_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
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

@Composable
private fun PrivacyScreen(
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.Privacy, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Text(
                    text = stringResource(R.string.privacy_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
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
                Spacer(Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun FocusModeScreen(
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
                Text(
                    text = stringResource(R.string.focus_mode_title),
                    color = LocalFocusPrimary.current,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
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
                Text(
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
private fun DefaultHomeScreen(
    onOpenSettings: () -> Unit,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.DefaultHome, showBack = true, onNavigate = onNavigate, onMenuClick = onMenuClick) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.default_home_title),
                color = LocalFocusPrimary.current,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 27.sp
            )
            Spacer(Modifier.height(12.dp))
            Text(
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
private fun IntentPromptScreen(
    uiState: LauncherUiState,
    app: InstalledApp,
    onContinue: (String) -> Unit,
    onCancel: () -> Unit,
    onMenuClick: () -> Unit
) {
    var remaining by remember(app.id, uiState.settings.focusPauseSeconds) {
        mutableIntStateOf(uiState.settings.focusPauseSeconds)
    }
    LaunchedEffect(app.id, uiState.settings.focusPauseSeconds) {
        remaining = uiState.settings.focusPauseSeconds
        while (remaining > 0) {
            delay(1_000)
            remaining -= 1
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    var typedIntent by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalFocusBackground.current)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        TopMark(onMenuClick = onMenuClick)
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .background(LocalFocusPrimary.current.copy(alpha = 0.15f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(75.dp)
                    .scale(scale)
                    .background(LocalFocusPrimary.current.copy(alpha = 0.3f), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .scale(scale)
                    .background(LocalFocusPrimary.current, CircleShape)
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.intent_prompt_title, appDisplayLabel(app, uiState.settings)),
            color = LocalFocusPrimary.current,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 25.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Type your intent to unlock.",
            color = FocusMuted,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(22.dp))

        BasicTextField(
            value = typedIntent,
            onValueChange = { typedIntent = it },
            textStyle = TextStyle(color = LocalFocusPrimary.current, fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, FocusSecondary)
                .padding(12.dp),
            decorationBox = { innerTextField ->
                if (typedIntent.isEmpty()) {
                    Text("I am opening this because...", color = FocusSecondary, fontSize = 16.sp)
                }
                innerTextField()
            }
        )
        Spacer(Modifier.height(16.dp))
        
        Text(text = "Recent Intents", color = FocusSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Column(Modifier.fillMaxWidth()) {
            uiState.settings.intentLogs.take(3).forEach { log ->
                AppRow(label = log, onClick = { typedIntent = log })
            }
        }
        Spacer(Modifier.height(32.dp))

        FocusButton(
            text = if (remaining > 0) stringResource(R.string.intent_prompt_btn_wait, remaining) else stringResource(R.string.intent_prompt_btn_continue),
            enabled = remaining <= 0 && typedIntent.isNotBlank(),
            onClick = { onContinue(typedIntent) }
        )
        Spacer(Modifier.height(10.dp))
        GhostButton(text = stringResource(R.string.intent_prompt_btn_cancel), onClick = onCancel)
    }
}

@Composable
private fun QuotaBlockScreen(
    uiState: LauncherUiState,
    quotaBlock: QuotaBlock,
    onOpenUsageAccess: () -> Unit,
    onDismiss: () -> Unit,
    onMenuClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalFocusBackground.current)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        TopMark(onMenuClick = onMenuClick)
        Spacer(Modifier.height(42.dp))
        Eyebrow(stringResource(R.string.quota_block_eyebrow))
        Spacer(Modifier.height(10.dp))
        Text(
            text = if (quotaBlock.requiresUsageAccess) {
                stringResource(R.string.quota_block_usage_title)
            } else {
                stringResource(R.string.quota_block_title, appDisplayLabel(quotaBlock.app, uiState.settings))
            },
            color = LocalFocusPrimary.current,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 29.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = if (quotaBlock.requiresUsageAccess) {
                stringResource(R.string.quota_block_usage_desc, quotaBlock.quotaMinutes)
            } else {
                stringResource(
                    R.string.quota_block_desc,
                    formatUsageMinutes(quotaBlock.usedMillis),
                    quotaBlock.quotaMinutes
                )
            },
            color = FocusSecondary,
            fontSize = 13.sp,
            lineHeight = 19.sp
        )
        Spacer(Modifier.height(36.dp))
        if (quotaBlock.requiresUsageAccess) {
            FocusButton(text = stringResource(R.string.quota_block_open_usage_access), onClick = onOpenUsageAccess)
            Spacer(Modifier.height(10.dp))
        }
        GhostButton(text = stringResource(R.string.quota_block_back), onClick = onDismiss)
    }
}

@Composable
private fun FocusShell(
    selected: LauncherScreen,
    selectedScreen: LauncherScreen,
    showBack: Boolean = false,
    onNavigate: (LauncherScreen) -> Unit,
    onMenuClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalFocusBackground.current)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Header(
            selectedScreen = selectedScreen,
            showBack = showBack,
            onBack = { onNavigate(LauncherScreen.Settings) },
            onMenuClick = onMenuClick
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 24.dp)
        ) {
            content()
        }
        BottomNav(selected = selected, onNavigate = onNavigate)
    }
}

@Composable
private fun Header(
    selectedScreen: LauncherScreen,
    showBack: Boolean,
    onBack: () -> Unit,
    onMenuClick: () -> Unit
) {
    val systemTime = remember { mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))) }
    if (selectedScreen == LauncherScreen.Apps) {
        LaunchedEffect(Unit) {
            while (true) {
                systemTime.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("h:mm a"))
                delay(30_000)
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (selectedScreen == LauncherScreen.DefaultHome) {
            Text(
                text = "FOCUS",
                color = LocalFocusPrimary.current,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = LocalFocusPrimary.current,
                modifier = Modifier
                    .size(18.dp)
                    .clickable { onMenuClick() }
            )
        } else {
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (showBack) {
                    Text(
                        text = stringResource(R.string.header_back),
                        color = LocalFocusPrimary.current,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onBack() }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = LocalFocusPrimary.current,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable { onMenuClick() }
                    )
                }
            }

            Text(
                text = "FOCUS",
                color = LocalFocusPrimary.current,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (selectedScreen == LauncherScreen.Apps) {
                    Text(
                        text = systemTime.value.uppercase(),
                        color = FocusSecondary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TopMark(onMenuClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = LocalFocusPrimary.current,
            modifier = Modifier
                .size(18.dp)
                .clickable { onMenuClick() }
        )
        Text(
            text = "FOCUS",
            color = LocalFocusPrimary.current,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        Spacer(Modifier.width(18.dp)) // Equal width to balance the hamburger icon
    }
}

@Composable
private fun BottomNav(selected: LauncherScreen, onNavigate: (LauncherScreen) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(LocalFocusBackground.current)
            .border(BorderStroke(0.5.dp, FocusDivider))
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem(
            label = stringResource(R.string.nav_home),
            icon = Icons.Outlined.Home,
            selected = selected == LauncherScreen.Home
        ) { onNavigate(LauncherScreen.Home) }

        NavItem(
            label = stringResource(R.string.nav_apps),
            icon = Icons.Outlined.Apps,
            selected = selected == LauncherScreen.Apps
        ) { onNavigate(LauncherScreen.Apps) }

        NavItem(
            label = stringResource(R.string.nav_settings),
            icon = Icons.Outlined.Settings,
            selected = selected == LauncherScreen.Settings
        ) { onNavigate(LauncherScreen.Settings) }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(86.dp)
            .fillMaxSize()
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) LocalFocusPrimary.current else FocusMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = if (selected) LocalFocusPrimary.current else FocusMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SearchInput(value: String, placeholder: String, onValueChange: (String) -> Unit) {
    val multiplier = LocalTextSizeMultiplier.current
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = LocalFocusPrimary.current, fontSize = (14 * multiplier).sp, fontFamily = FontFamily.Monospace),
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(FocusInput)
            .padding(horizontal = 16.dp),
        decorationBox = { innerTextField ->
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f)) {
                    if (value.isBlank()) {
                        Text(
                            text = placeholder,
                            color = FocusMuted,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                    innerTextField()
                }
            }
        }
    )
}

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
private fun AppRow(label: String, onClick: () -> Unit, onLongClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = LocalFocusPrimary.current,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CheckRow(label: String, selected: Boolean, muted: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (muted) FocusMuted else LocalFocusPrimary.current,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(if (selected) LocalFocusPrimary.current else Color.Transparent)
                .border(BorderStroke(1.dp, if (selected) LocalFocusPrimary.current else FocusMuted))
        )
    }
}

@Composable
private fun DisguiseRow(app: InstalledApp, value: String, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Text(
            text = app.label,
            color = FocusSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = LocalFocusPrimary.current, fontSize = 15.sp),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(FocusInput)
                .padding(horizontal = 12.dp),
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        if (value.isBlank()) {
                            Text(
                                text = stringResource(R.string.disguises_placeholder),
                                color = FocusMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}

@Composable
@Composable
private fun QuotaRow(label: String, minutes: Int, usedMillis: Long = 0L, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = LocalFocusPrimary.current,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        val usageText = if (minutes <= 0) {
            stringResource(R.string.quotas_off)
        } else {
            val usedMinutes = formatUsageMinutes(usedMillis)
            "$usedMinutes / ${stringResource(R.string.quotas_minutes, minutes)}"
        }
        Text(
            text = usageText,
            color = if (minutes <= 0) FocusMuted else LocalFocusPrimary.current,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
    }
}
@Composable
private fun ProfileWifiField(label: String, value: String, onValueChange: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label,
            color = FocusSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(color = LocalFocusPrimary.current, fontSize = 15.sp),
            modifier = Modifier
                .fillMaxWidth()
                .height(42.dp)
                .background(FocusInput)
                .padding(horizontal = 12.dp),
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.weight(1f)) {
                        if (value.isBlank()) {
                            Text(
                                text = stringResource(R.string.profiles_wifi_placeholder),
                                color = FocusMuted,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp
                            )
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}

@Composable
private fun PrivacyTextBlock(heading: String, body: String) {
    SettingsSection(heading)
    Text(
        text = body,
        color = FocusSecondary,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(22.dp))
}

@Composable
private fun SettingRow(label: String, value: String? = null, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = LocalFocusPrimary.current,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value ?: if (onClick != null) ">" else "",
            color = FocusMuted,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun ToggleRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = LocalFocusPrimary.current, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(14.dp)
                .border(BorderStroke(1.dp, FocusMuted))
                .padding(2.dp),
            contentAlignment = if (selected) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (selected) LocalFocusPrimary.current else FocusMuted)
            )
        }
    }
}

@Composable
private fun StepRow(number: String, text: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(text = number, color = FocusMuted, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        Text(text = text, color = LocalFocusPrimary.current, fontSize = 16.sp)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun StaticReasonRow(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(42.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = text, color = FocusSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun FocusButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(if (enabled) LocalFocusPrimary.current else FocusMuted)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (enabled) Color.Black else FocusSecondary,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun GhostButton(text: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(BorderStroke(1.dp, FocusDivider))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = LocalFocusPrimary.current,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun SettingsSection(text: String) {
    Text(
        text = text,
        color = FocusMuted,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    )
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun Eyebrow(text: String) {
    Text(
        text = text,
        color = FocusMuted,
        fontFamily = FontFamily.Monospace,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    )
}

@Composable
private fun MutedEmptyText(text: String) {
    Text(
        text = text,
        color = FocusMuted,
        fontSize = 13.sp,
        modifier = Modifier.padding(vertical = 10.dp)
    )
}

@Composable
private fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: androidx.compose.ui.text.font.FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: androidx.compose.ui.text.style.TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((androidx.compose.ui.text.TextLayoutResult) -> Unit)? = null,
    style: TextStyle = androidx.compose.material3.LocalTextStyle.current
) {
    val multiplier = LocalTextSizeMultiplier.current
    val scaledFontSize = if (fontSize.isSp) {
        (fontSize.value * multiplier).sp
    } else {
        fontSize
    }
    val scaledLineHeight = if (lineHeight.isSp) {
        (lineHeight.value * multiplier).sp
    } else {
        lineHeight
    }
    val scaledStyle = if (style.fontSize.isSp) {
        style.copy(
            fontSize = (style.fontSize.value * multiplier).sp,
            lineHeight = if (style.lineHeight.isSp) (style.lineHeight.value * multiplier).sp else style.lineHeight
        )
    } else {
        style
    }
    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = scaledFontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = fontFamily,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = scaledLineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = scaledStyle
    )
}

private fun selectorApps(
    uiState: LauncherUiState,
    includeHidden: Boolean,
    includeOriginalLabel: Boolean = false
): List<InstalledApp> {
    val query = uiState.searchQuery.trim()
    return uiState.installedApps
        .filter { includeHidden || it.id !in uiState.settings.hiddenAppIds }
        .filter {
            query.isBlank() ||
                appDisplayLabel(it, uiState.settings).contains(query, ignoreCase = true) ||
                (includeOriginalLabel && it.label.contains(query, ignoreCase = true))
        }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { appDisplayLabel(it, uiState.settings) })
}

private fun appDisplayLabel(app: InstalledApp, settings: com.focuslauncher.data.model.LauncherSettings): String {
    return settings.appDisguises[app.id]?.takeIf { it.isNotBlank() } ?: app.label
}

private fun formatUsageMinutes(usedMillis: Long): Int {
    return (usedMillis / 60_000L).coerceAtLeast(0L).toInt()
}

private fun displayProfileName(profileId: String): String {
    return when (profileId) {
        "work" -> "Work"
        "home" -> "Home"
        "weekend" -> "Weekend"
        else -> "Default"
    }
}

private fun isTimeInSchedule(now: LocalDateTime, start: String, end: String): Boolean {
    return try {
        val startParts = start.split(":")
        val endParts = end.split(":")
        val startHour = startParts[0].toInt()
        val startMin = startParts[1].toInt()
        val endHour = endParts[0].toInt()
        val endMin = endParts[1].toInt()

        val startTime = now.withHour(startHour).withMinute(startMin).withSecond(0).withNano(0)
        val endTime = now.withHour(endHour).withMinute(endMin).withSecond(0).withNano(0)

        if (startTime.isBefore(endTime) || startTime.isEqual(endTime)) {
            (now.isEqual(startTime) || now.isAfter(startTime)) && now.isBefore(endTime)
        } else {
            (now.isEqual(startTime) || now.isAfter(startTime)) || now.isBefore(endTime)
        }
    } catch (e: Exception) {
        false
    }
}

private fun appVersionText(): String = "1.1.1"

@Composable
private fun StatsScreen(
    uiState: LauncherUiState,
    onMenuClick: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.Stats, showBack = true, onNavigate = {}, onMenuClick = onMenuClick) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Text(
                    text = "DIGITAL STOICISM",
                    color = LocalFocusPrimary.current,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "A minimalist environment for reclaiming attention.",
                    color = LocalFocusPrimary.current,
                    fontSize = 14.sp,
                )
                Spacer(Modifier.height(28.dp))
                
                SettingsSection("FRICTION DEFLECTIONS")
                val deflections = uiState.settings.appDeflectionCounts
                if (deflections.isEmpty()) {
                    MutedEmptyText("No deflections yet.")
                } else {
                    deflections.entries.sortedByDescending { it.value }.forEach { (pkg, count) ->
                        val app = uiState.installedApps.find { it.packageName == pkg }
                        val label = app?.let { appDisplayLabel(it, uiState.settings) } ?: pkg
                        SettingRow(label = label, value = count.toString())
                    }
                }
                Spacer(Modifier.height(28.dp))
                
                SettingsSection("LAUNCH VELOCITY")
                val launches = uiState.settings.appLaunchCounts
                if (launches.isEmpty()) {
                    MutedEmptyText("No launches yet.")
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
