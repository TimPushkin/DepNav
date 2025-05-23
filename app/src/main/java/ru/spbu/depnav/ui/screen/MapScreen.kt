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

package ru.spbu.depnav.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.ui.MapUI
import ru.spbu.depnav.R
import ru.spbu.depnav.data.composite.MarkerWithText
import ru.spbu.depnav.data.preferences.PreferencesManager
import ru.spbu.depnav.ui.component.FloorSwitch
import ru.spbu.depnav.ui.component.MainMenuSheet
import ru.spbu.depnav.ui.component.MapSearchBar
import ru.spbu.depnav.ui.component.MarkerInfoLines
import ru.spbu.depnav.ui.component.MarkerView
import ru.spbu.depnav.ui.component.PinPointer
import ru.spbu.depnav.ui.component.ZoomInHint
import ru.spbu.depnav.ui.dialog.MapLegendDialog
import ru.spbu.depnav.ui.dialog.SettingsDialog
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING
import ru.spbu.depnav.ui.theme.ON_MAP_SURFACE_ALPHA
import ru.spbu.depnav.ui.viewmodel.MapUiState
import ru.spbu.depnav.ui.viewmodel.MapViewModel
import ru.spbu.depnav.ui.viewmodel.SearchResults
import ru.spbu.depnav.ui.viewmodel.SearchUiState
import ru.spbu.depnav.ui.viewmodel.SearchViewModel

/** Screen containing a navigable map. */
@Composable
fun MapScreen(
    prefs: PreferencesManager,
    mapVm: MapViewModel = viewModel(),
    searchVm: SearchViewModel = viewModel()
) {
    var openSettings by rememberSaveable { mutableStateOf(false) }
    if (openSettings) {
        SettingsDialog(prefs, onDismiss = { openSettings = false })
    }

    var openMapLegend by rememberSaveable { mutableStateOf(false) }
    if (openMapLegend) {
        MapLegendDialog(onDismiss = { openMapLegend = false })
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        val selectedMapId by prefs.selectedMapIdFlow.collectAsStateWithLifecycle()
        val mapUiState by mapVm.uiState.collectAsStateWithLifecycle()

        val scope = rememberCoroutineScope()
        val drawerState = rememberDrawerState(
            initialValue = if (selectedMapId != null) DrawerValue.Closed else DrawerValue.Open,
            confirmStateChange = { selectedMapId != null }
        )

        ModalNavigationDrawer(
            drawerContent = {
                MainMenuSheet(
                    selectedMapId = selectedMapId,
                    availableMaps = mapUiState.availableMaps,
                    onMapSelected = {
                        prefs.updateSelectedMapId(it)
                        scope.launch { drawerState.close() }
                    },
                    onSettingsClick = { openSettings = true },
                    onMapLegendClick = { openMapLegend = true }
                )
            },
            drawerState = drawerState,
            gesturesEnabled = selectedMapId != null
        ) {
            val readyMapUiState = mapUiState as? MapUiState.Ready ?: return@ModalNavigationDrawer
            val searchUiState by searchVm.uiState.collectAsStateWithLifecycle()

            val markerAlpha by mapVm.markerAlpha.collectAsStateWithLifecycle()
            val markersVisible by remember { derivedStateOf { markerAlpha > 0f } }

            val mapColor = MaterialTheme.colorScheme.outline
            LaunchedEffect(mapColor) { mapVm.setMapColor(mapColor) }

            MapUI(state = readyMapUiState.mapState)

            OnMapUi(
                mapUiState = readyMapUiState,
                searchUiState = searchUiState,
                onSearchQueryChange = searchVm::search,
                onSearchResultClick = { markerId ->
                    mapVm.focusOnMarker(markerId)
                    searchVm.addToSearchHistory(markerId)
                },
                onMainMenuClick = { scope.launch { drawerState.open() } },
                onFloorSwitch = mapVm::setFloor,
                markersVisible = markersVisible
            )
        }
    }

    LaunchedEffect(ConfigurationCompat.getLocales(LocalConfiguration.current)[0]) {
        val locale = Locale.current // This is not a State
        searchVm.onLocaleChange(locale)
        mapVm.onLocaleChange(locale)
    }
}

@Composable
@Suppress("LongParameterList") // Considered OK for a composable
private fun OnMapUi(
    mapUiState: MapUiState.Ready,
    searchUiState: SearchUiState,
    onSearchQueryChange: (String) -> Unit,
    onSearchResultClick: (Int) -> Unit,
    onMainMenuClick: () -> Unit,
    onFloorSwitch: (Int) -> Unit,
    markersVisible: Boolean
) {
    CompositionLocalProvider(LocalAbsoluteTonalElevation provides 4.dp) {
        Box(modifier = Modifier.fillMaxSize()) {
            PinPointer(mapUiState.mapState, mapUiState.pinnedMarker?.marker)

            AnimatedSearchBar(
                visible = mapUiState.showOnMapUi,
                mapTitle = mapUiState.mapTitle,
                query = searchUiState.query,
                onQueryChange = onSearchQueryChange,
                searchResults = searchUiState.results,
                onResultClick = onSearchResultClick,
                onMenuClick = onMainMenuClick
            )

            AnimatedFloorSwitch(
                visible = mapUiState.showOnMapUi,
                currentFloor = mapUiState.currentFloor,
                maxFloor = mapUiState.floorsNum,
                onFloorSwitch = onFloorSwitch
            )

            AnimatedBottom(
                pinnedMarker = mapUiState.pinnedMarker,
                showZoomInHint = !markersVisible
            )
        }
    }
}

@Composable
@Suppress("LongParameterList") // Considered OK for a composable
private fun BoxScope.AnimatedSearchBar(
    visible: Boolean,
    mapTitle: String,
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: SearchResults,
    onResultClick: (Int) -> Unit,
    onMenuClick: () -> Unit
) {
    var searchBarExpanded by rememberSaveable { mutableStateOf(false) }
    if (!visible) {
        searchBarExpanded = false
    }

    if (!searchBarExpanded && query.isNotEmpty()) {
        onQueryChange("")
    }

    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .align(Alignment.TopCenter)
            .zIndex(1f),
        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
    ) {
        val horizontalPadding by animateDpAsState(
            if (searchBarExpanded) 0.dp else DEFAULT_PADDING,
            label = "Map search bar horizontal padding"
        )

        MapSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            mapTitle = mapTitle,
            expanded = searchBarExpanded,
            onExpandedChange = { searchBarExpanded = it },
            results = searchResults,
            onResultClick = onResultClick,
            onMenuClick = onMenuClick,
            modifier = Modifier.padding(horizontal = horizontalPadding)
        )
    }
}

@Composable
private fun BoxScope.AnimatedFloorSwitch(
    visible: Boolean,
    currentFloor: Int,
    maxFloor: Int,
    onFloorSwitch: (Int) -> Unit
) {
    val horizontalOffset: (Int) -> Int =
        if (LocalLayoutDirection.current == LayoutDirection.Ltr) {
            { it }
        } else {
            { -it }
        }

    AnimatedVisibility(
        visible = visible,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .safeDrawingPadding()
            .padding(top = 64.dp) // Estimated search bar height
            .padding(DEFAULT_PADDING),
        enter = slideInHorizontally(initialOffsetX = horizontalOffset) + fadeIn(),
        exit = slideOutHorizontally(targetOffsetX = horizontalOffset) + fadeOut()
    ) {
        FloorSwitch(
            floor = currentFloor,
            maxFloor = maxFloor,
            onClick = onFloorSwitch
        )
    }
}

@Composable
private fun BoxScope.AnimatedBottom(pinnedMarker: MarkerWithText?, showZoomInHint: Boolean) {
    // Not using insets here to let the Surface reside under bottom bar
    AnimatedVisibility(
        visible = pinnedMarker != null,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Surface(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                .width(640.dp),
            shape = MaterialTheme.shapes.large.copy(
                bottomStart = CornerSize(0),
                bottomEnd = CornerSize(0)
            ),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = ON_MAP_SURFACE_ALPHA)
        ) {
            // Have to remember the latest pinned marker to continue showing it while the exit
            // animation is still in progress
            var latestPinnedMarker by remember { mutableStateOf(pinnedMarker) }
            if (pinnedMarker != null) {
                latestPinnedMarker = pinnedMarker
            }

            AnimatedContent(
                targetState = latestPinnedMarker,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "Marker info lines change"
            ) { markerWithText ->
                val (marker, markerText) = markerWithText ?: return@AnimatedContent

                MarkerInfoLines(
                    title = markerText.title ?: stringResource(R.string.no_title),
                    location = markerText.location,
                    description = markerText.description,
                    modifier = Modifier
                        .padding(DEFAULT_PADDING)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom)
                        )
                ) {
                    MarkerView(
                        title = markerText.title ?: stringResource(R.string.no_title),
                        type = marker.type,
                        simplified = true
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = showZoomInHint && pinnedMarker == null,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .windowInsetsPadding(
                WindowInsets.safeDrawing.run { exclude(only(WindowInsetsSides.Top)) }
            ),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ZoomInHint()
    }
}
