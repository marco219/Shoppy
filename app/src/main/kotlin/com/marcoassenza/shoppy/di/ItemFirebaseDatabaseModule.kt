package com.marcoassenza.shoppy.di

import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.marcoassenza.shoppy.utils.Constant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ItemFirebaseDatabaseModule {

    @Provides
    @Singleton
    fun provideItemFirebaseDatabaseReference(): DatabaseReference {
        return FirebaseDatabase
            .getInstance(Constant.FIREBASE_DB_URL)
            .reference
            .child(Constant.CHILD_DB_NAME)
    }
}