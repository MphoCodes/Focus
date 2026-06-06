package com.focuslauncher.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.focuslauncher.MainActivity
import com.focuslauncher.R
import com.focuslauncher.data.model.LauncherSettings
import com.focuslauncher.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FocusNotificationListenerService : NotificationListenerService() {
    private val settingsRepository by lazy { SettingsRepository(applicationContext) }
    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    @Volatile
    private var currentSettings: LauncherSettings? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        scope.launch {
            settingsRepository.settings.collect {
                currentSettings = it
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        scope.launch {
            deliverBatchIfDue(settingsRepository.settings.first())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName || sbn.notification.isGroupSummary()) return

        val settings = currentSettings ?: return
        if (!settings.notificationBatchingEnabled) return
        
        if (isEssential(sbn.packageName, settings)) {
            scope.launch {
                deliverBatchIfDue(settings)
            }
            return
        }

        scope.launch {
            cancelNotification(sbn.key)
            settingsRepository.incrementBatchedNotificationCount(sbn.packageName)
            deliverBatchIfDue(settingsRepository.settings.first())
        }
    }

    private suspend fun deliverBatchIfDue(settings: LauncherSettings) {
        val token = currentBatchToken() ?: return
        if (settings.lastNotificationBatchToken == token) return
        if (settings.batchedNotificationCounts.isEmpty()) {
            settingsRepository.setLastNotificationBatchToken(token)
            return
        }

        val total = settings.batchedNotificationCounts.values.sum()
        val summary = settings.batchedNotificationCounts.entries
            .sortedByDescending { it.value }
            .take(4)
            .joinToString(", ") { (packageName, count) ->
                "${appLabel(packageName)}: $count"
            }

        notificationManager.notify(
            SUMMARY_NOTIFICATION_ID,
            Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Focus notification summary")
                .setContentText("$total notifications: $summary")
                .setStyle(Notification.BigTextStyle().bigText("$total notifications: $summary"))
                .setContentIntent(mainActivityIntent())
                .setAutoCancel(true)
                .build()
        )
        settingsRepository.clearBatchedNotificationCounts()
        settingsRepository.setLastNotificationBatchToken(token)
    }

    private fun isEssential(packageName: String, settings: LauncherSettings): Boolean {
        return settings.essentialNotificationAppIds.any { appId ->
            appId.substringBefore("/") == packageName
        }
    }

    private fun appLabel(packageName: String): String {
        return runCatching {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        }.getOrDefault(packageName)
    }

    private fun mainActivityIntent() = android.app.PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java),
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Focus summaries",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun currentBatchToken(now: LocalDateTime = LocalDateTime.now()): String? {
        val slot = when {
            now.hour >= 18 -> "18"
            now.hour >= 12 -> "12"
            else -> return null
        }
        return "${LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)}-$slot"
    }

    private fun Notification.isGroupSummary(): Boolean {
        return (flags and Notification.FLAG_GROUP_SUMMARY) != 0
    }

    private companion object {
        const val CHANNEL_ID = "focus_notification_summaries"
        const val SUMMARY_NOTIFICATION_ID = 2001
    }
}
