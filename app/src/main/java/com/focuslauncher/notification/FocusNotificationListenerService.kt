package com.focuslauncher.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.focuslauncher.R
import com.focuslauncher.data.model.LauncherSettings
import com.focuslauncher.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
            NotificationBatchDeliverer.deliverBatchIfDue(this@FocusNotificationListenerService, settingsRepository, settingsRepository.settings.first())
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName || sbn.notification.isGroupSummary()) return

        val settings = currentSettings ?: return
        if (!settings.notificationBatchingEnabled) return

        if (isEssential(sbn.packageName, settings)) {
            scope.launch {
                NotificationBatchDeliverer.deliverBatchIfDue(this@FocusNotificationListenerService, settingsRepository, settings)
            }
            return
        }

        scope.launch {
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
            CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun Notification.isGroupSummary(): Boolean {
        return (flags and Notification.FLAG_GROUP_SUMMARY) != 0
    }

    private companion object {
        const val CHANNEL_ID = "focus_notification_summaries"
    }
}
