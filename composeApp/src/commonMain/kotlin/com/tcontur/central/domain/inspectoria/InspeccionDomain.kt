package com.tcontur.central.domain.inspectoria

// ─── Domain entities ──────────────────────────────────────────────────────────

data class Inspeccion(
    val id: Int,
    val estado: Boolean,
    val padron: Int?,
    val placa: String?,
    val unidadId: Int,
    val salidaId: Int,
    val subida: String?,
    val subidaId: Int?,
    val bajada: String?,
    val bajadaId: Int?,
    val inicio: String?,
    val fin: String?,
    val cortes: List<CorteItem>,
    val reintegros: Int,
    val reintegrosMonto: Double,
    val pasajerosVivos: Int,
    val pasajerosMonto: Double,
    val ticketera: Boolean,
    val tieneOcurrencias: Boolean,
    val subidaPos: GeoPos?,
    val bajadaPos: GeoPos?,
    val conductorNombre: String?,
    val cobradorNombre: String?
)

data class CorteItem(
    val boletoId: Int,
    val nombre: String,
    val serie: String,
    val tarifa: Double,
    val color: String,
    val inicio: Int,
    val fin: Int,
    val numero: Int = fin,
    val reintegros: Int = 0,
    val pasajerosVivos: Int = 0,
    val mostrar: Boolean = true,
    val terminado: Boolean = false,
    /** true when another supply (suministro) for the same boleto type comes after this one */
    val quedan: Boolean = false
)

data class GeoPos(val lat: Double, val lng: Double)

data class ResumenInspector(
    val nombre: String,
    val inspectorId: Int,
    val inspecciones: Int,
    val totalReintegrosMonto: Double,
    val totalPasajerosMonto: Double,
    val horaInicio: String?
)

data class OcurrenciaItem(
    val id: Int,
    val motivo: String,
    val falta: String?,
    val cargo: String,            // "Conductor" | "Cobrador"
    val cargoEsConductor: Boolean,
    val imagenes: List<String> = emptyList()
)

data class UnidadOption(
    val id: Int,
    val padron: Int?,
    val placa: String?
) {
    val displayText: String get() = "PAD ${padron ?: "?"} – ${placa ?: "?"}"
}

data class InspDraft(
    val id: Int,
    val cortes: List<CorteDraftItem>,
    val ocurrencias: List<OcurrenciaItem>
)

data class CorteDraftItem(
    val boletoId: Int,
    val numero: Int,
    val reintegros: Int,
    val pasajerosVivos: Int,
    val terminado: Boolean
)
