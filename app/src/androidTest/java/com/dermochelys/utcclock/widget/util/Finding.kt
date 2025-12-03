package com.dermochelys.utcclock.widget.util

import android.util.Log
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2

/**
 * Widget finding and verification utilities.
 */

/**
 * Finds all UTC Clock widgets on the home screen.
 * Returns a list of all matching widgets to handle scenarios where multiple widgets exist.
 */
fun findAllUtcClockWidgets(device: UiDevice): List<UiObject2> {
    Log.d(TAG, "findAllUtcClockWidgets")
    val widgets = mutableListOf<UiObject2>()

    // Look for LauncherAppWidgetHostView with contentDescription containing "UTC Clock"
    val widgetHostViews = findClass(device, CLASS_LAUNCHER_APP_WIDGET_HOST_VIEW)

    for (hostView in widgetHostViews) {
        val hostDesc = hostView.contentDescription

        if (hostDesc != null && hostDesc.contains(UTC_CLOCK, ignoreCase = true)) {
            widgets.add(hostView)
            continue
        }
    }

    // Needed for API 23 (and others?)
    // Strategy 2: Fallback - Look for FrameLayout with resourceName containing "rootView" and find parent
    if (widgets.isNotEmpty()) {
        Log.d(TAG, "======|| findAllUtcClockWidgets - STRATEGY 1 SUCCEEDED ||======")
    } else {
        val frameLayouts = findClass(device, CLASS_FRAME_LAYOUT)

        for (frameLayout in frameLayouts) {
            if (frameLayout.resourceName?.contains(RES_ROOT_VIEW) != true) continue

            // Find parent LauncherAppWidgetHostView
            var parent = frameLayout.parent
            var hostView: UiObject2? = null
            var depth = 0

            while (parent != null && depth < 15) {
                if (parent.className.contains("AppWidgetHostView", ignoreCase = true) ||
                    parent.className.contains("WidgetHostView", ignoreCase = true)) {
                    hostView = parent
                    break
                }
                parent = parent.parent
                depth++
            }

            if (hostView != null && !widgets.contains(hostView)) {
                Log.d(TAG, "======|| findAllUtcClockWidgets - STRATEGY 2 SUCCEEDED ||======")
                widgets.add(hostView)
            }
        }
    }

    if (widgets.isEmpty()) {
        throw Exception("findAllUtcClockWidgets did not find any widgets")
    }

    Log.d(TAG, "findAllUtcClockWidgets: Found ${widgets.size} widget(s)")
    return widgets
}

/**
 * Taps on the UTC Clock widget.
 */
fun tapAnyUtcClockWidget(device: UiDevice) {
    Log.d(TAG, "tapAnyUtcClockWidget")

    val widget = findAllUtcClockWidgets(device).firstOrNull()
        ?: throw Exception("UTC Clock widget not found on home screen")

    widget.click()
    device.waitForIdle()
}

/**
 * Waits for the widget loading indicator to disappear.
 * Checks ALL UTC Clock widgets on screen to handle scenarios where multiple widgets exist.
 */
fun waitForWidgetLoadingToComplete(device: UiDevice) {
    if (!hasVisibleProgressBar(findAllUtcClockWidgets(device))) {
        return
    }

    // Wait up to 10 seconds for the ProgressBar to disappear in all widgets
    val startTime = System.currentTimeMillis()
    val checkIntervalMs = 200L

    while (System.currentTimeMillis() - startTime < TIMEOUT_10S_IN_MS) {
        Thread.sleep(checkIntervalMs)
        val currentWidgets = findAllUtcClockWidgets(device)

        if (!hasVisibleProgressBar(currentWidgets)) {
            return
        }
    }
}

/**
 * Checks if any widget in the list has a visible ProgressBar.
 */
private fun hasVisibleProgressBar(widgets: List<UiObject2>): Boolean {
    for (widget in widgets) {
        val progressBars = widget.findObjects(By.clazz(CLASS_PROGRESS_BAR))
        if (progressBars.any { pb ->
                val bounds = pb.visibleBounds
                !bounds.isEmpty && bounds.width() > 0 && bounds.height() > 0
            }) {
            return true
        }
    }
    return false
}

/**
 * Verifies that the widget displays the "Tap to begin" text.
 * Checks ALL UTC Clock widgets on screen to handle scenarios where multiple widgets exist.
 */
fun verifyWidgetShowsTapToBegin(device: UiDevice): Boolean {
    waitForWidgetLoadingToComplete(device)

    val widgets = findAllUtcClockWidgets(device)

    // Check TextView text in all widgets
    for (widget in widgets) {
        val textViews = widget.findObjects(By.clazz(CLASS_TEXT_VIEW))
        if (textViews.any { it.text?.contains(TAP_TO_BEGIN, ignoreCase = true) == true }) {
            return true
        }
    }

    return false
}

/**
 * Verifies that the widget displays the "Tap to enable precise updates" text.
 * Checks ALL UTC Clock widgets on screen to handle scenarios where multiple widgets exist.
 */
fun verifyWidgetShowsExactAlarmPrompt(device: UiDevice): Boolean {
    waitForWidgetLoadingToComplete(device)

    val widgets = findAllUtcClockWidgets(device)

    // Check TextView text in all widgets
    for (widget in widgets) {
        val textViews = widget.findObjects(By.clazz(CLASS_TEXT_VIEW))
        Log.d(TAG, "verifyWidgetShowsExactAlarmPrompt: widget texts=${textViews.map { it.text }}")
        if (textViews.any { it.text?.contains(ALARM_PERMISSION_REQUIRED, ignoreCase = true) == true }) {
            return true
        }
    }

    return false
}

/**
 * Verifies that the widget displays UTC time content.
 * Checks ALL UTC Clock widgets on screen to handle scenarios where multiple widgets exist.
 * Retries up to 3 times with 2 second delays to handle minute boundary updates.
 */
fun verifyWidgetShowsUtcTime(device: UiDevice): Boolean {
    waitForWidgetLoadingToComplete(device)

    repeat(3) { attempt ->
        val widgets = findAllUtcClockWidgets(device)
        if (widgets.isEmpty()) {
            if (attempt < 2) Thread.sleep(2000)
            return@repeat
        }

        try {
            // Check TextView text in all widgets
            for (widget in widgets) {
                val textViews = widget.findObjects(By.clazz(CLASS_TEXT_VIEW))
                if (textViews.any { tv ->
                        tv.text?.contains(":") == true && tv.text?.contains(UTC, ignoreCase = true) == true
                    }) {
                    return true
                }
            }
        } catch (_: androidx.test.uiautomator.StaleObjectException) {
            // Widget was updated/recreated, will retry on next attempt
            if (attempt < 2) Thread.sleep(2000)
            return@repeat
        }

        // Wait before next attempt (except on last attempt)
        if (attempt < 2) Thread.sleep(2000)
    }

    return false
}

/**
 * Waits for the widget to update its content.
 */
fun waitForWidgetUpdate(device: UiDevice, timeoutMs: Long = 3000L) {
    Thread.sleep(timeoutMs)
    device.waitForIdle()
}

/**
 * Returns the number of UTC Clock widgets on the home screen.
 * Returns 0 if none are found (rather than throwing).
 */
fun countUtcClockWidgets(device: UiDevice): Int =
    try { findAllUtcClockWidgets(device).size } catch (_: Exception) { 0 }

/**
 * Verifies that ALL UTC Clock widgets display the "Tap to begin" text.
 * Unlike verifyWidgetShowsTapToBegin (which succeeds if any widget matches),
 * this requires every widget on screen to match — used for multi-widget tests.
 */
fun allWidgetsShowTapToBegin(device: UiDevice): Boolean {
    waitForWidgetLoadingToComplete(device)
    val widgets = findAllUtcClockWidgets(device)
    if (widgets.isEmpty()) return false
    return widgets.all { widget ->
        val textViews = widget.findObjects(By.clazz(CLASS_TEXT_VIEW))
        textViews.any { it.text?.contains(TAP_TO_BEGIN, ignoreCase = true) == true }
    }
}

/**
 * Verifies that ALL UTC Clock widgets display UTC time content.
 * Retries up to 3 times to handle minute-boundary updates, matching
 * verifyWidgetShowsUtcTime's behavior.
 */
fun allWidgetsShowUtcTime(device: UiDevice): Boolean {
    waitForWidgetLoadingToComplete(device)

    repeat(3) { attempt ->
        val widgets = findAllUtcClockWidgets(device)
        if (widgets.isEmpty()) {
            if (attempt < 2) Thread.sleep(2000)
            return@repeat
        }

        try {
            val allShow = widgets.all { widget ->
                val textViews = widget.findObjects(By.clazz(CLASS_TEXT_VIEW))
                textViews.any { tv ->
                    tv.text?.contains(":") == true && tv.text?.contains(UTC, ignoreCase = true) == true
                }
            }
            if (allShow) return true
        } catch (_: androidx.test.uiautomator.StaleObjectException) {
            if (attempt < 2) Thread.sleep(2000)
            return@repeat
        }

        if (attempt < 2) Thread.sleep(2000)
    }

    return false
}
