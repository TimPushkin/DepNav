package ru.spbu.depnav.model

import kotlinx.coroutines.Deferred
import ovh.plrapps.mapcompose.core.TileStreamProvider

/**
 * Displayable floor of a map.
 */
data class Floor(
    /** Layers of tiles that this floor consist of. */
    val layers: Iterable<TileStreamProvider>,
    /** Markers placed on this floor. */
    val markers: Deferred<Map<Marker, MarkerText>>
)
