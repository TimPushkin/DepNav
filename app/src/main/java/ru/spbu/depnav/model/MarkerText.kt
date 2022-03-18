package ru.spbu.depnav.model

import androidx.room.*

@Fts4(languageId = "lid", tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "marker_texts")
data class MarkerText(
    @ColumnInfo(name = "marker_id") val markerId: Int,
    @ColumnInfo(name = "lid") val languageId: LanguageId?, // TODO: try to remove
    val title: String?,
    val description: String?
) {
    enum class LanguageId { RU, EN }
}
