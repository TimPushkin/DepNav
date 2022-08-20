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

@AndroidEntryPoint
@Suppress("UndocumentedPublicClass") // Class name is self-explanatory
class MainActivity : ComponentActivity() {
    private val mapScreenVm: MapScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DepNavTheme(
                darkTheme = when (mapScreenVm.prefs.themeMode) {
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
                        SearchScreen {
                            mapScreenVm.run { viewModelScope.launch { focusOnMarker(it) } }
                            navController.popBackStack(
                                route = NavDestination.MAP.name,
                                inclusive = false
                            )
                        }
                    }
                }
            }
        }
    }
}
