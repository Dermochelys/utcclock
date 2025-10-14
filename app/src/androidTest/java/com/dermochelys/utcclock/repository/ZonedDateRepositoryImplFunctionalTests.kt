package com.dermochelys.utcclock.repository

import android.content.Context
import android.content.res.Resources
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.dermochelys.utcclock.repository.internal.ZonedDateRepositoryImpl
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class ZonedDateRepositoryImplFunctionalTests {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var coroutineDispatcher: CoroutineDispatcher

    lateinit var underTest: ZonedDateRepository

    private lateinit var targetContext: Context

    private lateinit var resources: Resources

    @Before
    fun setup() {
        targetContext = InstrumentationRegistry.getInstrumentation().targetContext
        resources = targetContext.resources
        hiltRule.inject()
        underTest = ZonedDateRepositoryImpl(dispatcher = coroutineDispatcher)
    }

    @Test
    fun flow_and_onTimeUpdated_operateCorrectly() = runTest {
        val firstTime = underTest.zonedDateFlow().first().first.time
        assertTrue("invalid first time", firstTime != 0L)

        delay(100.milliseconds)

        val secondTime = underTest.zonedDateFlow().first().first.time
        assertEquals("times were not equal", firstTime, secondTime)

        underTest.onTimeUpdated()
        delay(100.milliseconds)

        val thirdTime = underTest.zonedDateFlow().first().first.time
        assertTrue("times were equal",thirdTime != firstTime)
        assertTrue("times were out of order",thirdTime > firstTime)
    }
}
