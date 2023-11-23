/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofei Pushkin
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
import ru.spbu.depnav.data.composite.MarkerWithText
import ru.spbu.depnav.data.db.MarkerWithTextDao
import ru.spbu.depnav.data.model.Language
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.data.model.rankWith
import ru.spbu.depnav.utils.ranking.Bm25
import ru.spbu.depnav.utils.ranking.Ranker
import javax.inject.Inject

private const val TAG = "MarkerWithTextRepo"

/** Repository for [Marker]s and [MarkerText]s. */
class MarkerWithTextRepo(
    private val dao: MarkerWithTextDao,
    /** Ranking algorithm used to sort FTS-queried entries. */
    var ranker: Ranker
) {
    /** Repository for [Marker]s and [MarkerText]s with [Bm25] as a ranker. */
    @Inject
    constructor(dao: MarkerWithTextDao) : this(dao, Bm25())

    /** Loads a [Marker] by its ID and its corresponding [MarkerText] on the specified language. */
    suspend fun loadById(id: Int, language: Language): MarkerWithText {
        val (marker, markerTexts) = requireNotNull(
            dao.loadById(id, language).entries.firstOrNull()
        ) { "No markers with ID $id" }
        val markerText = checkNotNull(markerTexts.firstOrNull()) {
            "No text on $language for $marker"
        }
        return MarkerWithText(marker, markerText)
    }

    /**
     * Loads [Marker]s from the specified map and floor with their corresponding [MarkerText]s on
     * the specified language.
     */
    suspend fun loadByFloor(mapId: Int, floor: Int, language: Language): List<MarkerWithText> {
        val markersWithTexts = dao.loadByFloor(mapId, floor, language)
        return markersWithTexts.entries.map { (marker, texts) ->
            MarkerWithText(
                marker,
                text = checkNotNull(texts.firstOrNull()) { "No text on $language for $marker" }
            )
        }
    }

    /**
     * Loads [Marker]s from the specified map with their corresponding [MarkerText]s on the given
     * language so that the text satisfies the specified query. The results are sorted first by
     * relevance (most relevant first), then alphabetically.
     */
    suspend fun loadByQuery(mapId: Int, query: String, language: Language): List<MarkerWithText> {
        val tokenized = query.tokenized()

        Log.d(TAG, "Loading query '$query' tokenized as '$tokenized'")
        val rankedTextsWithMarkers = dao.loadByTokens(mapId, tokenized, language).map {
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
            .map { (markerText, markers, _) ->
                val marker = checkNotNull(markers.firstOrNull()) { "No marker for $markerText" }
                MarkerWithText(marker, markerText)
            }
    }

    private fun String.tokenized() =
        plus(" ").replace(Regex("\\W+"), Regex.escapeReplacement("* "))
}
