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
import ru.spbu.depnav.model.MarkerText

private const val TAG = "MapViewModel"

class MapScreenState : ViewModel() {
    var state by mutableStateOf(MapState(0, 0, 0))
        private set
    var usesDarkThemeTiles: Boolean? = null
        private set
    var currentFloor by mutableStateOf(Int.MIN_VALUE)

    var showUI by mutableStateOf(true)
        private set

    // These are separated because text needs to stay visible while hiding animation is playing
    var highlightMarker by mutableStateOf(false)
    var highlightedMarker by mutableStateOf<Pair<Marker, MarkerText>?>(null)
        private set

    fun setParams(width: Int, height: Int, tileSize: Int = 1024) {
        state.shutdown()
        state = MapState(1, width, height, tileSize) { scale(0f) }.apply {
            setScrollOffsetRatio(0.5f, 0.5f)
            onTap { _, _ ->
                if (!highlightMarker) showUI = !showUI
                highlightMarker = false
                highlightedMarker?.let { (marker, _) -> state.removeMarker(marker.idStr) }
            }
        }
    }

    private fun placeMarker(marker: Marker, markerText: MarkerText, isHighlighted: Boolean) {
        state.addMarker(
            id = marker.idStr,
            x = marker.x,
            y = marker.y,
            zIndex = if (isHighlighted) 1f else 0f,
            relativeOffset = Offset(-0.5f, -0.5f),
            clickable = false,
            clipShape = null
        ) {
            MarkerView(
                title = markerText.title ?: "",
                type = marker.type,
                isClosed = marker.isClosed,
                isHighlighted = isHighlighted,
                modifier =
                if (!markerText.title.isNullOrBlank() || !markerText.description.isNullOrBlank())
                    Modifier.clickable { highlightMarker(marker, markerText) }
                else Modifier
            )
        }
    }

    private fun highlightMarker(marker: Marker, markerText: MarkerText) {
        val newMarker = marker.copy(id = Int.MIN_VALUE) // Real IDs start from 1
        val newMarkerText = markerText.copy(markerId = newMarker.id)

        highlightedMarker?.let { (oldMarker, _) -> state.removeMarker(oldMarker.idStr) }

        showUI = true
        highlightedMarker = newMarker to newMarkerText
        highlightMarker = true

        placeMarker(newMarker, newMarkerText, isHighlighted = true)
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

        for ((marker, markerText) in markersWithText)
            placeMarker(marker, markerText, isHighlighted = false)
    }

    fun focusOnMarker(marker: Marker, markerText: MarkerText) {
        Log.d(TAG, "Centering on marker $marker")

        highlightMarker(marker, markerText)
        viewModelScope.launch { state.centerOnMarker(marker.idStr, 1f) }
    }
}
