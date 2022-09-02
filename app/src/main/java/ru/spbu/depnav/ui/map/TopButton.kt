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

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.ui.theme.DEFAULT_ELEVATION
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING
import ru.spbu.depnav.ui.theme.FADED_ALPHA

private val HEIGHT = 68.dp

/** Button with a search icon and text. */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TopButton(
    text: String,
    onMenuClick: () -> Unit,
    onSurfaceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onSurfaceClick,
        modifier = Modifier
            .height(HEIGHT)
            .then(modifier),
        shape = CircleShape,
        elevation = DEFAULT_ELEVATION
    ) {
        Row(
            modifier = Modifier.padding(DEFAULT_PADDING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Rounded.Menu, contentDescription = "Open menu")
            }

            Text(
                text = text,
                modifier = Modifier.alpha(FADED_ALPHA),
                maxLines = 1
            )
        }
    }
}
