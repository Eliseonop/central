package com.tcontur.central.inspectoria.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.core.socket.ProtoSocketManager
import com.tcontur.central.core.socket.SocketEvent
import com.tcontur.central.core.socket.SocketServiceManager
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.data.AuthRepositoryImpl
import com.tcontur.central.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InspectoriaHomeState(
    val user: User? = null,
    val webUrl: String = "",
    val isSocketConnected: Boolean = false
)

sealed class InspectoriaHomeEvent {
    data object LoggedOut : InspectoriaHomeEvent()
}

class InspectoriaHomeViewModel(
    private val auth: AuthRepositoryImpl,
    private val storage: AppStorage,
    private val protoSocketManager: ProtoSocketManager,
    private val socketServiceManager: SocketServiceManager
) : ViewModel() {

    private val _state = MutableStateFlow(InspectoriaHomeState())
    val state: StateFlow<InspectoriaHomeState> = _state

    private val _events = MutableStateFlow<InspectoriaHomeEvent?>(null)
    val events: StateFlow<InspectoriaHomeEvent?> = _events

    init {
        loadFromStorage()
        observeSocket()
    }

    private fun observeSocket() {
        viewModelScope.launch {
            protoSocketManager.isConnected.collect { connected ->
                _state.update { it.copy(isSocketConnected = connected) }
            }
        }
        viewModelScope.launch {
            protoSocketManager.socketEvents.collect { event ->
                if (event is SocketEvent.ConnectionSuccess) sendLoginMessage()
            }
        }
    }

    private fun sendLoginMessage() {
        val user = _state.value.user ?: return
        socketServiceManager.send(
            data      = hashMapOf("id" to user.id, "code" to user.codigo),
            formatKey = "login"
        )
    }

    private fun loadFromStorage() {
        val token = storage.getString(StorageKeys.AUTH_TOKEN)
        val code  = storage.getString(StorageKeys.EMPRESA_CODE)
        if (token.isNotBlank() && code.isNotBlank()) {
            _state.update { it.copy(webUrl = "https://$code.tcontur.pe/verify-token?token=$token") }
        }
        viewModelScope.launch {
            auth.getStoredUser()?.let { user -> _state.update { it.copy(user = user) } }
        }
    }

    fun logout() {
        viewModelScope.launch {
            socketServiceManager.disconnect()
            auth.logout()
            _events.value = InspectoriaHomeEvent.LoggedOut
        }
    }

    fun consumeEvent() { _events.value = null }
}
