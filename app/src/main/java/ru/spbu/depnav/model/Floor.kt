package ru.spbu.depnav.model

import ovh.plrapps.mapcompose.core.TileStreamProvider

data class Floor(val layers: List<TileStreamProvider>, val markers: List<Marker>)
