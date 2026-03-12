package com.tcontur.central.inspectoria.map

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tcontur.central.core.socket.models.ProtoVehicle
import com.tcontur.central.ui.theme.BtnBlueBg
import com.tcontur.central.ui.theme.BtnBlueFg
import com.tcontur.central.ui.theme.BtnGreenBg
import com.tcontur.central.ui.theme.BtnGreenFg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.MapView

// ── Colours — vehicle cards (fully saturated for distinction on map) ──────────
private val ColorNearest = Color(0xFF2E7D32)   // dark green — nearest vehicle card
private val ColorOther   = Color(0xFF1565C0)   // dark blue — other vehicle cards

@Composable
actual fun OSMMapView(modifier: Modifier) {
    val context    = LocalContext.current
    val viewModel: OSMMapViewModel = koinViewModel()
    val uiState    by viewModel.uiState.collectAsStateWithLifecycle()
    val location   by viewModel.location.collectAsStateWithLifecycle()
    val isTracking by viewModel.isTracking.collectAsStateWithLifecycle()
    val vehicles   by viewModel.vehicles.collectAsStateWithLifecycle()

    var isConfigured by remember { mutableStateOf(false) }

    // ── Initialise osmdroid on IO thread ────────────────────────────────────────
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            Configuration.getInstance()
                .load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            Configuration.getInstance().userAgentValue = context.packageName
            Configuration.getInstance().osmdroidTileCache.mkdirs()
        }
        isConfigured = true
    }

    val mapView = remember(isConfigured) {
        if (!isConfigured) return@remember null
        MapView(context).apply {
            setMultiTouchControls(true)
            setTileSource(TileSourceFactory.MAPNIK)
            setUseDataConnection(true)
            controller.setZoom(13.0)
            minZoomLevel = 5.0
            maxZoomLevel = 19.0
            isTilesScaledToDpi = true
            viewModel.setMapView(this)
        }
    }

    LaunchedEffect(location, isTracking) {
        val loc = location
        if (isTracking && loc != null) {
            mapView?.let { viewModel.updateLocationMarker(it, loc, context) }
        }
    }

    LaunchedEffect(vehicles, uiState.isMapReady) {
        if (uiState.isMapReady) {
            mapView?.let { viewModel.updateVehicleMarkers(it, vehicles, context) }
        }
    }

    LaunchedEffect(uiState.isMapReady) {
        val loc = location
        if (uiState.isMapReady && isTracking && loc != null) {
            mapView?.let { viewModel.centerOnLocation(it, loc) }
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.cleanupMap() }
    }

    // ── Layout ──────────────────────────────────────────────────────────────────
    Box(modifier = modifier.fillMaxSize()) {

        // Map or loading indicator
        when {
            mapView == null || uiState.isLoading -> LoadingMapState(Modifier.fillMaxSize())
            else -> AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
        }

        // ── Floating vehicle card carousel (top) ────────────────────────────
        AnimatedVisibility(
            visible  = vehicles.isNotEmpty(),
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            LazyRow(
                modifier              = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                contentPadding        = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(vehicles) { index, vehicle ->
                    VehicleCard(vehicle = vehicle, isNearest = index == 0)
                }
            }
        }

        // ── Ticket FAB — request QR for nearest (green) vehicle ─────────────
        AnimatedVisibility(
            visible  = uiState.isMapReady && mapView != null && vehicles.isNotEmpty(),
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    val vehicleId = vehicles.firstOrNull()?.id ?: return@FloatingActionButton
                    viewModel.sendRequestQr(vehicleId)
                },
                modifier       = Modifier.size(56.dp),
                shape          = CircleShape,
                containerColor = BtnGreenBg,
                contentColor   = BtnGreenFg
            ) {
                Icon(
                    imageVector        = Icons.Default.ConfirmationNumber,
                    contentDescription = "Solicitar ticket",
                    modifier           = Modifier.size(24.dp)
                )
            }
        }

        // ── Location FAB — centre on my position ────────────────────────────
        AnimatedVisibility(
            visible  = uiState.isMapReady && mapView != null && isTracking,
            enter    = fadeIn(),
            exit     = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    val loc = location
                    if (loc != null) mapView?.let { viewModel.centerOnLocation(it, loc) }
                },
                modifier       = Modifier.size(56.dp),
                shape          = CircleShape,
                containerColor = BtnBlueBg,
                contentColor   = BtnBlueFg
            ) {
                Icon(
                    imageVector        = Icons.Default.MyLocation,
                    contentDescription = "Centrar en mi ubicación",
                    modifier           = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ── Vehicle card ─────────────────────────────────────────────────────────────
/**
 * Card with coloured background: green for nearest, blue for others.
 * Uses a vector bus icon — map markers still use the PNG drawables.
 */
@Composable
private fun VehicleCard(vehicle: ProtoVehicle, isNearest: Boolean) {
    val bgColor   = if (isNearest) ColorNearest else ColorOther
    val textColor = Color.White

    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier  = Modifier.width(160.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {

            // Bus icon + padron number
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector        = Icons.Default.DirectionsBus,
                    contentDescription = null,
                    tint               = textColor,
                    modifier           = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text       = "${vehicle.padron ?: "-"}",
                    style      = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color      = textColor
                )
            }

            Spacer(Modifier.height(4.dp))

            // License plate
            Text(
                text  = vehicle.licensePlate ?: "-",
                style = MaterialTheme.typography.bodySmall,
                color = textColor.copy(alpha = 0.85f)
            )

            // Route
            Text(
                text     = vehicle.route ?: "-",
                style    = MaterialTheme.typography.bodySmall,
                color    = textColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Distance
            if (vehicle.distance != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text       = "${vehicle.distance} m",
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color      = textColor
                )
            }
        }
    }
}

// ── Loading state ────────────────────────────────────────────────────────────

@Composable
private fun LoadingMapState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Cargando mapa...", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
