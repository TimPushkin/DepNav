package ru.spbu.depnav.providers

import android.content.res.AssetManager
import android.util.Log
import ovh.plrapps.mapcompose.core.TileStreamProvider

private const val TAG = "TileProviderFactory"

class TileProviderFactory(
    private val assets: AssetManager,
    var rootPath: String,
    var maxFloor: Int,
    var minFloor: Int = 1,
    var floorPrefix: String = "floor"
) {

    fun makeTileProviderForFloor(floor: Int = minFloor) =
        TileStreamProvider { row, col, lvl ->
            if (floor < minFloor || floor > maxFloor) {
                Log.e(TAG, "Floor $floor is not in [minFloor, maxFloor] = [$minFloor, $maxFloor]")
                return@TileStreamProvider null
            }

            val path = "$rootPath/$floorPrefix$floor/$lvl/${row}_$col.png"

            runCatching {
                assets.open(path)
            }.onFailure {
                Log.e(TAG, "Failed to load a tile from $path", it)
            }.getOrNull()
        }
}
