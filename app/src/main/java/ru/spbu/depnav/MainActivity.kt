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

package ru.spbu.depnav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import ru.spbu.depnav.ui.NavDestination
import ru.spbu.depnav.ui.map.MapScreen
import ru.spbu.depnav.ui.map.MapScreenViewModel
import ru.spbu.depnav.ui.search.SearchScreen
import ru.spbu.depnav.ui.theme.DepNavTheme
import ru.spbu.depnav.utils.preferences.PreferencesManager
import javax.inject.Inject

@AndroidEntryPoint
@Suppress("UndocumentedPublicClass") // Class name is self-explanatory
class MainActivity : ComponentActivity() {
    private val mapScreenVm: MapScreenViewModel by viewModels()

    /** User preferences. */
    @Inject
    lateinit var prefs: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DepNavTheme(
                darkTheme = when (prefs.themeMode) {
                    PreferencesManager.ThemeMode.LIGHT -> false
                    PreferencesManager.ThemeMode.DARK -> true
                    PreferencesManager.ThemeMode.SYSTEM -> isSystemInDarkTheme()
                }
            ) {
                val navController = rememberNavController()

                NavHost(navController = navController, startDestination = NavDestination.MAP.name) {
                    composable(NavDestination.MAP.name) {
                        MapScreen(vm = mapScreenVm) {
                            navController.navigate(NavDestination.SEARCH.name)
                        }
                    }
                    composable(NavDestination.SEARCH.name) {
                        fun navigateToMap() {
                            navController.popBackStack(
                                route = NavDestination.MAP.name,
                                inclusive = false
                            )
                        }

                        SearchScreen(
                            onResultClick = {
                                with(mapScreenVm) { viewModelScope.launch { focusOnMarker(it) } }
                                navigateToMap()
                            },
                            onNavigateBack = ::navigateToMap
                        )
                    }
                }
            }
        }
    }
}
