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
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import ru.spbu.depnav.data.model.SearchHistoryEntry

/** DAO fot the table containing [marker search history entries][SearchHistoryEntry]. */
@Dao
abstract class SearchHistoryDao {
    /**
     * Inserts the provided [SearchHistoryEntry] deleting the oldest ones if needed so that
     * [maxEntriesNum] is not exceeded after the insertion. If an entry with the same marker ID is
     * already inserted it is replaced.
     *
     * Note that the provided entry will not end up in the table if maxEntriesNum is exceeded after
     * the insertion and the provided entry is among the ones that are deleted.
     */
    @Transaction
    open suspend fun insertNotExceeding(entry: SearchHistoryEntry, maxEntriesNum: Int) {
        insert(entry)
        repeat(getEntriesNum() - maxEntriesNum) { deleteOldest() }
    }

    @Query("SELECT COUNT(*) FROM search_history")
    protected abstract suspend fun getEntriesNum(): Int

    @Query(
        "DELETE FROM search_history WHERE timestamp = (SELECT MIN(timestamp) FROM search_history)"
    )
    protected abstract suspend fun deleteOldest()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(entry: SearchHistoryEntry)

    /** Returns all [marker search history entries][SearchHistoryEntry]. */
    @Query("SELECT * FROM search_history ORDER BY timestamp DESC")
    abstract fun loadAll(): Flow<List<SearchHistoryEntry>>
}
