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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.data.model.SearchHistoryEntry
import ru.spbu.depnav.data.repository.MarkerWithTextRepo
import ru.spbu.depnav.data.repository.SearchHistoryRepo
import ru.spbu.depnav.utils.preferences.PreferencesManager
import javax.inject.Inject

private const val TAG = "MarkerSearchViewModel"

private const val MIN_QUERY_PERIOD_MS = 300L
private const val SEARCH_HISTORY_SIZE = 10

/** View model for [SearchScreen]. */
@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class SearchScreenViewModel @Inject constructor(
    private val markerWithTextRepo: MarkerWithTextRepo,
    private val searchHistoryRepo: SearchHistoryRepo,
    private val prefs: PreferencesManager
) : ViewModel() {
    /** Current search query flow. Values emitted into it will be used in search. */
    val queryTextFlow = MutableSharedFlow<String?>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    /**
     * Markers with their texts that were found by the search sorted by relevance. Null if no
     * search was attempted.
     */
    var searchMatches by mutableStateOf<Map<Marker, MarkerText>?>(null)
        private set

    /** Markers with their texts that were searched in the past. */
    var searchHistory by mutableStateOf<Map<Marker, MarkerText>>(emptyMap())
        private set

    init {
        queryTextFlow
            .debounce(MIN_QUERY_PERIOD_MS) // Filter out queries that change too frequently
            .distinctUntilChanged() // Filter out identical queries
            .filterNotNull()
            .mapLatest { if (it.isNotEmpty()) search(it) else clearMatches() } // Cancel unfinished
            .launchIn(viewModelScope)

        searchHistoryRepo.loadByMap(prefs.selectedMap.persistedName)
            .distinctUntilChanged() // Filter out identical search entity lists
            .mapLatest { entries -> // Cancel previous if unfinished
                searchHistory = entries.associate { markerWithTextRepo.loadById(it.markerId) }
            }
            .launchIn(viewModelScope)
    }

    private suspend fun search(query: String) {
        val matches = withContext(Dispatchers.IO) {
            markerWithTextRepo.loadByQuery(prefs.selectedMap.persistedName, query)
        }
        Log.v(TAG, "Query '$query' has ${matches.size} matches")
        searchMatches = matches
    }

    /** Clear the search results by setting them to null. */
    fun clearMatches() {
        queryTextFlow.tryEmit(null) // Update distinctUntilChanged state
        searchMatches = null
    }

    /** Add the provided marker ID to the search history. */
    fun addToSearchHistory(markerId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryRepo.insertNotExceeding(SearchHistoryEntry(markerId), SEARCH_HISTORY_SIZE)
        }
    }
}
