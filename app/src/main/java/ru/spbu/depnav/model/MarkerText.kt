package ru.spbu.depnav.model

import androidx.room.*

@Fts4(notIndexed = ["marker_id"], languageId = "lid", tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "marker_texts")
data class MarkerText(
    @ColumnInfo(name = "marker_id") val markerId: Int,
    @ColumnInfo(name = "lid") val languageId: LanguageId,
    val title: String?,
    val description: String?
) {
    enum class LanguageId { EN, RU }
}
