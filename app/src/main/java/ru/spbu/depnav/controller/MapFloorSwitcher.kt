package ru.spbu.depnav.controller

import android.util.Log
import ru.spbu.depnav.model.Floor
import ru.spbu.depnav.viewmodel.MapViewModel

private const val TAG = "MapFloorSwitcher"

class MapFloorSwitcher(
    private val mMapViewModel: MapViewModel,
    private val floors: Map<Int, Floor>
) {

    fun setFloor(floor: Int) {
        floors[floor]?.run {
            Log.i(TAG, "Switching to floor $floor")

            mMapViewModel.replaceLayersWith(layers)
            mMapViewModel.replaceMarkersWith(markers)
        } ?: run { Log.e(TAG, "Cannot switch to the floor $floor which does not exist") }
    }
}
