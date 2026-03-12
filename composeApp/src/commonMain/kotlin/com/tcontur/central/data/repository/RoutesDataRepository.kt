package com.tcontur.central.data.repository


import com.tcontur.central.core.network.ApiResult
import com.tcontur.central.core.network.map
import com.tcontur.central.data.InspeccionApiService
import com.tcontur.central.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RoutesDataRepository(private val api: InspeccionApiService) {

    private val _boletos          = MutableStateFlow<List<BoletoDto>>(emptyList())
    private val _configuraciones  = MutableStateFlow<List<ConfiguracionDto>>(emptyList())
    private val _configuracionesRuta = MutableStateFlow<List<ConfiguracionRutaDto>>(emptyList())
    private val _rutas            = MutableStateFlow<List<RutaDto>>(emptyList())
    private val _geocercas        = MutableStateFlow<List<GeocercaDto>>(emptyList())
    private val _paraderos        = MutableStateFlow<List<ParaderoDto>>(emptyList())
    private val _recorridos       = MutableStateFlow<List<RecorridoDto>>(emptyList())
    private val _tarifas          = MutableStateFlow<List<TarifaDto>>(emptyList())
    private val _tiposEvento      = MutableStateFlow<List<TipoEventoDto>>(emptyList())
    private val _usuarios         = MutableStateFlow<List<UsuarioDto>>(emptyList())

    val boletos:             StateFlow<List<BoletoDto>>           = _boletos.asStateFlow()
    val configuraciones:     StateFlow<List<ConfiguracionDto>>    = _configuraciones.asStateFlow()
    val configuracionesRuta: StateFlow<List<ConfiguracionRutaDto>> = _configuracionesRuta.asStateFlow()
    val rutas:               StateFlow<List<RutaDto>>             = _rutas.asStateFlow()
    val geocercas:           StateFlow<List<GeocercaDto>>         = _geocercas.asStateFlow()
    val paraderos:           StateFlow<List<ParaderoDto>>         = _paraderos.asStateFlow()
    val recorridos:          StateFlow<List<RecorridoDto>>        = _recorridos.asStateFlow()
    val tarifas:             StateFlow<List<TarifaDto>>           = _tarifas.asStateFlow()
    val tiposEvento:         StateFlow<List<TipoEventoDto>>       = _tiposEvento.asStateFlow()
    val usuarios:            StateFlow<List<UsuarioDto>>          = _usuarios.asStateFlow()

    suspend fun load(empresaCodigo: String, token: String): ApiResult<Unit> {
        val result = api.getRoutesData(empresaCodigo, token)
        if (result is ApiResult.Success) populate(result.data)
        return result.map { }
    }

    private fun populate(data: RoutesDataDto) {
        _boletos.value           = data.boletos
        _configuraciones.value   = data.configuraciones
        _configuracionesRuta.value = data.configuraciones_ruta
        _rutas.value             = data.rutas
        _geocercas.value         = data.geocercas
        _paraderos.value         = data.paraderos
        _recorridos.value        = data.recorridos
        _tarifas.value           = data.tarifas
        _tiposEvento.value       = data.tipos_evento
        _usuarios.value          = data.usuarios
    }

    fun clear() {
        _boletos.value           = emptyList()
        _configuraciones.value   = emptyList()
        _configuracionesRuta.value = emptyList()
        _rutas.value             = emptyList()
        _geocercas.value         = emptyList()
        _paraderos.value         = emptyList()
        _recorridos.value        = emptyList()
        _tarifas.value           = emptyList()
        _tiposEvento.value       = emptyList()
        _usuarios.value          = emptyList()
    }
}

