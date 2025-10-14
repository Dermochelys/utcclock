package com.dermochelys.utcclock.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.repository.ZonedDateRepository
import com.dermochelys.utcclock.repository.internal.DisclaimerRepositoryImpl
import com.dermochelys.utcclock.repository.internal.ZonedDateRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideZonedDateRepository(coroutineDispatcher: CoroutineDispatcher): ZonedDateRepository {
        return ZonedDateRepositoryImpl(coroutineDispatcher)
    }

    @Provides
    @Singleton
    fun provideDisclaimerRepository(dataStore: DataStore<Preferences>,
                                    coroutineDispatcher: CoroutineDispatcher): DisclaimerRepository {
        return DisclaimerRepositoryImpl(dataStore, coroutineDispatcher)
    }
}
