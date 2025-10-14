package com.dermochelys.utcclock.view.common

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds

private const val CONTENT_DISPLAY_TIME_S = 30

@HiltViewModel
class AutoNavBackViewModel @Inject constructor(
    private val coroutineScope: CoroutineScope
) : ViewModel() {
    private var navBackJob: Job? = null

    private val navigationActions = MutableSharedFlow<Int>(
        // Warning: enabling replay will cause issues with rotation
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    init {
        setupForAutoDismiss()
    }

    fun getNavigationActions() = navigationActions as Flow<Int>

    // Helpers

    override fun onCleared() {
        super.onCleared()
        clearNavBackJob()
        coroutineScope.cancel()
    }

    private fun setupForAutoDismiss() {
        if (navBackJob?.isActive == true) {
            return
        }

        navBackJob = coroutineScope.launch {
            delay(CONTENT_DISPLAY_TIME_S.seconds)
            navigationActions.emit(1)
        }
    }

    private fun clearNavBackJob() {
        navBackJob?.let {
            navBackJob = null
            it.cancel()
        }
    }
}
