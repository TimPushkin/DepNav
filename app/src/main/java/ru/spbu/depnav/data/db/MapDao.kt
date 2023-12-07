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

package ru.spbu.depnav.data.db

import androidx.room.Dao
import androidx.room.Query
import ru.spbu.depnav.data.model.Language
import ru.spbu.depnav.data.model.MapInfo
import ru.spbu.depnav.data.model.MapTitle

/** DAO for [MapInfo] and [MapTitle] tables. */
@Dao
interface MapDao {
    /** Returns all [MapInfo]s with their corresponding [MapTitle]s on the specified language. */
    @Query(
        """
            SELECT *
            FROM map_info
                JOIN map_title ON map_info.id = map_title.map_id
            WHERE map_title.language_id = :language
            ORDER BY map_info.id ASC
        """
    )
    suspend fun loadAll(language: Language): Map<MapInfo, List<MapTitle>>

    /** Returns a [MapInfo] with the specified ID. */
    @Query("SELECT * FROM map_info WHERE id = :id")
    suspend fun loadInfoById(id: Int): MapInfo

    /** Returns [MapTitle] with the specified map ID and language. */
    @Query("SELECT * FROM map_title WHERE map_id = :mapId AND language_id = :language")
    suspend fun loadTitleByMapId(mapId: Int, language: Language): MapTitle
}
