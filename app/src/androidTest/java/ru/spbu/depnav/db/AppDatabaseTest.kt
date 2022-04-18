package ru.spbu.depnav.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.spbu.depnav.model.MapInfo
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.model.MarkerText

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var mapInfoDao: MapInfoDao
    private lateinit var markerDao: MarkerDao
    private lateinit var markerTextDao: MarkerTextDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        mapInfoDao = db.mapInfoDao()
        markerDao = db.markerDao()
        markerTextDao = db.markerTextDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    /* MapInfo tests */

    @Test
    fun loadByName_returnsMapInfoWithQueriedName() {
        val expected = listOf(
            MapInfo("abc", 100, 100, 1024, 5),
            MapInfo("cba", 100, 200, 512, 100)
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

    @Test
    fun loadById_returnsMarkerWithQueriedId() {
        val expected = listOf(
            Marker(1, Marker.MarkerType.ELEVATOR, false, 1, 0.0, 0.0),
            Marker(3, Marker.MarkerType.WC, true, 1, 0.1, 1.0)
        )
        runBlocking { markerDao.insertAll(*expected.toTypedArray()) }

        val actual = runBlocking {
            listOf(
                markerDao.loadById(expected[0].id),
                markerDao.loadById(expected[1].id)
            )
        }

        for (i in 0..1) assertEquals(expected[i], actual[i])
    }

    @Test
    fun loadByFloor_returnsAllMarkersOnSpecifiedFloor() {
        val markers = mutableMapOf<Int, MutableList<Marker>>()
        val markerTexts = mutableListOf<MarkerText>()
        var id = 1
        listOf(1, 2, 5).forEach { floor ->
            markers.getOrPut(floor) { mutableListOf() } +=
                Marker(id, Marker.MarkerType.OTHER, false, floor, 1.1, -2.0)
            markerTexts += MarkerText(id++, MarkerText.LanguageId.EN, null, null)
        }
        runBlocking {
            markerDao.insertAll(*markers.values.flatten().toTypedArray())
            markerTextDao.insertAll(*markerTexts.toTypedArray())
        }

        for (floor in markers.keys) {
            val actualMarkers = runBlocking { markerDao.loadWithTextByFloor(floor).keys }

            assert(actualMarkers.isNotEmpty())
            for (marker in actualMarkers) assertEquals(floor, marker.floor)
        }
    }

    /* MarkerText tests */

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
