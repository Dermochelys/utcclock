package com.dermochelys.utcclock

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dermochelys.utcclock.databinding.FragmentMainBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import kotlin.random.Random

private const val SAVED_INSTANCE_STATE_TEXT_SIZE_DATE = "textSizeDate"

private const val SAVED_INSTANCE_STATE_TEXT_SIZE_TIME = "textSizeTime"

private const val SAVED_INSTANCE_STATE_TEXT_COLOR = "fontOpacity"

private const val SAVED_INSTANCE_STATE_TEXT_ORDER = "textOrder"

class ContentFragment : Fragment() {

    private var textSizeDate: Float = 60f

    private var textSizeTime: Float = 100f

    private var horizontalDividerHeight: Int = 10

    private var textColor: Int = Color.WHITE

    private var textOrderDateFirst: Boolean = true

    private var _binding: FragmentMainBinding? = null

    /** Only valid between onCreateView and onDestroyView. */
    private val binding get() = _binding!!

    /** Only valid between onViewCreated and onViewDestroyed. */
    private lateinit var broacastReceiver: BroadcastReceiver

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
        savedInstanceState?.getInt(SAVED_INSTANCE_STATE_TEXT_COLOR)?.let { textColor = it }
        savedInstanceState?.getBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER)?.let { textOrderDateFirst = it }
        applyUiState()

        broacastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                tweakUi()
                applyUiState()
            }
        }

        val intentFilter = IntentFilter(Intent.ACTION_TIME_TICK)
        context?.registerReceiver(broacastReceiver, intentFilter)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat(SAVED_INSTANCE_STATE_TEXT_SIZE_DATE, textSizeDate)
        outState.putFloat(SAVED_INSTANCE_STATE_TEXT_SIZE_TIME, textSizeTime)
        outState.putInt(SAVED_INSTANCE_STATE_TEXT_COLOR, textColor)
        outState.putBoolean(SAVED_INSTANCE_STATE_TEXT_ORDER, textOrderDateFirst)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

        if (this::broacastReceiver.isInitialized) {
            context?.unregisterReceiver(broacastReceiver)
        }
    }

    // Helpers

    private fun tweakUi() {
        textOrderDateFirst = !textOrderDateFirst
        textSizeDate = Random.nextInt(54, 60).toFloat()
        textSizeTime = Random.nextInt(95, 105).toFloat()
        horizontalDividerHeight = Random.nextInt(0, 20)

        textColor = Color.argb(
            Random.nextInt(205, 255),
            Random.nextInt(220, 255),
            Random.nextInt(220, 255),
            Random.nextInt(220, 255))
    }

    private fun applyUiState() {
        val layoutParams = binding.horizontalDivider.layoutParams
        layoutParams.height = horizontalDividerHeight
        binding.horizontalDivider.layoutParams = layoutParams

        val textViewDate = binding.textviewDate
        val textViewTime = binding.textviewTime
        textViewDate.setTextColor(textColor)
        textViewTime.setTextColor(textColor)

        textViewDate.text = printCurrentDateFormatted()
        textViewDate.textSize = textSizeDate
        textViewTime.text = printCurrentTimeFormatted()
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
    }

    private fun printCurrentTimeFormatted() =
        getFormattedDateTime(getString(R.string.time_format_pattern))

    private fun printCurrentDateFormatted() =
        getFormattedDateTime(getString(R.string.date_format_pattern))

    @SuppressLint("SimpleDateFormat")
    private fun getFormattedDateTime(formatString: String): String {
        val simpleDateFormat = SimpleDateFormat(formatString)
        simpleDateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return simpleDateFormat.format(Date())
    }
}
