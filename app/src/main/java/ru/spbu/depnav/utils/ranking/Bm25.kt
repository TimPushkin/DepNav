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

package ru.spbu.depnav.utils.ranking

import kotlin.math.ln

/** Okapi BM25 ranking algorithm. */
class Bm25(
    /**
     * A free BM25 parameter which controls the effect of document length normalization. The bigger
     * `b` is, the greater is the effect of the document length compared to the average.
     *
     * Should be in `[0, 1]`. The default value of `0,75` should be suitable in most cases.
     */
    var b: Double = 0.75,
    /**
     * A free BM25 parameter which controls term frequency saturation. The lower `k1` is, the
     * quicker the number of occurrences of a word in a document stops having a noticeable effect.
     *
     * Should be `>= 0`, usually is in `[0, 3]`. The default value of `1,2` should be suitable in
     * most cases.
     */
    val k1: Double = 1.2
) : Ranker {
    override fun rank(
        queryWordStats: Iterable<Ranker.QueryWordStat>,
        docWordNum: Int,
        avgWordNum: Double,
        docsNum: Int
    ): Double {
        val numeratorCoefficient = k1 + 1
        val denominatorAddend = when {
            docWordNum == 0 -> k1 * (1 - b) // make (docWordNum / avgWordNum) equal 0
            avgWordNum == 0.0 -> k1 * (1 + b) // make (docWordNum / avgWordNum) equal 2
            else -> k1 * (1 - b + b * docWordNum / avgWordNum)
        }

        var rank = 0.0
        for (queryWordStat in queryWordStats) {
            val numerator = queryWordStat.appearanceNum * numeratorCoefficient
            val denominator = queryWordStat.appearanceNum + denominatorAddend
            rank += if (denominator != 0.0) {
                idf(docsNum, queryWordStat.matchedDocsNum) * numerator / denominator
            } else {
                0.0
            }
        }

        return rank
    }

    private fun idf(docsCount: Int, matchedDocsCount: Int) =
        ln((docsCount - matchedDocsCount + 0.5) / (matchedDocsCount + 0.5) + 1)
}
