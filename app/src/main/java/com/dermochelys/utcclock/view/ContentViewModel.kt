package com.dermochelys.utcclock

import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorInt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.repository.ZonedDateRepository
import com.dermochelys.utcclock.resources.getRandomMiddleSpringWeight
import com.dermochelys.utcclock.resources.getRandomTextSizeDate
import com.dermochelys.utcclock.resources.getRandomTextSizeTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private const val FONT_LICENSE_CONTENT_DISPLAY_SECONDS = 15

/**
 * NOTE: Only supports US locale
 *
 * @see Locale.US
 * */
class ContentViewModel : ViewModel() {
    val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            this@ContentViewModel.handleOnBackPressed()
        }
    }

    var dateText by mutableStateOf("...")
        private set

    var timeText by mutableStateOf("...")
        private set

    var showingFontLicense by mutableStateOf(false)
        private set

    var showingDisclaimer by mutableStateOf(false)
        private set

    var showingLoading by mutableStateOf(true)
        private set

    var fontLicenseButtonAlignment: Alignment by mutableStateOf(Alignment.Center)
        private set

    @delegate:ColorInt
    var contentColor by mutableStateOf(Color.WHITE)
        private set

    var textOrderDateFirst by mutableStateOf(true)
        private set

    var overlayPositionShift by mutableStateOf(false)
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

    private var fontLicenseHideJob: Job? = null

    private var overlayTweakJob: Job? = null

    private var disclaimerJob: Job? = null

    private var timeJob: Job? = null

    private lateinit var timeFormat: String

    private lateinit var dateFormat: String

    private lateinit var disclaimerRepository: DisclaimerRepository

    private lateinit var zonedDateRepository: ZonedDateRepository

    fun initialize(
        resources: Resources,
        savedInstanceState: Bundle?,
        disclaimerRepository: DisclaimerRepository,
        zonedDateRepository: ZonedDateRepository,
    ) {
        timeFormat = resources.getString(com.dermochelys.utcclock.shared.R.string.time_format_pattern)
        dateFormat = resources.getString(com.dermochelys.utcclock.shared.R.string.date_format_pattern)
        this.disclaimerRepository = disclaimerRepository
        this.zonedDateRepository = zonedDateRepository

        loadFromSavedInstanceState(savedInstanceState)

        textSizeDate = resources.getRandomTextSizeDate()
        textSizeTime = resources.getRandomTextSizeTime()
        middleSpringWeight = resources.getRandomMiddleSpringWeight()
        overlayPositionShift = Random.nextBoolean()

        checkDisclaimerAcceptance()
        startRepeatingOverlayTweak()
        subscribeToTimeUpdates(resources)
    }

    private fun subscribeToTimeUpdates(resources: Resources) {
        timeJob = viewModelScope.launch(Dispatchers.Default) {
            zonedDateRepository.zonedDateFlow.collect { zonedDate ->
                this@ContentViewModel.timeLoaded = true
                updateDisplayText(zonedDate)
                tweakContent(resources)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        clearFontLicenseHideJob()
        clearOverlayTweakJob()
        clearDisclaimerJob()
        clearTimeJob()
    }

    /**
     * @return true if state changed and the license text should now be shown
     * */
    fun onFontLicenseButtonClicked() {
        if (!showingFontLicense) {
            setupForFontLicenseContentHiding()
        }
    }

    fun handleOnBackPressed() {
        if (!showingFontLicense) {
            return
        }

        onBackPressedCallback.isEnabled = false
        showingFontLicense = false

        clearFontLicenseHideJob()
    }

    fun onDisclaimerAgreeClicked() {
        viewModelScope.launch(Dispatchers.Default) { disclaimerRepository.onDisclaimerAgreeClicked() }
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SAVED_INSTANCE_STATE_CONTENT_COLOR, contentColor)
        outState.putBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER, textOrderDateFirst)
        outState.putInt(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_ALIGNMENT, fontLicenseButtonAlignment.toInt())
        outState.putBoolean(SAVED_INSTANCE_STATE_FONT_LICENSE_CONTENT_VISIBLE, showingFontLicense)
        outState.putBoolean(SAVED_INSTANCE_STATE_APP_LICENSE_CONTENT_VISIBLE, showingDisclaimer)
        outState.putBoolean(SAVED_INSTANCE_STATE_LOADING_VISIBLE, showingLoading)
    }

    // Helpers

    private fun loadFromSavedInstanceState(savedInstanceState: Bundle?) {
        contentColor = savedInstanceState
            ?.getInt(SAVED_INSTANCE_STATE_CONTENT_COLOR) ?: getRandomContentColor()

        textOrderDateFirst = savedInstanceState
            ?.getBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER) ?: Random.nextBoolean()

        fontLicenseButtonAlignment = savedInstanceState
            ?.getInt(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_ALIGNMENT)?.toAlignment() ?: getRandomAlignment()

        showingFontLicense =
            savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_FONT_LICENSE_CONTENT_VISIBLE)
                ?.let { it ->
                    if (it) { setupForFontLicenseContentHiding() }
                    showingFontLicense = it
                    it
                } ?: false

        showingDisclaimer =
            savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_APP_LICENSE_CONTENT_VISIBLE) ?: false

        showingLoading =
            savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_LOADING_VISIBLE) ?: true
    }

    private fun setupForFontLicenseContentHiding() {
        if (fontLicenseHideJob?.isActive == true) {
            return
        }

        onBackPressedCallback.isEnabled = true
        showingFontLicense = true

        fontLicenseHideJob = viewModelScope.launch(Dispatchers.Default) {
            delay(FONT_LICENSE_CONTENT_DISPLAY_SECONDS.seconds)
            handleOnBackPressed()
        }
    }

    private fun startRepeatingOverlayTweak() {
        if (overlayTweakJob?.isActive == true) {
            return
        }

        overlayTweakJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(5.seconds)
                tweakOverlay()
            }
        }
    }

    private fun checkDisclaimerAcceptance() {
        if (disclaimerJob?.isActive == true) {
            return
        }

        disclaimerJob = viewModelScope.launch(Dispatchers.Default) {
            disclaimerRepository.shouldShowDisclaimer().collect { shouldShowDisclaimer ->
                disclaimerStateLoaded = true
                showingDisclaimer = shouldShowDisclaimer
            }
        }
    }

    private fun tweakContent(resources: Resources) {
        textOrderDateFirst = !textOrderDateFirst
        textSizeDate = resources.getRandomTextSizeDate()
        textSizeTime = resources.getRandomTextSizeTime()
        middleSpringWeight = resources.getRandomMiddleSpringWeight()
        contentColor = getRandomContentColor()
        fontLicenseButtonAlignment = getRandomAlignment()
    }

    private fun tweakOverlay() {
        overlayPositionShift = !overlayPositionShift
    }

    private fun updateLoadingIndicator() {
        showingLoading = !timeLoaded || !disclaimerStateLoaded
    }

    private suspend fun updateDisplayText(zonedDate: Pair<Date, TimeZone>) {
        dateText = printCurrentDateFormatted(zonedDate)
        timeText = printCurrentTimeFormatted(zonedDate)
    }

    private suspend fun printCurrentTimeFormatted(zonedDate: Pair<Date, TimeZone>): String {
        return getFormattedDateTime(zonedDate, timeFormat)
    }

    private suspend fun printCurrentDateFormatted(zonedDate: Pair<Date, TimeZone>): String {
        return getFormattedDateTime(zonedDate, dateFormat)
    }


    private suspend fun getFormattedDateTime(zonedDate: Pair<Date, TimeZone>,
                                             formatString: String,
                                             ): String {
        return withContext(Dispatchers.Default) {
            val simpleDateFormat = SimpleDateFormat(formatString, Locale.US)
            simpleDateFormat.timeZone = zonedDate.second
            simpleDateFormat.format(zonedDate.first)
        }
    }

    private fun clearFontLicenseHideJob() {
        fontLicenseHideJob?.let {
            fontLicenseHideJob = null
            it.cancel()
        }
    }

    private fun clearOverlayTweakJob() {
        overlayTweakJob?.let {
            overlayTweakJob = null
            it.cancel()
        }
    }

    private fun clearDisclaimerJob() {
        disclaimerJob?.let {
            disclaimerJob = null
            it.cancel()
        }
    }

    private fun clearTimeJob() {
        timeJob?.let {
            timeJob = null
            it.cancel()
        }
    }
}
