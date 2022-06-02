package ru.spbu.depnav.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

/**
 * Text information related to a [Marker].
 */
@Fts4(notIndexed = ["marker_id"], languageId = "lid", tokenizer = FtsOptions.TOKENIZER_UNICODE61)
@Entity(tableName = "marker_texts")
data class MarkerText(
    /** ID of the [Marker] to which this text relates. */
    @ColumnInfo(name = "marker_id") val markerId: Int,
    /** Language used in this text. */
    @ColumnInfo(name = "lid") val languageId: LanguageId,
    /** Title of the [Marker] to which this text relates. */
    val title: String?,
    /** Description of the [Marker] to which this text relates. */
    val description: String?
) {
    /**
     * IDs of the supported languages.
     */
    enum class LanguageId { EN, RU }
}
