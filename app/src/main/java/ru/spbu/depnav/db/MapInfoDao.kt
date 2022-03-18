package ru.spbu.depnav.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.spbu.depnav.model.MapInfo

@Dao
interface MapInfoDao {
    @Insert
    suspend fun insertAll(vararg mapInfos: MapInfo)

    @Query("SELECT * FROM map_infos WHERE map_name = :name")
    suspend fun loadByName(name: String): MapInfo
}
