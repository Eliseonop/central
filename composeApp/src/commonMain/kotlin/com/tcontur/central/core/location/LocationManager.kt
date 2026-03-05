package com.tcontur.central.core.location


interface LocationManager {
    /** Returns the current GPS position, or null if unavailable. */
    suspend fun getCurrentLocation(): LocationData?

    /** Requests OS-level location permission. Returns true if granted. */
    suspend fun requestPermission(): Boolean

    /** Checks whether location permission has been granted. */
    fun isPermissionGranted(): Boolean

    /** Checks whether the device location service (GPS) is enabled. */
    fun isServiceEnabled(): Boolean

    /** Opens the OS location settings screen. */
    suspend fun openLocationSettings()

    /** Opens the app permission settings screen. */
    suspend fun openAppSettings()
}
