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

private const val DB_ASSET = "markers.db"

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
}
