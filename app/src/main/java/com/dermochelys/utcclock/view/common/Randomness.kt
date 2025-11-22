package com.dermochelys.utcclock.view.common

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

fun getRandomContentColor(): Color = android.graphics.Color.argb(
    Random.nextInt(240, 256),
    Random.nextInt(210, 256),
    Random.nextInt(210, 256),
    Random.nextInt(210, 256)
).toColor()
