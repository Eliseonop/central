package com.tcontur.central.inspectoria.inspeccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.core.QrDataHolder
import com.tcontur.central.core.location.LocationManager
import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.data.AuthRepositoryImpl
import com.tcontur.central.data.InspeccionApiService
import com.tcontur.central.data.model.InspeccionDto
import com.tcontur.central.data.model.PosDto
import com.tcontur.central.domain.inspectoria.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InspeccionState(
    val inspeccion: Inspeccion? = null,
    val cortes: List<CorteItem> = emptyList(),
    val ocurrencias: List<OcurrenciaItem> = emptyList(),
    val selectedTab: Int = 0,          // 0=Cortes, 1=Cobros, 2=Ocurrencias
    val selectedCobrosTab: Int = 0,    // 0=Reintegros, 1=Pasajeros Vivos
    val totalReintegros: Int = 0,
    val totalReintegrosMonto: Double = 0.0,
    val totalPasajeros: Int = 0,
    val totalPasajerosMonto: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val cercaniaMessage: String? = null,  // proximity result shown before finalize
    val showCancelDialog: Boolean = false,
    val cancelMotivo: String = "",
    val showFinalizeDialog: Boolean = false,
    val newOcurrenciaMotivo: String = "",
    val newOcurrenciaCargo: String = "Conductor"
)

sealed class InspeccionEvent {
    data object Finalized : InspeccionEvent()
    data object Cancelled : InspeccionEvent()
}

class InspeccionViewModel(
    private val auth: AuthRepositoryImpl,
    private val storage: AppStorage,
    private val inspeccionApi: InspeccionApiService,
    private val locationManager: LocationManager,
    private val qrDataHolder: QrDataHolder
) : ViewModel() {

    private val _state = MutableStateFlow(InspeccionState())
    val state: StateFlow<InspeccionState> = _state

    private val _events = MutableStateFlow<InspeccionEvent?>(null)
    val events: StateFlow<InspeccionEvent?> = _events

    fun loadInspeccion(id: Int) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val token = storage.getString(StorageKeys.AUTH_TOKEN)
            val code  = storage.getString(StorageKeys.EMPRESA_CODE)

            val qrCortes = qrDataHolder.qrCortes
            qrDataHolder.clear()   // consume once

            val inspResult = inspeccionApi.verInspeccion(code, token)
            if (inspResult is ApiResult.Success && inspResult.data != null) {
                val dto  = inspResult.data
                println(dto)
                val insp = dto.toDomain()

                val cortes: List<CorteItem> = if ( qrCortes != null && false) {
                    qrCortes.map { qrc ->
                        CorteItem(
                            boletoId       = qrc.boletoId,
                            nombre         = "",     // boleto name not in QR; UI shows boletoId
                            serie          = "TKT",
                            tarifa         = 0.0,    // tarifa fetched separately if needed
                            color          = "blue",
                            inicio         = qrc.inicio,
                            fin            = qrc.fin,
                            numero         = qrc.fin,
                            reintegros     = 0,
                            pasajerosVivos = 0,
                            mostrar        = true,
                            terminado      = false
                        )
                    }
                } else {
                    // GET /api/suministros?unidad={unidadId}
                    val suministrosResult = inspeccionApi.getSuministros(code, token, insp.unidadId)
                    val suministros = if (suministrosResult is ApiResult.Success) suministrosResult.data else emptyList()
                    buildCortes(suministros.map { s ->
                        CorteItem(
                            boletoId       = s.boleto.id,
                            nombre         = s.boleto.nombre,
                            serie          = s.serie,            // batch series (from suministro, not boleto)
                            tarifa         = s.boleto.tarifa,
                            color          = s.boleto.color,
                            inicio         = s.actual,           // current position (inspector starts here)
                            fin            = s.fin,
                            numero         = s.actual,           // initial display number
                            reintegros     = 0,
                            pasajerosVivos = 0,
                            mostrar        = s.estado == "En Uso", // API-driven visibility
                            terminado      = false
                        )
                    }, dto)
                }

                _state.update { it.copy(inspeccion = insp, cortes = cortes, isLoading = false) }
                recalcularTotales()
            } else {
                _state.update { it.copy(isLoading = false, error = "No se encontró la inspección") }
            }
        }
    }

    fun selectTab(index: Int)       = _state.update { it.copy(selectedTab = index) }
    fun selectCobrosTab(index: Int) = _state.update { it.copy(selectedCobrosTab = index) }

    // ── Cortes ────────────────────────────────────────────────────────────────

    fun updateCorteNumero(index: Int, numero: Int) {
        val cortes = _state.value.cortes.toMutableList()
        if (index !in cortes.indices) return
        cortes[index] = cortes[index].copy(numero = numero)
        _state.update { it.copy(cortes = cortes) }
    }

    fun terminarCorte(index: Int) {
        val cortes = _state.value.cortes.toMutableList()
        if (index !in cortes.indices) return
        val boletoId = cortes[index].boletoId
        // Angular behavior: terminated card becomes HIDDEN (mostrar = false).
        // Reset numero back to inicio (current position) in case inspector typed incorrectly.
        cortes[index] = cortes[index].copy(
            terminado = true,
            mostrar   = false,
            numero    = cortes[index].inicio
        )
        // Reveal the next non-terminated supply for the same boleto type
        val nextIdx = (index + 1 until cortes.size)
            .firstOrNull { cortes[it].boletoId == boletoId && !cortes[it].terminado }
        if (nextIdx != null) {
            cortes[nextIdx] = cortes[nextIdx].copy(mostrar = true)
        }
        _state.update { it.copy(cortes = cortes) }
    }

    fun reestablecerCorte(index: Int) {
        val cortes    = _state.value.cortes.toMutableList()
        if (index !in cortes.indices) return
        val boletoId  = cortes[index].boletoId
        val totalSame = cortes.count { it.boletoId == boletoId }
        // Hide and reset all OTHER supplies for the same boleto (Angular behavior)
        cortes.indices.forEach { i ->
            if (i != index && cortes[i].boletoId == boletoId) {
                cortes[i] = cortes[i].copy(mostrar = false, terminado = false)
            }
        }
        // Restore this corte as the active one
        cortes[index] = cortes[index].copy(
            mostrar   = true,
            terminado = false,
            quedan    = totalSame > 1
        )
        _state.update { it.copy(cortes = cortes) }
    }

    // ── Reintegros ────────────────────────────────────────────────────────────

    fun incrementarReintegro(index: Int) = updateReintegro(index, 1)
    fun decrementarReintegro(index: Int) = updateReintegro(index, -1)

    private fun updateReintegro(index: Int, delta: Int) {
        val cortes = _state.value.cortes.toMutableList()
        if (index !in cortes.indices) return
        val nuevo = (cortes[index].reintegros + delta).coerceAtLeast(0)
        cortes[index] = cortes[index].copy(reintegros = nuevo)
        _state.update { it.copy(cortes = cortes) }
        recalcularTotales()
    }

    // ── Pasajeros ─────────────────────────────────────────────────────────────

    fun incrementarPasajero(index: Int) = updatePasajero(index, 1)
    fun decrementarPasajero(index: Int) = updatePasajero(index, -1)

    private fun updatePasajero(index: Int, delta: Int) {
        val cortes = _state.value.cortes.toMutableList()
        if (index !in cortes.indices) return
        val nuevo = (cortes[index].pasajerosVivos + delta).coerceAtLeast(0)
        cortes[index] = cortes[index].copy(pasajerosVivos = nuevo)
        _state.update { it.copy(cortes = cortes) }
        recalcularTotales()
    }

    // ── Ocurrencias ───────────────────────────────────────────────────────────

    fun setOcurrenciaMotivo(motivo: String) =
        _state.update { it.copy(newOcurrenciaMotivo = motivo) }

    fun setOcurrenciaCargo(cargo: String) =
        _state.update { it.copy(newOcurrenciaCargo = cargo) }

    fun agregarOcurrencia() {
        val s = _state.value
        if (s.newOcurrenciaMotivo.length < 10) {
            _state.update { it.copy(error = "El motivo debe tener al menos 10 caracteres") }
            return
        }
        val nueva = OcurrenciaItem(
            id                = s.ocurrencias.size + 1,
            motivo            = s.newOcurrenciaMotivo.trim(),
            falta             = null,
            cargo             = s.newOcurrenciaCargo,
            cargoEsConductor  = s.newOcurrenciaCargo == "Conductor"
        )
        _state.update {
            it.copy(
                ocurrencias         = it.ocurrencias + nueva,
                newOcurrenciaMotivo = "",
                newOcurrenciaCargo  = "Conductor"
            )
        }
    }

    fun eliminarOcurrencia(id: Int) =
        _state.update { it.copy(ocurrencias = it.ocurrencias.filter { o -> o.id != id }) }

    // ── Cancel ────────────────────────────────────────────────────────────────

    fun showCancelDialog(show: Boolean) = _state.update { it.copy(showCancelDialog = show) }
    fun setCancelMotivo(m: String) = _state.update { it.copy(cancelMotivo = m) }

    fun cancelar() {
        val insp   = _state.value.inspeccion ?: return
        val motivo = _state.value.cancelMotivo.trim()
        if (motivo.isBlank()) { _state.update { it.copy(error = "Indica el motivo de cancelación") }; return }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showCancelDialog = false) }
            val token = storage.getString(StorageKeys.AUTH_TOKEN)
            val code  = storage.getString(StorageKeys.EMPRESA_CODE)
            when (inspeccionApi.cancelarInspeccion(code, token, insp.id, motivo)) {
                is ApiResult.Success -> _events.value = InspeccionEvent.Cancelled
                is ApiResult.Error   -> _state.update { it.copy(isLoading = false, error = "Error al cancelar") }
                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    // ── Finalize ──────────────────────────────────────────────────────────────

    fun showFinalizeDialog(show: Boolean) = _state.update { it.copy(showFinalizeDialog = show) }

    fun finalizar() {
        val s    = _state.value
        val insp = s.inspeccion ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, showFinalizeDialog = false) }
            val token = storage.getString(StorageKeys.AUTH_TOKEN)
            val code  = storage.getString(StorageKeys.EMPRESA_CODE)

            val loc = locationManager.getCurrentLocation()
            val bajadaPos = if (loc != null) PosDto(loc.latitude, loc.longitude) else null

            val cortesDto = s.cortes.map { c ->
                InspeccionApiService.CorteFinDto(
                    boleto          = c.boletoId,
                    numero          = c.numero,
                    reintegros      = c.reintegros,
                    pasajeros_vivos = c.pasajerosVivos
                )
            }
            val ocurrenciasDto = s.ocurrencias.map { o ->
                InspeccionApiService.OcurrenciaFinDto(
                    motivo   = o.motivo,
                    falta    = null,
                    cargo    = o.cargoEsConductor,
                    imagenes = o.imagenes
                )
            }

            when (inspeccionApi.finalizarInspeccion(
                code, token, insp.id,
                cortesDto,
                s.totalReintegros, s.totalReintegrosMonto,
                s.totalPasajeros, s.totalPasajerosMonto,
                null, bajadaPos,
                ocurrenciasDto
            )) {
                is ApiResult.Success -> _events.value = InspeccionEvent.Finalized
                is ApiResult.Error   -> _state.update { it.copy(isLoading = false, error = "Error al finalizar") }
                else -> _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun clearCercaniaMessage() = _state.update { it.copy(cercaniaMessage = null) }

    fun clearError() = _state.update { it.copy(error = null) }
    fun consumeEvent() { _events.value = null }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun recalcularTotales() {
        val cortes = _state.value.cortes
        val totalRei     = cortes.sumOf { it.reintegros }
        val montoRei     = cortes.sumOf { it.reintegros * it.tarifa }
        val totalPas     = cortes.sumOf { it.pasajerosVivos }
        val montoPas     = cortes.sumOf { it.pasajerosVivos * it.tarifa }
        _state.update {
            it.copy(
                totalReintegros      = totalRei,
                totalReintegrosMonto = montoRei,
                totalPasajeros       = totalPas,
                totalPasajerosMonto  = montoPas
            )
        }
    }

    /**
     * Build the corte list from suministros.
     *
     * Rules (mirrors Angular's inspeccion component):
     *  - `mostrar` is already set from `s.estado == "En Uso"` during the mapping step;
     *    we do NOT override it here.
     *  - `quedan` = true when the total count of supplies for that boleto type > 1.
     *    (All copies of a multi-supply boleto show the "Terminar" button.)
     *  - Restore previously saved corte numbers from the DTO (persisted on server).
     */
    private fun buildCortes(
        fromSuministros: List<CorteItem>,
        dto: InspeccionDto
    ): List<CorteItem> {
        val saved          = dto.cortes.associateBy { it.boleto }
        // How many suministros exist per boleto type?
        val countPerBoleto = fromSuministros.groupingBy { it.boletoId }.eachCount()
        return fromSuministros.map { item ->
            val savedNum = saved[item.boletoId]?.numero ?: item.numero
            item.copy(
                numero = savedNum,
                // mostrar stays as-is (already from s.estado in the mapping step)
                quedan = (countPerBoleto[item.boletoId] ?: 1) > 1
            )
        }
    }

    private fun InspeccionDto.toDomain() = Inspeccion(
        id               = id,
        estado           = estado,
        padron           = padron,
        placa            = placa,
        unidadId         = unidad.id,
        salidaId         = salida.id,
        subida           = subida?.nombre,
        subidaId         = subida?.id,
        bajada           = bajada?.nombre,
        bajadaId         = bajada?.id,
        inicio           = inicio,
        fin              = fin,
        cortes           = emptyList(),
        reintegros       = reintegros,
        reintegrosMonto  = reintegrosMonto,
        pasajerosVivos   = pasajerosVivos,
        pasajerosMonto   = pasajerosMonto,
        ticketera        = ticketera,
        tieneOcurrencias = tieneOcurrencias,
        subidaPos        = subidaPos?.let { GeoPos(it.lat, it.lng) },
        bajadaPos        = bajadaPos?.let { GeoPos(it.lat, it.lng) },
        conductorNombre  = salida.conductor?.nombre,
        cobradorNombre   = salida.cobrador?.nombre
    )
}
