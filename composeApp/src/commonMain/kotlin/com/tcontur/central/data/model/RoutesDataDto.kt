package com.tcontur.central.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiaDto(val id: Int, val nombre: String, val codigo: String)

@Serializable
data class RutaRefDto(val id: Int, val codigo: String, val plantilla_actual: Int)

@Serializable
data class StockDto(val id: Int, val serie: String, val actual: Int, val fin: Int)

@Serializable
data class BoletoDto(
    val id: Int,
    val activo: Boolean,
    val alerta: Int,
    val color: String,
    val desde: String,
    val dias: List<DiaDto>,
    val hasta: String?,
    val minimo: Int,
    val nombre: String,
    val opcional: Int,
    val orden: Int,
    val reintegro: Boolean,
    val ruta: RutaRefDto,
    val segunda: Boolean,
    val stock: StockDto?,
    val tacos: Int,
    val tarifa: Double,
    val timestamp: String
)

@Serializable
data class ConfiguracionDto(
    val id: Int,
    val nombre: String,
    val data: String,
    val json: String,
    val descripcion: String,
    val orden: Int,
    val timestamp: String,
    val tipo: String
)

@Serializable
data class ConfiguracionRutaDto(
    val id: Int,
    val orden: Int,
    val ruta: Int,
    val grupo: String,
    val nombre: String,
    val descripcion: String,
    val data: kotlinx.serialization.json.JsonElement,
    val tipo: String,
    val timestamp: String,
    val json: kotlinx.serialization.json.JsonElement
)

@Serializable
data class PlantillaRefDto(val id: Int, val nombre: String)
@Serializable
data class TipoRutaDto(val id: Int, val orden: Int, val nombre: String)
@Serializable
data class RutaPadreDto(val id: Int, val activo: Boolean, val codigo: String)

@Serializable
data class RutaDto(
    val id: Int,
    val activo: Boolean,
    val codigo: String,
    val fin: String,
    val inicio: String,
    val padre_a: RutaPadreDto?,
    val padre_b: RutaPadreDto?,
    val timestamp: String,
    val tipo: TipoRutaDto,
    val ualabee: String?,
    val plantilla: PlantillaRefDto
)

@Serializable
data class GeocercaDto(
    val id: Int,
    val activo: Boolean,
    val adelanto: Int,
    val audio: String,
    val control: Boolean,
    val datear: Boolean,
    val desde: String,
    val frecuencia: Int?,
    val hasta: String?,
    val lado: Boolean,
    val latitud: Double,
    val liquidar: Boolean,
    val longitud: Double,
    val metros: Int,
    val nombre: String,
    val orden: Int,
    val pantalla: Boolean,
    val radio: Double,
    val refrecuenciar: Boolean,
    val retorno: Boolean,
    val ruta: RutaRefDto,
    val sagrado: Boolean,
    val super_geocerca: Int?,
    val terminal: Boolean?,
    val timestamp: String
)

@Serializable
data class ParaderoDto(
    val id: Int,
    val activo: Boolean,
    val audio: String,
    val lado: Boolean,
    val latitud: Double,
    val liquidar: Boolean,
    val longitud: Double,
    val nombre: String,
    val orden: Int,
    val radio: Double,
    val ruta: RutaRefDto,
    val terminal: Boolean,
    val velocidad: Int,
    val timestamp: String,
    val metros: Int
)

@Serializable
data class PuntoRecorridoDto(val orden: Int, val latitud: Double, val longitud: Double)

@Serializable
data class RecorridoDto(
    val id: Int,
    val ruta: RutaRefDto,
    val lado: Boolean,
    val trayecto: List<PuntoRecorridoDto>
)

@Serializable
data class ParaderoRefDto(val id: Int, val nombre: String)
@Serializable
data class BoletoRefDto(val id: Int, val nombre: String, val tarifa: Int, val color: String)

@Serializable
data class TarifaDto(
    val id: Int,
    val inicio: ParaderoRefDto,
    val fin: ParaderoRefDto,
    val boleto: BoletoRefDto
)

@Serializable
data class TipoEventoDto(
    val id: Int,
    val automatico: Boolean,
    val color: String,
    val conductor: Boolean,
    val manual: Boolean,
    val nombre: String,
    val orden: Int
)

@Serializable
data class UsuarioRutaDto(val ruta: Int)

@Serializable
data class UsuarioDto(
    val id: Int,
    val celular: String,
    val nombre: String,
    val rutas: List<UsuarioRutaDto>,
    val timestamp: String
)

@Serializable
data class RoutesDataDto(
    val boletos: List<BoletoDto>,
    val configuraciones: List<ConfiguracionDto>,
    val configuraciones_ruta: List<ConfiguracionRutaDto>,
    val rutas: List<RutaDto>,
    val geocercas: List<GeocercaDto>,
    val paraderos: List<ParaderoDto>,
    val recorridos: List<RecorridoDto>,
    val tarifas: List<TarifaDto>,
    val tipos_evento: List<TipoEventoDto>,
    val usuarios: List<UsuarioDto>
)