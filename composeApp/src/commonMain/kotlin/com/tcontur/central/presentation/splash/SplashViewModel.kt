package com.tcontur.central.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.domain.usecase.CheckLoginStatusUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SplashState {
    data object Loading : SplashState()
    data class Authenticated(val role: String) : SplashState()
    data object Unauthenticated : SplashState()
}

class SplashViewModel(
    private val checkLoginStatus: CheckLoginStatusUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state

    init {
        viewModelScope.launch {
            delay(1_800) // minimum splash display time
            val user = checkLoginStatus()
            _state.value = if (user != null)
                SplashState.Authenticated(user.cargo)
            else
                SplashState.Unauthenticated
        }
    }
}
