package ru.spbu.depnav.ui.search

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.spbu.depnav.db.MarkerTextDao
import ru.spbu.depnav.model.MarkerText

private const val TAG = "MarkerSearchViewModel"

class MarkerSearchState : ViewModel() {
    private val _matchedMarkers = MutableStateFlow(emptyList<MarkerText>())
    val matchedMarkers: StateFlow<List<MarkerText>>
        get() = _matchedMarkers

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

    fun clear() {
        _matchedMarkers.value = emptyList()
    }
}
