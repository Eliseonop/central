package com.tcontur.central.core.socket

sealed class SocketEvent {
    
    data class MessageDecoded(
        val header: String,
        val data: Map<String, Any>
    ) : SocketEvent()

    
    data class ConnectionSuccess(val connected: Boolean) : SocketEvent()

    
    data class ConnectionClosed(val code: Int, val reason: String) : SocketEvent()

    
    data class ConnectionError(val message: String) : SocketEvent()
}
