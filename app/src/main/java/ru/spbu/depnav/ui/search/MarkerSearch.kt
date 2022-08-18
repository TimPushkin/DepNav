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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.spbu.depnav.R
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING

/** Screen containing a marker search and the results found. */
@Composable
fun MarkerSearch(
    matches: List<MarkerText>,
    onSearch: (String) -> Unit,
    onClear: () -> Unit,
    onResultClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(modifier)
    ) {
        SearchField(
            modifier = Modifier.fillMaxWidth(),
            placeholder = stringResource(R.string.search_markers),
            onTextChange = onSearch,
            onClear = onClear
        )

        Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.2f))

        SearchResults(
            markerTexts = matches,
            onResultClick = onResultClick
        )
    }
}

@Composable
private fun SearchResults(markerTexts: List<MarkerText>, onResultClick: (Int) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxWidth()) {
        items(markerTexts) { markerText ->
            if (markerText.title == null) return@items

            Column(
                modifier = Modifier
                    .clickable { onResultClick(markerText.markerId) }
                    .fillMaxWidth()
                    .padding(DEFAULT_PADDING)
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
