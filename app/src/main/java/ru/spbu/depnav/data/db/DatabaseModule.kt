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

@file:Suppress("UndocumentedPublicFunction") // Method names are self-explanatory

package ru.spbu.depnav.data.db

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DB_ASSET = "maps.db"

/** Database-related providers for Hilt. */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        DB_ASSET
    )
        .createFromAsset(DB_ASSET)
        .fallbackToDestructiveMigration()
        .build()

    @Provides
    fun provideMapInfoDao(appDatabase: AppDatabase) = appDatabase.mapInfoDao()

    @Provides
    fun provideMarkerWithTextDao(appDatabase: AppDatabase) = appDatabase.markerWithTextDao()

    @Provides
    fun provideSearchHistoryDao(appDatabase: AppDatabase) = appDatabase.searchHistoryDao()
}
