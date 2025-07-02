package com.dermochelys.utcclock.disclaimer

import com.dermochelys.utcclock.R
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.view.disclaimer.DisclaimerViewModel
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class DisclaimerViewModelUnitTests {
    @MockK
    private lateinit var disclaimerRepo: DisclaimerRepository

    private lateinit var underTest: DisclaimerViewModel

    @Before
    fun setup() {
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true) // turn relaxUnitFun on for all mocks
    }

    @After
    fun teardown() {
        underTest.onCleared()
    }

    @Test
    fun when_disclaimerAgreed_hidesDisclaimer() = runTest {
        val dispatcher = UnconfinedTestDispatcher()
        var navigationResult = -1

        val shouldShowDisclaimerFlow = MutableStateFlow(true)
        every { disclaimerRepo.shouldShowDisclaimer() }.returns(shouldShowDisclaimerFlow)

        underTest = DisclaimerViewModel(disclaimerRepo, CoroutineScope(dispatcher))

        backgroundScope.launch(dispatcher) {
            underTest.getNavigationActions().collect { navigationResult = it }
        }

        coEvery { disclaimerRepo.onDisclaimerAgreeClicked() }.coAnswers {
            shouldShowDisclaimerFlow.emit(false)
        }

        assertEquals(-1, navigationResult)
        underTest.onViewLaunched()
        assertEquals(-1, navigationResult)

        underTest.onDisclaimerAgreeClicked()
        coVerify { disclaimerRepo.onDisclaimerAgreeClicked() }
        assertEquals(R.id.clock_fragment, navigationResult)
    }
}
