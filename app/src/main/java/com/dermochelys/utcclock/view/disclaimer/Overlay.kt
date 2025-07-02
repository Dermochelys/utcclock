package com.dermochelys.utcclock.view.disclaimer

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp

@Composable
fun Overlay(
    overlayBitmap: Bitmap,
    overlayPositionShift: Boolean
) {
    val brush = remember(overlayBitmap) {
        ShaderBrush(
            ImageShader(
                overlayBitmap.asImageBitmap(),
                TileMode.Repeated,
                TileMode.Repeated
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(if (overlayPositionShift) 1.dp else 0.dp, 0.dp, 0.dp, 0.dp)
            .background(brush)
    )
}
