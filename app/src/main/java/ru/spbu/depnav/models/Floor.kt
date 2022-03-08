package ru.spbu.depnav.models

import ovh.plrapps.mapcompose.core.TileStreamProvider

data class Floor(val layers: List<TileStreamProvider>, val markers: List<Marker>)
