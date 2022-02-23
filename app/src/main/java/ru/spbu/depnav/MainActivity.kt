package ru.spbu.depnav

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import ru.spbu.depnav.ui.theme.DepNavTheme
import kotlin.math.roundToInt

private const val TAG = "MainActivity"

class MainActivity : ComponentActivity() {
    private lateinit var mCoordsProvider: CoordinatesProvider

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mCoordsProvider = CoordinatesProvider(resources.openRawResource(R.raw.spbu_mathmech_coords))

        setContent {
            DepNavTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Box(
                        contentAlignment = Alignment.TopCenter
                    ) {
                        var mapOffset by remember { mutableStateOf(Offset.Zero) }

                        NavigationMap(mapOffset)
                        SearchField {
                            mCoordsProvider.getCoordinatesOf(it)?.run {
                                mapOffset = Offset(first, second)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchField(onSearch: (String) -> Unit) {
    var value by remember { mutableStateOf("") }

    TextField(
        value = value,
        onValueChange = { value = it },
        placeholder = { Text("Search...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { onSearch(value) }
        ),
        singleLine = true
    )
}

@Composable
fun NavigationMap(initOffset: Offset) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember(initOffset) { mutableStateOf(initOffset) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(initOffset) {
                detectTransformGestures { _, pan, zoom, _ ->
                    offset += pan
                    scale *= zoom
                }
            }
    ) {
        Image(
            painter = painterResource(R.drawable.map),
            contentDescription = "Navigation map",
            modifier = Modifier
                .absoluteOffset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
        )
    }
}
