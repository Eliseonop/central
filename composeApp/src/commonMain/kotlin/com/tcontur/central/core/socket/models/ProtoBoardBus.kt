package com.tcontur.central.core.socket.models

/**
 * Server response to the "board_bus" frame.
 *
 * Schema (server side):
 *   success → bool — whether boarding was accepted
 *
 * Usage:
 *   val board = ProtoBoardBus.fromMap(data)
 */
data class ProtoBoardBus(
    /** True if the server accepted the boarding event. */
    val success: Boolean?
) {
    companion object {
        fun fromMap(map: Map<String, Any>): ProtoBoardBus = ProtoBoardBus(
            success = map["success"] as? Boolean
        )
    }
}
