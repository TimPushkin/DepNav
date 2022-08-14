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
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.unit.IntSize
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ovh.plrapps.mapcompose.api.fullSize
import ru.spbu.depnav.db.AppDatabase
import ru.spbu.depnav.model.Floor
import ru.spbu.depnav.model.MarkerText
import ru.spbu.depnav.ui.map.MapScreen
import ru.spbu.depnav.ui.map.MapScreenViewModel
import ru.spbu.depnav.ui.theme.DepNavTheme
import ru.spbu.depnav.utils.PreferencesManager
import ru.spbu.depnav.utils.TileProviderFactory

private const val TAG = "MainActivity"

// TODO: retrieve from saved state
private const val MAP_NAME = "spbu-mm"
private const val TILES_PATH = "$MAP_NAME/tiles"

/**
 * Activity which displays the map screen.
 */
@AndroidEntryPoint
class MainActivity : LanguageAwareActivity() {
    private val mapScreenViewModel: MapScreenViewModel by viewModels()
    private lateinit var appDatabase: AppDatabase
    private lateinit var floors: Map<Int, Floor>

    private val startSearch = registerForActivityResult(SearchForMarker()) { result ->
        Log.i(TAG, "Received $result as a search result")

        val markerId = result ?: return@registerForActivityResult
        lifecycleScope.launch {
            val (marker, markerTexts) =
                appDatabase.markerDao().loadWithTextById(markerId, systemLanguage).entries.first()

            Log.d(TAG, "Loaded searched marker: $marker")

            val markerText = markerTexts.firstOrNull() ?: run {
                Log.w(TAG, "Marker $marker has no text on $systemLanguage")
                MarkerText(marker.id, systemLanguage, null, null)
            }

            setFloor(marker.floor) { mapScreenViewModel.focusOnMarker(marker, markerText) }
        }
    }

    private class SearchForMarker : ActivityResultContract<Unit, Int?>() {
        override fun createIntent(context: Context, input: Unit) =
            Intent(context, SearchActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?) =
            if (resultCode == Activity.RESULT_OK) intent?.extras?.getInt(EXTRA_MARKER_ID) else null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        appDatabase = AppDatabase.getInstance(this)
        val mapInfo = runBlocking { appDatabase.mapInfoDao().loadByName(MAP_NAME) }

        initFloors(mapInfo.floorsNum)

        if (mapScreenViewModel.mapState.fullSize == IntSize.Zero) { // State is not initialized
            mapScreenViewModel.setParams(
                mapInfo.levelsNum,
                mapInfo.floorWidth,
                mapInfo.floorHeight,
                mapInfo.tileSize
            )
            mapScreenViewModel.currentFloor = floors.keys.first()
            setFloor(mapScreenViewModel.currentFloor)
        }

        setContent {
            DepNavTheme(
                darkTheme = when (mapScreenViewModel.prefs.themeMode) {
                    PreferencesManager.ThemeMode.LIGHT -> false
                    PreferencesManager.ThemeMode.DARK -> true
                    PreferencesManager.ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                mapScreenViewModel.tileColor = MaterialTheme.colors.onBackground

                MapScreen(
                    vm = mapScreenViewModel,
                    floorsNum = mapInfo.floorsNum, // TODO: move into screen state
                    onStartSearch = startSearch::launch,
                    onFloorSwitch = this::setFloor
                )
            }
        }
    }

    private fun initFloors(floorsNum: Int) {
        val factory = TileProviderFactory(applicationContext.assets, TILES_PATH)
        val markerDao = appDatabase.markerDao()

        runBlocking {
            floors = List(floorsNum) {
                val floorNum = it + 1
                val layers = listOf(factory.makeTileProviderForFloor(floorNum))
                val markers = async(Dispatchers.IO) {
                    val markersWithTexts = markerDao.loadWithTextByFloor(floorNum, systemLanguage)
                    markersWithTexts.entries.associate { (marker, markerTexts) ->
                        val markerText = markerTexts.firstOrNull() ?: run {
                            Log.w(TAG, "Marker $marker has no text on $systemLanguage")
                            MarkerText(marker.id, systemLanguage, null, null)
                        }
                        marker to markerText
                    }
                }
                floorNum to Floor(layers, markers)
            }.toMap()
        }
    }

    private fun setFloor(floorIndex: Int, onFinished: () -> Unit = {}) {
        val floor = floors[floorIndex]
        if (floor == null) {
            Log.e(TAG, "Cannot switch to floor $floorIndex which does not exist")
            return
        }

        Log.i(TAG, "Switching to floor $floorIndex")

        mapScreenViewModel.currentFloor = floorIndex
        mapScreenViewModel.isMarkerPinned = false

        lifecycleScope.launch {
            mapScreenViewModel.replaceLayersWith(floor.layers)
            mapScreenViewModel.replaceMarkersWith(floor.markers.await())
            Log.d(TAG, "Switched to floor $floorIndex")
            onFinished()
        }
    }
}
