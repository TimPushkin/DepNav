/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofey Pushkin
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

package ru.spbu.depnav.utils.tiles

import android.content.res.AssetManager
import android.util.Log
import ovh.plrapps.mapcompose.core.TileStreamProvider

private const val TAG = "TileProviderFactory"

/**
 * Factory for creating [TileStreamProviders][TileStreamProvider] of tiles from a certain path.
 */
class TileProviderFactory(
    private val assets: AssetManager,
    /**
     * Root directory of the tiles from which [TileStreamProviders][TileStreamProvider] are created.
     */
    var rootPath: String,
    /** Floor subdirectories prefix. */
    var floorPrefix: String = "floor"
) {
    /**
     * Returns a [TileStreamProvider] for the specified floor and theme.
     */
    fun makeTileProviderForFloor(floor: Int): TileStreamProvider {
        return TileStreamProvider { row, col, lvl ->
            val path = "$rootPath/$floorPrefix$floor/$lvl/${row}_$col.png"

            runCatching {
                assets.open(path)
            }.onFailure {
                Log.e(TAG, "Failed to load a tile from $path", it)
            }.getOrNull()
        }
    }
}
