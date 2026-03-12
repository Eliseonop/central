package com.tcontur.central.core.socket.models

/**
 * Server response to the "error" frame (or any server-originated error).
 *
 * Schema (server side):
 *   error → string — human-readable error message
 *
 * Usage:
 *   val err = ProtoError.fromMap(data)
 */
data class ProtoError(
    val error: String?
) {
    companion object {
        fun fromMap(map: Map<String, Any>): ProtoError = ProtoError(
            error = map["error"] as? String
        )
    }
}
