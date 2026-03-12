package com.tcontur.central.core.socket.models

data class ProtoCheckQrTicket(
    val fare: Int?,
    val correlative: Long?
) {
    companion object {
        fun fromMap(map: Map<String, Any>) = ProtoCheckQrTicket(
            fare        = (map["fare"]        as? Number)?.toInt(),
            correlative = (map["correlative"] as? Number)?.toLong()
        )
    }
}

data class ProtoCheckQr(
    val inspeccion: Int?,
    val tickets: List<ProtoCheckQrTicket>
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any>): ProtoCheckQr {
            val tickets = (map["tickets"] as? List<*> ?: emptyList<Any>())
                .mapNotNull { (it as? Map<String, Any>)?.let(ProtoCheckQrTicket::fromMap) }
            return ProtoCheckQr(
                inspeccion = (map["inspeccion"] as? Number)?.toInt(),
                tickets    = tickets
            )
        }
    }
}