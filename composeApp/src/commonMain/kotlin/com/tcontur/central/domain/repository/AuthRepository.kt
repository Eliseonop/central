package com.tcontur.central.domain.repository

import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.domain.model.User

interface AuthRepository {
    /** Attempts a remote login and persists the session locally on success. */
    suspend fun login(empresaCodigo: String, username: String, password: String): ApiResult<User>

    /** Clears the local session. */
    suspend fun logout()

    /** Returns the locally-persisted user if a valid session exists, otherwise null. */
    suspend fun getStoredUser(): User?
}
