package com.tcontur.central.inspectoria.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.core.socket.ProtoSocketManager
import com.tcontur.central.core.socket.SocketServiceManager
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.data.AuthRepositoryImpl
import com.tcontur.central.data.InspeccionApiService
import com.tcontur.central.data.model.InspeccionDto
import com.tcontur.central.domain.User
import com.tcontur.central.domain.inspectoria.Inspeccion
import com.tcontur.central.domain.inspectoria.GeoPos
import com.tcontur.central.domain.inspectoria.CorteItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.tcontur.central.core.utils.currentDateStr
import kotlinx.coroutines.launch

data class InspectoriaDashboardState(
    val user: User? = null,
    val inspeccionesHoy: List<Inspeccion> = emptyList(),
    val inspPendiente: Inspeccion? = null,
    val totalReintegrosMonto: Double = 0.0,
    val totalPasajerosMonto: Double = 0.0,
    val ultimaBajada: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class InspectoriaDashboardEvent {
    data object LoggedOut : InspectoriaDashboardEvent()
    data class NavigateToInspeccion(val id: Int) : InspectoriaDashboardEvent()
    data object NavigateToIniciar : InspectoriaDashboardEvent()
}

class InspectoriaDashboardViewModel(
    private val auth: AuthRepositoryImpl,
    private val storage: AppStorage,
    private val inspeccionApi: InspeccionApiService,
    private val protoSocketManager: ProtoSocketManager,
    private val socketServiceManager: SocketServiceManager
) : ViewModel() {

    private val _state = MutableStateFlow(InspectoriaDashboardState())
    val state: StateFlow<InspectoriaDashboardState> = _state

    private val _events = MutableStateFlow<InspectoriaDashboardEvent?>(null)
    val events: StateFlow<InspectoriaDashboardEvent?> = _events

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            val user = auth.getStoredUser() ?: return@launch
            _state.update { it.copy(user = user) }
            loadDashboard(user)
        }
    }

    fun sendRquestQr() {
        socketServiceManager.send(
            data = hashMapOf(
                "vehicle" to 61,
            ),
            formatKey = "request_qr"
        )
    }

    fun refresh() {
        val user = _state.value.user ?: return
        viewModelScope.launch { loadDashboard(user) }
    }

    private suspend fun loadDashboard(user: User) {
        _state.update { it.copy(isLoading = true, error = null) }
        val token = storage.getString(StorageKeys.AUTH_TOKEN)
        val code  = storage.getString(StorageKeys.EMPRESA_CODE)

        inspeccionApi.verInspeccion(code, token).let { result ->
            if (result is ApiResult.Success && result.data != null) {
                inspeccionApi.getInspeccionById(code, token, result.data.id).let { full ->
                    if (full is ApiResult.Success) {
                        _state.update { it.copy(inspPendiente = full.data.toDomain()) }
                    }
                }
            }
        }

        inspeccionApi.getInspecciones(code, token, user.id, currentDateStr()).let { result ->
            if (result is ApiResult.Success) {
                val list = result.data.map { it.toDomain() }
                _state.update {
                    it.copy(
                        inspeccionesHoy      = list,
                        totalReintegrosMonto = list.sumOf { it.reintegrosMonto },
                        totalPasajerosMonto  = list.sumOf { it.pasajerosMonto },
                        ultimaBajada         = list.filter { it.bajada != null }
                            .maxByOrNull { it.fin ?: "" }
                            ?.let { "${it.bajada} – ${formatTime(it.fin)}" },
                        isLoading            = false
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }
    fun onInspeccionarClick() {
        val pending = _state.value.inspPendiente
        if (pending != null) {
            _events.value = InspectoriaDashboardEvent.NavigateToInspeccion(pending.id)
        } else {
            _events.value = InspectoriaDashboardEvent.NavigateToIniciar
        }
    }

    fun logout() {
        viewModelScope.launch {
            socketServiceManager.stopLocationTracking()
            socketServiceManager.disconnect()
            auth.logout()
            _events.value = InspectoriaDashboardEvent.LoggedOut
        }
    }

    fun consumeEvent() { _events.value = null }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun formatTime(isoString: String?): String {
        if (isoString == null) return ""
        return runCatching {
            // "2024-01-01T08:30:00" → "08:30"
            isoString.substringAfter("T").take(5)
        }.getOrDefault("")
    }

    private fun InspeccionDto.toDomain() = Inspeccion(
        id                = id,
        estado            = estado,
        padron            = padron,
        placa             = placa,
        unidadId          = unidad.id,
        salidaId          = salida.id,
        subida            = subida?.nombre,
        subidaId          = subida?.id,
        bajada            = bajada?.nombre,
        bajadaId          = bajada?.id,
        inicio            = inicio,
        fin               = fin,
        cortes            = cortes.map { c ->
            CorteItem(
                boletoId = c.boleto,
                nombre   = "",
                serie    = "",
                tarifa   = 0.0,
                color    = "blue",
                inicio   = 0,
                fin      = c.numero,
                numero   = c.numero
            )
        },
        reintegros        = reintegros,
        reintegrosMonto   = reintegrosMonto,
        pasajerosVivos    = pasajerosVivos,
        pasajerosMonto    = pasajerosMonto,
        ticketera         = ticketera,
        tieneOcurrencias  = tieneOcurrencias,
        subidaPos         = subidaPos?.let { GeoPos(it.lat, it.lng) },
        bajadaPos         = bajadaPos?.let { GeoPos(it.lat, it.lng) },
        conductorNombre   = salida.conductor?.nombre,
        cobradorNombre    = salida.cobrador?.nombre
    )
}
