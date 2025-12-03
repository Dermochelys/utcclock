package com.dermochelys.utcclock.widget.internal

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.createBitmap
import com.dermochelys.utcclock.R

// Android remove view memory limit is approximately 12 MB, set the limit slightly smaller
// to account for overhead
const val MAX_BITMAP_BYTES = 10 * 1024 * 1024

/**
 * Calculates the expected bitmap size in bytes for the given dimensions.
 * Uses RGB_565 format (2 bytes per pixel).
 */
fun calculateBitmapSize(widthPx: Int, heightPx: Int): Int {
    return widthPx * heightPx * 2 // RGB_565 = 2 bytes per pixel
}

/**
 * Checks if a bitmap with the given dimensions would exceed the widget memory limit.
 */
fun wouldExceedMemoryLimit(widthPx: Int, heightPx: Int): Boolean {
    return calculateBitmapSize(widthPx, heightPx) > MAX_BITMAP_BYTES
}

/**
 * Renders text to a Bitmap using Android Canvas with the Doto font.
 * Automatically sizes the text to maximize usage of available space.
 * This allows custom fonts to be displayed in Glance widgets.
 */
fun renderTextToBitmap(
    context: Context,
    text: String,
    marginPx: Size,
    widthPx: Int,
    heightPx: Int,
    textColor: Color = Color.White,
    textAlign: TextAlign = TextAlign.Center,
): Bitmap {
    // Use RGB_565 bitmap format (2 bytes per pixel) to reduce memory usage by 50%
    // compared to ARGB_8888 (4 bytes per pixel). This prevents widget memory limit errors
    // on large widgets while maintaining good visual quality for text.
    val bitmap = createBitmap(widthPx, heightPx, Bitmap.Config.RGB_565)
    val canvas = Canvas(bitmap)

    // RGB_565 doesn't support transparency, so we need to draw the background color
    // Use white background for black text, black background for white text
    val backgroundColor = if (textColor == Color.Black) Color.White else Color.Black
    canvas.drawColor(backgroundColor.toArgb())

    // Load Doto font
    val typeface = ResourcesCompat.getFont(context, R.font.doto) ?: Typeface.MONOSPACE

    // Setup paint
    val paint = Paint().apply {
        this.typeface = typeface
        this.color = textColor.toArgb()
        this.isAntiAlias = true
    }

    // Split text into lines
    val lines = text.split("\n")

    val availableHeight = heightPx.toFloat() - 2.0f * marginPx.height

    // Limit first line height to a portion of overall available height
    val firstLineMaxHeight = availableHeight * 0.6f

    // Find the maximum font size that fits
    val firstLinefontSize = getFontSize(
        paint = paint,
        lines = lines.take(1),
        width = widthPx.toFloat() - 2.0f * marginPx.width,
        height = firstLineMaxHeight,
    )

    paint.textSize = firstLinefontSize
    var fontMetrics = paint.fontMetrics
    val firstLineHeight = fontMetrics.descent - fontMetrics.ascent

    // Find the maximum font size that fits
    val otherLinesFontSize = getFontSize(
        paint = paint,
        lines = lines.drop(1),
        width = widthPx.toFloat() - 2.0f * marginPx.width,
        height = heightPx.toFloat() - 2.0f * marginPx.height - firstLineHeight,
    )

    // Calculate vertical positioning to center text
    paint.textSize = otherLinesFontSize
    fontMetrics = paint.fontMetrics
    val otherLineHeight = fontMetrics.descent - fontMetrics.ascent

    // Calculate actual visual bounds for more accurate centering
    val firstLineBounds = Rect()
    val lastLineBounds = Rect()

    // Get bounds for first and last lines
    paint.textSize = firstLinefontSize
    paint.getTextBounds(lines[0], 0, lines[0].length, firstLineBounds)

    if (lines.size > 1) {
        paint.textSize = otherLinesFontSize
        paint.getTextBounds(lines[lines.size - 1], 0, lines[lines.size - 1].length, lastLineBounds)
    } else {
        lastLineBounds.set(firstLineBounds)
    }

    // Calculate visual height using actual bounds
    paint.textSize = firstLinefontSize
    val firstLineFontMetrics = paint.fontMetrics

    paint.textSize = otherLinesFontSize
    val otherLineFontMetrics = paint.fontMetrics

    // Visual height from top of first line to bottom of last line
    // Add extra spacing after first line (time) equal to vertical margin
    val firstLineExtraSpacing = if (lines.size > 1) marginPx.height else 0f

    val visualHeight = (-firstLineBounds.top) +
                       (if (lines.size > 1) (firstLineFontMetrics.descent - otherLineFontMetrics.ascent) else 0f) +
                       firstLineExtraSpacing +
                       otherLineHeight * (lines.size - 2).coerceAtLeast(0) +
                       lastLineBounds.bottom

    var y: Float = marginPx.height + (availableHeight - visualHeight) / 2 - firstLineBounds.top

    val offsetX: Float = marginPx.width

    lines.forEachIndexed { lineNumber, line ->
        val isFirstLine = lineNumber == 0

        paint.textSize = if (isFirstLine) firstLinefontSize else otherLinesFontSize
        fontMetrics = paint.fontMetrics

        // First line (time) is center-aligned, other lines use specified alignment
        val lineAlign = if (isFirstLine) Paint.Align.CENTER else {
            when (textAlign) {
                TextAlign.Start, TextAlign.Left -> Paint.Align.LEFT
                TextAlign.End, TextAlign.Right -> Paint.Align.RIGHT
                else -> Paint.Align.CENTER
            }
        }

        val x: Float = when (lineAlign) {
            Paint.Align.LEFT -> offsetX
            Paint.Align.RIGHT -> widthPx.toFloat() - offsetX
            else -> widthPx / 2f
        }

        val previousAlign = paint.textAlign
        paint.textAlign = lineAlign
        canvas.drawText(line, x, y, paint)
        paint.textAlign = previousAlign

        // Calculate spacing to next line accounting for font size changes

        val nextLineFontSize = if (lineNumber + 1 == 0) firstLinefontSize else otherLinesFontSize
        paint.textSize = nextLineFontSize
        val nextFontMetrics = paint.fontMetrics
        val baseSpacing = fontMetrics.descent - nextFontMetrics.ascent

        // Add extra spacing after the first line (time) equal to vertical margin
        val extraSpacing = if (isFirstLine) marginPx.height else 0f
        y += baseSpacing + extraSpacing
    }

    return bitmap
}
