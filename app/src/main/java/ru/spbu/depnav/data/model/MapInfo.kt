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

package ru.spbu.depnav.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Information about a map required to display it.
 */
@Entity(
    tableName = "map_info",
    indices = [Index("internal_name", unique = true)]
)
data class MapInfo(
    /** ID of this map. */
    // Not autogenerated because the ID is persisted in user preferences and thus must be monitored
    // to remain the same across releases
    @PrimaryKey val id: Int,
    /** Internal name used for data storage outside of the database. */
    @ColumnInfo(name = "internal_name") val internalName: String,
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
