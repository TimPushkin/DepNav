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
import androidx.room.Query
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.data.model.MarkerTextWithMatchInfo

/** DAO for the tables containing the available [Marker] and [MarkerText] entries. */
@Dao
interface MarkerWithTextDao {
    /**
     * Returns [Marker] entries with the provided ID and the corresponding [MarkerText] entries on
     * the specified language.
     */
    @Query(
        """
            SELECT *
            FROM marker
            JOIN marker_text ON marker.id = marker_text.marker_id
            WHERE marker.id = :id
                AND marker_text.language_id = :language
        """
    )
    suspend fun loadById(id: Int, language: MarkerText.LanguageId): Map<Marker, List<MarkerText>>

    /**
     * Returns [Marker] entries from the specified map and floor with the corresponding [MarkerText]
     * entries on the requested language.
     */
    @Query(
        """
            SELECT *
            FROM marker
                JOIN marker_text ON marker.id = marker_text.marker_id
            WHERE marker.map_name = :mapName
                AND marker.floor = :floor
                AND marker_text.language_id = :language
        """
    )
    suspend fun loadByFloor(
        mapName: String,
        floor: Int,
        language: MarkerText.LanguageId
    ): Map<Marker, List<MarkerText>>

    /**
     * Returns [MarkerTextWithMatchInfo] entries from the specified map containing the specified
     * tokens as a prefix on the specified language with the corresponding [Marker] entry.
     */
    @Query(
        """
            SELECT marker_text.*, match_info, marker.*
            FROM (
                SELECT docid, matchinfo(
                    marker_text_fts, '${MarkerTextWithMatchInfo.formatString}'
                ) AS match_info
                FROM marker_text_fts
                WHERE marker_text_fts MATCH :tokens
            ) AS fts_results
                JOIN marker_text ON fts_results.docid = marker_text.rowid
                JOIN marker ON marker_text.marker_id = marker.id
            WHERE marker.map_name = :mapName
                AND marker_text.language_id = :language;
        """
    )
    suspend fun loadByTokens(
        mapName: String,
        tokens: String,
        language: MarkerText.LanguageId
    ): Map<MarkerTextWithMatchInfo, List<Marker>>
}
