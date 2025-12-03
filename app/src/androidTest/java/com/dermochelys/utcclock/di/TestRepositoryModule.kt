package com.dermochelys.utcclock.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.repository.PermissionsRepository
import com.dermochelys.utcclock.repository.ZonedDateRepository
import com.dermochelys.utcclock.repository.internal.DisclaimerRepositoryImpl
import com.dermochelys.utcclock.repository.internal.PermissionsRepositoryImpl
import com.dermochelys.utcclock.repository.internal.ZonedDateRepositoryImpl
import com.dermochelys.utcclock.widget.DisclaimerStateBroadcaster
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
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
                                    coroutineDispatcher: CoroutineDispatcher,
                                    disclaimerStateBroadcaster: DisclaimerStateBroadcaster): DisclaimerRepository {
        return DisclaimerRepositoryImpl(dataStore, coroutineDispatcher, disclaimerStateBroadcaster)
    }

    @Provides
    @Singleton
    fun provideZonedDateRepository(coroutineDispatcher: CoroutineDispatcher): ZonedDateRepository {
        return ZonedDateRepositoryImpl(coroutineDispatcher)
    }

    @Provides
    @Singleton
    fun providePermissionsRepository(@ApplicationContext context: Context): PermissionsRepository {
        return PermissionsRepositoryImpl(context)
    }
}
