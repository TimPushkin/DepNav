package ru.spbu.depnav

import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

private const val TAG = "CoordinatesProvider"

@OptIn(ExperimentalSerializationApi::class)
class CoordinatesProvider(inputStream: InputStream) {
    private val mCoordinates = Json.decodeFromStream<Map<String, Pair<Float, Float>>>(inputStream)

    fun getCoordinatesOf(name: String) = mCoordinates[name]?.apply {
        Log.i(TAG, "Coordinates of $name: ($first, $second)")
    } ?: run {
        Log.i(TAG, "Coordinates of $name: not found")
        null
    }
}
