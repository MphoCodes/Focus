package com.focuslauncher.data.model

import android.content.ComponentName

data class InstalledApp(
    val id: String,
    val label: String,
    val packageName: String,
    val className: String
) {
    val componentName: ComponentName
        get() = ComponentName(packageName, className)
}
