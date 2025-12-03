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
 * Removes ALL UTC Clock widgets to handle scenarios where multiple widgets exist.
 *
 * Uses two approaches based on API level:
 * - **API 29+**: `input motionevent` shell commands for precise gesture control
 * - **API < 29**: `device.drag()` which has built-in long-press-drag support
 */
fun removeUtcClockWidget(device: UiDevice,
                         instrumentation: Instrumentation) {
    // Ensure we're on home screen
    goToHomeScreen(device)
    Thread.sleep(500)
    device.waitForIdle()

    // Find all widgets (return early if none found — widget may not have been placed)
    val widgets = try {
        findAllUtcClockWidgets(device)
    } catch (e: Exception) {
        Log.d(TAG, "removeUtcClockWidget: No widgets found to remove")
        return
    }

    Log.d(TAG, "removeUtcClockWidget: Found ${widgets.size} widget(s) to remove")

    for ((index, widget) in widgets.withIndex()) {
        val widgetBounds = widget.visibleBounds
        val widgetX = widgetBounds.centerX()
        val widgetY = widgetBounds.centerY()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                removeWidgetViaMotionEvents(device, instrumentation, widgetX, widgetY)
            } else {
                removeWidgetViaDrag(device, widgetX, widgetY)
            }
        } catch (e: Exception) {
            Log.e(TAG, "removeUtcClockWidget: ERROR removing widget ${index + 1}", e)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Try to release gesture if still holding
                try {
                    executeShellCommand(instrumentation, "input motionevent UP $widgetX $widgetY")
                    device.waitForIdle()
                } catch (releaseException: Exception) {
                    Log.e(TAG, "removeUtcClockWidget: Failed to release gesture", releaseException)
                }
            }
        }
    }
}

/**
 * Removes a widget using `device.drag()` for API < 29.
 * drag() performs a long-press at the start point, then drags to the end point.
 * We drag to the top center of the screen where "Remove" appears on the stock launcher.
 */
private fun removeWidgetViaDrag(device: UiDevice, widgetX: Int, widgetY: Int) {
    val removeX = device.displayWidth / 2
    val removeY = 100

    // drag() does: touch down → hold (long press) → move → lift
    // 500 steps ≈ 2.5s which is enough for long-press to trigger
    device.drag(widgetX, widgetY, removeX, removeY, 500)
    Thread.sleep(500)
    device.waitForIdle()
}

/**
 * Removes a widget using `input motionevent` shell commands for API 29+.
 */
private fun removeWidgetViaMotionEvents(
    device: UiDevice,
    instrumentation: Instrumentation,
    widgetX: Int,
    widgetY: Int
) {
    // Step 1: Long-press on widget
    executeShellCommand(instrumentation, "input motionevent DOWN $widgetX $widgetY")
    Thread.sleep(2500)
    device.waitForIdle()

    // Step 2: Look for "Remove" element
    val removeElement = findRemoveElement(device)

    if (removeElement == null) {
        Log.w(TAG, "removeWidgetViaMotionEvents: 'Remove' element not found, releasing gesture")
        executeShellCommand(instrumentation, "input motionevent UP $widgetX $widgetY")
        device.waitForIdle()
        return
    }

    val removeBounds = removeElement.visibleBounds
    val removeX = removeBounds.centerX()
    val removeY = removeBounds.centerY()

    // Step 3: Drag widget to "Remove" element
    dragToRemove(instrumentation, widgetX, widgetY, removeX, removeY)

    // Step 4: Release at "Remove" position
    executeShellCommand(instrumentation, "input motionevent UP $removeX $removeY")
    Thread.sleep(500)
    device.waitForIdle()
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
