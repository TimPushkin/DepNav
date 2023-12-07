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

package ru.spbu.depnav.data.composite

import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText

/** [Marker] with its corresponding [MarkerText]. */
data class MarkerWithText(val marker: Marker, val text: MarkerText) {
    init {
        require(marker.id == text.markerId) {
            "Marker ID ${marker.id} != marker text's marker ID ${text.markerId}"
        }
    }

    /** ID of this marker as a string. It is parsed by the marker clustering code. */
    val extendedId by lazy {
        if (marker.type == Marker.MarkerType.ROOM) {
            "${marker.id}$ID_DIVIDER${text.title}"
        } else {
            "${marker.id}"
        }
    }

    companion object {
        const val ID_DIVIDER = ':'
    }
}
