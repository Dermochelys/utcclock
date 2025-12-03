package com.dermochelys.utcclock.widget.util

import android.app.Instrumentation
import android.os.Build
import androidx.annotation.CheckResult
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2

internal const val MAX_SCROLL_ATTEMPTS = 10

private const val WIDGET_PICKER_PACKAGE = "com.android.launcher3.widgetpicker"

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
 * Adds widget using the API 31+ flow:
 * 1. Find "UTC Clock" text in widget chooser
 * 2. Tap "UTC Clock" text to expand
 * 3. Long-press widget preview
 * 4. Drag upward
 */
private fun addUtcClockWidgetApi31Plus(device: UiDevice,
                                       instrumentation: Instrumentation) {
    // Find and tap the "UTC Clock" app title
    val utcClockAppTitle = findWidgetHeaderViewInWidgetPicker31Plus(device)
        ?: throw Exception("UTC Clock text not found in widget picker")

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
        // Strategy 1: Find by app_title resource ID (API 31-35)
        var appTitleElements = findAllRes(device, RES_APP_TITLE, true)
        if (appTitleElements.isEmpty()) {
            appTitleElements = findClass(device, CLASS_TEXT_VIEW)
                .filter { it.resourceName?.contains(RES_APP_TITLE) == true }
        }

        val utcClockByRes = appTitleElements.firstOrNull { it.text == UTC_CLOCK }
        if (utcClockByRes != null) {
            ensureVisible(device, utcClockByRes)
            return utcClockByRes
        }

        // Strategy 2: Find by text directly (API 36+ uses different resource IDs)
        val textMatch = findAllText(device, UTC_CLOCK)
            .firstOrNull { it.className == CLASS_TEXT_VIEW }
        if (textMatch != null) {
            ensureVisible(device, textMatch)
            return textMatch
        }

        // Scroll down for more
        scrollDownInPicker(device)
        scrollAttempts++
    }

    return null
}

/**
 * Finds the widget preview element to long-press and drag.
 * Tries multiple strategies as the resource IDs and packages vary across API levels.
 */
private fun findWidgetPreview(device: UiDevice): UiObject2? {
    var scrollAttempts = 0

    while (scrollAttempts < MAX_SCROLL_ATTEMPTS) {
        // Strategy 1: Find by widget_preview resource ID in the widget picker package (API 36+)
        device.findObject(By.res("$WIDGET_PICKER_PACKAGE:id/$RES_WIDGET_PREVIEW"))?.let { return it }

        // Strategy 2: Find WidgetCell class then widget_preview_container resource (API 31-35)
        val widgetCells = waitForLauncherClass(device, "com.android.launcher3.widget.WidgetCell", TIMEOUT_1S_IN_MS)
        if (!widgetCells.isNullOrEmpty()) {
            findRes(device, RES_WIDGET_PREVIEW_CONTAINER, true)?.let { return it }
            findRes(device, RES_WIDGET_PREVIEW, true)?.let { return it }
            return widgetCells.first()
        }

        // Strategy 3: Look for content description containing widget size info (API 36+)
        val descMatches = waitForContentDescription(device, UTC_CLOCK, TIMEOUT_1S_IN_MS)
        val previewMatch = descMatches?.firstOrNull {
            it.className != CLASS_TEXT_VIEW
        }
        if (previewMatch != null) return previewMatch

        scrollDownInPicker(device)
        scrollAttempts++
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
