/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofei Pushkin
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

package ru.spbu.depnav.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import ru.spbu.depnav.R
import ru.spbu.depnav.data.model.Language
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING
import ru.spbu.depnav.ui.theme.DISABLED_ALPHA
import ru.spbu.depnav.ui.theme.DepNavTheme
import ru.spbu.depnav.ui.viewmodel.SearchResults

/**
 * Column with clickable information about markers that were found by the search.
 *
 * @param results results sorted by importance: elements listed first will be displayed on top.
 */
@Composable
fun SearchResultsView(
    results: SearchResults,
    onScroll: (onTop: Boolean) -> Unit,
    onResultClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(top = DEFAULT_PADDING / 2),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(results.items.asReversed()) { (marker, markerText) ->
            if (markerText.title == null) return@items

            SearchResultView(
                marker = marker,
                markerText = markerText,
                onClick = onResultClick,
                trailingIcon = (
                    @Composable {
                        Icon(
                            painter = painterResource(R.drawable.ic_searched_for),
                            contentDescription = null,
                            modifier = Modifier
                                .size(26.dp)
                                .alpha(DISABLED_ALPHA)
                        )
                    }
                    ).takeIf { results.isHistory }
            )
        }
    }

    LaunchedEffect(state) {
        snapshotFlow {
            state.run { firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0 }
        }
            .onEach { onScroll(it) }
            .launchIn(scope)
    }
}

@Composable
private fun SearchResultView(
    marker: Marker,
    markerText: MarkerText,
    onClick: (Int) -> Unit,
    trailingIcon: (@Composable () -> Unit)?
) {
    Row(
        modifier = Modifier
            .clickable { onClick(marker.id) }
            .fillMaxWidth()
            .height(LocalViewConfiguration.current.minimumTouchTargetSize.height),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(DEFAULT_PADDING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarkerView(
                title = null, // Not needed for a simplified view
                type = marker.type,
                simplified = true
            )

            with(markerText) {
                MarkerTextLines(
                    title = if (title.isNullOrBlank()) stringResource(R.string.no_title) else title,
                    location = location,
                    description = description
                )
            }
        }

        trailingIcon?.invoke()
    }
}

@Composable
private fun MarkerTextLines(title: String, location: String?, description: String?) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val lineLength = title.length.toFloat() + (location?.length ?: 0)

            if (!location.isNullOrBlank()) {
                Text(
                    location,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(location.length / lineLength, fill = false)
                )

                Text(" › ")
            }

            Text(
                title,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(title.length / lineLength, fill = false)
            )
        }

        if (!description.isNullOrBlank()) {
            Text(
                description,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.alpha(DISABLED_ALPHA),
            )
        }
    }
}

@Composable
@Preview
@Suppress("UnusedPrivateMember")
private fun SearchResultUsualPreview() {
    DepNavTheme {
        Surface {
            SearchResultView(
                marker = Marker(1, 0, Marker.MarkerType.ROOM, 1, 0.0, 0.0),
                markerText = MarkerText(1, Language.EN, "1234", "Location", "Some description"),
                onClick = {}
            ) {}
        }
    }
}

@Composable
@Preview
@Suppress("UnusedPrivateMember")
private fun SearchResultLongContentsPreview() {
    DepNavTheme {
        Surface {
            SearchResultView(
                marker = Marker(1, 0, Marker.MarkerType.ROOM, 1, 0.0, 0.0),
                markerText = MarkerText(
                    1,
                    Language.EN,
                    "Some very very very very very long title",
                    "Some very very very very very very long location",
                    "Some very very very very very very long description"
                ),
                onClick = {}
            ) {}
        }
    }
}

@Composable
@Preview
@Suppress("UnusedPrivateMember")
private fun SearchResultHistoryPreview() {
    DepNavTheme {
        Surface {
            SearchResultView(
                marker = Marker(1, 0, Marker.MarkerType.ROOM, 1, 0.0, 0.0),
                markerText = MarkerText(1, Language.EN, "1234", "Location", "Some description"),
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_searched_for),
                    contentDescription = null,
                    modifier = Modifier
                        .size(28.dp)
                        .alpha(DISABLED_ALPHA)
                )
            }
        }
    }
}
