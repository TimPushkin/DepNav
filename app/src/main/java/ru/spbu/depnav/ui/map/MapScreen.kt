package ru.spbu.depnav.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ovh.plrapps.mapcompose.ui.MapUI
import ru.spbu.depnav.R
import ru.spbu.depnav.ui.search.SearchButton

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
                SearchButton(
                    text = stringResource(R.string.search_markers),
                    onClick = onStartSearch,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(top = 10.dp, bottom = 10.dp)
                )

                FloorSwitch(
                    floor = mapScreenState.currentFloor,
                    minFloor = 1,
                    maxFloor = floorsNum,
                    modifier = Modifier
                        .padding(10.dp)
                        .align(Alignment.End),
                    onClick = onFloorSwitch
                )
            }
        }
    }

    LaunchedEffect(mapScreenState.highlightMarker) {
        if (mapScreenState.highlightMarker) scaffoldState.bottomSheetState.expand()
        else scaffoldState.bottomSheetState.collapse()
    }
}
