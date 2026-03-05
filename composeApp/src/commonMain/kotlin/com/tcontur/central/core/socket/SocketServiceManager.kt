package com.tcontur.central.core.socket

interface SocketServiceManager {

    fun connect(wsUrl: String)

    fun disconnect()

    fun send(data: HashMap<String, Any?>, formatKey: String)
}
