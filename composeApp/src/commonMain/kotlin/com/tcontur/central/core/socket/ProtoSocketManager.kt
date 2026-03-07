package com.tcontur.central.core.socket

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

private const val TAG = "[TCONTUR][PROTO_SOCKET]"

class ProtoSocketManager {

    // ── Socket connection ──────────────────────────────────────────────────────

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected

    // ── Socket authentication ──────────────────────────────────────────────────

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    // ── Raw socket events ──────────────────────────────────────────────────────

    private val _socketEvents = MutableSharedFlow<SocketEvent>(
        replay = 0,
        extraBufferCapacity = 64
    )
    val socketEvents: SharedFlow<SocketEvent> = _socketEvents

    // ── State mutators ─────────────────────────────────────────────────────────

    fun updateConnectionState(connected: Boolean) {
        val prev = _isConnected.value
        _isConnected.value = connected
        if (prev != connected) {
            println("$TAG isConnected: $prev → $connected")
        }
        if (!connected && _isAuthenticated.value) {
            println("$TAG Socket desconectado — reseteando isAuthenticated → false")
            _isAuthenticated.value = false
        }
    }

    fun setAuthenticated(value: Boolean) {
        println("$TAG isAuthenticated → $value")
        _isAuthenticated.value = value
    }

    // ── Event emitters ─────────────────────────────────────────────────────────

    fun emitMessageDecoded(header: String, data: Map<String, Any>) {
        println("$TAG 📨 Mensaje recibido — header='$header' data=$data")
        _socketEvents.tryEmit(SocketEvent.MessageDecoded(header, data))
    }

    fun emitConnectionSuccess(connected: Boolean) {
        println("$TAG 🔗 ConnectionSuccess($connected)")
        _socketEvents.tryEmit(SocketEvent.ConnectionSuccess(connected))
    }

    fun emitConnectionClosed(code: Int, reason: String) {
        println("$TAG 🔒 ConnectionClosed — code=$code reason='$reason'")
        _socketEvents.tryEmit(SocketEvent.ConnectionClosed(code, reason))
    }

    fun emitError(message: String) {
        println("$TAG ❌ Error — $message")
        _socketEvents.tryEmit(SocketEvent.ConnectionError(message))
    }
}
