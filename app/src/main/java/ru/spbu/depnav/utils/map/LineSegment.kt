/**
 * DepNav -- department navigator.
 * Copyright (C) 2024  Timofei Pushkin
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

import ovh.plrapps.mapcompose.utils.AngleDegree
import ovh.plrapps.mapcompose.utils.Point
import kotlin.math.atan2

/**
 * A segment of a line lying between two points.
 */
data class LineSegment(val p1: Point, val p2: Point) {
    /**
     * Returns the slope of this line in degrees in the range from -180 to 180 with 0 representing
     * a horizontal line. Positive values correspond to clockwise rotation while negative values
     * correspond to counterclockwise rotation.
     */
    fun slope(): AngleDegree = Math.toDegrees(atan2(y = p2.y - p1.y, x = p2.x - p1.x)).toFloat()

    /**
     * Returns true if projection of the provided point on the line of this segment lies within the
     * segment, or false otherwise.
     */
    fun containsProjectionOf(p: Point) = fractionOfProjectionOf(p) in 0f..1f

    /**
     * Returns the fraction from the start of this segment to its point that is the closest to the
     * specified point.
     */
    fun fractionOfClosestPointTo(p: Point) = fractionOfProjectionOf(p).coerceIn(0f, 1f)

    private fun fractionOfProjectionOf(p: Point): Float {
        val vecP1ToP2 = Point(p2.x - p1.x, p2.y - p1.y)
        val vecP1ToP = Point(p.x - p1.x, p.y - p1.y)

        val squaredLength = vecP1ToP2.x * vecP1ToP2.x + vecP1ToP2.y * vecP1ToP2.y
        if (squaredLength == 0.0) {
            return 0f
        }
        val dotProduct = vecP1ToP.x * vecP1ToP2.x + vecP1ToP.y * vecP1ToP2.y

        return (dotProduct / squaredLength).toFloat()
    }
}
