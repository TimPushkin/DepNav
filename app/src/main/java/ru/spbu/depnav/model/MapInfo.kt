package ru.spbu.depnav.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Information about a map required to display it.
 */
@Entity(tableName = "map_infos")
data class MapInfo(
    /** Name of this map. */
    @PrimaryKey
    @ColumnInfo(name = "map_name")
    val mapName: String,
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
