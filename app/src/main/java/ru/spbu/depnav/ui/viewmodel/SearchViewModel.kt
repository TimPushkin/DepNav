/**
 * DepNav -- department navigator.
 * Copyright (C) 2023  Timofei Pushkin
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

package ru.spbu.depnav.ui.viewmodel

import android.util.Log
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.spbu.depnav.data.composite.MarkerWithText
import ru.spbu.depnav.data.model.Language
import ru.spbu.depnav.data.model.SearchHistoryEntry
import ru.spbu.depnav.data.model.toLanguage
import ru.spbu.depnav.data.preferences.PreferencesManager
import ru.spbu.depnav.data.repository.MarkerWithTextRepo
import ru.spbu.depnav.data.repository.SearchHistoryRepo
import javax.inject.Inject

private const val TAG = "SearchViewModel"

private const val MIN_QUERY_PERIOD_MS = 300L
private const val SEARCH_HISTORY_SIZE = 10

/** View model for map markers search on [MapScreen][ru.spbu.depnav.ui.screen.MapScreen]. */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModel @Inject constructor(
    private val searchHistoryRepo: SearchHistoryRepo,
    private val markerRepo: MarkerWithTextRepo,
    prefs: PreferencesManager
) : ViewModel() {
    // Updated only on the main thread
    private val state = MutableStateFlow(PrivateState())

    /** UI-visible state. */
    val uiState = state.map { it.toSearchUiState() }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = state.value.toSearchUiState()
    )

    private data class PrivateState(
        val mapId: Int? = null,
        val query: String = "",
        val language: Language = Locale.current.toLanguage(),
        val results: SearchResults = SearchResults(emptyList(), true)
    ) {
        fun toSearchUiState() = SearchUiState(query, results)
    }

    init {
        state
            .debounce(MIN_QUERY_PERIOD_MS)
            .distinctUntilChangedBy { Triple(it.mapId, it.query, it.language) }
            .mapLatest { (mapId, query, language) ->
                val results = withContext(Dispatchers.IO) {
                    if (mapId == null) {
                        SearchResults(emptyList(), true)
                    } else if (query.isNotEmpty()) {
                        SearchResults(
                            items = markerRepo.loadByQuery(mapId, query, language),
                            isHistory = false
                        )
                    } else {
                        SearchResults(
                            items = searchHistoryRepo.loadByMap(mapId).map {
                                markerRepo.loadById(it.markerId, language)
                            },
                            isHistory = true
                        )
                    }
                }
                Log.d(TAG, "Query '$query', map $mapId, $language: ${results.items.size} items")
                state.update { it.copy(results = results) }
            }
            .launchIn(viewModelScope)

        prefs.selectedMapIdFlow
            .onEach { mapId -> state.update { it.copy(mapId = mapId, query = "") } }
            .launchIn(viewModelScope)
    }

    /** Updates the search query and asynchronously runs marker search with it. */
    fun search(query: String) {
        state.update { it.copy(query = query) }
    }

    /** Asynchronously adds the provided marker ID to the search history. */
    fun addToSearchHistory(markerId: Int) {
        val entry = SearchHistoryEntry(markerId) // Records the timestamp
        viewModelScope.launch(Dispatchers.IO) {
            searchHistoryRepo.insertNotExceeding(entry, SEARCH_HISTORY_SIZE)
        }
    }

    /**
     * Asynchronously updates language of the search results which come from the persisted storage
     * and thus are not updated automatically by Android.
     */
    fun onLocaleChange(locale: Locale) {
        val language = locale.toLanguage()
        if (state.value.language != language) {
            Log.i(TAG, "Updating language to $language")
            state.update { it.copy(language = language) }
        }
    }
}

/** Contents to be displayed as search results. */
data class SearchResults(
    /** Contents themselves. */
    val items: List<MarkerWithText>,
    /** Whether these are the search history entries or the actual search results. */
    val isHistory: Boolean
)

/** Map markers search UI state. */
data class SearchUiState(
    /** Marker search query entered by the user. */
    val query: String = "",
    /**
     * Either the actual results of the query or a history of previous searches.
     *
     * Note that these results mey correspond not to the current query, but to some query in the
     * past while the current query is being processed.
     */
    val results: SearchResults = SearchResults(emptyList(), true)
)
