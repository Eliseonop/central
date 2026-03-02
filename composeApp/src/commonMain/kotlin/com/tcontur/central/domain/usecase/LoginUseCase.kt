package com.tcontur.central.domain.usecase

import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.domain.model.User
import com.tcontur.central.domain.repository.AuthRepository

class LoginUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(
        empresaCodigo: String,
        username: String,
        password: String
    ): ApiResult<User> = repository.login(empresaCodigo, username, password)
}
