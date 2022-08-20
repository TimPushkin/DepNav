package ru.spbu.depnav.data.repository

import ru.spbu.depnav.data.db.MapInfoDao
import ru.spbu.depnav.data.model.MapInfo
import javax.inject.Inject

/** Repository for loading and saving [MapInfo] objects. */
class MapInfoRepo @Inject constructor(private val dao: MapInfoDao) {
    /** Saves the provided [MapInfo] objects. */
    suspend fun insertAll(vararg mapInfos: MapInfo) = dao.insertAll(*mapInfos)

    /** Loads a [MapInfo] by its name. */
    suspend fun loadByName(name: String) = dao.loadByName(name)
}
