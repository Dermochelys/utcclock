package com.dermochelys.utcclock

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dermochelys.utcclock.shared.R

@Composable
@Preview
fun DisclaimerContent() {
    Icon(
        painter = painterResource(R.drawable.contract_24px),
        contentDescription = stringResource(R.string.contract_description),
        tint = colorResource(R.color.blue),
        modifier = Modifier.Companion
            .size(36.dp)
    )

    Text(
        text = stringResource(R.string.disclaimer),
        color = Color.Companion.White,
        fontSize = dimensionResource(R.dimen.disclaimer_font_size).value.sp,
    )
}