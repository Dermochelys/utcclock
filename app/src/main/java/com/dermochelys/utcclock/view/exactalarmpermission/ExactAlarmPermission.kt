package com.dermochelys.utcclock.view.exactalarmpermission

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dermochelys.utcclock.R

@Composable
@Preview
fun ExactAlarmPermission(
    onOpenSettingsClicked: () -> Unit = {},
    onNotNowClicked: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.exact_alarm_permission_explanation),
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onOpenSettingsClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp),
            ) {
                Text(
                    text = stringResource(R.string.open_settings),
                    color = Color.Black,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onNotNowClicked) {
                Text(
                    text = stringResource(R.string.not_now),
                    color = Color.White,
                )
            }
        }
    }
}
