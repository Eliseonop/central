package com.tcontur.central.inspectoria.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Platform-specific online map view powered by OpenStreetMap tiles.
 *
 * - Android: osmdroid MapView with MAPNIK online tile source
 * - iOS:     placeholder (osmdroid is Android-only)
 *
 * Shows the device's current GPS position when location is being tracked,
 * and includes a FAB to re-center the map on the user's location.
 */
@Composable
expect fun OSMMapView(modifier: Modifier = Modifier)
