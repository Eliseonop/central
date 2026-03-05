package com.tcontur.central.core.socket

/**
 * iOS stub for [SocketServiceManager].
 * A full URLSession/Starscream implementation can replace this later.
 */
class IosSocketServiceManager : SocketServiceManager {
    override fun connect(wsUrl: String) { /* TODO: iOS WebSocket */ }
    override fun disconnect() { /* TODO: iOS WebSocket */ }
    override fun send(data: HashMap<String, Any?>, formatKey: String) { /* TODO: iOS WebSocket */ }
}
