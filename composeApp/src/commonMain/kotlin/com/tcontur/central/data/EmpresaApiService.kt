package com.tcontur.central.data

import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.data.model.EmpresaResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header

private const val API_URL_URBANITO     = "https://urbanito-23lnu3rcea-uc.a.run.app"
private const val API_URL_MULTIEMPRESA = "https://multiempresa-23lnu3rcea-uc.a.run.app"

private const val MULTIEMPRESA_TOKEN =
    "Token r9\$a2+v\$htsjxjm_z6i3@f5@=u0jicpyx&*2w+0-id24pg_u+"

class EmpresaApiService(private val client: HttpClient) {

    suspend fun fetchEmpresas(): ApiResult<List<EmpresaResponse>> = runCatching {
        client.get("$API_URL_URBANITO/tracker/empresas").body<List<EmpresaResponse>>()
    }.fold(
        onSuccess = { ApiResult.Success(it) },
        onFailure = { ApiResult.Error(message = it.message ?: "Could not fetch companies") }
    )


    suspend fun getEmpresaById(id: Int): ApiResult<EmpresaResponse> = runCatching {
        client.get("$API_URL_MULTIEMPRESA/api/empresas/$id") {
            header("Authorization", MULTIEMPRESA_TOKEN)
        }.body<EmpresaResponse>()
    }.fold(
        onSuccess = { ApiResult.Success(it) },
        onFailure = { ApiResult.Error(message = it.message ?: "Could not fetch empresa") }
    )
}
