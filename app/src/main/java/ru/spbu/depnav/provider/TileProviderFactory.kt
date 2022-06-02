package ru.spbu.depnav.provider

import android.content.res.AssetManager
import android.util.Log
import ovh.plrapps.mapcompose.core.TileStreamProvider

private const val TAG = "TileProviderFactory"

/**
 * A factory for creating [TileStreamProviders][TileStreamProvider] of tiles from a certain path.
 */
class TileProviderFactory(
    private val assets: AssetManager,
    /**
     * Root directory of the tiles from which [TileStreamProviders][TileStreamProvider] are created.
     * */
    var rootPath: String,
    /** Floor subdirectories prefix. */
    var floorPrefix: String = "floor",
    /** Name of a subdirectory with tiles for the dark theme. */
    var darkSubdirName: String = "dark",
    /** Name of a subdirectory with tiles for the light theme. */
    var lightSubdirName: String = "light"
) {
    /**
     * Returns a [TileStreamProvider] for the specified floor and theme.
     */
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
