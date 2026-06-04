package com.focuslauncher.data.model

data class LauncherSettings(
    val onboardingComplete: Boolean = false,
    val favoriteAppIds: Set<String> = emptySet(),
    val hiddenAppIds: Set<String> = emptySet(),
    val intentPromptAppIds: Set<String> = emptySet(),
    val focusPauseSeconds: Int = 5,
    val localDataOnly: Boolean = true,
    val blackThemeEnabled: Boolean = true,
    val grayscaleEnabled: Boolean = false,
    val zeroAppsScheduleEnabled: Boolean = false,
    val zeroAppsStartTime: String = "22:00",
    val zeroAppsEndTime: String = "07:00",
    val textSize: String = "Standard",
    val appOrder: String = "Alphabetical",
    val themeAccent: String = "E-Ink Grey",
    val appLaunchCounts: Map<String, Int> = emptyMap(),
    val appDeflectionCounts: Map<String, Int> = emptyMap(),
    val intentLogs: List<String> = emptyList()
)
