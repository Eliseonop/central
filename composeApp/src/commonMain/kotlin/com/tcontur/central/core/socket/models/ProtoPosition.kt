package com.tcontur.central.core.socket.models

/**
 * A single vehicle entry inside a [ProtoPosition] response.
 *
 * Schema (array item):
 *   id           → id (2 bytes)
 *   padron       → id (2 bytes)
 *   license_plate → string
 *   route        → string
 *   time         → datetime  (decoded as String or platform datetime → normalised to String)
 *   latitude     → float (4 bytes, 6 decimals)
 *   longitude    → float (4 bytes, 6 decimals)
 *   distance     → unsigned (2 bytes) — metres from inspector
 */
data class ProtoVehicle(
    val id: Int?,
    val padron: Int?,
    val licensePlate: String?,
    val route: String?,
    /** ISO datetime string (e.g. "2026-03-09T08:30:00") — last GPS fix time. */
    val time: String?,
    val latitude: Double?,
    val longitude: Double?,
    /** Distance from the inspector's current position, in metres. */
    val distance: Int?
) {
    companion object {
        fun fromMap(map: Map<String, Any>): ProtoVehicle = ProtoVehicle(
            id           = (map["id"]      as? Number)?.toInt(),
            padron       = (map["padron"]  as? Number)?.toInt(),
            licensePlate = map["license_plate"] as? String,
            route        = map["route"]    as? String,
            // datetime may come as java.time.LocalDateTime or String depending on decoder
            time         = map["time"]?.toString(),
            latitude     = (map["latitude"]  as? Number)?.toDouble(),
            longitude    = (map["longitude"] as? Number)?.toDouble(),
            distance     = (map["distance"] as? Number)?.toInt()
        )
    }
}

/**
 * Server response to the "position" frame.
 *
 * Schema (server side):
 *   vehicles → array of vehicle GPS snapshots
 *
 * This is the primary data model for the map tab —
 * [SocketDispatcherViewModel] writes it to [SocketSessionRepository]
 * and the map composable reads it to render nearby vehicle markers.
 *
 * Usage:
 *   val position = ProtoPosition.fromMap(data)
 *   socketSessionRepository.onPositionReceived(position)
 */
data class ProtoPosition(
    val vehicles: List<ProtoVehicle>
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>): ProtoPosition {
            val rawList = map["vehicles"] as? List<*> ?: emptyList<Any>()
            val vehicles = rawList.mapNotNull { item ->
                (item as? Map<String, Any>)?.let { ProtoVehicle.fromMap(it) }
            }
            return ProtoPosition(vehicles = vehicles)
        }
    }
}
