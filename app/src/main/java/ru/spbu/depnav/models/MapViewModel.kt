package ru.spbu.depnav.models

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.addLayer
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.api.centerOnMarker
import ovh.plrapps.mapcompose.api.replaceLayer
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ovh.plrapps.mapcompose.ui.state.MapState
import ru.spbu.depnav.providers.MarkerIconProvider

private const val TAG = "MapViewModel"

class MapViewModel(
    width: Int,
    height: Int,
    tileSize: Int = 1024,
    tileProvider: TileStreamProvider,
    markers: List<Marker>
) : ViewModel() {
    private val mMarkerIconProvider = MarkerIconProvider()
    private var mPrimaryLayerId: String

    val state by mutableStateOf(
        MapState(1, width, height, tileSize) {
            scroll(0.5, 0.5)
            scale(0f)
        }.apply {
            mPrimaryLayerId = addLayer(tileProvider)

            for (marker in markers) addMarker(marker.id, marker.x, marker.y) {
                mMarkerIconProvider.getIcon(marker.type)
            }
        }
    )

    fun changeTileProvider(tileProvider: TileStreamProvider) {
        mPrimaryLayerId = state.replaceLayer(mPrimaryLayerId, tileProvider) ?: run {
            Log.w(TAG, "Primary layer id was null")
            state.addLayer(tileProvider)
        }
    }

    fun centerOnMarker(id: String) {
        viewModelScope.launch { state.centerOnMarker(id, 1f) }
    }
}
