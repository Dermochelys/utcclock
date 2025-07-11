package com.dermochelys.utcclock.repository

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val SHARED_PREFS_DISCLAIMER_FILE_NAME = "disclaimer"

private const val SHARED_PREFS_DISCLAIMER_AGREED_VALUE_NAME = "agreed"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SHARED_PREFS_DISCLAIMER_FILE_NAME,
    produceMigrations = { applicationContext -> applicationContext.produceMigrations() }
)

class DisclaimerRepository(private val applicationContext: Context) {
    private val agreedKey: Preferences.Key<Boolean> =
        booleanPreferencesKey(SHARED_PREFS_DISCLAIMER_AGREED_VALUE_NAME)

    fun shouldShowDisclaimer(): Flow<Boolean> = applicationContext.dataStore.data.map { preferences ->
        preferences[agreedKey]?.let { agreed -> !agreed } ?: true
    }

    suspend fun onDisclaimerAgreeClicked() {
        applicationContext.dataStore.edit { it[agreedKey] = true }
    }
}

// Extension functions

private fun Context.produceMigrations(): List<DataMigration<Preferences>> {
    return listOf(
        SharedPreferencesMigration(
            context = this,
            sharedPreferencesName = SHARED_PREFS_DISCLAIMER_FILE_NAME,
        )
    )
}
