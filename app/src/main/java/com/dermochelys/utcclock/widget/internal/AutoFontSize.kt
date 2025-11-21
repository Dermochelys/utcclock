/*
 * Portions of this file are derived from AndroidX Compose Foundation TextAutoSize
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Source: https://github.com/androidx/androidx/blob/androidx-main/compose/foundation/foundation/src/commonMain/kotlin/androidx/compose/foundation/text/TextAutoSize.kt
 */

package com.dermochelys.utcclock.widget.internal

import android.graphics.Paint
import android.graphics.Rect
import kotlin.math.floor

/**
 * Binary search to find the maximum font size that fits within bounds.
 * Adapted from AndroidX Compose Foundation TextAutoSize.getFontSize()
 *
 * @see <a href="https://github.com/androidx/androidx/blob/androidx-main/compose/foundation/foundation/src/commonMain/kotlin/androidx/compose/foundation/text/TextAutoSize.kt">TextAutoSize.kt</a>
 */
fun getFontSize(
    paint: Paint,
    lines: List<String>,
    width: Float,
    height: Float,
    minFontSize: Float = 1.0f,
    maxFontSize: Float = 500.0f,
    stepSize: Float = 1.0f,
): Float {
    var min = minFontSize
    var max = maxFontSize
    var current = (min + max) / 2

    // Binary search for optimal font size
    while ((max - min) >= stepSize) {
        if (didOverflow(paint, lines, width, height, current)) {
            max = current
        } else {
            min = current
        }

        current = (min + max) / 2
    }

    // Snap to step size
    current = floor((min - minFontSize) / stepSize) * stepSize + minFontSize

    // Try one step larger if it still fits
    if ((current + stepSize) <= maxFontSize) {
        if (!didOverflow(paint, lines, width, height, current + stepSize)) {
            current += stepSize
        }
    }

    return current
}

/**
 * Check if the text overflows the given bounds at the specified font size
 */
private fun didOverflow(
    paint: Paint,
    lines: List<String>,
    width: Float,
    height: Float,
    fontSize: Float
): Boolean {
    paint.textSize = fontSize

    // Check width overflow
    val bounds = Rect()

    val maxWidth = lines.maxOfOrNull { line ->
        paint.getTextBounds(line, 0, line.length, bounds)
        bounds.width()
    } ?: return false

    if (maxWidth > width) return true

    // Check height overflow
    val fontMetrics = paint.fontMetrics
    val lineHeight = fontMetrics.descent - fontMetrics.ascent
    val totalHeight = lineHeight * lines.size

    return totalHeight > height
}