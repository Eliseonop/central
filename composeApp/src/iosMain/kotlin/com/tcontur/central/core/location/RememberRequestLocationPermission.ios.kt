package com.tcontur.central.core.location

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
actual fun rememberRequestLocationPermission(
    onResult: (granted: Boolean) -> Unit
): () -> Unit {
    val locationManager: LocationManager = koinInject()
    val scope = rememberCoroutineScope()
    return {
        scope.launch {
            val granted = locationManager.requestPermission()
            onResult(granted)
        }
    }
}
