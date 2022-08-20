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

package ru.spbu.depnav.ui.search

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.data.repository.MarkerWithTextRepo
import javax.inject.Inject

private const val TAG = "MarkerSearchViewModel"

/** View model for [SearchScreen]. */
@HiltViewModel
class SearchScreenViewModel @Inject constructor(private val markerWithTextRepo: MarkerWithTextRepo) :
    ViewModel() {
    /** Markers that were found by the search. */
    var matchedMarkers by mutableStateOf<Collection<MarkerText>>(emptyList())
        private set

    /**
     * Initiate a marker search with the provided text on the specified language. The provided DAO
     * will be used for the search.
     */
    fun search(text: String) {
        if (text.isBlank()) {
            matchedMarkers = emptyList()
            return
        }

        val language = MarkerText.LanguageId.getCurrent()
        Log.d(TAG, "Processing query $text with language $language")
        val query = text.split(' ').joinToString(" ") { "$it*" }

        viewModelScope.launch(Dispatchers.IO) {
            val matches = markerWithTextRepo.loadByTokens(query, language)
            Log.v(TAG, "Found ${matches.size} matches")
            launch(Dispatchers.Main) { matchedMarkers = matches.values }
        }
    }

    /** Clear the search results. */
    fun clearResults() {
        matchedMarkers = emptyList()
    }
}
