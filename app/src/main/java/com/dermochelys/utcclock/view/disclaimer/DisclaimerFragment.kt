package com.dermochelys.utcclock.view.disclaimer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.dermochelys.utcclock.R
import com.dermochelys.utcclock.isRunningOnTv
import com.dermochelys.utcclock.vectorToBitmap
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DisclaimerFragment : Fragment() {
    private val viewModel: DisclaimerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val context = requireContext()
        val onDisclaimerAgreeClicked = viewModel::onDisclaimerAgreeClicked
        val onViewLaunched = viewModel::onViewLaunched

        return ComposeView(context = context).apply {
            setContent {
                val overlayBitmap = context.vectorToBitmap(com.dermochelys.utcclock.shared.R.drawable.overlay)

                if (context.isRunningOnTv()) {
                    TvDisclaimer(
                        onDisclaimerAgreeClick = onDisclaimerAgreeClicked,
                        onViewLaunched = onViewLaunched,
                        overlayPositionShift = viewModel.overlayPositionShift,
                        overlayBitmap = overlayBitmap,
                    )
                } else {
                    NonTvDisclaimer(
                        onDisclaimerAgreeClick = onDisclaimerAgreeClicked,
                        onViewLaunched = onViewLaunched,
                        overlayPositionShift = viewModel.overlayPositionShift,
                        overlayBitmap = overlayBitmap,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.getNavigationActions().collect { it ->
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()

                findNavController().navigate(it, null, navOptions)
            }
        }
    }
}
