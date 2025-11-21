package com.dermochelys.utcclock.widget

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.updateAll
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.repository.ZonedDateRepository
import com.dermochelys.utcclock.widget.internal.goAsyncWork
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// When the app is force-stopped and restarted we need to reschedule alarms
const val ACTION_APP_STARTED = "com.dermochelys.utcclock.ACTION_APP_STARTED"

@AndroidEntryPoint
class BroadcastReceiver : BroadcastReceiver() {
    @Inject
    lateinit var disclaimerRepository: DisclaimerRepository

    @Inject
    lateinit var zonedDateRepository: ZonedDateRepository

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action

        // Handle app updates - reschedule alarms if disclaimer already accepted
        if (action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != ACTION_APP_STARTED &&
            action != ACTION_DISCLAIMER_STATE_CHANGED &&
            action != ACTION_UPDATE_WIDGET) {
            return
        }

        goAsyncWork {
            // For the clear app data case after disclaimer was accepted.  Trigger update back to
            // disclaimer not-yet-accepted state of widget.
            if (action == ACTION_APP_STARTED) {
                updateWidgets(context)
                return@goAsyncWork
            }

            // Check if disclaimer was already accepted
            val shouldShowDisclaimer = disclaimerRepository.shouldShowDisclaimer().first()

            if (shouldShowDisclaimer) {
                return@goAsyncWork
            }

            // Disclaimer is accepted, schedule widget updates
            updateTime()
            updateWidgets(context)
            scheduleNextUpdate(context)
        }
    }

    private suspend fun updateTime() {
        zonedDateRepository.onTimeUpdated()
    }

    private suspend fun updateWidgets(context: Context) {
        GlanceAppWidget().updateAll(context)
    }
}
