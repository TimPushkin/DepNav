package ru.spbu.depnav.ui

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
    modifier: Modifier = Modifier,
    onClick: (new: Int) -> Unit,
    minFloor: Int = 1,
    maxFloor: Int
) {
    var currentFloor by remember { mutableStateOf(1) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                if (currentFloor < maxFloor) {
                    currentFloor++
                    onClick(currentFloor)
                }
            }
        ) {
            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up arrow")
        }

        Text(currentFloor.toString(10))

        Button(
            onClick = {
                if (currentFloor > minFloor) {
                    currentFloor--
                    onClick(currentFloor)
                }
            }
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down arrow")
        }
    }
}
