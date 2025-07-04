package com.dermochelys.utcclock

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.ColorInt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dermochelys.utcclock.resources.getRandomMiddleSpringWeight
import com.dermochelys.utcclock.resources.getRandomTextSizeDate
import com.dermochelys.utcclock.resources.getRandomTextSizeTime
import kotlinx.coroutines.cancelChildren
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

private const val SHARED_PREFS_DISCLAIMER_FILE_NAME = "disclaimer"

private const val SHARED_PREFS_DISCLAIMER_VALUE_NAME = "agreed"

class ContentFragment : Fragment() {
    /** Only valid between onViewCreated and onViewDestroyed. */
    private lateinit var broacastReceiver: BroadcastReceiver

    private val onBackPressedCallback: OnBackPressedCallback = object: OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            lifecycleScope.coroutineContext.cancelChildren()
            isEnabled = false
            hideFontLicenseContent()
        }
    }

    private val utc: TimeZone by lazy { TimeZone.getTimeZone("UTC") }

    private var dateText by mutableStateOf("Loading...")

    private var timeText by mutableStateOf("Loading...")

    private var showingFontLicense by mutableStateOf(false)

    private var showingAppLicense by mutableStateOf(false)

    private var fontLicenseButtonAlignment: Alignment by mutableStateOf(Alignment.Center)

    @delegate:ColorInt
    private var contentColor by mutableStateOf(Color.WHITE)

    private var textOrderDateFirst by mutableStateOf(true)

    private var overlayPositionShift by mutableStateOf(false)

    private var textSizeDate by mutableFloatStateOf(0f)

    private var textSizeTime by mutableFloatStateOf(0f)

    private var middleSpringWeight by mutableFloatStateOf(0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MainScreen(
                    dateText = dateText,
                    timeText = timeText,
                    textSizeDate = textSizeDate,
                    textSizeTime = textSizeTime,
                    textOrderDateFirst = textOrderDateFirst,
                    showingFontLicense = showingFontLicense,
                    showingAppLicense = showingAppLicense,
                    fontLicenseButtonAlignment = fontLicenseButtonAlignment,
                    overlayPositionShift = overlayPositionShift,
                    middleSprintWeight = middleSpringWeight,

                    onFontLicenseButtonClick = {
                        if (!showingFontLicense) {
                            setupForFontLicenseContentHiding()
                        }
                    },

                    onAgreeClick = {
                        val sharedPreferences = requireContext().getSharedPreferences(
                            SHARED_PREFS_DISCLAIMER_FILE_NAME,
                            Context.MODE_PRIVATE
                        )
                        sharedPreferences.edit {
                            putBoolean(
                                SHARED_PREFS_DISCLAIMER_VALUE_NAME,
                                true
                            )
                        }
                        showingAppLicense = false
                    },
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.decorView?.setBackgroundColor(Color.BLACK)

        textSizeDate = resources.getRandomTextSizeDate()
        textSizeTime = resources.getRandomTextSizeTime()
        middleSpringWeight = resources.getRandomMiddleSpringWeight()

        overlayPositionShift = Random.nextBoolean()

        contentColor = savedInstanceState?.getInt(SAVED_INSTANCE_STATE_CONTENT_COLOR) ?: getRandomContentColor()
        textOrderDateFirst = savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER) ?: Random.nextBoolean()
        fontLicenseButtonAlignment = savedInstanceState?.getInt(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_ALIGNMENT)?.toAlignment() ?: getRandomAlignment()

        showingFontLicense = savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_FONT_LICENSE_CONTENT_VISIBLE) ?: false

        if (showingFontLicense) {
            setupForFontLicenseContentHiding()
        }

        showingAppLicense = savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_APP_LICENSE_CONTENT_VISIBLE) ?: false

        showingAppLicense =
            !view.context.getSharedPreferences(SHARED_PREFS_DISCLAIMER_FILE_NAME, Context.MODE_PRIVATE).getBoolean(
                SHARED_PREFS_DISCLAIMER_VALUE_NAME, false)

        broacastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                tweakContent(resources)
                updateDisplayText()
            }
        }

        repeatingOverlayTweak()
        updateDisplayText()

        view.context.registerReceiver(broacastReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        view.context.registerReceiver(broacastReceiver, IntentFilter(Intent.ACTION_TIME_CHANGED))
    }

    private fun setupForFontLicenseContentHiding() {
        showingFontLicense = true
        onBackPressedCallback.isEnabled = true

        lifecycleScope.launch {
            delay(FONT_LICENSE_CONTENT_DISPLAY_SECONDS.seconds)
            hideFontLicenseContent()
        }
    }

    private fun hideFontLicenseContent() {
        showingFontLicense = false
        onBackPressedCallback.isEnabled = false
    }

    private fun repeatingOverlayTweak() {
        lifecycleScope.launch {
            delay(5.seconds)
            tweakOverlay()
            repeatingOverlayTweak()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(SAVED_INSTANCE_STATE_CONTENT_COLOR, contentColor)
        outState.putBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER, textOrderDateFirst)
        outState.putInt(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_ALIGNMENT, fontLicenseButtonAlignment.toInt())
        outState.putBoolean(SAVED_INSTANCE_STATE_FONT_LICENSE_CONTENT_VISIBLE, showingFontLicense)
        outState.putBoolean(SAVED_INSTANCE_STATE_APP_LICENSE_CONTENT_VISIBLE, showingAppLicense)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (this::broacastReceiver.isInitialized) {
            context?.unregisterReceiver(broacastReceiver)
        }
    }

    // Helpers

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

    private fun printCurrentTimeFormatted(date: Date) =
        getFormattedDateTime(date, getString(com.dermochelys.utcclock.shared.R.string.time_format_pattern))

    private fun printCurrentDateFormatted(date: Date) =
        getFormattedDateTime(date, getString(com.dermochelys.utcclock.shared.R.string.date_format_pattern))

    @SuppressLint("SimpleDateFormat")
    private fun getFormattedDateTime(date: Date, formatString: String): String {
        val simpleDateFormat = SimpleDateFormat(formatString)
        simpleDateFormat.timeZone = utc
        return simpleDateFormat.format(date)
    }
}

internal fun getRandomFontLicenseButtonHorizontalMargin() = Random.nextInt(96, 188)
