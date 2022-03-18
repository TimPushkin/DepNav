package ru.spbu.depnav.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.spbu.depnav.model.MapInfo
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.model.MarkerText

@Database(entities = [MapInfo::class, Marker::class, MarkerText::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mapInfoDao(): MapInfoDao

    abstract fun markerDao(): MarkerDao

    abstract fun markerTextDao(): MarkerTextDao
}
