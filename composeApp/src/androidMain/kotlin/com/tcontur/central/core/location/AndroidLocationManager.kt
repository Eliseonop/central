package com.tcontur.central.core.location

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager as AndroidSysLocationManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AndroidLocationManager(private val context: Context) : LocationManager {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    override suspend fun getCurrentLocation(): LocationData? =
        suspendCancellableCoroutine { cont ->
            if (!isPermissionGranted()) {
                cont.resume(null)
                return@suspendCancellableCoroutine
            }
            try {
                fusedClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY, null
                ).addOnSuccessListener { location ->
                    cont.resume(
                        location?.let {
                            LocationData(it.latitude, it.longitude, it.accuracy, it.time)
                        }
                    )
                }.addOnFailureListener {
                    cont.resume(null)
                }
            } catch (e: SecurityException) {
                cont.resume(null)
            }
        }

    override suspend fun requestPermission(): Boolean {
        // Runtime permission must be triggered from an Activity.
        // We rely on the Compose permission API in the UI layer.
        // This method is a no-op here; UI should use rememberPermissionState.
        return isPermissionGranted()
    }

    override fun isPermissionGranted(): Boolean {
        val fine   = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fine == PackageManager.PERMISSION_GRANTED ||
               coarse == PackageManager.PERMISSION_GRANTED
    }

    override fun isServiceEnabled(): Boolean {
        val mgr = context.getSystemService(Context.LOCATION_SERVICE) as AndroidSysLocationManager
        return mgr.isProviderEnabled(AndroidSysLocationManager.GPS_PROVIDER) ||
               mgr.isProviderEnabled(AndroidSysLocationManager.NETWORK_PROVIDER)
    }

    override suspend fun openLocationSettings() {
        context.startActivity(
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }

    override suspend fun openAppSettings() {
        context.startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", context.packageName, null))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
