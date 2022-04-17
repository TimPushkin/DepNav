package ru.spbu.depnav.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ovh.plrapps.mapcompose.ui.MapUI
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
            mapScreenState.displayedMarkerText?.run {
                MarkerInfo(
                    title = title ?: "",
                    description = description
                )
            }
        },
        modifier = Modifier.fillMaxSize(),
        scaffoldState = scaffoldState,
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
                    text = "",
                    onClick = onStartSearch
                )

                FloorSwitch(
                    floor = mapScreenState.currentFloor,
                    modifier = Modifier.align(Alignment.End),
                    onClick = onFloorSwitch,
                    maxFloor = floorsNum
                )
            }
        }
    }
}
