package com.marcoassenza.shoppy.di

import android.app.Application
import com.marcoassenza.shoppy.utils.NetworkStatusTracker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkStatusTrackerModule {

    @Provides
    @Singleton
    fun provideNetworkStatusTracker(application: Application): NetworkStatusTracker {
        return NetworkStatusTracker(application.applicationContext)
    }
}