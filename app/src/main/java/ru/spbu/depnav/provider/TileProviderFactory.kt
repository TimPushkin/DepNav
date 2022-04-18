package ru.spbu.depnav.provider

import android.content.res.AssetManager
import android.util.Log
import ovh.plrapps.mapcompose.core.TileStreamProvider

private const val TAG = "TileProviderFactory"

class TileProviderFactory(
    private val assets: AssetManager,
    var rootPath: String,
    var floorPrefix: String = "floor",
    var darkSubdirName: String = "dark",
    var lightSubdirName: String = "light"
) {
    fun makeTileProviderForFloor(floor: Int, isDark: Boolean): TileStreamProvider {
        val theme = if (isDark) darkSubdirName else lightSubdirName
        return TileStreamProvider { row, col, lvl ->
            val path = "$rootPath/$theme/$floorPrefix$floor/$lvl/${row}_$col.png"

            runCatching {
                assets.open(path)
            }.onFailure {
                Log.e(TAG, "Failed to load a tile from $path", it)
            }.getOrNull()
        }
    }
}
