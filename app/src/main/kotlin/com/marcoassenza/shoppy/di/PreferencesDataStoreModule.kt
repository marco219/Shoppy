package com.marcoassenza.shoppy.di

import android.content.Context
import com.marcoassenza.shoppy.data.local.datastore.PreferencesDataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object PreferencesDataStoreModule {
    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext appContext: Context): PreferencesDataStoreManager =
        PreferencesDataStoreManager(appContext)
}
