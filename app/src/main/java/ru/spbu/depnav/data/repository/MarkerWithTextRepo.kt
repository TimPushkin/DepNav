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
import ru.spbu.depnav.data.composite.rank
import ru.spbu.depnav.data.db.MarkerWithTextDao
import ru.spbu.depnav.data.model.Language
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.data.model.lowercase
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
        val rankedMarkerWithTexts = dao.loadByTokens(mapId, tokenized, language)
            .map { (match, markers) ->
                val (markerText, matchInfo) = match
                val marker = checkNotNull(markers.firstOrNull()) { "No marker for $markerText" }
                val rank = with(markerText) {
                    when (query.lowercase(language)) {
                        title?.lowercase(language) -> Double.POSITIVE_INFINITY
                        location?.lowercase(language) -> Double.MAX_VALUE
                        description?.lowercase(language) -> Double.MAX_VALUE
                        else -> ranker.rank(matchInfo)
                    }
                }
                Log.v(TAG, "$markerText ranked $rank")
                Pair(MarkerWithText(marker, markerText), rank)
            }

        return rankedMarkerWithTexts
            .sortedWith(
                compareBy<Pair<MarkerWithText, Double>> { it.second }
                    .thenByDescending { it.first.text.location } // Location dominates the title
                    .thenByDescending { it.first.text.title }
                    .thenByDescending { it.first.text.description }
            )
            .map { it.first }
    }

    private fun String.tokenized() =
        plus(" ").replace(Regex("\\W+"), Regex.escapeReplacement("* "))
}
