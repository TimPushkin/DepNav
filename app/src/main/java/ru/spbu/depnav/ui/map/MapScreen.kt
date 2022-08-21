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

package ru.spbu.depnav.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ovh.plrapps.mapcompose.api.fullSize
import ovh.plrapps.mapcompose.ui.MapUI
import ru.spbu.depnav.R
import ru.spbu.depnav.ui.theme.DEFAULT_ELEVATION
import ru.spbu.depnav.ui.theme.DEFAULT_PADDING

private const val MIN_FLOOR = 1

/** Screen containing a navigable map. */
@Composable
fun MapScreen(vm: MapScreenViewModel = hiltViewModel(), onStartSearch: () -> Unit) {
    val onBackgroundColor = MaterialTheme.colors.onBackground
    LaunchedEffect(onBackgroundColor) { vm.tileColor = onBackgroundColor }

    if (vm.mapState.fullSize == IntSize.Zero) { // Compose crashes when trying to display empty map
        StubScreen()
        return
    }

    val insetsNoTop = WindowInsets.systemBars.run { exclude(only(WindowInsetsSides.Top)) }
    val insetsNoBottom = WindowInsets.systemBars.run { exclude(only(WindowInsetsSides.Bottom)) }

    var openMenu by rememberSaveable { mutableStateOf(false) }
    if (openMenu) {
        SettingsDialog(
            prefs = vm.prefs,
            onDismiss = { openMenu = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        MapUI(state = vm.mapState)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(insetsNoBottom),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedVisibility(
                visible = vm.showUI,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
            ) {
                TopButton(
                    text = stringResource(R.string.search_markers),
                    onMenuClick = { openMenu = true },
                    onSurfaceClick = onStartSearch,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = DEFAULT_PADDING)
                )
            }

            val horizontalOffset: (Int) -> Int =
                if (LocalLayoutDirection.current == LayoutDirection.Ltr) {
                    { it }
                } else {
                    { -it }
                }

            AnimatedVisibility(
                visible = vm.showUI,
                modifier = Modifier.align(Alignment.End),
                enter = slideInHorizontally(initialOffsetX = horizontalOffset) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = horizontalOffset) + fadeOut()
            ) {
                FloorSwitch(
                    floor = vm.currentFloor.coerceAtLeast(MIN_FLOOR),
                    minFloor = MIN_FLOOR,
                    maxFloor = vm.floorsNum,
                    modifier = Modifier.padding(DEFAULT_PADDING),
                    onClick = { vm.viewModelScope.launch { vm.setFloor(it) } }
                )
            }
        }

        AnimatedVisibility(
            visible = vm.showUI && vm.isMarkerPinned,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large.copy(
                    bottomStart = CornerSize(0),
                    bottomEnd = CornerSize(0)
                ),
                elevation = DEFAULT_ELEVATION
            ) {
                vm.pinnedMarker?.let { (marker, markerText) ->
                    MarkerInfoLines(
                        title = markerText.title ?: stringResource(R.string.no_title),
                        description = markerText.description,
                        isClosed = marker.isClosed,
                        modifier = Modifier.windowInsetsPadding(insetsNoTop)
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
    }
}

@Composable
private fun StubScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    )
}
