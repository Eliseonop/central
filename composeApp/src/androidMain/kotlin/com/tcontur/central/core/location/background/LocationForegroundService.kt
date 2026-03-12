package com.tcontur.central.core.location.background

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.tcontur.central.core.location.LocationData
import com.tcontur.central.core.location.LocationRepository
import com.tcontur.central.core.services.SocketService
import com.tcontur.central.core.socket.ProtoSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.android.ext.android.inject
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import java.time.LocalDateTime
import java.time.ZoneOffset

private const val TAG = "TCONTUR_GPS"

class LocationForegroundService : Service() {

    private val locationRepo:       LocationRepository by inject()
    private val protoSocketManager: ProtoSocketManager by inject()
    private val appStorage:         AppStorage         by inject()

    private lateinit var fusedClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    private val serviceScope       = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastSocketSendTime = 0L

    companion object {
        private const val CHANNEL_ID           = "TID"
        private const val NOTIFICATION_ID      = 112233
        private const val LOCATION_INTERVAL_MS = 2_500L
        private const val SEND_INTERVAL_MS     = 5_000L
        private const val MIN_ACCURACY_METERS  = 50f
        private const val STEP_TIMEOUT_MS      = 30_000L
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        Log.d(TAG, "  LocationForegroundService CREADO")
        Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("🔴 Iniciando conexión...", Color.RED))
        Log.d(TAG, "startForeground() llamado con éxito")
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        serviceScope.launch { runStartupSequence() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand() — intent.action=${intent?.action}")
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "LocationForegroundService DESTRUIDO")
        stopLocationUpdates()
        serviceScope.cancel()
        super.onDestroy()
    }

    // ─── Startup sequence ─────────────────────────────────────────────────────

    private suspend fun runStartupSequence() {
        Log.d(TAG, "── runStartupSequence() iniciado ──")

        // ── 0. Credential guard ────────────────────────────────────────────────
        val userJson = appStorage.getString(StorageKeys.USER_JSON)
        if (userJson.isBlank()) {
            Log.w(TAG, "⛔ [0] Sin sesión almacenada — deteniendo servicio")
            updateNotification("⛔ Sin sesión activa", Color.RED)
            stopSelf()
            return
        }
        Log.d(TAG, "✅ [0] Sesión almacenada encontrada")

        // ── 1. Iniciando conexión ──────────────────────────────────────────────
        Log.d(TAG, "⏳ [1] Esperando que el socket se conecte...")
        updateNotification("🔴 Iniciando conexión...", Color.RED)

        if (!protoSocketManager.isConnected.value) {
            val connected = withTimeoutOrNull(STEP_TIMEOUT_MS) {
                protoSocketManager.isConnected.first { it }
            }
            if (connected == null) {
                Log.e(TAG, "❌ [1] Timeout esperando conexión WS (${STEP_TIMEOUT_MS / 1000}s) — deteniendo servicio")
                stopSelf()
                return
            }
        }
        Log.d(TAG, "✅ [1] Socket conectado")

        // ── 2 & 3. Socket conectado → Logueando ───────────────────────────────
        Log.d(TAG, "⏳ [2] Esperando confirmación de login del servidor (isAuthenticated)...")
        updateNotification("🟡 Socket conectado · Logueando...", Color.YELLOW)

        if (!protoSocketManager.isAuthenticated.value) {
            val authenticated = withTimeoutOrNull(STEP_TIMEOUT_MS) {
                protoSocketManager.isAuthenticated.first { it }
            }
            if (authenticated == null) {
                Log.e(TAG, "❌ [2] Timeout esperando confirmación de login (${STEP_TIMEOUT_MS / 1000}s) — deteniendo servicio")
                updateNotification("⛔ Login no confirmado", Color.RED)
                stopSelf()
                return
            }
        }
        Log.d(TAG, "✅ [2] Login confirmado por el servidor — GPS tracking habilitado")

        // ── 4. GPS tracking ───────────────────────────────────────────────────
        Log.d(TAG, "📍 [3] Iniciando FusedLocationProvider...")
        withContext(Dispatchers.Main) { startLocationUpdates() }
    }

    // ─── FusedLocationProvider ────────────────────────────────────────────────

    private fun startLocationUpdates() {
        if (locationCallback != null) {
            Log.w(TAG, "startLocationUpdates() — ya activo, ignorado")
            return
        }
        if (!hasPermission()) {
            Log.e(TAG, "❌ Sin permisos de ubicación — no se puede iniciar GPS")
            updateNotification("⛔ Sin permisos de ubicación", Color.RED)
            return
        }

        Log.d(TAG, "📍 Registrando LocationCallback (interval=${LOCATION_INTERVAL_MS}ms, minAccuracy=${MIN_ACCURACY_METERS}m)")

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MS)
            .setMinUpdateIntervalMillis(LOCATION_INTERVAL_MS)
            .setWaitForAccurateLocation(false)
            .setGranularity(Granularity.GRANULARITY_FINE)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val raw = result.lastLocation ?: return

                // Always emit to repository (UI observers)
                locationRepo.emit(LocationData(raw.latitude, raw.longitude, raw.accuracy, raw.time))

                // Skip inaccurate fixes for socket send
                if (raw.accuracy > MIN_ACCURACY_METERS) {
                    Log.v(TAG, "📍 Fix ignorado — precisión insuficiente: ${raw.accuracy}m (máx ${MIN_ACCURACY_METERS}m)")
                    return
                }

                // Block sends until login is confirmed for this socket session.
                // isAuthenticated resets to false on disconnect, so reconnects
                // without login are automatically blocked until SocketDispatcherViewModel
                // completes the login handshake.
                if (!protoSocketManager.isAuthenticated.value) {
                    Log.d(TAG, "📍 Sin login en sesión actual — omitiendo envío de posición")
                    return
                }

                val now = System.currentTimeMillis()
                if (now - lastSocketSendTime < SEND_INTERVAL_MS) return
                lastSocketSendTime = now

                Log.d(TAG, "📤 Enviando posición → lat=%.6f lon=%.6f acc=%.1fm".format(
                    raw.latitude, raw.longitude, raw.accuracy
                ))

                // Notification is NOT updated here — it stays "🟢 Conectado"
                // to avoid flickering back to "🔴 Iniciando..." on every send.

                val dt = LocalDateTime.ofEpochSecond(now / 1000L, 0, ZoneOffset.UTC)
                SocketService.sendMessage(
                    context   = this@LocationForegroundService,
                    data      = hashMapOf(
                        "time"      to dt,
                        "latitude"  to raw.latitude,
                        "longitude" to raw.longitude
                    ),
                    formatKey = "position"
                )
            }
        }

        try {
            fusedClient.requestLocationUpdates(
                request,
                locationCallback!!,
                Looper.getMainLooper()
            ).addOnSuccessListener {
                Log.d(TAG, "✅ FusedLocationProvider activo — tracking ON")
                locationRepo.setTracking(true)
                // Set final stable notification — stays here until service is destroyed
                updateNotification("🟢 Conectado", Color.GREEN)
            }.addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al iniciar FusedLocationProvider: ${e.message}")
                updateNotification("⛔ Error al iniciar GPS", Color.RED)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "❌ SecurityException al iniciar GPS: ${e.message}")
            updateNotification("⛔ Sin permisos de ubicación", Color.RED)
        }
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { cb ->
            Log.d(TAG, "📍 Deteniendo FusedLocationProvider")
            fusedClient.removeLocationUpdates(cb)
            locationCallback = null
        }
        locationRepo.setTracking(false)
    }

    // ─── Notification ─────────────────────────────────────────────────────────

    private fun buildNotification(content: String, color: Int = Color.GRAY): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Inspector TCONTUR")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setColor(color)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setShowWhen(false)
            .build()

    private fun updateNotification(content: String, color: Int = Color.GRAY) {
        Log.d(TAG, "🔔 Notificación → \"$content\"")
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(content, color))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Inspector GPS",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description          = "Estado del rastreo GPS del inspector"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación '$CHANNEL_ID' creado")
        }
    }

    // ─── Permission check ─────────────────────────────────────────────────────

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
}
