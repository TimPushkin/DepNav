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

package ru.spbu.depnav.data.composite

import androidx.room.ColumnInfo
import androidx.room.Embedded
import ru.spbu.depnav.data.model.MarkerText
import ru.spbu.depnav.utils.ranking.Ranker
import java.nio.ByteBuffer
import java.nio.ByteOrder

/** [MarkerText] with its `matchinfo` collected by SQLite FTS4 during matching. */
data class MarkerTextMatch(
    /** [MarkerText] that was matched. */
    @Embedded val markerText: MarkerText,
    /**
     * [`matchinfo`](https://www.sqlite.org/fts3.html#matchinfo) that was collected during FTS4
     * matching with [MarkerTextMatch.Companion.FORMAT_STRING] as format string.
     */
    @ColumnInfo(name = "match_info") val matchInfo: ByteArray
) {
    // Required because of a ByteArray property
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MarkerTextMatch) return false
        if (markerText != other.markerText) return false
        return matchInfo.contentEquals(other.matchInfo)
    }

    // Required because of a ByteArray property
    override fun hashCode() = 31 * markerText.hashCode() + matchInfo.contentHashCode()

    companion object {
        /** Format string that must be used for [matchInfo] retrieval from a FTS4 table. */
        const val FORMAT_STRING = "pcxnal"
    }
}

/** Rank a match based on its matchinfo. */
fun Ranker.rank(matchInfo: ByteArray): Double {
    val buf = ByteBuffer.wrap(matchInfo).apply { order(ByteOrder.nativeOrder()) }

    val p = buf.int // Number of matchable phrases in the query
    val c = buf.int // Number of columns in the table

    // Parse x -- phrase-column matching statistics
    val queryWordStats = mutableListOf<Ranker.QueryWordStat>()
    repeat(p) {
        var appearanceNum = 0 // Appearances in the whole row
        var matchedDocsNum = 0 // All docs where the word appears
        repeat(c) {
            appearanceNum += buf.int // Take hits_this_row
            buf.position(buf.position() + Int.SIZE_BYTES) // Skip hits_all_rows
            matchedDocsNum += buf.int // Take docs_with_hits
        }
        queryWordStats += Ranker.QueryWordStat(appearanceNum, matchedDocsNum)
    }

    val n = buf.int // Number of rows in the table

    // Parse a -- for each column, average number of tokens in the whole table
    var aSum = 0.0 // Average number of tokens in table's rows
    repeat(c) { aSum += buf.int }

    // Parse l -- for each column, number of tokens in the current row
    var lSum = 0 // Number of tokens in the whole row
    repeat(c) { lSum += buf.int }

    return rank(
        queryWordStats = queryWordStats,
        docWordNum = lSum,
        avgWordNum = aSum,
        docsNum = n
    )
}
