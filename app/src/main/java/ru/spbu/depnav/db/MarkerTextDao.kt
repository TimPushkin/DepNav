package ru.spbu.depnav.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.spbu.depnav.model.MarkerText

/**
 * DAO for the table containing the available [MarkerText] entries.
 */
@Dao
interface MarkerTextDao {
    /**
     * Inserts the provided [MarkerText] entries into the database.
     */
    @Insert
    suspend fun insertAll(vararg markerTexts: MarkerText)

    /**
     * Returns [MarkerText] entries containing the specified tokens as a substring on the specified
     * language.
     */
    @Query(
        "SELECT *, lid FROM marker_texts WHERE marker_texts MATCH :tokens AND lid = :language"
    )
    suspend fun loadByTokens(tokens: String, language: MarkerText.LanguageId): List<MarkerText>
}
