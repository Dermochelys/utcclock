package com.dermochelys.utcclock.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.os.Build

fun scheduleNextUpdate(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = getActionUpdateWidgetIntent(context)

    val flags = FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags)

    // Calculate time to next minute boundary
    val now = System.currentTimeMillis()
    val nextMinute = ((now / 60000) + 1) * 60000

    // Use RTC (not RTC_WAKEUP) so we don't wake the device - the widget will update
    // when the device naturally wakes. Use exact alarms for precise minute-boundary updates.

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        // API 31+: Check if we can schedule exact alarms
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExact(
                AlarmManager.RTC,
                nextMinute,
                pendingIntent
            )
        } else {
            // Fall back to inexact alarm if permission not granted
            alarmManager.set(
                AlarmManager.RTC,
                nextMinute,
                pendingIntent
            )
        }
    } else {
        // API 21-30: setExact available, no permission check needed
        alarmManager.setExact(
            AlarmManager.RTC,
            nextMinute,
            pendingIntent
        )
    }
}

fun getActionUpdateWidgetIntent(context: Context): Intent =
    Intent(ACTION_UPDATE_WIDGET).apply { setPackage(context.packageName) }

fun cancelScheduledUpdate(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = getActionUpdateWidgetIntent(context)

    val flags = FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, flags)
    alarmManager.cancel(pendingIntent)
}
