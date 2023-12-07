/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofei Pushkin
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

package ru.spbu.depnav.utils.map

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import ovh.plrapps.mapcompose.core.TileStreamProvider
import javax.inject.Inject

private const val TAG = "TileStreamProvider"

/** Factory for creating [TileStreamProviders][TileStreamProvider] for tiles from a certain path. */
@ViewModelScoped
class TileStreamProviderFactory(
    private val assets: AssetManager,
    private val tilesPath: String = "tiles",
    private val floorPrefix: String = "floor",
    private val tileExt: String = "webp"
) {
    @Inject
    constructor(@ApplicationContext context: Context) : this(context.assets)

    /** Returns a [TileStreamProvider] for the specified map floor. */
    suspend fun makeTileStreamProvider(mapName: String, floor: Int): TileStreamProvider {
        val tileExistenceMap = withContext(Dispatchers.IO) {
            async { buildTileExistenceMap(mapName, floor) }
        }
        return TileStreamProvider { row, col, lvl ->
            if (tileExistenceMap.await()[Triple(lvl, row, col)] == true) {
                Log.v(TAG, "Opening tile $lvl/$row/$col")
                assets.open("$tilesPath/$mapName/$floorPrefix$floor/$lvl/$row/$col.$tileExt")
            } else {
                Log.v(TAG, "Omitting tile $lvl/$row/$col")
                null
            }
        }
    }

    private fun buildTileExistenceMap(
        mapName: String,
        floor: Int
    ): Map<Triple<Int, Int, Int>, Boolean> {
        val tileExistenceMap = mutableMapOf<Triple<Int, Int, Int>, Boolean>()
        val floorDir = "$tilesPath/$mapName/$floorPrefix$floor"
        assets.list(floorDir)?.forEach { lvlDir ->
            val lvl = lvlDir.toInt()
            assets.list("$floorDir/$lvlDir")?.forEach { rowDir ->
                val row = rowDir.toInt()
                assets.list("$floorDir/$lvlDir/$rowDir")?.forEach { tile ->
                    val col = tile.removeSuffix(".$tileExt").toInt()
                    tileExistenceMap[Triple(lvl, row, col)] = true
                }
            }
        }
        return tileExistenceMap
    }
}
