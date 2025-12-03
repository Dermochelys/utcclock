package com.dermochelys.utcclock.widget

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

const val ACTION_DISCLAIMER_STATE_CHANGED = "com.dermochelys.utcclock.ACTION_DISCLAIMER_STATE_CHANGED"

/**
 * Singleton that broadcasts disclaimer state changes to widgets.
 * Widgets can register for ACTION_DISCLAIMER_STATE_CHANGED to receive notifications
 * when the disclaimer acceptance state changes.
 */
@Singleton
class DisclaimerStateBroadcaster @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Broadcasts that the disclaimer state has changed.
     * This notifies widgets to update their state (e.g., schedule alarms after acceptance).
     */
    fun notifyDisclaimerStateChanged() {
        context.sendBroadcast(intent())
    }

    private fun intent(): Intent = Intent(ACTION_DISCLAIMER_STATE_CHANGED).apply { setPackage(context.packageName) }
}
