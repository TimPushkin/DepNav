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
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/** Information about a marker that was searched. */
@Entity(
    tableName = "search_history_entry",
    foreignKeys = [
        ForeignKey(
            entity = Marker::class,
            parentColumns = ["id"],
            childColumns = ["marker_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class SearchHistoryEntry(
    /** ID of the searched marker. */
    @PrimaryKey
    @ColumnInfo(name = "marker_id")
    val markerId: Int,
    /**
     * The "wall clock" timestamp of the moment when the search for the marker was performed in
     * milliseconds.
     */
    val timestamp: Long = System.currentTimeMillis()
)
