package ru.spbu.depnav.ui.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ovh.plrapps.mapcompose.ui.MapUI
import ru.spbu.depnav.ui.search.SearchButton

@Composable
fun MapScreen(
    mapScreenState: MapScreenState,
    floorsNum: Int,
    onStartSearch: () -> Unit,
    onFloorSwitch: (Int) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colors.background
    ) {
        Box {
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
