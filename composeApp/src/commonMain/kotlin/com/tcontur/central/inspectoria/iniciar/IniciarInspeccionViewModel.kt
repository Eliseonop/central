package com.tcontur.central.inspectoria.iniciar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.core.QrDataHolder
import com.tcontur.central.core.location.LocationManager
import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.data.AuthRepositoryImpl
import com.tcontur.central.data.InspeccionApiService
import com.tcontur.central.data.model.PosDto
import com.tcontur.central.domain.inspectoria.UnidadOption
import com.tcontur.central.inspectoria.iniciar.qr.QrData
import com.tcontur.central.inspectoria.iniciar.qr.QrError
import com.tcontur.central.inspectoria.iniciar.qr.QrParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ProximityStatus { IDLE, CHECKING, VALID, INVALID }

data class IniciarInspeccionState(
    val selectedTab: Int = 0,
    // ── Formulario tab ────────────────────────────────────────────────────────
    val unidades: List<UnidadOption> = emptyList(),
    val selectedUnidad: UnidadOption? = null,
    val ticketera: Boolean = false,
    val subidaId: Int? = null,
    val subidaNombre: String? = null,
    val subidaLat: Double? = null,
    val subidaLng: Double? = null,
    val proximityStatus: ProximityStatus = ProximityStatus.IDLE,
    val isLoadingUnidades: Boolean = false,
    // ── QR tab ────────────────────────────────────────────────────────────────
    val qrData: QrData? = null,     // non-null = scan succeeded
    val qrScanKey: Int = 0,         // increment to remount scanner
    // ── Shared ────────────────────────────────────────────────────────────────
    val isCreating: Boolean = false,
    val error: String? = null
)

sealed class IniciarEvent {
    data class InspeccionCreada(val id: Int) : IniciarEvent()
    data class Error(val message: String)    : IniciarEvent()
}

class IniciarInspeccionViewModel(
    private val auth:           AuthRepositoryImpl,
    private val storage:        AppStorage,
    private val inspeccionApi:  InspeccionApiService,
    private val locationManager: LocationManager,
    private val qrDataHolder:   QrDataHolder
) : ViewModel() {

    private val _state  = MutableStateFlow(IniciarInspeccionState())
    val state: StateFlow<IniciarInspeccionState> = _state

    private val _events = MutableStateFlow<IniciarEvent?>(null)
    val events: StateFlow<IniciarEvent?> = _events

    init { loadUnidades() }

    fun selectTab(index: Int) = _state.update { it.copy(selectedTab = index) }

    // ── Formulario ────────────────────────────────────────────────────────────

    fun selectUnidad(option: UnidadOption) {
        _state.update { it.copy(selectedUnidad = option, proximityStatus = ProximityStatus.IDLE) }
        validateProximity(option)
    }

    fun setTicketera(value: Boolean) = _state.update { it.copy(ticketera = value) }

    fun clearError() = _state.update { it.copy(error = null) }

    // ── QR tab ────────────────────────────────────────────────────────────────

    /** Called by the UI when the camera layer decodes a QR string. */
    fun onQrScanned(raw: String) {
        val data = try {
            QrParser.parse(raw)
        } catch (e: QrError) {
            _state.update { it.copy(error = e.message) }
            return
        } catch (_: Exception) {
            _state.update { it.copy(error = "QR con formato inválido") }
            return
        }
        // Success → store parsed data; ticketera is always true for QR flow
        _state.update { it.copy(qrData = data) }
    }

    /** Reset the scanner to allow re-scanning. */
    fun resetQrScan() = _state.update { it.copy(qrData = null, qrScanKey = it.qrScanKey + 1) }

    // ── Crear ─────────────────────────────────────────────────────────────────

    fun crear() {
        val s = _state.value
        viewModelScope.launch {
            _state.update { it.copy(isCreating = true, error = null) }
            val token = storage.getString(StorageKeys.AUTH_TOKEN)
            val code  = storage.getString(StorageKeys.EMPRESA_CODE)

            // ── QR path ───────────────────────────────────────────────────────
            val qr = s.qrData
            if (qr != null) {
                // Store cortes so InspeccionViewModel can pick them up
                qrDataHolder.qrCortes = qr.cortes

                when (val r = inspeccionApi.iniciarInspeccion(
                    code, token,
                    unidadId  = qr.unidad,
                    subidaId  = qr.subida,
                    subidaPos = PosDto(qr.subidaLat, qr.subidaLng),
                    ticketera = true
                )) {
                    is ApiResult.Success -> {
                        _state.update { it.copy(isCreating = false) }
                        _events.value = IniciarEvent.InspeccionCreada(r.data.id)
                    }
                    is ApiResult.Error -> {
                        qrDataHolder.clear()
                        _state.update { it.copy(isCreating = false, error = r.message) }
                        _events.value = IniciarEvent.Error(r.message)
                    }
                    else -> _state.update { it.copy(isCreating = false) }
                }
                return@launch
            }

            // ── Manual (Formulario) path ──────────────────────────────────────
            val unidad = s.selectedUnidad ?: run {
                _state.update { it.copy(isCreating = false, error = "Selecciona una unidad") }
                return@launch
            }

            val pos = if (s.subidaLat != null && s.subidaLng != null)
                PosDto(s.subidaLat, s.subidaLng) else null

            when (val r = inspeccionApi.iniciarInspeccion(
                code, token,
                unidadId  = unidad.id,
                subidaId  = s.subidaId,
                subidaPos = pos,
                ticketera = s.ticketera
            )) {
                is ApiResult.Success -> {
                    _state.update { it.copy(isCreating = false) }
                    _events.value = IniciarEvent.InspeccionCreada(r.data.id)
                }
                is ApiResult.Error -> {
                    _state.update { it.copy(isCreating = false, error = r.message) }
                    _events.value = IniciarEvent.Error(r.message)
                }
                else -> _state.update { it.copy(isCreating = false) }
            }
        }
    }

    fun consumeEvent() { _events.value = null }

    // ── Private ───────────────────────────────────────────────────────────────

    private fun loadUnidades() {
        viewModelScope.launch {
            _state.update { it.copy(isLoadingUnidades = true) }
            val token = storage.getString(StorageKeys.AUTH_TOKEN)
            val code  = storage.getString(StorageKeys.EMPRESA_CODE)
            when (val r = inspeccionApi.getUnidades(code, token)) {
                is ApiResult.Success -> {
                    val opts = r.data.map { UnidadOption(it.id, it.padron?.padron, it.placa) }
                    _state.update { it.copy(unidades = opts, isLoadingUnidades = false) }
                }
                is ApiResult.Error -> _state.update {
                    it.copy(isLoadingUnidades = false, error = r.message)
                }
                else -> _state.update { it.copy(isLoadingUnidades = false) }
            }
        }
    }

    private fun validateProximity(unidad: UnidadOption) {
        viewModelScope.launch {
            _state.update { it.copy(proximityStatus = ProximityStatus.CHECKING) }
            val loc = locationManager.getCurrentLocation()
            if (loc == null) {
                _state.update { it.copy(proximityStatus = ProximityStatus.INVALID, error = "No se pudo obtener la ubicación") }
                return@launch
            }
            val token = storage.getString(StorageKeys.AUTH_TOKEN)
            val code  = storage.getString(StorageKeys.EMPRESA_CODE)
            when (val r = inspeccionApi.validarCercania(code, token, unidad.id, loc.latitude, loc.longitude)) {
                is ApiResult.Success -> {
                    if (r.data.validation) {
                        _state.update {
                            it.copy(
                                proximityStatus = ProximityStatus.VALID,
                                subidaId        = r.data.paradero,
                                subidaNombre    = null,
                                subidaLat       = loc.latitude,
                                subidaLng       = loc.longitude
                            )
                        }
                    } else {
                        _state.update { it.copy(proximityStatus = ProximityStatus.INVALID) }
                    }
                }
                is ApiResult.Error -> _state.update {
                    it.copy(proximityStatus = ProximityStatus.INVALID, error = r.message)
                }
                else -> Unit
            }
        }
    }
}
