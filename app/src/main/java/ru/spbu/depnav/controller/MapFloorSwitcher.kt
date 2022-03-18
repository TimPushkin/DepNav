package ru.spbu.depnav.controller

import android.util.Log
import kotlinx.coroutines.runBlocking
import ru.spbu.depnav.db.MarkerDao
import ru.spbu.depnav.model.Floor
import ru.spbu.depnav.provider.TileProviderFactory
import ru.spbu.depnav.viewmodel.MapViewModel

private const val TAG = "MapFloorSwitcher"

class MapFloorSwitcher(
    private val mMapViewModel: MapViewModel,
    private val floors: Map<Int, Floor>
) {
    constructor(
        mapViewModel: MapViewModel,
        tileProviderFactory: TileProviderFactory,
        markerDao: MarkerDao,
        floorNum: Int
    ) : this(
        mapViewModel,
        List(floorNum) {
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

            mMapViewModel.replaceLayersWith(layers)
            mMapViewModel.replaceMarkersWith(markers)
        } ?: run { Log.e(TAG, "Cannot switch to the floor $floor which does not exist") }
    }
}
