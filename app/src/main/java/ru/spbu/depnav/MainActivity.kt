package ru.spbu.depnav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ovh.plrapps.mapcompose.ui.MapUI
import ru.spbu.depnav.controller.MapFloorSwitcher
import ru.spbu.depnav.model.Floor
import ru.spbu.depnav.provider.MarkerProvider
import ru.spbu.depnav.ui.theme.DepNavTheme
import ru.spbu.depnav.provider.TileProviderFactory
import ru.spbu.depnav.ui.elements.FloorSwitch
import ru.spbu.depnav.ui.elements.SearchField
import ru.spbu.depnav.viewmodel.MapViewModel

private const val TAG = "MainActivity"

private const val MAP_WIDTH = 11264
private const val MAP_HEIGHT = 5120
private const val TILES_PATH = "spbu-mm/tiles"
private const val FLOOR_NUM = 4

class MainActivity : ComponentActivity() {
    private lateinit var mMapViewModel: MapViewModel
    private lateinit var mTileProviderFactory: TileProviderFactory
    private lateinit var mMapFloorSwitcher: MapFloorSwitcher
    private val mMarkerProvider = MarkerProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mTileProviderFactory = TileProviderFactory(applicationContext.assets, TILES_PATH)
        mMapViewModel = MapViewModel(MAP_WIDTH, MAP_HEIGHT)

        val floors = List(FLOOR_NUM) {
            val floor = it + 1
            floor to Floor(
                listOf(mTileProviderFactory.makeTileProviderForFloor(floor)),
                emptyList()
            )
        }.toMap()
        mMapFloorSwitcher = MapFloorSwitcher(mMapViewModel, floors).apply { setFloor(1) }

        setContent {
            DepNavTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box {
                        MapUI(
                            modifier = Modifier.fillMaxSize(),
                            state = mMapViewModel.state
                        )

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            SearchField {
                                mMarkerProvider.getMarker(it)?.run {
                                    mMapViewModel.centerOnMarker(idStr)
                                }
                            }

                            FloorSwitch(
                                modifier = Modifier.align(Alignment.End),
                                onClick = mMapFloorSwitcher::setFloor,
                                maxFloor = FLOOR_NUM
                            )
                        }
                    }
                }
            }
        }
    }
}
