package com.dermochelys.utcclock.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.repository.ZonedDateRepository
import com.dermochelys.utcclock.repository.internal.DisclaimerRepositoryImpl
import com.dermochelys.utcclock.repository.internal.ZonedDateRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {
    @Provides
    @Singleton
    fun provideTestDataStore(@ApplicationContext context: Context,
                             coroutineScope: CoroutineScope): DataStore<Preferences> {
        return object {
            val Context.testDataStore: DataStore<Preferences> by preferencesDataStore(
                name = "test_disclaimer",
                scope = coroutineScope,
            )

            fun testDataStore(context: Context): DataStore<Preferences> = context.testDataStore
        }.testDataStore(context)
    }

    @Provides
    @Singleton
    fun provideDisclaimerRepository(testDataStore: DataStore<Preferences>,
                                    coroutineDispatcher: CoroutineDispatcher): DisclaimerRepository {
        return DisclaimerRepositoryImpl(testDataStore, coroutineDispatcher)
    }

    @Provides
    @Singleton
    fun provideZonedDateRepository(coroutineDispatcher: CoroutineDispatcher): ZonedDateRepository {
        return ZonedDateRepositoryImpl(coroutineDispatcher)
    }
}
