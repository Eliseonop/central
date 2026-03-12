package com.tcontur.central.core.socket.models

/**
 * Server response to the "request_qr" frame.
 *
 * Schema (server side):
 *   expires_at → datetime — timestamp when the QR becomes invalid
 *
 * Usage:
 *   val qr = ProtoRequestQr.fromMap(data)
 */
data class ProtoRequestQr(
    /** ISO datetime string — when the QR code expires. */
    val expiresAt: String?
) {
    companion object {
        fun fromMap(map: Map<String, Any>): ProtoRequestQr = ProtoRequestQr(
            expiresAt = map["expires_at"]?.toString()
        )
    }
}
