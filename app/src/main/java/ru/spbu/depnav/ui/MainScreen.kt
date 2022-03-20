package ru.spbu.depnav.ui

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
import ru.spbu.depnav.viewmodel.MapViewModel

@Composable
fun MainScreen(
    mapViewModel: MapViewModel,
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
                state = mapViewModel.state
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
                    modifier = Modifier.align(Alignment.End),
                    onClick = onFloorSwitch,
                    maxFloor = floorsNum
                )
            }
        }
    }
}
