/**
 * DepNav -- department navigator.
 * Copyright (C) 2026  Timofei Pushkin
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

import kotlinx.serialization.Serializable

// Language tags in the database are expected to be in uppercase
private const val LANG_RU = "RU"
private const val LANG_EN = "EN"

@Serializable
internal data class MapInfo(
    val id: Int,
    val internalName: String,
    val title: Title,
    val floorWidth: Int,
    val floorHeight: Int,
    val tileSize: Int,
    val zoomLevelsNum: Int,
    val floors: List<Floor>
) {
    @Serializable
    data class Title(
        val ru: String,
        val en: String
    ) {
        fun toMap() = mapOf(
            LANG_RU to ru,
            LANG_EN to en
        )
    }

    @Serializable
    data class Floor(
        val floor: Int,
        val markers: List<Marker>
    ) {
        @Serializable
        data class Marker(
            val type: String,
            val x: Int,
            val y: Int,
            val ru: TextInfo,
            val en: TextInfo
        ) {
            @Serializable
            data class TextInfo(
                val title: String?,
                val location: String?,
                val description: String?
            )

            fun textInfos() = mapOf(
                LANG_RU to ru,
                LANG_EN to en
            )
        }
    }
}
