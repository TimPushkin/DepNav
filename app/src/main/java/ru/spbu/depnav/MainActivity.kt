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
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import ovh.plrapps.mapcompose.ui.MapUI
import ru.spbu.depnav.controller.MapFloorSwitcher
import ru.spbu.depnav.db.AppDatabase
import ru.spbu.depnav.provider.MarkerProvider
import ru.spbu.depnav.ui.theme.DepNavTheme
import ru.spbu.depnav.provider.TileProviderFactory
import ru.spbu.depnav.ui.elements.FloorSwitch
import ru.spbu.depnav.ui.elements.SearchField
import ru.spbu.depnav.viewmodel.MapViewModel

private const val TAG = "MainActivity"

// TODO: retrieve from saved state
private const val MAP_NAME = "spbu-mm"
private const val DB_ASSET = "$MAP_NAME/markers/markers.db"
private const val TILES_PATH = "$MAP_NAME/tiles"

class MainActivity : ComponentActivity() {
    private lateinit var mAppDatabase: AppDatabase
    private lateinit var mMapViewModel: MapViewModel
    private lateinit var mMapFloorSwitcher: MapFloorSwitcher
    private val mMarkerProvider = MarkerProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAppDatabase = Room.databaseBuilder(this, AppDatabase::class.java, "markers.db")
            .createFromAsset(DB_ASSET)
            .build()

        val mapInfo = runBlocking { mAppDatabase.mapInfoDao().loadByName(MAP_NAME) }

        mMapViewModel = MapViewModel(mapInfo.floorWidth, mapInfo.floorHeight)
        mMapFloorSwitcher = MapFloorSwitcher(
            mMapViewModel,
            TileProviderFactory(applicationContext.assets, TILES_PATH),
            mAppDatabase.markerDao(),
            mapInfo.floorsNum
        ).apply { setFloor(1) }

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
                                maxFloor = mapInfo.floorsNum
                            )
                        }
                    }
                }
            }
        }
    }
}
