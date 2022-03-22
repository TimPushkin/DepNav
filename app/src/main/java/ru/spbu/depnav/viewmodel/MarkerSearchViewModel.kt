package ru.spbu.depnav.viewmodel

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import ru.spbu.depnav.db.MarkerTextDao
import ru.spbu.depnav.model.MarkerText

private const val TAG = "MarkerSearchViewModel"

class MarkerSearchViewModel(
    private val mMarkerTextDao: MarkerTextDao,
    var language: MarkerText.LanguageId
) : ViewModel() {
    private val _matchedMarkers = MutableStateFlow(emptyList<MarkerText>())
    val matchedMarkers: StateFlow<List<MarkerText>>
        get() = _matchedMarkers

    fun onSearch(text: String) {
        if (text.isBlank()) {
            _matchedMarkers.value = emptyList()
            return
        }

        viewModelScope.launch {
            Log.v(TAG, "Processing query $text with language $language")
            val query = text.split(' ').joinToString(" ") { "$it*" }
            val matches = mMarkerTextDao.loadByTokens(query, language)
            Log.v(TAG, "Found ${matches.size} matches")
            _matchedMarkers.value = matches
        }
    }

    fun onClear() {
        _matchedMarkers.value = emptyList()
    }
}
