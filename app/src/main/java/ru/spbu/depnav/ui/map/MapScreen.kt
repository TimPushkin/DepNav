package ru.spbu.depnav.ui.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import ovh.plrapps.mapcompose.ui.MapUI
import ru.spbu.depnav.R
import ru.spbu.depnav.ui.search.SearchButton

/**
 * Screen containing a navigable map.
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MapScreen(
    mapScreenState: MapScreenState,
    floorsNum: Int,
    onStartSearch: () -> Unit,
    onFloorSwitch: (Int) -> Unit,
) {
    val scaffoldState = rememberBottomSheetScaffoldState()

    BottomSheetScaffold(
        sheetContent = {
            mapScreenState.highlightedMarker?.let { (marker, markerText) ->
                MarkerInfoLines(
                    title = markerText.title ?: stringResource(R.string.no_title),
                    isClosed = marker.isClosed,
                    description = markerText.description ?: stringResource(R.string.no_description)
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
        sheetShape = MaterialTheme.shapes.large.copy(
            bottomStart = CornerSize(0),
            bottomEnd = CornerSize(0)
        ),
        sheetPeekHeight = 0.dp
    ) { contentPadding ->
        Box(modifier = Modifier.padding(contentPadding)) {
            MapUI(
                modifier = Modifier.fillMaxSize(),
                state = mapScreenState.state
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedVisibility(
                    visible = mapScreenState.showUI,
                    enter = slideInVertically(initialOffsetY = { -it }),
                    exit = slideOutVertically(targetOffsetY = { -it })
                ) {
                    SearchButton(
                        text = stringResource(R.string.search_markers),
                        onClick = onStartSearch,
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .padding(top = 10.dp, bottom = 10.dp)
                    )
                }

                val horizontalOffset: (Int) -> Int =
                    if (LocalLayoutDirection.current == LayoutDirection.Ltr) {
                        { it }
                    } else {
                        { -it }
                    }

                AnimatedVisibility(
                    visible = mapScreenState.showUI,
                    modifier = Modifier.align(Alignment.End),
                    enter = slideInHorizontally(initialOffsetX = horizontalOffset),
                    exit = slideOutHorizontally(targetOffsetX = horizontalOffset)
                ) {
                    FloorSwitch(
                        floor = mapScreenState.currentFloor,
                        minFloor = 1,
                        maxFloor = floorsNum,
                        modifier = Modifier.padding(10.dp),
                        onClick = onFloorSwitch
                    )
                }
            }
        }
    }

    LaunchedEffect(mapScreenState.showUI, mapScreenState.highlightMarker) {
        if (mapScreenState.showUI && mapScreenState.highlightMarker) {
            scaffoldState.bottomSheetState.expand()
        } else if (mapScreenState.highlightedMarker != null) {
            scaffoldState.bottomSheetState.collapse()
        }
    }
}
