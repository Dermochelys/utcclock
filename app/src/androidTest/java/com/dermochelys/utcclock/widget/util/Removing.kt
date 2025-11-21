package com.dermochelys.utcclock.widget.util

import android.app.Instrumentation
import android.os.Build
import android.util.Log
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2

/**
 * Widget removal utilities.
 */

/**
 * Removes the UTC Clock widget from the home screen.
 * Uses a long-press and drag gesture to move the widget to the "Remove" area.
 * Removes ALL UTC Clock widgets to handle scenarios where multiple widgets exist.
 *
 * **API Limitations:**
 * - **API 23-28**: Widget removal is not supported because the gesture is complex and shell commands
 *   for this complex gesture don't work properly until API 29+. Method returns early.
 * - **API 29+**: Widget removal is supported
 */
fun removeUtcClockWidget(device: UiDevice,
                         instrumentation: Instrumentation) {
    val apiLevel = Build.VERSION.SDK_INT

    if (apiLevel < Build.VERSION_CODES.Q) { // API 23-28
        Log.d(TAG, "removeUtcClockWidget: Skipping on API $apiLevel (not supported until API 29+)")
        return
    }

    // Ensure we're on home screen
    goToHomeScreen(device)
    Thread.sleep(500)
    device.waitForIdle()

    // Find all widgets
    val widgets = findAllUtcClockWidgets(device)

    Log.d(TAG, "removeUtcClockWidget: Found ${widgets.size} widget(s) to remove")

    for ((index, widget) in widgets.withIndex()) {
        val widgetBounds = widget.visibleBounds
        val widgetX = widgetBounds.centerX()
        val widgetY = widgetBounds.centerY()

        try {
            // Step 1: Long-press on widget (press down and wait)
            val downCommand = "input motionevent DOWN $widgetX $widgetY"
            executeShellCommand(instrumentation, downCommand)

            // Step 2: Hold for long-press duration
            Thread.sleep(2500)
            device.waitForIdle()

            // Step 3: Look for "Remove" element
            val removeElement = findRemoveElement(device)

            if (removeElement == null) {
                Log.w(TAG, "removeUtcClockWidget: 'Remove' element not found, releasing gesture")
                val upCommand = "input motionevent UP $widgetX $widgetY"
                executeShellCommand(instrumentation, upCommand)
                device.waitForIdle()
                continue
            }

            val removeBounds = removeElement.visibleBounds
            val removeX = removeBounds.centerX()
            val removeY = removeBounds.centerY()

            // Step 4: Drag widget to "Remove" element
            dragToRemove(instrumentation, widgetX, widgetY, removeX, removeY)

            // Step 5: Release at "Remove" position
            val upCommand = "input motionevent UP $removeX $removeY"
            executeShellCommand(instrumentation, upCommand)

            // Wait for widget removal to complete
            Thread.sleep(500)
            device.waitForIdle()
        } catch (e: Exception) {
            Log.e(TAG, "removeUtcClockWidget: ERROR removing widget ${index + 1}", e)

            // Try to release gesture if still holding
            try {
                val releaseCommand = "input motionevent UP $widgetX $widgetY"
                executeShellCommand(instrumentation, releaseCommand)
                device.waitForIdle()
            } catch (releaseException: Exception) {
                Log.e(TAG, "removeUtcClockWidget: Failed to release gesture", releaseException)
            }
        }
    }
}

/**
 * Finds the "Remove" element on screen during widget removal.
 */
private fun findRemoveElement(device: UiDevice): UiObject2? {
    var attempts = 0
    val maxAttempts = 5

    while (attempts < maxAttempts) {
        findText(device, REMOVE)?.let { return it }

        Thread.sleep(500)
        attempts++
    }

    return null
}

/**
 * Drags widget to the Remove element using motion events.
 */
private fun dragToRemove(
    instrumentation: Instrumentation,
    widgetX: Int,
    widgetY: Int,
    removeX: Int,
    removeY: Int
) {
    // Drag in two steps for smoother gesture

    // Midpoint
    val midX = (widgetX + removeX) / 2
    val midY = (widgetY + removeY) / 2
    val midMoveCommand = "input motionevent MOVE $midX $midY"
    executeShellCommand(instrumentation, midMoveCommand)

    // Final position
    val finalMoveCommand = "input motionevent MOVE $removeX $removeY"
    executeShellCommand(instrumentation, finalMoveCommand)
}
