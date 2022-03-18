package ru.spbu.depnav.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.spbu.depnav.model.MarkerText

@Dao
interface MarkerTextDao {
    @Insert
    suspend fun insertAll(vararg markerTexts: MarkerText)

    @Query(
        "SELECT * FROM marker_texts " +
                "WHERE marker_texts MATCH :tokens AND lid = :language"
    )
    suspend fun loadByTokens(tokens: String, language: MarkerText.LanguageId): List<MarkerText>
}
