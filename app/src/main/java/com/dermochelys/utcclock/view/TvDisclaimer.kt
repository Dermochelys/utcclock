package com.dermochelys.utcclock

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
@Preview
fun TvDisclaimer(onDisclaimerAgreeClick: () -> Unit = { }) {
    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(Color.Companion.Black)
            .padding(top = 20.dp, start = 0.dp, end = 0.dp, bottom = 0.dp)
            .focusable(false),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.Companion
                .verticalScroll(rememberScrollState())
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
                .weight(1.0f)
                .focusable(false)
        ) {
            DisclaimerContent()
        }

        Button(
            onClick = onDisclaimerAgreeClick,
            colors = ButtonDefaults.colors(containerColor = Color.DarkGray, focusedContainerColor = Color.White),
            enabled = true,
            modifier = Modifier.Companion
                .defaultMinSize(minHeight = 48.dp)
                .padding(top = 15.dp, bottom = 30.dp)
        ) {
            AgreeButtonContent()
        }
    }
}
