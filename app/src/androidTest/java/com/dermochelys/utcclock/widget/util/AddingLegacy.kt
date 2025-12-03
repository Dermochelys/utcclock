package com.dermochelys.utcclock.widget.util

import androidx.annotation.CheckResult
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2

/**
 * Legacy widget addition utilities for API < 31.
 */

/**
 * Adds widget using the legacy flow (API < 31):
 * Long-press on widget preview and release to add directly (no expansion needed)
 */
internal fun addUtcClockWidgetLegacy(device: UiDevice) {
    // Find the widget by looking for "UTC Clock" text
    var widgetNameLabel: UiObject2 = findWidgetNameViewInWidgetPickerLegacy(device) ?:
        throw Exception("addUtcClockWidgetLegacy failed to find widget name view")

    // Find widget_preview and ensure it's a WidgetImageView and fully visible
    var widgetPreview: UiObject2? = null
    var previewScrollAttempts = 0
    val maxPreviewScrollAttempts = 5

    while (widgetPreview == null && previewScrollAttempts < maxPreviewScrollAttempts) {
        widgetPreview = findWidgetPreviewLegacy(widgetNameLabel)

        if (widgetPreview != null) {
            // Verify it's actually a WidgetImageView
            val className = widgetPreview.className

            widgetPreview = if (!className.contains("WidgetImageView", ignoreCase = true) &&
                className.contains("LinearLayout", ignoreCase = true)) {
                null
            } else {
                ensureWidgetPreviewVisibleLegacy(device, widgetPreview)
            }
        }

        // If preview not found or not valid, scroll down to expose more
        if (widgetPreview == null) {
            scrollDownInPicker(device)

            // Re-find widget_name after scrolling
            widgetNameLabel = refindWidgetNameLegacy(device)
            previewScrollAttempts++
        }
    }

    // Final verification
    if (widgetPreview == null || widgetPreview.className.contains("LinearLayout", ignoreCase = true)) {
        throw AssertionError("widget_preview (WidgetImageView) not found or not fully visible after scrolling")
    }

    // Final check: ensure preview is not obscured by nav bar
    widgetPreview = ensureWidgetPreviewVisibleLegacy(device, widgetPreview)

    // Long press on widget preview (hold for 3 seconds)
    val visibleCenter = widgetPreview.visibleBounds

    device.swipe(visibleCenter.centerX(), visibleCenter.centerY(),
        visibleCenter.centerX(), visibleCenter.centerY(), 150)

    // Wait for widget to be placed on home screen
    Thread.sleep(2000)
    device.waitForIdle()
}

/**
 * Finds the widget_name TextView with text "UTC Clock".
 */
private fun findWidgetNameViewInWidgetPickerLegacy(device: UiDevice): UiObject2? {
    var scrollAttempts = 0

    while (scrollAttempts < MAX_SCROLL_ATTEMPTS) {
        // Find by resource ID "widget_name" with text "UTC Clock"
        val widgetNameElements = waitForRes(device, RES_WIDGET_NAME, true, TIMEOUT_5S_IN_MS)

        if (widgetNameElements.isNullOrEmpty()) {
            throw Exception("$RES_WIDGET_NAME was not found")
        }

        val widgetNameView = widgetNameElements.firstOrNull { it.text == UTC_CLOCK && !isSectionHeader(it.resourceName) }
        if (widgetNameView != null) {
            return widgetNameView
        }

        // Scroll down to reveal more widgets
        scrollDownInPicker(device)
        scrollAttempts++
    }

    return null
}

/**
 * Finds the widget_preview from the widget_name.
 */
private fun findWidgetPreviewLegacy(widgetNameView: UiObject2): UiObject2? {
    val container = widgetNameView.parent.parent
    val allChildren = container.children

    return allChildren.firstOrNull { child ->
        child.resourceName?.contains(RES_WIDGET_PREVIEW) == true
    }
}

/**
 * Ensures the widget preview is not obscured by the navigation bar.
 * Scrolls if necessary and returns the updated preview element.
 */
@CheckResult
private fun ensureWidgetPreviewVisibleLegacy(device: UiDevice, widgetPreview: UiObject2): UiObject2 {
    val navBarHeight = 200
    val safeBottomLimit = device.displayHeight - navBarHeight

    var preview = widgetPreview
    val previewY = preview.visibleBounds.centerY()

    if (previewY > safeBottomLimit) {
        // Scroll down to move preview into safe viewing area
        scrollDownInPicker(device)

        // Re-find widget_preview after scrolling
        val updatedWidgetNameView = findTextWithResName(device, UTC_CLOCK, RES_WIDGET_NAME)

        if (updatedWidgetNameView != null) {
            val updatedPreview = findWidgetPreviewLegacy(updatedWidgetNameView)
            if (updatedPreview != null) {
                preview = updatedPreview
            }
        }
    }

    return preview
}

/**
 * Re-finds widget_name after scrolling for legacy APIs.
 */
private fun refindWidgetNameLegacy(device: UiDevice): UiObject2 {
    val utcClockElements = findAllText(device, UTC_CLOCK)

    return utcClockElements.firstOrNull { elem ->
        elem.resourceName?.contains(RES_WIDGET_NAME) == true && !isSectionHeader(elem.resourceName)
    } ?: utcClockElements.firstOrNull { elem ->
        elem.className.contains(CLASS_TEXT_VIEW, ignoreCase = true) && !isSectionHeader(elem.resourceName)
    } ?: throw Exception("refindWidgetNameLegacy failed to find widget name")
}
