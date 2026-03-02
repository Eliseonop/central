package com.tcontur.central.domain.usecase

import com.tcontur.central.domain.repository.AuthRepository

class LogoutUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke() = repository.logout()
}
