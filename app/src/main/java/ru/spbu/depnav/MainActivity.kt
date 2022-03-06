package ru.spbu.depnav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ovh.plrapps.mapcompose.ui.MapUI
import ru.spbu.depnav.providers.MarkerProvider
import ru.spbu.depnav.ui.theme.DepNavTheme
import ru.spbu.depnav.models.MapViewModel
import ru.spbu.depnav.ui.elements.SearchField

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var mMapViewModel: MapViewModel
    private val mMarkerProvider = MarkerProvider()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mMapViewModel = MapViewModel(applicationContext.assets, "tiles/spbu-mm", emptyList())

        setContent {
            DepNavTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(
                        contentAlignment = Alignment.TopCenter
                    ) {
                        MapUI(
                            modifier = Modifier.fillMaxSize(),
                            state = mMapViewModel.state
                        )
                        SearchField {
                            mMarkerProvider.getMarkerInfo(it)?.run {
                                mMapViewModel.centerOnMarker(id)
                            }
                        }
                    }
                }
            }
        }
    }
}
