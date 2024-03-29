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

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.R
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.Marker.MarkerType
import ru.spbu.depnav.ui.theme.DepNavTheme

/** Size of marker icon in each dimension. */
val MARKER_ICON_SIZE = 20.dp

/** Visual representation of a [Marker]. */
@Composable
fun MarkerView(
    title: String?,
    type: MarkerType,
    modifier: Modifier = Modifier,
    simplified: Boolean = false
) = when (type) {
    MarkerType.ROOM -> if (simplified) {
        MarkerIcon(
            painter = painterResource(R.drawable.mrk_room),
            contentDescription = stringResource(R.string.label_room_icon),
            modifier = modifier
        )
    } else {
        RoomName(
            name = requireNotNull(title) { "Room markers must have titles" },
            modifier = modifier
        )
    }
    MarkerType.ENTRANCE -> MarkerIcon(
        painter = painterResource(R.drawable.mrk_entrance),
        contentDescription = stringResource(R.string.label_entrance_icon),
        modifier = modifier
    )
    MarkerType.STAIRS_UP -> MarkerIcon(
        painter = painterResource(R.drawable.mrk_stairs_up),
        contentDescription = stringResource(R.string.label_stairs_up_icon),
        modifier = modifier
    )
    MarkerType.STAIRS_DOWN -> MarkerIcon(
        painter = painterResource(R.drawable.mrk_stairs_down),
        contentDescription = stringResource(R.string.label_stairs_down_icon),
        modifier = modifier
    )
    MarkerType.STAIRS_BOTH -> MarkerIcon(
        painter = painterResource(R.drawable.mrk_stairs),
        contentDescription = stringResource(R.string.label_stairs_both_icon),
        modifier = modifier
    )
    MarkerType.ELEVATOR -> MarkerIcon(
        painter = painterResource(R.drawable.mrk_elevator),
        contentDescription = stringResource(R.string.label_elevator_icon),
        modifier = modifier
    )
    MarkerType.WC_MAN -> MarkerIcon(
        painter = painterResource(R.drawable.mrk_wc_man),
        contentDescription = stringResource(R.string.label_wc_man_icon),
        modifier = modifier
    )
    MarkerType.WC_WOMAN -> MarkerIcon(
        painter = painterResource(R.drawable.mrk_wc_woman),
        contentDescription = stringResource(R.string.label_wc_woman_icon),
        modifier = modifier
    )
    MarkerType.WC -> MarkerIcon(
        painter = painterResource(R.drawable.mrk_wc),
        contentDescription = stringResource(R.string.label_wc_icon),
        modifier = modifier
    )
    MarkerType.OTHER -> MarkerIcon(
        painter = painterResource(R.drawable.mrk_other),
        contentDescription = stringResource(R.string.label_other_icon),
        modifier = modifier
    )
}

@Composable
private fun MarkerIcon(painter: Painter, contentDescription: String?, modifier: Modifier) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = Modifier
            .size(MARKER_ICON_SIZE)
            .then(modifier)
    )
}

@Composable
private fun RoomName(name: String, modifier: Modifier) {
    Text(
        text = name,
        modifier = modifier,
        maxLines = 1,
        style = LocalTextStyle.current.copy(
            shadow = Shadow(color = MaterialTheme.colorScheme.background, blurRadius = 6f)
        )
    )
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun MarkerIconPreview() {
    DepNavTheme { MarkerView(title = "", type = MarkerType.OTHER) }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun RoomIconPreview() {
    DepNavTheme { MarkerView(title = "1337", type = MarkerType.ROOM) }
}
