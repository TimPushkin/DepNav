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
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.spbu.depnav.db.AppDatabase
import ru.spbu.depnav.model.Floor
import ru.spbu.depnav.ui.theme.DepNavTheme
import ru.spbu.depnav.provider.TileProviderFactory
import ru.spbu.depnav.ui.map.FLOOR_UNINITIALIZED
import ru.spbu.depnav.ui.map.MapScreen
import ru.spbu.depnav.ui.map.MapScreenState

private const val TAG = "MainActivity"

// TODO: retrieve from saved state
private const val MAP_NAME = "spbu-mm"
private const val TILES_PATH = "$MAP_NAME/tiles"

class MainActivity : ComponentActivity() {
    private val mMapScreenState: MapScreenState by viewModels()
    private lateinit var mAppDatabase: AppDatabase
    private lateinit var mFloors: Map<Int, Floor>

    private val startSearch = registerForActivityResult(SearchForMarker()) { result ->
        Log.i(TAG, "Received ${result.toString()} as a search result")

        val markerId = result ?: return@registerForActivityResult
        lifecycleScope.launch {
            val marker = mAppDatabase.markerDao().loadById(markerId)
            setFloor(marker.floor)
            mMapScreenState.centerOnMarker(marker.idStr)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mAppDatabase = AppDatabase.getInstance(this)
        val mapInfo = runBlocking { mAppDatabase.mapInfoDao().loadByName(MAP_NAME) }

        initFloors(mapInfo.floorsNum)

        if (mMapScreenState.currentFloor == FLOOR_UNINITIALIZED) {
            mMapScreenState.setParams(mapInfo.floorWidth, mapInfo.floorHeight)
            setFloor(mFloors.keys.first())
        }

        setContent {
            DepNavTheme {
                MapScreen(
                    mapScreenState = mMapScreenState,
                    floorsNum = mapInfo.floorsNum,
                    onStartSearch = startSearch::launch,
                    onFloorSwitch = this::setFloor
                )
            }
        }
    }

    private fun initFloors(floorsNum: Int) {
        val factory = TileProviderFactory(applicationContext.assets, TILES_PATH)
        val markerDao = mAppDatabase.markerDao()

        mFloors = List(floorsNum) {
            val floor = it + 1
            floor to Floor(listOf(factory.makeTileProviderForFloor(floor))) {
                runBlocking { markerDao.loadWithTextByFloor(floor).keys } // TODO: fix blocking
            }
        }.toMap()
    }

    private fun setFloor(floor: Int) {
        mFloors[floor]?.run {
            Log.i(
                TAG,
                "Switching to floor $floor: ${layers.count()} layers, ${markers.count()} markers"
            )

            mMapScreenState.currentFloor = floor
            mMapScreenState.replaceLayersWith(layers)
            mMapScreenState.replaceMarkersWith(markers)
        } ?: run { Log.e(TAG, "Cannot switch to floor $floor which does not exist") }
    }
}
