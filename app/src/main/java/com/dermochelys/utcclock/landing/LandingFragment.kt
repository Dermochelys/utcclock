package com.dermochelys.utcclock.landing

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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LandingFragment : Fragment() {
    private val viewModel: LandingViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ComposeView(requireContext()).apply { setContent { Landing() } }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launch {
            viewModel.getNavigationActions().collect {
                findNavController().navigate(resId = it, args = null, navOptions = navOptions())
            }
        }

        viewModel.onViewCreated()
    }

    // Helpers

    private fun navOptions() = NavOptions.Builder()
        .setPopUpTo(R.id.landing_fragment, true)
        .build()
}
