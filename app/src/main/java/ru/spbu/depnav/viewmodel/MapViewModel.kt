package ru.spbu.depnav.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
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
    private val width: Int,
    private val height: Int,
    tileSize: Int = 1024
) : ViewModel() {
    private val mMarkerIds = mutableListOf<String>()

    val state by mutableStateOf(
        MapState(1, width, height, tileSize) {
            scroll(0.5, 0.5)
            scale(0f)
        }
    )

    fun replaceLayersWith(tileProviders: Iterable<TileStreamProvider>) {
        Log.d(TAG, "Replacing layers...")

        state.removeAllLayers()
        for (tileProvider in tileProviders) state.addLayer(tileProvider)
    }

    fun replaceMarkersWith(markers: Iterable<Marker>) {
        Log.d(TAG, "Replacing markers...")

        mMarkerIds.forEach { state.removeMarker(it) }
        mMarkerIds.clear()

        for (marker in markers) marker.run {
            // TODO: store normalized coordinates in the database
            state.addMarker(
                id = idStr,
                x = x / width,
                y = y / height,
                relativeOffset = Offset(-0.5f, -0.5f),
                clipShape = null
            ) { MarkerView(type) }
            mMarkerIds += idStr
        }

        Log.i(TAG, "Placed ${mMarkerIds.size} markers")
    }

    fun centerOnMarker(id: String) {
        Log.d(TAG, "Centering on marker $id")

        viewModelScope.launch { state.centerOnMarker(id, 1f) }
    }
}
