package com.tcontur.central.inspectoria.initializer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.core.socket.ProtoSocketManager
import com.tcontur.central.core.socket.SocketEvent
import com.tcontur.central.core.socket.SocketServiceManager
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.data.AuthRepositoryImpl
import com.tcontur.central.data.EmpresaApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "[TCONTUR][INSPECTORIA_INIT]"

data class InspectoriaInitializerState(
    val isConnected: Boolean = false,
    val statusMessage: String = "Conectando con el servidor..."
)

sealed class InspectoriaInitializerEvent {
    data object NavigateToHome : InspectoriaInitializerEvent()
}

class InspectoriaInitializerViewModel(
    private val storage: AppStorage,
    private val authRepository: AuthRepositoryImpl,
    private val protoSocketManager: ProtoSocketManager,
    private val socketServiceManager: SocketServiceManager,
    private val empresaApiService: EmpresaApiService
) : ViewModel() {

    private val _state = MutableStateFlow(InspectoriaInitializerState())
    val state: StateFlow<InspectoriaInitializerState> = _state

    private val _events = MutableStateFlow<InspectoriaInitializerEvent?>(null)
    val events: StateFlow<InspectoriaInitializerEvent?> = _events

    init {
        println("$TAG ViewModel creado — iniciando conexión")
        fetchEmpresaAndConnect()
        observeSocketConnection()
        observeLoginConfirmation()
    }

    // ── Step 1: get empresa → extract compute IP → connect socket ─────────────

    private fun fetchEmpresaAndConnect() {
        viewModelScope.launch {
            val idStr = storage.getString(StorageKeys.EMPRESA_ID)
            val id    = idStr.toIntOrNull()
            println("$TAG Empresa ID almacenado: '$idStr'")

            val wsUrl: String = if (id != null) {
                println("$TAG Buscando empresa id=$id en API...")
                when (val result = empresaApiService.getEmpresaById(id)) {
                    is ApiResult.Success -> {
                        val ip = result.data.compute
                        println("$TAG Empresa encontrada — compute IP: '${ip.ifBlank { "(vacío — usando fallback)" }}'")
                        if (ip.isNotBlank()) "ws://$ip:22222?tipo=I" else fallbackWsUrl()
                    }
                    else -> {
                        println("$TAG Error al obtener empresa — usando fallback")
                        fallbackWsUrl()
                    }
                }
            } else {
                println("$TAG Sin empresa ID almacenado — usando fallback")
                fallbackWsUrl()
            }

            if (wsUrl.isNotBlank()) {
                println("$TAG 🚀 Iniciando tracking y conexión WS → $wsUrl")
                socketServiceManager.startLocationTracking()   // Inspectoria-only
                socketServiceManager.connect(wsUrl)
            } else {
                println("$TAG ❌ No se pudo determinar la URL del WS — sin conexión")
            }
        }
    }

    private fun fallbackWsUrl(): String {
        val code = storage.getString(StorageKeys.EMPRESA_CODE)
        val url  = if (code.isNotBlank()) "wss://$code-23lnu3rcea-uc.a.run.app/ws" else ""
        println("$TAG Fallback WS URL: '${url.ifBlank { "(vacío)" }}'")
        return url
    }

    // ── Step 2: socket connected → send login immediately ────────────────────

    private fun observeSocketConnection() {
        viewModelScope.launch {
            protoSocketManager.isConnected.collect { connected ->
                if (connected) {
                    println("$TAG ✅ Socket conectado — enviando frame de login")
                    _state.update { it.copy(statusMessage = "Logueando...") }
                    sendSocketLogin()
                } else {
                    println("$TAG 🔴 Socket desconectado")
                }
            }
        }
    }

    // ── Step 3: wait for server's login confirmation ──────────────────────────

    private fun observeLoginConfirmation() {
        viewModelScope.launch {
            protoSocketManager.socketEvents.collect { event ->
                if (event is SocketEvent.MessageDecoded && event.header == "login") {
                    println("$TAG 🟢 Login confirmado por el servidor — data: ${event.data}")
                    protoSocketManager.setAuthenticated(true)
                    _state.update { it.copy(isConnected = true, statusMessage = "¡Conexión exitosa!") }
                    delay(600)
                    println("$TAG 🏠 Navegando a InspectoriaRoot")
                    _events.value = InspectoriaInitializerEvent.NavigateToHome
                }
            }
        }
    }

    private suspend fun sendSocketLogin() {
        val user = authRepository.getStoredUser()
        if (user == null) {
            println("$TAG ❌ sendSocketLogin() — no hay usuario almacenado")
            return
        }
        println("$TAG 📤 Enviando login — id=${user.id} codigo=${user.codigo}")
        socketServiceManager.send(
            data      = hashMapOf("id" to user.id, "code" to user.codigo),
            formatKey = "login"
        )
    }

    fun consumeEvent() {
        _events.value = null
    }
}
