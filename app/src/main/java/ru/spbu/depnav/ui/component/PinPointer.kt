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

package ru.spbu.depnav.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import ovh.plrapps.mapcompose.api.VisibleArea
import ovh.plrapps.mapcompose.api.fullSize
import ovh.plrapps.mapcompose.api.getLayoutSizeFlow
import ovh.plrapps.mapcompose.ui.state.MapState
import ovh.plrapps.mapcompose.utils.AngleDegree
import ovh.plrapps.mapcompose.utils.Point
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.utils.map.LineSegment
import ru.spbu.depnav.utils.map.contains
import ru.spbu.depnav.utils.map.left
import ru.spbu.depnav.utils.map.rectangularVisibleArea
import ru.spbu.depnav.utils.map.rotation
import ru.spbu.depnav.utils.map.top

/**
 * When the pin is outside of the map area visible on the screen, shows a pointer towards the pin.
 *
 * It is intended to be placed exactly over the map's composable.
 */
@Composable
fun PinPointer(mapState: MapState, pin: Marker?) {
    var mapLayoutSizeFlow by remember { mutableStateOf<Flow<IntSize>?>(null) }
    LaunchedEffect(mapState) { mapLayoutSizeFlow = mapState.getLayoutSizeFlow() }
    val mapLayoutSize by mapLayoutSizeFlow?.collectAsStateWithLifecycle(IntSize.Zero) ?: return
    if (mapLayoutSize == IntSize.Zero) {
        return
    }

    val pinSize = with(LocalDensity.current) { PIN_SIZE.roundToPx() }

    val pinPoint = pin?.run { Point(x * mapState.fullSize.width, y * mapState.fullSize.height) }
    val visibleArea = mapState.rectangularVisibleArea(mapLayoutSize) // Area visible on the screen

    val currentPointerPose =
        if (
            pinPoint != null &&
            !mapState.rectangularVisibleArea( // Area on which the pin is visible on the screen
                mapLayoutSize,
                leftPadding = pinSize / 2,
                rightPadding = pinSize / 2,
                bottomPadding = pinSize
            ).contains(pinPoint)
        ) {
            calculatePointerPose(visibleArea, pinPoint)
        } else {
            null // There is no pin or it is visible on the screen
        }

    // Have to remember the latest non-null pointer pose to continue showing it while the exit
    // animation is still in progress
    var lastPointerPose by remember { mutableStateOf(PinPointerPose.Empty) }
    if (currentPointerPose != null) {
        lastPointerPose = currentPointerPose
    }

    AnimatedVisibility(
        visible = currentPointerPose != null,
        modifier = Modifier.absoluteOffset { lastPointerPose.coordinates(mapLayoutSize, pinSize) },
        enter = fadeIn() + slideIn { lastPointerPose.slideAnimationOffset(it) } + scaleIn(),
        exit = fadeOut() + slideOut { lastPointerPose.slideAnimationOffset(it) } + scaleOut()
    ) {
        // Cannot use mapState.rotation since it has a different pivot
        Pin(modifier = Modifier.rotate(lastPointerPose.direction - visibleArea.rotation()))
    }
}

private data class PinPointerPose(
    val side: Side,
    val sideFraction: Float,
    val direction: AngleDegree
) {
    enum class Side { LEFT, RIGHT, TOP, BOTTOM }

    companion object {
        val Empty = PinPointerPose(Side.TOP, 0f, 0f)
    }

    fun coordinates(boxSize: IntSize, pinSize: Int): IntOffset {
        return when (side) {
            Side.LEFT -> IntOffset(
                x = 0,
                y = (boxSize.height * sideFraction - pinSize / 2f)
                    .toInt()
                    .coerceIn(0, boxSize.height - pinSize)
            )
            Side.RIGHT -> IntOffset(
                x = (boxSize.width - pinSize).coerceAtLeast(0),
                y = (boxSize.height * sideFraction - pinSize / 2f)
                    .toInt()
                    .coerceIn(0, boxSize.height - pinSize)
            )
            Side.TOP -> IntOffset(
                x = (boxSize.width * sideFraction - pinSize / 2f)
                    .toInt()
                    .coerceIn(0, boxSize.width - pinSize),
                y = 0
            )
            Side.BOTTOM -> IntOffset(
                x = (boxSize.width * sideFraction - pinSize / 2f)
                    .toInt()
                    .coerceIn(0, boxSize.width - pinSize),
                y = (boxSize.height - pinSize).coerceAtLeast(0)
            )
        }
    }

    fun slideAnimationOffset(pinSize: IntSize) = when (side) {
        Side.LEFT -> IntOffset(x = -pinSize.width, y = 0)
        Side.RIGHT -> IntOffset(x = pinSize.width, y = 0)
        Side.TOP -> IntOffset(x = 0, y = -pinSize.height)
        Side.BOTTOM -> IntOffset(x = 0, y = pinSize.height)
    }
}

private const val EPSILON = 1e-5f

private fun calculatePointerPose(visibleArea: VisibleArea, pin: Point): PinPointerPose {
    val topBorder = visibleArea.top()
    val leftBorder = visibleArea.left()

    val horizontalFraction = topBorder.fractionOfClosestPointTo(pin)
    val verticalFraction = leftBorder.fractionOfClosestPointTo(pin)

    return when {
        // Corners
        horizontalFraction < EPSILON && verticalFraction < EPSILON -> {
            val direction = LineSegment(topBorder.p1, pin).slope() - 90
            PinPointerPose(PinPointerPose.Side.TOP, 0f, direction)
        }
        horizontalFraction > 1f - EPSILON && verticalFraction < EPSILON -> {
            val direction = LineSegment(topBorder.p2, pin).slope() - 90
            PinPointerPose(PinPointerPose.Side.TOP, 1f, direction)
        }
        horizontalFraction < EPSILON && verticalFraction > 1f - EPSILON -> {
            val direction = LineSegment(leftBorder.p2, pin).slope() - 90
            PinPointerPose(PinPointerPose.Side.BOTTOM, 0f, direction)
        }
        horizontalFraction > 1f - EPSILON && verticalFraction > 1f - EPSILON -> {
            val direction = LineSegment(with(visibleArea) { Point(p3x, p3y) }, pin).slope() - 90
            PinPointerPose(PinPointerPose.Side.BOTTOM, 1f, direction)
        }
        // Sides
        horizontalFraction < EPSILON -> {
            val direction = (topBorder.slope() - 180) - 90
            PinPointerPose(PinPointerPose.Side.LEFT, verticalFraction, direction)
        }
        horizontalFraction > 1f - EPSILON -> {
            val direction = topBorder.slope() - 90
            PinPointerPose(PinPointerPose.Side.RIGHT, verticalFraction, direction)
        }
        verticalFraction < EPSILON -> {
            val direction = (leftBorder.slope() - 180) - 90
            PinPointerPose(PinPointerPose.Side.TOP, horizontalFraction, direction)
        }
        verticalFraction > 1f - EPSILON -> {
            val direction = leftBorder.slope() - 90
            PinPointerPose(PinPointerPose.Side.BOTTOM, horizontalFraction, direction)
        }
        // Pin is inside the area
        else -> throw IllegalArgumentException("Pin lies inside the visible area")
    }
}
