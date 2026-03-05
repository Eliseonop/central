package com.tcontur.central.inspectoria.iniciar.qr

// ── Parsed QR payload ─────────────────────────────────────────────────────────

data class QrData(
    val version: Int,
    val unidad: Int,
    val subida: Int,
    val subidaLat: Double,
    val subidaLng: Double,
    val salida: Int,
    val cortes: List<QrCorte>
)

data class QrCorte(
    val boletoId: Int,
    val inicio: Int,
    val fin: Int
)

// ── Errors ────────────────────────────────────────────────────────────────────

sealed class QrError(message: String) : Exception(message) {
    object Empty              : QrError("QR vacío")
    object InvalidFormat      : QrError("QR con formato inválido")
    object UnsupportedVersion : QrError("Versión de QR no soportada")
    object Expired            : QrError("El QR ha expirado. Debe ser escaneado dentro de 5 minutos.")
    object FutureDate         : QrError("La fecha del QR es inválida. Verifica la hora del dispositivo.")
}
