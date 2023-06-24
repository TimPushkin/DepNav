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

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.spbu.depnav.data.model.MapInfo
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.data.model.MarkerTextFts
import ru.spbu.depnav.data.model.SearchHistoryEntry

/** Room database containing information about maps and their markers. */
@Database(
    entities = [
        MapInfo::class, Marker::class, MarkerText::class, MarkerTextFts::class,
        SearchHistoryEntry::class
    ],
    version = 8
)
abstract class AppDatabase : RoomDatabase() {
    /** DAO for the table containing information about the available maps. */
    abstract fun mapInfoDao(): MapInfoDao

    /** DAO for the tables containing markers description and marker texts. */
    abstract fun markerWithTextDao(): MarkerWithTextDao

    /** DAO for the table containing search history entries. */
    abstract fun searchHistoryDao(): SearchHistoryDao
}
