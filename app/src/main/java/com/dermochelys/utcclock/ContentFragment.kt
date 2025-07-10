package com.dermochelys.utcclock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dermochelys.utcclock.repository.SharedPreferencesRepository
import com.dermochelys.utcclock.shared.R


class ContentFragment : Fragment() {
    private val viewModel: ContentViewModel by viewModels()

    /** Only valid between onViewCreated and onViewDestroyed. */
    private lateinit var broacastReceiver: BroadcastReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(this, viewModel.onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val requireContext = requireContext()

        return ComposeView(requireContext).apply {
            setContent {
                MainScreen(
                    dateText = viewModel.dateText,
                    timeText = viewModel.timeText,
                    textSizeDate = viewModel.textSizeDate,
                    textSizeTime = viewModel.textSizeTime,
                    onFontLicenseButtonClick = { viewModel.onFontLicenseButtonClicked() },
                    showingFontLicense = viewModel.showingFontLicense,
                    showingDisclaimer = viewModel.showingAppLicense,
                    onDisclaimerAgreeClick = { viewModel.onDisclaimerAgreeClicked() },
                    overlayPositionShift = viewModel.overlayPositionShift,
                    fontLicenseButtonAlignment = viewModel.fontLicenseButtonAlignment,
                    textOrderDateFirst = viewModel.textOrderDateFirst,
                    middleSprintWeight = viewModel.middleSpringWeight,
                    isOnTv = requireContext.isRunningOnTv(),
                    overlayBitmap = requireContext.vectorToBitmap(R.drawable.overlay_vector),
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.initialize(resources, savedInstanceState,
            SharedPreferencesRepository(view.context.applicationContext)
        )

        broacastReceiver = object: BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                viewModel.onTimeChanged(resources)
            }
        }

        view.context.registerReceiver(broacastReceiver, IntentFilter(Intent.ACTION_TIME_TICK))
        view.context.registerReceiver(broacastReceiver, IntentFilter(Intent.ACTION_TIME_CHANGED))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        // Be defensive if this is called out of order unexpectedly
        if (this::broacastReceiver.isInitialized) {
            context?.unregisterReceiver(broacastReceiver)
        }

        super.onDestroyView()
    }
}
