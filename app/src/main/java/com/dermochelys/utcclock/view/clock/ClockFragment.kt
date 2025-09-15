package com.dermochelys.utcclock.view.clock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dermochelys.utcclock.shared.R
import com.dermochelys.utcclock.toColor
import com.dermochelys.utcclock.vectorToBitmap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ClockFragment : Fragment() {
    private val viewModel: ClockViewModel by viewModels()

    /** Only valid between onViewCreated and onViewDestroyed. */
    private lateinit var broadcastReceiver: BroadcastReceiver

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return createView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onViewCreated(resources = resources, savedInstanceState = savedInstanceState)

        lifecycleScope.launch {
            viewModel.getNavigationActions().collect {
                findNavController().navigate(it)
            }
        }

        broadcastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) { viewModel.onTimeUpdated() }
        }

        registerForTimeUpdates(view)
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
                dateText = viewModel.dateText,
                timeText = viewModel.timeText,
                textSizeDate = viewModel.textSizeDate,
                textSizeTime = viewModel.textSizeTime,
                onFontLicenseButtonClicked = viewModel::onFontLicenseButtonClicked,
                onDonationButtonClicked = viewModel::onDonationButtonClicked,
                overlayPositionShift = viewModel.overlayPositionShift,
                fontLicenseButtonAlignmentToStart = viewModel.fontLicenseButtonAlignToStart,
                dateTextAlignToStart = viewModel.dateTextAlignToStart,
                buttonRowTop = viewModel.buttonRowTop,
                contentColor = viewModel.contentColor,
                focusedButtonColor = ContextCompat.getColor(context, R.color.blue).toColor(),
                textOrderDateFirst = viewModel.textOrderDateFirst,
                middleSprintWeight = viewModel.middleSpringWeight,
                overlayBitmap = context.vectorToBitmap(R.drawable.overlay),
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
}
