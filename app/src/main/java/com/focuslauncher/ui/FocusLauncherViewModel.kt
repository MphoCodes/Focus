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
    Disguises,
    Quotas,
    FocusMode,
    DefaultHome,
    Stats
}

data class LauncherUiState(
    val screen: LauncherScreen = LauncherScreen.Home,
    val installedApps: List<InstalledApp> = emptyList(),
    val visibleApps: List<InstalledApp> = emptyList(),
    val favoriteApps: List<InstalledApp> = emptyList(),
    val searchQuery: String = "",
    val settings: LauncherSettings = LauncherSettings(),
    val pendingPromptApp: InstalledApp? = null,
    val quotaBlock: QuotaBlock? = null,
    val usageAccessGranted: Boolean = false,
    val loading: Boolean = true
)

data class QuotaBlock(
    val app: InstalledApp,
    val quotaMinutes: Int,
    val usedMillis: Long,
    val requiresUsageAccess: Boolean
)

class FocusLauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val appsRepository = AppsRepository(application.applicationContext)
    private val settingsRepository = SettingsRepository(application.applicationContext)
    private val installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val searchQuery = MutableStateFlow("")
    private val screen = MutableStateFlow(LauncherScreen.Home)
    private val pendingPromptApp = MutableStateFlow<InstalledApp?>(null)
    private val quotaBlock = MutableStateFlow<QuotaBlock?>(null)
    private val usageAccessGranted = MutableStateFlow(false)
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

        val sortedApps = when (settings.appOrder) {
            "Installation Date" -> {
                val pm = getApplication<Application>().packageManager
                apps.sortedWith(
                    compareBy<InstalledApp> { app ->
                        try {
                            @Suppress("DEPRECATION")
                            pm.getPackageInfo(app.packageName, 0).firstInstallTime
                        } catch (e: Exception) {
                            0L
                        }
                    }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.displayLabel(settings) }
                )
            }
            "Most Used" -> {
                apps.sortedWith(
                    compareByDescending<InstalledApp> { app ->
                        settings.appLaunchCounts[app.packageName] ?: 0
                    }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.displayLabel(settings) }
                )
            }
            else -> { // "Alphabetical"
                apps.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.displayLabel(settings) })
            }
        }

        val visibleApps = sortedApps
            .filterNot { it.id in settings.hiddenAppIds }
            .filter { normalizedQuery.isBlank() || it.displayLabel(settings).contains(normalizedQuery, ignoreCase = true) }
        val favorites = apps
            .filter { it.id in settings.favoriteAppIds && it.id !in settings.hiddenAppIds }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.displayLabel(settings) })

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

    val uiState = combine(baseUiState, loading, quotaBlock, usageAccessGranted) { state, isLoading, currentQuotaBlock, hasUsageAccess ->
        state.copy(
            loading = isLoading,
            quotaBlock = currentQuotaBlock,
            usageAccessGranted = hasUsageAccess
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LauncherUiState()
    )

    init {
        refreshApps()
        refreshUsageAccess()
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
        refreshUsageAccess()
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

    fun toggleLocalDataOnly() {
        val current = uiState.value.settings.localDataOnly
        viewModelScope.launch { settingsRepository.setLocalDataOnly(!current) }
    }

    fun toggleBlackThemeEnabled() {
        val current = uiState.value.settings.blackThemeEnabled
        viewModelScope.launch { settingsRepository.setBlackThemeEnabled(!current) }
    }

    fun toggleGrayscaleEnabled() {
        val current = uiState.value.settings.grayscaleEnabled
        viewModelScope.launch { settingsRepository.setGrayscaleEnabled(!current) }
    }

    fun toggleZeroAppsScheduleEnabled() {
        val current = uiState.value.settings.zeroAppsScheduleEnabled
        viewModelScope.launch { settingsRepository.setZeroAppsScheduleEnabled(!current) }
    }

    fun setZeroAppsStartTime(time: String) {
        viewModelScope.launch { settingsRepository.setZeroAppsStartTime(time) }
    }

    fun setZeroAppsEndTime(time: String) {
        viewModelScope.launch { settingsRepository.setZeroAppsEndTime(time) }
    }

    fun openAppInfo(app: InstalledApp) {
        appsRepository.openAppInfo(app)
    }

    fun cycleTextSize() {
        val next = when (uiState.value.settings.textSize) {
            "Small" -> "Standard"
            "Standard" -> "Large"
            "Large" -> "Small"
            else -> "Standard"
        }
        viewModelScope.launch { settingsRepository.setTextSize(next) }
    }

    fun cycleThemeAccent() {
        val next = when (uiState.value.settings.themeAccent) {
            "E-Ink Grey" -> "Amber Mono"
            "Amber Mono" -> "Forest Green"
            "Forest Green" -> "E-Ink Grey"
            else -> "E-Ink Grey"
        }
        viewModelScope.launch { settingsRepository.setThemeAccent(next) }
    }

    fun cycleAppOrder() {
        val next = when (uiState.value.settings.appOrder) {
            "Alphabetical" -> "Installation Date"
            "Installation Date" -> "Most Used"
            "Most Used" -> "Alphabetical"
            else -> "Alphabetical"
        }
        viewModelScope.launch { settingsRepository.setAppOrder(next) }
    }

    fun setAppDisguise(app: InstalledApp, disguise: String) {
        viewModelScope.launch { settingsRepository.setAppDisguise(app.id, disguise) }
    }

    fun cycleAppDailyQuota(app: InstalledApp) {
        val current = uiState.value.settings.appDailyQuotaMinutes[app.id] ?: 0
        val next = when (current) {
            0 -> 15
            15 -> 30
            30 -> 60
            60 -> 120
            else -> 0
        }
        viewModelScope.launch { settingsRepository.setAppDailyQuotaMinutes(app.id, next) }
    }

    private suspend fun launchApp(app: InstalledApp) {
        if (appsRepository.openApp(app)) {
            settingsRepository.incrementAppLaunchCount(app.packageName)
        }
    }

    fun requestOpenApp(app: InstalledApp) {
        viewModelScope.launch {
            if (blockIfQuotaExceeded(app)) return@launch

            if (app.id in uiState.value.settings.intentPromptAppIds) {
                pendingPromptApp.value = app
            } else {
                launchApp(app)
            }
        }
    }

    fun confirmPromptOpen(intent: String) {
        val app = pendingPromptApp.value ?: return
        pendingPromptApp.value = null
        viewModelScope.launch {
            if (blockIfQuotaExceeded(app)) return@launch
            settingsRepository.addIntentLog(intent)
            launchApp(app)
        }
    }

    fun cancelPromptOpen() {
        val app = pendingPromptApp.value ?: return
        pendingPromptApp.value = null
        viewModelScope.launch {
            settingsRepository.incrementAppDeflectionCount(app.packageName)
        }
    }

    fun openDefaultHomeSettings() {
        appsRepository.openDefaultHomeSettings()
    }

    fun openUsageAccessSettings() {
        appsRepository.openUsageAccessSettings()
    }

    fun dismissQuotaBlock() {
        quotaBlock.value = null
    }

    fun refreshUsageAccess() {
        usageAccessGranted.value = appsRepository.hasUsageAccess()
    }

    private suspend fun blockIfQuotaExceeded(app: InstalledApp): Boolean {
        val quotaMinutes = uiState.value.settings.appDailyQuotaMinutes[app.id] ?: return false
        if (quotaMinutes <= 0) return false

        val hasUsageAccess = appsRepository.hasUsageAccess()
        usageAccessGranted.value = hasUsageAccess
        if (!hasUsageAccess) {
            quotaBlock.value = QuotaBlock(
                app = app,
                quotaMinutes = quotaMinutes,
                usedMillis = 0L,
                requiresUsageAccess = true
            )
            return true
        }

        val usedMillis = withContext(Dispatchers.IO) {
            appsRepository.getTodayUsageMillis(app.packageName)
        }
        if (usedMillis >= quotaMinutes * 60_000L) {
            quotaBlock.value = QuotaBlock(
                app = app,
                quotaMinutes = quotaMinutes,
                usedMillis = usedMillis,
                requiresUsageAccess = false
            )
            return true
        }

        return false
    }
}

private fun InstalledApp.displayLabel(settings: LauncherSettings): String {
    return settings.appDisguises[id]?.takeIf { it.isNotBlank() } ?: label
}
