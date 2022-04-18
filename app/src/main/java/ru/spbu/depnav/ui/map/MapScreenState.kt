package ru.spbu.depnav.ui.map

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.*
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.model.MarkerInfo
import ru.spbu.depnav.model.MarkerText

private const val TAG = "MapViewModel"

// TODO: instead of placing all markers, create a layer with marker graphics adding markers by one dynamically when needed to center and removing afterwards

class MapScreenState : ViewModel() {
    var state by mutableStateOf(MapState(0, 0, 0))
        private set
    var usesDarkThemeTiles: Boolean? = null
        private set
    var currentFloor by mutableStateOf(1)

    var displayMarkerInfo by mutableStateOf(false)
    var displayedMarkerInfo by mutableStateOf(MarkerInfo(MarkerText.EMPTY, false))
        private set

    fun setParams(width: Int, height: Int, tileSize: Int = 1024) {
        state.shutdown()
        state = MapState(1, width, height, tileSize) {
            scroll(0.5, 0.5)
            scale(0f)
        }.apply { onTap { _, _ -> displayMarkerInfo = false } }
    }

    fun replaceLayersWith(tileProviders: Iterable<TileStreamProvider>, isDark: Boolean) {
        Log.d(TAG, "Replacing layers...")

        state.removeAllLayers()
        for (tileProvider in tileProviders) state.addLayer(tileProvider)
        usesDarkThemeTiles = isDark
    }

    fun replaceMarkersWith(markersWithText: Map<Marker, MarkerText>) {
        Log.d(TAG, "Replacing markers...")

        state.removeAllMarkers()

        for ((marker, markerText) in markersWithText) {
            state.addMarker(
                id = marker.idStr,
                x = marker.x,
                y = marker.y,
                relativeOffset = Offset(-0.5f, -0.5f),
                clickable = false,
                clipShape = null
            ) {
                val modifier =
                    if (!markerText.title.isNullOrBlank() || !markerText.description.isNullOrBlank())
                        Modifier.clickable {
                            displayedMarkerInfo = MarkerInfo(markerText, marker.isClosed)
                            displayMarkerInfo = true
                        }
                    else Modifier

                MarkerView(
                    markerText.title ?: "",
                    marker.type,
                    marker.isClosed,
                    modifier = modifier
                )
            }
        }
    }

    fun centerOnMarker(id: String) {
        Log.d(TAG, "Centering on marker $id")

        viewModelScope.launch { state.centerOnMarker(id, 1f) }
    }
}
