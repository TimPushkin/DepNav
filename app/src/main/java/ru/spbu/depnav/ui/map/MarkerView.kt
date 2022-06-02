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

private const val LARGE_SCALE_FACTOR = 1.5f

/**
 * Visual representation of a [Marker].
 */
@Composable
fun MarkerView(
    title: String,
    type: MarkerType,
    isClosed: Boolean,
    isHighlighted: Boolean,
    modifier: Modifier = Modifier
) =
    when (type) {
        MarkerType.ENTRANCE -> MarkerIcon(
            painter = painterResource(R.drawable.door_emoji),
            faded = isClosed,
            large = isHighlighted,
            contentDescription = "Entrance",
            modifier = modifier
        )
        MarkerType.ROOM -> RoomIcon(
            name = title,
            faded = isClosed,
            large = isHighlighted,
            modifier = modifier
        )
        MarkerType.STAIRS_UP -> MarkerIcon(
            painter = painterResource(R.drawable.up_arrow_emoji),
            faded = isClosed,
            large = isHighlighted,
            contentDescription = "Stairs up",
            modifier = modifier
        )
        MarkerType.STAIRS_DOWN -> MarkerIcon(
            painter = painterResource(R.drawable.down_arrow_emoji),
            faded = isClosed,
            large = isHighlighted,
            contentDescription = "Stairs down",
            modifier = modifier
        )
        MarkerType.STAIRS_BOTH -> MarkerIcon(
            painter = painterResource(R.drawable.up_down_arrow_emoji),
            faded = isClosed,
            large = isHighlighted,
            contentDescription = "Stairs up and down",
            modifier = modifier
        )
        MarkerType.ELEVATOR -> MarkerIcon(
            painter = painterResource(R.drawable.elevator_emoji),
            faded = isClosed,
            large = isHighlighted,
            contentDescription = "Elevator",
            modifier = modifier
        )
        MarkerType.WC_MAN -> MarkerIcon(
            painter = painterResource(R.drawable.mens_room_emoji),
            faded = isClosed,
            large = isHighlighted,
            contentDescription = "Men's room",
            modifier = modifier
        )
        MarkerType.WC_WOMAN -> MarkerIcon(
            painter = painterResource(R.drawable.womens_room_emoji),
            faded = isClosed,
            large = isHighlighted,
            contentDescription = "Women's room",
            modifier = modifier
        )
        MarkerType.WC -> MarkerIcon(
            painter = painterResource(R.drawable.restroom_emoji),
            faded = isClosed,
            large = isHighlighted,
            contentDescription = "Restroom",
            modifier = modifier
        )
        MarkerType.OTHER -> MarkerIcon(
            painter = painterResource(R.drawable.keycap_asterisk_emoji),
            faded = isClosed,
            large = isHighlighted,
            contentDescription = "Other marker",
            modifier = modifier
        )
    }

@Composable
private fun MarkerIcon(
    painter: Painter,
    faded: Boolean,
    large: Boolean,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val size = 20.dp
    val shadow = 2.dp

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = Modifier
            .size(if (large) size * LARGE_SCALE_FACTOR else size)
            .shadow(if (large) shadow * LARGE_SCALE_FACTOR else shadow)
            .then(modifier),
        alpha = if (faded) 0.5f else 1f,
        colorFilter =
        if (faded) ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) }) else null
    )
}

@Composable
private fun RoomIcon(name: String, faded: Boolean, large: Boolean, modifier: Modifier = Modifier) {
    val elevation = 2.dp
    val textPadding = 5.dp

    Card(
        modifier = Modifier
            .alpha(if (faded) 0.3f else 1f)
            .then(modifier),
        shape = MaterialTheme.shapes.small,
        elevation = if (large) elevation * LARGE_SCALE_FACTOR else elevation
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(if (large) textPadding * LARGE_SCALE_FACTOR else textPadding),
            maxLines = 1,
            style = MaterialTheme.typography.body1.let { style ->
                if (large) style.copy(fontSize = style.fontSize * LARGE_SCALE_FACTOR) else style
            }
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
            large = false,
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
            large = false,
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun MarkerIconLargePreview() {
    DepNavTheme {
        MarkerIcon(
            painter = painterResource(R.drawable.keycap_asterisk_emoji),
            faded = false,
            large = true,
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
            large = false
        )
    }
}

@Preview
@Composable
private fun RoomIconFadedPreview() {
    DepNavTheme {
        RoomIcon(
            name = "1337",
            faded = true,
            large = false
        )
    }
}

@Preview
@Composable
private fun RoomIconLargePreview() {
    DepNavTheme {
        RoomIcon(
            name = "1337",
            faded = false,
            large = true
        )
    }
}
