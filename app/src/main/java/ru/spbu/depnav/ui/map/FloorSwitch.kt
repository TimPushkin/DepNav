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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ru.spbu.depnav.ui.theme.DepNavTheme

/** Two buttons to switch the current map one floor up or down. */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FloorSwitch(
    floor: Int,
    minFloor: Int,
    maxFloor: Int,
    modifier: Modifier = Modifier,
    onClick: (new: Int) -> Unit
) {
    Surface(
        modifier = modifier,
        shape = CircleShape
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(
                onClick = { onClick(floor + 1) },
                enabled = floor < maxFloor
            ) {
                Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = "Up arrow")
            }

            AnimatedContent(
                targetState = floor,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInVertically { height -> -height } + fadeIn() with
                            slideOutVertically { height -> height } + fadeOut()
                    } else {
                        slideInVertically { height -> height } + fadeIn() with
                            slideOutVertically { height -> -height } + fadeOut()
                    } using SizeTransform(clip = false)
                }
            ) { targetFloor ->
                Text(targetFloor.toString())
            }

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
