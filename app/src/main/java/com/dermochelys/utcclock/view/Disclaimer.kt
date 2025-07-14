package com.dermochelys.utcclock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dermochelys.utcclock.shared.R

@Composable
@Preview
fun Disclaimer(onDisclaimerAgreeClick: () -> Unit = { },
               ) {
    val verticalPadding = dimensionResource(R.dimen.disclaimer_vertical_padding).value
    val horizontalPadding = dimensionResource(R.dimen.disclaimer_horizontal_padding).value

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .background(Color.Companion.Black)
            .padding(top = verticalPadding.dp, start = 0.dp, end = 0.dp, bottom = 0.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.Companion
                .verticalScroll(rememberScrollState())
                .padding(start = horizontalPadding.dp, end = horizontalPadding.dp)
                .weight(1.0f)
        ) {
            DisclaimerContent()
        }

        Button(
            onClick = onDisclaimerAgreeClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Companion.White),
            modifier = Modifier.Companion.defaultMinSize(minHeight = 48.dp).padding(vertical = 20.dp)
        ) {
            AgreeButtonContent()
        }
    }
}
