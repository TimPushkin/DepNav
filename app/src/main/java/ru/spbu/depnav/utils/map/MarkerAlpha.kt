package ru.spbu.depnav.utils.map

import ovh.plrapps.mapcompose.api.maxScale
import ovh.plrapps.mapcompose.api.minScale
import ovh.plrapps.mapcompose.api.scale
import ovh.plrapps.mapcompose.ui.state.MapState

private const val MARKERS_INVISIBLE_UNTIL_SCALE = 0.2f
private const val MARKERS_FULLY_VISIBLE_FROM_SCALE = 0.5f

/** Returns alpha value to use for map markers calculated based on the current map scale. */
fun MapState.getMarkerAlpha(): Float {
    val minScale = minScale.coerceAtLeast(MARKERS_INVISIBLE_UNTIL_SCALE)
    val maxScale = maxScale.coerceAtMost(MARKERS_FULLY_VISIBLE_FROM_SCALE)
    val scale = scale.coerceIn(minScale, maxScale)
    return (scale - minScale) / (maxScale - minScale)
}
