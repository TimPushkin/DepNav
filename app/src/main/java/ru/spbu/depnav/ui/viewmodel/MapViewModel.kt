/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofei Pushkin
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

package ru.spbu.depnav.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import ovh.plrapps.mapcompose.api.ExperimentalClusteringApi
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.centerOnMarker
import ovh.plrapps.mapcompose.api.disableRotation
import ovh.plrapps.mapcompose.api.enableRotation
import ovh.plrapps.mapcompose.api.maxScale
import ovh.plrapps.mapcompose.api.onMarkerClick
import ovh.plrapps.mapcompose.api.onTap
import ovh.plrapps.mapcompose.api.removeAllLayers
import ovh.plrapps.mapcompose.api.removeAllMarkers
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.api.rotateTo
import ovh.plrapps.mapcompose.api.setColorFilterProvider
import ovh.plrapps.mapcompose.api.setScrollOffsetRatio
import ovh.plrapps.mapcompose.api.shouldLoopScale
import ovh.plrapps.mapcompose.ui.state.MapState
import ovh.plrapps.mapcompose.ui.state.markers.model.RenderingStrategy
import ru.spbu.depnav.data.composite.MarkerWithText
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.data.model.SearchHistoryEntry
import ru.spbu.depnav.data.repository.MapInfoRepo
import ru.spbu.depnav.data.repository.MarkerWithTextRepo
import ru.spbu.depnav.data.repository.SearchHistoryRepo
import ru.spbu.depnav.ui.component.MarkerView
import ru.spbu.depnav.ui.component.Pin
import ru.spbu.depnav.utils.map.Floor
import ru.spbu.depnav.utils.map.TileStreamProviderFactory
import ru.spbu.depnav.utils.map.addClusterers
import ru.spbu.depnav.utils.map.getClustererId
import ru.spbu.depnav.utils.map.getMarkerAlpha
import ru.spbu.depnav.utils.misc.updateIf
import ru.spbu.depnav.utils.preferences.PreferencesManager
import javax.inject.Inject

private const val TAG = "MapScreenViewModel"

private const val MIN_QUERY_PERIOD_MS = 300L
private const val SEARCH_HISTORY_SIZE = 10

private const val PIN_ID = "Pin" // Real IDs start with integers

/** ViewModel for [MapScreen][ru.spbu.depnav.ui.screen.MapScreen]. */
@HiltViewModel
@OptIn(ExperimentalClusteringApi::class, ExperimentalCoroutinesApi::class, FlowPreview::class)
class MapViewModel @Inject constructor(
    private val tileStreamProviderFactory: TileStreamProviderFactory,
    private val mapInfoRepo: MapInfoRepo,
    private val markerRepo: MarkerWithTextRepo,
    private val searchHistoryRepo: SearchHistoryRepo,
    /** User preferences. */
    val prefs: PreferencesManager
) : ViewModel() {
    private val state = MutableStateFlow(MapViewModelState())

    /** State of the main map UI. */
    val mapUiState = state.map { it.toMapUiState() }

    /** State of marker search UI. */
    val searchUiState = state.map { it.toSearchUiState() }

    private data class MapViewModelState(
        val mapName: String? = null,
        val mapState: MapState? = null,
        val mapColor: Color = Color.Black,
        val floors: Map<Int, Floor> = emptyMap(),
        val currentFloorId: Int = 0,
        val pinnedMarker: MarkerWithText? = null,
        val showOnMapUi: Boolean = true,
        val searchQuery: String = "",
        val searchResults: List<MarkerWithText> = emptyList()
    ) {
        fun toMapUiState() =
            if (mapState == null) {
                MapUiState.Loading
            } else {
                MapUiState.Ready(
                    mapState = mapState,
                    floorsNum = floors.size,
                    currentFloor = currentFloorId,
                    pinnedMarker = pinnedMarker,
                    showOnMapUi = showOnMapUi
                )
            }

        fun toSearchUiState() = SearchUiState(query = searchQuery, results = searchResults)
    }

    init {
        snapshotFlow { prefs.selectedMap }
            .onEach { initMap(it.persistedName) }
            .launchIn(viewModelScope)

        snapshotFlow { prefs.enableRotation }
            .mapLatest { enableRotation ->
                state.value.mapState?.apply {
                    if (enableRotation) {
                        enableRotation()
                    } else {
                        disableRotation()
                        rotateTo(0f)
                    }
                }
            }
            .launchIn(viewModelScope)

        state
            .distinctUntilChangedBy { it.mapColor }
            .onEach {
                state.value.mapState?.setColorFilterProvider { _, _, _ ->
                    ColorFilter.tint(it.mapColor)
                }
            }
            .launchIn(viewModelScope)

        state
            .debounce(MIN_QUERY_PERIOD_MS)
            .filter { it.mapName != null }
            .distinctUntilChangedBy { it.searchQuery }
            .mapLatest { state ->
                val mapName = checkNotNull(state.mapName)
                val query = state.searchQuery
                val results = if (query.isNotEmpty()) {
                    markerRepo.loadByQuery(mapName, query)
                } else {
                    searchHistoryRepo.loadByMap(mapName).map { markerRepo.loadById(it.markerId) }
                }
                Log.d(TAG, "Searched '$query' on map $mapName, got ${results.size} results")
                mapName to results
            }
            .flowOn(Dispatchers.IO)
            .onEach { (searchedMapName, results) ->
                state.updateIf(condition = { it.mapName == searchedMapName }) {
                    it.copy(searchResults = results)
                }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun initMap(mapName: String) {
        Log.i(TAG, "Initializing map $mapName")

        val mapInfo = withContext(Dispatchers.IO) { mapInfoRepo.loadByName(mapName) }

        val mapState = with(mapInfo) {
            MapState(levelsNum, floorWidth, floorHeight, tileSize) { scale(0f) }
        }.apply {
            setScrollOffsetRatio(0.5f, 0.5f)
            setColorFilterProvider { _, _, _ -> ColorFilter.tint(state.value.mapColor) }
            addClusterers()
            shouldLoopScale = true

            if (prefs.enableRotation) {
                enableRotation()
            }

            onTap { _, _ ->
                state.updateIf(condition = { it.mapState == this }) { state ->
                    state.copy(
                        showOnMapUi = if (state.pinnedMarker != null) {
                            removeMarker(PIN_ID)
                            state.showOnMapUi
                        } else {
                            !state.showOnMapUi
                        },
                        pinnedMarker = null
                    )
                }
            }

            onMarkerClick { id, _, _ ->
                val currentState = state.value
                if (currentState.mapState != this) {
                    return@onMarkerClick
                }

                val floor = checkNotNull(currentState.run { floors[currentFloorId] }) {
                    "Illegal current floor"
                }
                val markerWithText = checkNotNull(floor.markers[id]) {
                    "Unknown marker $id clicked"
                }
                pinMarker(markerWithText.marker)

                state.compareAndSet(
                    currentState,
                    currentState.copy(pinnedMarker = markerWithText, showOnMapUi = true)
                )
            }
        }

        val floors = with(tileStreamProviderFactory) {
            List(mapInfo.floorsNum) { i ->
                val floorNum = i + 1
                val layers = listOf(makeTileStreamProvider(mapName, floorNum))
                val markers = withContext(Dispatchers.IO) {
                    markerRepo.loadByFloor(mapName, floorNum)
                }.associateBy { it.extendedId }
                floorNum to Floor(layers, markers)
            }.toMap()
        }

        state.value = MapViewModelState(
            mapName = mapName,
            mapState = mapState,
            floors = floors
        )

        setFloor(checkNotNull(floors.keys.firstOrNull()) { "Map has no floors" })
    }

    private fun MapState.pinMarker(marker: Marker) {
        removeMarker(PIN_ID)
        addMarker(
            id = PIN_ID,
            x = marker.x,
            y = marker.y,
            zIndex = 1f,
            clickable = false,
            relativeOffset = Offset(-0.5f, -0.5f),
            clipShape = null
        ) { Pin() }
    }

    /** Sets the color of map tiles. */
    fun setMapColor(color: Color) {
        state.update { it.copy(mapColor = color) }
    }

    /** Sets the map floor to display. */
    fun setFloor(floorId: Int) {
        val mapState = checkNotNull(state.value.mapState) { "No map to switch floors on" }
        viewModelScope.launch { mapState.setFloor(floorId) }
    }

    private val floorSwitchLock = Mutex()

    private suspend fun MapState.setFloor(floorId: Int) {
        // No need to worry about floors changing concurrently since then the map state would also
        // change and we check for that
        val floor = state.value.floors[floorId]
        if (floor == null) {
            Log.w(TAG, "Tried switch to floor $floorId which does not exist")
            return
        }

        // Using a lock because otherwise if this method is called twice concurrently with the same
        // parameter state.update {} will allow the same layers and markers to be added twice: CAS
        // will succeed for the second update since the state will have the value it expects from
        // the first update
        floorSwitchLock.withLock(floorId) {
            if (floorId == state.value.currentFloorId) {
                return
            }
            Log.i(TAG, "Switching to floor $floorId")

            removeAllLayers()
            removeAllMarkers()

            for (layer in floor.layers) {
                addLayer(layer)
            }
            for (markerWithText in floor.markers.values) {
                addMarker(markerWithText.extendedId, markerWithText.marker, markerWithText.text)
            }

            state.updateIf(condition = { it.mapState == this@setFloor }) {
                it.copy(currentFloorId = floorId, pinnedMarker = null)
            }

            Log.d(TAG, "Switched to floor $floorId")
        }
    }

    private fun MapState.addMarker(id: String, marker: Marker, markerText: MarkerText) {
        addMarker(
            id = id,
            x = marker.x,
            y = marker.y,
            clickable = markerText.run { !title.isNullOrBlank() || !description.isNullOrBlank() },
            relativeOffset = Offset(-0.5f, -0.5f),
            clipShape = null,
            renderingStrategy = RenderingStrategy.Clustering(getClustererId(marker.type))
        ) {
            val alpha = getMarkerAlpha()
            if (alpha > 0f) { // Not to consume clicks when invisible
                MarkerView(
                    title = markerText.title,
                    type = marker.type,
                    isClosed = marker.isClosed,
                    modifier = Modifier.alpha(alpha)
                )
            }
        }
    }

    /** Centers on the specified marker and pins it. */
    fun focusOnMarker(markerId: Int) {
        Log.d(TAG, "Focusing on marker $markerId")
        val mapState = checkNotNull(state.value.mapState) { "No map to do focusing on" }
        viewModelScope.launch {
            val markerWithText = withContext(Dispatchers.IO) { markerRepo.loadById(markerId) }
            mapState.setFloor(markerWithText.marker.floor) // Updates the state internally
            state.updateIf(
                condition = {
                    it.mapState == mapState && it.currentFloorId == markerWithText.marker.floor
                }
            ) { state ->
                with(mapState) {
                    pinMarker(markerWithText.marker)
                    launch { centerOnMarker(markerWithText.extendedId, maxScale) }
                }
                state.copy(pinnedMarker = markerWithText, showOnMapUi = true)
            }
        }
    }

    /** Runs marker search with the specified query. */
    fun searchForMarker(query: String) {
        state.update { it.copy(searchQuery = query) }
    }

    /** Adds the provided marker ID to the marker search history. */
    fun addToMarkerSearchHistory(markerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryRepo.insertNotExceeding(SearchHistoryEntry(markerId), SEARCH_HISTORY_SIZE)
        }
    }
}

/** Describes states of map UI. */
sealed interface MapUiState {
    /** Map has not yet been loaded. */
    data object Loading : MapUiState

    /** Map has been loaded. */
    data class Ready(
        /** State of the map. */
        val mapState: MapState,
        /** Number of floors the current map has. */
        val floorsNum: Int,
        /** The floor currently displayed. */
        val currentFloor: Int,
        /** Latest pinned marker. */
        val pinnedMarker: MarkerWithText?,
        /** Whether any UI is displayed above the map. */
        val showOnMapUi: Boolean
    ) : MapUiState
}

/** Map markers search UI state. */
data class SearchUiState(
    /** Marker search query entered by the user. */
    val query: String = "",
    /**
     * Either the actual results of the query or a history of previous searches if the query was
     * empty.
     *
     * Note that these result mey correspond not to the current query, but to some query in the
     * past.
     * */
    val results: List<MarkerWithText> = emptyList()
)
