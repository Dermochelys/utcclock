package com.dermochelys.utcclock.view.clock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dermochelys.utcclock.R
import com.dermochelys.utcclock.view.common.getRandomMiddleSpringWeight
import com.dermochelys.utcclock.view.common.getRandomTextSizeDate
import com.dermochelys.utcclock.view.common.getRandomTextSizeTime
import com.dermochelys.utcclock.view.common.toColor
import com.dermochelys.utcclock.view.common.vectorToBitmap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ClockFragment : Fragment() {
    private val viewModel: ClockViewModel by viewModels()

    /** Only valid between onViewCreated and onViewDestroyed. */
    private lateinit var broadcastReceiver: BroadcastReceiver

    private var timeFormat by mutableStateOf("")

    private var dateFormat by mutableStateOf("")

    private var textSizeDate by mutableFloatStateOf(0f)

    private var textSizeTime by mutableFloatStateOf(0f)

    private var middleSpringWeight by mutableFloatStateOf(1.0f)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return createView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateFormats()
        randomizeSizes()
        savedInstanceState?.let { viewModel.onLoadInstanceState(it) }

        lifecycleScope.launch {
            viewModel.getNavigationActions().collect {
                findNavController().navigate(it)
            }
        }

        broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModel.onTimeUpdated()
                randomizeSizes()
            }
        }

        registerForTimeUpdates(view)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateFormats()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        unregisterFromTimeUpdates()
        super.onDestroyView()
    }

    // Helpers

    private fun createView(context: Context): ComposeView = ComposeView(context).apply {
        setContent {
            Clock(
                onFontLicenseButtonClicked = viewModel::onFontLicenseButtonClicked,
                onDonationButtonClicked = viewModel::onDonationButtonClicked,
                overlayPositionShift = viewModel.overlayPositionShift,
                fontLicenseButtonAlignmentToStart = viewModel.fontLicenseButtonAlignToStart,
                dateTextAlignToStart = viewModel.dateTextAlignToStart,
                buttonRowTop = viewModel.buttonRowTop,
                contentColor = viewModel.contentColor,
                zonedDateTime = viewModel.zonedDateTime,
                focusedButtonColor = ContextCompat.getColor(context, R.color.blue).toColor(),
                textOrderDateFirst = viewModel.textOrderDateFirst,
                overlayBitmap = context.vectorToBitmap(R.drawable.overlay),

                middleSprintWeight = middleSpringWeight,
                timeFormat = timeFormat,
                dateFormat = dateFormat,
                textSizeDate = textSizeDate,
                textSizeTime = textSizeTime,
            )
        }
    }

    private fun registerForTimeUpdates(view: View) {
        view.context.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        view.context.registerReceiver(broadcastReceiver, IntentFilter(Intent.ACTION_TIME_CHANGED))
    }

    private fun unregisterFromTimeUpdates() {
        // Be defensive if this is called out of order unexpectedly
        if (this::broadcastReceiver.isInitialized) {
            context?.unregisterReceiver(broadcastReceiver)
        }
    }

    private fun updateFormats() {
        timeFormat = resources.getString(R.string.time_format_pattern)
        dateFormat = resources.getString(R.string.date_format_pattern)
    }

    private fun randomizeSizes() {
        textSizeDate = resources.getRandomTextSizeDate()
        textSizeTime = resources.getRandomTextSizeTime()
        middleSpringWeight = resources.getRandomMiddleSpringWeight()
    }
}
