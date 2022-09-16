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

package ru.spbu.depnav.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val LightColorPalette = lightColors(
    primary = BlueGrey200,
    primaryVariant = BlueGrey200Dark,
    secondary = BlueGrey200,
    secondaryVariant = BlueGrey200Dark
)

private val DarkColorPalette = darkColors(
    primary = BlueGrey200Light,
    primaryVariant = BlueGrey200,
    secondary = BlueGrey200Light,
    secondaryVariant = BlueGrey200
)

/** Padding applied where elements need to be spaced out by default. */
val DEFAULT_PADDING = 10.dp

/** Elevation applied to elements that need one by default. */
val DEFAULT_ELEVATION = 5.dp

/** Alpha value for faded elements. */
const val FADED_ALPHA = 0.6f

/** Theme of the application. */
@Composable
fun DepNavTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette
    val systemUiController = rememberSystemUiController()

    SideEffect {
        systemUiController.setSystemBarsColor(
            color = Color.Transparent,
            darkIcons = colors.background.luminance() > 0.5f
        )
    }

    MaterialTheme(
        colors = colors,
        shapes = Shapes,
        content = content
    )
}
