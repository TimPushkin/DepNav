package ru.spbu.depnav.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.ui.theme.DepNavTheme

/**
 * Two buttons to switch the current map one floor up or down.
 */
@Composable
fun FloorSwitch(
    floor: Int,
    minFloor: Int,
    maxFloor: Int,
    modifier: Modifier = Modifier,
    onClick: (new: Int) -> Unit
) {
    Card(
        modifier = modifier,
        shape = CircleShape,
        elevation = 5.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = { onClick(floor + 1) },
                enabled = floor < maxFloor
            ) {
                Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Up arrow")
            }

            Text(floor.toString(10))

            IconButton(
                onClick = { onClick(floor - 1) },
                enabled = floor > minFloor
            ) {
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = "Down arrow")
            }
        }
    }
}

@Preview
@Composable
private fun FloorSwitchPreview() {
    DepNavTheme {
        FloorSwitch(
            floor = 1,
            minFloor = 1,
            maxFloor = 2,
            onClick = {}
        )
    }
}
