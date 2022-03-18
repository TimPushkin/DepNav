package ru.spbu.depnav.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.model.MarkerText

@Database(entities = [Marker::class, MarkerText::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun markerDao(): MarkerDao

    abstract fun markerTextDao(): MarkerTextDao
}
