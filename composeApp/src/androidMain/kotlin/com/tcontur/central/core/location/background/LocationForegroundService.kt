package com.tcontur.central.core.location.background

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.core.utils.currentFormattedTimestamp
import com.tcontur.central.data.model.LocationRequest
import com.tcontur.central.data.remote.LocationApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Foreground service that sends the inspector's GPS position to the API every second.
 * Mirrors the behavior of the original Flutter background_service implementation.
 */
class LocationForegroundService : Service() {

    private val locationApiService: LocationApiService by inject()
    private val storage: AppStorage by inject()
    private val locationManager: com.tcontur.central.core.location.AndroidLocationManager by inject()

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private var trackingJob: Job? = null

    companion object {
        private const val CHANNEL_ID    = "TID"
        private const val NOTIFICATION_ID = 112233
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Conectando..."))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startTracking()
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        trackingJob?.cancel()
        super.onDestroy()
    }

    // ─── Tracking loop ────────────────────────────────────────────────────────

    private fun startTracking() {
        trackingJob = serviceScope.launch {
            while (isActive) {
                val token      = storage.getString(StorageKeys.AUTH_TOKEN)
                val empresaCod = storage.getString(StorageKeys.EMPRESA_CODE)

                if (token.isBlank() || empresaCod.isBlank()) {
                    updateNotification("Sin sesión activa")
                    stopSelf()
                    break
                }

                if (!locationManager.isPermissionGranted() || !locationManager.isServiceEnabled()) {
                    updateNotification("Ubicación no disponible")
                    delay(5_000)
                    continue
                }

                val location = locationManager.getCurrentLocation()
                if (location != null) {
                    val result = locationApiService.sendLocation(
                        empresaCodigo = empresaCod,
                        token         = token,
                        request       = LocationRequest(
                            lat = location.latitude,
                            lon = location.longitude,
                            ts  = currentFormattedTimestamp()
                        )
                    )
                    updateNotification(
                        if (result is com.tcontur.central.core.network.ApiResult.Success)
                            "Sincronizando ubicación..."
                        else
                            "Error al sincronizar"
                    )
                }

                delay(1_000) // send every second
            }
        }
    }

    // ─── Notification helpers ─────────────────────────────────────────────────

    private fun buildNotification(content: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Inspector")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setSilent(true)
            .build()

    private fun updateNotification(content: String) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(content))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "TCONTUR", NotificationManager.IMPORTANCE_LOW
            ).apply { setShowBadge(false) }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }
}
