package com.dermochelys.utcclock.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import com.dermochelys.utcclock.shared.R

@Composable
@Preview
fun Landing() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.black))
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .wrapContentSize()
                .align(Alignment.Center),
            color = colorResource(R.color.medium_blue),
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
