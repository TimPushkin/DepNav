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

package ru.spbu.depnav.ui

import android.app.ActivityManager
import android.content.res.Resources
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import ru.spbu.depnav.data.preferences.PreferencesManager
import ru.spbu.depnav.data.preferences.ThemeMode
import ru.spbu.depnav.ui.screen.MapScreen
import ru.spbu.depnav.ui.theme.DepNavTheme
import javax.inject.Inject
import android.graphics.Color as PlatformColor
import androidx.compose.ui.graphics.Color as ComposeColor

@AndroidEntryPoint
@Suppress("UndocumentedPublicClass") // Class name is self-explanatory
class MainActivity : ComponentActivity() {
    /** User preferences. */
    @Inject
    lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            // Set primary color for recents list of older Androids; it only depends on system's
            // dark mode setting, not on the in-app one
            DepNavTheme {
                val primary = MaterialTheme.colorScheme.primary
                SideEffect { setPrimaryColor(primary) }
            }

            val themeMode by prefs.themeModeFlow.collectAsStateWithLifecycle()
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            SideEffect { enableEdgeToEdge { darkTheme } }
            DepNavTheme(darkTheme) { MapScreen(prefs) }
        }
    }

    private fun enableEdgeToEdge(detectDarkMode: (Resources) -> Boolean) {
        // Scrims are the defaults from enableEdgeToEdge sources
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = PlatformColor.TRANSPARENT,
                darkScrim = PlatformColor.TRANSPARENT,
                detectDarkMode
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = PlatformColor.argb(0xe6, 0xFF, 0xFF, 0xFF),
                darkScrim = PlatformColor.argb(0x80, 0x1b, 0x1b, 0x1b),
                detectDarkMode
            )
        )
    }

    private fun setPrimaryColor(color: ComposeColor) {
        val colorArgb = color.toArgb()
        val taskDescription = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityManager.TaskDescription.Builder().setPrimaryColor(colorArgb).build()
        } else {
            ActivityManager.TaskDescription(null, null, colorArgb)
        }
        setTaskDescription(taskDescription)
    }
}
