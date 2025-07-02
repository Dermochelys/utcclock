package com.dermochelys.utcclock.view.clock

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import androidx.annotation.OpenForTesting
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import com.dermochelys.utcclock.getRandomContentColor
import com.dermochelys.utcclock.repository.ZonedDateRepository
import com.dermochelys.utcclock.resources.getRandomMiddleSpringWeight
import com.dermochelys.utcclock.resources.getRandomTextSizeDate
import com.dermochelys.utcclock.resources.getRandomTextSizeTime
import com.dermochelys.utcclock.shared.R
import com.dermochelys.utcclock.toColor
import com.dermochelys.utcclock.view.OVERLAY_ROTATION_PERIOD_S
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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

    var dateText by mutableStateOf("...")
        private set

    var timeText by mutableStateOf("...")
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

    var textSizeDate by mutableFloatStateOf(0f)
        private set

    var textSizeTime by mutableFloatStateOf(0f)
        private set

    var middleSpringWeight by mutableFloatStateOf(0f)
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

    private lateinit var timeFormat: String

    private lateinit var dateFormat: String

    fun onViewCreated(
        resources: Resources,
        savedInstanceState: Bundle? = null,
    ) {
        timeFormat = resources.getString(R.string.time_format_pattern)
        dateFormat = resources.getString(R.string.date_format_pattern)

        loadFromSavedInstanceState(savedInstanceState)

        textSizeDate = resources.getRandomTextSizeDate()
        textSizeTime = resources.getRandomTextSizeTime()
        middleSpringWeight = resources.getRandomMiddleSpringWeight()

        overlayTweakJob = coroutineScope.launch { startRepeatingOverlayTweak() }
        timeJob = coroutineScope.launch { subscribeToTimeUpdates(resources) }
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

    fun onTimeUpdated() { coroutineScope.launch { zonedDateRepository.onTimeUpdated() } }

    fun getNavigationActions() = navigationActions as Flow<Int>

    // Helpers

    private fun loadFromSavedInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.getInt(SAVED_INSTANCE_STATE_CONTENT_COLOR)?.toColor()?.let { contentColor = it }
        savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER)?.let { textOrderDateFirst = it }
        savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_ALIGNMENT)?.let { fontLicenseButtonAlignToStart = it }
        savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_BUTTON_ROW_ALIGNMENT)?.let { buttonRowTop = it }
        savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_DATE_TEXT_ALIGNMENT)?.let { dateTextAlignToStart = it }
    }

    private suspend fun subscribeToTimeUpdates(resources: Resources) {
        zonedDateRepository.zonedDateFlow().collect { zonedDate ->
            this@ClockViewModel.timeLoaded = true
            updateDisplayText(zonedDate)
            tweakContent(resources)
        }
    }

    private suspend fun startRepeatingOverlayTweak() {
        while (true) {
            delay(OVERLAY_ROTATION_PERIOD_S.seconds)
            tweakOverlay()
        }
    }

    private fun tweakContent(resources: Resources) {
        textSizeDate = resources.getRandomTextSizeDate()
        textSizeTime = resources.getRandomTextSizeTime()
        middleSpringWeight = resources.getRandomMiddleSpringWeight()
        contentColor = getRandomContentColor()

        rotateDateText()
        rotateButtons()

        overlayPositionShift = !overlayPositionShift
    }

    private fun rotateDateText() {
        textOrderDateFirst = !textOrderDateFirst
        if (textOrderDateFirst) { dateTextAlignToStart = !dateTextAlignToStart }
    }

    private fun rotateButtons() {
        buttonRowTop = !buttonRowTop
        if (buttonRowTop) { fontLicenseButtonAlignToStart = !fontLicenseButtonAlignToStart }
    }

    private fun tweakOverlay() {
        overlayPositionShift = !overlayPositionShift
    }

    private fun updateLoadingIndicator() {
        showingLoading = !timeLoaded || !disclaimerStateLoaded
    }

    private fun updateDisplayText(zonedDate: Pair<Date, TimeZone>) {
        coroutineScope.launch {
            dateText = printCurrentDateFormatted(zonedDate)
            timeText = printCurrentTimeFormatted(zonedDate)
        }
    }

    private fun printCurrentTimeFormatted(zonedDate: Pair<Date, TimeZone>): String {
        return getFormattedDateTime(zonedDate, timeFormat)
    }

    private fun printCurrentDateFormatted(zonedDate: Pair<Date, TimeZone>): String {
        return getFormattedDateTime(zonedDate, dateFormat)
    }

    private fun getFormattedDateTime(zonedDate: Pair<Date, TimeZone>,
                                     formatString: String,
                                     ): String {
        val simpleDateFormat = SimpleDateFormat(formatString, Locale.US)
        simpleDateFormat.timeZone = zonedDate.second
        return simpleDateFormat.format(zonedDate.first)
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
