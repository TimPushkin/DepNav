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
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Displayable marker. */
@Entity(
    tableName = "marker",
    foreignKeys = [
        ForeignKey(
            entity = MapInfo::class,
            parentColumns = ["id"],
            childColumns = ["map_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("map_id", "floor")]
)
data class Marker(
    /** ID of this marker. */
    @PrimaryKey(autoGenerate = true) val id: Int,
    /** Name of the map to which this marker belongs. */
    @ColumnInfo(name = "map_id") val mapId: Int,
    /** Type of this marker. */
    val type: MarkerType,
    /** Whether this marker indicates a closed object. */
    @ColumnInfo(name = "is_closed") val isClosed: Boolean,
    /** Number of the floor on which this marker is placed. */
    val floor: Int,
    /** X coordinate of this marker. */
    val x: Double,
    /** Y coordinate of this marker. */
    val y: Double
) {
    /** Type of an object represented by a [Marker]. */
    enum class MarkerType {
        /** Building entrance. */
        ENTRANCE,

        /** Room entrance. */
        ROOM,

        /** Staircase leading up. */
        STAIRS_UP,

        /** Staircase leading down. */
        STAIRS_DOWN,

        /** Staircase leading both up and down. */
        STAIRS_BOTH,

        /** Elevator entrance. */
        ELEVATOR,

        /** Men's restroom. */
        WC_MAN,

        /** Women's restroom. */
        WC_WOMAN,

        /** Restroom. */
        WC,

        /** Anything else. */
        OTHER
    }
}
