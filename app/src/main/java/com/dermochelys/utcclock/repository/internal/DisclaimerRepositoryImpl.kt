package com.dermochelys.utcclock.repository.internal

import androidx.annotation.VisibleForTesting
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.widget.DisclaimerStateBroadcaster
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val SHARED_PREFS_DISCLAIMER_AGREED_VALUE_NAME = "agreed"

class DisclaimerRepositoryImpl(
    private val dataStore: DataStore<Preferences>,
    private val dispatcher: CoroutineDispatcher,
    private val disclaimerStateBroadcaster: DisclaimerStateBroadcaster,
): DisclaimerRepository {

    private val agreedKey: Preferences.Key<Boolean> =
        disclaimerAgreedKey()

    override fun shouldShowDisclaimer(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[agreedKey]?.let { agreed -> !agreed } ?: true
    }

    override suspend fun onDisclaimerAgreeClicked() {
        withContext(dispatcher) {
            dataStore.edit { it[agreedKey] = true }

            // Notify any/all listener(s) that disclaimer state has changed.
            // In particular, this is necessary for the widget as widgets are not able to
            // listen for datastore changes directly.
            disclaimerStateBroadcaster.notifyDisclaimerStateChanged()
        }
    }
}

@VisibleForTesting
fun disclaimerAgreedKey(): Preferences.Key<Boolean> =
    booleanPreferencesKey(SHARED_PREFS_DISCLAIMER_AGREED_VALUE_NAME)
