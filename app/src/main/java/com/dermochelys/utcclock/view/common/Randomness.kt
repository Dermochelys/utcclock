package com.dermochelys.utcclock.view.common

import android.content.res.Resources
import android.util.TypedValue
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

fun getRandomContentColor(): Color = android.graphics.Color.argb(
    Random.nextInt(240, 256),
    Random.nextInt(210, 256),
    Random.nextInt(210, 256),
    Random.nextInt(210, 256)
).toColor()

fun Resources.getRandomDimension(idMin: Int, idMax: Int): Float {
    val min = getDimension(idMin).toDouble()
    val max = getDimension(idMax).toDouble()
    if (min == max) return min.toFloat()
    return Random.nextDouble(min, max).toFloat()
}

fun Resources.getRandomValue(idMin: Int, idMax: Int): Float {
    val outValue = TypedValue()
    getValue(idMin, outValue, true)
    val min = outValue.float.toDouble()

    getValue(idMax, outValue, true)
    val max = outValue.float.toDouble()

    if (min == max) return min.toFloat()
    return Random.nextDouble(min, max).toFloat()
}
