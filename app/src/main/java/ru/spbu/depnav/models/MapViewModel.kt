package ru.spbu.depnav.models

import android.content.res.AssetManager
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.centerOnMarker
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import ru.spbu.depnav.providers.MarkerIconProvider

private const val TAG = "MapViewModel"

class MapViewModel(assets: AssetManager, tilesPath: String, markers: List<Marker>) : ViewModel() {
    private val markerIconProvider = MarkerIconProvider()
    private val tileStreamProvider =
        TileStreamProvider { row, col, lvl ->
            runCatching {
                assets.open("$tilesPath/$lvl/${row}_$col.png")
            }.getOrElse { e ->
                Log.e(TAG, "Failed to load a tile: lvl $lvl, row $row, col $col", e)
                null
            }
        }

    val state by mutableStateOf(
        MapState(1, 12288, 24576, 2048).apply {
            addLayer(tileStreamProvider)

            for (marker in markers) addMarker(marker.id, marker.x, marker.y) {
                markerIconProvider.getIcon(marker.type)
            }
        }
    )

    fun centerOnMarker(id: String) {
        viewModelScope.launch { state.centerOnMarker(id, 1f) }
    }
}
