package ru.spbu.depnav.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.model.MarkerText

@Dao
interface MarkerDao {
    @Insert
    suspend fun insertAll(vararg markers: Marker)

    @Query("SELECT * FROM markers WHERE id = :id")
    suspend fun loadById(id: Int): Marker

    @Query(
        "SELECT *, marker_texts.lid FROM markers " +
                "JOIN marker_texts ON markers.id = marker_texts.marker_id " +
                "WHERE markers.floor = :floor"
    )
    suspend fun loadWithTextByFloor(floor: Int): Map<Marker, List<MarkerText>>
}
