package com.dermochelys.utcclock

import com.dermochelys.utcclock.repository.internal.DateProvider
import com.dermochelys.utcclock.repository.internal.TimeZoneProvider
import com.dermochelys.utcclock.repository.internal.ZonedDateRepositoryImpl
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Date
import java.util.TimeZone

@OptIn(ExperimentalCoroutinesApi::class)
class ZonedDateRepositoryImplUnitTests {
    @MockK
    private lateinit var dateProvider: DateProvider

    @MockK
    private lateinit var timeZoneProvider: TimeZoneProvider

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true) // turn relaxUnitFun on for all mocks
    }

    @Test
    fun onTimeUpdated_updatesZonedDateFlowCorrectly() = runTest {
        val dispatcher = UnconfinedTestDispatcher()
        val underTest = ZonedDateRepositoryImpl(dispatcher, dateProvider, timeZoneProvider)

        val utc = TimeZone.getTimeZone("UTC")

        coEvery { timeZoneProvider.utc() }.returns(utc)
        coEvery { dateProvider.currentDate() }.returns(Date(0))
        underTest.onTimeUpdated()
        coEvery { dateProvider.currentDate() }.returns(Date(100))
        var pair = underTest.zonedDateFlow().first()
        assertEquals(100, pair.first.time)
        assertEquals(utc, pair.second)

        coEvery { dateProvider.currentDate() }.returns(Date(200))
        underTest.onTimeUpdated()
        pair = underTest.zonedDateFlow().first()
        assertEquals(200, pair.first.time)
    }
}