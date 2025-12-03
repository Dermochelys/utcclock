package com.dermochelys.utcclock.view.landing

import android.appwidget.AppWidgetManager
import android.content.ComponentName
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
import com.dermochelys.utcclock.widget.GlanceAppWidgetReceiver
import com.dermochelys.utcclock.widget.canScheduleExactAlarms
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
                if (it == -1) return@collect
                val destination = interceptForExactAlarmPermission(it)
                findNavController().navigate(resId = destination, args = null, navOptions = navOptions())
            }
        }
    }

    // Helpers

    private fun interceptForExactAlarmPermission(destination: Int): Int {
        if (destination != R.id.clock_fragment) return destination
        if (canScheduleExactAlarms(requireContext())) return destination
        if (!hasWidgets()) return destination
        return R.id.exact_alarm_permission_fragment
    }

    private fun hasWidgets(): Boolean {
        val manager = AppWidgetManager.getInstance(requireContext())
        val component = ComponentName(requireContext(), GlanceAppWidgetReceiver::class.java)
        return manager.getAppWidgetIds(component).isNotEmpty()
    }

    private fun navOptions() = NavOptions.Builder()
        .setPopUpTo(R.id.landing_fragment, true)
        .build()
}
