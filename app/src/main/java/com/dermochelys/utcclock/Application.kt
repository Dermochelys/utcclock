package com.dermochelys.utcclock

import android.app.Application
import android.content.Intent
import com.dermochelys.utcclock.widget.ACTION_APP_STARTED
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class Application : Application() {
    override fun onCreate() {
        super.onCreate()

        // This is required to handle the use case for the widget, when the user
        // force-stops the application and then later reopens it -- we need to reschedule
        // alarms to ensure the widget clock is updated.
        sendBroadcast(Intent(ACTION_APP_STARTED).apply { setPackage(packageName) })
    }
}
