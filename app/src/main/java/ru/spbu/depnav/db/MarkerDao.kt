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

package ru.spbu.depnav.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.model.MarkerText

/**
 * DAO for the table containing the available [Marker] entries.
 */
@Dao
interface MarkerDao {
    /**
     * Inserts the provided [Marker] entries into the database.
     */
    @Insert
    suspend fun insertAll(vararg markers: Marker)

    /**
     * Returns [Marker] entries with the provided ID and the corresponding [MarkerText] entries on
     * the specified language.
     */
    @Query(
        "SELECT *, marker_texts.lid FROM markers " +
                "JOIN marker_texts ON markers.id = marker_texts.marker_id " +
                "WHERE markers.id = :id AND marker_texts.lid = :language"
    )
    suspend fun loadWithTextById(
        id: Int,
        language: MarkerText.LanguageId
    ): Map<Marker, List<MarkerText>>

    /**
     * Returns [Marker] entries from the specified floor and the corresponding [MarkerText] entries
     * on the requested language.
     */
    @Query(
        "SELECT *, marker_texts.lid FROM markers " +
                "JOIN marker_texts ON markers.id = marker_texts.marker_id " +
                "WHERE markers.floor = :floor AND marker_texts.lid = :language"
    )
    suspend fun loadWithTextByFloor(
        floor: Int,
        language: MarkerText.LanguageId
    ): Map<Marker, List<MarkerText>>
}
