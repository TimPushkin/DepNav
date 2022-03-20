package ru.spbu.depnav.ui.elements.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import ru.spbu.depnav.model.MarkerText
import ru.spbu.depnav.viewmodel.MarkerSearchViewModel

@Composable
fun MarkerSearchView(
    markerSearchViewModel: MarkerSearchViewModel,
    onResultClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchText by remember { mutableStateOf("") }
    val matches by markerSearchViewModel.matchedMarkers.collectAsState(emptyList()) // TODO: make safer

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        SearchField(
            text = searchText,
            placeholder = "Search markers",
            onTextChange = {
                searchText = it
                markerSearchViewModel.onSearch(it)
            },
            onClear = markerSearchViewModel::onClear
        )
        SearchResults(
            markerTexts = matches,
            onResultClick = onResultClick
        )
    }
}

@Composable
private fun SearchResults(
    markerTexts: List<MarkerText>,
    onResultClick: (Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(markerTexts) { markerText ->
            if (markerText.title == null) return@items

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onResultClick(markerText.markerId) }
            ) {
                Text(markerText.title)
                markerText.description?.let { Text(it) }
            }
        }
    }
}
