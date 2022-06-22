package ru.spbu.depnav.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.spbu.depnav.model.MapInfo
import ru.spbu.depnav.model.Marker
import ru.spbu.depnav.model.MarkerText

/**
 * Room database containing information about maps and their markers.
 */
@Database(entities = [MapInfo::class, Marker::class, MarkerText::class], version = 2)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    /**
     * DAO for the table containing information about the available maps.
     */
    abstract fun mapInfoDao(): MapInfoDao

    /**
     * DAO for the table containing markers description.
     */
    abstract fun markerDao(): MarkerDao

    /**
     * DAO for the table containing marker texts.
     */
    abstract fun markerTextDao(): MarkerTextDao

    companion object {
        private const val DB_ASSET = "markers.db"
        private lateinit var instance: AppDatabase

        /**
         * Get the singleton instance of this database.
         */
        fun getInstance(context: Context): AppDatabase {
            synchronized(AppDatabase::class.java) {
                if (!this::instance.isInitialized) {
                    instance = Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            DB_ASSET
                        )
                        .createFromAsset(DB_ASSET)
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return instance
        }
    }
}
