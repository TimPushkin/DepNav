package ru.spbu.depnav.ui.map

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.R
import ru.spbu.depnav.model.Marker.MarkerType
import ru.spbu.depnav.ui.theme.DepNavTheme

// TODO: try to use clickable icons instead of clickable markers

@Composable
fun MarkerView(title: String, type: MarkerType, modifier: Modifier = Modifier) = when (type) {
    MarkerType.ENTRANCE -> MarkerIcon(
        painter = painterResource(R.drawable.door_emoji),
        contentDescription = "Entrance",
        modifier = modifier
    )
    MarkerType.ROOM -> RoomIcon(
        name = title,
        modifier = modifier
    )
    MarkerType.STAIRS_UP -> MarkerIcon(
        painter = painterResource(R.drawable.up_arrow_emoji),
        contentDescription = "Stairs up",
        modifier = modifier
    )
    MarkerType.STAIRS_DOWN -> MarkerIcon(
        painter = painterResource(R.drawable.down_arrow_emoji),
        contentDescription = "Stairs down",
        modifier = modifier
    )
    MarkerType.STAIRS_BOTH -> MarkerIcon(
        painter = painterResource(R.drawable.up_down_arrow_emoji),
        contentDescription = "Stairs up and down",
        modifier = modifier
    )
    MarkerType.ELEVATOR -> MarkerIcon(
        painter = painterResource(R.drawable.elevator_emoji),
        contentDescription = "Elevator",
        modifier = modifier
    )
    MarkerType.WC_MAN -> MarkerIcon(
        painter = painterResource(R.drawable.mens_room_emoji),
        contentDescription = "Men's room",
        modifier = modifier
    )
    MarkerType.WC_WOMAN -> MarkerIcon(
        painter = painterResource(R.drawable.womens_room_emoji),
        contentDescription = "Women's room",
        modifier = modifier
    )
    MarkerType.WC -> MarkerIcon(
        painter = painterResource(R.drawable.restroom_emoji),
        contentDescription = "Restroom",
        modifier = modifier
    )
    MarkerType.OTHER -> MarkerIcon(
        painter = painterResource(R.drawable.keycap_asterisk_emoji),
        contentDescription = "Other marker",
        modifier = modifier
    )
}

@Composable
private fun MarkerIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = Modifier
            .size(20.dp)
            .shadow(2.dp)
            .then(modifier),
        tint = Color.Unspecified
    )
}

@Composable
private fun RoomIcon(name: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        elevation = 2.dp
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(5.dp),
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
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun RoomIconPreview() {
    DepNavTheme {
        RoomIcon(name = "1337")
    }
}
