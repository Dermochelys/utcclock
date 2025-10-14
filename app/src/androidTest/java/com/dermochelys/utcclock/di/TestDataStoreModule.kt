package com.dermochelys.utcclock.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DataStoreModule::class]
)
object TestDataStoreModule {
    // Since components are recreated during tests, we need to manage the singleton on our own
    private var dataStoreProvider: DataStoreProvider? = null

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context,
                         coroutineDispatcher: CoroutineDispatcher): DataStore<Preferences> {
        val provider = dataStoreProvider ?: let {
            DataStoreProvider(context, coroutineDispatcher).also { dataStoreProvider = it }
        }

        return provider.datastore()
    }
}

class DataStoreProvider(private val context: Context,
                        coroutineDispatcher: CoroutineDispatcher) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "disclaimer", scope = TestScope(
        SupervisorJob() + coroutineDispatcher))

    fun datastore() = context.dataStore
}
