/**
 * DepNav -- department navigator.
 * Copyright (C) 2022  Timofey Pushkin
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ru.spbu.depnav.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import ru.spbu.depnav.data.model.MapInfo
import ru.spbu.depnav.data.model.Marker
import ru.spbu.depnav.data.model.MarkerText

/**
 * Room database containing information about maps and their markers.
 */
@Database(entities = [MapInfo::class, Marker::class, MarkerText::class], version = 3)
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
                    instance = Room.databaseBuilder(
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
