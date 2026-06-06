package com.focuslauncher.data.model

enum class TextSize(val value: String) {
    SMALL("Small"),
    STANDARD("Standard"),
    LARGE("Large");

    companion object {
        fun fromString(value: String?): TextSize = entries.find { it.value == value } ?: STANDARD
    }
}

enum class AppOrder(val value: String) {
    ALPHABETICAL("Alphabetical"),
    INSTALLATION_DATE("Installation Date"),
    MOST_USED("Most Used");

    companion object {
        fun fromString(value: String?): AppOrder = entries.find { it.value == value } ?: ALPHABETICAL
    }
}

enum class ThemeAccent(val value: String) {
    E_INK_GREY("E-Ink Grey"),
    AMBER_MONO("Amber Mono"),
    FOREST_GREEN("Forest Green");

    companion object {
        fun fromString(value: String?): ThemeAccent = entries.find { it.value == value } ?: E_INK_GREY
    }
}

enum class ProfileId(val id: String) {
    DEFAULT("default"),
    WORK("work"),
    HOME("home"),
    WEEKEND("weekend");

    companion object {
        fun fromString(id: String?): ProfileId = entries.find { it.id == id } ?: DEFAULT
    }
}

object QuotaPresets {
    val PRESETS = listOf(0, 15, 30, 60, 120)
    
    fun next(current: Int): Int {
        val index = PRESETS.indexOf(current)
        if (index == -1 || index == PRESETS.lastIndex) return PRESETS.first()
        return PRESETS[index + 1]
    }
}
