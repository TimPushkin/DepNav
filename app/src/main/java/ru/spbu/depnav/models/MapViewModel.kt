package ru.spbu.depnav.models

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.*
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import ru.spbu.depnav.providers.MarkerIconProvider

private const val TAG = "MapViewModel"

class MapViewModel(
    width: Int,
    height: Int,
    tileSize: Int = 1024
) : ViewModel() {
    private val mMarkerIconProvider = MarkerIconProvider()
    private var mMarkerIds = emptyList<Marker>()

    val state by mutableStateOf(
        MapState(1, width, height, tileSize) {
            scroll(0.5, 0.5)
            scale(0f)
        }
    )

    fun replaceLayersWith(tileProviders: List<TileStreamProvider>) {
        Log.d(TAG, "Replacing layers...")

        state.removeAllLayers()
        for (tileProvider in tileProviders) state.addLayer(tileProvider)
    }

    fun replaceMarkersWith(markers: List<Marker>) {
        Log.d(TAG, "Replacing markers...")

        mMarkerIds.forEach { state.removeMarker(it.id) }
        for (marker in markers) state.addMarker(marker.id, marker.x, marker.y) {
            mMarkerIconProvider.getIcon(marker.type)
        }
    }

    fun centerOnMarker(id: String) {
        Log.d(TAG, "Centering on marker $id")

        viewModelScope.launch { state.centerOnMarker(id, 1f) }
    }
}
