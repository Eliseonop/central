package com.tcontur.central.inspectoria.initializer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.core.socket.SocketServiceManager
import com.tcontur.central.core.socket.SocketSessionRepository
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.data.EmpresaApiService
import com.tcontur.central.data.repository.RoutesDataRepository
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
    private val socketServiceManager: SocketServiceManager,
    private val empresaApiService: EmpresaApiService,
    private val socketSessionRepository: SocketSessionRepository,
    private val routesDataRepository: RoutesDataRepository
) : ViewModel() {

    private val _state = MutableStateFlow(InspectoriaInitializerState())
    val state: StateFlow<InspectoriaInitializerState> = _state

    private val _events = MutableStateFlow<InspectoriaInitializerEvent?>(null)
    val events: StateFlow<InspectoriaInitializerEvent?> = _events

    init {
        println("$TAG ViewModel creado — iniciando conexión")
        fetchEmpresaAndConnect()
        // Login is sent by SocketDispatcherViewModel (app-scoped) on every
        // (re)connect — including reconnects after this ViewModel is destroyed.
        observeSessionAuthentication()
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
                socketServiceManager.startLocationTracking()
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

    // ── Step 2: wait for SocketDispatcherViewModel to confirm "login" ─────────
    //
    // SocketDispatcherViewModel (app-scoped) sends login on every (re)connect
    // and processes the server's "login" response → SocketSessionRepository.
    // We just observe the resulting authenticated state.

    private fun observeSessionAuthentication() {
        viewModelScope.launch {
            socketSessionRepository.isAuthenticated.collect { isAuth ->
                if (!isAuth) return@collect

                println("$TAG 🟢 Autenticación confirmada")
                _state.update { it.copy(isConnected = true, statusMessage = "¡Conexión exitosa!") }

                val empresaCodigo = storage.getString(StorageKeys.EMPRESA_CODE)
                val token = "r9a2vhtsjxjm_z6i3f5u0jicpyx2w0id24pguu"
                routesDataRepository.load(empresaCodigo, token)

                delay(600)
                println("$TAG 🏠 Navegando a InspectoriaRoot")
                _events.value = InspectoriaInitializerEvent.NavigateToHome
            }
        }
    }

    fun consumeEvent() {
        _events.value = null
    }
}
