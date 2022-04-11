package ru.spbu.depnav.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun FloorSwitch(
    floor: Int,
    modifier: Modifier = Modifier,
    onClick: (new: Int) -> Unit,
    minFloor: Int = 1,
    maxFloor: Int
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = { onClick(floor + 1) },
            enabled = floor < maxFloor
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up arrow")
        }

        Text(floor.toString(10))

        Button(
            onClick = { onClick(floor - 1) },
            enabled = floor > minFloor
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down arrow")
        }
    }
}
