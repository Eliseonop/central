package com.tcontur.central.core.socket.models

/**
 * Server response to the "alight_from_bus" frame.
 *
 * Schema (server side):
 *   success → bool — whether alighting was accepted
 *
 * Usage:
 *   val alight = ProtoAlightFromBus.fromMap(data)
 */
data class ProtoAlightFromBus(
    /** True if the server accepted the alight event. */
    val success: Boolean?
) {
    companion object {
        fun fromMap(map: Map<String, Any>): ProtoAlightFromBus = ProtoAlightFromBus(
            success = map["success"] as? Boolean
        )
    }
}
