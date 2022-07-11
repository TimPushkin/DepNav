package ru.spbu.depnav.ui.map

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import ovh.plrapps.mapcompose.api.setScrollOffsetRatio
import ovh.plrapps.mapcompose.api.shouldLoopScale
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import ovh.plrapps.mapcompose.ui.state.markers.model.RenderingStrategy
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.model.MarkerText

private const val TAG = "MapViewModel"

private const val LAZY_LOADER_ID = "main"
private const val MIN_MARKER_VISIBILITY_SCALE = 0.2f
private const val MAX_MARKER_VISIBILITY_SCALE = 0.5f

/**
 * State of the [MapScreen].
 */
@OptIn(ExperimentalClusteringApi::class)
class MapScreenState : ViewModel() {
    /**
     * State of the map currently displayed.
     */
    var state by mutableStateOf(MapState(0, 0, 0))
        private set

    /**
     * Whether the map currently displayed is in dark theme.
     */
    var usesDarkThemeTiles: Boolean? = null
        private set

    /**
     * The floor currently displayed. Equals [Int.MIN_VALUE] by default.
     */
    var currentFloor by mutableStateOf(Int.MIN_VALUE)

    /**
     * Whether any UI is displayed on top of the map.
     */
    var showUI by mutableStateOf(true)
        private set

    /**
     * Whether a marker is highlighted.
     *
     * It is separate from [highlightedMarker] because text needs to stay visible while hiding
     * animation is playing.
     */
    var highlightMarker by mutableStateOf(false)

    /**
     * The marker currently highlighted.
     */
    var highlightedMarker by mutableStateOf<Pair<Marker, MarkerText>?>(null)
        private set

    private val clickableMarkers = mutableMapOf<String, Pair<Marker, MarkerText>>()

    private var minScaleCollectionJob: Job? = null
    private var minMarkerVisScale = 0f
    private val maxMarkerVisScale = state.maxScale.coerceAtMost(MAX_MARKER_VISIBILITY_SCALE)
    private val markerAlpha
        get() = (state.scale - minMarkerVisScale) / (maxMarkerVisScale - minMarkerVisScale)

    /**
     * Sets the parameters of the displayed map.
     */
    fun setParams(levelsNum: Int, width: Int, height: Int, tileSize: Int) {
        minScaleCollectionJob?.cancel("State changed")
        state.shutdown()

        state = MapState(levelsNum, width, height, tileSize) { scale(0f) }.apply {
            setScrollOffsetRatio(0.5f, 0.5f)
            addLazyLoader(LAZY_LOADER_ID, padding = 20.dp)
            shouldLoopScale = true

            onTap { _, _ ->
                if (!highlightMarker) showUI = !showUI
                highlightMarker = false
                highlightedMarker?.let { (marker, _) ->
                    clickableMarkers.remove(marker.idStr)
                    state.removeMarker(marker.idStr)
                }
            }

            onMarkerClick { id, _, _ ->
                if (markerAlpha <= 0) return@onMarkerClick
                Log.d(TAG, "Received a click on marker $id")
                clickableMarkers[id]?.let { (marker, markerText) ->
                    highlightMarker(marker, markerText)
                } ?: Log.e(TAG, "Marker $id is not clickable")
            }
        }

        minScaleCollectionJob = state.minScaleSnapshotFlow()
            .onEach { minMarkerVisScale = it.coerceAtLeast(MIN_MARKER_VISIBILITY_SCALE) }
            .launchIn(viewModelScope)
    }

    private fun placeMarker(marker: Marker, markerText: MarkerText, isHighlighted: Boolean) {
        val isClickable =
            !markerText.title.isNullOrBlank() || !markerText.description.isNullOrBlank()

        if (isClickable) {
            if (clickableMarkers.containsKey(marker.idStr)) {
                Log.e(TAG, "Adding a clickable marker ${marker.idStr} which is already added")
            }
            clickableMarkers[marker.idStr] = marker to markerText
        }

        state.addMarker(
            id = marker.idStr,
            x = marker.x,
            y = marker.y,
            zIndex = if (isHighlighted) 1f else 0f,
            clickable = isClickable,
            relativeOffset = Offset(-0.5f, -0.5f),
            clipShape = null,
            renderingStrategy = RenderingStrategy.LazyLoading(LAZY_LOADER_ID)
        ) {
            if (!isHighlighted && markerAlpha <= 0) return@addMarker // Not to consume clicks

            MarkerView(
                title = markerText.title ?: "",
                type = marker.type,
                isClosed = marker.isClosed,
                isHighlighted = isHighlighted,
                modifier =
                if (!isHighlighted) {
                    Modifier.graphicsLayer(alpha = markerAlpha)
                } else {
                    Modifier
                }
            )
        }
    }

    private fun highlightMarker(marker: Marker, markerText: MarkerText) {
        if (marker.id <= 0) { // Highlighting twice will result in occupying an ID twice
            if (highlightedMarker?.first?.id == marker.id) {
                Log.d(TAG, "Marker ${marker.id} (${markerText.title}) is already highlighted")
            } else {
                Log.e(TAG, "Unhighlighted marker IDs must start from 1, but got ${marker.id}")
            }
            return
        }

        val newMarker = marker.copy(id = -marker.id) // TODO: replace with fixed ID
        val newMarkerText = markerText.copy(markerId = newMarker.id)

        highlightedMarker?.let { (oldMarker, _) ->
            clickableMarkers.remove(oldMarker.idStr) // Not to trigger "double adding" logs
            state.removeMarker(oldMarker.idStr)
        }

        highlightedMarker = newMarker to newMarkerText
        showUI = true
        highlightMarker = true

        placeMarker(newMarker, newMarkerText, isHighlighted = true)
    }

    /**
     * Replaces the currently displayed layers.
     */
    fun replaceLayersWith(tileProviders: Iterable<TileStreamProvider>, isDark: Boolean) {
        Log.d(TAG, "Replacing layers...")

        state.removeAllLayers()
        for (tileProvider in tileProviders) state.addLayer(tileProvider)
        usesDarkThemeTiles = isDark
    }

    /**
     * Replaces the currently displayed markers.
     */
    fun replaceMarkersWith(markersWithText: Map<Marker, MarkerText>) {
        Log.d(TAG, "Replacing markers...")

        clickableMarkers.clear()
        state.removeAllMarkers()

        for ((marker, markerText) in markersWithText) {
            placeMarker(marker, markerText, isHighlighted = false)
        }
    }

    /**
     * Centers on the specified marker and highlights it.
     */
    fun focusOnMarker(marker: Marker, markerText: MarkerText) {
        Log.d(TAG, "Centering on marker $marker")

        highlightMarker(marker, markerText)
        viewModelScope.launch { state.centerOnMarker(marker.idStr, 1f) }
    }
}
