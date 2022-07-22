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

package ru.spbu.depnav

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.spbu.depnav.db.AppDatabase
import ru.spbu.depnav.model.Floor
import ru.spbu.depnav.model.MarkerText
import ru.spbu.depnav.provider.TileProviderFactory
import ru.spbu.depnav.ui.map.MapScreen
import ru.spbu.depnav.ui.map.MapScreenState
import ru.spbu.depnav.ui.theme.DepNavTheme

private const val TAG = "MainActivity"

// TODO: retrieve from saved state
private const val MAP_NAME = "spbu-mm"
private const val TILES_PATH = "$MAP_NAME/tiles"

/**
 * Activity which displays the map screen.
 */
class MainActivity : LanguageAwareActivity() {
    private val mMapScreenState: MapScreenState by viewModels()
    private lateinit var mAppDatabase: AppDatabase
    private lateinit var mFloors: Map<Int, Floor>

    private val isInDarkTheme: Boolean
        get() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK ==
                Configuration.UI_MODE_NIGHT_YES

    private val startSearch = registerForActivityResult(SearchForMarker()) { result ->
        Log.i(TAG, "Received $result as a search result")

        val markerId = result ?: return@registerForActivityResult
        lifecycleScope.launch {
            val (marker, markerTexts) =
                mAppDatabase.markerDao().loadWithTextById(markerId, systemLanguage).entries.first()

            Log.d(TAG, "Loaded searched marker: $marker")

            val markerText = markerTexts.firstOrNull() ?: run {
                Log.w(TAG, "Marker $marker has no text on $systemLanguage")
                MarkerText(marker.id, systemLanguage, null, null)
            }

            setFloor(marker.floor) { mMapScreenState.focusOnMarker(marker, markerText) }
        }
    }

    private class SearchForMarker : ActivityResultContract<Unit, Int?>() {
        override fun createIntent(context: Context, input: Unit) =
            Intent(context, SearchActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?): Int? {
            if (resultCode != Activity.RESULT_OK) return null
            // getInt() is not used as it requires a default value
            return intent?.extras?.get(EXTRA_MARKER_ID)?.run { toString().toInt() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isInDarkTheme) {
            WindowInsetsControllerCompat(window, window.decorView).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }

        mAppDatabase = AppDatabase.getInstance(this)
        val mapInfo = runBlocking { mAppDatabase.mapInfoDao().loadByName(MAP_NAME) }

        initFloors(mapInfo.floorsNum)

        when {
            mMapScreenState.usesDarkThemeTiles == null -> { // Map screen state is not initialized
                mMapScreenState.setParams(
                    mapInfo.levelsNum,
                    mapInfo.floorWidth,
                    mapInfo.floorHeight,
                    mapInfo.tileSize
                )
                setFloor(mFloors.keys.first())
            }
            mMapScreenState.usesDarkThemeTiles != isInDarkTheme -> // Tiles update required
                setFloor(mMapScreenState.currentFloor)
        }

        setContent {
            DepNavTheme {
                window.statusBarColor = MaterialTheme.colors.background.toArgb()
                window.navigationBarColor = MaterialTheme.colors.surface.toArgb()

                MapScreen(
                    mapScreenState = mMapScreenState,
                    floorsNum = mapInfo.floorsNum,
                    onStartSearch = startSearch::launch,
                    onFloorSwitch = this::setFloor
                )
            }
        }
    }

    private fun initFloors(floorsNum: Int) {
        val factory = TileProviderFactory(applicationContext.assets, TILES_PATH)
        val markerDao = mAppDatabase.markerDao()

        runBlocking {
            mFloors = List(floorsNum) {
                val floor = it + 1
                floor to Floor(
                    layers = listOf(factory.makeTileProviderForFloor(floor, isInDarkTheme)),
                    markers = async(Dispatchers.IO) {
                        markerDao.loadWithTextByFloor(
                            floor,
                            systemLanguage
                        ).entries.associate { (marker, markerTexts) ->
                            val markerText = markerTexts.firstOrNull() ?: run {
                                Log.w(TAG, "Marker $marker has no text on $systemLanguage")
                                MarkerText(marker.id, systemLanguage, null, null)
                            }
                            marker to markerText
                        }
                    }
                )
            }.toMap()
        }
    }

    private fun setFloor(floorIndex: Int, onFinished: () -> Unit = {}) {
        val floor = mFloors[floorIndex]
        if (floor == null) {
            Log.e(TAG, "Cannot switch to floor $floorIndex which does not exist")
            return
        }

        Log.i(TAG, "Switching to floor $floorIndex")

        val shouldReplaceMarkers = floorIndex != mMapScreenState.currentFloor

        mMapScreenState.currentFloor = floorIndex
        mMapScreenState.isMarkerPinned = false

        lifecycleScope.launch {
            mMapScreenState.replaceLayersWith(floor.layers, isInDarkTheme)
            if (shouldReplaceMarkers) mMapScreenState.replaceMarkersWith(floor.markers.await())
            Log.d(TAG, "Switched to floor $floorIndex")
            onFinished()
        }
    }
}
