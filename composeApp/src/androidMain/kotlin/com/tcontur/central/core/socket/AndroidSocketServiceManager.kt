package com.tcontur.central.core.socket

import android.content.Context
import android.content.Intent
import android.util.Log
import com.tcontur.central.core.location.background.BackgroundServiceManager
import com.tcontur.central.core.services.SocketService

private const val TAG = "[TCONTUR][SOCKET_MGR]"

class AndroidSocketServiceManager(private val context: Context) : SocketServiceManager {

    private val backgroundServiceManager = BackgroundServiceManager(context)

    override fun connect(wsUrl: String) {
        Log.d(TAG, "🔌 connect() → $wsUrl")
        SocketService.startConnection(context, wsUrl)
    }

    override fun disconnect() {
        // Step 1: Send ACTION_DISCONNECT so SocketService sets shouldReconnect=false
        //         and closes the WebSocket connection gracefully.
        Log.d(TAG, "📴 disconnect() → enviando ACTION_DISCONNECT al SocketService")
        SocketService.disconnect(context)

        // Step 2: Stop the SocketService foreground service itself.
        //         ACTION_DISCONNECT alone only closes the WebSocket; the Android
        //         Service keeps running as a foreground service until we stop it.
        //         onDestroy() handles final cleanup (shouldReconnect=false,
        //         serviceScope.cancel(), cleanupResources).
        Log.d(TAG, "🛑 disconnect() → deteniendo SocketService (stopService)")
        context.stopService(Intent(context, SocketService::class.java))
    }

    override fun send(data: HashMap<String, Any?>, formatKey: String) {
        SocketService.sendMessage(context, data, formatKey)
    }

    override fun startLocationTracking() {
        Log.d(TAG, "📍 startLocationTracking() → iniciando LocationForegroundService")
        backgroundServiceManager.startService()
    }

    override fun stopLocationTracking() {
        Log.d(TAG, "🛑 stopLocationTracking() → deteniendo LocationForegroundService")
        backgroundServiceManager.stopService()
    }
}
