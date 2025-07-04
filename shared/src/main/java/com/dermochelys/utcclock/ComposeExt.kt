package com.dermochelys.utcclock

import android.graphics.ColorSpace
import android.util.Log
import androidx.annotation.ColorInt
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color

fun Alignment.toInt(): Int {
    return when (this) {
        Alignment.CenterStart, Alignment.BottomStart, Alignment.TopStart -> {
            -1
        }

        Alignment.Center, Alignment.TopCenter, Alignment.BottomCenter -> {
            0
        }

        Alignment.CenterEnd, Alignment.TopEnd, Alignment.BottomEnd -> {
            1
        }

        else -> {
            Log.e("Alignment", "Unknown type: $this, returning ${Alignment.Center}'s value")
            Alignment.Center.toInt()
        }
    }
}

fun Int.toAlignment(): Alignment {
    return when {
        this == -1 -> {
            Alignment.CenterStart
        }

        this == 0 -> {
            Alignment.Center
        }

        this == 1 -> {
            Alignment.CenterEnd
        }

        else -> {
            Log.e("Alignment", "Unknown type: $this, returning ${Alignment.Center}")
            Alignment.Center
        }
    }
}

fun Color.withAlpha(alpha: Float): Color {
    return Color(red, green, blue, alpha, colorSpace)
}

@ColorInt
fun Int.toColor(): Color {
    val r = ((this shr 16) and 0xff) / 255.0f
    val g = ((this shr 8) and 0xff) / 255.0f
    val b = ((this) and 0xff) / 255.0f
    val a = ((this shr 24) and 0xff) / 255.0f
    return Color(r, g, b, a)
}