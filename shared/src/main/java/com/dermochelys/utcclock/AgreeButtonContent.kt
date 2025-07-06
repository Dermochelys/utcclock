package com.dermochelys.utcclock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dermochelys.utcclock.shared.R
import kotlin.math.min

@Composable
@Preview
fun AgreeButtonContent() {
    Row(
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            painter = painterResource(R.drawable.task_alt_24px),
            contentDescription = stringResource(R.string.checkmark_description),
            tint = Color.Black,
            modifier = Modifier.Companion
                .requiredSize(36.dp)
                .padding(start = 10.dp)
                .align(Alignment.Companion.CenterVertically)
        )

        Text(
            text = stringResource(R.string.i_agree),
            color = Color.Black,
            fontSize = (min(
                dimensionResource(R.dimen.disclaimer_font_size).value + 2.0f,
                30.0f
            )).sp,
            modifier = Modifier.Companion
                .padding(start = 10.dp, end = 10.dp)
                .align(Alignment.Companion.CenterVertically)
        )
    }
}