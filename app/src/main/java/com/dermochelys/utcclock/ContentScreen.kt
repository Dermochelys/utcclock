package com.dermochelys.utcclock

import androidx.annotation.ColorInt
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dermochelys.utcclock.shared.R

private const val LICENSE_BUTTON_DIMMING_ALPHA = 0.8f

internal val dotoFont = FontFamily(Font(R.font.doto))

@Composable
@Preview
fun MainScreen(
    dateText: String = "Saturday\nJuly 05\n2025",
    timeText: String = "22:22",

    @ColorInt
    contentColor: Int = android.graphics.Color.WHITE,

    textSizeDate: Float = 70.sp.value,
    textSizeTime: Float = 110.sp.value,
    onFontLicenseButtonClick: () -> Unit = {},
    showingFontLicense: Boolean = false,
    showingDisclaimer: Boolean = false,
    onDisclaimerAgreeClick: () -> Unit = {},
    overlayPositionShift: Boolean = true,
    fontLicenseButtonAlignment: Alignment = Alignment.Center,
    textOrderDateFirst: Boolean = false,
    middleSprintWeight: Float = 1.0f,
    isOnTv: Boolean = true,
    ) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxWidth()
                    .padding(start = 10.dp, end = 10.dp, top = 5.dp)
            ) {
                IconButton(
                    onClick = if (showingDisclaimer) { { } } else { onFontLicenseButtonClick },
                    modifier = Modifier
                        .requiredSize(48.dp)
                        .align(fontLicenseButtonAlignment)
                        .clickable(
                            enabled = true,
                            onClickLabel = stringResource(R.string.show_license_description),
                            role = Role.Button,
                            onClick = if (showingDisclaimer) { { } } else { onFontLicenseButtonClick } )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.license_24px),
                        contentDescription = stringResource(R.string.licenses_description),
                        tint = Color.White.withAlpha(LICENSE_BUTTON_DIMMING_ALPHA),
                        modifier = Modifier.focusable(false)
                    )
                }
            }

            Text(
                text = if (textOrderDateFirst) { dateText } else { timeText },
                color = Color.White,
                fontSize = if (textOrderDateFirst) { textSizeDate } else { textSizeTime }.sp,
                textAlign = TextAlign.Center,
                fontFamily = dotoFont,
                modifier = Modifier.fillMaxWidth().padding(start = 13.dp)
            )

            Spacer(modifier = Modifier.weight(middleSprintWeight).focusable(false))

            Text(
                text = if (!textOrderDateFirst) { dateText } else { timeText },
                color = Color.White,
                fontSize = if (!textOrderDateFirst) { textSizeDate } else { textSizeTime }.sp,
                textAlign = TextAlign.Center,
                fontFamily = dotoFont,
                modifier = Modifier.fillMaxWidth().padding(start = 13.dp)
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        // Overlay pattern
        val image = ImageBitmap.imageResource(R.drawable.overlay_bitmap)

        val brush = remember(image) {
            ShaderBrush(ImageShader(image, TileMode.Repeated, TileMode.Repeated))
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (overlayPositionShift) 1.dp else 0.dp, 0.dp, 0.dp, 0.dp)
                .background(brush),
        )

        // Font license overlay
        if (showingFontLicense) {
            FontLicense(contentColor)
        }

        // App license overlay
        if (showingDisclaimer) {
            if (isOnTv) {
                TvDisclaimer(onDisclaimerAgreeClick)
            } else {
                Disclaimer(onDisclaimerAgreeClick)
            }
        }
    }
}
