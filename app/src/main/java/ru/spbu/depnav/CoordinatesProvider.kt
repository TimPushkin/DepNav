package ru.spbu.depnav

import android.util.Log
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.io.InputStream

private const val TAG = "CoordinatesProvider"

@OptIn(ExperimentalSerializationApi::class)
class CoordinatesProvider(inputStream: InputStream) {
    val coordinates: Map<String, Pair<Float, Float>> = Json.decodeFromStream(inputStream)

    fun getCoordinatesOf(name: String): Pair<Float, Float>? {
        Log.i(TAG, "Searching for the coordinates of $name")
        return coordinates[name]
    }
}
