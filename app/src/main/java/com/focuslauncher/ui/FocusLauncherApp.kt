package com.focuslauncher.ui

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focuslauncher.R
import com.focuslauncher.data.model.InstalledApp
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val FocusBackground = Color(0xFF000000) // Mockup has pure black backgrounds
private val FocusPrimary = Color(0xFFFFFFFF)
private val FocusSecondary = Color(0xFF8E8E8E) // Clean medium gray
private val FocusMuted = Color(0xFF4E4E4E) // Darker gray for less contrast elements
private val FocusDivider = Color(0xFF161616) // Subtle dividers
private val FocusInput = Color(0xFF0F0F0F) // Lighter box for inputs

@Composable
fun FocusLauncherApp(viewModel: FocusLauncherViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pendingPromptApp = uiState.pendingPromptApp

    Surface(color = FocusBackground, modifier = Modifier.fillMaxSize()) {
        when {
            pendingPromptApp != null -> IntentPromptScreen(
                uiState = uiState,
                app = pendingPromptApp,
                onContinue = viewModel::confirmPromptOpen,
                onCancel = viewModel::cancelPromptOpen
            )

            !uiState.settings.onboardingComplete -> OnboardingScreen(
                onGetStarted = viewModel::completeOnboarding
            )

            else -> when (uiState.screen) {
                LauncherScreen.Home -> HomeScreen(
                    uiState = uiState,
                    onSearchChange = viewModel::updateSearch,
                    onOpenApp = viewModel::requestOpenApp,
                    onNavigate = viewModel::navigateTo
                )

                LauncherScreen.Apps -> AppsScreen(
                    uiState = uiState,
                    onSearchChange = viewModel::updateSearch,
                    onOpenApp = viewModel::requestOpenApp,
                    onNavigate = viewModel::navigateTo
                )

                LauncherScreen.Settings -> SettingsScreen(
                    uiState = uiState,
                    onNavigate = viewModel::navigateTo,
                    onCyclePauseDuration = viewModel::cyclePauseDuration
                )

                LauncherScreen.Favorites -> AppSelectorScreen(
                    title = stringResource(R.string.favorites_selector_title),
                    description = stringResource(R.string.favorites_selector_desc),
                    uiState = uiState,
                    selectedIds = uiState.settings.favoriteAppIds,
                    onSearchChange = viewModel::updateSearch,
                    onToggleApp = viewModel::toggleFavorite,
                    onNavigate = viewModel::navigateTo
                )

                LauncherScreen.Hidden -> AppSelectorScreen(
                    title = stringResource(R.string.hidden_selector_title),
                    description = stringResource(R.string.hidden_selector_desc),
                    uiState = uiState,
                    selectedIds = uiState.settings.hiddenAppIds,
                    includeHidden = true,
                    onSearchChange = viewModel::updateSearch,
                    onToggleApp = viewModel::toggleHidden,
                    onNavigate = viewModel::navigateTo
                )

                LauncherScreen.FocusMode -> FocusModeScreen(
                    uiState = uiState,
                    onSearchChange = viewModel::updateSearch,
                    onToggleApp = viewModel::toggleIntentPrompt,
                    onCyclePauseDuration = viewModel::cyclePauseDuration,
                    onNavigate = viewModel::navigateTo
                )

                LauncherScreen.DefaultHome -> DefaultHomeScreen(
                    onOpenSettings = viewModel::openDefaultHomeSettings,
                    onNavigate = viewModel::navigateTo
                )
            }
        }
    }
}

@Composable
private fun OnboardingScreen(onGetStarted: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 28.dp, vertical = 24.dp)
    ) {
        TopMark()
        Spacer(Modifier.height(36.dp))
        Box(
            modifier = Modifier
                .size(44.dp)
                .border(BorderStroke(1.dp, FocusSecondary)),
            contentAlignment = Alignment.Center
        ) {
            Text("F", color = FocusPrimary, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = stringResource(R.string.onboarding_title),
            color = FocusPrimary,
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
    onNavigate: (LauncherScreen) -> Unit
) {
    FocusShell(selected = LauncherScreen.Home, selectedScreen = LauncherScreen.Home, onNavigate = onNavigate) {
        var now by remember { mutableStateOf(LocalDateTime.now()) }
        LaunchedEffect(Unit) {
            while (true) {
                now = LocalDateTime.now()
                delay(30_000)
            }
        }

        Column(Modifier.fillMaxSize()) {
            Text(
                text = now.format(DateTimeFormatter.ofPattern("HH:mm")),
                color = FocusPrimary,
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
                color = FocusPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(28.dp))
            Eyebrow(stringResource(R.string.home_favorites_header))
            Spacer(Modifier.height(8.dp))
            val homeList = if (uiState.searchQuery.isBlank()) uiState.favoriteApps else uiState.visibleApps
            if (homeList.isEmpty()) {
                MutedEmptyText(
                    if (uiState.searchQuery.isBlank()) stringResource(R.string.home_no_favorites) else stringResource(R.string.home_no_apps_found)
                )
            } else {
                homeList.take(7).forEach { app ->
                    AppRow(label = app.label, onClick = { onOpenApp(app) })
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
    onNavigate: (LauncherScreen) -> Unit
) {
    FocusShell(selected = LauncherScreen.Apps, selectedScreen = LauncherScreen.Apps, onNavigate = onNavigate) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.apps_title),
                color = FocusPrimary,
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
            LazyColumn(Modifier.fillMaxSize()) {
                items(uiState.visibleApps, key = { it.id }) { app ->
                    AppRow(label = app.label, onClick = { onOpenApp(app) })
                }
                if (uiState.loading) {
                    item { MutedEmptyText(stringResource(R.string.apps_loading)) }
                } else if (uiState.visibleApps.isEmpty()) {
                    item { MutedEmptyText(stringResource(R.string.home_no_apps_found)) }
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    uiState: LauncherUiState,
    onNavigate: (LauncherScreen) -> Unit,
    onCyclePauseDuration: () -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.Settings, onNavigate = onNavigate) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Text(
                    text = stringResource(R.string.settings_title),
                    color = FocusPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(28.dp))
                SettingsSection(stringResource(R.string.settings_section_home_screen))
                SettingRow(stringResource(R.string.settings_fav_apps), onClick = { onNavigate(LauncherScreen.Favorites) })
                SettingRow(stringResource(R.string.settings_hidden_apps), onClick = { onNavigate(LauncherScreen.Hidden) })
                SettingRow(stringResource(R.string.settings_app_order), value = stringResource(R.string.settings_app_order_val))
                SettingRow(stringResource(R.string.settings_set_default), onClick = { onNavigate(LauncherScreen.DefaultHome) })
                Spacer(Modifier.height(28.dp))

                SettingsSection(stringResource(R.string.settings_section_focus))
                SettingRow(
                    label = stringResource(R.string.settings_focus_mode),
                    value = stringResource(R.string.settings_focus_mode_apps_count, uiState.settings.intentPromptAppIds.size),
                    onClick = { onNavigate(LauncherScreen.FocusMode) }
                )
                SettingRow(stringResource(R.string.settings_intent_prompts), onClick = { onNavigate(LauncherScreen.FocusMode) })
                SettingRow(
                    label = stringResource(R.string.settings_pause_duration),
                    value = if (uiState.settings.focusPauseSeconds == 0) stringResource(R.string.settings_pause_duration_off) else stringResource(R.string.settings_pause_duration_sec, uiState.settings.focusPauseSeconds),
                    onClick = onCyclePauseDuration
                )
                Spacer(Modifier.height(28.dp))

                SettingsSection(stringResource(R.string.settings_section_privacy))
                ToggleRow(stringResource(R.string.settings_usage_access), selected = false)
                ToggleRow(stringResource(R.string.settings_local_data_only), selected = true)
                Spacer(Modifier.height(28.dp))

                SettingsSection(stringResource(R.string.settings_section_appearance))
                ToggleRow(stringResource(R.string.settings_black_theme), selected = true)
                SettingRow(stringResource(R.string.settings_text_size), value = stringResource(R.string.settings_text_size_val))
                Spacer(Modifier.height(28.dp))

                SettingsSection(stringResource(R.string.settings_section_about))
                SettingRow(stringResource(R.string.app_name), value = "v${appVersionText()}")
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
    onNavigate: (LauncherScreen) -> Unit
) {
    FocusShell(selected = LauncherScreen.Apps, selectedScreen = LauncherScreen.Apps, showBack = true, onNavigate = onNavigate) {
        Column(Modifier.fillMaxSize()) {
            Text(title, color = FocusPrimary, fontSize = 22.sp, fontWeight = FontWeight.Bold)
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
                        label = app.label,
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
private fun FocusModeScreen(
    uiState: LauncherUiState,
    onSearchChange: (String) -> Unit,
    onToggleApp: (InstalledApp) -> Unit,
    onCyclePauseDuration: () -> Unit,
    onNavigate: (LauncherScreen) -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.FocusMode, showBack = true, onNavigate = onNavigate) {
        val apps = selectorApps(uiState, includeHidden = true)
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                Text(
                    text = stringResource(R.string.focus_mode_title),
                    color = FocusPrimary,
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
                    label = app.label,
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
    onNavigate: (LauncherScreen) -> Unit
) {
    FocusShell(selected = LauncherScreen.Settings, selectedScreen = LauncherScreen.DefaultHome, showBack = true, onNavigate = onNavigate) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.default_home_title),
                color = FocusPrimary,
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
    onContinue: () -> Unit,
    onCancel: () -> Unit
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp)
    ) {
        TopMark()
        Spacer(Modifier.height(72.dp))
        Text(
            text = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm")),
            color = Color(0xFF111111),
            fontSize = 62.sp,
            fontWeight = FontWeight.Light
        )
        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(FocusSecondary)
        )
        Spacer(Modifier.height(26.dp))
        Text(
            text = stringResource(R.string.intent_prompt_title, app.label),
            color = FocusPrimary,
            fontSize = 21.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 25.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.intent_prompt_subtitle),
            color = FocusMuted,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(22.dp))
        listOf(
            stringResource(R.string.intent_prompt_reason_message),
            stringResource(R.string.intent_prompt_reason_post),
            stringResource(R.string.intent_prompt_reason_work),
            stringResource(R.string.intent_prompt_reason_habit)
        ).forEach { reason ->
            StaticReasonRow(reason)
        }
        Spacer(Modifier.height(18.dp))
        FocusButton(
            text = if (remaining > 0) stringResource(R.string.intent_prompt_btn_wait, remaining) else stringResource(R.string.intent_prompt_btn_continue),
            enabled = remaining <= 0,
            onClick = onContinue
        )
        Spacer(Modifier.height(10.dp))
        GhostButton(text = stringResource(R.string.intent_prompt_btn_cancel), onClick = onCancel)
    }
}

@Composable
private fun FocusShell(
    selected: LauncherScreen,
    selectedScreen: LauncherScreen,
    showBack: Boolean = false,
    onNavigate: (LauncherScreen) -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FocusBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Header(
            selectedScreen = selectedScreen,
            showBack = showBack,
            onBack = { onNavigate(LauncherScreen.Settings) }
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
    onBack: () -> Unit
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
            // Set as Default Screen (4th mockup): "FOCUS" on left, hamburger menu icon on right
            Text(
                text = "FOCUS",
                color = FocusPrimary,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = FocusPrimary,
                modifier = Modifier.size(18.dp)
            )
        } else {
            // Standard Screen header: hamburger menu or back button on left, "FOCUS" center, drawer time on right
            Box(
                modifier = Modifier.width(48.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (showBack) {
                    Text(
                        text = stringResource(R.string.header_back),
                        color = FocusPrimary,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onBack() }
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = FocusPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = "FOCUS",
                color = FocusPrimary,
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
private fun TopMark() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Menu,
            contentDescription = "Menu",
            tint = FocusPrimary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "FOCUS",
            color = FocusPrimary,
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
            .background(FocusBackground)
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
            tint = if (selected) FocusPrimary else FocusMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = if (selected) FocusPrimary else FocusMuted,
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
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = FocusPrimary, fontSize = 14.sp, fontFamily = FontFamily.Monospace),
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

@Composable
private fun AppRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = FocusPrimary,
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
            color = if (muted) FocusMuted else FocusPrimary,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(14.dp)
                .background(if (selected) FocusPrimary else Color.Transparent)
                .border(BorderStroke(1.dp, if (selected) FocusPrimary else FocusMuted))
        )
    }
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
            color = FocusPrimary,
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
private fun ToggleRow(label: String, selected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = FocusPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
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
                    .background(if (selected) FocusPrimary else FocusMuted)
            )
        }
    }
}

@Composable
private fun StepRow(number: String, text: String) {
    Column(Modifier.fillMaxWidth()) {
        Text(text = number, color = FocusMuted, fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        Spacer(Modifier.height(6.dp))
        Text(text = text, color = FocusPrimary, fontSize = 16.sp)
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
            .background(if (enabled) FocusPrimary else FocusMuted)
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
            color = FocusPrimary,
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

private fun selectorApps(uiState: LauncherUiState, includeHidden: Boolean): List<InstalledApp> {
    val query = uiState.searchQuery.trim()
    return uiState.installedApps
        .filter { includeHidden || it.id !in uiState.settings.hiddenAppIds }
        .filter { query.isBlank() || it.label.contains(query, ignoreCase = true) }
}

private fun appVersionText(): String = "1.0.0"
