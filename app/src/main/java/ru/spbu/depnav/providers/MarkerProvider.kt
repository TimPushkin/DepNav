package ru.spbu.depnav.providers

import android.util.Log
import ru.spbu.depnav.models.Marker

private const val TAG = "MarkerProvider"

class MarkerProvider {

    fun getMarkerInfo(id: String): Marker? {
        Log.d(TAG, "Received query for \"$id\"")
        TODO("Not implemented")
    }
}
