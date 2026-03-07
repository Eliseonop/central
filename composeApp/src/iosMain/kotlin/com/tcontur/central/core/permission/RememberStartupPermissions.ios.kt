package com.tcontur.central.core.permission

import androidx.compose.runtime.Composable

/**
 * iOS: permissions are requested by the system at point-of-use
 * (CLLocationManager for location, UNUserNotificationCenter for notifications).
 * The startup gate is a no-op — always report all granted so the app proceeds.
 */
@Composable
actual fun rememberStartupPermissions(): StartupPermissionsState =
    StartupPermissionsState(
        allGranted = true,
        request    = {}
    )
