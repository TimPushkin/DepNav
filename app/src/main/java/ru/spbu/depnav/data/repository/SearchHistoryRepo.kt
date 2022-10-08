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

import ru.spbu.depnav.data.db.SearchHistoryDao
import ru.spbu.depnav.data.model.SearchHistoryEntry
import javax.inject.Inject

/** Repository for [SearchHistoryEntry] objects. */
class SearchHistoryRepo @Inject constructor(private val dao: SearchHistoryDao) {
    /**
     * Saves the provided entry and deletes the oldest ones if needed so that [maxEntriesNum] is not
     * exceeded for the corresponding map. If an entry with the same marker ID is already saved it
     * is replaced.
     *
     * Note that the provided entry will not be saved if maxEntriesNum is exceeded and the provided
     * entry is among the ones that are deleted.
     */
    suspend fun insertNotExceeding(entry: SearchHistoryEntry, maxEntriesNum: Int) =
        dao.insertNotExceeding(entry, maxEntriesNum)

    /** Loads the current entries for the specified map sorted by timestamps (older first). */
    fun loadByMap(mapName: String) = dao.loadByMap(mapName)
}
