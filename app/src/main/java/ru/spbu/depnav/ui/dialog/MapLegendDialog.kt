/**
 * DepNav -- department navigator.
 * Copyright (C) 2023  Timofei Pushkin
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

package ru.spbu.depnav.ui.dialog

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import ru.spbu.depnav.R
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.ui.component.MarkerView
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING

/** Dialog with the map legend. **/
@Composable
fun MapLegendDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.ok))
            }
        },
        title = { Text(stringResource(R.string.map_legend)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(DEFAULT_PADDING)) {
                item { LegendItem(Marker.MarkerType.ENTRANCE, R.string.entrance_descr) }
                item { LegendItem(Marker.MarkerType.STAIRS_UP, R.string.stairs_up_descr) }
                item { LegendItem(Marker.MarkerType.STAIRS_DOWN, R.string.stairs_down_descr) }
                item { LegendItem(Marker.MarkerType.STAIRS_BOTH, R.string.stairs_both_descr) }
                item { LegendItem(Marker.MarkerType.ELEVATOR, R.string.elevator_descr) }
                item { LegendItem(Marker.MarkerType.WC_MAN, R.string.wc_man_descr) }
                item { LegendItem(Marker.MarkerType.WC_WOMAN, R.string.wc_woman_descr) }
                item { LegendItem(Marker.MarkerType.WC, R.string.wc_descr) }
                item { LegendItem(Marker.MarkerType.OTHER, R.string.other_descr) }
            }
        }
    )
}

private const val INLINE_ICON_ID = "icon"

@Composable
private fun LegendItem(markerType: Marker.MarkerType, @StringRes descriptionId: Int) {
    Text(
        text = buildAnnotatedString {
            appendInlineContent(INLINE_ICON_ID)
            append(" — ${stringResource(descriptionId)}")
        },
        inlineContent = mapOf(INLINE_ICON_ID to getInlineMarkerView(markerType))
    )
}

@Composable
private fun getInlineMarkerView(type: Marker.MarkerType) = InlineTextContent(
    Placeholder(
        width = LocalTextStyle.current.lineHeight,
        height = LocalTextStyle.current.lineHeight,
        PlaceholderVerticalAlign.TextCenter
    )
) {
    MarkerView(title = null, type = type)
}
