package com.dermochelys.utcclock

import androidx.annotation.ColorInt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val LICENSE_BUTTON_DIMMING_ALPHA = 0.8f

private val dotoFont = FontFamily(Font(com.dermochelys.utcclock.shared.R.font.doto))

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
    showingAppLicense: Boolean = false,
    onAgreeClick: () -> Unit = {},
    overlayPositionShift: Boolean = true,
    fontLicenseButtonAlignment: Alignment = Alignment.Center,
    textOrderDateFirst: Boolean = false,
    middleSprintWeight: Float = 1.0f,
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
                    onClick = onFontLicenseButtonClick,
                    modifier = Modifier
                        .requiredSize(48.dp)
                        .align(fontLicenseButtonAlignment)
                ) {
                    Icon(
                        painter = painterResource(com.dermochelys.utcclock.shared.R.drawable.license_24px),
                        contentDescription = stringResource(com.dermochelys.utcclock.shared.R.string.licenses_description),
                        tint = Color.White.withAlpha(LICENSE_BUTTON_DIMMING_ALPHA),
                    )
                }
            }

            Text(
                text = if (textOrderDateFirst) { dateText } else { timeText },
                color = Color.White,
                fontSize = if (textOrderDateFirst) { textSizeDate } else { textSizeTime }.sp,
                textAlign = TextAlign.Center,
                fontFamily = dotoFont,
                modifier = Modifier.fillMaxWidth().padding(start = 13.dp),
            )

            Spacer(modifier = Modifier.weight(middleSprintWeight))

            Text(
                text = if (!textOrderDateFirst) { dateText } else { timeText },
                color = Color.White,
                fontSize = if (!textOrderDateFirst) { textSizeDate } else { textSizeTime }.sp,
                textAlign = TextAlign.Center,
                fontFamily = dotoFont,
                modifier = Modifier.fillMaxWidth().padding(start = 13.dp),
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        // Overlay pattern
        val image = ImageBitmap.imageResource(com.dermochelys.utcclock.shared.R.drawable.overlay_bitmap)

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
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .background(contentColor.toColor())
                        .padding(horizontal = 10.dp, vertical = 30.dp)
                        .wrapContentHeight(Alignment.CenterVertically),

                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SelectionContainer {
                        Text(
                            text = buildAnnotatedString {
                                append(stringResource(com.dermochelys.utcclock.shared.R.string.license_text))

                                val licenseUrl =
                                    stringResource(com.dermochelys.utcclock.shared.R.string.license_url)

                                val link =
                                    LinkAnnotation.Url(
                                        licenseUrl,
                                        TextLinkStyles(SpanStyle(color = colorResource(
                                            com.dermochelys.utcclock.shared.R.color.blue)))
                                    )

                                withLink(link) { append(licenseUrl) }
                            },
                            color = Color.Black,
                            fontFamily = dotoFont,
                            textAlign = TextAlign.Center
                        )
                    }

                    Image(
                        painter = painterResource(com.dermochelys.utcclock.shared.R.drawable.doto_license_qr),
                        contentDescription = stringResource(com.dermochelys.utcclock.shared.R.string.qr_description),
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .size(100.dp)
                    )
                }
            }
        }

        // App license overlay
        if (showingAppLicense) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .padding(top = 15.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp),
                ) {
                    Icon(
                        painter = painterResource(com.dermochelys.utcclock.shared.R.drawable.contract_24px),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Text(
                        text = stringResource(com.dermochelys.utcclock.shared.R.string.disclaimer),
                        color = Color.White,
                        fontSize = dimensionResource(com.dermochelys.utcclock.shared.R.dimen.disclaimer_font_size).value.sp,
                    )
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onAgreeClick() },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .defaultMinSize(minHeight = 48.dp)
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(com.dermochelys.utcclock.shared.R.drawable.task_alt_24px),
                            contentDescription = stringResource(com.dermochelys.utcclock.shared.R.string.checkmark_description),
                            tint = Color(0xFF00C853),
                            modifier = Modifier.padding(end = 10.dp)
                        )

                        Text(
                            text = stringResource(com.dermochelys.utcclock.shared.R.string.i_agree),
                            color = Color.Black,
                        )
                    }
                }
            }
        }
    }
}
