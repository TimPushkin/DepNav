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

package ru.spbu.depnav.data.repository

import ru.spbu.depnav.data.db.MapInfoDao
import ru.spbu.depnav.data.model.MapInfo
import javax.inject.Inject

/** Repository for loading and saving [MapInfo] objects. */
class MapInfoRepo @Inject constructor(private val dao: MapInfoDao) {
    /** Saves the provided objects. */
    suspend fun insertAll(mapInfos: Collection<MapInfo>) = dao.insertAll(mapInfos)

    /** Loads a [MapInfo] by its name. */
    suspend fun loadByName(name: String) = dao.loadByName(name)
}
