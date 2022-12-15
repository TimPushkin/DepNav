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

package ru.spbu.depnav.ui.map

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import ru.spbu.depnav.R
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING
import ru.spbu.depnav.ui.theme.DepNavTheme

/** Lines with information about a marker. */
@Composable
fun MarkerInfoLines(
    title: String,
    description: String?,
    isClosed: Boolean,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(DEFAULT_PADDING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()

        Column(verticalArrangement = Arrangement.spacedBy(DEFAULT_PADDING / 2)) {
            CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.titleLarge) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title.ifBlank { stringResource(R.string.no_title) },
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (isClosed) {
                        CompositionLocalProvider(
                            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
                        ) {
                            Text(
                                text = "•",
                                modifier = Modifier.padding(horizontal = DEFAULT_PADDING),
                                fontWeight = FontWeight.Light,
                            )

                            Text(
                                text = stringResource(R.string.closed),
                                fontWeight = FontWeight.Light,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun MarkerInfoShortPreview() {
    DepNavTheme {
        Column {
            MarkerInfoLines(
                title = "Some title",
                isClosed = true,
                description = "Some description"
            ) {
                MarkerView(
                    title = "Some title 1",
                    type = Marker.MarkerType.ROOM,
                    isClosed = false,
                    simplified = true
                )
            }
        }
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun MarkerInfoLongPreview() {
    DepNavTheme {
        Column {
            MarkerInfoLines(
                title = "Some very very very very very very long title",
                isClosed = true,
                description = "Some very very very very very very long description"
            ) {
                MarkerView(
                    title = "Some title 2",
                    type = Marker.MarkerType.ROOM,
                    isClosed = false,
                    simplified = true
                )
            }
        }
    }
}
