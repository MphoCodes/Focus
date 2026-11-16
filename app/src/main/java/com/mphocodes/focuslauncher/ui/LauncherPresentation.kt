package com.mphocodes.focuslauncher.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.mphocodes.focuslauncher.R
import com.mphocodes.focuslauncher.data.model.InstalledApp
import com.mphocodes.focuslauncher.data.model.Workspace
import java.time.LocalDateTime


internal fun selectorApps(
    uiState: LauncherUiState,
    includeHidden: Boolean,
    includeOriginalLabel: Boolean = false
): List<InstalledApp> {
    val query = uiState.searchQuery.trim()
    return uiState.installedApps
        .filter { includeHidden || it.id !in uiState.settings.hiddenAppIds }
        .filter {
            query.isBlank() ||
                appDisplayLabel(it, uiState.settings).contains(query, ignoreCase = true) ||
                (includeOriginalLabel && it.label.contains(query, ignoreCase = true))
        }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { appDisplayLabel(it, uiState.settings) })
}

internal fun workspaceApps(uiState: LauncherUiState, workspace: Workspace): List<InstalledApp> {
    return uiState.installedApps
        .filter { it.id in workspace.appIds }
        .filter { it.id !in uiState.settings.hiddenAppIds }
        .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { appDisplayLabel(it, uiState.settings) })
}

internal fun appDisplayLabel(app: InstalledApp, settings: com.mphocodes.focuslauncher.data.model.LauncherSettings): String {
    return settings.appDisguises[app.id]?.takeIf { it.isNotBlank() } ?: app.label
}

internal fun formatUsageMinutes(usedMillis: Long): Int {
    return (usedMillis / 60_000L).coerceAtLeast(0L).toInt()
}

@Composable
internal fun displayProfileName(profileId: com.mphocodes.focuslauncher.data.model.ProfileId): String {
    return when (profileId) {
        com.mphocodes.focuslauncher.data.model.ProfileId.WORK -> stringResource(R.string.profiles_work)
        com.mphocodes.focuslauncher.data.model.ProfileId.HOME -> stringResource(R.string.profiles_home)
        com.mphocodes.focuslauncher.data.model.ProfileId.WEEKEND -> stringResource(R.string.profiles_weekend)
        else -> stringResource(R.string.profiles_default)
    }
}

internal fun isTimeInSchedule(now: LocalDateTime, start: String, end: String): Boolean {
    return try {
        val startParts = start.split(":")
        val endParts = end.split(":")
        val startHour = startParts[0].toInt()
        val startMin = startParts[1].toInt()
        val endHour = endParts[0].toInt()
        val endMin = endParts[1].toInt()

        val startTime = now.withHour(startHour).withMinute(startMin).withSecond(0).withNano(0)
        val endTime = now.withHour(endHour).withMinute(endMin).withSecond(0).withNano(0)

        if (startTime.isBefore(endTime) || startTime.isEqual(endTime)) {
            (now.isEqual(startTime) || now.isAfter(startTime)) && now.isBefore(endTime)
        } else {
            (now.isEqual(startTime) || now.isAfter(startTime)) || now.isBefore(endTime)
        }
    } catch (e: Exception) {
        false
    }
}

internal fun appVersionText(): String = com.mphocodes.focuslauncher.BuildConfig.VERSION_NAME
