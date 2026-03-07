package com.tcontur.central.core.socket

import android.content.Context
import com.tcontur.central.core.location.background.BackgroundServiceManager
import com.tcontur.central.core.services.SocketService

class AndroidSocketServiceManager(private val context: Context) : SocketServiceManager {

    private val backgroundServiceManager = BackgroundServiceManager(context)

    override fun connect(wsUrl: String) {
        SocketService.startConnection(context, wsUrl)
    }

    override fun disconnect() {
        SocketService.disconnect(context)
    }

    override fun send(data: HashMap<String, Any?>, formatKey: String) {
        SocketService.sendMessage(context, data, formatKey)
    }

    override fun startLocationTracking() {
        backgroundServiceManager.startService()
    }

    override fun stopLocationTracking() {
        backgroundServiceManager.stopService()
    }
}
