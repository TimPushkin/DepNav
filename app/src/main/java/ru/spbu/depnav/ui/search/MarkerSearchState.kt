package ru.spbu.depnav.ui.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.spbu.depnav.db.MarkerTextDao
import ru.spbu.depnav.model.MarkerText

private const val TAG = "MarkerSearchViewModel"

/**
 * State of the [MarkerSearch].
 */
class MarkerSearchState : ViewModel() {
    private val _matchedMarkers = MutableStateFlow(emptyList<MarkerText>())

    /**
     * Markers that were found by the search.
     */
    val matchedMarkers: StateFlow<List<MarkerText>>
        get() = _matchedMarkers

    /**
     * Initiate a marker search with the provided text on the specified language. The provided DAO
     * will be used for the search.
     */
    fun search(text: String, markerTextDao: MarkerTextDao, language: MarkerText.LanguageId) {
        if (text.isBlank()) {
            _matchedMarkers.value = emptyList()
            return
        }

        viewModelScope.launch {
            Log.v(TAG, "Processing query $text with language $language")
            val query = text.split(' ').joinToString(" ") { "$it*" }
            val matches = markerTextDao.loadByTokens(query, language)
            Log.v(TAG, "Found ${matches.size} matches")
            _matchedMarkers.value = matches
        }
    }

    /**
     * Clear the search results.
     */
    fun clear() {
        _matchedMarkers.value = emptyList()
    }
}
