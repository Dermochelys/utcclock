package com.dermochelys.utcclock.widget

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.updateAll
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.repository.PermissionsRepository
import com.dermochelys.utcclock.repository.ZonedDateRepository
import com.dermochelys.utcclock.widget.internal.goAsyncWork
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// When the app is force-stopped and restarted we need to reschedule alarms
const val ACTION_APP_STARTED = "com.dermochelys.utcclock.ACTION_APP_STARTED"

@AndroidEntryPoint
class WidgetBroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var disclaimerRepository: DisclaimerRepository

    @Inject
    lateinit var zonedDateRepository: ZonedDateRepository

    @Inject
    lateinit var permissionsRepository: PermissionsRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d(TAG, "BroadcastReceiver.onReceive: action=$action")

        // Handle app updates - reschedule alarms if disclaimer already accepted
        if (action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != ACTION_APP_STARTED &&
            action != ACTION_DISCLAIMER_STATE_CHANGED &&
            action != ACTION_UPDATE_WIDGET &&
            action != AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
            return
        }

        goAsyncWork {
            // For the clear app data case after disclaimer was accepted.  Trigger update back to
            // disclaimer not-yet-accepted state of widget.
            if (action == ACTION_APP_STARTED) {
                Log.d(TAG, "BroadcastReceiver: ACTION_APP_STARTED, updating widgets")
                updateWidgets(context)
                return@goAsyncWork
            }

            // Sync permission state when the OS notifies us of a change
            if (action == AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED) {
                Log.d(TAG, "BroadcastReceiver: syncing exact alarm permission state")
                permissionsRepository.onExactAlarmPermissionChanged()
            }

            // Check if disclaimer was already accepted
            val shouldShowDisclaimer = disclaimerRepository.shouldShowDisclaimer().first()
            Log.d(TAG, "BroadcastReceiver: shouldShowDisclaimer=$shouldShowDisclaimer")

            if (shouldShowDisclaimer) {
                Log.d(TAG, "BroadcastReceiver: disclaimer not accepted, skipping update")
                return@goAsyncWork
            }

            // Disclaimer is accepted, schedule widget updates
            Log.d(TAG, "BroadcastReceiver: updating time, widgets, and scheduling next update")
            updateTime()
            updateWidgets(context)
            scheduleNextUpdate(context)
            Log.d(TAG, "BroadcastReceiver: update complete")
        }
    }

    private suspend fun updateTime() {
        zonedDateRepository.onTimeUpdated()
    }

    private suspend fun updateWidgets(context: Context) {
        UtcClockGlanceAppWidget.updateAll(context)
    }
}
