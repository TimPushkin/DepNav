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

package ru.spbu.depnav.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import ru.spbu.depnav.R
import ru.spbu.depnav.data.composite.MarkerWithText
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING

/** Maximum size a marker cluster composable can have in each dimension. */
val MAX_MARKER_CLUSTER_VIEW_SIZE = 50.dp

/** Multiple markers clustered together. */
@Composable
fun MarkersCluster(
    markerIds: List<String>,
    type: Marker.MarkerType,
    modifier: Modifier = Modifier
) = when (type) {
    Marker.MarkerType.ROOM -> RoomMarkersCluster(markerIds, modifier)
    Marker.MarkerType.ENTRANCE ->
        NonRoomMarkersCluster(painterResource(R.drawable.mrk_entrance), modifier)
    Marker.MarkerType.STAIRS_UP, Marker.MarkerType.STAIRS_DOWN, Marker.MarkerType.STAIRS_BOTH ->
        NonRoomMarkersCluster(painterResource(R.drawable.mrk_stairs), modifier)
    Marker.MarkerType.ELEVATOR ->
        NonRoomMarkersCluster(painterResource(R.drawable.mrk_elevator), modifier)
    Marker.MarkerType.WC_MAN, Marker.MarkerType.WC_WOMAN, Marker.MarkerType.WC ->
        NonRoomMarkersCluster(painterResource(R.drawable.mrk_wc), modifier)
    Marker.MarkerType.OTHER ->
        NonRoomMarkersCluster(painterResource(R.drawable.mrk_other), modifier)
}

@Composable
private fun RoomMarkersCluster(markerIds: List<String>, modifier: Modifier = Modifier) {
    val title = rememberSaveable {
        val commonPrefix = markerIds.fold(markerTitleFromExtendedId(markerIds.first())) { acc, s ->
            val title = markerTitleFromExtendedId(s)
            acc.commonPrefixWith(title)
        }
        "$commonPrefix…"
    }

    Surface(
        modifier = Modifier
            .sizeIn(
                maxWidth = MAX_MARKER_CLUSTER_VIEW_SIZE,
                maxHeight = MAX_MARKER_CLUSTER_VIEW_SIZE
            )
            .then(modifier),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.onBackground
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                title,
                modifier = Modifier.padding(DEFAULT_PADDING / 4),
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun markerTitleFromExtendedId(extendedId: String) =
    extendedId.substringAfter(MarkerWithText.ID_DIVIDER, "")

@Composable
private fun NonRoomMarkersCluster(painter: Painter, modifier: Modifier = Modifier) {
    Surface(
        modifier = Modifier
            .sizeIn(
                maxWidth = MARKER_ICON_SIZE.coerceAtMost(MAX_MARKER_CLUSTER_VIEW_SIZE),
                maxHeight = MARKER_ICON_SIZE.coerceAtMost(MAX_MARKER_CLUSTER_VIEW_SIZE)
            )
            .then(modifier),
        shape = MaterialTheme.shapes.extraSmall,
        color = MaterialTheme.colorScheme.onBackground
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painter,
                contentDescription = stringResource(R.string.label_non_room_cluster),
                modifier = Modifier
                    .size(MARKER_ICON_SIZE) // Will be shrunk by the surface because of the padding
                    .padding(DEFAULT_PADDING / 4)
            )
        }
    }
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun RoomMarkersClusterPreview() {
    MarkersCluster(
        markerIds = listOf(
            "1${MarkerWithText.ID_DIVIDER}1234",
            "2${MarkerWithText.ID_DIVIDER}1235",
            "3${MarkerWithText.ID_DIVIDER}1236"
        ),
        type = Marker.MarkerType.ROOM
    )
}

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun NonRoomMarkersClusterPreview() {
    MarkersCluster(markerIds = listOf("1", "2"), type = Marker.MarkerType.WC)
}
