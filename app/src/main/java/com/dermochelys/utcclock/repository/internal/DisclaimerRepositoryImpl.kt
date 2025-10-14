package com.dermochelys.utcclock.repository.internal

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.dermochelys.utcclock.repository.DisclaimerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val SHARED_PREFS_DISCLAIMER_AGREED_VALUE_NAME = "agreed"

class DisclaimerRepositoryImpl(private val dataStore: DataStore<Preferences>,
                               private val dispatcher: CoroutineDispatcher,
): DisclaimerRepository {

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
