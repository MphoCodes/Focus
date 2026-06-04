package com.focuslauncher.data.repository

import android.content.Context
import android.content.Intent
import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.os.Process
import android.provider.Settings
import android.util.Log
import android.net.wifi.WifiManager
import com.focuslauncher.data.model.InstalledApp
import java.time.LocalDate
import java.time.ZoneId

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

    fun openAppInfo(app: InstalledApp) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = android.net.Uri.parse("package:${app.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        runCatching { context.startActivity(intent) }
    }

    @Suppress("DEPRECATION")
    fun hasUsageAccess(): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java)
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        } else {
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun getTodayUsageMillis(packageName: String): Long {
        if (!hasUsageAccess()) return 0L

        val usageStatsManager = context.getSystemService(UsageStatsManager::class.java)
        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val now = System.currentTimeMillis()

        return usageStatsManager
            .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startOfDay, now)
            .filter { it.packageName == packageName }
            .sumOf { it.totalTimeInForeground }
    }

    fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun hasNotificationListenerAccess(): Boolean {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        ).orEmpty()
        return enabledListeners.contains(context.packageName)
    }

    fun openNotificationListenerSettings() {
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    @Suppress("DEPRECATION")
    fun getCurrentWifiSsid(): String? {
        val wifiManager = context.applicationContext.getSystemService(WifiManager::class.java) ?: return null
        return wifiManager.connectionInfo?.ssid
            ?.trim()
            ?.trim('"')
            ?.takeIf { it.isNotBlank() && it != WifiManager.UNKNOWN_SSID }
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
