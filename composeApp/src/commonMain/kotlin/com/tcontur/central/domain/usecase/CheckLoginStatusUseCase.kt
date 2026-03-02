package com.tcontur.central.domain.usecase

import com.tcontur.central.domain.model.User
import com.tcontur.central.domain.repository.AuthRepository

class CheckLoginStatusUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): User? = repository.getStoredUser()
}
