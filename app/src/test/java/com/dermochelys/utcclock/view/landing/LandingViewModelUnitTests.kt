package com.dermochelys.utcclock.view.landing

import com.dermochelys.utcclock.R
import com.dermochelys.utcclock.repository.DisclaimerRepository
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class LandingViewModelUnitTests {
    @MockK
    private lateinit var disclaimerRepository: DisclaimerRepository

    private lateinit var underTest: LandingViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true) // turn relaxUnitFun on for all mocks
    }

    @After
    fun teardown() {
        underTest.onCleared()
    }

    @Test
    fun when_disclaimerNotAgreed_showsDisclaimer() = runTest {
        runTest(true, R.id.disclaimer_fragment)
    }

    @Test
    fun when_disclaimerAgreed_showsClock() = runTest {
        runTest(false, R.id.clock_fragment)
    }

    // Helpers

    private suspend fun TestScope.runTest(
        initialValue: Boolean,
        expectedDestination: Int
    ) {
        val dispatcher = UnconfinedTestDispatcher()
        val coroutineScope = CoroutineScope(dispatcher)

        val mutableStateFlow = MutableSharedFlow<Boolean>(1).apply { emit(initialValue) }
        every { disclaimerRepository.shouldShowDisclaimer() }.returns(mutableStateFlow)

        underTest = LandingViewModel(disclaimerRepository, coroutineScope)

        var navigation = -1

        backgroundScope.launch(dispatcher) {
            underTest.getNavigationActions().collect { navigation = it }
        }

        assertEquals(expectedDestination, navigation)
    }
}
