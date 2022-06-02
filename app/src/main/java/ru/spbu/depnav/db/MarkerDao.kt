package ru.spbu.depnav.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.model.MarkerText

/**
 * DAO for the table containing the available [Marker] entries.
 */
@Dao
interface MarkerDao {
    /**
     * Inserts the provided [Marker] entries into the database.
     */
    @Insert
    suspend fun insertAll(vararg markers: Marker)

    /**
     * Returns [Marker] entries with the provided ID and the corresponding [MarkerText] entries on
     * the specified language.
     */
    @Query(
        "SELECT *, marker_texts.lid FROM markers " +
                "JOIN marker_texts ON markers.id = marker_texts.marker_id " +
                "WHERE markers.id = :id AND marker_texts.lid = :language"
    )
    suspend fun loadWithTextById(
        id: Int,
        language: MarkerText.LanguageId
    ): Map<Marker, List<MarkerText>>

    /**
     * Returns [Marker] entries from the specified floor and the corresponding [MarkerText] entries
     * on the requested language.
     */
    @Query(
        "SELECT *, marker_texts.lid FROM markers " +
                "JOIN marker_texts ON markers.id = marker_texts.marker_id " +
                "WHERE markers.floor = :floor AND marker_texts.lid = :language"
    )
    suspend fun loadWithTextByFloor(
        floor: Int,
        language: MarkerText.LanguageId
    ): Map<Marker, List<MarkerText>>
}
