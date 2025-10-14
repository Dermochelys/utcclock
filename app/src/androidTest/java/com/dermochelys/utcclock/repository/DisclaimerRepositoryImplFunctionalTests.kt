package com.dermochelys.utcclock.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dermochelys.utcclock.repository.internal.DisclaimerRepositoryImpl
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class DisclaimerRepositoryImplFunctionalTests {
    private lateinit var targetContext: Context

    @Before
    fun setup() {
        targetContext = InstrumentationRegistry.getInstrumentation().targetContext
    }

    @Test
    fun disclaimerBehavior_shouldOperateCorrectly() = runTest {
        val dispatcher = UnconfinedTestDispatcher()
        val dataStore = DataStoreProvider(targetContext, dispatcher).datastore()
        val underTest = DisclaimerRepositoryImpl(dataStore, dispatcher)

        val flow = underTest.shouldShowDisclaimer()
        assertTrue(flow.first())
        underTest.onDisclaimerAgreeClicked()
        assertFalse(flow.first())
        dataStore.edit { it.clear() }
    }
}

private class DataStoreProvider(private val context: Context,
                                dispatcher: CoroutineDispatcher,
) {
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = DisclaimerRepositoryImplFunctionalTests::class.simpleName!!,
        scope = CoroutineScope(dispatcher)
    )

    fun datastore(): DataStore<Preferences> = context.dataStore
}
