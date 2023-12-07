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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.centerOnMarker
import ovh.plrapps.mapcompose.api.disableRotation
import ovh.plrapps.mapcompose.api.enableRotation
import ovh.plrapps.mapcompose.api.maxScale
import ovh.plrapps.mapcompose.api.minScale
import ovh.plrapps.mapcompose.api.onMarkerClick
import ovh.plrapps.mapcompose.api.onTap
import ovh.plrapps.mapcompose.api.removeAllLayers
import ovh.plrapps.mapcompose.api.removeAllMarkers
import ovh.plrapps.mapcompose.api.removeMarker
import ovh.plrapps.mapcompose.api.rotateTo
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.api.setColorFilterProvider
import ovh.plrapps.mapcompose.api.setScrollOffsetRatio
import ovh.plrapps.mapcompose.api.shouldLoopScale
import ovh.plrapps.mapcompose.ui.state.MapState
import ru.spbu.depnav.data.composite.MarkerWithText
import ru.spbu.depnav.data.model.Language
import ru.spbu.depnav.data.model.MapInfo
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.toLanguage
import ru.spbu.depnav.data.preferences.PreferencesManager
import ru.spbu.depnav.data.repository.MapRepo
import ru.spbu.depnav.data.repository.MarkerWithTextRepo
import ru.spbu.depnav.ui.component.Pin
import ru.spbu.depnav.utils.map.Floor
import ru.spbu.depnav.utils.map.TileStreamProviderFactory
import ru.spbu.depnav.utils.map.addClusterers
import ru.spbu.depnav.utils.map.addMarker
import ru.spbu.depnav.utils.map.getMarkerAlpha
import javax.inject.Inject

private const val TAG = "MapViewModel"

private const val PIN_ID = "Pin" // Real IDs start with integers

/** View model for [MapState]-related parts of [MapScreen][ru.spbu.depnav.ui.screen.MapScreen]. */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModel @Inject constructor(
    private val tileStreamProviderFactory: TileStreamProviderFactory,
    private val mapRepo: MapRepo,
    private val markerRepo: MarkerWithTextRepo,
    prefs: PreferencesManager
) : ViewModel() {
    // Updated only on the main thread
    private val state = MutableStateFlow(PrivateState())

    /** UI-visible state. */
    val uiState = state.map { it.toMapUiState() }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = state.value.toMapUiState()
    )

    private data class PrivateState(
        val language: Language = Locale.current.toLanguage(),
        val availableMaps: List<AvailableMap> = emptyList(),
        val displayedMap: DisplayedMap? = null,
        val mapColor: Color = Color.Unspecified,
        val pinnedMarker: MarkerWithText? = null,
        val showOnMapUi: Boolean = false
    ) {
        data class DisplayedMap(
            val id: Int,
            val internalName: String,
            val mapState: MapState,
            val title: String,
            val floorsNum: Int,
            val floor: Floor
        ) {
            constructor(mapInfo: MapInfo, mapState: MapState, title: String, floor: Floor) :
                this(mapInfo.id, mapInfo.internalName, mapState, title, mapInfo.floorsNum, floor)
        }

        fun toMapUiState() = if (displayedMap == null) {
            MapUiState.Loading(availableMaps)
        } else {
            MapUiState.Ready(
                availableMaps,
                displayedMap.mapState,
                displayedMap.title,
                displayedMap.floorsNum,
                displayedMap.floor.number,
                pinnedMarker,
                showOnMapUi
            )
        }
    }

    private var _markerAlpha = MutableStateFlow(0f)

    /** Alpha of marker views and clusters. */
    val markerAlpha = _markerAlpha.asStateFlow()
    private var markerAlphaUpdater: Job = Job().apply { complete() }

    init {
        viewModelScope.launch {
            state.update {
                val availableMaps = withContext(Dispatchers.IO) {
                    mapRepo.loadAll(it.language).map { (info, title) ->
                        AvailableMap(info.id, info.internalName, title)
                    }
                }
                it.copy(availableMaps = availableMaps)
            }
        }

        prefs.selectedMapIdFlow
            .filterNotNull() // Can only be null initially, but not set to null by the user
            .onEach(::initMap)
            .launchIn(viewModelScope)

        state
            .mapNotNull { it.displayedMap?.mapState }
            .distinctUntilChanged() // State can change for other reasons
            .combine(prefs.enableRotationFlow) { a, b -> a to b }
            .mapLatest { (mapState, enableRotation) ->
                with(mapState) {
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
            .mapNotNull {
                val (mapState, color) = with(it) { displayedMap?.mapState to mapColor }
                if (mapState != null && color != Color.Unspecified) mapState to color else null
            }
            .distinctUntilChanged() // State can change for other reasons
            .onEach { (mapState, color) ->
                mapState.setColorFilterProvider { _, _, _ -> ColorFilter.tint(color) }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun initMap(mapId: Int) {
        Log.d(TAG, "Initializing map $mapId")
        val mapInfo = withContext(Dispatchers.IO) { mapRepo.loadInfoById(mapId) }

        var mapTitle: String
        var firstFloor: Floor
        do {
            val language = state.value.language
            mapTitle = mapRepo.loadTitleById(mapId, language)
            firstFloor = loadFloor(mapInfo.id, mapInfo.internalName, 1, language)
        } while (state.value.language != language)

        val mapState = with(mapInfo) {
            MapState(levelsNum, floorWidth, floorHeight, tileSize) { scale(0f) }
        }.apply {
            setScrollOffsetRatio(0.5f, 0.5f)
            addClusterers(markerAlpha)
            shouldLoopScale = true

            onTap { _, _ ->
                state.update {
                    if (it.pinnedMarker != null) {
                        removeMarker(PIN_ID)
                        it.copy(pinnedMarker = null)
                    } else {
                        it.copy(showOnMapUi = !it.showOnMapUi)
                    }
                }
            }

            onMarkerClick { id, _, _ ->
                val floor = checkNotNull(state.value.displayedMap) {
                    "Marker can be clicked only when a map is displayed"
                }.floor
                val markerWithText = checkNotNull(floor.markers[id]) {
                    "Marker outside of the current floor got clicked"
                }
                pinMarker(markerWithText.marker)
                state.update { it.copy(pinnedMarker = markerWithText, showOnMapUi = true) }
            }

            for (layer in firstFloor.layers) {
                addLayer(layer)
            }
            for (markerWithText in firstFloor.markers.values) {
                with(markerWithText) { addMarker(extendedId, marker, text, markerAlpha) }
            }

            markerAlphaUpdater.cancel("New map state arrived")
            markerAlphaUpdater = snapshotFlow { getMarkerAlpha(minScale, scale, maxScale) }
                .onEach { _markerAlpha.value = it }
                .launchIn(viewModelScope)
        }

        state.getAndUpdate {
            PrivateState(
                availableMaps = it.availableMaps,
                displayedMap = PrivateState.DisplayedMap(mapInfo, mapState, mapTitle, firstFloor),
                mapColor = it.mapColor,
                pinnedMarker = null,
                showOnMapUi = true
            )
        }.displayedMap?.mapState?.shutdown() // Shutdown the previous map state, if any

        Log.i(TAG, "Initialized map $mapInfo titled $mapTitle")
    }

    private suspend fun loadFloor(
        mapId: Int,
        internalMapName: String,
        floorNo: Int,
        language: Language
    ) = Floor(
        number = floorNo,
        layers = listOf(tileStreamProviderFactory.makeTileStreamProvider(internalMapName, floorNo)),
        markers = withContext(Dispatchers.IO) { markerRepo.loadByFloor(mapId, floorNo, language) }
            .associateBy { it.extendedId }
    )

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

    /** Asynchronously sets the color of map tiles. */
    fun setMapColor(color: Color) {
        if (state.value.mapColor != color) {
            state.update { it.copy(mapColor = color) }
            Log.i(TAG, "Updated map color to $color")
        }
    }

    /** Asynchronously sets the map floor to be displayed. */
    fun setFloor(floorNo: Int) {
        Log.d(TAG, "Switching to floor $floorNo")
        val mapState = checkNotNull(state.value.displayedMap?.mapState) { "No map to set floors" }
        viewModelScope.launch { mapState.setFloor(floorNo) }
    }

    private suspend fun MapState.setFloor(floorNo: Int) {
        var displayedMap = state.value.displayedMap.takeIf { it?.mapState == this } ?: return
        var floor: Floor
        do {
            val language = state.value.language
            floor = with(displayedMap) { loadFloor(id, internalName, floorNo, language) }
            displayedMap = state.value.displayedMap.takeIf { it?.mapState == this } ?: return
        } while (state.value.language != language)

        with(displayedMap.mapState) {
            removeAllMarkers()
            if (displayedMap.floor.number != floorNo) {
                removeAllLayers()
                for (layer in floor.layers) {
                    addLayer(layer)
                }
            }
            for (markerWithText in floor.markers.values) {
                with(markerWithText) { addMarker(extendedId, marker, text, markerAlpha) }
            }
        }
        state.update {
            it.copy(
                displayedMap = displayedMap.copy(floor = floor),
                pinnedMarker = it.pinnedMarker?.takeIf { displayedMap.floor.number == floorNo }
            )
        }

        Log.i(
            TAG,
            with(state.value) {
                "Changed floor of map ${displayedMap.internalName} to $floorNo on $language"
            }
        )
    }

    /** Asynchronously centers on the specified marker and pins it. */
    fun focusOnMarker(markerId: Int) {
        Log.d(TAG, "Focusing on marker $markerId")
        val mapState = checkNotNull(state.value.displayedMap?.mapState) { "No map to do focusing" }
        viewModelScope.launch {
            var markerWithText: MarkerWithText
            do {
                val language = state.value.language
                markerWithText = withContext(Dispatchers.IO) {
                    markerRepo.loadById(markerId, language)
                }
                mapState.setFloor(markerWithText.marker.floor)
                if (
                    state.value.displayedMap?.let {
                        it.mapState == mapState && it.floor.number == markerWithText.marker.floor
                    } != true
                ) {
                    return@launch // Map or floor got switched
                }
            } while (state.value.language != language)

            state.update { it.copy(pinnedMarker = markerWithText, showOnMapUi = true) }
            with(mapState) {
                pinMarker(markerWithText.marker)
                centerOnMarker(markerWithText.extendedId, maxScale)
            }
            Log.i(TAG, "Finished focusing on marker with text ${markerWithText.text}")
        }
    }

    /**
     * Asynchronously updates language of the map-related UI parts that come from the persisted
     * storage and thus are not updated automatically by Android.
     */
    fun onLocaleChange(locale: Locale) {
        val language = locale.toLanguage()
        if (state.value.language == language) {
            return
        }
        Log.d(TAG, "Updating language to $language")

        viewModelScope.launch {
            val newAvailableMaps = withContext(Dispatchers.IO) {
                mapRepo.loadAll(language).map { (info, title) ->
                    AvailableMap(info.id, info.internalName, title)
                }
            }

            while (true) {
                val stateSnapshot = state.value

                val (newDisplayedMap, newPinnedMarker) = stateSnapshot.displayedMap?.run {
                    val newMapTitle = mapRepo.loadTitleById(id, language)
                    val newFloor = loadFloor(id, internalName, floor.number, language)
                    val newPinnedMarker = stateSnapshot.pinnedMarker?.let { (marker) ->
                        newFloor.markers.values.first { it.marker.id == marker.id }
                    }
                    copy(title = newMapTitle, floor = newFloor) to newPinnedMarker
                } ?: (null to null)

                val currentState = state.value
                if (currentState.language == language) {
                    return@launch // The language has already been set
                }
                if (currentState.displayedMap?.let { it != stateSnapshot.displayedMap } == true ||
                    currentState.pinnedMarker?.let { it != stateSnapshot.pinnedMarker } == true) {
                    continue // State got changed significantly, need to try again
                }

                val newState = stateSnapshot.copy(
                    language = language,
                    availableMaps = newAvailableMaps,
                    displayedMap = newDisplayedMap.takeIf { currentState.displayedMap != null },
                    pinnedMarker = newPinnedMarker.takeIf { currentState.pinnedMarker != null }
                )
                state.value = newState

                newState.displayedMap?.run {
                    mapState.removeAllMarkers()
                    for (markerWithText in floor.markers.values) {
                        mapState.addMarker(
                            markerWithText.extendedId,
                            markerWithText.marker,
                            markerWithText.text,
                            markerAlpha
                        )
                    }
                }

                Log.i(TAG, "Updated language to $language")
                return@launch
            }
        }
    }
}

/** Describes states of map UI. */
sealed interface MapUiState {
    /** State that has a list of available maps. */
    sealed interface WithAvailableMaps : MapUiState {
        /** Maps available in the app. */
        val availableMaps: List<AvailableMap>
    }

    /** State in which a map has not yet been loaded. */
    data class Loading(
        override val availableMaps: List<AvailableMap> = emptyList()
    ) : WithAvailableMaps

    /** State in which a map has been loaded. */
    data class Ready(
        override val availableMaps: List<AvailableMap>,
        /** State of the map. */
        val mapState: MapState,
        /** Localized title of the map. */
        val mapTitle: String,
        /** Number of floors the current map has. */
        val floorsNum: Int,
        /** The floor currently displayed. */
        val currentFloor: Int,
        /** Latest pinned marker. */
        val pinnedMarker: MarkerWithText?,
        /** Whether any UI is displayed above the map. */
        val showOnMapUi: Boolean
    ) : WithAvailableMaps
}

/** Map description for the UI. */
data class AvailableMap(
    /** ID of the map. */
    val id: Int,
    /** Name needed to retrieve resources and assets for the map. */
    val internalName: String,
    /** Localized title of the map. */
    val title: String
)
