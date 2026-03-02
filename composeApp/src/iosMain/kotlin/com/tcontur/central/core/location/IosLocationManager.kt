package com.tcontur.central.core.location

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLAuthorizationStatus
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.Foundation.NSURL
import platform.UIKit.UIApplication
import platform.UIKit.UIApplicationOpenSettingsURLString
import platform.darwin.NSObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@OptIn(ExperimentalForeignApi::class)
class IosLocationManager : LocationManager {

    private val clManager = CLLocationManager()

    override suspend fun getCurrentLocation(): LocationData? {
        if (!isPermissionGranted()) return null
        val loc = clManager.location ?: return null
        return loc.coordinate.useContents {
            LocationData(latitude, longitude)
        }
    }

    override suspend fun requestPermission(): Boolean = suspendCoroutine { cont ->
        clManager.requestWhenInUseAuthorization()
        // iOS permission result handled in delegate; simplified here
        cont.resume(isPermissionGranted())
    }

    override fun isPermissionGranted(): Boolean {
        val status = CLLocationManager.authorizationStatus()
        return status == kCLAuthorizationStatusAuthorizedAlways ||
               status == kCLAuthorizationStatusAuthorizedWhenInUse
    }

    override fun isServiceEnabled(): Boolean =
        CLLocationManager.locationServicesEnabled()

    override suspend fun openLocationSettings() {
        // On iOS, we can only open app settings; system location settings require Settings.app
        openAppSettings()
    }

    override suspend fun openAppSettings() {
        val url = NSURL.URLWithString(UIApplicationOpenSettingsURLString)
        if (url != null) UIApplication.sharedApplication.openURL(url)
    }
}
