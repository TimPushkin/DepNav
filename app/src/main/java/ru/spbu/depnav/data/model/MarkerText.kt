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

/** Text information related to a [Marker]. */
@Entity(
    tableName = "marker_text",
    primaryKeys = ["marker_id", "language_id"],
    foreignKeys = [
        ForeignKey(
            entity = Marker::class,
            parentColumns = ["id"],
            childColumns = ["marker_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class MarkerText(
    /** ID of the [Marker] to which this text relates. */
    @ColumnInfo(name = "marker_id") val markerId: Int,
    /** Language used in this text. */
    @ColumnInfo(name = "language_id") val languageId: Language,
    /** Title of the [Marker] to which this text relates. */
    val title: String?,
    /** Name of the location of the [Marker] to which this text relates. */
    val location: String?,
    /** Description of the [Marker] to which this text relates. */
    val description: String?
)
