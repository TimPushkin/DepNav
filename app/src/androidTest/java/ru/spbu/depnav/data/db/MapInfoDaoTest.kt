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

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.spbu.depnav.data.model.MapInfo

/** Instrumentation tests for [MapInfoDao]. */
class MapInfoDaoTest : AppDatabaseDaoTest() {
    private lateinit var mapInfoDao: MapInfoDao

    @Before
    override fun setUpDao() {
        mapInfoDao = db.mapInfoDao()
    }

    /** Checks that [MapInfoDao.loadByName] returns a [MapInfo] with the queried name. */
    @Test
    fun loadByName_returnsMapInfoWithQueriedName() {
        val expected = listOf(
            MapInfo("abc", 100, 100, 1024, 1, 5),
            MapInfo("cba", 100, 200, 512, 2, 100)
        )
        db.insertAll(expected)

        val actual = runBlocking { expected.map { mapInfoDao.loadByName(it.name) } }

        for ((exp, act) in expected.zip(actual)) assertEquals(exp, act)
    }
}
