package com.tcontur.central.core.location

import androidx.compose.runtime.Composable

/**
 * Returns a lambda that, when invoked, triggers the OS location permission dialog.
 * [onResult] receives true if the user granted the permission.
 *
 * - androidMain: uses rememberLauncherForActivityResult (ACCESS_FINE + ACCESS_COARSE)
 * - iosMain:     delegates to IosLocationManager.requestPermission()
 */
@Composable
expect fun rememberRequestLocationPermission(
    onResult: (granted: Boolean) -> Unit
): () -> Unit
