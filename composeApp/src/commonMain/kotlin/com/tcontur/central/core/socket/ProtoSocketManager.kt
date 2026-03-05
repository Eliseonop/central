package com.tcontur.central.core.socket

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow


class ProtoSocketManager {

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    private val _socketEvents = MutableSharedFlow<SocketEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val socketEvents: SharedFlow<SocketEvent> = _socketEvents

    fun updateConnectionState(connected: Boolean) {
        _isConnected.value = connected
    }

    fun emitMessageDecoded(header: String, data: Map<String, Any>) {
        _socketEvents.tryEmit(SocketEvent.MessageDecoded(header, data))
    }

    fun emitConnectionSuccess(connected: Boolean) {
        _socketEvents.tryEmit(SocketEvent.ConnectionSuccess(connected))
    }

    fun emitConnectionClosed(code: Int, reason: String) {
        _socketEvents.tryEmit(SocketEvent.ConnectionClosed(code, reason))
    }

    fun emitError(message: String) {
        _socketEvents.tryEmit(SocketEvent.ConnectionError(message))
    }
}
