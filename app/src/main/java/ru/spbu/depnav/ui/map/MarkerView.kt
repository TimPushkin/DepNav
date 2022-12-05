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

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.R
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.Marker.MarkerType
import ru.spbu.depnav.ui.theme.DepNavTheme

private val ICON_SIZE = 20.dp

/** Visual representation of a [Marker]. */
@Composable
fun MarkerView(
    title: String,
    type: MarkerType,
    isClosed: Boolean,
    modifier: Modifier = Modifier,
    simplified: Boolean = false
) = when (type) {
    MarkerType.ROOM -> if (simplified) {
        MarkerIcon(
            painter = painterResource(R.drawable.key_emoji),
            faded = isClosed,
            contentDescription = "Room",
            modifier = modifier
        )
    } else {
        RoomName(
            name = title,
            lineTrough = isClosed,
            modifier = modifier
        )
    }
    MarkerType.ENTRANCE -> MarkerIcon(
        painter = painterResource(R.drawable.door_emoji),
        faded = isClosed,
        contentDescription = "Entrance",
        modifier = modifier
    )
    MarkerType.STAIRS_UP -> MarkerIcon(
        painter = painterResource(R.drawable.up_arrow_emoji),
        faded = isClosed,
        contentDescription = "Stairs up",
        modifier = modifier
    )
    MarkerType.STAIRS_DOWN -> MarkerIcon(
        painter = painterResource(R.drawable.down_arrow_emoji),
        faded = isClosed,
        contentDescription = "Stairs down",
        modifier = modifier
    )
    MarkerType.STAIRS_BOTH -> MarkerIcon(
        painter = painterResource(R.drawable.up_down_arrow_emoji),
        faded = isClosed,
        contentDescription = "Stairs up and down",
        modifier = modifier
    )
    MarkerType.ELEVATOR -> MarkerIcon(
        painter = painterResource(R.drawable.elevator_emoji),
        faded = isClosed,
        contentDescription = "Elevator",
        modifier = modifier
    )
    MarkerType.WC_MAN -> MarkerIcon(
        painter = painterResource(R.drawable.mens_room_emoji),
        faded = isClosed,
        contentDescription = "Men's room",
        modifier = modifier
    )
    MarkerType.WC_WOMAN -> MarkerIcon(
        painter = painterResource(R.drawable.womens_room_emoji),
        faded = isClosed,
        contentDescription = "Women's room",
        modifier = modifier
    )
    MarkerType.WC -> MarkerIcon(
        painter = painterResource(R.drawable.restroom_emoji),
        faded = isClosed,
        contentDescription = "Restroom",
        modifier = modifier
    )
    MarkerType.OTHER -> MarkerIcon(
        painter = painterResource(R.drawable.keycap_asterisk_emoji),
        faded = isClosed,
        contentDescription = "Other marker",
        modifier = modifier
    )
}

@Composable
private fun MarkerIcon(
    painter: Painter,
    faded: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = Modifier
            .size(ICON_SIZE)
            .then(modifier),
        alpha = if (faded) 0.38f else 1f,
        colorFilter = ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
            .takeIf { faded }
    )
}

@Composable
private fun RoomName(name: String, lineTrough: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier,
        maxLines = 1,
        style = LocalTextStyle.current.copy(
            textDecoration = TextDecoration.LineThrough.takeIf { lineTrough },
            shadow = Shadow(color = MaterialTheme.colorScheme.background, blurRadius = 6f)
        )
    )
}

@Preview
@Composable
private fun MarkerIconPreview() {
    DepNavTheme {
        MarkerView(
            title = "",
            type = MarkerType.OTHER,
            isClosed = false
        )
    }
}

@Preview
@Composable
private fun MarkerIconClosedPreview() {
    DepNavTheme {
        MarkerView(
            title = "",
            type = MarkerType.OTHER,
            isClosed = true
        )
    }
}

@Preview
@Composable
private fun RoomIconPreview() {
    DepNavTheme(darkTheme = false) {
        MarkerView(
            title = "1337",
            type = MarkerType.ROOM,
            isClosed = false
        )
    }
}

@Preview
@Composable
private fun RoomIconClosedPreview() {
    DepNavTheme {
        MarkerView(
            title = "1337",
            type = MarkerType.ROOM,
            isClosed = true
        )
    }
}
