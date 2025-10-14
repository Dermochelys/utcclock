package com.dermochelys.utcclock.view.clock

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dermochelys.utcclock.R
import com.dermochelys.utcclock.view.ButtonRow
import com.dermochelys.utcclock.view.common.formatted
import com.dermochelys.utcclock.view.disclaimer.Overlay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal val dotoFont = FontFamily(Font(R.font.doto))

@Composable
@Preview
fun Clock(
    focusedButtonColor: Color = Color.LightGray,

    onFontLicenseButtonClicked: () -> Unit = {},
    onDonationButtonClicked: () -> Unit = {},

    overlayBitmap: Bitmap? = null,

    // These are the randomly altered inputs
    contentColor: Color = Color.White,
    textSizeDate: Float = 50.sp.value,
    textSizeTime: Float = 110.sp.value,
    overlayPositionShift: Boolean = true,
    fontLicenseButtonAlignmentToStart: Boolean = true,
    buttonRowTop: Boolean = false,
    dateTextAlignToStart: Boolean = false,
    textOrderDateFirst: Boolean = true,
    middleSprintWeight: Float = 1.0f,
    zonedDateTime: Pair<Date, TimeZone> = Pair(Date(), TimeZone.getDefault()),
    timeFormat: String = "HH:mm\nz",
    dateFormat: String = "EEEE\nMMMM dd\nyyyy",
    ) {

    val simpleDateFormat = remember(dateFormat) { SimpleDateFormat(dateFormat, Locale.US) }
    val simpleTimeFormat = remember(timeFormat) { SimpleDateFormat(timeFormat, Locale.US) }

    val dateText = remember(simpleDateFormat, zonedDateTime) { zonedDateTime.formatted(simpleDateFormat) }
    val timeText = remember(simpleTimeFormat, zonedDateTime) { zonedDateTime.formatted(simpleTimeFormat) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column{
            if (buttonRowTop) {
                ButtonRow(
                    fontLicenseButtonAlignmentToStart,
                    onFontLicenseButtonClicked,
                    focusedButtonColor,
                    contentColor,
                    middleSprintWeight,
                    onDonationButtonClicked
                )
            }

            Spacer(modifier = Modifier.weight(middleSprintWeight))

            Text(
                text = if (textOrderDateFirst) { dateText} else { timeText },
                color = Color.White,
                fontSize = if (textOrderDateFirst) { textSizeDate } else { textSizeTime }.sp,
                textAlign = getTextAlignment(isFirstText = true, textOrderDateFirst, dateTextAlignToStart),
                fontFamily = dotoFont,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 13.dp, end = 13.dp)
            )

            Spacer(modifier = Modifier.weight(middleSprintWeight))

            Text(
                text = if (textOrderDateFirst) { timeText } else { dateText },
                color = Color.White,
                fontSize = if (!textOrderDateFirst) { textSizeDate } else { textSizeTime }.sp,
                textAlign = getTextAlignment(isFirstText = false, textOrderDateFirst, dateTextAlignToStart),
                fontFamily = dotoFont,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 13.dp, end = 3.dp)
            )

            Spacer(modifier = Modifier.weight(middleSprintWeight))

            if (!buttonRowTop) {
                ButtonRow(
                    fontLicenseButtonAlignmentToStart,
                    onFontLicenseButtonClicked,
                    focusedButtonColor,
                    contentColor,
                    middleSprintWeight,
                    onDonationButtonClicked
                )
            }
        }

        overlayBitmap?.let { it -> Overlay(it, overlayPositionShift) }
    }
}

@Composable
private fun getTextAlignment(
    isFirstText: Boolean,
    textOrderDateFirst: Boolean,
    dateTextAlignToStart: Boolean
): TextAlign = if (isFirstText && !textOrderDateFirst || !isFirstText && textOrderDateFirst) {
    TextAlign.Center
} else {
    if (dateTextAlignToStart) { TextAlign.Start } else { TextAlign.End }
}
