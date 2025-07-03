package com.dermochelys.utcclock

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.alpha
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dermochelys.utcclock.databinding.FragmentMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import androidx.core.content.edit

private const val SAVED_INSTANCE_STATE_TEXT_SIZE_DATE = "textSizeDate"

private const val SAVED_INSTANCE_STATE_TEXT_SIZE_TIME = "textSizeTime"

private const val SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_GRAVITY = "licenseGravity"

private const val SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_MARGIN_HORIZONTAL = "licenseMarginHorizontal"

private const val SAVED_INSTANCE_STATE_FONT_LICENSE_CONTENT_VISIBILITY = "fontLicenseContentVisibility"

private const val SAVED_INSTANCE_STATE_OVERLAY_POSITION_X = "overlayPositionX"

private const val SAVED_INSTANCE_STATE_OVERLAY_POSITION_Y = "overlayPositionY"

private const val SAVED_INSTANCE_STATE_CONTENT_COLOR = "contentColor"

private const val SAVED_INSTANCE_STATE_TEXT_ORDER = "textOrder"

private const val SAVED_INSTANCE_STATE_APP_LICENSE_CONTENT_VISIBILITY = "appLicenseContentVisibility"

private const val FONT_LICENSE_BUTTON_ALPHA_DIMMING = 20

private const val FONT_LICENSE_CONTENT_DISPLAY_SECONDS = 15

private const val SHARED_PREFS_DISCLAIMER_FILE_NAME = "disclaimer"

private const val SHARED_PREFS_DISCLAIMER_VALUE_NAME = "agreed"

class ContentFragment : Fragment() {

    private var fontLicenseButtonGravity: Int = getRandomFontLicenseButtonGravity()

    private var fontLicenseButtonHorizontalMargin: Int = getRandomFontLicenseButtonHorizontalMargin()

    private var textSizeDate: Float = 0f

    private var textSizeTime: Float = 0f

    @ColorInt
    private var contentColor: Int = getRandomContentColor()

    private var textOrderDateFirst: Boolean = Random.nextBoolean()

    private var overlayPositionX: Int = getRandomOverlayPosition()

    private var overlayPositionY: Int = getRandomOverlayPosition()

    private var fontLicenseContentVisibility: Int = View.GONE

    private var appLicenseContentVisibility: Int = View.VISIBLE

    private var _binding: FragmentMainBinding? = null

    /** Only valid between onCreateView and onDestroyView. */
    private val binding get() = _binding!!

    /** Only valid between onViewCreated and onViewDestroyed. */
    private lateinit var broacastReceiver: BroadcastReceiver

    private val utc: TimeZone by lazy { TimeZone.getTimeZone("UTC") }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.decorView?.setBackgroundColor(Color.BLACK)

        savedInstanceState?.getFloat(SAVED_INSTANCE_STATE_TEXT_SIZE_DATE)?.let { textSizeDate = it }
        savedInstanceState?.getFloat(SAVED_INSTANCE_STATE_TEXT_SIZE_TIME)?.let { textSizeTime = it }
        savedInstanceState?.getInt(SAVED_INSTANCE_STATE_CONTENT_COLOR)?.let { contentColor = it }
        savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER)?.let { textOrderDateFirst = it }
        savedInstanceState?.getInt(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_GRAVITY)?.let { fontLicenseButtonGravity = it }
        savedInstanceState?.getInt(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_MARGIN_HORIZONTAL)?.let { fontLicenseButtonHorizontalMargin = it }
        savedInstanceState?.getInt(SAVED_INSTANCE_STATE_FONT_LICENSE_CONTENT_VISIBILITY)?.let { fontLicenseContentVisibility = it }
        savedInstanceState?.getInt(SAVED_INSTANCE_STATE_OVERLAY_POSITION_X)?.let { overlayPositionX = it }
        savedInstanceState?.getInt(SAVED_INSTANCE_STATE_OVERLAY_POSITION_Y)?.let { overlayPositionY = it }
        savedInstanceState?.getInt(SAVED_INSTANCE_STATE_APP_LICENSE_CONTENT_VISIBILITY)?.let { appLicenseContentVisibility = it }

        if (view.context.getSharedPreferences(SHARED_PREFS_DISCLAIMER_FILE_NAME, Context.MODE_PRIVATE).getBoolean(
                SHARED_PREFS_DISCLAIMER_VALUE_NAME, false)) {
            appLicenseContentVisibility = View.GONE
        }

        textSizeTime = getRandomTextSizeTime(resources)
        textSizeDate = getRandomTextSizeDate(resources)

        binding.textviewTime.paint.isAntiAlias = false
        binding.textviewDate.paint.isAntiAlias = false

        binding.fontLicenseButton.setOnClickListener {
            if (fontLicenseContentVisibility == View.VISIBLE) {
                return@setOnClickListener
            }

            fontLicenseContentVisibility = View.VISIBLE
            applyFontLicenseContentState()
        }

        binding.appLicenseContentAgree.setOnClickListener {
            val sharedPreferences = binding.appLicenseContentAgree.context.getSharedPreferences(
                SHARED_PREFS_DISCLAIMER_FILE_NAME,
                Context.MODE_PRIVATE
            )

            sharedPreferences.edit { putBoolean(SHARED_PREFS_DISCLAIMER_VALUE_NAME, true) }
            appLicenseContentVisibility = View.GONE
            applyAppLicenseContentState()
        }

        applyContentState()
        applyOverlayState()
        applyFontLicenseContentState()
        applyAppLicenseContentState()

        broacastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                tweakContent(resources)
                applyContentState()
            }
        }

        repeatingOverlayTweak()
        view.context.registerReceiver(broacastReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        view.context.registerReceiver(broacastReceiver, IntentFilter(Intent.ACTION_TIME_CHANGED))
    }

    private fun scheduleFontLicenseContentHiding() {
        lifecycleScope.launch {
            delay(FONT_LICENSE_CONTENT_DISPLAY_SECONDS.seconds)
            fontLicenseContentVisibility = View.GONE
            applyFontLicenseContentState()
        }
    }

    private fun repeatingOverlayTweak() {
        lifecycleScope.launch {
            delay(5.seconds)
            tweakOverlay()
            applyOverlayState()
            repeatingOverlayTweak()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat(SAVED_INSTANCE_STATE_TEXT_SIZE_DATE, textSizeDate)
        outState.putFloat(SAVED_INSTANCE_STATE_TEXT_SIZE_TIME, textSizeTime)
        outState.putInt(SAVED_INSTANCE_STATE_CONTENT_COLOR, contentColor)
        outState.putBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER, textOrderDateFirst)
        outState.putInt(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_GRAVITY, fontLicenseButtonGravity)
        outState.putInt(SAVED_INSTANCE_STATE_FONT_LICENSE_BUTTON_MARGIN_HORIZONTAL, fontLicenseButtonHorizontalMargin)
        outState.putInt(SAVED_INSTANCE_STATE_FONT_LICENSE_CONTENT_VISIBILITY, fontLicenseContentVisibility)
        outState.putInt(SAVED_INSTANCE_STATE_APP_LICENSE_CONTENT_VISIBILITY, appLicenseContentVisibility)
        outState.putInt(SAVED_INSTANCE_STATE_OVERLAY_POSITION_X, overlayPositionX)
        outState.putInt(SAVED_INSTANCE_STATE_OVERLAY_POSITION_Y, overlayPositionY)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        if (this::broacastReceiver.isInitialized) {
            context?.unregisterReceiver(broacastReceiver)
        }
    }

    // Helpers

    private fun tweakContent(resources: Resources) {
        textOrderDateFirst = !textOrderDateFirst
        textSizeDate = getRandomTextSizeDate(resources)
        textSizeTime = getRandomTextSizeTime(resources)
        contentColor = getRandomContentColor()
        fontLicenseButtonGravity = getRandomFontLicenseButtonGravity()
        fontLicenseButtonHorizontalMargin = getRandomFontLicenseButtonHorizontalMargin()
    }

    private fun tweakOverlay() {
        overlayPositionX = getRandomOverlayPosition()
        overlayPositionY = getRandomOverlayPosition()
    }

    private fun applyContentState(date: Date = Date()) {
        val textViewDate = binding.textviewDate
        textViewDate.setTextColor(contentColor)
        textViewDate.text = printCurrentDateFormatted(date)
        textViewDate.textSize = textSizeDate

        val textViewTime = binding.textviewTime
        textViewTime.setTextColor(contentColor)
        textViewTime.text = printCurrentTimeFormatted(date)
        textViewTime.textSize = textSizeTime

        binding.topView.removeAllViews()
        binding.bottomView.removeAllViews()

        if (textOrderDateFirst) {
            binding.topView.addView(textViewDate)
            binding.bottomView.addView(textViewTime)
        } else {
            binding.topView.addView(textViewTime)
            binding.bottomView.addView(textViewDate)
        }

        applyFontLicenseButtonState()
        applyFontLicenseContentBackgroundState()
    }

    private fun applyFontLicenseContentBackgroundState() {
        binding.fontLicenseContent.background.setTint(
            ColorUtils.setAlphaComponent(contentColor, 255))
    }

    private fun printCurrentTimeFormatted(date: Date) =
        getFormattedDateTime(date, getString(R.string.time_format_pattern))

    private fun printCurrentDateFormatted(date: Date) =
        getFormattedDateTime(date, getString(R.string.date_format_pattern))

    @SuppressLint("SimpleDateFormat")
    private fun getFormattedDateTime(date: Date, formatString: String): String {
        val simpleDateFormat = SimpleDateFormat(formatString)
        simpleDateFormat.timeZone = utc
        return simpleDateFormat.format(date)
    }

    private fun applyFontLicenseButtonState() {
        val layoutParams = binding.fontLicenseButton.layoutParams as FrameLayout.LayoutParams
        layoutParams.gravity = fontLicenseButtonGravity

        when (fontLicenseButtonGravity) {
            Gravity.START -> layoutParams.setMargins(fontLicenseButtonHorizontalMargin, 0, 0 ,0)
            else -> layoutParams.setMargins(0, 0, fontLicenseButtonHorizontalMargin, 0)
        }

        binding.fontLicenseButton.layoutParams = layoutParams
        val buttonAlpha = contentColor.alpha - FONT_LICENSE_BUTTON_ALPHA_DIMMING
        binding.fontLicenseButton.drawable.setTint(ColorUtils.setAlphaComponent(contentColor, buttonAlpha))
    }

    private fun applyFontLicenseContentState() {
        if (binding.fontLicenseContent.visibility == fontLicenseContentVisibility) {
            return
        }

        binding.fontLicenseContent.visibility = fontLicenseContentVisibility

        if (fontLicenseContentVisibility == View.VISIBLE) {
            scheduleFontLicenseContentHiding()
        }
    }

    private fun applyAppLicenseContentState() {
        if (binding.appLicenseContent.visibility == appLicenseContentVisibility) {
            return
        }

        binding.appLicenseContent.visibility = appLicenseContentVisibility
    }
    private fun applyOverlayState() {
        val layoutParams = binding.overlay.layoutParams as FrameLayout.LayoutParams
        layoutParams.setMargins(overlayPositionX, overlayPositionY, 0 ,0)
        binding.overlay.layoutParams = layoutParams
    }

    private fun getRandomTextSizeTime(resources: Resources) =
        resources.getRandomDimension(R.dimen.time_font_size_min, R.dimen.time_font_size_max)

    private fun getRandomTextSizeDate(resources: Resources) =
        resources.getRandomDimension(R.dimen.date_font_size_min, R.dimen.date_font_size_max)
}

private fun getRandomFontLicenseButtonGravity() =
    when (Random.nextInt(1, 4)) {
        1 -> Gravity.CENTER_HORIZONTAL
        2 -> Gravity.START
        else -> Gravity.END
    } or Gravity.CENTER_VERTICAL

private fun getRandomOverlayPosition() = Random.nextInt(-5, 1)

@ColorInt
private fun getRandomContentColor(): Int = Color.argb(
    Random.nextInt(240, 256),
    Random.nextInt(220, 256),
    Random.nextInt(220, 256),
    Random.nextInt(220, 256)
)

private fun getRandomFontLicenseButtonHorizontalMargin() = Random.nextInt(6, 55)

private fun Resources.getRandomDimension(idMin: Int, idMax: Int): Float =
    Random.nextDouble(getDimension(idMin).toDouble(), getDimension(idMax).toDouble()).toFloat()

private fun Resources.getRandomInteger(idMin: Int, idMax: Int): Int =
    Random.nextInt(getInteger(idMin), getInteger(idMax))