package com.tcontur.central.data

import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.data.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.*
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.serialization.Serializable

private const val API_URL_GENERAL = "-23lnu3rcea-uc.a.run.app"

class InspeccionApiService(private val client: HttpClient) {

    private fun base(empresaCodigo: String) =
        "https://$empresaCodigo$API_URL_GENERAL"

    private fun HttpRequestBuilder.auth(token: String) =
        header(HttpHeaders.Authorization, "Token $token")

    // ── Listar inspecciones de hoy ────────────────────────────────────────────
    suspend fun getInspecciones(
        empresaCodigo: String,
        token: String,
        inspectorId: Int,
        dia: String
    ): ApiResult<List<InspeccionDto>> = runCatching {
        client.get("${base(empresaCodigo)}/api/inspecciones") {
            auth(token)
            parameter("inspector", inspectorId)
            parameter("dia", dia)
        }.body<List<InspeccionDto>>()
    }.toApiResult("Error al obtener inspecciones")

    // ── Ver inspección activa ─────────────────────────────────────────────────
    suspend fun verInspeccion(
        empresaCodigo: String,
        token: String
    ): ApiResult<InspeccionDto?> = runCatching {
        client.get("${base(empresaCodigo)}/api/inspecciones/ver") {
            auth(token)
        }.body<InspeccionDto?>()
    }.toApiResult("Error al verificar inspección activa")

    // ── Resumen del día ───────────────────────────────────────────────────────
    suspend fun getResumen(
        empresaCodigo: String,
        token: String,
        dia: String
    ): ApiResult<List<ResumenInspectorDto>> = runCatching {
        client.get("${base(empresaCodigo)}/api/inspecciones/resumen") {
            auth(token)
            parameter("dia", dia)
        }.body<List<ResumenInspectorDto>>()
    }.toApiResult("Error al obtener resumen")

    // ── Iniciar inspección ────────────────────────────────────────────────────
    suspend fun iniciarInspeccion(
        empresaCodigo: String,
        token: String,
        unidadId: Int,
        subidaId: Int?,
        subidaPos: PosDto?,
        ticketera: Boolean
    ): ApiResult<InspeccionDto> = runCatching {
        client.post("${base(empresaCodigo)}/api/inspecciones/iniciar") {
            auth(token)
            contentType(ContentType.Application.Json)
            setBody(IniciarBody(unidadId, subidaId, subidaPos, ticketera))
        }.body<InspeccionDto>()
    }.toApiResult("Error al iniciar inspección")

    // ── Cancelar inspección ───────────────────────────────────────────────────
    suspend fun cancelarInspeccion(
        empresaCodigo: String,
        token: String,
        id: Int,
        motivo: String
    ): ApiResult<InspeccionDto> = runCatching {
        client.post("${base(empresaCodigo)}/api/inspecciones/$id/cancelar") {
            auth(token)
            contentType(ContentType.Application.Json)
            setBody(CancelarBody(motivo))
        }.body<InspeccionDto>()
    }.toApiResult("Error al cancelar inspección")

    // ── Finalizar inspección ──────────────────────────────────────────────────
    suspend fun finalizarInspeccion(
        empresaCodigo: String,
        token: String,
        id: Int,
        cortes: List<CorteFinDto>,
        reintegros: Int,
        reintegrosMonto: Double,
        pasajerosVivos: Int,
        pasajerosMonto: Double,
        bajadaId: Int?,
        bajadaPos: PosDto?,
        ocurrencias: List<OcurrenciaFinDto>
    ): ApiResult<InspeccionDto> = runCatching {
        client.put("${base(empresaCodigo)}/api/inspecciones/$id/finalizar") {
            auth(token)
            contentType(ContentType.Application.Json)
            setBody(
                FinalizarBody(
                    id, cortes, reintegros, reintegrosMonto,
                    pasajerosVivos, pasajerosMonto, bajadaId, bajadaPos, ocurrencias
                )
            )
        }.body<InspeccionDto>()
    }.toApiResult("Error al finalizar inspección")

    // ── Suministros (stock de tickets por unidad) ─────────────────────────────
    // GET /api/suministros?unidad={unidadId}
    // Fetched by UNIT id (not inspection id) – only "En Uso" supplies are active.
    suspend fun getSuministros(
        empresaCodigo: String,
        token: String,
        unidadId: Int
    ): ApiResult<List<SuministroDto>> = runCatching {
        client.get("${base(empresaCodigo)}/api/suministros") {
            auth(token)
            parameter("unidad", unidadId)
        }.body<List<SuministroDto>>()
    }.toApiResult("Error al obtener suministros")

    // ── Listar unidades ───────────────────────────────────────────────────────
    suspend fun getUnidades(
        empresaCodigo: String,
        token: String
    ): ApiResult<List<UnidadSelectDto>> = runCatching {
        client.get("${base(empresaCodigo)}/api/unidades") {
            auth(token)
        }.body<List<UnidadSelectDto>>()
    }.toApiResult("Error al obtener unidades")

    // ── Validar cercanía ──────────────────────────────────────────────────────
    // POST /api/unidades/validar_cercania
    // Body: { posicion: {lat, lng}, unidad: <id> }
    // Response: { validation: bool, paradero: Int }
    suspend fun validarCercania(
        empresaCodigo: String,
        token: String,
        unidadId: Int,
        lat: Double,
        lng: Double
    ): ApiResult<ValidarCercaniaResponse> = runCatching {
        client.post("${base(empresaCodigo)}/api/unidades/validar_cercania") {
            auth(token)
            contentType(ContentType.Application.Json)
            setBody(ValidarCercaniaBody(posicion = PosDto(lat, lng), unidad = unidadId))
        }.body<ValidarCercaniaResponse>()
    }.toApiResult("Error al validar cercanía")

    // ── Body DTOs ─────────────────────────────────────────────────────────────

    @Serializable
    data class ValidarCercaniaBody(
        val posicion: PosDto,
        val unidad: Int
    )

    @Serializable
    data class IniciarBody(
        val unidad: Int,
        val subida: Int?,
        val subida_pos: PosDto?,
        val ticketera: Boolean
    )

    @Serializable
    data class CancelarBody(val motivo: String)

    @Serializable
    data class FinalizarBody(
        val id: Int,
        val cortes: List<CorteFinDto>,
        val reintegros: Int,
        val reintegros_monto: Double,
        val pasajeros_vivos: Int,
        val pasajeros_monto: Double,
        val bajada: Int?,
        val bajada_pos: PosDto?,
        val ocurrencias: List<OcurrenciaFinDto>
    )

    @Serializable
    data class CorteFinDto(
        val boleto: Int,
        val numero: Int,
        val reintegros: Int,
        val pasajeros_vivos: Int
    )

    @Serializable
    data class OcurrenciaFinDto(
        val motivo: String,
        val falta: Int?,
        val cargo: Boolean,      // true = conductor, false = cobrador
        val imagenes: List<String>
    )

    // ── Helper ────────────────────────────────────────────────────────────────
    private fun <T> Result<T>.toApiResult(fallback: String): ApiResult<T> = fold(
        onSuccess = { ApiResult.Success(it) },
        onFailure = { ApiResult.Error(message = it.message ?: fallback) }
    )
}
