package com.tcontur.central.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Global singleton bus for session-level events.
 *
 * Any part of the app (Ktor validator, API services) can call
 * [notifyUnauthorized] when a 401 response is received.
 * The navigation layer (AppNavHost) observes [unauthorized] and
 * clears the session + forces navigation to the Login screen.
 */
object SessionEventBus {

    private val _unauthorized = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val unauthorized: SharedFlow<Unit> = _unauthorized.asSharedFlow()

    /** Call this whenever a 401 response is received from any API endpoint. */
    fun notifyUnauthorized() {
        _unauthorized.tryEmit(Unit)
    }
}
