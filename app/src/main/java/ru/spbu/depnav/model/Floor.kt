package ru.spbu.depnav.model

import kotlinx.coroutines.Deferred
import ovh.plrapps.mapcompose.core.TileStreamProvider

data class Floor(val layers: Iterable<TileStreamProvider>, val markers: Deferred<Iterable<Marker>>)
