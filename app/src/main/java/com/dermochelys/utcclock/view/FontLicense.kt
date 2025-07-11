package com.dermochelys.utcclock

import androidx.annotation.ColorInt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dermochelys.utcclock.shared.R

@Composable
@Preview
fun FontLicense(@ColorInt contentColor: Int = android.graphics.Color.WHITE) {
    Box(
        modifier = Modifier.Companion.fillMaxSize(),
        contentAlignment = Alignment.Companion.Center
    ) {
        Column(
            modifier = Modifier.Companion
                .background(contentColor.toColor())
                .padding(horizontal = 10.dp, vertical = 20.dp)
                .wrapContentHeight(Alignment.Companion.CenterVertically),

            horizontalAlignment = Alignment.Companion.CenterHorizontally
        ) {
            SelectionContainer {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.license_text))

                        val licenseUrl =
                            stringResource(R.string.license_url)

                        val link =
                            LinkAnnotation.Url(
                                licenseUrl,
                                TextLinkStyles(
                                    SpanStyle(
                                        color = colorResource(
                                            R.color.blue
                                        )
                                    )
                                )
                            )

                        withLink(link) { append(licenseUrl) }
                    },
                    color = Color.Companion.Black,
                    fontFamily = dotoFont,
                    textAlign = TextAlign.Companion.Center
                )
            }

            Image(
                painter = painterResource(R.drawable.doto_license_qr),
                contentDescription = stringResource(R.string.qr_description),
                contentScale = ContentScale.Companion.FillBounds,
                modifier = Modifier.Companion
                    .padding(top = 10.dp)
                    .size(100.dp)
            )
        }
    }
}