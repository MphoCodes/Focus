package com.focuslauncher.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.focuslauncher.MainActivity
import com.focuslauncher.R
import com.focuslauncher.data.model.LauncherSettings
import com.focuslauncher.data.repository.SettingsRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object NotificationBatchDeliverer {

    suspend fun deliverBatchIfDue(context: Context, settingsRepository: SettingsRepository, settings: LauncherSettings) {
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
                "${appLabel(context, packageName)}: $count"
            }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(
            2001, // SUMMARY_NOTIFICATION_ID
            Notification.Builder(context, "focus_notification_summaries")
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(R.string.notification_summary_title))
                .setContentText(context.getString(R.string.notification_summary, total, summary))
                .setStyle(Notification.BigTextStyle().bigText(context.getString(R.string.notification_summary, total, summary)))
                .setContentIntent(mainActivityIntent(context))
                .setAutoCancel(true)
                .build()
        )
        settingsRepository.clearBatchedNotificationCounts()
        settingsRepository.setLastNotificationBatchToken(token)
    }

    private fun appLabel(context: Context, packageName: String): String {
        return runCatching {
            val appInfo = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(appInfo).toString()
        }.getOrDefault(packageName)
    }

    private fun mainActivityIntent(context: Context) = android.app.PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java),
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )

    fun currentBatchToken(now: LocalDateTime = LocalDateTime.now()): String? {
        val slot = when {
            now.hour >= 18 -> "18"
            now.hour >= 12 -> "12"
            else -> return null
        }
        return "${now.toLocalDate().format(DateTimeFormatter.BASIC_ISO_DATE)}-$slot"
    }
}
