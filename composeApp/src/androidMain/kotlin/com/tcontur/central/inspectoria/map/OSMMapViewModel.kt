package com.tcontur.central.inspectoria.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcontur.central.core.location.LocationData
import com.tcontur.central.core.location.LocationRepository
import com.tcontur.central.core.socket.SocketServiceManager
import com.tcontur.central.core.socket.SocketSessionRepository
import com.tcontur.central.core.socket.models.ProtoVehicle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private const val TAG = "[TCONTUR][MAP_VM]"

data class OSMMapUiState(
    val isLoading: Boolean = true,
    val isMapReady: Boolean = false
)

class OSMMapViewModel(
    private val locationRepository: LocationRepository,
    private val socketSessionRepository: SocketSessionRepository,
    private val socketServiceManager: SocketServiceManager
) : ViewModel() {

    // ── UI state ───────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(OSMMapUiState())
    val uiState: StateFlow<OSMMapUiState> = _uiState.asStateFlow()

    // ── Location ───────────────────────────────────────────────────────────────

    val location   = locationRepository.location
    val isTracking = locationRepository.isTracking

    // ── Vehicles (sorted by distance ascending) ────────────────────────────────

    /**
     * Nearest vehicle first (index 0).
     * Drives both the map markers and the floating carousel above the map.
     */
    val vehicles: StateFlow<List<ProtoVehicle>> = socketSessionRepository.positionData
        .map { proto ->
            (proto?.vehicles ?: emptyList())
                .sortedBy { it.distance ?: Int.MAX_VALUE }
        }
        .stateIn(
            scope          = viewModelScope,
            started        = SharingStarted.WhileSubscribed(5_000),
            initialValue   = emptyList()
        )

    // ── Internal map state ─────────────────────────────────────────────────────

    private var mapView: MapView? = null
    private var locationMarker: Marker? = null
    private val vehicleMarkers = mutableListOf<Marker>()

    // ── Public API ─────────────────────────────────────────────────────────────

    fun setMapView(view: MapView) {
        mapView = view
        _uiState.value = _uiState.value.copy(isLoading = false, isMapReady = true)
    }

    /**
     * Adds or updates the inspector's location marker using a blue-dot drawable.
     * Must be called from the main thread (Compose LaunchedEffect guarantees this).
     */
    fun updateLocationMarker(mapView: MapView, location: LocationData, context: Context) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)

        if (locationMarker == null) {
            locationMarker = Marker(mapView).apply {
                position = geoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon  = createGpsLocationDrawable(context)
                title = "Mi ubicación"
            }
            mapView.overlays.add(locationMarker)
        } else {
            locationMarker!!.position = geoPoint
        }
        mapView.invalidate()
    }

    /**
     * Replaces all vehicle markers on the map.
     * Index 0 of [vehicles] gets the green bus icon (nearest); the rest get blue.
     * Must be called from the main thread.
     */
    fun updateVehicleMarkers(mapView: MapView, vehicles: List<ProtoVehicle>, context: Context) {
        println("$TAG updateVehicleMarkers — count=${vehicles.size}")

        // Remove previous vehicle markers
        vehicleMarkers.forEach { mapView.overlays.remove(it) }
        vehicleMarkers.clear()

        vehicles.forEachIndexed { index, vehicle ->
            val lat     = vehicle.latitude  ?: return@forEachIndexed
            val lon     = vehicle.longitude ?: return@forEachIndexed
            val padronStr = vehicle.padron?.toString() ?: return@forEachIndexed
            val isNearest = (index == 0)

            val icon = createCustomMarkerDrawable(context, padronStr, isNearest)
                ?: return@forEachIndexed

            val marker = Marker(mapView).apply {
                position = GeoPoint(lat, lon)
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                this.icon    = icon
                title        = "Padrón $padronStr"
                subDescription = vehicle.route ?: ""
            }
            vehicleMarkers.add(marker)
            mapView.overlays.add(marker)
        }

        mapView.invalidate()
    }

    fun sendRequestQr(vehicleId: Int) {
        socketServiceManager.send(
            data      = hashMapOf("vehicle" to vehicleId),
            formatKey = "request_qr"
        )
    }

    fun centerOnLocation(mapView: MapView, location: LocationData) {
        val geoPoint = GeoPoint(location.latitude, location.longitude)
        mapView.controller.animateTo(geoPoint, 16.0, 500L)
    }

    fun cleanupMap() {
        mapView?.let { mv ->
            locationMarker?.let { mv.overlays.remove(it) }
            vehicleMarkers.forEach { mv.overlays.remove(it) }
        }
        locationMarker = null
        vehicleMarkers.clear()
        mapView = null
        _uiState.value = OSMMapUiState()
    }

    override fun onCleared() {
        super.onCleared()
        cleanupMap()
    }
}
