package ru.spbu.depnav.model

import ovh.plrapps.mapcompose.core.TileStreamProvider

class Floor(val layers: Iterable<TileStreamProvider>, getMarkers: () -> Iterable<Marker>) {
    val markers: Iterable<Marker> by lazy { getMarkers() }
}
