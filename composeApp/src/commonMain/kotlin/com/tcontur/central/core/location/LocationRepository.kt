package com.tcontur.central.core.location

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Single source of truth for the device's live GPS position.
 *
 * The background service (LocationForegroundService on Android) pushes
 * every location update here via [emit].  Any ViewModel or use-case that
 * needs the current position just collects [location] — no need to make a
 * new GPS request each time.
 */
class LocationRepository {

    private val _location   = MutableStateFlow<LocationData?>(null)
    val location: StateFlow<LocationData?> = _location.asStateFlow()

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    /** Called by the background service on every new GPS fix. */
    fun emit(data: LocationData) {
        _location.value = data
    }

    /** Called by the background service when it starts / stops tracking. */
    fun setTracking(active: Boolean) {
        _isTracking.value = active
    }
}
