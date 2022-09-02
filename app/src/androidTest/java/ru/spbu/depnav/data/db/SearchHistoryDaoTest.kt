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
import ru.spbu.depnav.data.model.SearchHistoryEntry

/** Instrumentation tests for [SearchHistoryDao]. */
class SearchHistoryDaoTest : AppDatabaseDaoTest() {
    private lateinit var searchHistoryDao: SearchHistoryDao

    @Before
    override fun setUpDao() {
        searchHistoryDao = db.searchHistoryDao()
    }

    private fun testInsertNotExceeding(maxEntriesNum: Int, insertEntriesNum: Int) {
        val expected = mutableListOf<SearchHistoryEntry>()
        runBlocking {
            repeat(insertEntriesNum) { index ->
                val entry = SearchHistoryEntry(index, index.toLong())
                searchHistoryDao.insertNotExceeding(entry, maxEntriesNum)
                expected += entry
            }
        }
        expected.sortBy { it.timestamp }
        while (expected.size > maxEntriesNum) expected.removeFirst()

        val actual = runBlocking { searchHistoryDao.loadAll().first() }

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
    fun insertNotExceedingLessThanMax_loadAllReturnsAllInserted() {
        val maxEntriesNum = 2
        testInsertNotExceeding(maxEntriesNum, maxEntriesNum - 1)
    }

    /**
     * Checks that [SearchHistoryDao.insertNotExceeding] inserts all passed entries when their total
     * amount equals maxEntriesNum.
     */
    @Test
    fun insertNotExceedingExactlyMax_loadAllReturnsAllInserted() {
        val maxEntriesNum = 3
        testInsertNotExceeding(maxEntriesNum, maxEntriesNum)
    }

    /**
     * Checks that [SearchHistoryDao.insertNotExceeding] inserts all passed entries and removes the
     * oldest one of them when their total amount is maxEntriesNum + 1.
     */
    @Test
    fun insertNotExceedingOneMoreThanMax_loadAllReturnsAllInsertedExceptOldest() {
        val maxEntriesNum = 5
        testInsertNotExceeding(maxEntriesNum, maxEntriesNum + 1)
    }

    /**
     * Checks that [SearchHistoryDao.insertNotExceeding] inserts all passed entries and removes the
     * oldest of them when their total amount greatly exceeds maxEntriesNum.
     */
    @Test
    fun insertNotExceedingManyMoreThanMax_loadAllReturnsAllInsertedExceptOldest() {
        val maxEntriesNum = 5
        testInsertNotExceeding(maxEntriesNum, maxEntriesNum + 5)
    }
}
