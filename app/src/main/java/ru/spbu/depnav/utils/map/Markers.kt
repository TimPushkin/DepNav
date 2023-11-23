/**
 * DepNav -- department navigator.
 * Copyright (C) 2023  Timofei Pushkin
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

package ru.spbu.depnav.utils.map

import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import ovh.plrapps.mapcompose.api.ExperimentalClusteringApi
import ovh.plrapps.mapcompose.api.addMarker
import ovh.plrapps.mapcompose.ui.state.MapState
import ovh.plrapps.mapcompose.ui.state.markers.model.RenderingStrategy
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.ui.component.MarkerView

private const val INVISIBLE_UNTIL_SCALE_PORTION = 0.2f
private const val OPAQUE_SINCE_SCALE_PORTION = 0.5f
private const val INVISIBLE_UNTIL_SCALED_BY = 0.1f
private const val OPAQUE_SINCE_SCALED_BY = 0.5f

/** Returns alpha value to use for map markers calculated based on the current map scale. */
fun getMarkerAlpha(minScale: Float, scale: Float, maxScale: Float) = if (minScale < maxScale) {
    val invisibleUntil = minScale + ((maxScale - minScale) * INVISIBLE_UNTIL_SCALE_PORTION)
        .coerceAtMost(INVISIBLE_UNTIL_SCALED_BY)
    val opaqueFrom = minScale + ((maxScale - minScale) * OPAQUE_SINCE_SCALE_PORTION)
        .coerceAtMost(OPAQUE_SINCE_SCALED_BY)
    val coercedScale = scale.coerceIn(invisibleUntil, opaqueFrom)
    (coercedScale - invisibleUntil) / (opaqueFrom - invisibleUntil)
} else {
    1f // minScale >= maxScale, so zooming is impossible, i.e. the scale cannot be changed
}

/** Creates a [MarkerView] and adds it to this [MapState]. */
@OptIn(ExperimentalClusteringApi::class)
fun MapState.addMarker(
    id: String,
    marker: Marker,
    markerText: MarkerText,
    alphaFlow: StateFlow<Float>
) {
    addMarker(
        id = id,
        x = marker.x,
        y = marker.y,
        clickable = markerText.run { !title.isNullOrBlank() || !description.isNullOrBlank() },
        relativeOffset = Offset(-0.5f, -0.5f),
        clipShape = null,
        renderingStrategy = RenderingStrategy.Clustering(getClustererId(marker.type))
    ) {
        val alpha by alphaFlow.collectAsStateWithLifecycle()
        if (alpha > 0f) { // Not to consume clicks when invisible
            MarkerView(
                title = markerText.title,
                type = marker.type,
                isClosed = marker.isClosed,
                modifier = Modifier.alpha(alpha)
            )
        }
    }
}
