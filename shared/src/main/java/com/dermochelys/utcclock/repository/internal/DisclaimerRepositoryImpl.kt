package com.dermochelys.utcclock.repository.internal

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataMigration
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.dermochelys.utcclock.repository.DisclaimerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val SHARED_PREFS_DISCLAIMER_FILE_NAME = "disclaimer"

private const val SHARED_PREFS_DISCLAIMER_AGREED_VALUE_NAME = "agreed"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = SHARED_PREFS_DISCLAIMER_FILE_NAME,
    produceMigrations = { applicationContext -> applicationContext.produceMigrations() },
)

class DisclaimerRepositoryImpl @VisibleForTesting constructor(private val dataStore: DataStore<Preferences>,
                                                              private val dispatcher: CoroutineDispatcher,
): DisclaimerRepository {
    constructor(applicationContext: Context, dispatcher: CoroutineDispatcher = Dispatchers.Default) :
            this(applicationContext.dataStore, dispatcher)

    private val agreedKey: Preferences.Key<Boolean> =
        disclaimerAgreedKey()

    override fun shouldShowDisclaimer(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[agreedKey]?.let { agreed -> !agreed } ?: true
    }

    override suspend fun onDisclaimerAgreeClicked() {
        withContext(dispatcher) { dataStore.edit { it[agreedKey] = true } }
    }
}

@VisibleForTesting
fun disclaimerAgreedKey(): Preferences.Key<Boolean> =
    booleanPreferencesKey(SHARED_PREFS_DISCLAIMER_AGREED_VALUE_NAME)

// Extension functions

private fun Context.produceMigrations(): List<DataMigration<Preferences>> {
    return listOf(
        SharedPreferencesMigration(
            context = this,
            sharedPreferencesName = SHARED_PREFS_DISCLAIMER_FILE_NAME,
        )
    )
}
