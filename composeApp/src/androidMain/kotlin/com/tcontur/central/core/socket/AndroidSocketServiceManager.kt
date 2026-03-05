package com.tcontur.central.core.socket

import android.content.Context
import com.tcontur.central.core.services.SocketService


class AndroidSocketServiceManager(private val context: Context) : SocketServiceManager {

    override fun connect(wsUrl: String) {
        SocketService.startConnection(context, wsUrl)
    }

    override fun disconnect() {
        SocketService.disconnect(context)
    }

    override fun send(data: HashMap<String, Any?>, formatKey: String) {
        SocketService.sendMessage(context, data, formatKey)
    }
}
