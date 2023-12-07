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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import ru.spbu.depnav.R
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING
import ru.spbu.depnav.ui.theme.DepNavTheme

/** Lines with information about a marker. */
@Composable
fun MarkerInfoLines(
    title: String,
    location: String?,
    description: String?,
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
            Text(
                buildAnnotatedString {
                    if (!location.isNullOrBlank()) {
                        append("$location › ")
                    }
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(title.ifBlank { stringResource(R.string.no_title) })
                    }
                },
                style = MaterialTheme.typography.titleLarge
            )

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
        Surface {
            Column {
                MarkerInfoLines(
                    title = "Some title",
                    location = "Location",
                    description = "Some description"
                ) {
                    MarkerView(
                        title = "Some title 1",
                        type = Marker.MarkerType.ROOM,
                        simplified = true
                    )
                }
            }
        }
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun MarkerInfoLongPreview() {
    DepNavTheme {
        Surface {
            Column {
                MarkerInfoLines(
                    title = "Some very very very very very very long title",
                    location = "Some very very very very very very long location",
                    description = "Some very very very very very very long description"
                ) {
                    MarkerView(
                        title = "Some title 2",
                        type = Marker.MarkerType.ROOM,
                        simplified = true
                    )
                }
            }
        }
    }
}
