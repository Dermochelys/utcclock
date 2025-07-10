package com.dermochelys.utcclock

import android.annotation.SuppressLint
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
import com.dermochelys.utcclock.repository.FlagStore
import com.dermochelys.utcclock.resources.getRandomMiddleSpringWeight
import com.dermochelys.utcclock.resources.getRandomTextSizeDate
import com.dermochelys.utcclock.resources.getRandomTextSizeTime
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

private const val SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_ALIGNMENT = "fontLicenseButtonAlignment"

private const val SAVED_INSTANCE_STATE_FONT_LICENSE_CONTENT_VISIBLE = "fontLicenseContentVisible"

private const val SAVED_INSTANCE_STATE_CONTENT_COLOR = "contentColor"

private const val SAVED_INSTANCE_STATE_TEXT_ORDER = "textOrder"

private const val SAVED_INSTANCE_STATE_APP_LICENSE_CONTENT_VISIBLE = "appLicenseContentVisible"

private const val FONT_LICENSE_CONTENT_DISPLAY_SECONDS = 15

class ContentViewModel : ViewModel() {
    val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            this@ContentViewModel.handleOnBackPressed()
        }
    }

    private val utc: TimeZone by lazy { TimeZone.getTimeZone("UTC") }

    var dateText by mutableStateOf("Loading...")
        private set

    var timeText by mutableStateOf("Loading...")
        private set

    var showingFontLicense by mutableStateOf(false)
        private set

    var showingAppLicense by mutableStateOf(false)
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

    private var fontLicenseHideJob: Job? = null

    private var overlayTweakJob: Job? = null

    private lateinit var timeFormat: String

    private lateinit var dateFormat: String

    private lateinit var flagStore: FlagStore

    fun initialize(
        resources: Resources,
        savedInstanceState: Bundle?,
        flagStore: FlagStore,
    ) {
        timeFormat = resources.getString(com.dermochelys.utcclock.shared.R.string.time_format_pattern)
        dateFormat = resources.getString(com.dermochelys.utcclock.shared.R.string.date_format_pattern)
        this.flagStore = flagStore

        contentColor = savedInstanceState?.getInt(SAVED_INSTANCE_STATE_CONTENT_COLOR) ?: getRandomContentColor()
        textOrderDateFirst = savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER) ?: Random.nextBoolean()
        fontLicenseButtonAlignment = savedInstanceState?.getInt(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_ALIGNMENT)?.toAlignment() ?: getRandomAlignment()

        showingFontLicense = savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_FONT_LICENSE_CONTENT_VISIBLE)?.let { it ->
            if (it) { setupForFontLicenseContentHiding() }
            showingFontLicense = it
            it
        } ?: false

        showingAppLicense = savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_APP_LICENSE_CONTENT_VISIBLE) ?: !flagStore.isFlagSet()

        textSizeDate = resources.getRandomTextSizeDate()
        textSizeTime = resources.getRandomTextSizeTime()
        middleSpringWeight = resources.getRandomMiddleSpringWeight()
        overlayPositionShift = Random.nextBoolean()

        startRepeatingOverlayTweak()
        updateDisplayText()
    }

    fun onTimeChanged(resources: Resources) {
        tweakContent(resources)
        updateDisplayText()
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


    override fun onCleared() {
        super.onCleared()
        clearFontLicenseHideJob()
        clearOverlayTweakJob()
    }

    fun onDisclaimerAgreeClicked() {
        flagStore.setBoolean()
        showingAppLicense = false
    }

    fun onSaveInstanceState(outState: Bundle) {
        outState.putInt(SAVED_INSTANCE_STATE_CONTENT_COLOR, contentColor)
        outState.putBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER, textOrderDateFirst)
        outState.putInt(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_ALIGNMENT, fontLicenseButtonAlignment.toInt())
        outState.putBoolean(SAVED_INSTANCE_STATE_FONT_LICENSE_CONTENT_VISIBLE, showingFontLicense)
        outState.putBoolean(SAVED_INSTANCE_STATE_APP_LICENSE_CONTENT_VISIBLE, showingAppLicense)
    }

    // Helpers

    private fun setupForFontLicenseContentHiding() {
        onBackPressedCallback.isEnabled = true
        showingFontLicense = true

        fontLicenseHideJob = viewModelScope.launch {
            delay(FONT_LICENSE_CONTENT_DISPLAY_SECONDS.seconds)
            handleOnBackPressed()
        }
    }

    private fun startRepeatingOverlayTweak() {
        if (overlayTweakJob?.isActive == true) {
            return
        }

        overlayTweakJob = viewModelScope.launch {
            while (true) {
                delay(5.seconds)
                tweakOverlay()
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

    private fun updateDisplayText(date: Date = Date()) {
        dateText = printCurrentDateFormatted(date)
        timeText = printCurrentTimeFormatted(date)
    }

    private fun printCurrentTimeFormatted(date: Date): String {
        return getFormattedDateTime(date, timeFormat)
    }

    private fun printCurrentDateFormatted(date: Date): String {
        return getFormattedDateTime(date, dateFormat)
    }

    @SuppressLint("SimpleDateFormat")
    private fun getFormattedDateTime(date: Date, formatString: String): String {
        val simpleDateFormat = SimpleDateFormat(formatString)
        simpleDateFormat.timeZone = utc
        return simpleDateFormat.format(date)
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
}
