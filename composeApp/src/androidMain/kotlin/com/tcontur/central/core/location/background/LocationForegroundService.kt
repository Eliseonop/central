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
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.tcontur.central.core.location.LocationData
import com.tcontur.central.core.location.LocationRepository
import com.tcontur.central.core.storage.AppStorage
import com.tcontur.central.core.storage.StorageKeys
import com.tcontur.central.core.utils.currentFormattedTimestamp
import com.tcontur.central.data.LocationApiService
import com.tcontur.central.data.model.LocationRequest as LocationApiRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Foreground service that uses FusedLocationProvider continuous callbacks
 * to track the inspector's GPS position.
 *
 * Every update is emitted to [LocationRepository] so that any ViewModel
 * can collect the live position without triggering a new GPS request.
 * The position is also forwarded to the API every [SEND_INTERVAL_MS] ms.
 */
class LocationForegroundService : Service() {

    private val locationRepo: LocationRepository by inject()
    private val locationApi:  LocationApiService  by inject()
    private val storage:      AppStorage          by inject()

    private lateinit var fusedClient: FusedLocationProviderClient
    private var locationCallback: LocationCallback? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var lastSendTime = 0L

    companion object {
        private const val CHANNEL_ID      = "TID"
        private const val NOTIFICATION_ID = 112233

        /** Minimum interval between FusedLocationProvider updates. */
        private const val LOCATION_INTERVAL_MS = 2_500L

        /** How often the position is forwarded to the remote API. */
        private const val SEND_INTERVAL_MS = 5_000L

        /** Discard fixes worse than this accuracy (metres). */
        private const val MIN_ACCURACY_METERS = 50f
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification("Conectando...", Color.GRAY))

        // FusedLocationProviderClient must be created on the main thread.
        // onCreate() is always called on the main thread, so this is safe.
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        startLocationUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int =
        START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopLocationUpdates()
        serviceScope.cancel()
        super.onDestroy()
    }

    // ─── FusedLocationProvider ────────────────────────────────────────────────

    private fun startLocationUpdates() {
        if (locationCallback != null) return   // already active
        if (!hasPermission()) {
            updateNotification("Sin permisos de ubicación", Color.RED)
            locationRepo.setTracking(false)
            return
        }

        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_INTERVAL_MS)
            .setMinUpdateIntervalMillis(LOCATION_INTERVAL_MS)
            .setWaitForAccurateLocation(false)
            .setGranularity(Granularity.GRANULARITY_FINE)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val raw = result.lastLocation ?: return
                val data = LocationData(
                    latitude  = raw.latitude,
                    longitude = raw.longitude,
                    accuracy  = raw.accuracy,
                    timestamp = raw.time
                )

                // 1. Emit to repository — all collectors receive the update immediately
                locationRepo.emit(data)

                // 2. Skip very inaccurate fixes for the API call
                if (raw.accuracy > MIN_ACCURACY_METERS) return

                // 3. Forward to the API at most once every SEND_INTERVAL_MS
                val now = System.currentTimeMillis()
                if (now - lastSendTime >= SEND_INTERVAL_MS) {
                    lastSendTime = now
                    serviceScope.launch(Dispatchers.IO) { sendToApi(data) }
                }
            }
        }

        try {
            fusedClient.requestLocationUpdates(
                request,
                locationCallback!!,
                Looper.getMainLooper()
            ).addOnSuccessListener {
                locationRepo.setTracking(true)
                updateNotification("Rastreando ubicación", Color.GREEN)
            }.addOnFailureListener {
                locationRepo.setTracking(false)
                updateNotification("Error al iniciar GPS", Color.RED)
            }
        } catch (e: SecurityException) {
            locationRepo.setTracking(false)
            updateNotification("Sin permisos de ubicación", Color.RED)
        }
    }

    private fun stopLocationUpdates() {
        locationCallback?.let { cb ->
            fusedClient.removeLocationUpdates(cb)
            locationCallback = null
        }
        locationRepo.setTracking(false)
    }

    // ─── API forwarding ───────────────────────────────────────────────────────

    private suspend fun sendToApi(data: LocationData) {
        val token = storage.getString(StorageKeys.AUTH_TOKEN)
        val code  = storage.getString(StorageKeys.EMPRESA_CODE)
        if (token.isBlank() || code.isBlank()) return

        locationApi.sendLocation(
            empresaCodigo = code,
            token         = token,
            request       = LocationApiRequest(
                lat = data.latitude,
                lon = data.longitude,
                ts  = currentFormattedTimestamp()
            )
        )
    }

    // ─── Notification helpers ─────────────────────────────────────────────────

    private fun buildNotification(content: String, color: Int = Color.GRAY): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Inspector")
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setColor(color)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setVisibility(NotificationCompat.VISIBILITY_SECRET)
            .setShowWhen(false)
            .build()

    private fun updateNotification(content: String, color: Int = Color.GRAY) {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(content, color))
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TCONTUR",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Rastreo GPS del inspector"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = Notification.VISIBILITY_SECRET
            }
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }
    }

    // ─── Permission check ─────────────────────────────────────────────────────

    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
}
