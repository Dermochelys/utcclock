package com.dermochelys.utcclock

import androidx.annotation.ColorInt
import androidx.compose.ui.graphics.Color

fun @receiver:ColorInt Int.toColor(): Color {
    val r = ((this shr 16) and 0xff) / 255.0f
    val g = ((this shr 8) and 0xff) / 255.0f
    val b = ((this) and 0xff) / 255.0f
    val a = ((this shr 24) and 0xff) / 255.0f
    return Color(r, g, b, a)
}