package com.mphocodes.focuslauncher.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mphocodes.focuslauncher.data.repository.SettingsRepository
import kotlinx.coroutines.flow.first

class BatchDeliveryWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settingsRepository = SettingsRepository(context)
        val settings = settingsRepository.settings.first()
        if (!settings.notificationBatchingEnabled) return Result.success()

        NotificationBatchDeliverer.deliverBatchIfDue(context, settingsRepository, settings)
        return Result.success()
    }
}
