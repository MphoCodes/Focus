package com.focuslauncher.data.repository

import android.content.Context
import android.content.Intent
import android.util.Log
import android.provider.Settings
import com.focuslauncher.data.model.InstalledApp

class AppsRepository(private val context: Context) {
    private val packageManager = context.packageManager

    @Suppress("DEPRECATION")
    fun loadLaunchableApps(): List<InstalledApp> {
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(launcherIntent, 0)
            .mapNotNull { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo ?: return@mapNotNull null
                if (activityInfo.packageName == context.packageName) return@mapNotNull null
                val label = resolveInfo.loadLabel(packageManager)?.toString()?.trim().orEmpty()
                if (label.isBlank()) return@mapNotNull null
                val component = android.content.ComponentName(activityInfo.packageName, activityInfo.name)
                InstalledApp(
                    id = component.flattenToShortString(),
                    label = label,
                    packageName = activityInfo.packageName,
                    className = activityInfo.name
                )
            }
            .distinctBy { it.id }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    fun openApp(app: InstalledApp): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(app.componentName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return runCatching {
            context.startActivity(intent)
        }.onFailure { error ->
            Log.w(TAG, "Unable to open app ${app.packageName}", error)
        }.isSuccess
    }

    fun openDefaultHomeSettings() {
        val intent = Intent(Settings.ACTION_HOME_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private companion object {
        const val TAG = "AppsRepository"
    }
}
