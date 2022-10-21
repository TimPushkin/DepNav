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

package ru.spbu.depnav.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Information about a map required to display it.
 */
@Entity(tableName = "map_info")
data class MapInfo(
    /** Name of this map. */
    @PrimaryKey val name: String,
    /** Width of floors of this map in pixels. */
    @ColumnInfo(name = "floor_width") val floorWidth: Int,
    /** Height of floors of this map in pixels. */
    @ColumnInfo(name = "floor_height") val floorHeight: Int,
    /** Size of the sides of square tiles used in this map in pixels. */
    @ColumnInfo(name = "tile_size") val tileSize: Int,
    /** Number of levels of detail this map has. */
    @ColumnInfo(name = "levels_num") val levelsNum: Int,
    /** Number of floors on this map. */
    @ColumnInfo(name = "floors_num") val floorsNum: Int
)
