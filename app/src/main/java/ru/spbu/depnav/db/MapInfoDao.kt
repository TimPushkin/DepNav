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
import ru.spbu.depnav.model.MapInfo

/**
 * DAO for the table containing the available [MapInfo] entries.
 */
@Dao
interface MapInfoDao {
    /**
     * Inserts the provided [MapInfo] entries into the database.
     */
    @Insert
    suspend fun insertAll(vararg mapInfos: MapInfo)

    /**
     * Returns a [MapInfo] with the provided map name.
     */
    @Query("SELECT * FROM map_infos WHERE map_name = :name")
    suspend fun loadByName(name: String): MapInfo
}
