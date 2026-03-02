package com.tcontur.central.presentation.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.domain.model.User
import com.tcontur.central.domain.usecase.GetEmpresasUseCase
import com.tcontur.central.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class LoginEvent {
    data class LoginSuccess(val user: User) : LoginEvent()
}

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val getEmpresasUseCase: GetEmpresasUseCase,
    private val storage: AppStorage,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events

    init {
        loadSavedCredentials()
        fetchEmpresas()
    }

    fun onUsernameChange(value: String) = _uiState.update { it.copy(username = value) }
    fun onPasswordChange(value: String) = _uiState.update { it.copy(password = value) }
    fun onRememberMeChange(value: Boolean) = _uiState.update { it.copy(rememberMe = value) }
    fun onEmpresaSelected(index: Int) {
        val empresa = _uiState.value.empresas.getOrNull(index) ?: return
        _uiState.update { it.copy(selectedEmpresa = empresa) }
    }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun login() {
        val state = _uiState.value
        val empresa = state.selectedEmpresa ?: run {
            _uiState.update { it.copy(errorMessage = "Selecciona una empresa") }
            return
        }
        if (state.username.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu usuario") }
            return
        }
        if (state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Ingresa tu contraseña") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingIn = true, errorMessage = null) }
            when (val result = loginUseCase(empresa.codigo, state.username, state.password)) {
                is ApiResult.Success -> {
                    if (state.rememberMe) {
                        storage.putBoolean(StorageKeys.REMEMBER_ME, true)
                        storage.putString(StorageKeys.SAVED_USERNAME, state.username)
                    }
                    _events.emit(LoginEvent.LoginSuccess(result.data))
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(isLoggingIn = false, errorMessage = result.message)
                    }
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    private fun fetchEmpresas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEmpresas = true) }
            when (val result = getEmpresasUseCase()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(empresas = result.data, isLoadingEmpresas = false)
                }
                is ApiResult.Error   -> _uiState.update {
                    it.copy(isLoadingEmpresas = false, errorMessage = result.message)
                }
                ApiResult.Loading    -> Unit
            }
        }
    }

    private fun loadSavedCredentials() {
        if (storage.getBoolean(StorageKeys.REMEMBER_ME)) {
            _uiState.update {
                it.copy(
                    username   = storage.getString(StorageKeys.SAVED_USERNAME),
                    rememberMe = true
                )
            }
        }
    }
}
