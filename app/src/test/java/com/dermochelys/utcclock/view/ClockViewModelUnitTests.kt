package com.dermochelys.utcclock.view

import android.content.res.Resources
import com.dermochelys.utcclock.R
import com.dermochelys.utcclock.repository.internal.ZonedDateRepositoryImpl
import com.dermochelys.utcclock.view.clock.ClockViewModel
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ClockViewModelUnitTests {
    @MockK
    private lateinit var resources: Resources

    private lateinit var underTest: ClockViewModel

    @Before
    fun mockSetup() {
        // turn relaxUnitFun on for all mocks
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true)
    }

    @After
    fun teardown() {
        underTest.onCleared()
    }

    @Test
    fun fontLicenseOverlay_and_fontLicenseButton_workCorrectly() = runTest {
        val dispatcher = setupUnderTest()
        var navigationResult = -1

        backgroundScope.launch(dispatcher) {
            underTest.getNavigationActions().collect { navigationResult = it }
        }

        underTest.onFontLicenseButtonClicked()
        assertEquals(R.id.font_license_dialog, navigationResult)
    }

    @Test
    fun dateRotation_worksCorrectly() = runTest {
        setupUnderTest()
        verifyRotation(::currentDatePosition)
    }

    @Test
    fun fontLicenseButtonRotation_worksCorrectly() = runTest {
        setupUnderTest()
        verifyRotation(::currentFontLicenseButtonPosition)
    }

    @Test
    fun donationButtonRotation_worksCorrectly() = runTest {
        setupUnderTest()
        verifyRotation(::currentDonationButtonPosition)
    }

    // Helpers

    private fun setupUnderTest(): TestDispatcher {
        val dispatcher = UnconfinedTestDispatcher()
        val coroutineScope = CoroutineScope(dispatcher)

        val zonedDateRepo = ZonedDateRepositoryImpl(dispatcher = dispatcher)
        underTest = ClockViewModel(zonedDateRepo, coroutineScope)
        underTest.onViewCreated(resources)
        return dispatcher
    }

    private fun verifyRotation(currentPositionLambda: () -> Position) {
        val originalPosition = currentPositionLambda.invoke()
        advanceTimeAndVerifyNextPosition(currentPositionLambda)
        advanceTimeAndVerifyNextPosition(currentPositionLambda)
        advanceTimeAndVerifyNextPosition(currentPositionLambda)
        advanceTimeAndVerifyNextPosition(currentPositionLambda)
        assertEquals(originalPosition, currentPositionLambda.invoke(), "Did not return to original position")
    }

    private fun advanceTimeAndVerifyNextPosition(currentPositionLambda: () -> Position) {
        val previousPosition = currentPositionLambda.invoke()
        underTest.onTimeUpdated()
        val currentPosition = currentPositionLambda.invoke()
        assertNotEquals(previousPosition, currentPosition, "Position did not change")
        assertEquals(previousPosition.next(), currentPosition, "New position was incorrect")
    }

    private fun currentDatePosition(): Position {
        if (underTest.textOrderDateFirst && underTest.dateTextAlignToStart) return Position.UPPER_LEFT
        if (underTest.textOrderDateFirst && !underTest.dateTextAlignToStart) return Position.UPPER_RIGHT
        if (!underTest.textOrderDateFirst && underTest.dateTextAlignToStart) return Position.LOWER_LEFT
        return Position.LOWER_RIGHT
    }

    private fun currentFontLicenseButtonPosition(): Position {
        if (underTest.fontLicenseButtonAlignToStart && underTest.buttonRowTop) return Position.UPPER_LEFT
        if (underTest.buttonRowTop && !underTest.fontLicenseButtonAlignToStart) return Position.UPPER_RIGHT
        if (!underTest.buttonRowTop && underTest.fontLicenseButtonAlignToStart) return Position.LOWER_LEFT
        return Position.LOWER_RIGHT
    }

    private fun currentDonationButtonPosition(): Position {
        if (!underTest.fontLicenseButtonAlignToStart && underTest.buttonRowTop) return Position.UPPER_LEFT
        if (underTest.buttonRowTop && underTest.fontLicenseButtonAlignToStart) return Position.UPPER_RIGHT
        if (!underTest.buttonRowTop && !underTest.fontLicenseButtonAlignToStart) return Position.LOWER_LEFT
        return Position.LOWER_RIGHT
    }

    enum class Position {
        UPPER_LEFT, LOWER_LEFT, UPPER_RIGHT, LOWER_RIGHT;

        fun next(): Position {
            if (this == UPPER_LEFT) return LOWER_LEFT
            if (this == LOWER_LEFT) return UPPER_RIGHT
            if (this == UPPER_RIGHT) return LOWER_RIGHT

            // (this == LOWER_RIGHT)
            return UPPER_LEFT
        }
    }
}
