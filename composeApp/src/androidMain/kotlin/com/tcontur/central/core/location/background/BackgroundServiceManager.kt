package com.tcontur.central.core.location.background

import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Helper to start/stop the [LocationForegroundService].
 */
class BackgroundServiceManager(private val context: Context) {

    fun startService() {
        val intent = Intent(context, LocationForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun stopService() {
        context.stopService(Intent(context, LocationForegroundService::class.java))
    }
}
