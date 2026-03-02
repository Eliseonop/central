package com.tcontur.central.core.location

data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float = 0f,
    val timestamp: Long = 0L
)
