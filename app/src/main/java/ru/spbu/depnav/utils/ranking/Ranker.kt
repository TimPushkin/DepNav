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

/** Algorithm for document relevance estimation. */
interface Ranker {
    /**
     * Estimates rank for a query and a document.
     *
     * @param queryWordStats statistics for each word in the query.
     * @param docWordNum number of words in the document.
     * @param avgWordNum average number of words in all considered documents.
     * @param docsNum number of considered documents.
     */
    fun rank(
        queryWordStats: Iterable<QueryWordStat>,
        docWordNum: Int,
        avgWordNum: Double,
        docsNum: Int
    ): Double

    /** Statistics for a word in a query. */
    data class QueryWordStat(
        /** Number of appearances the word has in the document being ranked. */
        val appearanceNum: Int,
        /** Total number of documents in which the word appear. */
        val matchedDocsNum: Int
    )
}
