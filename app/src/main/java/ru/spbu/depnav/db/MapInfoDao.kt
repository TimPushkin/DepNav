package ru.spbu.depnav.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.spbu.depnav.model.MapInfo

/**
 * DAO for the table containing the available [MapInfo] entries.
 */
@Dao
interface MapInfoDao {
    /**
     * Inserts the provided [MapInfo] entries into the database.
     */
    @Insert
    suspend fun insertAll(vararg mapInfos: MapInfo)

    /**
     * Returns a [MapInfo] with the provided map name.
     */
    @Query("SELECT * FROM map_infos WHERE map_name = :name")
    suspend fun loadByName(name: String): MapInfo
}
