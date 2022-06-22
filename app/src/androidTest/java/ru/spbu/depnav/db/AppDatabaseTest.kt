package ru.spbu.depnav.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.spbu.depnav.model.MapInfo
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.model.MarkerText

/**
 * Instrumentation texts for [AppDatabase].
 */
@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var mapInfoDao: MapInfoDao
    private lateinit var markerDao: MarkerDao
    private lateinit var markerTextDao: MarkerTextDao

    /**
     * Initializes an instance of a database and related DAOs.
     */
    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        mapInfoDao = db.mapInfoDao()
        markerDao = db.markerDao()
        markerTextDao = db.markerTextDao()
    }

    /**
     * Closes the database instance.
     */
    @After
    fun tearDown() {
        db.close()
    }

    /* MapInfo tests */

    /**
     * Checks that [MapInfoDao.loadByName] returns a [MapInfo] with the queried name.
     */
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

    /**
     * Checks that [MarkerDao.loadWithTextById] returns the queried [Marker].
     */
    @Test
    fun loadWithTextById_returnsQueriedMarker() {
        val expected = mutableListOf<Pair<Marker, MarkerText>>()
        for (id in listOf(1, 2, 5)) {
            expected += Marker(id, Marker.MarkerType.OTHER, false, 1, 0.0, 0.0) to
                    MarkerText(id, MarkerText.LanguageId.EN, null, null)
        }
        runBlocking {
            markerDao.insertAll(*expected.map { it.first }.toTypedArray())
            markerTextDao.insertAll(*expected.map { it.second }.toTypedArray())
        }

        for ((expectedMarker, markerText) in expected) {
            val actual =
                runBlocking { markerDao.loadWithTextById(expectedMarker.id, markerText.languageId) }
            val (actualMarker, _) = actual.entries.firstOrNull()?.toPair() ?: (null to null)

            assertEquals(1, actual.size)
            assertEquals(expectedMarker, actualMarker)
        }
    }

    /**
     * Checks that [MarkerDao.loadWithTextById] returns a [MarkerText] with the specified language.
     */
    @Test
    fun loadWithTextById_returnedMarkerHasSpecifiedLanguage() {
        val markersWithTexts = mutableMapOf<Marker, List<MarkerText>>()
        for (id in listOf(1, 2, 5)) {
            val marker = Marker(id, Marker.MarkerType.OTHER, false, 1, 0.0, 0.0)
            markersWithTexts[marker] = listOf(
                MarkerText(id, MarkerText.LanguageId.EN, null, null),
                MarkerText(id, MarkerText.LanguageId.RU, null, null)
            )
        }
        runBlocking {
            markerDao.insertAll(*markersWithTexts.keys.toTypedArray())
            markerTextDao.insertAll(*markersWithTexts.values.flatten().toTypedArray())
        }

        for ((expectedMarker, markerTexts) in markersWithTexts) {
            for (expectedMarkerText in markerTexts) {
                val actual = runBlocking {
                    markerDao.loadWithTextById(expectedMarker.id, expectedMarkerText.languageId)
                }
                val actualMarkerTexts = actual.values.firstOrNull()

                assertEquals(1, actual.size)
                assertEquals(1, actualMarkerTexts?.size)
                assertEquals(expectedMarkerText, actualMarkerTexts?.first())
            }
        }
    }

    /**
     * Checks that [MarkerDao.loadWithTextByFloor] returns all [Markers][Marker] placed on the
     * specified floor.
     */
    @Test
    fun loadWithTextByFloor_returnsAllMarkersWithSpecifiedFloor() {
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
            markerDao.insertAll(*markers.values.flatten().toTypedArray())
            markerTextDao.insertAll(*markerTexts.toTypedArray())
        }

        for (floor in markers.keys) {
            val actualMarkers =
                runBlocking { markerDao.loadWithTextByFloor(floor, languageId).keys }

            assert(actualMarkers.isNotEmpty())
            for (marker in actualMarkers) assertEquals(floor, marker.floor)
        }
    }

    /**
     * Checks that [MarkerDao.loadWithTextByFloor] returns [MarkerTexts][MarkerText] with the
     * specified language.
     */
    @Test
    fun loadWithTextByFloor_returnedMarkersHaveSpecifiedLanguage() {
        val floors = listOf(1, 2, 5)
        var id = 1
        for (floor in floors) runBlocking {
            markerDao.insertAll(Marker(id, Marker.MarkerType.OTHER, false, floor, 0.0, 0.0))
            for (languageId in MarkerText.LanguageId.values()) {
                markerTextDao.insertAll(MarkerText(id, languageId, null, null))
            }
            id++
        }

        for (floor in floors) {
            for (languageId in MarkerText.LanguageId.values()) {
                val actual = runBlocking {
                    markerDao.loadWithTextByFloor(floor, languageId).values.flatten()
                }

                assert(actual.isNotEmpty())
                actual.forEach { assertEquals(languageId, it.languageId) }
            }
        }
    }

    /* MarkerText tests */

    /**
     * Checks that [MarkerTextDao.loadByTokens] returns all inserted [MarkerTexts][MarkerText] with
     * the queried tokens on the default language (English).
     */
    @Test
    fun loadByTokensEnglish_returnsAllTextsWithAllQueriedTokensInDescription() {
        val expectedTitle = "+"
        val unexpectedTitle = "-"
        val language = MarkerText.LanguageId.EN
        val markerTexts = listOf(
            MarkerText(1, language, expectedTitle, "Lorem ipsum dolor sit amet"),
            MarkerText(2, language, expectedTitle, "123 lorem iPsUm"),
            MarkerText(3, language, expectedTitle, "123 lorem a iPsUm"),
            MarkerText(4, language, expectedTitle, "ipsum lorem"),
            MarkerText(4, language, unexpectedTitle, "lorem"),
            MarkerText(4, language, unexpectedTitle, "ipsum"),
            MarkerText(4, language, unexpectedTitle, "lor ip"),
            MarkerText(4, language, unexpectedTitle, ""),
        )
        runBlocking { markerTextDao.insertAll(*markerTexts.toTypedArray()) }

        val actual = runBlocking { markerTextDao.loadByTokens("Lorem ipsum", language) }

        actual.forEach { assertEquals(expectedTitle, it.title) }
        assertEquals(markerTexts.count { it.title == expectedTitle }, actual.size)
    }

    /**
     * Checks that [MarkerTextDao.loadByTokens] returns all inserted [MarkerTexts][MarkerText] with
     * the queried tokens on a non-default language.
     */
    @Test
    fun loadByTokensNotEnglish_returnsAllTextsWithAllQueriedTokensInDescription() {
        val expectedTitle = "+"
        val unexpectedTitle = "-"
        val language = MarkerText.LanguageId.RU
        val markerTexts = listOf(
            MarkerText(1, language, expectedTitle, "Лорем ипсум долор сит амет"),
            MarkerText(2, language, expectedTitle, "123 лорем иПсУм"),
            MarkerText(3, language, expectedTitle, "123 лорем а иПсУм"),
            MarkerText(4, language, expectedTitle, "ипсум лорем"),
            MarkerText(4, language, unexpectedTitle, "лорем"),
            MarkerText(4, language, unexpectedTitle, "ипсум"),
            MarkerText(4, language, unexpectedTitle, "лор ип"),
            MarkerText(4, language, unexpectedTitle, ""),
        )
        runBlocking { markerTextDao.insertAll(*markerTexts.toTypedArray()) }

        val actual = runBlocking { markerTextDao.loadByTokens("Лорем ипсум", language) }

        actual.forEach { assertEquals(expectedTitle, it.title) }
        assertEquals(markerTexts.count { it.title == expectedTitle }, actual.size)
    }
}
