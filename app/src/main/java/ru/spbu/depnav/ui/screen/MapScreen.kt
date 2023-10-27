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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import ovh.plrapps.mapcompose.ui.MapUI
import ru.spbu.depnav.R
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.ui.component.FloorSwitch
import ru.spbu.depnav.ui.component.MapSearchBar
import ru.spbu.depnav.ui.component.MarkerInfoLines
import ru.spbu.depnav.ui.component.MarkerView
import ru.spbu.depnav.ui.component.ZoomInHint
import ru.spbu.depnav.ui.dialog.MapLegendDialog
import ru.spbu.depnav.ui.dialog.SettingsDialog
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING
import ru.spbu.depnav.ui.viewmodel.MapUiState
import ru.spbu.depnav.ui.viewmodel.MapViewModel
import ru.spbu.depnav.ui.viewmodel.SearchUiState

/** Screen containing a navigable map. */
@Composable
fun MapScreen(vm: MapViewModel = viewModel()) {
    val uncheckedMapUiState by vm.mapUiState.collectAsStateWithLifecycle(MapUiState.Loading)
    val mapUiState = uncheckedMapUiState // Direct usage won't allow auto-cast
    if (mapUiState !is MapUiState.Ready) {
        return
    }

    val searchUiState by vm.searchUiState.collectAsStateWithLifecycle(SearchUiState())

    val mapColor = MaterialTheme.colorScheme.outline
    LaunchedEffect(mapColor) { vm.setMapColor(mapColor) }

    var openMapLegend by rememberSaveable { mutableStateOf(false) }
    if (openMapLegend) {
        MapLegendDialog(onDismiss = { openMapLegend = false })
    }

    var openSettings by rememberSaveable { mutableStateOf(false) }
    if (openSettings) {
        SettingsDialog(prefs = vm.prefs, onDismiss = { openSettings = false })
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        MapUI(state = mapUiState.mapState)

        Box(modifier = Modifier.fillMaxSize()) {
            CompositionLocalProvider(LocalAbsoluteTonalElevation provides 4.dp) {
                AnimatedSearchBar(
                    visible = mapUiState.showOnMapUi,
                    query = searchUiState.query,
                    onQueryChange = vm::searchForMarker,
                    searchResults = searchUiState.results,
                    onResultClick = { markerId ->
                        vm.focusOnMarker(markerId)
                        vm.addToMarkerSearchHistory(markerId)
                    },
                    onInfoCLick = { openMapLegend = true },
                    onSettingsClick = { openSettings = true }
                )

                AnimatedFloorSwitch(
                    visible = mapUiState.showOnMapUi,
                    currentFloor = mapUiState.currentFloor,
                    maxFloor = mapUiState.floorsNum,
                    onFloorSwitch = vm::setFloor
                )

                AnimatedBottom(
                    pinnedMarker = mapUiState.pinnedMarker,
                    showZoomInHint = !vm.markersVisible
                )
            }
        }
    }
}

@Composable
@Suppress("LongParameterList") // Considered OK for composables
private fun BoxScope.AnimatedSearchBar(
    visible: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    searchResults: Map<Marker, MarkerText>,
    onResultClick: (Int) -> Unit,
    onInfoCLick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var searchBarActive by rememberSaveable { mutableStateOf(false) }
    if (!visible) {
        searchBarActive = false
    }

    if (!searchBarActive && query.isNotEmpty()) {
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
            if (searchBarActive) 0.dp else DEFAULT_PADDING,
            label = "Map search bar horizontal padding"
        )

        MapSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            active = searchBarActive,
            onActiveChange = { searchBarActive = it },
            results = searchResults,
            onResultClick = onResultClick,
            onInfoClick = onInfoCLick,
            onSettingsClick = onSettingsClick,
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
            .safeContentPadding()
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
private fun BoxScope.AnimatedBottom(
    pinnedMarker: Pair<Marker, MarkerText>?,
    showZoomInHint: Boolean
) {
    val insetsNoTop = WindowInsets.systemBars.run { exclude(only(WindowInsetsSides.Top)) }

    // Not using insets here to let the Surface reside under bottom bar
    AnimatedVisibility(
        visible = pinnedMarker != null,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large.copy(
                bottomStart = CornerSize(0),
                bottomEnd = CornerSize(0)
            )
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
                    description = markerText.description,
                    isClosed = marker.isClosed,
                    modifier = Modifier
                        .padding(DEFAULT_PADDING)
                        .windowInsetsPadding(insetsNoTop)
                ) {
                    MarkerView(
                        title = markerText.title ?: stringResource(R.string.no_title),
                        type = marker.type,
                        isClosed = marker.isClosed,
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
            .windowInsetsPadding(insetsNoTop),
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        ZoomInHint()
    }
}
