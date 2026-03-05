package com.tcontur.central.data

import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.core.network.map
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.data.model.UserResponse
import com.tcontur.central.data.model.UserRole
import com.tcontur.central.domain.User
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val api: AuthApiService,
    private val storage: AppStorage
) {
    suspend fun login(empresaCodigo: String, username: String, password: String): ApiResult<User> {
        val result = api.login(empresaCodigo, username, password)
        if (result is ApiResult.Success) {
            storage.putString(StorageKeys.USER_JSON, Json.encodeToString(result.data))
            storage.putString(StorageKeys.AUTH_TOKEN, result.data.token)
            storage.putString(StorageKeys.EMPRESA_CODE, empresaCodigo)
        }
        return result.map { it.toDomain() }
    }

    suspend fun logout() {
        storage.remove(StorageKeys.USER_JSON)
        storage.remove(StorageKeys.AUTH_TOKEN)
        storage.remove(StorageKeys.EMPRESA_CODE)
    }

    suspend fun getStoredUser(): User? {
        val json = storage.getString(StorageKeys.USER_JSON)
        return if (json.isBlank()) null
        else runCatching { Json.decodeFromString<UserResponse>(json).toDomain() }.getOrNull()
    }

    private fun UserResponse.toDomain() = User(
        id       = id,
        nombre   = nombre,
        username = username,
        email    = email,
        token    = token,
        cargo    = cargo,
        empresa  = empresa,
        codigo   = codigo,
        role     = UserRole.fromCargo(cargo)
    )
}
