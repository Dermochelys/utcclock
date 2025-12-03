package com.dermochelys.utcclock.view.exactalarmpermission

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.dermochelys.utcclock.repository.PermissionsRepository
import com.dermochelys.utcclock.widget.canScheduleExactAlarms
import com.dermochelys.utcclock.widget.scheduleNextUpdate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ExactAlarmPermissionFragment : Fragment() {

    @Inject
    lateinit var permissionsRepository: PermissionsRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: android.os.Bundle?
    ) = ComposeView(requireContext()).apply {
        setContent {
            ExactAlarmPermission(
                onOpenSettingsClicked = ::openAlarmSettings,
                onNotNowClicked = { requireActivity().finish() },
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (canScheduleExactAlarms(requireContext())) {
            scheduleNextUpdate(requireContext())
            permissionsRepository.onExactAlarmPermissionChanged()
            requireActivity().finish()
        }
    }

    private fun openAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                }
            )
        }
    }
}
