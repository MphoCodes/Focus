package com.focuslauncher.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.focuslauncher.data.model.InstalledApp
import com.focuslauncher.data.model.LauncherSettings
import com.focuslauncher.data.model.AppOrder
import com.focuslauncher.data.model.TextSize
import com.focuslauncher.data.model.ThemeAccent
import com.focuslauncher.data.model.ProfileId
import com.focuslauncher.data.model.QuotaPresets
import com.focuslauncher.data.model.Workspace
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
    Workspaces,
    WorkspaceDetail,
    WorkspaceApps,
    WorkProfileApps,
    HomeProfileApps,
    WeekendProfileApps,
    Notifications,
    EssentialNotifications,
    Privacy,
    FocusMode,
    DefaultHome,
    Stats,
    DataManagement,
    Setup
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
    val quotaUsages: Map<String, Long> = emptyMap(),
    val selectedWorkspaceId: String? = null,
    val selectedWorkspace: Workspace? = null,
    val usageAccessGranted: Boolean = false,
    val notificationListenerAccessGranted: Boolean = false,
    val activeContextProfileId: ProfileId = ProfileId.DEFAULT,
    val currentWifiSsid: String? = null,
    val loading: Boolean = true
)

data class QuotaState(
    val block: QuotaBlock? = null,
    val usages: Map<String, Long> = emptyMap()
)

data class QuotaBlock(
    val app: InstalledApp,
    val quotaMinutes: Int,
    val usedMillis: Long,
    val requiresUsageAccess: Boolean
)

internal data class ContextSignal(
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
    private val selectedWorkspaceId = MutableStateFlow<String?>(null)
    private val quotaState = MutableStateFlow(QuotaState())
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
            AppOrder.INSTALLATION_DATE -> {
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
            AppOrder.MOST_USED -> {
                apps.sortedWith(
                    compareByDescending<InstalledApp> { app ->
                        settings.appLaunchCounts[app.packageName] ?: 0
                    }.thenBy(String.CASE_INSENSITIVE_ORDER) { it.displayLabel(settings) }
                )
            }
            else -> { // Alphabetical
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

    val uiState = combine(baseUiState, loading, quotaState, systemAccess, contextSignal, selectedWorkspaceId) { values ->
        val state = values[0] as LauncherUiState
        val isLoading = values[1] as Boolean
        val currentQuotaState = values[2] as QuotaState
        val access = values[3] as SystemAccess
        val signal = values[4] as ContextSignal
        val workspaceId = values[5] as String?
        val activeProfileId = resolveActiveContextProfile(state.settings, signal)
        state.copy(
            visibleApps = applyContextProfile(state.visibleApps, state.settings, activeProfileId),
            favoriteApps = applyContextProfile(state.favoriteApps, state.settings, activeProfileId),
            selectedWorkspaceId = workspaceId,
            selectedWorkspace = state.settings.workspaces.firstOrNull { it.id == workspaceId },
            loading = isLoading,
            quotaBlock = currentQuotaState.block,
            quotaUsages = currentQuotaState.usages,
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
        if (target != LauncherScreen.WorkspaceDetail && target != LauncherScreen.WorkspaceApps) {
            selectedWorkspaceId.value = null
        }
        refreshSystemAccess()
    }

    fun openWorkspace(workspace: Workspace) {
        selectedWorkspaceId.value = workspace.id
        screen.value = LauncherScreen.WorkspaceDetail
        searchQuery.value = ""
    }

    fun openWorkspaceApps(workspace: Workspace) {
        selectedWorkspaceId.value = workspace.id
        screen.value = LauncherScreen.WorkspaceApps
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

    fun toggleWorkspaceApp(app: InstalledApp) {
        val workspaceId = selectedWorkspaceId.value ?: return
        val workspaces = uiState.value.settings.workspaces.map { workspace ->
            if (workspace.id != workspaceId) return@map workspace
            val appIds = workspace.appIds.toMutableSet()
            if (!appIds.add(app.id)) appIds.remove(app.id)
            workspace.copy(appIds = appIds)
        }
        viewModelScope.launch { settingsRepository.setWorkspaces(workspaces) }
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
            TextSize.SMALL -> TextSize.STANDARD
            TextSize.STANDARD -> TextSize.LARGE
            TextSize.LARGE -> TextSize.SMALL
        }
        viewModelScope.launch { settingsRepository.setTextSize(next) }
    }

    fun cycleThemeAccent() {
        val next = when (uiState.value.settings.themeAccent) {
            ThemeAccent.E_INK_GREY -> ThemeAccent.AMBER_MONO
            ThemeAccent.AMBER_MONO -> ThemeAccent.FOREST_GREEN
            ThemeAccent.FOREST_GREEN -> ThemeAccent.E_INK_GREY
        }
        viewModelScope.launch { settingsRepository.setThemeAccent(next) }
    }

    fun cycleAppOrder() {
        val next = when (uiState.value.settings.appOrder) {
            AppOrder.ALPHABETICAL -> AppOrder.INSTALLATION_DATE
            AppOrder.INSTALLATION_DATE -> AppOrder.MOST_USED
            AppOrder.MOST_USED -> AppOrder.ALPHABETICAL
        }
        viewModelScope.launch { settingsRepository.setAppOrder(next) }
    }

    fun setAppDisguise(app: InstalledApp, disguise: String) {
        viewModelScope.launch { settingsRepository.setAppDisguise(app.id, disguise) }
    }

    fun cycleAppDailyQuota(app: InstalledApp) {
        val current = uiState.value.settings.appDailyQuotaMinutes[app.id] ?: 0
        val next = QuotaPresets.next(current)
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
            ProfileId.DEFAULT -> ProfileId.WORK
            ProfileId.WORK -> ProfileId.HOME
            ProfileId.HOME -> ProfileId.WEEKEND
            ProfileId.WEEKEND -> ProfileId.DEFAULT
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
        quotaState.value = quotaState.value.copy(block = null)
    }

    fun checkActiveAppQuotas() {
        viewModelScope.launch {
            val quotas = uiState.value.settings.appDailyQuotaMinutes
            if (quotas.isEmpty()) return@launch
            if (!appsRepository.hasUsageAccess()) return@launch

            val apps = installedApps.value
            for ((appId, minutes) in quotas) {
                if (minutes <= 0) continue
                val app = apps.find { it.id == appId } ?: continue
                val usedMillis = withContext(Dispatchers.IO) {
                    appsRepository.getTodayUsageMillis(app.packageName)
                }
                if (usedMillis >= minutes * 60_000L) {
                    quotaState.value = quotaState.value.copy(
                        block = QuotaBlock(
                            app = app,
                            quotaMinutes = minutes,
                            usedMillis = usedMillis,
                            requiresUsageAccess = false
                        )
                    )
                    break
                }
            }
        }
    }

    fun refreshQuotaUsages() {
        viewModelScope.launch {
            if (!appsRepository.hasUsageAccess()) return@launch
            val usages = mutableMapOf<String, Long>()
            val quotas = uiState.value.settings.appDailyQuotaMinutes
            val apps = installedApps.value
            for ((appId, minutes) in quotas) {
                if (minutes > 0) {
                    val app = apps.find { it.id == appId }
                    if (app != null) {
                        val used = withContext(Dispatchers.IO) {
                            appsRepository.getTodayUsageMillis(app.packageName)
                        }
                        usages[appId] = used
                    }
                }
            }
            quotaState.value = quotaState.value.copy(usages = usages)
        }
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

    internal suspend fun blockIfQuotaExceeded(app: InstalledApp): Boolean {
        val quotaMinutes = uiState.value.settings.appDailyQuotaMinutes[app.id] ?: return false
        if (quotaMinutes <= 0) return false

        val hasUsageAccess = appsRepository.hasUsageAccess()
        systemAccess.value = systemAccess.value.copy(usageAccessGranted = hasUsageAccess)
        if (!hasUsageAccess) {
            quotaState.value = quotaState.value.copy(
                block = QuotaBlock(
                    app = app,
                    quotaMinutes = quotaMinutes,
                    usedMillis = 0L,
                    requiresUsageAccess = true
                )
            )
            return true
        }

        val usedMillis = withContext(Dispatchers.IO) {
            appsRepository.getTodayUsageMillis(app.packageName)
        }
        if (usedMillis >= quotaMinutes * 60_000L) {
            quotaState.value = quotaState.value.copy(
                block = QuotaBlock(
                    app = app,
                    quotaMinutes = quotaMinutes,
                    usedMillis = usedMillis,
                    requiresUsageAccess = false
                )
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
    activeProfileId: ProfileId
): List<InstalledApp> {
    if (!settings.contextProfilesEnabled || activeProfileId == ProfileId.DEFAULT) return apps
    val allowedIds = profileAppIds(settings, activeProfileId)
    if (allowedIds.isEmpty()) return apps
    return apps.filter { it.id in allowedIds }
}

private fun profileAppIds(settings: LauncherSettings, profileId: ProfileId): Set<String> {
    return when (profileId) {
        ProfileId.WORK -> settings.workProfileAppIds
        ProfileId.HOME -> settings.homeProfileAppIds
        ProfileId.WEEKEND -> settings.weekendProfileAppIds
        else -> emptySet()
    }
}

internal fun resolveActiveContextProfile(settings: LauncherSettings, signal: ContextSignal): ProfileId {
    if (!settings.contextProfilesEnabled) return ProfileId.DEFAULT
    if (!settings.contextProfilesAutoSwitch) return settings.manualContextProfileId

    signal.wifiSsid?.let { ssid ->
        when (ssid) {
            settings.workProfileWifiSsid.takeIf { it.isNotBlank() } -> return ProfileId.WORK
            settings.homeProfileWifiSsid.takeIf { it.isNotBlank() } -> return ProfileId.HOME
            settings.weekendProfileWifiSsid.takeIf { it.isNotBlank() } -> return ProfileId.WEEKEND
        }
    }

    val day = signal.now.dayOfWeek
    if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) return ProfileId.WEEKEND

    val hour = signal.now.hour
    if (hour in 9..16) return ProfileId.WORK

    return ProfileId.HOME
}
