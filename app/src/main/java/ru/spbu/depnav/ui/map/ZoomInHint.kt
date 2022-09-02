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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import ru.spbu.depnav.R
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING
import ru.spbu.depnav.ui.theme.FADED_ALPHA

/** Hint that tells a user to zoom in to see map markers. */
@Composable
fun ZoomInHint() {
    Row(
        modifier = Modifier.padding(DEFAULT_PADDING),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val textSize =
            with(LocalDensity.current) { MaterialTheme.typography.caption.fontSize.toDp() }

        Icon(
            painter = painterResource(R.drawable.ic_pinch_zoom_in),
            contentDescription = "Pinch to zoom in",
            modifier = Modifier
                .size(textSize * 2)
                .alpha(FADED_ALPHA)
        )

        Spacer(modifier = Modifier.width(DEFAULT_PADDING))

        Text(
            text = stringResource(R.string.zoom_in_to_see_markers),
            modifier = Modifier.alpha(FADED_ALPHA),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.caption
        )
    }
}
