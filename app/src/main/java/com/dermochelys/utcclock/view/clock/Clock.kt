package com.dermochelys.utcclock.view.clock

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dermochelys.utcclock.R
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

    // These are randomly altered inputs
    contentColor: Color = Color.White,
    overlayPositionShift: Boolean = true,
    fontLicenseButtonAlignmentToStart: Boolean = true,
    buttonRowTop: Boolean = false,
    dateTextAlignToStart: Boolean = false,
    textOrderDateFirst: Boolean = true,

    zonedDateTime: Pair<Date, TimeZone> = Pair(Date(), TimeZone.getDefault())
) {
    val configuration = LocalConfiguration.current
    val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
    // Adjust formats based on orientation
    val dateFormat = if (isPortrait) "EEEE\nMMMM dd\nyyyy" else "EEEE\nMMMM dd yyyy"
    val timeFormat = if (isPortrait) "HH:mm\nz" else "HH:mm z"
    // Adjust maxLines based on orientation
    val dateMaxLines = if (isPortrait) 3 else 2
    val timeMaxLines = if (isPortrait) 2 else 1
    val simpleDateFormat = remember(dateFormat) { SimpleDateFormat(dateFormat, Locale.US) }
    val simpleTimeFormat = remember(timeFormat) { SimpleDateFormat(timeFormat, Locale.US) }
    val dateText = remember(simpleDateFormat, key2 = zonedDateTime) { zonedDateTime.formatted(simpleDateFormat) }
    val timeText = remember(simpleTimeFormat, key2 = zonedDateTime) { zonedDateTime.formatted(simpleTimeFormat) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(start = 13.dp, end = 13.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (buttonRowTop) {
                ButtonRow(
                    fontLicenseButtonAlignmentToStart = fontLicenseButtonAlignmentToStart,
                    onFontLicenseButtonClicked = onFontLicenseButtonClicked,
                    focusedButtonColor = focusedButtonColor,
                    contentColor = contentColor,
                    onDonationButtonClicked = onDonationButtonClicked,
                )
            }

            Spacer(modifier = Modifier.weight(middleSpringWeight(isPortrait)))

            Box(
                modifier = Modifier
                    .weight(getWeight( isFirstText = true,
                        textOrderDateFirst = textOrderDateFirst,
                        isPortrait = isPortrait
                    ))
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = if (textOrderDateFirst) { dateText } else { timeText },
                    autoSize = autoSize(),
                    maxLines = if (textOrderDateFirst) { dateMaxLines } else { timeMaxLines },
                    style = TextStyle(
                        color = contentColor,
                        fontFamily = dotoFont,
                        textAlign = getTextAlignment(isFirstText = true,
                            textOrderDateFirst = textOrderDateFirst,
                            dateTextAlignToStart = dateTextAlignToStart
                        ),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(middleSpringWeight(isPortrait)))

            Box(
                modifier = Modifier
                    .weight(getWeight(isFirstText = false,
                        textOrderDateFirst = textOrderDateFirst,
                        isPortrait = isPortrait
                    ))
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                BasicText(
                    text = if (textOrderDateFirst) { timeText } else { dateText },
                    autoSize = autoSize(),
                    maxLines = if (textOrderDateFirst) { timeMaxLines } else { dateMaxLines },
                    style = TextStyle(
                        color = contentColor,
                        fontFamily = dotoFont,
                        textAlign = getTextAlignment(
                            isFirstText = false,
                            textOrderDateFirst = textOrderDateFirst,
                            dateTextAlignToStart = dateTextAlignToStart
                        ),
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.weight(middleSpringWeight(isPortrait)))

            if (!buttonRowTop) {
                ButtonRow(
                    fontLicenseButtonAlignmentToStart = fontLicenseButtonAlignmentToStart,
                    onFontLicenseButtonClicked = onFontLicenseButtonClicked,
                    focusedButtonColor = focusedButtonColor,
                    contentColor = contentColor,
                    onDonationButtonClicked = onDonationButtonClicked
                )
            }
        }

        overlayBitmap?.let { Overlay(it, overlayPositionShift) }
    }
}

// Give the text views a bit more vertical room since vertical space is more limited in landscape
@Composable
private fun middleSpringWeight(isPortrait: Boolean): Float = if (isPortrait) 1.0f else 0.5f

@Composable
private fun autoSize(): TextAutoSize = TextAutoSize.StepBased(4.sp, 500.sp, 2.sp)

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

@Composable
private fun getWeight(
    isFirstText: Boolean,
    textOrderDateFirst: Boolean,
    isPortrait: Boolean,
): Float {
    val dateWeight = if (isPortrait) 2.0f else 1.5f
    val isDate = textOrderDateFirst == isFirstText
    return if (isDate) dateWeight else dateWeight * 2.0f
}
