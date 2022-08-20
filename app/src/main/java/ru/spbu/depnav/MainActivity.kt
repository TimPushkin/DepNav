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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.fullSize
import ru.spbu.depnav.data.repository.MarkerWithTextRepo
import ru.spbu.depnav.ui.map.MapScreen
import ru.spbu.depnav.ui.map.MapScreenViewModel
import ru.spbu.depnav.ui.theme.DepNavTheme
import ru.spbu.depnav.utils.preferences.PreferencesManager
import javax.inject.Inject

private const val TAG = "MainActivity"

private const val MAP_NAME = "spbu-mm"
private const val TILES_PATH = "$MAP_NAME/tiles"

/** Activity which displays the map screen. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val mapScreenViewModel: MapScreenViewModel by viewModels()

    @Inject // Has to be a public lateinit var for Hilt to be able to inject
    lateinit var markerWithTextRepo: MarkerWithTextRepo

    private val searchLauncher = registerForActivityResult(SearchForMarker()) { result ->
        Log.i(TAG, "Received $result as a search result")

        val markerId = result ?: return@registerForActivityResult

        mapScreenViewModel.viewModelScope.launch(Dispatchers.IO) {
            val (marker, markerText) = markerWithTextRepo.loadById(markerId)
            launch(Dispatchers.Main) {
                mapScreenViewModel.setFloor(marker.floor)
                mapScreenViewModel.focusOnMarker(marker, markerText)
            }
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

        var isMapInitialized by mutableStateOf(mapScreenViewModel.mapState.fullSize != IntSize.Zero)
        if (!isMapInitialized) {
            lifecycleScope.launch {
                mapScreenViewModel.initMap(MAP_NAME, TILES_PATH)
                isMapInitialized = true
            }
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

                if (isMapInitialized) {
                    MapScreen(
                        vm = mapScreenViewModel,
                        onStartSearch = searchLauncher::launch
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background)
                    )
                }
            }
        }
    }
}
