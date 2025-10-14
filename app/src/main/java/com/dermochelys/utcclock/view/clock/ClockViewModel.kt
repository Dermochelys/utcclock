package com.dermochelys.utcclock.view.clock

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.annotation.OpenForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.dermochelys.utcclock.repository.ZonedDateRepository
import com.dermochelys.utcclock.view.OVERLAY_ROTATION_PERIOD_S
import com.dermochelys.utcclock.view.common.getRandomContentColor
import com.dermochelys.utcclock.view.common.toColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private const val SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_ALIGNMENT = "fontLicenseButtonAlignment"
private const val SAVED_INSTANCE_STATE_BUTTON_ROW_ALIGNMENT = "buttonRowAlignment"
private const val SAVED_INSTANCE_STATE_DATE_TEXT_ALIGNMENT = "dateTextAlignment"
private const val SAVED_INSTANCE_STATE_CONTENT_COLOR = "contentColor"
private const val SAVED_INSTANCE_STATE_TEXT_ORDER = "textOrder"

/**
 * NOTE: Only supports US locale
 *
 * @see Locale.US
 *
 * Suppress due to warning on [contentColor]
 * */
@SuppressLint("AutoboxingStateCreation")
@HiltViewModel
class ClockViewModel @Inject constructor(
    private val zonedDateRepository: ZonedDateRepository,
    private val coroutineScope: CoroutineScope
) : ViewModel() {
    private val navigationActions = MutableSharedFlow<Int>(
        // Warning: enabling replay will cause issues with rotation
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    var zonedDateTime by mutableStateOf(Pair<Date, TimeZone>(Date(), TimeZone.getDefault()))
        private set

    var showingLoading by mutableStateOf(true)
        private set

    var fontLicenseButtonAlignToStart: Boolean by mutableStateOf(Random.nextBoolean())
        private set

    var dateTextAlignToStart: Boolean by mutableStateOf(Random.nextBoolean())
        private set

    var buttonRowTop: Boolean by mutableStateOf(Random.nextBoolean())
        private set

    var contentColor by mutableStateOf(getRandomContentColor())
        private set

    var textOrderDateFirst by mutableStateOf(Random.nextBoolean())
        private set

    var overlayPositionShift by mutableStateOf(Random.nextBoolean())
        private set

    //  Start loaded data

    private var timeLoaded: Boolean = false
        set(value) {
            val updated = field != value
            field = value
            if (updated) { updateLoadingIndicator() }
        }

    private var disclaimerStateLoaded: Boolean = false
        set(value) {
            val updated = field != value
            field = value
            if (updated) { updateLoadingIndicator() }
        }

    // End loaded data

    private var overlayTweakJob: Job? = null

    private var timeJob: Job? = null

    init {
        overlayTweakJob = coroutineScope.launch { startRepeatingOverlayTweak() }
        timeJob = coroutineScope.launch { subscribeToTimeUpdates() }
    }

    @OpenForTesting
    public override fun onCleared() {
        super.onCleared()
        clearOverlayTweakJob()
        clearTimeJob()
        coroutineScope.cancel()
    }

    fun onFontLicenseButtonClicked() {
        navigateTo(com.dermochelys.utcclock.R.id.font_license_dialog)
    }

    fun onDonationButtonClicked() {
        navigateTo(com.dermochelys.utcclock.R.id.donation_dialog)
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SAVED_INSTANCE_STATE_CONTENT_COLOR, contentColor.toArgb())
        outState.putBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER, textOrderDateFirst)
        outState.putBoolean(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_ALIGNMENT, fontLicenseButtonAlignToStart)
        outState.putBoolean(SAVED_INSTANCE_STATE_BUTTON_ROW_ALIGNMENT, buttonRowTop)
        outState.putBoolean(SAVED_INSTANCE_STATE_DATE_TEXT_ALIGNMENT, dateTextAlignToStart)
    }

    fun onLoadInstanceState(inState: Bundle) {
        contentColor = inState.getInt(SAVED_INSTANCE_STATE_CONTENT_COLOR).toColor()
        textOrderDateFirst = inState.getBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER)
        fontLicenseButtonAlignToStart = inState.getBoolean(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_ALIGNMENT)
        buttonRowTop = inState.getBoolean(SAVED_INSTANCE_STATE_BUTTON_ROW_ALIGNMENT)
        dateTextAlignToStart = inState.getBoolean(SAVED_INSTANCE_STATE_DATE_TEXT_ALIGNMENT)
    }

    fun onTimeUpdated() { coroutineScope.launch { zonedDateRepository.onTimeUpdated() } }

    fun getNavigationActions() = navigationActions as Flow<Int>

    // Helpers

    private suspend fun subscribeToTimeUpdates() {
        zonedDateRepository.zonedDateFlow().collect { zonedDate ->
            this@ClockViewModel.timeLoaded = true
            zonedDateTime = zonedDate
            tweakContent()
        }
    }

    private suspend fun startRepeatingOverlayTweak() {
        while (true) {
            delay(OVERLAY_ROTATION_PERIOD_S.seconds)
            tweakOverlay()
        }
    }

    private fun tweakContent() {
        contentColor = getRandomContentColor()

        textOrderDateFirst = !textOrderDateFirst
        if (textOrderDateFirst) { dateTextAlignToStart = !dateTextAlignToStart }

        buttonRowTop = !buttonRowTop
        if (buttonRowTop) { fontLicenseButtonAlignToStart = !fontLicenseButtonAlignToStart }

        overlayPositionShift = !overlayPositionShift
    }

    private fun tweakOverlay() {
        overlayPositionShift = !overlayPositionShift
    }

    private fun updateLoadingIndicator() {
        showingLoading = !timeLoaded || !disclaimerStateLoaded
    }

    private fun clearOverlayTweakJob() {
        overlayTweakJob?.let {
            overlayTweakJob = null
            it.cancel()
        }
    }

    private fun clearTimeJob() {
        timeJob?.let {
            timeJob = null
            it.cancel()
        }
    }

    private fun navigateTo(navigationDestination: Int) {
        coroutineScope.launch {
            navigationActions.emit(navigationDestination)
        }
    }
}
