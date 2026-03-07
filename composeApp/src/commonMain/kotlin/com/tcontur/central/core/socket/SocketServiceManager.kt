package com.tcontur.central.core.socket

interface SocketServiceManager {

    fun connect(wsUrl: String)

    fun disconnect()

    fun send(data: HashMap<String, Any?>, formatKey: String)

    /**
     * Starts the platform-specific background location tracking service.
     * On Android this launches [LocationForegroundService].
     * Must be called after a successful login so the service can show
     * progress through its WS-connection notification steps.
     */
    fun startLocationTracking()

    /** Stops the background location tracking service. */
    fun stopLocationTracking()
}
