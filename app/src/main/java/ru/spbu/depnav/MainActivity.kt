package ru.spbu.depnav

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.spbu.depnav.controller.MapFloorSwitcher
import ru.spbu.depnav.db.AppDatabase
import ru.spbu.depnav.ui.theme.DepNavTheme
import ru.spbu.depnav.provider.TileProviderFactory
import ru.spbu.depnav.ui.MainScreen
import ru.spbu.depnav.viewmodel.MapViewModel

private const val TAG = "MainActivity"

// TODO: retrieve from saved state
private const val MAP_NAME = "spbu-mm"
private const val TILES_PATH = "$MAP_NAME/tiles"

class MainActivity : ComponentActivity() {
    private lateinit var mAppDatabase: AppDatabase
    private lateinit var mMapViewModel: MapViewModel
    private lateinit var mMapFloorSwitcher: MapFloorSwitcher

    private val startSearch = registerForActivityResult(SearchForMarker()) { result ->
        Log.i(TAG, "Received ${result.toString()} as a search result")

        val markerId = result ?: return@registerForActivityResult
        lifecycleScope.launch {
            val marker = mAppDatabase.markerDao().loadById(markerId)
            mMapFloorSwitcher.setFloor(marker.floor)
            mMapViewModel.centerOnMarker(marker.idStr)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAppDatabase = AppDatabase.getInstance(this)

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
                    floorsNum = mapInfo.floorsNum,
                    onStartSearch = startSearch::launch,
                    onFloorSwitch = mMapFloorSwitcher::setFloor
                )
            }
        }
    }

    class SearchForMarker : ActivityResultContract<Unit, Int?>() {
        override fun createIntent(context: Context, input: Unit) =
            Intent(context, SearchActivity::class.java)

        override fun parseResult(resultCode: Int, intent: Intent?): Int? {
            if (resultCode != Activity.RESULT_OK) return null
            // getInt() is not used as it requires a default value
            return intent?.extras?.get(EXTRA_MARKER_ID)?.run { toString().toInt() }
        }
    }
}
