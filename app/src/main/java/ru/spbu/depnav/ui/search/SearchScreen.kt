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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.spbu.depnav.R
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.ui.map.MarkerView
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING
import ru.spbu.depnav.ui.theme.DepNavTheme

/** Screen containing a marker search and the results found. */
@Composable
fun SearchScreen(
    vm: SearchScreenViewModel = hiltViewModel(),
    onResultClick: (Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    val insetsNoBottom = WindowInsets.systemBars.run { exclude(only(WindowInsetsSides.Bottom)) }

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(insetsNoBottom)
        ) {
            SearchField(
                onTextChange = vm::search,
                onClear = vm::clearMatches,
                onBackClick = onNavigateBack,
                modifier = Modifier.fillMaxWidth(),
                placeholder = stringResource(R.string.search_markers)
            )

            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f))

            SearchResults(
                markersWithTexts = vm.searchMatches.ifEmpty { vm.searchHistory },
                isHistory = vm.searchMatches.isEmpty(),
                onResultClick = { markerId ->
                    vm.addToSearchHistory(markerId)
                    onResultClick(markerId)
                }
            )
        }
    }
}

@Composable
private fun SearchResults(
    markersWithTexts: Map<Marker, MarkerText>,
    isHistory: Boolean,
    onResultClick: (Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(markersWithTexts.toList()) { (marker, markerText) ->
            if (markerText.title == null) return@items

            SearchResult(marker, markerText, isHistory, onResultClick)

            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f))
        }
    }
}

@Composable
private fun SearchResult(
    marker: Marker,
    markerText: MarkerText,
    isHistory: Boolean,
    onClick: (Int) -> Unit
) {
    checkNotNull(markerText.title) { "MarkerText title cannot be null in SearchResult" }

    Row(
        modifier = Modifier
            .clickable { onClick(markerText.markerId) }
            .fillMaxWidth()
            .padding(horizontal = DEFAULT_PADDING * 2)
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarkerView(
                title = markerText.title,
                type = marker.type,
                isClosed = marker.isClosed,
                simplified = true
            )

            Column(modifier = Modifier.padding(start = DEFAULT_PADDING * 2)) {
                Text(
                    text = markerText.title,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )

                markerText.description?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.45f),
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        if (isHistory) {
            Icon(
                painter = painterResource(R.drawable.ic_history),
                contentDescription = "Search history",
                modifier = Modifier.scale(0.6f),
                tint = MaterialTheme.colors.onSurface.copy(alpha = 0.45f)
            )
        }
    }
}

@Composable
@Preview
private fun SearchResultUsualPreview() {
    DepNavTheme {
        SearchResult(
            marker = Marker(1, Marker.MarkerType.ROOM, false, 1, 0.0, 0.0),
            markerText = MarkerText(1, MarkerText.LanguageId.EN, "1234", "Some description"),
            isHistory = false,
            onClick = {}
        )
    }
}

@Composable
@Preview
private fun SearchResultHistoryPreview() {
    DepNavTheme {
        SearchResult(
            marker = Marker(1, Marker.MarkerType.ROOM, false, 1, 0.0, 0.0),
            markerText = MarkerText(1, MarkerText.LanguageId.EN, "1234", "Some description"),
            isHistory = true,
            onClick = {}
        )
    }
}
