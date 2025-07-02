package com.dermochelys.utcclock.di

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [CoroutineModule::class]
)
object TestCoroutineModule {
    @Provides
    @Singleton
    fun provideCoroutineScope(coroutineDispatcher: CoroutineDispatcher): CoroutineScope {
        return TestScope(SupervisorJob() + coroutineDispatcher)
    }
}
