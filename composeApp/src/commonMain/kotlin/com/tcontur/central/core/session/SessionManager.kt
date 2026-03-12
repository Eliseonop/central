package com.tcontur.central.core.session

import com.tcontur.central.core.socket.SocketServiceManager
import com.tcontur.central.core.socket.SocketSessionRepository
import com.tcontur.central.data.AuthRepositoryImpl

private const val TAG = "[TCONTUR][SESSION]"

/**
 * Centralizes session shutdown logic.
 *
 * Call [logout] whenever the session must be terminated — both on explicit
 * user action (drawer/menu logout) and on automatic 401 / token-invalid responses.
 *
 * Shutdown order:
 * 1. Stop background location tracking service (LocationForegroundService)
 * 2. Disconnect and stop the WebSocket service (SocketService)
 * 3. Clear server-pushed session data (SocketSessionRepository)
 * 4. Clear persisted auth credentials
 *
 * This ensures no background process keeps running with an invalid session.
 */
class SessionManager(
    private val socketServiceManager: SocketServiceManager,
    private val authRepository: AuthRepositoryImpl,
    private val socketSessionRepository: SocketSessionRepository
) {
    /**
     * Stops all session-dependent background processes, disconnects the socket,
     * clears server-pushed session data, and wipes stored auth credentials.
     */
    suspend fun logout() {
        println("$TAG ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        println("$TAG  LOGOUT — iniciando cierre de sesión")
        println("$TAG ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")

        println("$TAG [1/4] Deteniendo servicio de tracking GPS...")
        socketServiceManager.stopLocationTracking()
        println("$TAG [1/4] ✅ Tracking GPS detenido")

        println("$TAG [2/4] Desconectando y deteniendo SocketService...")
        socketServiceManager.disconnect()
        println("$TAG [2/4] ✅ Socket desconectado y servicio detenido")

        println("$TAG [3/4] Limpiando datos de sesión del socket...")
        socketSessionRepository.clearSession()
        println("$TAG [3/4] ✅ SocketSessionRepository limpiado")

        println("$TAG [4/4] Limpiando credenciales almacenadas...")
        authRepository.logout()
        println("$TAG [4/4] ✅ Credenciales eliminadas")

        println("$TAG ✅ Cierre de sesión completado — redirigiendo a Login")
    }
}
