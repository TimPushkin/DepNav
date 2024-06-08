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

import androidx.compose.ui.unit.IntSize
import ovh.plrapps.mapcompose.api.VisibleArea
import ovh.plrapps.mapcompose.api.centroidX
import ovh.plrapps.mapcompose.api.centroidY
import ovh.plrapps.mapcompose.api.fullSize
import ovh.plrapps.mapcompose.api.rotation
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.ui.state.MapState
import ovh.plrapps.mapcompose.utils.Point
import ovh.plrapps.mapcompose.utils.rotateCenteredX
import ovh.plrapps.mapcompose.utils.rotateCenteredY
import ovh.plrapps.mapcompose.utils.toRad

/**
 * Like [ovh.plrapps.mapcompose.api.visibleArea] but scaled to on-map pixel coordinates which makes
 * it a rectangle.
 *
 * When the map is not a square its normalized coordinates become non-uniform when compared to the
 * screen's pixel coordinates. This causes the visible area, calculated as in
 * [ovh.plrapps.mapcompose.api.visibleArea], to become a non-rectangular parallelogram when the map
 * is rotated. Defining it in map's pixel coordinates fixes the problem.
 *
 * @param layoutSize size of map's composable in pixels
 * @param leftPadding padding in pixels to add to map's layout size on its left
 * @param topPadding padding in pixels to add to map's layout size on its top
 * @param rightPadding padding in pixels to add to map's layout size on its right
 * @param bottomPadding padding in pixels to add to map's layout size on its bottom
 */
fun MapState.rectangularVisibleArea(
    layoutSize: IntSize,
    leftPadding: Int = 0,
    topPadding: Int = 0,
    rightPadding: Int = 0,
    bottomPadding: Int = 0
): VisibleArea {
    val leftX = centroidX - (layoutSize.width / 2 + leftPadding) / (fullSize.width * scale)
    val topY = centroidY - (layoutSize.height / 2 + topPadding) / (fullSize.height * scale)
    val rightX = centroidX + (layoutSize.width / 2 + rightPadding) / (fullSize.width * scale)
    val bottomY = centroidY + (layoutSize.height / 2 + bottomPadding) / (fullSize.height * scale)

    val xAxisScale = fullSize.height / fullSize.width.toDouble()
    val scaledCenterX = centroidX / xAxisScale
    val scaledLeftX = leftX / xAxisScale
    val scaledRightX = rightX / xAxisScale

    val p1x = rotateCenteredX(
        scaledLeftX, topY, scaledCenterX, centroidY, -rotation.toRad()
    ) * /* xAxisScale * fullSize.width = */ fullSize.height
    val p1y = rotateCenteredY(
        scaledLeftX, topY, scaledCenterX, centroidY, -rotation.toRad()
    ) * fullSize.height

    val p2x = rotateCenteredX(
        scaledRightX, topY, scaledCenterX, centroidY, -rotation.toRad()
    ) * fullSize.height
    val p2y = rotateCenteredY(
        scaledRightX, topY, scaledCenterX, centroidY, -rotation.toRad()
    ) * fullSize.height

    val p3x = rotateCenteredX(
        scaledRightX, bottomY, scaledCenterX, centroidY, -rotation.toRad()
    ) * fullSize.height
    val p3y = rotateCenteredY(
        scaledRightX, bottomY, scaledCenterX, centroidY, -rotation.toRad()
    ) * fullSize.height

    val p4x = rotateCenteredX(
        scaledLeftX, bottomY, scaledCenterX, centroidY, -rotation.toRad()
    ) * fullSize.height
    val p4y = rotateCenteredY(
        scaledLeftX, bottomY, scaledCenterX, centroidY, -rotation.toRad()
    ) * fullSize.height

    return VisibleArea(p1x, p1y, p2x, p2y, p3x, p3y, p4x, p4y)
}

/**
 * Centroid of the area.
 */
fun VisibleArea.centroid() = Point((p1x + p3x) / 2, (p1y + p3y) / 2)

/**
 * Top border of the area.
 */
fun VisibleArea.top() = LineSegment(Point(p1x, p1y), Point(p2x, p2y))

/**
 * Bottom border of the area.
 */
fun VisibleArea.bottom() = LineSegment(Point(p4x, p4y), Point(p3x, p3y))

/**
 * Left border of the area.
 */
fun VisibleArea.left() = LineSegment(Point(p1x, p1y), Point(p4x, p4y))

/**
 * Right border of the area.
 */
fun VisibleArea.right() = LineSegment(Point(p2x, p2y), Point(p3x, p3y))

/**
 * Returns true if the provided point lies inside this area, or false otherwise.
 *
 * In theory, it should always return the same result as [ovh.plrapps.mapcompose.utils.contains] but
 * in practice this version seems to be more accurate.
 */
fun VisibleArea.contains(p: Point) =
    top().containsProjectionOf(p) && left().containsProjectionOf(p)

/**
 * Returns the rotation of this area in degrees in the range from -180 to 180. Positive values
 * correspond to clockwise rotation while negative values correspond to counterclockwise rotation.
 */
fun VisibleArea.rotation() = top().slope()
