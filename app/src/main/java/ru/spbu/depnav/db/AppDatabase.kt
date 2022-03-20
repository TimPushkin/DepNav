package ru.spbu.depnav.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
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

    companion object {
        private const val DB_ASSET = "markers.db"
        private lateinit var instance: AppDatabase

        fun getInstance(context: Context): AppDatabase {
            synchronized(AppDatabase::class.java) {
                if (!this::instance.isInitialized) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "markers.db"
                    ).createFromAsset(DB_ASSET).build()
                }
            }
            return instance
        }
    }
}
