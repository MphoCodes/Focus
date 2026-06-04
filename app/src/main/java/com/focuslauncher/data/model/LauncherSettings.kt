package com.focuslauncher.data.model

data class LauncherSettings(
    val onboardingComplete: Boolean = false,
    val favoriteAppIds: Set<String> = emptySet(),
    val hiddenAppIds: Set<String> = emptySet(),
    val intentPromptAppIds: Set<String> = emptySet(),
    val focusPauseSeconds: Int = 5,
    val localDataOnly: Boolean = true,
    val blackThemeEnabled: Boolean = true,
    val textSize: String = "Standard",
    val appOrder: String = "Alphabetical",
    val themeAccent: String = "E-Ink Grey",
    val appLaunchCounts: Map<String, Int> = emptyMap()
)
