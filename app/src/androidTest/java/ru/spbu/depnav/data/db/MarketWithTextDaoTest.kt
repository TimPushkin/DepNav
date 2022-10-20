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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import ru.spbu.depnav.data.model.MapInfo
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText

private val INSERTED_MAP = MapInfo("test map", 100, 100, 128, 5, 3)

/** Instrumentation tests for [MarkerWithTextDao]. */
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
        val expected = mutableListOf<Pair<Marker, MarkerText>>()
        for (id in listOf(1, 2, 5)) {
            expected += Marker(
                id,
                INSERTED_MAP.name,
                Marker.MarkerType.OTHER,
                false,
                1,
                0.0,
                0.0
            ) to MarkerText(id, MarkerText.LanguageId.EN, null, null)
        }
        db.insertAll(expected.map { it.first })
        db.insertAll(expected.map { it.second })

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
            val marker = Marker(id, INSERTED_MAP.name, Marker.MarkerType.OTHER, false, 1, 0.0, 0.0)
            markersWithTexts[marker] = listOf(
                MarkerText(id, MarkerText.LanguageId.EN, null, null),
                MarkerText(id, MarkerText.LanguageId.RU, null, null)
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
     * Checks that [MarkerWithTextDao.loadByMapAndFloor] returns all [Markers][Marker] placed on the
     * specified floor.
     */
    @Test
    fun loadByFloor_returnsAllMarkersWithSpecifiedFloor() {
        val languageId = MarkerText.LanguageId.EN
        val floors = List(INSERTED_MAP.floorsNum) { it * 2 }
        val markers = mutableMapOf<Int, MutableList<Marker>>()
        val markerTexts = mutableListOf<MarkerText>()
        for ((floor, id) in floors.zip(List(INSERTED_MAP.floorsNum) { it + 1 })) {
            markers.getOrPut(floor) { mutableListOf() } +=
                Marker(id, INSERTED_MAP.name, Marker.MarkerType.OTHER, false, floor, 1.1, -2.0)
            markerTexts += MarkerText(id, languageId, null, null)
        }
        db.insertAll(markers.values.flatten())
        db.insertAll(markerTexts)

        for (floor in floors) {
            val actualMarkers = runBlocking {
                markerWithTextDao.loadByMapAndFloor(
                    INSERTED_MAP.name,
                    floor,
                    languageId
                ).keys
            }

            assertTrue("Loaded markers are empty", actualMarkers.isNotEmpty())
            for (marker in actualMarkers) assertEquals(floor, marker.floor)
        }
    }

    /**
     * Checks that [MarkerWithTextDao.loadByMapAndFloor] returns [MarkerTexts][MarkerText] with the
     * specified language.
     */
    @Test
    fun loadByFloor_returnsMarkersWithSpecifiedLanguage() {
        val floors = List(INSERTED_MAP.floorsNum) { it * 2 }
        for ((floor, id) in floors.zip(List(floors.size) { it + 1 })) {
            db.insertAll(
                listOf(
                    Marker(id, INSERTED_MAP.name, Marker.MarkerType.WC, true, floor, 0.0, 0.0)
                )
            )
            for (languageId in MarkerText.LanguageId.values()) {
                db.insertAll(listOf(MarkerText(id, languageId, null, null)))
            }
        }

        for (floor in floors) {
            for (languageId in MarkerText.LanguageId.values()) {
                val actual = runBlocking {
                    markerWithTextDao.loadByMapAndFloor(
                        INSERTED_MAP.name,
                        floor,
                        languageId
                    ).values.flatten()
                }

                assertTrue("Loaded markers are empty", actual.isNotEmpty())
                actual.forEach { assertEquals(languageId, it.languageId) }
            }
        }
    }

    /**
     * Checks that [MarkerWithTextDao.loadByMapAndTokens] returns all inserted
     * [MarkerTexts][MarkerText] with the queried tokens on the default language (English).
     */
    @Test
    fun loadByTokensEnglish_returnsAllTextsWithAllQueriedTokensInDescription() {
        val expectedTitle = "+"
        val unexpectedTitle = "-"
        var maxMarkerId = 0
        val language = MarkerText.LanguageId.EN
        val markerTexts = listOf(
            MarkerText(maxMarkerId++, language, expectedTitle, "Lorem ipsum dolor sit amet"),
            MarkerText(maxMarkerId++, language, expectedTitle, "123 lorem iPsUm"),
            MarkerText(maxMarkerId++, language, expectedTitle, "123 lorem a iPsUm"),
            MarkerText(maxMarkerId++, language, expectedTitle, "ipsum lorem"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "lorem"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "ipsum"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "lor ip"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "")
        )
        db.insertAll(
            List(maxMarkerId) {
                Marker(it, INSERTED_MAP.name, Marker.MarkerType.WC, false, 1, 0.0, 0.0)
            }
        )
        db.insertAll(markerTexts)

        val actual = runBlocking {
            markerWithTextDao.loadByMapAndTokens(INSERTED_MAP.name, "Lorem ipsum", language).keys
        }

        actual.forEach { assertEquals(expectedTitle, it.markerText.title) }
        assertEquals(markerTexts.count { it.title == expectedTitle }, actual.size)
    }

    /**
     * Checks that [MarkerWithTextDao.loadByMapAndTokens] returns all inserted
     * [MarkerTexts][MarkerText] with the queried tokens on a non-default language.
     */
    @Test
    fun loadByTokensNotEnglish_returnsAllTextsWithAllQueriedTokensInDescription() {
        val expectedTitle = "+"
        val unexpectedTitle = "-"
        var maxMarkerId = 0
        val language = MarkerText.LanguageId.RU
        val markerTexts = listOf(
            MarkerText(maxMarkerId++, language, expectedTitle, "Лорем ипсум долор сит амет"),
            MarkerText(maxMarkerId++, language, expectedTitle, "123 лорем иПсУм"),
            MarkerText(maxMarkerId++, language, expectedTitle, "123 лорем а иПсУм"),
            MarkerText(maxMarkerId++, language, expectedTitle, "ипсум лорем"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "лорем"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "ипсум"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "лор ип"),
            MarkerText(maxMarkerId++, language, unexpectedTitle, "")
        )

        db.insertAll(
            List(maxMarkerId) {
                Marker(it, INSERTED_MAP.name, Marker.MarkerType.WC, false, 1, 0.0, 0.0)
            }
        )
        db.insertAll(markerTexts)

        val actual = runBlocking {
            markerWithTextDao.loadByMapAndTokens(INSERTED_MAP.name, "Лорем ипсум", language).keys
        }

        actual.forEach { assertEquals(expectedTitle, it.markerText.title) }
        assertEquals(markerTexts.count { it.title == expectedTitle }, actual.size)
    }

    /**
     * Checks that [MarkerWithTextDao.loadByMapAndTokens] returns a single Marker for each
     * MarkerText and that it has a corresponding ID.
     */
    @Test
    fun loadByTokens_returnsSingleMarkerWithCorrectIdForEachMarkerText() {
        val title = "title"
        val language = MarkerText.LanguageId.EN
        val markersWithText = mutableMapOf<Marker, MarkerText>()
        for (id in listOf(1, 2, 5)) {
            val marker = Marker(id, INSERTED_MAP.name, Marker.MarkerType.WC, true, 1, 0.0, 0.0)
            markersWithText[marker] = MarkerText(id, language, title, null)
        }
        db.insertAll(markersWithText.keys)
        db.insertAll(markersWithText.values)

        val actual =
            runBlocking { markerWithTextDao.loadByMapAndTokens(INSERTED_MAP.name, title, language) }

        for ((markerTextWithMatchInfo, markers) in actual) {
            assertTrue(
                "Expected markers to be of size 1, but was ${markers.size}",
                markers.size == 1
            )
            assertEquals(markerTextWithMatchInfo.markerText.markerId, markers.first().id)
        }
    }
}
