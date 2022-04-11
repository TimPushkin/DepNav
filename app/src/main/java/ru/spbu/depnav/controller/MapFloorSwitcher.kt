package ru.spbu.depnav.controller

import android.util.Log
import kotlinx.coroutines.runBlocking
import ru.spbu.depnav.db.MarkerDao
import ru.spbu.depnav.model.Floor
import ru.spbu.depnav.provider.TileProviderFactory
import ru.spbu.depnav.ui.map.MapScreenState

private const val TAG = "MapFloorSwitcher"

class MapFloorSwitcher(
    private val mMapScreenState: MapScreenState,
    private val floors: Map<Int, Floor>
) {
    constructor(
        mapScreenState: MapScreenState,
        tileProviderFactory: TileProviderFactory,
        markerDao: MarkerDao,
        floorsNum: Int
    ) : this(
        mapScreenState,
        List(floorsNum) {
            val floor = it + 1
            floor to Floor(listOf(tileProviderFactory.makeTileProviderForFloor(floor))) {
                runBlocking { markerDao.loadWithTextByFloor(floor).keys } // TODO: fix blocking
            }
        }.toMap()
    )

    fun setFloor(floor: Int) {
        floors[floor]?.run {
            Log.i(
                TAG,
                "Switching to floor $floor: ${layers.count()} layers, ${markers.count()} markers"
            )

            mMapScreenState.currentFloor = floor
            mMapScreenState.replaceLayersWith(layers)
            mMapScreenState.replaceMarkersWith(markers)
        } ?: run { Log.e(TAG, "Cannot switch to the floor $floor which does not exist") }
    }
}
