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
            textSize = preferences[Keys.TEXT_SIZE] ?: "Standard",
            appOrder = preferences[Keys.APP_ORDER] ?: "Alphabetical",
            appLaunchCounts = LaunchCountSerializer.decode(preferences[Keys.APP_LAUNCH_COUNTS].orEmpty())
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

    suspend fun setTextSize(value: String) {
        dataStore.edit { it[Keys.TEXT_SIZE] = value }
    }

    suspend fun setAppOrder(value: String) {
        dataStore.edit { it[Keys.APP_ORDER] = value }
    }

    suspend fun incrementAppLaunchCount(packageName: String) {
        dataStore.edit { preferences ->
            val counts = LaunchCountSerializer.decode(preferences[Keys.APP_LAUNCH_COUNTS].orEmpty()).toMutableMap()
            counts[packageName] = (counts[packageName] ?: 0) + 1
            preferences[Keys.APP_LAUNCH_COUNTS] = LaunchCountSerializer.encode(counts)
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
        val TEXT_SIZE = stringPreferencesKey("text_size")
        val APP_ORDER = stringPreferencesKey("app_order")
        val APP_LAUNCH_COUNTS = stringSetPreferencesKey("app_launch_counts")
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

