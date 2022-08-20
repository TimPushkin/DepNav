package ru.spbu.depnav.data.repository

import android.util.Log
import ru.spbu.depnav.data.db.MarkerWithTextDao
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import javax.inject.Inject

private const val TAG = "MarkerWithTextRepo"

/** Repository for loading and saving [Marker] objects with associated [MarkerText] objects. */
class MarkerWithTextRepo @Inject constructor(private val dao: MarkerWithTextDao) {
    suspend fun insertAll(markersWithText: Map<Marker, MarkerText>) {
        dao.insertMarkers(markersWithText.keys)
        dao.insertMarkerTexts(markersWithText.values)
    }

    suspend fun loadById(id: Int): Pair<Marker, MarkerText> {
        val language = MarkerText.LanguageId.getCurrent()
        val (marker, markerTexts) = dao.loadById(id, language).entries.firstOrNull()
            ?: throw IllegalArgumentException("No markers with ID $id")
        val markerText = markerTexts.squeezedFor(marker, language)
        return marker to markerText
    }

    suspend fun loadByFloor(floor: Int, language: MarkerText.LanguageId): Map<Marker, MarkerText> {
        val markersWithTexts = dao.loadByFloor(floor, language)
        return markersWithTexts.entries.associate { (marker, markerTexts) ->
            val markerText = markerTexts.squeezedFor(marker, language)
            marker to markerText
        }
    }

    private fun List<MarkerText>.squeezedFor(marker: Marker, language: MarkerText.LanguageId) =
        firstOrNull() ?: run {
            Log.w(TAG, "Marker $marker has no text on $language")
            MarkerText(marker.id, language, null, null)
        }

    suspend fun loadByTokens(
        tokens: String,
        language: MarkerText.LanguageId
    ): Map<Marker, MarkerText> {
        val textsWithMarkers = dao.loadByTokens(tokens, language)
        return textsWithMarkers.entries.associate { (markerText, markers) ->
            val marker = markers.firstOrNull()
            checkNotNull(marker) { "MarkerText $markerText has no associated marker" }
            marker to markerText
        }
    }
}
