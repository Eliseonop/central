package com.tcontur.central.core.permission

import androidx.compose.runtime.Composable

/**
 * Holds the current state of all startup permissions required by the app.
 *
 * @param allGranted  true when every required permission has been granted.
 * @param request     lambda to trigger the OS permission dialog(s).
 */
data class StartupPermissionsState(
    val allGranted: Boolean,
    val request: () -> Unit
)

/**
 * Returns the current [StartupPermissionsState] for this platform.
 *
 * Required permissions (Android):
 *   • POST_NOTIFICATIONS  (API 33+)
 *   • ACCESS_FINE_LOCATION + ACCESS_COARSE_LOCATION
 *
 * iOS: always returns allGranted = true (platform handles permissions at point-of-use).
 */
@Composable
expect fun rememberStartupPermissions(): StartupPermissionsState
