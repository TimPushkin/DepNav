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

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.spbu.depnav.data.model.Language
import ru.spbu.depnav.data.model.MapInfo
import ru.spbu.depnav.data.model.MapTitle

/** Instrumentation tests for [MapDao]. */
class MapDaoTest : AppDatabaseDaoTest() {
    private lateinit var mapDao: MapDao

    @Before
    override fun setUpDao() {
        mapDao = db.mapInfoDao()
    }

    /**
     * Checks that [MapDao.loadAll] returns all [MapInfo]s associated with a [MapTitle] on the
     * specified language.
     */
    @Test
    fun loadAll_returnsAllMapInfosWithTitlesOnSpecifiedLanguage() {
        val infosWithTitles = mutableMapOf<MapInfo, List<MapTitle>>()
        for (id in listOf(1, 2, 5)) {
            val info = MapInfo(id, "abc-$id", 100, 100, 1024, 1, 5)
            infosWithTitles[info] = listOf(
                MapTitle(id, Language.EN, "ABC $id"),
                MapTitle(id, Language.RU, "АБВ $id")
            )
        }
        db.insertAll(infosWithTitles.keys)
        db.insertAll(infosWithTitles.values.flatten())

        for (language in Language.entries) {
            val expected = infosWithTitles.mapValues { (_, titles) ->
                titles.filter { it.languageId == language }
            }
            val actual = runBlocking { mapDao.loadAll(language) }

            assertEquals(expected, actual)
        }
    }

    /** Checks that [MapDao.loadInfoById] returns a [MapInfo] with the specified ID. */
    @Test
    fun loadInfoById_returnsMapInfoWithSpecifiedId() {
        val expected = listOf(
            MapInfo(1, "abc", 100, 100, 1024, 1, 5),
            MapInfo(2, "cba", 100, 200, 512, 2, 100)
        )
        db.insertAll(expected)

        val actual = runBlocking { expected.map { mapDao.loadInfoById(it.id) } }

        for ((exp, act) in expected.zip(actual)) {
            assertEquals(exp, act)
        }
    }

    /**
     * Checks that [MapDao.loadTitleByMapId] returns a [MapTitle] with the specified map ID on the
     * specified language.
     */
    @Test
    fun loadTitleByMapId_returnsMapTitleWithSpecifiedMapIdAndLanguage() {
        val mapInfos = listOf(
            MapInfo(1, "abc", 100, 100, 1024, 1, 5),
            MapInfo(2, "cba", 100, 200, 512, 2, 100)
        )
        val expected = mutableListOf<MapTitle>()
        for (mapInfo in mapInfos) {
            expected.add(MapTitle(mapInfo.id, Language.EN, "ABC ${mapInfo.id}"))
            expected.add(MapTitle(mapInfo.id, Language.RU, "АБВ ${mapInfo.id}"))
        }
        db.insertAll(mapInfos)
        db.insertAll(expected)

        val actual = runBlocking {
            expected.map { mapDao.loadTitleByMapId(it.mapId, it.languageId) }
        }

        for ((exp, act) in expected.zip(actual)) {
            assertEquals(exp, act)
        }
    }
}
