package com.tcontur.central.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InspeccionDto(
    @SerialName("id")                val id: Int,
    @SerialName("estado")            val estado: Boolean,
    @SerialName("inspector")         val inspector: InspectorDto,
    @SerialName("subida")            val subida: BasicoDto? = null,
    @SerialName("bajada")            val bajada: BasicoDto? = null,
    @SerialName("inicio")            val inicio: String? = null,
    @SerialName("fin")               val fin: String? = null,
    @SerialName("padron")            val padron: Int? = null,
    @SerialName("placa")             val placa: String? = null,
    @SerialName("ruta")              val ruta: Int? = null,
    @SerialName("salida")            val salida: SalidaDto,
    @SerialName("unidad")            val unidad: UnidadDto,
    @SerialName("cortes")            val cortes: List<CorteDto> = emptyList(),
    @SerialName("reintegros")        val reintegros: Int = 0,
    @SerialName("reintegros_monto")  val reintegrosMonto: Double = 0.0,
    @SerialName("pasajeros_vivos")   val pasajerosVivos: Int = 0,
    @SerialName("pasajeros_monto")   val pasajerosMonto: Double = 0.0,
    @SerialName("ticketera")         val ticketera: Boolean = false,
    @SerialName("tiene_ocurrencias") val tieneOcurrencias: Boolean = false,
    @SerialName("subida_pos")        val subidaPos: PosDto? = null,
    @SerialName("bajada_pos")        val bajadaPos: PosDto? = null
)

@Serializable
data class InspectorDto(
    @SerialName("id")     val id: Int,
    @SerialName("nombre") val nombre: String,
    @SerialName("codigo") val codigo: Int? = null
)

@Serializable
data class BasicoDto(
    @SerialName("id")     val id: Int,
    @SerialName("nombre") val nombre: String
)

@Serializable
data class SalidaDto(
    @SerialName("id")        val id: Int,
    @SerialName("conductor") val conductor: PersonaDto? = null,
    @SerialName("cobrador")  val cobrador: PersonaDto? = null
)

@Serializable
data class PersonaDto(
    @SerialName("id")     val id: Int,
    @SerialName("nombre") val nombre: String
)

/**
 * The API returns `padron` as either null or a nested object like
 * {"id":1,"padron":1,"paquete":null,...}. We only need `padron` (the number).
 */
@Serializable
data class PadronInfoDto(
    @SerialName("id")     val id: Int = 0,
    @SerialName("padron") val padron: Int? = null
)

@Serializable
data class UnidadDto(
    @SerialName("id")     val id: Int,
    @SerialName("padron") val padron: Int? = null,
    @SerialName("placa")  val placa: String? = null
)

@Serializable
data class CorteDto(
    @SerialName("boleto") val boleto: Int,
    @SerialName("numero") val numero: Int
)

@Serializable
data class PosDto(
    @SerialName("lat") val lat: Double,
    @SerialName("lng") val lng: Double
)

@Serializable
data class SuministroDto(
    @SerialName("id")     val id: Int,
    @SerialName("boleto") val boleto: BoletoDto,
    @SerialName("inicio") val inicio: Int,               // physical range start
    @SerialName("actual") val actual: Int,               // current ticket position (inspector starts here)
    @SerialName("fin")    val fin: Int,                  // range end
    @SerialName("estado") val estado: String = "En Uso", // "En Uso" → visible to inspector
    @SerialName("serie")  val serie: String = ""         // batch series (on suministro, not boleto)
)

@Serializable
data class BoletoDto(
    @SerialName("id")     val id: Int,
    @SerialName("nombre") val nombre: String,
    // `serie` lives on the parent SuministroDto when coming from /api/suministros,
    // so it may be absent inside the boleto object → default to empty string.
    @SerialName("serie")  val serie: String = "",
    @SerialName("tarifa") val tarifa: Double,
    @SerialName("color")  val color: String = "blue"
)

@Serializable
data class UnidadSelectDto(
    @SerialName("id")     val id: Int,
    @SerialName("padron") val padron: PadronInfoDto? = null,
    @SerialName("placa")  val placa: String? = null
)

/**
 * POST /api/unidades/validar_cercania
 * Body: { posicion: {lat, lng}, unidad: Int }
 */
@Serializable
data class ValidarCercaniaResponse(
    @SerialName("validation") val validation: Boolean,
    @SerialName("paradero")   val paradero: Int
)

@Serializable
data class ResumenInspectorDto(
    @SerialName("nombre")                 val nombre: String,
    @SerialName("inspector_id")           val inspectorId: Int,
    @SerialName("inspecciones")           val inspecciones: Int,
    @SerialName("total_reintegros_monto") val totalReintegrosMonto: Double = 0.0,
    @SerialName("total_pasajeros_monto")  val totalPasajerosMonto: Double = 0.0,
    @SerialName("hora_inicio")            val horaInicio: String? = null
)
