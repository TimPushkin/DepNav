package ru.spbu.depnav.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.R

private val SIZE = 30.dp

/**
 * Pin for highlighting map markers.
 */
@Composable
fun Pin(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.round_pushpin_emoji),
        contentDescription = "Pin",
        modifier = Modifier
            .size(SIZE)
            .offset(y = -SIZE / 2)
            .then(modifier)
    )
}
