package com.tcontur.central.core.location.background

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

private const val TAG = "[TCONTUR][BG_SVC_MGR]"

/**
 * Helper to start/stop the [LocationForegroundService].
 */
class BackgroundServiceManager(private val context: Context) {

    fun startService() {
        Log.d(TAG, "▶️ startService() → iniciando LocationForegroundService")
        val intent = Intent(context, LocationForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        Log.d(TAG, "▶️ startService() → intent enviado")
    }

    fun stopService() {
        Log.d(TAG, "⏹️ stopService() → deteniendo LocationForegroundService")
        val stopped = context.stopService(Intent(context, LocationForegroundService::class.java))
        Log.d(TAG, "⏹️ stopService() → ${if (stopped) "servicio detenido ✅" else "servicio ya estaba inactivo ⚠️"}")
    }
}
