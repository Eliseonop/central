package com.tcontur.central.presentation.role.inspectoria.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.domain.model.User
import com.tcontur.central.domain.repository.AuthRepository
import com.tcontur.central.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InspectoriaHomeState(
    val user: User? = null,
    val webUrl: String = "",
    val isLocationPermissionGranted: Boolean = false,
    val isLocationServiceEnabled: Boolean = false,
    val isBackgroundServiceRunning: Boolean = false
)

sealed class InspectoriaHomeEvent {
    data object LoggedOut : InspectoriaHomeEvent()
    data object NeedsLocationPermission : InspectoriaHomeEvent()
    data object NeedsLocationService : InspectoriaHomeEvent()
}

class InspectoriaHomeViewModel(
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: AuthRepository,
    private val storage: AppStorage
) : ViewModel() {

    private val _state = MutableStateFlow(InspectoriaHomeState())
    val state: StateFlow<InspectoriaHomeState> = _state

    private val _events = MutableStateFlow<InspectoriaHomeEvent?>(null)
    val events: StateFlow<InspectoriaHomeEvent?> = _events

    init {
        loadFromStorage()
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _events.value = InspectoriaHomeEvent.LoggedOut
        }
    }

    fun setLocationPermissionStatus(granted: Boolean) =
        _state.update { it.copy(isLocationPermissionGranted = granted) }

    fun setLocationServiceStatus(enabled: Boolean) =
        _state.update { it.copy(isLocationServiceEnabled = enabled) }

    fun setBackgroundServiceRunning(running: Boolean) =
        _state.update { it.copy(isBackgroundServiceRunning = running) }

    fun consumeEvent() { _events.value = null }

    private fun loadFromStorage() {
        // Build the WebView URL from stored credentials
        val token      = storage.getString(StorageKeys.AUTH_TOKEN)
        val empresaCod = storage.getString(StorageKeys.EMPRESA_CODE)
        if (token.isNotBlank() && empresaCod.isNotBlank()) {
            _state.update {
                it.copy(webUrl = "https://$empresaCod.tcontur.pe/verify-token?token=$token")
            }
        }
        // Also load User object so the drawer can display the name
        viewModelScope.launch {
            val user = authRepository.getStoredUser()
            if (user != null) _state.update { it.copy(user = user) }
        }
    }
}
