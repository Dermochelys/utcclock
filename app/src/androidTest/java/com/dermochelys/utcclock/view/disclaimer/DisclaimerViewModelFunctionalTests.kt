package com.dermochelys.utcclock.view.disclaimer

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
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DisclaimerViewModelFunctionalTests {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var disclaimerRepository: DisclaimerRepository

    @Inject
    lateinit var coroutineDispatcher: CoroutineDispatcher

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    lateinit var coroutineScope: CoroutineScope

    lateinit var underTest: DisclaimerViewModel

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
        underTest = DisclaimerViewModel(disclaimerRepository, coroutineScope)
    }

    @After
    fun tearDown() {
        clearDataStore()
        underTest.onCleared()
    }

    @Test
    fun when_disclaimerAccepted_persistsStateAndShowsClock() = runTest {
        var navigationResult = -1

        backgroundScope.launch(coroutineDispatcher) {
            underTest.getNavigationActions().collect { navigationResult = it }
        }

        var acceptedDisclaimer = false

        backgroundScope.launch(coroutineDispatcher) {
            dataStore.data.collect {
                it[disclaimerAgreedKey()]?.let { value ->
                    acceptedDisclaimer = value
                }
            }
        }

        TestCase.assertEquals(-1, navigationResult)

        underTest.onDisclaimerAgreeClicked()
        TestCase.assertEquals(R.id.clock_fragment, navigationResult)
        TestCase.assertTrue(acceptedDisclaimer)
    }

    // Helpers

    private fun clearDataStore() {
        runBlocking { dataStore.edit { it.clear() } }
    }
}
