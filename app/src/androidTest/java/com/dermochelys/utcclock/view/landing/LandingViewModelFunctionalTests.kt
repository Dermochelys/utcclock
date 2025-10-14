package com.dermochelys.utcclock.view.landing

import android.content.Context
import android.content.res.Resources
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dermochelys.utcclock.R
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.repository.internal.disclaimerAgreedKey
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class LandingViewModelFunctionalTests {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var disclaimerRepository: DisclaimerRepository

    @Inject
    lateinit var coroutineDispatcher: CoroutineDispatcher

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    lateinit var coroutineScope: CoroutineScope

    lateinit var underTest: LandingViewModel

    private lateinit var targetContext: Context

    private lateinit var resources: Resources

    @Before
    fun setup() {
        targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        resources = targetContext.resources
        hiltRule.inject()

        // In case there is leftover state on the device from a previous test run
        clearDataStore()

        coroutineScope = CoroutineScope(SupervisorJob() + coroutineDispatcher)
        underTest = LandingViewModel(disclaimerRepository, coroutineScope)
    }

    @After
    fun tearDown() {
        clearDataStore()
        underTest.onCleared()
    }

    @Test
    fun disclaimerNotAcceptedYetShowsDisclaimer() = runTest {
        var navigationResult = -1

        backgroundScope.launch(coroutineDispatcher) {
            underTest.getNavigationActions().collect { navigationResult = it }
        }

        TestCase.assertEquals(R.id.disclaimer_fragment, navigationResult)
    }

    @Test
    fun disclaimerAlreadyAcceptedShowsClock() = runTest {
        var navigationResult = -1

        withContext(coroutineDispatcher) { dataStore.edit { it[disclaimerAgreedKey()] = true } }

        backgroundScope.launch(coroutineDispatcher) {
            underTest.getNavigationActions().collect { navigationResult = it }
        }

        TestCase.assertEquals(R.id.clock_fragment, navigationResult)
    }

    // Helpers

    private fun clearDataStore() {
        runBlocking { dataStore.edit { it.clear() } }
    }
}
