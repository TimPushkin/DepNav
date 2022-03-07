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
import ru.spbu.depnav.providers.MarkerProvider
import ru.spbu.depnav.ui.theme.DepNavTheme
import ru.spbu.depnav.models.MapViewModel
import ru.spbu.depnav.providers.TileProviderFactory
import ru.spbu.depnav.ui.elements.FloorSwitch
import ru.spbu.depnav.ui.elements.SearchField

private const val TAG = "MainActivity"

private const val MAP_WIDTH = 11264
private const val MAP_HEIGHT = 5120
private const val TILES_PATH = "tiles/spbu-mm"
private const val FLOOR_NUM = 4

class MainActivity : ComponentActivity() {
    private lateinit var mMapViewModel: MapViewModel
    private lateinit var mTileProviderFactory: TileProviderFactory
    private val mMarkerProvider = MarkerProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mTileProviderFactory = TileProviderFactory(applicationContext.assets, TILES_PATH, FLOOR_NUM)
        mMapViewModel = MapViewModel(
            MAP_WIDTH,
            MAP_HEIGHT,
            tileProvider = mTileProviderFactory.makeTileProviderForFloor(),
            markers = emptyList()
        )

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
                                mMarkerProvider.getMarkerInfo(it)?.run {
                                    mMapViewModel.centerOnMarker(id)
                                }
                            }

                            val onFloorSwitch = { floor: Int ->
                                mMapViewModel.changeTileProvider(
                                    mTileProviderFactory.makeTileProviderForFloor(floor)
                                )
                            }

                            FloorSwitch(
                                modifier = Modifier.align(Alignment.End),
                                onUpClick = onFloorSwitch,
                                onDownClick = onFloorSwitch,
                                maxFloor = FLOOR_NUM
                            )
                        }
                    }
                }
            }
        }
    }
}
