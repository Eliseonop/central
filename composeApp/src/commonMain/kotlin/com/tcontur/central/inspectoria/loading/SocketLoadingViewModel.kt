package com.tcontur.central.inspectoria.loading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.core.socket.ProtoSocketManager
import com.tcontur.central.core.socket.SocketServiceManager
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.data.AuthRepositoryImpl
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.data.EmpresaApiService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SocketLoadingState(
    val isConnected: Boolean = false,
    val statusMessage: String = "Conectando con el servidor..."
)

sealed class SocketLoadingEvent {
    data object NavigateToHome : SocketLoadingEvent()
}

class SocketLoadingViewModel(
    private val storage: AppStorage,
    private val authRepository: AuthRepositoryImpl,
    private val protoSocketManager: ProtoSocketManager,
    private val socketServiceManager: SocketServiceManager,
    private val empresaApiService: EmpresaApiService
) : ViewModel() {

    private val _state = MutableStateFlow(SocketLoadingState())
    val state: StateFlow<SocketLoadingState> = _state

    private val _events = MutableStateFlow<SocketLoadingEvent?>(null)
    val events: StateFlow<SocketLoadingEvent?> = _events

    init {
        fetchEmpresaAndConnect()
        observeSocketConnection()
    }

    // ── Step 1: get empresa → extract compute IP → connect socket ─────────────

    private fun fetchEmpresaAndConnect() {
        viewModelScope.launch {
            val idStr = storage.getString(StorageKeys.EMPRESA_ID)
            val id    = idStr.toIntOrNull()

            val wsUrl: String = if (id != null) {
                when (val result = empresaApiService.getEmpresaById(id)) {
                    is ApiResult.Success -> {
                        val ip = result.data.compute
                        if (ip.isNotBlank()) {
                            "ws://$ip:22222?tipo=I"
                        } else {
                            fallbackWsUrl()
                        }
                    }
                    else -> fallbackWsUrl()
                }
            } else {
                fallbackWsUrl()
            }

            if (wsUrl.isNotBlank()) {
                socketServiceManager.connect(wsUrl)
            }
        }
    }

    /** Fallback cloud URL in case the empresa has no compute IP. */
    private fun fallbackWsUrl(): String {
        val code = storage.getString(StorageKeys.EMPRESA_CODE)
        return if (code.isNotBlank()) "wss://$code-23lnu3rcea-uc.a.run.app/ws" else ""
    }

    // ── Step 2: observe connection → send login → navigate ───────────────────

    private fun observeSocketConnection() {
        viewModelScope.launch {
            protoSocketManager.isConnected.collect { connected ->
                if (connected) {
                    // Negotiate: send login message before showing home
                    sendSocketLogin()

                    _state.update { it.copy(isConnected = true, statusMessage = "¡Conexión exitosa!") }
                    delay(600) // brief success flash
                    _events.value = SocketLoadingEvent.NavigateToHome
                }
            }
        }
    }

    /**
     * Login negotiation over the WebSocket.
     *
     * Both fields come from the login response (stored in USER_JSON).
     * The protobin schema defines both as Number, so they are sent as Int:
     *   - "id"   → user.id     (Int)
     *   - "code" → user.codigo (Int)
     */
    private suspend fun sendSocketLogin() {
        val user = authRepository.getStoredUser() ?: return

        socketServiceManager.send(
            data      = hashMapOf("id" to user.id, "code" to user.codigo),
            formatKey = "login"
        )
    }

    fun consumeEvent() {
        _events.value = null
    }
}
