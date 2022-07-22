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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.R
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.model.Marker.MarkerType
import ru.spbu.depnav.ui.theme.DepNavTheme

private val SIZE = 20.dp
private val ELEVATION = 2.dp
private val TEXT_PADDING = 5.dp

/**
 * Visual representation of a [Marker].
 */
@Composable
fun MarkerView(title: String, type: MarkerType, isClosed: Boolean, modifier: Modifier = Modifier) =
    when (type) {
        MarkerType.ENTRANCE -> MarkerIcon(
            painter = painterResource(R.drawable.door_emoji),
            faded = isClosed,
            contentDescription = "Entrance",
            modifier = modifier
        )
        MarkerType.ROOM -> RoomIcon(
            name = title,
            faded = isClosed,
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
            .size(SIZE)
            .then(modifier) // Should be set before setting the shadow
            .shadow(ELEVATION),
        alpha = if (faded) 0.5f else 1f,
        colorFilter =
        if (faded) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null
    )
}

@Composable
private fun RoomIcon(name: String, faded: Boolean, modifier: Modifier = Modifier) {
    Card(
        modifier = Modifier
            .alpha(if (faded) 0.3f else 1f)
            .then(modifier),
        shape = MaterialTheme.shapes.small,
        elevation = ELEVATION
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(TEXT_PADDING),
            maxLines = 1
        )
    }
}

@Preview
@Composable
private fun MarkerIconPreview() {
    DepNavTheme {
        MarkerIcon(
            painter = painterResource(R.drawable.keycap_asterisk_emoji),
            faded = false,
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun MarkerIconFadedPreview() {
    DepNavTheme {
        MarkerIcon(
            painter = painterResource(R.drawable.keycap_asterisk_emoji),
            faded = true,
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun RoomIconPreview() {
    DepNavTheme {
        RoomIcon(
            name = "1337",
            faded = false,
        )
    }
}

@Preview
@Composable
private fun RoomIconFadedPreview() {
    DepNavTheme {
        RoomIcon(
            name = "1337",
            faded = true
        )
    }
}
