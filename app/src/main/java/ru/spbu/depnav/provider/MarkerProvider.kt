package ru.spbu.depnav.provider

import android.util.Log
import ru.spbu.depnav.model.Marker

private const val TAG = "MarkerProvider"

class MarkerProvider {

    fun getMarker(id: String): Marker? {
        Log.d(TAG, "Received query for \"$id\"")
        TODO("Not implemented")
    }
}
