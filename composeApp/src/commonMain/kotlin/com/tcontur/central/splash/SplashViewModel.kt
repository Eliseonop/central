package com.tcontur.central.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.data.AuthRepositoryImpl
import com.tcontur.central.domain.auth.UserRole
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SplashState {
    data object Loading : SplashState()
    data class Authenticated(val role: UserRole) : SplashState()
    data object Unauthenticated : SplashState()
}

class SplashViewModel(private val auth: AuthRepositoryImpl) : ViewModel() {
    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state

    init {
        viewModelScope.launch {
            delay(1_800)
            val user = auth.getStoredUser()
            _state.value = if (user != null) SplashState.Authenticated(user.role)
                           else SplashState.Unauthenticated
        }
    }
}
