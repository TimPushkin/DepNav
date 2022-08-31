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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.data.model.SearchHistoryEntry
import ru.spbu.depnav.data.repository.MarkerWithTextRepo
import ru.spbu.depnav.data.repository.SearchHistoryRepo
import javax.inject.Inject

private const val TAG = "MarkerSearchViewModel"

private const val SEARCH_HISTORY_SIZE = 10

/** View model for [SearchScreen]. */
@HiltViewModel
class SearchScreenViewModel @Inject constructor(
    private val markerWithTextRepo: MarkerWithTextRepo,
    private val searchHistoryRepo: SearchHistoryRepo
) : ViewModel() {
    /** Markers with their texts that were found by the search. */
    var searchMatches by mutableStateOf<Map<Marker, MarkerText>>(emptyMap())
        private set

    /** Markers with their texts to suggest to a user. */
    var searchHistory by mutableStateOf<Map<Marker, MarkerText>>(emptyMap())
        private set

    init {
        searchHistoryRepo.loadAll()
            .onEach { entries ->
                val history = entries.associate { markerWithTextRepo.loadById(it.markerId) }
                searchHistory = history
            }
            .launchIn(viewModelScope)
    }

    /**
     * Initiate a marker search with the provided text on the specified language. The provided DAO
     * will be used for the search.
     */
    fun search(text: String) {
        if (text.isBlank()) {
            searchMatches = emptyMap()
            return
        }

        val language = MarkerText.LanguageId.getCurrent()
        Log.d(TAG, "Processing query $text with language $language")
        val query = text.split(' ').joinToString(" ") { "$it*" }

        viewModelScope.launch(Dispatchers.IO) {
            val matches = markerWithTextRepo.loadByTokens(query)
            Log.v(TAG, "Found ${matches.size} matches")
            launch(Dispatchers.Main) { this@SearchScreenViewModel.searchMatches = matches }
        }
    }

    /** Clear the search results. */
    fun clearMatches() {
        searchMatches = emptyMap()
    }

    /** Add the provided marker ID to the search history. */
    fun addToSearchHistory(markerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryRepo.insertNotExceeding(SearchHistoryEntry(markerId), SEARCH_HISTORY_SIZE)
        }
    }
}
