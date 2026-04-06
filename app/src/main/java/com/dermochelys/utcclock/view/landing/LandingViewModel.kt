package com.dermochelys.utcclock.view.landing

import androidx.annotation.OpenForTesting
import androidx.lifecycle.ViewModel
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.view.NavigationAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val disclaimerRepository: DisclaimerRepository,
    private val coroutineScope: CoroutineScope,
) : ViewModel() {

    private val navigationActions = MutableStateFlow(NavigationAction.NONE)

    private val disclaimerJob = coroutineScope.launch { checkDisclaimerAcceptance() }

    @OpenForTesting
    public override fun onCleared() {
        super.onCleared()
        clearDisclaimerJob()
        coroutineScope.cancel()
    }

    fun getNavigationActions() = navigationActions as Flow<NavigationAction>

    // Helpers

    private suspend fun checkDisclaimerAcceptance() {
        disclaimerRepository.shouldShowDisclaimer().collect { shouldShowDisclaimer ->
            if (shouldShowDisclaimer) {
                navigationActions.emit(NavigationAction.SHOW_DISCLAIMER)
            } else {
                navigationActions.emit(NavigationAction.SHOW_CLOCK)
            }
        }
    }

    private fun clearDisclaimerJob() {
        disclaimerJob.cancel()
    }
}
