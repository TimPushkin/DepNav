/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofey Pushkin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.spbu.depnav.data.repository

import android.util.Log
import ru.spbu.depnav.data.db.MarkerWithTextDao
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.data.model.rankWith
import ru.spbu.depnav.utils.ranking.Bm25
import ru.spbu.depnav.utils.ranking.Ranker
import javax.inject.Inject

private const val TAG = "MarkerWithTextRepo"

/** Repository for [Marker] objects with associated [MarkerText] objects. */
class MarkerWithTextRepo(
    private val dao: MarkerWithTextDao,
    /** Ranking algorithm used to sort queried entries. */
    var ranker: Ranker
) {
    /**
     * Repository for [Marker] objects with associated [MarkerText] objects with BM25 as a ranker.
     */
    @Inject
    constructor(dao: MarkerWithTextDao) : this(dao, Bm25())

    /** Loads a [Marker] by its ID and its corresponding [MarkerText] on the current language. */
    suspend fun loadById(id: Int): Pair<Marker, MarkerText> {
        val language = MarkerText.LanguageId.getCurrent()
        val (marker, markerTexts) = dao.loadById(id, language).entries.firstOrNull()
            ?: throw IllegalArgumentException("No markers with ID $id")
        val markerText = markerTexts.squeezedFor(marker, language)
        return marker to markerText
    }

    /**
     * Loads all [Markers][Marker] from the specified map and floor with their corresponding
     * [MarkerTexts][MarkerText] on the current language.
     */
    suspend fun loadByFloor(mapName: String, floor: Int): Map<Marker, MarkerText> {
        val language = MarkerText.LanguageId.getCurrent()
        val markersWithTexts = dao.loadByFloor(mapName, floor, language)
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

    /**
     * Loads [Markers][Marker] from the specified map with their corresponding
     * [MarkerTexts][MarkerText] on the current language so that the text satisfies the specified
     * query. The results are sorted first by relevance, then alphabetically.
     */
    suspend fun loadByQuery(mapName: String, query: String): Map<Marker, MarkerText> {
        val language = MarkerText.LanguageId.getCurrent()
        val tokenized = query.tokenized()

        Log.d(TAG, "Loading query '$query' tokenized as '$tokenized'")
        val rankedTextsWithMarkers = dao.loadByTokens(mapName, tokenized, language).map {
            val rank = it.key.run {
                when (query) {
                    markerText.title -> Double.POSITIVE_INFINITY
                    markerText.description -> Double.MAX_VALUE
                    else -> rankWith(ranker)
                }
            }
            Log.v(TAG, "${it.key.markerText} ranked $rank")
            Triple(it.key.markerText, it.value, rank)
        }

        return rankedTextsWithMarkers
            .sortedWith(
                compareBy<Triple<MarkerText, List<Marker>, Double>> { it.third }
                    .thenByDescending { it.first.title }
                    .thenByDescending { it.first.description }
            )
            .associate { (markerText, markers, _) ->
                val marker = markers.firstOrNull()
                checkNotNull(marker) { "$markerText has no associated marker" }
                marker to markerText
            }
    }

    private fun String.tokenized() =
        plus(" ").replace(Regex("\\W+"), Regex.escapeReplacement("* "))
}
