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

import ovh.plrapps.mapcompose.core.TileStreamProvider
import ru.spbu.depnav.data.composite.MarkerWithText

/** Displayable floor of a map. */
data class Floor(
    /** Number of this floor among other floors on the same map. */
    val number: Int,
    /** Layers of tiles that this floor consist of. */
    val layers: Iterable<TileStreamProvider>,
    /** Markers placed on this floor, mapped by their IDs. */
    val markers: Map<String, MarkerWithText>
)
