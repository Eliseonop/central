package com.tcontur.central.inspectoria.iniciar.qr

// ── Parsed QR payload ─────────────────────────────────────────────────────────
//
// El QR del conductor contiene el payload en formato query string:
//   inspector=<id>&pin=<id>&vehicle=<id>
//
// Este payload se usa directamente para enviar check_qr por socket.
// Los datos de viaje (trip, busstop, tickets) llegan en la respuesta ProtoCheckQr.

data class QrData(
    val inspector: Int,
    val pin:       Int,
    val vehicle:   Int
)

// ── Errors ────────────────────────────────────────────────────────────────────

sealed class QrError(message: String) : Exception(message) {
    object Empty         : QrError("QR vacío")
    object InvalidFormat : QrError("QR con formato inválido")
}
