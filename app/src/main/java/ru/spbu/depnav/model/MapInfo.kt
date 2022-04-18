package ru.spbu.depnav.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "map_infos")
data class MapInfo(
    @PrimaryKey @ColumnInfo(name = "map_name") val mapName: String,
    @ColumnInfo(name = "floor_width") val floorWidth: Int,
    @ColumnInfo(name = "floor_height") val floorHeight: Int,
    @ColumnInfo(name = "tile_size") val tileSize: Int,
    @ColumnInfo(name = "floors_num") val floorsNum: Int
)
