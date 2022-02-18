package com.marcoassenza.shoppy.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.marcoassenza.shoppy.data.local.datastore.PreferencesDataStoreManager.Companion.DATASTORE_NAME
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(DATASTORE_NAME)

private val USERNAME = stringPreferencesKey("username")
private val NIGHT_MODE = intPreferencesKey("night_mode")

@Singleton
class PreferencesDataStoreManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun setNightMode(nightMode: Int) {
        context.dataStore.edit { preferences ->
            preferences[NIGHT_MODE] = nightMode
        }
    }

    val nightMode: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[NIGHT_MODE] ?: -1
    }

    suspend fun setUserName(userName: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME] = userName
        }
    }

    val userName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[USERNAME] ?: ""
    }

    companion object {
        const val DATASTORE_NAME = "preferences"
    }
}