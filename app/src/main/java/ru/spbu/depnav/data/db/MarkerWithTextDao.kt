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

package ru.spbu.depnav.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText

/** DAO for the tables containing the available [Marker] and [MarkerText] entries. */
@Dao
interface MarkerWithTextDao {
    /** Inserts the provided [Marker] entries into the database. */
    @Insert
    suspend fun insertMarkers(markers: Collection<Marker>)

    /** Inserts the provided [MarkerText] entries into the database. */
    @Insert
    suspend fun insertMarkerTexts(markerTexts: Collection<MarkerText>)

    /**
     * Returns [Marker] entries with the provided ID and the corresponding [MarkerText] entries on
     * the specified language.
     */
    @Query(
        "SELECT * FROM markers " +
            "JOIN marker_texts ON markers.id = marker_texts.marker_id " +
            "WHERE markers.id = :id AND marker_texts.language_id = :language"
    )
    suspend fun loadById(id: Int, language: MarkerText.LanguageId): Map<Marker, List<MarkerText>>

    /**
     * Returns [Marker] entries from the specified floor and the corresponding [MarkerText] entries
     * on the requested language.
     */
    @Query(
        "SELECT * FROM markers " +
            "JOIN marker_texts ON markers.id = marker_texts.marker_id " +
            "WHERE markers.floor = :floor AND marker_texts.language_id = :language"
    )
    suspend fun loadByFloor(
        floor: Int,
        language: MarkerText.LanguageId
    ): Map<Marker, List<MarkerText>>

    /**
     * Returns [MarkerText] entries containing the specified tokens as a substring on the specified
     * language with the corresponding [Marker] entry.
     */
    @Query(
        "SELECT * FROM marker_texts " +
            "JOIN markers ON marker_texts.marker_id = markers.id " +
            "WHERE marker_texts MATCH :tokens AND marker_texts.language_id = :language"
    )
    suspend fun loadByTokens(
        tokens: String,
        language: MarkerText.LanguageId
    ): Map<MarkerText, List<Marker>>
}
