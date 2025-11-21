package com.dermochelys.utcclock.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
import android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
import android.content.pm.PackageManager.DONT_KILL_APP
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import com.dermochelys.utcclock.repository.DisclaimerRepository
import com.dermochelys.utcclock.widget.internal.goAsyncWork
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

internal const val TAG = "Widget"
internal const val ACTION_UPDATE_WIDGET = "com.dermochelys.utcclock.ACTION_UPDATE_WIDGET"

@AndroidEntryPoint
class GlanceAppWidgetReceiver : GlanceAppWidgetReceiver() {
    @Inject
    lateinit var disclaimerRepository: DisclaimerRepository

    override val glanceAppWidget: GlanceAppWidget
        get() = com.dermochelys.utcclock.widget.GlanceAppWidget()

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        // Enable BroadcastReceiver when widgets are active
        context.updateBroadcastReceiverState(enabled = true)

        // No need to try to update, system will trigger update
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)

        // Disable MY_PACKAGE_REPLACED receiver when no widgets are active
        context.updateBroadcastReceiverState(enabled = false)

        cancelScheduledUpdate(context)
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Schedule alarms only when disclaimer is accepted
        goAsyncWork {
            val shouldShow = disclaimerRepository.shouldShowDisclaimer().first()

            if (shouldShow) {
                return@goAsyncWork
            }

            scheduleNextUpdate(context)
        }
    }

    private fun Context.updateBroadcastReceiverState(enabled: Boolean) {
        val componentName = ComponentName(this, BroadcastReceiver::class.java)

        val newState = if (enabled) {
            COMPONENT_ENABLED_STATE_ENABLED
        } else {
            COMPONENT_ENABLED_STATE_DISABLED
        }

        packageManager.setComponentEnabledSetting(componentName, newState, DONT_KILL_APP)
    }
}
