package com.dermochelys.utcclock

import android.view.Window
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat.Type
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

fun Window.hideSystemUi() {
    val windowInsetsController = WindowCompat.getInsetsController(this, decorView)
    WindowCompat.setDecorFitsSystemWindows(this, false)

    windowInsetsController.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

    val types = getTypes()
    windowInsetsController.hide(types)
}

fun getTypes() = Type.systemBars() or Type.displayCutout() or Type.navigationBars() or Type.statusBars()
