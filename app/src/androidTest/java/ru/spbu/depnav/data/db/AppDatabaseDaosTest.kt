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

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.spbu.depnav.data.model.MapInfo
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText

/** Instrumentation tests for [AppDatabase]'s DAOs. */
@RunWith(AndroidJUnit4::class)
class AppDatabaseDaosTest {
    private lateinit var db: AppDatabase
    private lateinit var mapInfoDao: MapInfoDao
    private lateinit var markerWithTextDao: MarkerWithTextDao

    /** Initializes an instance of a database and related DAOs. */
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        mapInfoDao = db.mapInfoDao()
        markerWithTextDao = db.markerWithTextDao()
    }

    /**
     * Closes the database instance.
     */
    @After
    fun tearDown() {
        db.close()
    }

    /* MapInfo tests */

    /** Checks that [MapInfoDao.loadByName] returns a [MapInfo] with the queried name. */
    @Test
    fun loadByName_returnsMapInfoWithQueriedName() {
        val expected = listOf(
            MapInfo("abc", 100, 100, 1024, 1, 5),
            MapInfo("cba", 100, 200, 512, 2, 100)
        )
        runBlocking { mapInfoDao.insertAll(*expected.toTypedArray()) }

        val actual = runBlocking {
            listOf(
                mapInfoDao.loadByName(expected[0].mapName),
                mapInfoDao.loadByName(expected[1].mapName)
            )
        }

        for (i in 0..1) assertEquals(expected[i], actual[i])
    }

    /* Marker tests */

    /** Checks that [MarkerWithTextDao.loadById] returns the queried [Marker]. */
    @Test
    fun loadById_returnsQueriedMarker() {
        val expected = mutableListOf<Pair<Marker, MarkerText>>()
        for (id in listOf(1, 2, 5)) {
            expected += Marker(id, Marker.MarkerType.OTHER, false, 1, 0.0, 0.0) to
                MarkerText(id, MarkerText.LanguageId.EN, null, null)
        }
        runBlocking {
            markerWithTextDao.insertMarkers(expected.map { it.first })
            markerWithTextDao.insertMarkerTexts(expected.map { it.second })
        }

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
            val marker = Marker(id, Marker.MarkerType.OTHER, false, 1, 0.0, 0.0)
            markersWithTexts[marker] = listOf(
                MarkerText(id, MarkerText.LanguageId.EN, null, null),
                MarkerText(id, MarkerText.LanguageId.RU, null, null)
            )
        }
        runBlocking {
            markerWithTextDao.insertMarkers(markersWithTexts.keys)
            markerWithTextDao.insertMarkerTexts(markersWithTexts.values.flatten())
        }

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
     * Checks that [MarkerWithTextDao.loadByFloor] returns all [Markers][Marker] placed on the
     * specified floor.
     */
    @Test
    fun loadByFloor_returnsAllMarkersWithSpecifiedFloor() {
        val languageId = MarkerText.LanguageId.EN
        val markers = mutableMapOf<Int, MutableList<Marker>>()
        val markerTexts = mutableListOf<MarkerText>()
        var id = 1
        for (floor in listOf(1, 2, 5)) {
            markers.getOrPut(floor) { mutableListOf() } +=
                Marker(id, Marker.MarkerType.OTHER, false, floor, 1.1, -2.0)
            markerTexts += MarkerText(id++, languageId, null, null)
        }
        runBlocking {
            markerWithTextDao.insertMarkers(markers.values.flatten())
            markerWithTextDao.insertMarkerTexts(markerTexts)
        }

        for (floor in markers.keys) {
            val actualMarkers =
                runBlocking { markerWithTextDao.loadByFloor(floor, languageId).keys }

            assert(actualMarkers.isNotEmpty())
            for (marker in actualMarkers) assertEquals(floor, marker.floor)
        }
    }

    /**
     * Checks that [MarkerWithTextDao.loadByFloor] returns [MarkerTexts][MarkerText] with the
     * specified language.
     */
    @Test
    fun loadByFloor_returnsMarkersWithSpecifiedLanguage() {
        val floors = listOf(1, 2, 5)
        var id = 1
        for (floor in floors) {
            with(markerWithTextDao) {
                runBlocking {
                    insertMarkers(
                        listOf(Marker(id, Marker.MarkerType.OTHER, false, floor, 0.0, 0.0))
                    )
                    for (languageId in MarkerText.LanguageId.values()) {
                        insertMarkerTexts(listOf(MarkerText(id, languageId, null, null)))
                    }
                }

            }
            id++
        }

        for (floor in floors) {
            for (languageId in MarkerText.LanguageId.values()) {
                val actual = runBlocking {
                    markerWithTextDao.loadByFloor(floor, languageId).values.flatten()
                }

                assert(actual.isNotEmpty())
                actual.forEach { assertEquals(languageId, it.languageId) }
            }
        }
    }

    /* MarkerText tests */

    /**
     * Checks that [MarkerWithTextDao.loadByTokens] returns all inserted [MarkerTexts][MarkerText]
     * with the queried tokens on the default language (English).
     */
    @Test
    fun loadByTokensEnglish_returnsAllTextsWithAllQueriedTokensInDescription() {
        val expectedTitle = "+"
        val unexpectedTitle = "-"
        val markerId = 1
        val language = MarkerText.LanguageId.EN
        val markerTexts = listOf(
            MarkerText(markerId, language, expectedTitle, "Lorem ipsum dolor sit amet"),
            MarkerText(markerId, language, expectedTitle, "123 lorem iPsUm"),
            MarkerText(markerId, language, expectedTitle, "123 lorem a iPsUm"),
            MarkerText(markerId, language, expectedTitle, "ipsum lorem"),
            MarkerText(markerId, language, unexpectedTitle, "lorem"),
            MarkerText(markerId, language, unexpectedTitle, "ipsum"),
            MarkerText(markerId, language, unexpectedTitle, "lor ip"),
            MarkerText(markerId, language, unexpectedTitle, ""),
        )
        runBlocking {
            markerWithTextDao.insertMarkerTexts(markerTexts)
            // Marker required because all MarkerTexts must have an associated marker
            markerWithTextDao.insertMarkers(
                listOf(Marker(markerId, Marker.MarkerType.WC, false, 1, 0.0, 0.0))
            )
        }

        val actual = runBlocking { markerWithTextDao.loadByTokens("Lorem ipsum", language).keys }

        actual.forEach { assertEquals(expectedTitle, it.title) }
        assertEquals(markerTexts.count { it.title == expectedTitle }, actual.size)
    }

    /**
     * Checks that [MarkerWithTextDao.loadByTokens] returns all inserted [MarkerTexts][MarkerText]
     * with the queried tokens on a non-default language.
     */
    @Test
    fun loadByTokensNotEnglish_returnsAllTextsWithAllQueriedTokensInDescription() {
        val expectedTitle = "+"
        val unexpectedTitle = "-"
        val markerId = 1
        val language = MarkerText.LanguageId.RU
        val markerTexts = listOf(
            MarkerText(markerId, language, expectedTitle, "Лорем ипсум долор сит амет"),
            MarkerText(markerId, language, expectedTitle, "123 лорем иПсУм"),
            MarkerText(markerId, language, expectedTitle, "123 лорем а иПсУм"),
            MarkerText(markerId, language, expectedTitle, "ипсум лорем"),
            MarkerText(markerId, language, unexpectedTitle, "лорем"),
            MarkerText(markerId, language, unexpectedTitle, "ипсум"),
            MarkerText(markerId, language, unexpectedTitle, "лор ип"),
            MarkerText(markerId, language, unexpectedTitle, ""),
        )
        runBlocking {
            markerWithTextDao.insertMarkerTexts(markerTexts)
            // Marker required because all MarkerTexts must have an associated marker
            markerWithTextDao.insertMarkers(
                listOf(Marker(markerId, Marker.MarkerType.WC, false, 1, 0.0, 0.0))
            )
        }

        val actual = runBlocking { markerWithTextDao.loadByTokens("Лорем ипсум", language).keys }

        actual.forEach { assertEquals(expectedTitle, it.title) }
        assertEquals(markerTexts.count { it.title == expectedTitle }, actual.size)
    }

    /**
     * Checks that [MarkerWithTextDao.loadByTokens] returns a single Marker for each MarkerText and
     * that it has a corresponding ID.
     */
    @Test
    fun loadByTokens_returnsSingleMarkerWithCorrectIdForEachMarkerText() {
        val title = "title"
        val language = MarkerText.LanguageId.EN
        val markersWithText = mutableMapOf<Marker, MarkerText>()
        for (id in listOf(1, 2, 5)) {
            val marker = Marker(id, Marker.MarkerType.OTHER, false, 1, 0.0, 0.0)
            markersWithText[marker] = MarkerText(id, language, title, null)
        }
        runBlocking {
            markerWithTextDao.insertMarkers(markersWithText.keys)
            markerWithTextDao.insertMarkerTexts(markersWithText.values)
        }

        val actual = runBlocking { markerWithTextDao.loadByTokens(title, language) }

        for ((markerText, markers) in actual) {
            assertTrue(markers.size == 1)
            assertEquals(markerText.markerId, markers.first().id)
        }
    }
}
