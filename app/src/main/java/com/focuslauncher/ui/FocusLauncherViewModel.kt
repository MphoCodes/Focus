package com.focuslauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuslauncher.data.model.InstalledApp
import com.focuslauncher.data.model.LauncherSettings
import com.focuslauncher.data.repository.AppsRepository
import com.focuslauncher.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDateTime

enum class LauncherScreen {
    Home,
    Apps,
    Settings,
    Favorites,
    Hidden,
    Disguises,
    Quotas,
    ContextProfiles,
    WorkProfileApps,
    HomeProfileApps,
    WeekendProfileApps,
    Notifications,
    EssentialNotifications,
    Privacy,
    FocusMode,
    DefaultHome,
    Stats
    ,DataManagement
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
    val notificationListenerAccessGranted: Boolean = false,
    val activeContextProfileId: String = "default",
    val currentWifiSsid: String? = null,
    val loading: Boolean = true
)

data class QuotaBlock(
    val app: InstalledApp,
    val quotaMinutes: Int,
    val usedMillis: Long,
    val requiresUsageAccess: Boolean
)

private data class ContextSignal(
    val now: LocalDateTime = LocalDateTime.now(),
    val wifiSsid: String? = null
)

private data class SystemAccess(
    val usageAccessGranted: Boolean = false,
    val notificationListenerAccessGranted: Boolean = false
)

class FocusLauncherViewModel(application: Application) : AndroidViewModel(application) {
    private val appsRepository = AppsRepository(application.applicationContext)
    private val settingsRepository = SettingsRepository(application.applicationContext)
    private val installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val searchQuery = MutableStateFlow("")
    private val screen = MutableStateFlow(LauncherScreen.Home)
    private val pendingPromptApp = MutableStateFlow<InstalledApp?>(null)
    private val quotaBlock = MutableStateFlow<QuotaBlock?>(null)
    private val systemAccess = MutableStateFlow(SystemAccess())
    private val contextSignal = MutableStateFlow(ContextSignal())
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

    val uiState = combine(baseUiState, loading, quotaBlock, systemAccess, contextSignal) { state, isLoading, currentQuotaBlock, access, signal ->
        val activeProfileId = resolveActiveContextProfile(state.settings, signal)
        state.copy(
            visibleApps = applyContextProfile(state.visibleApps, state.settings, activeProfileId),
            favoriteApps = applyContextProfile(state.favoriteApps, state.settings, activeProfileId),
            loading = isLoading,
            quotaBlock = currentQuotaBlock,
            usageAccessGranted = access.usageAccessGranted,
            notificationListenerAccessGranted = access.notificationListenerAccessGranted,
            activeContextProfileId = activeProfileId,
            currentWifiSsid = signal.wifiSsid
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LauncherUiState()
    )

    init {
        refreshApps()
        refreshSystemAccess()
        refreshContextSignal()
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                refreshContextSignal()
            }
        }
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
        refreshSystemAccess()
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

    fun toggleContextProfilesEnabled() {
        val current = uiState.value.settings.contextProfilesEnabled
        viewModelScope.launch { settingsRepository.setContextProfilesEnabled(!current) }
    }

    fun toggleContextProfilesAutoSwitch() {
        val current = uiState.value.settings.contextProfilesAutoSwitch
        viewModelScope.launch { settingsRepository.setContextProfilesAutoSwitch(!current) }
    }

    fun cycleManualContextProfile() {
        val next = when (uiState.value.settings.manualContextProfileId) {
            "default" -> "work"
            "work" -> "home"
            "home" -> "weekend"
            else -> "default"
        }
        viewModelScope.launch { settingsRepository.setManualContextProfileId(next) }
    }

    fun toggleWorkProfileApp(app: InstalledApp) {
        val current = uiState.value.settings.workProfileAppIds.toMutableSet()
        if (!current.add(app.id)) current.remove(app.id)
        viewModelScope.launch { settingsRepository.setWorkProfileAppIds(current) }
    }

    fun toggleHomeProfileApp(app: InstalledApp) {
        val current = uiState.value.settings.homeProfileAppIds.toMutableSet()
        if (!current.add(app.id)) current.remove(app.id)
        viewModelScope.launch { settingsRepository.setHomeProfileAppIds(current) }
    }

    fun toggleWeekendProfileApp(app: InstalledApp) {
        val current = uiState.value.settings.weekendProfileAppIds.toMutableSet()
        if (!current.add(app.id)) current.remove(app.id)
        viewModelScope.launch { settingsRepository.setWeekendProfileAppIds(current) }
    }

    fun setProfileWifiSsid(profileId: String, ssid: String) {
        viewModelScope.launch { settingsRepository.setProfileWifiSsid(profileId, ssid) }
    }

    fun toggleNotificationBatchingEnabled() {
        val current = uiState.value.settings.notificationBatchingEnabled
        viewModelScope.launch { settingsRepository.setNotificationBatchingEnabled(!current) }
    }

    fun toggleEssentialNotificationApp(app: InstalledApp) {
        val current = uiState.value.settings.essentialNotificationAppIds.toMutableSet()
        if (!current.add(app.id)) current.remove(app.id)
        viewModelScope.launch { settingsRepository.setEssentialNotificationAppIds(current) }
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

    fun openNotificationListenerSettings() {
        appsRepository.openNotificationListenerSettings()
    }

    fun clearIntentLogs() {
        viewModelScope.launch { settingsRepository.clearIntentLogs() }
    }

    fun clearStats() {
        viewModelScope.launch { settingsRepository.clearStats() }
    }

    fun clearPendingNotifications() {
        viewModelScope.launch { settingsRepository.clearBatchedNotificationCounts() }
    }

    fun clearAllSettings() {
        viewModelScope.launch { settingsRepository.clearAllSettings() }
    }

    fun dismissQuotaBlock() {
        quotaBlock.value = null
    }

    fun refreshUsageAccess() {
        systemAccess.value = systemAccess.value.copy(usageAccessGranted = appsRepository.hasUsageAccess())
    }

    fun refreshNotificationListenerAccess() {
        systemAccess.value = systemAccess.value.copy(
            notificationListenerAccessGranted = appsRepository.hasNotificationListenerAccess()
        )
    }

    fun refreshSystemAccess() {
        systemAccess.value = SystemAccess(
            usageAccessGranted = appsRepository.hasUsageAccess(),
            notificationListenerAccessGranted = appsRepository.hasNotificationListenerAccess()
        )
    }

    fun refreshContextSignal() {
        viewModelScope.launch {
            val wifiSsid = withContext(Dispatchers.IO) { appsRepository.getCurrentWifiSsid() }
            contextSignal.value = ContextSignal(now = LocalDateTime.now(), wifiSsid = wifiSsid)
        }
    }

    private suspend fun blockIfQuotaExceeded(app: InstalledApp): Boolean {
        val quotaMinutes = uiState.value.settings.appDailyQuotaMinutes[app.id] ?: return false
        if (quotaMinutes <= 0) return false

        val hasUsageAccess = appsRepository.hasUsageAccess()
        systemAccess.value = systemAccess.value.copy(usageAccessGranted = hasUsageAccess)
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

private fun applyContextProfile(
    apps: List<InstalledApp>,
    settings: LauncherSettings,
    activeProfileId: String
): List<InstalledApp> {
    if (!settings.contextProfilesEnabled || activeProfileId == "default") return apps
    val allowedIds = profileAppIds(settings, activeProfileId)
    if (allowedIds.isEmpty()) return apps
    return apps.filter { it.id in allowedIds }
}

private fun profileAppIds(settings: LauncherSettings, profileId: String): Set<String> {
    return when (profileId) {
        "work" -> settings.workProfileAppIds
        "home" -> settings.homeProfileAppIds
        "weekend" -> settings.weekendProfileAppIds
        else -> emptySet()
    }
}

private fun resolveActiveContextProfile(settings: LauncherSettings, signal: ContextSignal): String {
    if (!settings.contextProfilesEnabled) return "default"
    if (!settings.contextProfilesAutoSwitch) return settings.manualContextProfileId

    signal.wifiSsid?.let { ssid ->
        when (ssid) {
            settings.workProfileWifiSsid.takeIf { it.isNotBlank() } -> return "work"
            settings.homeProfileWifiSsid.takeIf { it.isNotBlank() } -> return "home"
            settings.weekendProfileWifiSsid.takeIf { it.isNotBlank() } -> return "weekend"
        }
    }

    val day = signal.now.dayOfWeek
    if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return "weekend"

    val hour = signal.now.hour
    if (hour in 9..16) return "work"

    return "home"
}
