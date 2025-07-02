package com.dermochelys.utcclock.landing

import androidx.annotation.OpenForTesting
import androidx.lifecycle.ViewModel
import com.dermochelys.utcclock.R
import com.dermochelys.utcclock.repository.DisclaimerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val disclaimerRepository: DisclaimerRepository,
    private val coroutineScope: CoroutineScope,
) : ViewModel() {

    private val navigationActions = MutableSharedFlow<Int>(
        // Warning: enabling replay will cause issues with rotation
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var disclaimerJob: Job? = null

    fun onViewCreated() {
        if (disclaimerJob == null) {
            disclaimerJob = coroutineScope.launch { checkDisclaimerAcceptance() }
        }
    }

    @OpenForTesting
    public override fun onCleared() {
        super.onCleared()
        clearDisclaimerJob()
        coroutineScope.cancel()
    }

    fun getNavigationActions() = navigationActions as Flow<Int>

    // Helpers

    private suspend fun checkDisclaimerAcceptance() {
        disclaimerRepository.shouldShowDisclaimer().collect { shouldShowDisclaimer ->
            if (shouldShowDisclaimer) {
                navigationActions.emit(R.id.disclaimer_fragment)
            } else {
                navigationActions.emit(R.id.clock_fragment)
            }
        }
    }

    private fun clearDisclaimerJob() {
        disclaimerJob?.let {
            disclaimerJob = null
            it.cancel()
        }
    }
}