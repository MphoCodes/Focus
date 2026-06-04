package com.focuslauncher.data.model

data class LauncherSettings(
    val onboardingComplete: Boolean = false,
    val favoriteAppIds: Set<String> = emptySet(),
    val hiddenAppIds: Set<String> = emptySet(),
    val intentPromptAppIds: Set<String> = emptySet(),
    val focusPauseSeconds: Int = 5
)
