package ru.spbu.depnav.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
            onClear = {
                searchText = ""
                markerSearchViewModel.onClear()
            }
        )

        Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f))

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

            // TODO: make a row with MarkerView to the left and description button to the right

            Column(
                modifier = Modifier
                    .clickable { onResultClick(markerText.markerId) }
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(markerText.title)
                markerText.description?.let {
                    Text(it, color = MaterialTheme.colors.onSurface.copy(alpha = 0.45f))
                }
            }

            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f))
        }
    }
}
