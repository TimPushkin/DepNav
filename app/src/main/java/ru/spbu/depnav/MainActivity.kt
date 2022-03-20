package ru.spbu.depnav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import ru.spbu.depnav.controller.MapFloorSwitcher
import ru.spbu.depnav.db.AppDatabase
import ru.spbu.depnav.provider.MarkerProvider
import ru.spbu.depnav.ui.theme.DepNavTheme
import ru.spbu.depnav.provider.TileProviderFactory
import ru.spbu.depnav.ui.MainScreen
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
                MainScreen(
                    mapViewModel = mMapViewModel,
                    markerProvider = mMarkerProvider,
                    floorsNum = mapInfo.floorsNum,
                    onFloorSwitch = mMapFloorSwitcher::setFloor
                )
            }
        }
    }
}
