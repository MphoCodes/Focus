package com.mphocodes.focuslauncher.data.model

data class Workspace(
    val id: String,
    val name: String,
    val appIds: Set<String> = emptySet()
) {
    val letter: String
        get() = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}

val DefaultWorkspaces = listOf(
    Workspace(id = "work", name = "Work"),
    Workspace(id = "money", name = "Money"),
    Workspace(id = "create", name = "Create"),
    Workspace(id = "tools", name = "Tools")
)
