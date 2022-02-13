package com.marcoassenza.shoppy.di

import android.app.Application
import androidx.room.Room
import com.marcoassenza.shoppy.data.local.ItemDao
import com.marcoassenza.shoppy.data.local.ItemDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ItemDatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(application: Application): ItemDatabase {
        return Room.databaseBuilder(application, ItemDatabase::class.java, "item_database")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideItemDao(db: ItemDatabase): ItemDao {
        return db.getItemDao()
    }
}