package ru.spbu.depnav.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.spbu.depnav.model.Marker.MarkerType

@Composable
fun MarkerView(type: MarkerType, modifier: Modifier = Modifier) {
    val text = when (type) {
        MarkerType.ENTRANCE -> "🏢"
        MarkerType.ROOM -> "🚪"
        MarkerType.STAIRS_UP -> "⬆"
        MarkerType.STAIRS_DOWN -> "⬇"
        MarkerType.STAIRS_BOTH -> "↕"
        MarkerType.ELEVATOR -> "\uD83D\uDED7"
        MarkerType.WC_MAN -> "🚹"
        MarkerType.WC_WOMAN -> "🚺"
        MarkerType.WC -> "🚻"
        MarkerType.OTHER -> "🔶"
    }

    return Text(text, modifier)
}
