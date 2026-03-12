package com.tcontur.central.core.socket.models

/**
 * Server response to the "login" frame.
 *
 * Schema (server side):
 *   id  → id (2 bytes)  — confirmed inspector ID
 *
 * Usage:
 *   val login = ProtoLogin.fromMap(data)
 *   socketSessionRepository.onLoginReceived(login)
 */
data class ProtoLogin(
    /** Inspector ID confirmed by the server. */
    val id: Int?
) {
    companion object {
        fun fromMap(map: Map<String, Any>): ProtoLogin = ProtoLogin(
            id = (map["id"] as? Number)?.toInt()
        )
    }
}
