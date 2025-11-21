package com.dermochelys.utcclock.widget.util

import android.app.Instrumentation
import android.os.Build
import androidx.annotation.CheckResult
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2

internal const val MAX_SCROLL_ATTEMPTS = 10

/**
 * Widget addition utilities for different Android API levels.
 */

/**
 * Finds and adds the UTC Clock widget to the home screen.
 * Assumes the widgets panel is already open.
 * Uses different flows based on API level:
 * - API 32+: Tap app name to expand, long-press widget preview and drag up
 * - API 31: Tap app name to expand, long-press widget preview and release
 * - API < 31: Long-press widget preview and release (no expansion needed)
 */
fun addUtcClockWidget(device: UiDevice,
                      instrumentation: Instrumentation) {
    openWidgetsPanel(device)

    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {  // API >= 31
            addUtcClockWidgetApi31Plus(device, instrumentation)
        }

        else -> {                                           // API < 31
            addUtcClockWidgetLegacy(device)
        }
    }
}

/**
 * Adds widget using the API 32+ flow:
 * 1. Find "UTC Clock" text in widget chooser
 * 2. Tap "UTC Clock" text to expand
 * 3. Long-press widget preview
 * 4. Drag upward
 */
private fun addUtcClockWidgetApi31Plus(device: UiDevice,
                                       instrumentation: Instrumentation) {
    // Find and tap the "UTC Clock" app title
    val utcClockAppTitle = findWidgetHeaderViewInWidgetPicker31Plus(device)
        ?: throw Exception("UTC Clock text (app_title) not found in widget picker")

    utcClockAppTitle.click()
    Thread.sleep(500)
    device.waitForIdle()

    val widgetPreview = findWidgetPreview(device)
        ?: throw Exception("widget_preview not found")

    // Ensure widget preview is not obscured by nav bar
    ensureVisible(device, widgetPreview)

    val previewBounds = widgetPreview.visibleBounds
    val previewX = previewBounds.centerX()
    val previewY = previewBounds.centerY()

    // Long-press and drag upward
    val displayHeight = device.displayHeight
    val originalDragEndY = displayHeight / 4
    val dragEndY = (previewY + originalDragEndY) / 2

    performLongPressAndDrag(device, instrumentation, previewX, previewY, previewX, dragEndY)

    // Brief wait for widget to be fully placed
    Thread.sleep(500)
    device.waitForIdle()
}

// Helper functions

/**
 * Finds the UTC Clock app title in the widget picker.
 * Returns the first match found.
 */
private fun findWidgetHeaderViewInWidgetPicker31Plus(device: UiDevice): UiObject2? {
    var scrollAttempts = 0

    while (scrollAttempts < MAX_SCROLL_ATTEMPTS) {
        var appTitleElements = findAllRes(device, RES_APP_TITLE, true)
        if (appTitleElements.isEmpty()) {
            appTitleElements = findClass(device, CLASS_TEXT_VIEW)
                .filter { it.resourceName?.contains(RES_APP_TITLE) == true }
        }

        val utcClockAppTitle = appTitleElements.firstOrNull { it.text == UTC_CLOCK }

        if (utcClockAppTitle != null) {
            ensureVisible(device, utcClockAppTitle)
            return utcClockAppTitle
        }

        // Scroll down for more
        scrollDownInPicker(device)
        scrollAttempts++
    }

    return null
}

/**
 * Finds the widget_preview via res ID
 */
private fun findWidgetPreview(device: UiDevice): UiObject2? {
    var scrollAttempts = 0

    while (scrollAttempts < MAX_SCROLL_ATTEMPTS) {
        val widgetCells = waitForLauncherClass(device, "com.android.launcher3.widget.WidgetCell", TIMEOUT_1S_IN_MS)

        if (widgetCells.isNullOrEmpty()) {
            // Scroll down to reveal more
            scrollDownInPicker(device)
            scrollAttempts++
            continue
        }

        findRes(device, RES_WIDGET_PREVIEW_CONTAINER, true)?.let { return it }
        scrollAttempts ++
    }

    return null
}

/**
 * Ensures the widget preview is not obscured by the navigation bar.
 * Scrolls if necessary and returns the updated preview element.
 */
@CheckResult
private fun ensureVisible(device: UiDevice, element: UiObject2) {
    val navBarHeight = 200
    val safeBottomLimit = device.displayHeight - navBarHeight
    var elementY = element.visibleBounds.centerY()

    while (elementY > safeBottomLimit) {
        // Scroll down to move preview into safe viewing area
        scrollDownInPicker(device)
        elementY = element.visibleBounds.centerY()
    }
}
