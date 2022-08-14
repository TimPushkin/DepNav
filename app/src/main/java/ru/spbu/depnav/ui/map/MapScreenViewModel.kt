/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofey Pushkin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.spbu.depnav.ui.map

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.ExperimentalClusteringApi
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.addLazyLoader
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.centerOnMarker
import ovh.plrapps.mapcompose.api.maxScale
import ovh.plrapps.mapcompose.api.minScaleSnapshotFlow
import ovh.plrapps.mapcompose.api.onMarkerClick
import ovh.plrapps.mapcompose.api.onTap
import ovh.plrapps.mapcompose.api.removeAllLayers
import ovh.plrapps.mapcompose.api.removeAllMarkers
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.api.setColorFilterProvider
import ovh.plrapps.mapcompose.api.setScrollOffsetRatio
import ovh.plrapps.mapcompose.api.shouldLoopScale
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import ovh.plrapps.mapcompose.ui.state.markers.model.RenderingStrategy
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.model.MarkerText
import ru.spbu.depnav.utils.PreferencesManager
import javax.inject.Inject

private const val TAG = "MapViewModel"

private const val LAZY_LOADER_ID = "main"
private const val MIN_MARKER_VISIBILITY_SCALE = 0.2f
private const val MAX_MARKER_VISIBILITY_SCALE = 0.5f
private const val PIN_ID = "Pin" // Real ID are integers

/**
 * State of the [MapScreen].
 */
@OptIn(ExperimentalClusteringApi::class)
@HiltViewModel
class MapScreenViewModel @Inject constructor(val prefs: PreferencesManager) : ViewModel() {
    /**
     * State of the map currently displayed.
     */
    var mapState by mutableStateOf(MapState(0, 0, 0))
        private set

    /**
     * The floor currently displayed. Equals [Int.MIN_VALUE] by default.
     */
    var currentFloor by mutableStateOf(Int.MIN_VALUE)

    /**
     * Controls the color of map tiles.
     */
    var tileColor: Color = Color.Black
        set(value) {
            mapState.setColorFilterProvider { _, _, _ -> ColorFilter.tint(tileColor) }
            field = value
        }

    /**
     * Whether any UI is displayed on top of the map.
     */
    var showUI by mutableStateOf(true)
        private set

    /**
     * Whether a marker is pinned.
     *
     * It is separate from [pinnedMarker] because text needs to stay visible while hiding animation
     * is playing.
     */
    var isMarkerPinned by mutableStateOf(false)

    /**
     * The marker currently pinned.
     */
    var pinnedMarker by mutableStateOf<Pair<Marker, MarkerText>?>(null)
        private set

    private val clickableMarkers = mutableMapOf<String, Pair<Marker, MarkerText>>()

    private var minScaleCollectionJob: Job? = null
    private var minMarkerVisScale = 0f
    private val maxMarkerVisScale = mapState.maxScale.coerceAtMost(MAX_MARKER_VISIBILITY_SCALE)
    private val markerAlpha
        get() = (mapState.scale - minMarkerVisScale) / (maxMarkerVisScale - minMarkerVisScale)

    /**
     * Sets the parameters of the displayed map.
     */
    fun setParams(levelsNum: Int, width: Int, height: Int, tileSize: Int) {
        minScaleCollectionJob?.cancel("State changed")
        mapState.shutdown()

        mapState = MapState(levelsNum, width, height, tileSize) { scale(0f) }.apply {
            setScrollOffsetRatio(0.5f, 0.5f)
            setColorFilterProvider { _, _, _ -> ColorFilter.tint(tileColor) }
            addLazyLoader(LAZY_LOADER_ID, padding = 20.dp)
            shouldLoopScale = true

            onTap { _, _ ->
                if (isMarkerPinned) mapState.removeMarker(PIN_ID) else showUI = !showUI
                isMarkerPinned = false
            }

            onMarkerClick { id, _, _ ->
                Log.d(TAG, "Received a click on marker $id")
                clickableMarkers[id]?.let { (marker, markerText) -> pinMarker(marker, markerText) }
                    ?: Log.e(TAG, "Marker $id is not clickable")
            }
        }

        minScaleCollectionJob = mapState.minScaleSnapshotFlow()
            .onEach { minMarkerVisScale = it.coerceAtLeast(MIN_MARKER_VISIBILITY_SCALE) }
            .launchIn(viewModelScope)
    }

    private fun placeMarker(marker: Marker, markerText: MarkerText) {
        val isClickable =
            !markerText.title.isNullOrBlank() || !markerText.description.isNullOrBlank()

        if (isClickable) {
            if (clickableMarkers.containsKey(marker.idStr)) {
                Log.e(TAG, "Adding a clickable marker ${marker.idStr} which is already added")
            }
            clickableMarkers[marker.idStr] = marker to markerText
        }

        mapState.addMarker(
            id = marker.idStr,
            x = marker.x,
            y = marker.y,
            clickable = isClickable,
            relativeOffset = Offset(-0.5f, -0.5f),
            clipShape = null,
            renderingStrategy = RenderingStrategy.LazyLoading(LAZY_LOADER_ID)
        ) {
            if (markerAlpha <= 0) return@addMarker // Not to consume clicks

            MarkerView(
                title = markerText.title ?: "",
                type = marker.type,
                isClosed = marker.isClosed,
                modifier = Modifier.graphicsLayer(alpha = markerAlpha)
            )
        }
    }

    private fun pinMarker(marker: Marker, markerText: MarkerText) {
        pinnedMarker = marker to markerText
        showUI = true
        isMarkerPinned = true

        mapState.removeMarker(PIN_ID)
        mapState.addMarker(
            id = PIN_ID,
            x = marker.x,
            y = marker.y,
            zIndex = 1f,
            clickable = false,
            relativeOffset = Offset(-0.5f, -0.5f),
            clipShape = null
        ) { Pin() }
    }

    /**
     * Replaces the currently displayed layers.
     */
    fun replaceLayersWith(tileProviders: Iterable<TileStreamProvider>) {
        Log.d(TAG, "Replacing layers...")

        mapState.removeAllLayers()
        for (tileProvider in tileProviders) mapState.addLayer(tileProvider)
    }

    /**
     * Replaces the currently displayed markers.
     */
    fun replaceMarkersWith(markersWithText: Map<Marker, MarkerText>) {
        Log.d(TAG, "Replacing markers...")

        clickableMarkers.clear()
        mapState.removeAllMarkers()

        for ((marker, markerText) in markersWithText) placeMarker(marker, markerText)
    }

    /**
     * Centers on the specified marker and highlights it.
     */
    fun focusOnMarker(marker: Marker, markerText: MarkerText) {
        Log.d(TAG, "Centering on marker $marker")

        pinMarker(marker, markerText)
        viewModelScope.launch { mapState.centerOnMarker(marker.idStr, 1f) }
    }
}
