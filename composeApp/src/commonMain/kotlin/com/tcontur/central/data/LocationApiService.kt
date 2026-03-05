package com.tcontur.central.data

import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.data.model.LocationRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

private const val API_URL_GENERAL = "-23lnu3rcea-uc.a.run.app"


class LocationApiService(private val client: HttpClient) {

    suspend fun sendLocation(
        empresaCodigo: String,
        token: String,
        request: LocationRequest
    ): ApiResult<Unit> = runCatching {
        val url = "https://$empresaCodigo$API_URL_GENERAL/api/inspecciones/update_position"
        client.post(url) {
            header(HttpHeaders.Authorization, "Token $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        Unit
    }.fold(
        onSuccess = { ApiResult.Success(Unit) },
        onFailure = { ApiResult.Error(message = it.message ?: "Location update failed") }
    )
}
