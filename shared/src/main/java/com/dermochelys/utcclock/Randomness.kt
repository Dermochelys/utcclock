package com.dermochelys.utcclock

import android.content.res.Resources
import android.graphics.Color
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.compose.ui.Alignment
import kotlin.random.Random


@ColorInt
fun getRandomContentColor(): Int = Color.argb(
    Random.nextInt(240, 256),
    Random.nextInt(210, 256),
    Random.nextInt(210, 256),
    Random.nextInt(210, 256)
)

fun Resources.getRandomDimension(idMin: Int, idMax: Int): Float =
    Random.nextDouble(getDimension(idMin).toDouble(), getDimension(idMax).toDouble()).toFloat()

fun Resources.getRandomValue(idMin: Int, idMax: Int): Float {
    val outValue = TypedValue()
    getValue(idMin, outValue, true)
    val min = outValue.float.toDouble()

    getValue(idMax, outValue, true)
    val max = outValue.float.toDouble()
    return Random.nextDouble(min, max).toFloat()
}

fun getRandomAlignment(): Alignment =
    when (Random.nextInt(1, 4)) {
        1 -> Alignment.CenterStart
        2 -> Alignment.Center
        else -> Alignment.CenterEnd
    }
