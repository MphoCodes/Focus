package com.focuslauncher.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.focuslauncher.data.model.LauncherSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.focusSettingsDataStore by preferencesDataStore(name = "focus_launcher_settings")

class SettingsRepository(context: Context) {
    private val dataStore = context.focusSettingsDataStore

    val settings: Flow<LauncherSettings> = dataStore.data.map { preferences ->
        LauncherSettings(
            onboardingComplete = preferences[Keys.ONBOARDING_COMPLETE] ?: false,
            favoriteAppIds = preferences[Keys.FAVORITE_APPS].orEmpty(),
            hiddenAppIds = preferences[Keys.HIDDEN_APPS].orEmpty(),
            intentPromptAppIds = preferences[Keys.INTENT_PROMPT_APPS].orEmpty(),
            focusPauseSeconds = preferences[Keys.FOCUS_PAUSE_SECONDS] ?: 5,
            localDataOnly = preferences[Keys.LOCAL_DATA_ONLY] ?: true,
            blackThemeEnabled = preferences[Keys.BLACK_THEME_ENABLED] ?: true,
            grayscaleEnabled = preferences[Keys.GRAYSCALE_ENABLED] ?: false,
            zeroAppsScheduleEnabled = preferences[Keys.ZERO_APPS_SCHEDULE_ENABLED] ?: false,
            zeroAppsStartTime = preferences[Keys.ZERO_APPS_START_TIME] ?: "22:00",
            zeroAppsEndTime = preferences[Keys.ZERO_APPS_END_TIME] ?: "07:00",
            textSize = preferences[Keys.TEXT_SIZE] ?: "Standard",
            appOrder = preferences[Keys.APP_ORDER] ?: "Alphabetical",
            themeAccent = preferences[Keys.THEME_ACCENT] ?: "E-Ink Grey",
            appDisguises = AppDisguiseSerializer.decode(preferences[Keys.APP_DISGUISES].orEmpty()),
            appDailyQuotaMinutes = LaunchCountSerializer.decode(preferences[Keys.APP_DAILY_QUOTA_MINUTES].orEmpty()),
            contextProfilesEnabled = preferences[Keys.CONTEXT_PROFILES_ENABLED] ?: false,
            contextProfilesAutoSwitch = preferences[Keys.CONTEXT_PROFILES_AUTO_SWITCH] ?: true,
            manualContextProfileId = preferences[Keys.MANUAL_CONTEXT_PROFILE_ID] ?: "default",
            workProfileAppIds = preferences[Keys.WORK_PROFILE_APPS].orEmpty(),
            homeProfileAppIds = preferences[Keys.HOME_PROFILE_APPS].orEmpty(),
            weekendProfileAppIds = preferences[Keys.WEEKEND_PROFILE_APPS].orEmpty(),
            workProfileWifiSsid = preferences[Keys.WORK_PROFILE_WIFI_SSID].orEmpty(),
            homeProfileWifiSsid = preferences[Keys.HOME_PROFILE_WIFI_SSID].orEmpty(),
            weekendProfileWifiSsid = preferences[Keys.WEEKEND_PROFILE_WIFI_SSID].orEmpty(),
            appLaunchCounts = LaunchCountSerializer.decode(preferences[Keys.APP_LAUNCH_COUNTS].orEmpty()),
            appDeflectionCounts = LaunchCountSerializer.decode(preferences[Keys.APP_DEFLECTION_COUNTS].orEmpty()),
            intentLogs = preferences[Keys.INTENT_LOGS]?.split("|||")?.filter { it.isNotEmpty() } ?: emptyList()
        )
    }

    suspend fun setOnboardingComplete(value: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = value }
    }

    suspend fun setFavoriteAppIds(ids: Set<String>) {
        dataStore.edit { it[Keys.FAVORITE_APPS] = ids }
    }

    suspend fun setHiddenAppIds(ids: Set<String>) {
        dataStore.edit { it[Keys.HIDDEN_APPS] = ids }
    }

    suspend fun setIntentPromptAppIds(ids: Set<String>) {
        dataStore.edit { it[Keys.INTENT_PROMPT_APPS] = ids }
    }

    suspend fun setFocusPauseSeconds(seconds: Int) {
        dataStore.edit { it[Keys.FOCUS_PAUSE_SECONDS] = seconds }
    }

    suspend fun setLocalDataOnly(value: Boolean) {
        dataStore.edit { it[Keys.LOCAL_DATA_ONLY] = value }
    }

    suspend fun setBlackThemeEnabled(value: Boolean) {
        dataStore.edit { it[Keys.BLACK_THEME_ENABLED] = value }
    }

    suspend fun setGrayscaleEnabled(value: Boolean) {
        dataStore.edit { it[Keys.GRAYSCALE_ENABLED] = value }
    }

    suspend fun setZeroAppsScheduleEnabled(value: Boolean) {
        dataStore.edit { it[Keys.ZERO_APPS_SCHEDULE_ENABLED] = value }
    }

    suspend fun setZeroAppsStartTime(value: String) {
        dataStore.edit { it[Keys.ZERO_APPS_START_TIME] = value }
    }

    suspend fun setZeroAppsEndTime(value: String) {
        dataStore.edit { it[Keys.ZERO_APPS_END_TIME] = value }
    }

    suspend fun setTextSize(value: String) {
        dataStore.edit { it[Keys.TEXT_SIZE] = value }
    }

    suspend fun setAppOrder(value: String) {
        dataStore.edit { it[Keys.APP_ORDER] = value }
    }

    suspend fun setThemeAccent(value: String) {
        dataStore.edit { it[Keys.THEME_ACCENT] = value }
    }

    suspend fun setAppDisguise(appId: String, disguise: String) {
        dataStore.edit { preferences ->
            val disguises = AppDisguiseSerializer.decode(preferences[Keys.APP_DISGUISES].orEmpty()).toMutableMap()
            val cleaned = disguise.trim()
            if (cleaned.isBlank()) {
                disguises.remove(appId)
            } else {
                disguises[appId] = cleaned
            }
            preferences[Keys.APP_DISGUISES] = AppDisguiseSerializer.encode(disguises)
        }
    }

    suspend fun setAppDailyQuotaMinutes(appId: String, minutes: Int) {
        dataStore.edit { preferences ->
            val quotas = LaunchCountSerializer.decode(preferences[Keys.APP_DAILY_QUOTA_MINUTES].orEmpty()).toMutableMap()
            if (minutes <= 0) {
                quotas.remove(appId)
            } else {
                quotas[appId] = minutes
            }
            preferences[Keys.APP_DAILY_QUOTA_MINUTES] = LaunchCountSerializer.encode(quotas)
        }
    }

    suspend fun setContextProfilesEnabled(value: Boolean) {
        dataStore.edit { it[Keys.CONTEXT_PROFILES_ENABLED] = value }
    }

    suspend fun setContextProfilesAutoSwitch(value: Boolean) {
        dataStore.edit { it[Keys.CONTEXT_PROFILES_AUTO_SWITCH] = value }
    }

    suspend fun setManualContextProfileId(value: String) {
        dataStore.edit { it[Keys.MANUAL_CONTEXT_PROFILE_ID] = value }
    }

    suspend fun setWorkProfileAppIds(ids: Set<String>) {
        dataStore.edit { it[Keys.WORK_PROFILE_APPS] = ids }
    }

    suspend fun setHomeProfileAppIds(ids: Set<String>) {
        dataStore.edit { it[Keys.HOME_PROFILE_APPS] = ids }
    }

    suspend fun setWeekendProfileAppIds(ids: Set<String>) {
        dataStore.edit { it[Keys.WEEKEND_PROFILE_APPS] = ids }
    }

    suspend fun setProfileWifiSsid(profileId: String, ssid: String) {
        dataStore.edit { preferences ->
            val cleaned = ssid.trim().trim('"')
            when (profileId) {
                "work" -> preferences[Keys.WORK_PROFILE_WIFI_SSID] = cleaned
                "home" -> preferences[Keys.HOME_PROFILE_WIFI_SSID] = cleaned
                "weekend" -> preferences[Keys.WEEKEND_PROFILE_WIFI_SSID] = cleaned
            }
        }
    }

    suspend fun incrementAppLaunchCount(packageName: String) {
        dataStore.edit { preferences ->
            val counts = LaunchCountSerializer.decode(preferences[Keys.APP_LAUNCH_COUNTS].orEmpty()).toMutableMap()
            counts[packageName] = (counts[packageName] ?: 0) + 1
            preferences[Keys.APP_LAUNCH_COUNTS] = LaunchCountSerializer.encode(counts)
        }
    }

    suspend fun incrementAppDeflectionCount(packageName: String) {
        dataStore.edit { preferences ->
            val counts = LaunchCountSerializer.decode(preferences[Keys.APP_DEFLECTION_COUNTS].orEmpty()).toMutableMap()
            counts[packageName] = (counts[packageName] ?: 0) + 1
            preferences[Keys.APP_DEFLECTION_COUNTS] = LaunchCountSerializer.encode(counts)
        }
    }

    suspend fun addIntentLog(log: String) {
        dataStore.edit { preferences ->
            val logs = (preferences[Keys.INTENT_LOGS]?.split("|||")?.filter { it.isNotEmpty() } ?: emptyList()).toMutableList()
            logs.add(0, log)
            preferences[Keys.INTENT_LOGS] = logs.take(50).joinToString("|||")
        }
    }

    private object Keys {
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val FAVORITE_APPS = stringSetPreferencesKey("favorite_apps")
        val HIDDEN_APPS = stringSetPreferencesKey("hidden_apps")
        val INTENT_PROMPT_APPS = stringSetPreferencesKey("intent_prompt_apps")
        val FOCUS_PAUSE_SECONDS = intPreferencesKey("focus_pause_seconds")
        val LOCAL_DATA_ONLY = booleanPreferencesKey("local_data_only")
        val BLACK_THEME_ENABLED = booleanPreferencesKey("black_theme_enabled")
        val GRAYSCALE_ENABLED = booleanPreferencesKey("grayscale_enabled")
        val ZERO_APPS_SCHEDULE_ENABLED = booleanPreferencesKey("zero_apps_schedule_enabled")
        val ZERO_APPS_START_TIME = stringPreferencesKey("zero_apps_start_time")
        val ZERO_APPS_END_TIME = stringPreferencesKey("zero_apps_end_time")
        val TEXT_SIZE = stringPreferencesKey("text_size")
        val APP_ORDER = stringPreferencesKey("app_order")
        val THEME_ACCENT = stringPreferencesKey("theme_accent")
        val APP_DISGUISES = stringSetPreferencesKey("app_disguises")
        val APP_DAILY_QUOTA_MINUTES = stringSetPreferencesKey("app_daily_quota_minutes")
        val CONTEXT_PROFILES_ENABLED = booleanPreferencesKey("context_profiles_enabled")
        val CONTEXT_PROFILES_AUTO_SWITCH = booleanPreferencesKey("context_profiles_auto_switch")
        val MANUAL_CONTEXT_PROFILE_ID = stringPreferencesKey("manual_context_profile_id")
        val WORK_PROFILE_APPS = stringSetPreferencesKey("work_profile_apps")
        val HOME_PROFILE_APPS = stringSetPreferencesKey("home_profile_apps")
        val WEEKEND_PROFILE_APPS = stringSetPreferencesKey("weekend_profile_apps")
        val WORK_PROFILE_WIFI_SSID = stringPreferencesKey("work_profile_wifi_ssid")
        val HOME_PROFILE_WIFI_SSID = stringPreferencesKey("home_profile_wifi_ssid")
        val WEEKEND_PROFILE_WIFI_SSID = stringPreferencesKey("weekend_profile_wifi_ssid")
        val APP_LAUNCH_COUNTS = stringSetPreferencesKey("app_launch_counts")
        val APP_DEFLECTION_COUNTS = stringSetPreferencesKey("app_deflection_counts")
        val INTENT_LOGS = stringPreferencesKey("intent_logs")
    }
}

// Standalone utility for encoding and decoding application launch counts
object LaunchCountSerializer {
    fun decode(encoded: Set<String>): Map<String, Int> {
        return encoded.mapNotNull { entry ->
            val separatorIndex = entry.lastIndexOf('=')
            if (separatorIndex <= 0 || separatorIndex == entry.lastIndex) return@mapNotNull null

            val packageName = entry.substring(0, separatorIndex)
            val count = entry.substring(separatorIndex + 1).toIntOrNull() ?: return@mapNotNull null
            packageName to count
        }.toMap()
    }

    fun encode(counts: Map<String, Int>): Set<String> {
        return counts
            .filterValues { it > 0 }
            .map { (packageName, count) -> "$packageName=$count" }
            .toSet()
    }
}

object AppDisguiseSerializer {
    fun decode(encoded: Set<String>): Map<String, String> {
        return encoded.mapNotNull { entry ->
            val separatorIndex = entry.indexOf(':')
            if (separatorIndex <= 0) return@mapNotNull null

            val idLength = entry.substring(0, separatorIndex).toIntOrNull() ?: return@mapNotNull null
            val idStart = separatorIndex + 1
            val aliasStart = idStart + idLength
            if (idLength <= 0 || aliasStart > entry.length) return@mapNotNull null

            val appId = entry.substring(idStart, aliasStart)
            val alias = entry.substring(aliasStart).trim()
            if (appId.isBlank() || alias.isBlank()) return@mapNotNull null

            appId to alias
        }.toMap()
    }

    fun encode(disguises: Map<String, String>): Set<String> {
        return disguises
            .mapNotNull { (appId, alias) ->
                val cleanedAlias = alias.trim()
                if (appId.isBlank() || cleanedAlias.isBlank()) return@mapNotNull null
                "${appId.length}:$appId$cleanedAlias"
            }
            .toSet()
    }
}
