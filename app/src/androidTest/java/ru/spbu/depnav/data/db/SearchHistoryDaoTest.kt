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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.spbu.depnav.data.model.MapInfo
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.SearchHistoryEntry

private val INSERTED_MAP = MapInfo("test map", 100, 100, 128, 5, 3)

/** Instrumentation tests for [SearchHistoryDao]. */
class SearchHistoryDaoTest : AppDatabaseDaoTest() {
    private lateinit var searchHistoryDao: SearchHistoryDao

    @Before
    override fun setUpDao() {
        searchHistoryDao = db.searchHistoryDao()
    }

    /** Fills database with a minimal set of entities to satisfy the constraints. */
    @Before
    fun preFillDb() {
        db.insertAll(listOf(INSERTED_MAP))
    }

    /** Checks that [SearchHistoryDao.loadByMap] returns entries for the specified map. */
    @Test
    fun loadByMap_returnsEntriesForSpecifiedMap() {
        val expectedMap = INSERTED_MAP.copy(name = "another map")
        db.insertAll(expectedMap)
        val expectedId = 2
        val unexpectedId = 1
        db.insertAll(
            Marker(unexpectedId, INSERTED_MAP.name, Marker.MarkerType.WC, true, 1, 0.0, 0.0),
            Marker(expectedId, expectedMap.name, Marker.MarkerType.WC, true, 1, 0.0, 0.0)
        )
        runBlocking {
            searchHistoryDao.insertNotExceeding(SearchHistoryEntry(unexpectedId, 1L), 10)
            searchHistoryDao.insertNotExceeding(SearchHistoryEntry(expectedId, 1L), 10)
        }

        val actual = runBlocking { searchHistoryDao.loadByMap(expectedMap.name).first() }

        assertTrue("Loaded entry list is empty", actual.isNotEmpty())
        actual.forEach { assertEquals(expectedId, it.markerId) }
    }

    private fun testInsertNotExceedingSingleMap(maxEntriesNum: Int, insertEntriesNum: Int) {
        val expected = mutableListOf<SearchHistoryEntry>()
        repeat(insertEntriesNum) { id ->
            db.insertAll(Marker(id, INSERTED_MAP.name, Marker.MarkerType.WC, true, 1, 0.0, 0.0))
            val entry = SearchHistoryEntry(id, id.toLong())
            runBlocking { searchHistoryDao.insertNotExceeding(entry, maxEntriesNum) }
            expected += entry
        }
        expected.sortBy { it.timestamp }
        while (expected.size > maxEntriesNum) expected.removeFirst()

        val actual = runBlocking { searchHistoryDao.loadByMap(INSERTED_MAP.name).first() }

        assertEquals(expected.size, actual.size)
        for (entry in actual) {
            assertTrue(
                "Entry $entry is not among expected entries",
                expected.contains(entry)
            )
        }
    }

    /**
     * Checks that [SearchHistoryDao.insertNotExceeding] inserts all passed entries when their total
     * amount is less than maxEntriesNum.
     */
    @Test
    fun insertNotExceedingLessThanMax_loadReturnsAllInserted() {
        val maxEntriesNum = 2
        testInsertNotExceedingSingleMap(maxEntriesNum, maxEntriesNum - 1)
    }

    /**
     * Checks that [SearchHistoryDao.insertNotExceeding] inserts all passed entries when their total
     * amount equals maxEntriesNum.
     */
    @Test
    fun insertNotExceedingExactlyMax_loadReturnsAllInserted() {
        val maxEntriesNum = 3
        testInsertNotExceedingSingleMap(maxEntriesNum, maxEntriesNum)
    }

    /**
     * Checks that [SearchHistoryDao.insertNotExceeding] inserts all passed entries and removes the
     * oldest one of them when their total amount is maxEntriesNum + 1.
     */
    @Test
    fun insertNotExceedingOneMoreThanMax_loadReturnsAllInsertedExceptOldest() {
        val maxEntriesNum = 5
        testInsertNotExceedingSingleMap(maxEntriesNum, maxEntriesNum + 1)
    }

    /**
     * Checks that [SearchHistoryDao.insertNotExceeding] inserts all passed entries and removes the
     * oldest of them when their total amount greatly exceeds maxEntriesNum.
     */
    @Test
    fun insertNotExceedingManyMoreThanMax_loadReturnsAllInsertedExceptOldest() {
        val maxEntriesNum = 5
        testInsertNotExceedingSingleMap(maxEntriesNum, maxEntriesNum + 5)
    }

    /**
     * Checks that [SearchHistoryDao.insertNotExceeding] count the maximum number of entries
     * separately for each map.
     * */
    @Test
    fun insertNotExceedingExactlyMaxForMultipleMaps_loadReturnsSameNumberAsInserted() {
        val anotherMap = INSERTED_MAP.copy(name = "another map")
        db.insertAll(anotherMap)
        val maxEntriesNum = 2
        for (id in 1..maxEntriesNum) {
            db.insertAll(Marker(id, INSERTED_MAP.name, Marker.MarkerType.WC, true, 1, 0.0, 0.0))
            runBlocking {
                searchHistoryDao.insertNotExceeding(SearchHistoryEntry(id, 1L), maxEntriesNum)
            }
        }
        for (id in maxEntriesNum + 1..2 * maxEntriesNum) {
            db.insertAll(Marker(id, anotherMap.name, Marker.MarkerType.WC, true, 1, 0.0, 0.0))
            runBlocking {
                searchHistoryDao.insertNotExceeding(SearchHistoryEntry(id, 1L), maxEntriesNum)
            }
        }

        val actual = runBlocking { searchHistoryDao.loadByMap(INSERTED_MAP.name).first() }

        assertEquals(maxEntriesNum, actual.size)
    }
}
