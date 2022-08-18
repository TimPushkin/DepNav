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

package ru.spbu.depnav.utils.tiles

import kotlinx.coroutines.Deferred
import ovh.plrapps.mapcompose.core.TileStreamProvider
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText

/** Displayable floor of a map. */
data class Floor(
    /** Layers of tiles that this floor consist of. */
    val layers: Iterable<TileStreamProvider>,
    /** Markers placed on this floor. */
    val markers: Deferred<Map<Marker, MarkerText>>
)
