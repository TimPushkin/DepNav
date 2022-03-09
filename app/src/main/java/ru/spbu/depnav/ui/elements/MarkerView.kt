package ru.spbu.depnav.ui.elements

import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.models.Marker

private const val SIZE = 20

@Composable
fun MarkerView(type: Marker.MarkerType, modifier: Modifier = Modifier) = when (type) {
    Marker.MarkerType.ENTRANCE -> Text("🏢", modifier.size(SIZE.dp))
    Marker.MarkerType.ROOM -> Text("🚪", modifier.size(SIZE.dp))
    Marker.MarkerType.STAIRS_UP -> Text("⬆", modifier.size(SIZE.dp))
    Marker.MarkerType.STAIRS_DOWN -> Text("⬇", modifier.size(SIZE.dp))
    Marker.MarkerType.STAIRS_BOTH -> Text("↕", modifier.size(SIZE.dp))
    Marker.MarkerType.ELEVATOR -> Text("\uD83D\uDED7", modifier.size(SIZE.dp))
    Marker.MarkerType.WC_MAN -> Text("🚹", modifier.size(SIZE.dp))
    Marker.MarkerType.WC_WOMAN -> Text("🚺", modifier.size(SIZE.dp))
    Marker.MarkerType.WC -> Text("🚻", modifier.size(SIZE.dp))
}
