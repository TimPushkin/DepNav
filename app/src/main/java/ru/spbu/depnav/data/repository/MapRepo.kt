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

package ru.spbu.depnav.data.repository

import ru.spbu.depnav.data.db.MapDao
import ru.spbu.depnav.data.model.Language
import ru.spbu.depnav.data.model.MapInfo
import ru.spbu.depnav.data.model.MapTitle
import javax.inject.Inject

/** Repository for [MapInfo]s and [MapTitle]s. */
class MapRepo @Inject constructor(private val dao: MapDao) {
    /** Loads all [MapInfo]s with map titles on the specified language. */
    suspend fun loadAll(language: Language): List<Pair<MapInfo, String>> {
        return dao.loadAll(language).map { (info, title) ->
            info to checkNotNull(title.firstOrNull()) { "No title on $language for $info" }.title
        }
    }

    /** Loads a [MapInfo] with the specified ID. */
    suspend fun loadInfoById(id: Int) = dao.loadInfoById(id)

    /** Loads the title of the map with the specified ID on the specified language. */
    suspend fun loadTitleById(mapId: Int, language: Language) =
        dao.loadTitleByMapId(mapId, language).title
}
