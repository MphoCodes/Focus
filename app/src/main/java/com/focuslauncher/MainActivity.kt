package com.focuslauncher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import com.focuslauncher.notification.BatchDeliveryWorker
import com.focuslauncher.ui.FocusLauncherApp
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.parseColor("#050505")
        window.navigationBarColor = android.graphics.Color.parseColor("#050505")
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = false

        scheduleBatchDelivery()

        setContent {
            FocusLauncherApp()
        }
    }

    private fun scheduleBatchDelivery() {
        // Enqueue periodic work to run every 15 minutes to check if we are in the delivery window
        val workRequest = PeriodicWorkRequestBuilder<BatchDeliveryWorker>(15, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "BatchDelivery",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}
