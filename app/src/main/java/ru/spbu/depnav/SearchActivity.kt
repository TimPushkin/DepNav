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
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import ru.spbu.depnav.ui.search.MarkerSearch
import ru.spbu.depnav.ui.search.MarkerSearchViewModel
import ru.spbu.depnav.ui.theme.DepNavTheme

private const val TAG = "SearchActivity"

/** ID of an extra containing the marker ID selected by a user after a search. */
const val EXTRA_MARKER_ID = "ru.spbu.depnav.MARKER_ID"

/** Activity which displays the search screen. */
@AndroidEntryPoint
class SearchActivity : LanguageAwareActivity() {
    private val markerSearchViewModel: MarkerSearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DepNavTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    val insetsNoBottom =
                        WindowInsets.systemBars.run { exclude(only(WindowInsetsSides.Bottom)) }
                    val searchMatches by
                    markerSearchViewModel.matchedMarkers.collectAsState(emptyList())
                    MarkerSearch(
                        matches = searchMatches,
                        onSearch = this::onSearch,
                        onClear = this::onClear,
                        onResultClick = this::onMarkerSelected,
                        modifier = Modifier.windowInsetsPadding(insetsNoBottom)
                    )
                }
            }
        }
    }

    private fun onSearch(text: String) {
        markerSearchViewModel.search(text, systemLanguage)
    }

    private fun onClear() {
        markerSearchViewModel.clear()
    }

    private fun onMarkerSelected(id: Int) {
        Log.i(TAG, "Marker $id has been selected")

        val intent = Intent(this, MainActivity::class.java).putExtra(EXTRA_MARKER_ID, id)
        setResult(Activity.RESULT_OK, intent)

        finish()
    }
}
