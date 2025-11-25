package com.dermochelys.utcclock.view.disclaimer

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.dermochelys.utcclock.R

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
@Preview
fun TvDisclaimer(
    onDisclaimerAgreeClick: () -> Unit = {},
    overlayPositionShift: Boolean = false,
    overlayBitmap: Bitmap? = null,
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(top = 20.dp, start = 20.dp, end = 20.dp, bottom = 0.dp)
            .background(Color.Black),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.weight(1.0f)
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_contract),
                contentDescription = stringResource(R.string.contract_description),
                tint = colorResource(R.color.blue),
                modifier = Modifier.size(36.dp)
            )

            BasicText(
                text = stringResource(R.string.disclaimer_text),
                autoSize = TextAutoSize.StepBased(minFontSize = 8.sp, maxFontSize = 100.sp, stepSize = 1.sp),
                style = TextStyle(color = Color.White),
                modifier = Modifier.fillMaxSize()
            )
        }

        Button(
            onClick = onDisclaimerAgreeClick,
            colors = ButtonDefaults.colors(containerColor = Color.DarkGray, focusedContainerColor = Color.White),
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .padding(top = 20.dp, bottom = 20.dp)
                .clickable(
                    enabled = true,
                    onClickLabel = stringResource(R.string.i_agree),
                    role = Role.Button,
                    onClick = onDisclaimerAgreeClick,
                )
                .focusRequester(focusRequester)

        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.wrapContentHeight()
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_check_mark),
                    contentDescription = stringResource(R.string.checkmark_description),
                    tint = Color.Black,
                    modifier = Modifier
                        .requiredSize(36.dp)
                        .padding(start = 10.dp)
                        .align(Alignment.CenterVertically)
                )

                Text(
                    text = stringResource(R.string.i_agree),
                    color = Color.Black,
                    fontSize = 30.sp,
                    modifier = Modifier
                        .padding(start = 10.dp, end = 10.dp)
                        .align(Alignment.CenterVertically)
                        .wrapContentHeight()
                )
            }
        }
    }

    overlayBitmap?.let { it -> Overlay(it, overlayPositionShift) }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}
