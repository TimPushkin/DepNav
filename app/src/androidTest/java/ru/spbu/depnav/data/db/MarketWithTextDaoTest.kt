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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.spbu.depnav.data.model.Language
import ru.spbu.depnav.data.model.MapInfo
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText

private val INSERTED_MAP = MapInfo(123, "test-map", 100, 100, 128, 5, 3)

/** Instrumentation tests for [MarkerWithTextDao]. */
@Suppress("TooManyFunctions", "StringLiteralDuplication") // OK for a test class
class MarketWithTextDaoTest : AppDatabaseDaoTest() {
    private lateinit var markerWithTextDao: MarkerWithTextDao

    @Before
    override fun setUpDao() {
        markerWithTextDao = db.markerWithTextDao()
    }

    /** Fills database with a minimal set of entities to satisfy the constraints. */
    @Before
    fun preFillDb() {
        db.insertAll(listOf(INSERTED_MAP))
    }

    /** Checks that [MarkerWithTextDao.loadById] returns the queried [Marker]. */
    @Test
    fun loadById_returnsQueriedMarker() {
        val expected = mutableMapOf<Marker, MarkerText>()
        for (id in listOf(1, 2, 5)) {
            expected += Marker(
                id,
                INSERTED_MAP.id,
                Marker.MarkerType.OTHER,
                1,
                0.0,
                0.0
            ) to MarkerText(id, Language.EN, null, null, null)
        }
        db.insertAll(expected.keys)
        db.insertAll(expected.values)

        for ((expectedMarker, markerText) in expected) {
            val actual =
                runBlocking { markerWithTextDao.loadById(expectedMarker.id, markerText.languageId) }
            val (actualMarker, _) = actual.entries.firstOrNull()?.toPair() ?: (null to null)

            assertEquals(1, actual.size)
            assertEquals(expectedMarker, actualMarker)
        }
    }

    /**
     * Checks that [MarkerWithTextDao.loadById] returns a [MarkerText] with the specified language.
     */
    @Test
    fun loadById_returnsMarkerWithSpecifiedLanguage() {
        val markersWithTexts = mutableMapOf<Marker, List<MarkerText>>()
        for (id in listOf(1, 2, 5)) {
            val marker = Marker(id, INSERTED_MAP.id, Marker.MarkerType.OTHER, 1, 0.0, 0.0)
            markersWithTexts[marker] = listOf(
                MarkerText(id, Language.EN, null, null, null),
                MarkerText(id, Language.RU, null, null, null)
            )
        }
        db.insertAll(markersWithTexts.keys)
        db.insertAll(markersWithTexts.values.flatten())

        for ((expectedMarker, markerTexts) in markersWithTexts) {
            for (expectedMarkerText in markerTexts) {
                val actual = runBlocking {
                    markerWithTextDao.loadById(expectedMarker.id, expectedMarkerText.languageId)
                }
                val actualMarkerTexts = actual.values.firstOrNull()

                assertEquals(1, actual.size)
                assertEquals(1, actualMarkerTexts?.size)
                assertEquals(expectedMarkerText, actualMarkerTexts?.first())
            }
        }
    }

    /**
     * Checks that [MarkerWithTextDao.loadByFloor] returns [Markers][Marker] from the specified map.
     */
    @Test
    fun loadByFloor_returnsMarkersFromSpecifiedMap() {
        val expectedMap = INSERTED_MAP.copy(id = 999, internalName = "another map")
        db.insertAll(expectedMap)
        val expectedId = 2
        val unexpectedId = 1
        val floor = 2
        val language = Language.EN
        db.insertAll(
            Marker(unexpectedId, INSERTED_MAP.id, Marker.MarkerType.WC, floor, 0.0, 0.0),
            Marker(expectedId, expectedMap.id, Marker.MarkerType.WC, floor, 0.0, 0.0)
        )
        db.insertAll(
            MarkerText(unexpectedId, language, "Some 1", "Some 2", "Some 3"),
            MarkerText(expectedId, language, "Some 1", "Some 2", "Some 3")
        )

        val actual = runBlocking {
            markerWithTextDao.loadByFloor(expectedMap.id, floor, Language.EN).keys
        }

        assertTrue("No markers loaded from ${expectedMap.id}", actual.isNotEmpty())
        actual.forEach { assertEquals(expectedMap.id, it.mapId) }
    }

    /**
     * Checks that [MarkerWithTextDao.loadByFloor] returns all [Markers][Marker] placed on the
     * specified floor.
     */
    @Test
    fun loadByFloor_returnsAllMarkersWithSpecifiedFloor() {
        val language = Language.EN
        val floors = List(INSERTED_MAP.floorsNum) { it * 2 }
        val markers = mutableMapOf<Int, MutableList<Marker>>()
        val markerTexts = mutableListOf<MarkerText>()
        for ((floor, id) in floors.zip(List(INSERTED_MAP.floorsNum) { it + 1 })) {
            markers.getOrPut(floor) { mutableListOf() } +=
                Marker(id, INSERTED_MAP.id, Marker.MarkerType.OTHER, floor, 1.1, -2.0)
            markerTexts += MarkerText(id, language, null, null, null)
        }
        db.insertAll(markers.values.flatten())
        db.insertAll(markerTexts)

        for (floor in floors) {
            val actualMarkers = runBlocking {
                markerWithTextDao.loadByFloor(INSERTED_MAP.id, floor, language).keys
            }

            assertTrue("No markers loaded from ${INSERTED_MAP.id}", actualMarkers.isNotEmpty())
            for (marker in actualMarkers) assertEquals(floor, marker.floor)
        }
    }

    /**
     * Checks that [MarkerWithTextDao.loadByFloor] returns [MarkerTexts][MarkerText] with the
     * specified language.
     */
    @Test
    fun loadByFloor_returnsMarkersWithSpecifiedLanguage() {
        val floors = List(INSERTED_MAP.floorsNum) { it * 2 }
        for ((floor, id) in floors.zip(List(floors.size) { it + 1 })) {
            db.insertAll(Marker(id, INSERTED_MAP.id, Marker.MarkerType.WC, floor, 0.0, 0.0))
            for (languageId in Language.entries) {
                db.insertAll(MarkerText(id, languageId, null, null, null))
            }
        }

        for (floor in floors) {
            for (languageId in Language.entries) {
                val actual = runBlocking {
                    markerWithTextDao.loadByFloor(
                        INSERTED_MAP.id,
                        floor,
                        languageId
                    ).values.flatten()
                }

                assertTrue("No marker texts loaded from ${INSERTED_MAP.id}", actual.isNotEmpty())
                actual.forEach { assertEquals(languageId, it.languageId) }
            }
        }
    }

    /**
     * Checks that [MarkerWithTextDao.loadByTokens] returns [Markers][Marker] for the specified map.
     */
    @Test
    fun loadByTokens_returnsMarkersFromSpecifiedMap() {
        val expectedMap = INSERTED_MAP.copy(id = 999, internalName = "another map")
        db.insertAll(expectedMap)
        val expectedId = 2
        val unexpectedId = 1
        val title = "Title"
        val language = Language.EN
        db.insertAll(
            Marker(unexpectedId, INSERTED_MAP.id, Marker.MarkerType.WC, 1, 0.0, 0.0),
            Marker(expectedId, expectedMap.id, Marker.MarkerType.WC, 1, 0.0, 0.0)
        )
        db.insertAll(
            MarkerText(unexpectedId, language, title, "Location", "Description"),
            MarkerText(expectedId, language, title, "Location", "Description")
        )

        val actual = runBlocking {
            markerWithTextDao.loadByTokens(expectedMap.id, title, language).values.flatten()
        }

        assertTrue("No marker texts loaded from ${INSERTED_MAP.id}", actual.isNotEmpty())
        actual.forEach { assertEquals(expectedMap.id, it.mapId) }
    }

    /**
     * Checks that [MarkerWithTextDao.loadByTokens] returns all inserted [MarkerTexts][MarkerText]
     * with the queried tokens in location or description on the default language (English).
     */
    @Test
    fun loadByTokensEnglish_returnsAllTextsWithAllQueriedTokensInLocationOrDescription() {
        val expectedTitle = "+"
        val unexpectedTitle = "-"
        var maxMarkerId = 0
        val language = Language.EN
        val markerTexts = listOf(
            MarkerText(maxMarkerId++, language, expectedTitle, "", "Lorem ipsum dolor sit amet"),
            MarkerText(maxMarkerId++, language, expectedTitle, "", "123 lorem iPsUm"),
            MarkerText(maxMarkerId++, language, expectedTitle, "a", "123 lorem a iPsUm"),
            MarkerText(maxMarkerId++, language, expectedTitle, "", "ipsum lorem"),
            MarkerText(maxMarkerId++, language, expectedTitle, "iPsUm lorem", ""),
            MarkerText(maxMarkerId++, language, expectedTitle, "lorem ipsum", "dolor sit amet"),
            MarkerText(maxMarkerId++, language, expectedTitle, "lorem", "ipsum"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "", "lorem"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "lorem", "ip"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "lor", "ipsum"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "", "lor ip"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "", "")
        )
        db.insertAll(
            List(maxMarkerId) {
                Marker(it, INSERTED_MAP.id, Marker.MarkerType.WC, 1, 0.0, 0.0)
            }
        )
        db.insertAll(markerTexts)

        val actual = runBlocking {
            markerWithTextDao.loadByTokens(INSERTED_MAP.id, "Lorem ipsum", language).keys
        }

        actual.forEach { assertEquals(expectedTitle, it.markerText.title) }
        assertEquals(markerTexts.count { it.title == expectedTitle }, actual.size)
    }

    /**
     * Checks that [MarkerWithTextDao.loadByTokens] returns all inserted [MarkerTexts][MarkerText]
     * with the queried tokens in location or description on a non-default language.
     */
    @Test
    fun loadByTokensNotEnglish_returnsAllTextsWithAllQueriedTokensInLocationOrDescription() {
        val expectedTitle = "+"
        val unexpectedTitle = "-"
        var maxMarkerId = 0
        val language = Language.RU
        val markerTexts = listOf(
            MarkerText(maxMarkerId++, language, expectedTitle, "", "Лорем ипсум долор сит амет"),
            MarkerText(maxMarkerId++, language, expectedTitle, "", "123 лорем иПсУм"),
            MarkerText(maxMarkerId++, language, expectedTitle, "а", "123 лорем а иПсУм"),
            MarkerText(maxMarkerId++, language, expectedTitle, "", "ипсум лорем"),
            MarkerText(maxMarkerId++, language, expectedTitle, "иПсУм лорем", ""),
            MarkerText(maxMarkerId++, language, expectedTitle, "лорем ипсум", "долор сит амет"),
            MarkerText(maxMarkerId++, language, expectedTitle, "лорем", "ипсум"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "", "лорем"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "лорем", "ип"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "лор", "ипсум"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "", "лор ип"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "", "")
        )

        db.insertAll(
            List(maxMarkerId) {
                Marker(it, INSERTED_MAP.id, Marker.MarkerType.WC, 1, 0.0, 0.0)
            }
        )
        db.insertAll(markerTexts)

        val actual = runBlocking {
            markerWithTextDao.loadByTokens(INSERTED_MAP.id, "Лорем ипсум", language).keys
        }

        actual.forEach { assertEquals(expectedTitle, it.markerText.title) }
        assertEquals(markerTexts.count { it.title == expectedTitle }, actual.size)
    }

    /**
     * Checks that [MarkerWithTextDao.loadByTokens] returns a single Marker for each [MarkerText]
     * and that it has a corresponding ID.
     */
    @Test
    fun loadByTokens_returnsSingleMarkerWithCorrectIdForEachMarkerText() {
        val title = "title"
        val language = Language.EN
        val markersWithText = mutableMapOf<Marker, MarkerText>()
        for (id in listOf(1, 2, 5)) {
            val marker = Marker(id, INSERTED_MAP.id, Marker.MarkerType.WC, 1, 0.0, 0.0)
            markersWithText[marker] = MarkerText(id, language, title, null, null)
        }
        db.insertAll(markersWithText.keys)
        db.insertAll(markersWithText.values)

        val actual =
            runBlocking { markerWithTextDao.loadByTokens(INSERTED_MAP.id, title, language) }

        for ((markerTextWithMatchInfo, markers) in actual) {
            assertTrue(
                "Expected markers to be of size 1, but was ${markers.size}",
                markers.size == 1
            )
            assertEquals(markerTextWithMatchInfo.markerText.markerId, markers.first().id)
        }
    }
}
