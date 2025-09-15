package com.dermochelys.utcclock.view.fontlicense

import androidx.annotation.ColorInt
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
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
import com.dermochelys.utcclock.toColor
import com.dermochelys.utcclock.view.clock.dotoFont

@Composable
@Preview
fun FontLicense(@ColorInt contentColor: Int = android.graphics.Color.WHITE) {
    Box(
        modifier = Modifier.wrapContentSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .background(contentColor.toColor())
                .padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    append(stringResource(R.string.font_license_text))

                    val licenseUrl =
                        stringResource(R.string.font_license_url)

                    val textLinkStyles = TextLinkStyles(
                        SpanStyle(color = colorResource(R.color.blue))
                    )

                    val link = LinkAnnotation.Url(licenseUrl, textLinkStyles)
                    withLink(link) { append(licenseUrl) }
                },
                color = Color.Black,
                fontFamily = dotoFont,
                textAlign = TextAlign.Center
            )

            Image(
                painter = painterResource(R.drawable.qr_font_license),
                contentDescription = stringResource(R.string.qr_description),
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(128.dp)
            )
        }
    }
}
