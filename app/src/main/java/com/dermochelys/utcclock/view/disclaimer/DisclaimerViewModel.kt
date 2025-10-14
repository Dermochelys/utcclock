package com.dermochelys.utcclock.view.disclaimer

import androidx.annotation.OpenForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.dermochelys.utcclock.R
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.view.OVERLAY_ROTATION_PERIOD_S
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

@HiltViewModel
class DisclaimerViewModel @Inject constructor(
    private val disclaimerRepository: DisclaimerRepository,
    private val coroutineScope: CoroutineScope,
) : ViewModel() {

    private val navigationActions = MutableStateFlow(-1)
//    <Int>(
//        // Warning: enabling replay will cause issues with rotation
//        extraBufferCapacity = 1,
//        onBufferOverflow = BufferOverflow.DROP_OLDEST,
//    )

    private val shouldShowJob: Job = coroutineScope.launch {
        disclaimerRepository.shouldShowDisclaimer().collect { shouldShowDisclaimer ->
            if (!shouldShowDisclaimer) {
                navigationActions.emit(R.id.clock_fragment)
            }
        }
    }

    private val overlayTweakJob = coroutineScope.launch { startRepeatingOverlayTweak() }

    private var disclaimerAgreedJob : Job? = null

    var overlayPositionShift by mutableStateOf(false)
        private set

    fun getNavigationActions() = navigationActions as Flow<Int>

    fun onDisclaimerAgreeClicked() {
        // Prevent theoretically possible re-click while background task is processing
        if (disclaimerAgreedJob != null) return

        disclaimerAgreedJob = coroutineScope.launch { disclaimerRepository.onDisclaimerAgreeClicked() }
    }

    @OpenForTesting
    public override fun onCleared() {
        super.onCleared()
        clearJobs()
        coroutineScope.cancel()
    }

    // Helpers

    private fun clearJobs() {
        shouldShowJob.cancel()
        overlayTweakJob.cancel()
        disclaimerAgreedJob?.cancel()
    }

    private suspend fun startRepeatingOverlayTweak() {
        while (true) {
            delay(OVERLAY_ROTATION_PERIOD_S.seconds)
            tweakOverlay()
        }
    }

    private fun tweakOverlay() {
        overlayPositionShift = !overlayPositionShift
    }
}
