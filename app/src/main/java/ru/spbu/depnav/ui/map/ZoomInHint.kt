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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import ru.spbu.depnav.R
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING

/** Hint that tells a user to zoom in to see map markers. */
@Composable
fun ZoomInHint() {
    CompositionLocalProvider(
        LocalTextStyle provides MaterialTheme.typography.bodySmall,
        LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(DEFAULT_PADDING),
            horizontalArrangement = Arrangement.spacedBy(DEFAULT_PADDING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_pinch_zoom_in),
                contentDescription = "Pinch to zoom in",
                modifier = Modifier.size(
                    with(LocalDensity.current) {
                        MaterialTheme.typography.bodySmall.fontSize.toDp() * 2
                    }
                )
            )

            Text(
                text = stringResource(R.string.zoom_in_to_see_markers),
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
        }
    }
}
