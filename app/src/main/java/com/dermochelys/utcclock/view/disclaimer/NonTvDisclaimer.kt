package com.dermochelys.utcclock.view.disclaimer

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
@Preview
fun NonTvDisclaimer(
    onDisclaimerAgreeClick: () -> Unit = {},
    overlayPositionShift: Boolean = false,
    overlayBitmap: Bitmap? = null,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(top = 20.dp, start = 0.dp, end = 0.dp, bottom = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 60.dp, end = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier
                .weight(1.0f)
                .verticalScroll(enabled = true, state = rememberScrollState())
            ) {
                DisclaimerContent()
            }

            Button(
                onClick = onDisclaimerAgreeClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier
                    .defaultMinSize(minHeight = 48.dp)
                    .padding(vertical = 20.dp)
            ) {
                AgreeButtonContent()
            }
        }
    }

    overlayBitmap?.let { it -> Overlay(it, overlayPositionShift) }
}
