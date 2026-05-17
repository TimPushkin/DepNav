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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
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
private const val SEARCH_HISTORY_SIZE = 10

/** View model for map markers search on [MapScreen][ru.spbu.depnav.ui.screen.MapScreen]. */
@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
class SearchViewModel @Inject constructor(
    private val searchHistoryRepo: SearchHistoryRepo,
    private val markerRepo: MarkerWithTextRepo,
    prefs: PreferencesManager
) : ViewModel() {
    private val languageState = MutableStateFlow(Locale.current.toLanguage())
    private val _resultsFlow = MutableStateFlow(SearchResults(emptyList(), true))

    val queryState = TextFieldState()
    val resultsFlow = _resultsFlow.asStateFlow()

    init {
        combine(
            prefs.selectedMapIdFlow.filterNotNull(),
            snapshotFlow { queryState.text },
            languageState
        ) { mapId, query, language ->
            _resultsFlow.value = search(mapId, query.toString(), language)
        }
            .launchIn(viewModelScope)
    }

    private suspend fun search(mapId: Int, query: String, language: Language): SearchResults =
        withContext(Dispatchers.IO) {
            if (query.isNotEmpty()) {
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
        }.apply {
            Log.d(TAG, "Query '$query', map $mapId, $language: ${items.size} items")
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
        languageState.value = locale.toLanguage()
    }
}

/** Contents to be displayed as search results. */
data class SearchResults(
    /** Contents themselves. */
    val items: List<MarkerWithText>,
    /** Whether these are the search history entries or the actual search results. */
    val isHistory: Boolean
)
