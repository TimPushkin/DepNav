package ru.spbu.depnav.viewmodel

import android.util.Log
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.*
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.ui.MarkerView

private const val TAG = "MapViewModel"

// TODO: instead of placing all markers, create a layer with marker graphics adding markers by one dynamically when needed to center and removing afterwards

class MapViewModel(
    private val width: Int,
    private val height: Int,
    tileSize: Int = 1024,
    initFloor: Int = 0
) : ViewModel() {
    var currentFloor by mutableStateOf(initFloor)
    val state by mutableStateOf(
        MapState(1, width, height, tileSize) {
            scroll(0.5, 0.5)
            scale(0f)
        }
    )

    private val mMarkerIds = mutableListOf<String>()

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
            state.addMarker(
                id = idStr,
                x = x,
                y = y,
                relativeOffset = Offset(-0.5f, -0.5f),
                clipShape = null
            ) { MarkerView(type, modifier = Modifier.size(20.dp)) }
            mMarkerIds += idStr
        }

        Log.i(TAG, "Placed ${mMarkerIds.size} markers")
    }

    fun centerOnMarker(id: String) {
        Log.d(TAG, "Centering on marker $id")

        viewModelScope.launch { state.centerOnMarker(id, 1f) }
    }
}
