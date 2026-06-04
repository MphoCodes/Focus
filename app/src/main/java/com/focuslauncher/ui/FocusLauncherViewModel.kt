package com.focuslauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuslauncher.data.model.InstalledApp
import com.focuslauncher.data.model.LauncherSettings
import com.focuslauncher.data.repository.AppsRepository
import com.focuslauncher.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class LauncherScreen {
    Home,
    Apps,
    Settings,
    Favorites,
    Hidden,
    FocusMode,
    DefaultHome
}

data class LauncherUiState(
    val screen: LauncherScreen = LauncherScreen.Home,
    val installedApps: List<InstalledApp> = emptyList(),
    val visibleApps: List<InstalledApp> = emptyList(),
    val favoriteApps: List<InstalledApp> = emptyList(),
    val searchQuery: String = "",
    val settings: LauncherSettings = LauncherSettings(),
    val pendingPromptApp: InstalledApp? = null,
    val loading: Boolean = true
)

class FocusLauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val appsRepository = AppsRepository(application.applicationContext)
    private val settingsRepository = SettingsRepository(application.applicationContext)
    private val installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val searchQuery = MutableStateFlow("")
    private val screen = MutableStateFlow(LauncherScreen.Home)
    private val pendingPromptApp = MutableStateFlow<InstalledApp?>(null)
    private val loading = MutableStateFlow(true)

    private val baseUiState = combine(
        installedApps,
        settingsRepository.settings,
        searchQuery,
        screen,
        pendingPromptApp
    ) { apps: List<InstalledApp>,
        settings: LauncherSettings,
        query: String,
        currentScreen: LauncherScreen,
        promptApp: InstalledApp? ->
        val normalizedQuery = query.trim()
        val visibleApps = apps
            .filterNot { it.id in settings.hiddenAppIds }
            .filter { normalizedQuery.isBlank() || it.label.contains(normalizedQuery, ignoreCase = true) }
        val favorites = apps
            .filter { it.id in settings.favoriteAppIds && it.id !in settings.hiddenAppIds }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })

        LauncherUiState(
            screen = currentScreen,
            installedApps = apps,
            visibleApps = visibleApps,
            favoriteApps = favorites,
            searchQuery = query,
            settings = settings,
            pendingPromptApp = promptApp,
            loading = false
        )
    }

    val uiState = combine(baseUiState, loading) { state, isLoading ->
        state.copy(loading = isLoading)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LauncherUiState()
    )

    init {
        refreshApps()
    }

    fun refreshApps() {
        viewModelScope.launch {
            loading.value = true
            installedApps.value = withContext(Dispatchers.IO) { appsRepository.loadLaunchableApps() }
            loading.value = false
        }
    }

    fun navigateTo(target: LauncherScreen) {
        screen.value = target
        searchQuery.value = ""
    }

    fun updateSearch(query: String) {
        searchQuery.value = query
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsRepository.setOnboardingComplete(true)
            navigateTo(LauncherScreen.DefaultHome)
        }
    }

    fun toggleFavorite(app: InstalledApp) {
        val current = uiState.value.settings.favoriteAppIds.toMutableSet()
        if (!current.add(app.id)) current.remove(app.id)
        viewModelScope.launch { settingsRepository.setFavoriteAppIds(current) }
    }

    fun toggleHidden(app: InstalledApp) {
        val current = uiState.value.settings.hiddenAppIds.toMutableSet()
        if (!current.add(app.id)) current.remove(app.id)
        viewModelScope.launch { settingsRepository.setHiddenAppIds(current) }
    }

    fun toggleIntentPrompt(app: InstalledApp) {
        val current = uiState.value.settings.intentPromptAppIds.toMutableSet()
        if (!current.add(app.id)) current.remove(app.id)
        viewModelScope.launch { settingsRepository.setIntentPromptAppIds(current) }
    }

    fun cyclePauseDuration() {
        val next = when (uiState.value.settings.focusPauseSeconds) {
            0 -> 5
            5 -> 10
            10 -> 20
            else -> 0
        }
        viewModelScope.launch { settingsRepository.setFocusPauseSeconds(next) }
    }

    fun requestOpenApp(app: InstalledApp) {
        if (app.id in uiState.value.settings.intentPromptAppIds) {
            pendingPromptApp.value = app
        } else {
            appsRepository.openApp(app)
        }
    }

    fun confirmPromptOpen() {
        val app = pendingPromptApp.value ?: return
        pendingPromptApp.value = null
        appsRepository.openApp(app)
    }

    fun cancelPromptOpen() {
        pendingPromptApp.value = null
    }

    fun openDefaultHomeSettings() {
        appsRepository.openDefaultHomeSettings()
    }
}
