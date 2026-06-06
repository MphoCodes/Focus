package com.focuslauncher.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.focuslauncher.data.model.LauncherSettings
import com.focuslauncher.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class FocusNotificationListenerService : NotificationListenerService() {
    private val settingsRepository by lazy { SettingsRepository(applicationContext) }
    private val notificationManager by lazy {
        getSystemService(NotificationManager::class.java)
    }

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        runBlocking {
            NotificationBatchDeliverer.deliverBatchIfDue(this@FocusNotificationListenerService, settingsRepository, settingsRepository.settings.first())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName || sbn.notification.isGroupSummary()) return

        runBlocking {
            val settings = settingsRepository.settings.first()
            if (!settings.notificationBatchingEnabled) return@runBlocking
            if (isEssential(sbn.packageName, settings)) {
                NotificationBatchDeliverer.deliverBatchIfDue(this@FocusNotificationListenerService, settingsRepository, settings)
                return@runBlocking
            }

            cancelNotification(sbn.key)
            settingsRepository.incrementBatchedNotificationCount(sbn.packageName)
            NotificationBatchDeliverer.deliverBatchIfDue(this@FocusNotificationListenerService, settingsRepository, settingsRepository.settings.first())
        }
    }

    private fun isEssential(packageName: String, settings: LauncherSettings): Boolean {
        return settings.essentialNotificationAppIds.any { appId ->
            appId.substringBefore("/") == packageName
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            "focus_notification_summaries",
            "Focus summaries",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun Notification.isGroupSummary(): Boolean {
        return (flags and Notification.FLAG_GROUP_SUMMARY) != 0
    }
}
