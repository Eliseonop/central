package com.tcontur.central.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.data.AuthRepositoryImpl
import com.tcontur.central.data.EmpresaApiService
import com.tcontur.central.domain.Empresa
import com.tcontur.central.domain.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val empresas: List<Empresa>    = emptyList(),
    val selectedEmpresa: Empresa?  = null,
    val username: String           = "",
    val password: String           = "",
    val rememberMe: Boolean        = false,
    val isLoadingEmpresas: Boolean = false,
    val isLoggingIn: Boolean       = false,
    val errorMessage: String?      = null
)

sealed class LoginEvent {
    data class LoginSuccess(val user: User) : LoginEvent()
}

class LoginViewModel(
    private val auth: AuthRepositoryImpl,
    private val empresaApi: EmpresaApiService,
    private val storage: AppStorage
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events

    init {
        loadSavedCredentials()
        fetchEmpresas()
    }

    fun onUsernameChange(value: String)  = _uiState.update { it.copy(username = value) }
    fun onPasswordChange(value: String)  = _uiState.update { it.copy(password = value) }
    fun onRememberMeChange(value: Boolean) = _uiState.update { it.copy(rememberMe = value) }
    fun onEmpresaSelected(index: Int) =
        _uiState.update { it.copy(selectedEmpresa = it.empresas.getOrNull(index)) }
    fun clearError() = _uiState.update { it.copy(errorMessage = null) }

    fun login() {
        val s = _uiState.value
        val empresa = s.selectedEmpresa ?: return _uiState.update { it.copy(errorMessage = "Selecciona una empresa") }
        if (s.username.isBlank()) return _uiState.update { it.copy(errorMessage = "Ingresa tu usuario") }
        if (s.password.isBlank()) return _uiState.update { it.copy(errorMessage = "Ingresa tu contraseña") }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingIn = true, errorMessage = null) }
            when (val result = auth.login(empresa.codigo, s.username, s.password)) {
                is ApiResult.Success -> {
                    storage.putString(StorageKeys.EMPRESA_ID, empresa.id.toString())
                    if (s.rememberMe) {
                        storage.putBoolean(StorageKeys.REMEMBER_ME, true)
                        storage.putString(StorageKeys.SAVED_USERNAME, s.username)
                    }
                    _events.emit(LoginEvent.LoginSuccess(result.data))
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoggingIn = false, errorMessage = result.message) }
                ApiResult.Loading  -> Unit
            }
        }
    }

    private fun fetchEmpresas() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingEmpresas = true) }
            when (val result = empresaApi.fetchEmpresas()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(
                        empresas = result.data.map { e -> Empresa(e.id, e.codigo, e.nombre) },
                        isLoadingEmpresas = false
                    )
                }
                is ApiResult.Error -> _uiState.update { it.copy(isLoadingEmpresas = false, errorMessage = result.message) }
                ApiResult.Loading  -> Unit
            }
        }
    }

    private fun loadSavedCredentials() {
        if (storage.getBoolean(StorageKeys.REMEMBER_ME)) {
            _uiState.update { it.copy(username = storage.getString(StorageKeys.SAVED_USERNAME), rememberMe = true) }
        }
    }
}
