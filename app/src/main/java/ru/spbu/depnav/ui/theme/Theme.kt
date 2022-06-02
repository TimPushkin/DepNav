package ru.spbu.depnav.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

private val LightColorPalette = lightColors(
    primary = BlueGrey200,
    primaryVariant = BlueGrey200Dark,
    secondary = BlueGrey200
)

private val DarkColorPalette = darkColors(
    primary = BlueGrey200Light,
    primaryVariant = BlueGrey200,
    secondary = BlueGrey200Light
)

/**
 * Theme of the application.
 */
@Composable
fun DepNavTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColorPalette else LightColorPalette

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
