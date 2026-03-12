package com.tcontur.central.core.socket

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.core.socket.models.ProtoAlightFromBus
import com.tcontur.central.core.socket.models.ProtoBoardBus
import com.tcontur.central.core.socket.models.ProtoCheckQr
import com.tcontur.central.core.socket.models.ProtoError
import com.tcontur.central.core.socket.models.ProtoLogin
import com.tcontur.central.core.socket.models.ProtoPosition
import com.tcontur.central.core.socket.models.ProtoRequestQr
import com.tcontur.central.data.AuthRepositoryImpl
import kotlinx.coroutines.launch

private const val TAG = "[TCONTUR][DISPATCHER]"

/**
 * Central processor of ALL incoming WebSocket messages.
 *
 * ─ Pattern mirrors NavigationViewModel in login_tcontur ─────────────────────
 *
 *  SocketService
 *      │ proto.decode()
 *      ▼
 *  ProtoSocketManager.socketEvents  ◄── single SharedFlow
 *      │
 *      ▼
 *  SocketDispatcherViewModel         ◄── ONLY consumer of socketEvents
 *      │ when (header) → Proto.fromMap(data)
 *      ├── "login"           → ProtoLogin           → SocketSessionRepository
 *      ├── "position"        → ProtoPosition         → SocketSessionRepository
 *      ├── "request_qr"      → ProtoRequestQr        → SocketSessionRepository
 *      ├── "check_qr"        → ProtoCheckQr          → SocketSessionRepository
 *      ├── "board_bus"       → ProtoBoardBus         → SocketSessionRepository
 *      ├── "alight_from_bus" → ProtoAlightFromBus    → SocketSessionRepository
 *      ├── "error"           → ProtoError            → SocketSessionRepository
 *      └── else              → log unhandled
 *
 * All other ViewModels read from [SocketSessionRepository] — NOT from
 * socketEvents directly.
 *
 * Must be activated at app scope in [AppNavHost] so it lives for the entire
 * user session and is never garbage-collected mid-navigation.
 */
class SocketDispatcherViewModel(
    private val protoSocketManager:      ProtoSocketManager,
    private val socketSessionRepository: SocketSessionRepository,
    private val socketServiceManager:    SocketServiceManager,
    private val authRepository:          AuthRepositoryImpl
) : ViewModel() {

    init {
        println("$TAG ViewModel creado — iniciando observación de socket")
        observeSocketEvents()
        observeConnectionAndLogin()
    }

    // ── Login on every (re)connect ────────────────────────────────────────────

    /**
     * Sends a login frame whenever the socket connects or reconnects.
     * This ViewModel lives at Activity scope, so it handles reconnects
     * even after [InspectoriaInitializerViewModel] has been destroyed.
     * A guard on [isAuthenticated] ensures we don't send if the server
     * already confirmed login for this connection.
     */
    private fun observeConnectionAndLogin() {
        viewModelScope.launch {
            protoSocketManager.isConnected.collect { connected ->
                if (connected) {
                    println("$TAG 🔌 Socket conectado — enviando login")
                    sendLogin()
                }
            }
        }
    }

    private suspend fun sendLogin() {
        val user = authRepository.getStoredUser() ?: run {
            println("$TAG sendLogin() — sin usuario almacenado, omitiendo")
            return
        }
        println("$TAG 📤 Enviando login — id=${user.id} codigo=${user.codigo}")
        socketServiceManager.send(
            data      = hashMapOf("id" to user.id, "code" to user.codigo),
            formatKey = "login"
        )
    }

    // ── Socket event consumer ─────────────────────────────────────────────────

    private fun observeSocketEvents() {
        viewModelScope.launch {
            protoSocketManager.socketEvents.collect { event ->
                if (event is SocketEvent.MessageDecoded) {
                    handleDecodedMessage(event.header, event.data)
                }
            }
        }
    }

    // ── Message router ────────────────────────────────────────────────────────

    /**
     * Parses each decoded socket message into a typed Proto model and routes
     * it to [SocketSessionRepository].
     *
     * To add a new message type:
     *  1. Create ProtoXxx.kt in core/socket/models/
     *  2. Add a fun onXxxReceived(proto: ProtoXxx) in SocketSessionRepository
     *  3. Add a branch here
     */
    private suspend fun handleDecodedMessage(header: String, data: Map<String, Any>) {
        println("$TAG handleDecodedMessage — header='$header'")
        when (header) {

            "login" -> {
                // Server confirmed our login → parse, store + mark authenticated.
                // InspectoriaInitializerViewModel observes isAuthenticated
                // to trigger navigation — it no longer subscribes to socketEvents.
                val proto = ProtoLogin.fromMap(data)
                socketSessionRepository.onLoginReceived(proto)
                protoSocketManager.setAuthenticated(true)
            }

            "position" -> {
                val proto = ProtoPosition.fromMap(data)
                socketSessionRepository.onPositionReceived(proto)
            }

            "request_qr" -> {
                val proto = ProtoRequestQr.fromMap(data)
                socketSessionRepository.onRequestQrReceived(proto)
            }

            "check_qr" -> {
                val proto = ProtoCheckQr.fromMap(data)
                socketSessionRepository.onCheckQrReceived(proto)
            }

            "board_bus" -> {
                val proto = ProtoBoardBus.fromMap(data)
                socketSessionRepository.onBoardBusReceived(proto)
            }

            "alight_from_bus" -> {
                val proto = ProtoAlightFromBus.fromMap(data)
                socketSessionRepository.onAlightFromBusReceived(proto)
            }

            "error" -> {
                val proto = ProtoError.fromMap(data)
                socketSessionRepository.onErrorReceived(proto)
            }

            else -> {
                println("$TAG Header no manejado: '$header' — data=$data")
            }
        }
    }
}
