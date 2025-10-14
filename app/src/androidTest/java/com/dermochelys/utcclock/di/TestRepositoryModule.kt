package com.dermochelys.utcclock.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.repository.ZonedDateRepository
import com.dermochelys.utcclock.repository.internal.DisclaimerRepositoryImpl
import com.dermochelys.utcclock.repository.internal.ZonedDateRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
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
    fun provideDisclaimerRepository(dataStore: DataStore<Preferences>,
                                    coroutineDispatcher: CoroutineDispatcher): DisclaimerRepository {
        return DisclaimerRepositoryImpl(dataStore, coroutineDispatcher)
    }

    @Provides
    @Singleton
    fun provideZonedDateRepository(coroutineDispatcher: CoroutineDispatcher): ZonedDateRepository {
        return ZonedDateRepositoryImpl(coroutineDispatcher)
    }
}
