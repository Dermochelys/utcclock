package com.dermochelys.utcclock.view.disclaimer

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dermochelys.utcclock.R

@Composable
@Preview
fun DisclaimerContent() {
    Column {
        Icon(
            painter = painterResource(R.drawable.ic_contract),
            contentDescription = stringResource(R.string.contract_description),
            tint = colorResource(R.color.blue),
            modifier = Modifier.size(36.dp)
        )

        BasicText(
            text = stringResource(R.string.disclaimer_text),
            autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 100.sp, stepSize = 1.sp),
            style = TextStyle(
                textAlign = TextAlign.Justify,
                color = Color.White,
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}
