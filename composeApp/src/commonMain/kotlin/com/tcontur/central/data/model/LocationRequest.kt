package com.tcontur.central.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocationRequest(
    @SerialName("lat") val lat: Double,
    @SerialName("lon") val lon: Double,
    @SerialName("ts")  val ts: String     // "YYYY-MM-DD HH:mm:ss"
)
