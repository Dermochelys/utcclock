package com.dermochelys.utcclock.di

import dagger.Module
import dagger.Provides
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.TestScope

@OptIn(ExperimentalCoroutinesApi::class)
@Module
@TestInstallIn(
    components = [ViewModelComponent::class],
    replaces = [CoroutineModule::class]
)
object TestViewModelCoroutineModule {
    @Provides
    @ViewModelScoped
    fun provideCoroutineScope(coroutineDispatcher: CoroutineDispatcher): CoroutineScope {
        return TestScope(SupervisorJob() + coroutineDispatcher)
    }
}
