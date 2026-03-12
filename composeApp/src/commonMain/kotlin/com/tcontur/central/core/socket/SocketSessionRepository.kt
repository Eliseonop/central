package com.tcontur.central.core.socket

import com.tcontur.central.core.socket.models.ProtoAlightFromBus
import com.tcontur.central.core.socket.models.ProtoBoardBus
import com.tcontur.central.core.socket.models.ProtoCheckQr
import com.tcontur.central.core.socket.models.ProtoError
import com.tcontur.central.core.socket.models.ProtoLogin
import com.tcontur.central.core.socket.models.ProtoPosition
import com.tcontur.central.core.socket.models.ProtoRequestQr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG = "[TCONTUR][SOCKET_SESSION]"

/**
 * Single source of truth for data received from the server via WebSocket.
 *
 * [SocketDispatcherViewModel] parses each decoded message into a typed Proto
 * model and writes it here.  Any ViewModel that needs server-pushed data
 * simply collects the relevant StateFlow — no direct socket subscription needed.
 *
 * Pattern mirrors SessionManager / GpsRepository in login_tcontur.
 *
 * ┌─────────────────────────────────────────────────────────────────┐
 * │  header           │  Proto model        │  StateFlow            │
 * ├─────────────────────────────────────────────────────────────────┤
 * │  "login"          │  ProtoLogin         │  loginData            │
 * │  "position"       │  ProtoPosition      │  positionData         │
 * │  "request_qr"     │  ProtoRequestQr     │  requestQrData        │
 * │  "check_qr"       │  ProtoCheckQr       │  checkQrData          │
 * │  "board_bus"      │  ProtoBoardBus      │  boardBusData         │
 * │  "alight_from_bus"│  ProtoAlightFromBus │  alightFromBusData    │
 * │  "error"          │  ProtoError         │  errorData            │
 * └─────────────────────────────────────────────────────────────────┘
 */
class SocketSessionRepository {

    // ── login ──────────────────────────────────────────────────────────────────

    private val _loginData = MutableStateFlow<ProtoLogin?>(null)
    val loginData: StateFlow<ProtoLogin?> = _loginData.asStateFlow()

    /**
     * True once the server has responded with a "login" confirmation.
     * [InspectoriaInitializerViewModel] observes this to trigger navigation.
     */
    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated.asStateFlow()

    fun onLoginReceived(proto: ProtoLogin) {
        println("$TAG onLoginReceived — id=${proto.id}")
        _loginData.value       = proto
        _isAuthenticated.value = true
    }

    // ── position ───────────────────────────────────────────────────────────────

    /**
     * Latest position snapshot from the server.
     * Contains the list of nearby vehicles with their GPS coordinates.
     * The map composable collects this to render vehicle markers.
     */
    private val _positionData = MutableStateFlow<ProtoPosition?>(null)
    val positionData: StateFlow<ProtoPosition?> = _positionData.asStateFlow()

    fun onPositionReceived(proto: ProtoPosition) {
        println("$TAG onPositionReceived — vehicles=${proto.vehicles.size}")
        _positionData.value = proto
    }

    // ── request_qr ─────────────────────────────────────────────────────────────

    private val _requestQrData = MutableStateFlow<ProtoRequestQr?>(null)
    val requestQrData: StateFlow<ProtoRequestQr?> = _requestQrData.asStateFlow()

    fun onRequestQrReceived(proto: ProtoRequestQr) {
        println("$TAG onRequestQrReceived — expiresAt=${proto.expiresAt}")
        _requestQrData.value = proto
    }

    // ── check_qr ───────────────────────────────────────────────────────────────

    private val _checkQrData = MutableStateFlow<ProtoCheckQr?>(null)
    val checkQrData: StateFlow<ProtoCheckQr?> = _checkQrData.asStateFlow()

    fun onCheckQrReceived(proto: ProtoCheckQr) {
        println("$TAG onCheckQrReceived — trip=${proto.inspeccion}  tickets=${proto.tickets.size}")
        _checkQrData.value = proto
    }

    // ── board_bus ──────────────────────────────────────────────────────────────

    private val _boardBusData = MutableStateFlow<ProtoBoardBus?>(null)
    val boardBusData: StateFlow<ProtoBoardBus?> = _boardBusData.asStateFlow()

    fun onBoardBusReceived(proto: ProtoBoardBus) {
        println("$TAG onBoardBusReceived — success=${proto.success}")
        _boardBusData.value = proto
    }

    // ── alight_from_bus ────────────────────────────────────────────────────────
    fun clearCheckQr() { _checkQrData.value = null }
    private val _alightFromBusData = MutableStateFlow<ProtoAlightFromBus?>(null)
    val alightFromBusData: StateFlow<ProtoAlightFromBus?> = _alightFromBusData.asStateFlow()

    fun onAlightFromBusReceived(proto: ProtoAlightFromBus) {
        println("$TAG onAlightFromBusReceived — success=${proto.success}")
        _alightFromBusData.value = proto
    }

    // ── error ──────────────────────────────────────────────────────────────────

    private val _errorData = MutableStateFlow<ProtoError?>(null)
    val errorData: StateFlow<ProtoError?> = _errorData.asStateFlow()

    fun onErrorReceived(proto: ProtoError) {
        println("$TAG onErrorReceived — error=${proto.error}")
        _errorData.value = proto
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────────

    /** Called by [SessionManager.logout] to wipe all session state. */
    fun clearSession() {
        println("$TAG clearSession")
        _loginData.value         = null
        _isAuthenticated.value   = false
        _positionData.value      = null
        _requestQrData.value     = null
        _checkQrData.value       = null
        _boardBusData.value      = null
        _alightFromBusData.value = null
        _errorData.value         = null
    }
}
