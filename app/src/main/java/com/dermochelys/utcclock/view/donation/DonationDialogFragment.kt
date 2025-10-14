package com.dermochelys.utcclock.view.donation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.dermochelys.utcclock.view.common.AutoNavBackViewModel
import com.dermochelys.utcclock.view.common.hideSystemUi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DonationDialogFragment : DialogFragment() {
    private val viewModel: AutoNavBackViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        lifecycleScope.launch { viewModel.getNavigationActions().collect { dismiss() } }
        return ComposeView(requireContext()).apply { setContent { Donation() } }
    }

    // On older Android versions this is required in order to auto-hide nav bars
    override fun onDestroyView() {
        requireActivity().window.hideSystemUi()
        super.onDestroyView()
    }
}
