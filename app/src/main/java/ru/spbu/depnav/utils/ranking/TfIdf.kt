package ru.spbu.depnav.utils.ranking

import kotlin.math.log10

/** tf-idf ranking algorithm. */
class TfIdf : Ranker {
    override fun rank(
        queryWordStats: Iterable<Ranker.QueryWordStat>,
        docWordNum: Int,
        avgWordNum: Double,
        docsNum: Int
    ): Double {
        var rank = 0.0
        for (queryWordStat in queryWordStats) {
            rank += tf(queryWordStat.appearanceNum, docWordNum) *
                idf(docsNum, queryWordStat.matchedDocsNum)
        }
        return rank
    }

    private fun tf(appearanceNum: Int, docWordNum: Int) = appearanceNum.toDouble() / docWordNum

    private fun idf(docsCount: Int, matchedDocsCount: Int) =
        log10((docsCount + 1.0) / (matchedDocsCount + 1))
}
