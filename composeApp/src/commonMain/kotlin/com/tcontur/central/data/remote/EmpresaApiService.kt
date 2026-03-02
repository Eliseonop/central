package com.tcontur.central.data.remote

import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.data.model.EmpresaResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

private const val API_URL_URBANITO = "https://urbanito-23lnu3rcea-uc.a.run.app"

/**
 * Fetches the list of available companies from the central API.
 *
 * Endpoint: GET {API_URL_URBANITO}/tracker/empresas
 */
class EmpresaApiService(private val client: HttpClient) {

    suspend fun fetchEmpresas(): ApiResult<List<EmpresaResponse>> = runCatching {
        client.get("$API_URL_URBANITO/tracker/empresas").body<List<EmpresaResponse>>()
    }.fold(
        onSuccess = { ApiResult.Success(it) },
        onFailure = { ApiResult.Error(message = it.message ?: "Could not fetch companies") }
    )
}
