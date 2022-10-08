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

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import ovh.plrapps.mapcompose.core.TileStreamProvider
import javax.inject.Inject

private const val TAG = "TileProviderFactory"

/** Factory for creating [TileStreamProviders][TileStreamProvider] for tiles from a certain path. */
@ViewModelScoped
class TileStreamProviderFactory(
    private val assets: AssetManager,
    private val tilesPath: String = "tiles",
    private val floorPrefix: String = "floor"
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(context.assets)

    /** Returns a [TileStreamProvider] for the specified floor and theme. */
    fun makeTileStreamProvider(mapName: String, floor: Int) = TileStreamProvider { row, col, lvl ->
        val path = "$tilesPath/$mapName/$floorPrefix$floor/$lvl/${row}_$col.png"

        runCatching {
            assets.open(path)
        }.onFailure {
            Log.e(TAG, "Failed to load a tile from $path", it)
        }.getOrNull()
    }
}
