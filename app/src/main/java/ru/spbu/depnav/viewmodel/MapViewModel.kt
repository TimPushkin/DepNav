package ru.spbu.depnav.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.*
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.ui.elements.MarkerView

private const val TAG = "MapViewModel"

class MapViewModel(
    width: Int,
    height: Int,
    tileSize: Int = 1024
) : ViewModel() {
    private val mMarkerIds = mutableListOf<String>()

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

        mMarkerIds.forEach { state.removeMarker(it) }
        mMarkerIds.clear()

        for (marker in markers) marker.run {
            state.addMarker(idStr, x, y, clipShape = null) { MarkerView(type) }
            mMarkerIds += idStr
        }
    }

    fun centerOnMarker(id: String) {
        Log.d(TAG, "Centering on marker $id")

        viewModelScope.launch { state.centerOnMarker(id, 1f) }
    }
}
