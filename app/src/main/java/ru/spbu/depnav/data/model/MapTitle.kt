/**
 * DepNav -- department navigator.
 * Copyright (C) 2023  Timofei Pushkin
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

/**
 * Localized map title.
 */
@Entity(
    tableName = "map_title",
    primaryKeys = ["map_id", "language_id"],
    foreignKeys = [
        ForeignKey(
            entity = MapInfo::class,
            parentColumns = ["id"],
            childColumns = ["map_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class MapTitle(
    /** ID of the [MapInfo] describing the map whose title this is. */
    @ColumnInfo(name = "map_id") val mapId: Int,
    /** Language of this title. */
    @ColumnInfo(name = "language_id") val languageId: Language,
    /** Localized title of the map. */
    val title: String
)
