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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.*
import ru.spbu.depnav.db.AppDatabase
import ru.spbu.depnav.model.Floor
import ru.spbu.depnav.model.MarkerText
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
    private val mScope = CoroutineScope(Dispatchers.Main)
    private lateinit var mAppDatabase: AppDatabase
    private lateinit var mFloors: Map<Int, Floor>

    private val startSearch = registerForActivityResult(SearchForMarker()) { result ->
        Log.i(TAG, "Received $result as a search result")

        val markerId = result ?: return@registerForActivityResult
        lifecycleScope.launch {
            val marker = mAppDatabase.markerDao().loadById(markerId)
            Log.i(TAG, "Loaded searched marker: $marker")
            setFloor(marker.floor) { mMapScreenState.centerOnMarker(marker.idStr) }
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
            mMapScreenState.setParams(mapInfo.floorWidth, mapInfo.floorHeight, mapInfo.tileSize)
            setFloor(mFloors.keys.first())
        }

        setContent {
            DepNavTheme {
                window.statusBarColor = MaterialTheme.colors.background.toArgb()
                window.navigationBarColor = MaterialTheme.colors.surface.toArgb()

                if (!isSystemInDarkTheme()) {
                    WindowInsetsControllerCompat(window, window.decorView).apply {
                        isAppearanceLightStatusBars = true
                        isAppearanceLightNavigationBars = true
                    }
                }

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

        runBlocking {
            mFloors = List(floorsNum) {
                val floor = it + 1
                floor to Floor(
                    layers = listOf(factory.makeTileProviderForFloor(floor)),
                    markers = async(Dispatchers.IO) {
                        markerDao.loadWithTextByFloor(floor).entries.associate { (marker, markerTexts) ->
                            val markerText = markerTexts.firstOrNull() ?: run {
                                Log.e(TAG, "Marker $marker has no text")
                                MarkerText.EMPTY
                            }
                            marker to markerText
                        }
                    }
                )
            }.toMap()
        }
    }

    private fun setFloor(floorIndex: Int, onFinished: () -> Unit = {}) {
        val floor = mFloors[floorIndex]
        if (floor == null) {
            Log.e(TAG, "Cannot switch to floor $floorIndex which does not exist")
            return
        }

        Log.i(TAG, "Switching to floor $floorIndex")

        mMapScreenState.currentFloor = floorIndex
        mMapScreenState.displayMarkerInfo = false
        mMapScreenState.replaceLayersWith(emptyList())
        mMapScreenState.replaceMarkersWith(emptyMap())

        mScope.launch {
            mMapScreenState.replaceLayersWith(floor.layers)
            mMapScreenState.replaceMarkersWith(floor.markers.await())
            Log.d(TAG, "Switched to floor $floorIndex")
            onFinished()
        }
    }
}
