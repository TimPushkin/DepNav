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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import ru.spbu.depnav.data.model.SearchHistoryEntry

/** DAO for [SearchHistoryEntry] table. */
@Dao
abstract class SearchHistoryDao {
    /**
     * Inserts the provided [SearchHistoryEntry] deleting the oldest ones if needed so that
     * [maxEntriesNum] is not exceeded for this entry's map after the insertion. If an entry with
     * the same marker ID is already inserted it is replaced.
     *
     * Note that the provided entry will not end up in the table if maxEntriesNum is exceeded after
     * the insertion and the provided entry is among the ones that are deleted.
     */
    @Transaction
    open suspend fun insertNotExceeding(entry: SearchHistoryEntry, maxEntriesNum: Int) {
        insert(entry)
        val mapId = loadMapIdByMarkerId(entry.markerId)
        repeat(loadEntriesNumFor(mapId) - maxEntriesNum) { deleteOldestFor(mapId) }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insert(entry: SearchHistoryEntry)

    @Query("SELECT map_id FROM marker WHERE id = :id")
    protected abstract suspend fun loadMapIdByMarkerId(id: Int): Int

    @Query(
        """
            SELECT count(*)
            FROM search_history_entry
                JOIN marker ON search_history_entry.marker_id = marker.id
            WHERE marker.map_id = :mapId
        """
    )
    protected abstract suspend fun loadEntriesNumFor(mapId: Int): Int

    @Query(
        """
            DELETE FROM search_history_entry
            WHERE timestamp = (
                SELECT min(timestamp)
                FROM search_history_entry
                    JOIN marker ON search_history_entry.marker_id = marker.id
                WHERE marker.map_id = :mapId
            )
        """
    )
    protected abstract suspend fun deleteOldestFor(mapId: Int)

    /**
     * Returns [SearchHistoryEntry]s for the specified map sorted by timestamps (oldest first).
     */
    @Query(
        """
            SELECT search_history_entry.*
            FROM search_history_entry
                JOIN marker ON search_history_entry.marker_id = marker.id
            WHERE marker.map_id = :mapId
            ORDER BY timestamp ASC
        """
    )
    abstract suspend fun loadByMap(mapId: Int): List<SearchHistoryEntry>
}
