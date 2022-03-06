package ru.spbu.depnav.providers

import android.graphics.drawable.Icon
import android.util.Log
import ru.spbu.depnav.models.Marker

private const val TAG = "MarkerIconProvider"

class MarkerIconProvider {

    fun getIcon(type: Marker.MarkerType): Icon {
        Log.d(TAG, "Received query for ${type.name}")
        TODO("Not implemented")
    }
}
