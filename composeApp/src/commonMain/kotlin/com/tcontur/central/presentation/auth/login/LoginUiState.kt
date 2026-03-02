package com.tcontur.central.presentation.auth.login

import com.tcontur.central.domain.model.Empresa

data class LoginUiState(
    val empresas: List<Empresa>     = emptyList(),
    val selectedEmpresa: Empresa?   = null,
    val username: String            = "",
    val password: String            = "",
    val rememberMe: Boolean         = false,
    val isLoadingEmpresas: Boolean  = false,
    val isLoggingIn: Boolean        = false,
    val errorMessage: String?       = null
)
