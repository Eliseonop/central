package com.tcontur.central.data.remote

import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.data.model.UserResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

private const val API_URL_GENERAL = "-23lnu3rcea-uc.a.run.app" // injected via env in prod

/**
 * Authenticates a user against the per-company token endpoint.
 *
 * Endpoint: POST https://{empresaCodigo}{API_URL_GENERAL}/api/token-auth
 */
class AuthApiService(private val client: HttpClient) {

    suspend fun login(
        empresaCodigo: String,
        username: String,
        password: String
    ): ApiResult<UserResponse> = runCatching {
        val url = "https://$empresaCodigo$API_URL_GENERAL/api/token-auth"
        val response = client.post(url) {
            contentType(ContentType.Application.Json)
            setBody(LoginBody(username, password))
        }
        response.body<UserResponse>()
    }.fold(
        onSuccess = { ApiResult.Success(it) },
        onFailure = { ApiResult.Error(message = it.message ?: "Login failed") }
    )

    @Serializable
    private data class LoginBody(val username: String, val password: String)
}
