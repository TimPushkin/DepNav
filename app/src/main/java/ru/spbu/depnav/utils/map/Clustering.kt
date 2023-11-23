/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofei Pushkin
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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.StateFlow
import ovh.plrapps.mapcompose.api.ExperimentalClusteringApi
import ovh.plrapps.mapcompose.api.addClusterer
import ovh.plrapps.mapcompose.ui.state.MapState
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.ui.component.MAX_MARKER_CLUSTER_VIEW_SIZE
import ru.spbu.depnav.ui.component.MarkersCluster

private const val ROOMS_CLUSTERER_ID = "rooms"
private const val ENTRANCE_CLUSTERER_ID = "entrances"
private const val STAIRS_CLUSTERER_ID = "stairs"
private const val ELEVATOR_CLUSTERER_ID = "elevators"
private const val WC_CLUSTERER_ID = "wcs"
private const val OTHER_CLUSTERER_ID = "others"

/** Adds clusterers for each marker type group. */
@OptIn(ExperimentalClusteringApi::class)
fun MapState.addClusterers(markerAlpha: StateFlow<Float>) {
    addClusterer(
        ROOMS_CLUSTERER_ID,
        clusteringThreshold = MAX_MARKER_CLUSTER_VIEW_SIZE,
        clusterFactory = createClusterFactory(markerAlpha, Marker.MarkerType.ROOM)
    )
    addClusterer(
        ENTRANCE_CLUSTERER_ID,
        clusteringThreshold = MAX_MARKER_CLUSTER_VIEW_SIZE,
        clusterFactory = createClusterFactory(markerAlpha, Marker.MarkerType.ENTRANCE)
    )
    addClusterer(
        STAIRS_CLUSTERER_ID,
        clusteringThreshold = MAX_MARKER_CLUSTER_VIEW_SIZE,
        clusterFactory = createClusterFactory(markerAlpha, Marker.MarkerType.STAIRS_BOTH)
    )
    addClusterer(
        ELEVATOR_CLUSTERER_ID,
        clusteringThreshold = MAX_MARKER_CLUSTER_VIEW_SIZE,
        clusterFactory = createClusterFactory(markerAlpha, Marker.MarkerType.ELEVATOR)
    )
    addClusterer(
        WC_CLUSTERER_ID,
        clusteringThreshold = MAX_MARKER_CLUSTER_VIEW_SIZE,
        clusterFactory = createClusterFactory(markerAlpha, Marker.MarkerType.WC)
    )
    addClusterer(
        OTHER_CLUSTERER_ID,
        clusteringThreshold = MAX_MARKER_CLUSTER_VIEW_SIZE,
        clusterFactory = createClusterFactory(markerAlpha, Marker.MarkerType.OTHER)
    )
}

private fun createClusterFactory(alphaFlow: StateFlow<Float>, type: Marker.MarkerType) =
    { ids: List<String> ->
        @Composable {
            val alpha by alphaFlow.collectAsStateWithLifecycle()
            if (alpha > 0f) { // To not consume clicks when invisible
                MarkersCluster(ids, type, modifier = Modifier.alpha(alpha))
            }
        }
    }

/** Returns ID of the clustersr responsible for the specified marker type. */
fun getClustererId(markerType: Marker.MarkerType) = when (markerType) {
    Marker.MarkerType.ROOM -> ROOMS_CLUSTERER_ID
    Marker.MarkerType.ENTRANCE -> ENTRANCE_CLUSTERER_ID
    Marker.MarkerType.STAIRS_UP, Marker.MarkerType.STAIRS_DOWN, Marker.MarkerType.STAIRS_BOTH ->
        STAIRS_CLUSTERER_ID
    Marker.MarkerType.ELEVATOR -> ELEVATOR_CLUSTERER_ID
    Marker.MarkerType.WC_MAN, Marker.MarkerType.WC_WOMAN, Marker.MarkerType.WC -> WC_CLUSTERER_ID
    Marker.MarkerType.OTHER -> OTHER_CLUSTERER_ID
}
