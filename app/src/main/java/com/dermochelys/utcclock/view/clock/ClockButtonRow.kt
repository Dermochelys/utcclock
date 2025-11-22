package com.dermochelys.utcclock.view.clock

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

private const val MIDDLE_SPRING_WEIGHT = 1.0f

@Composable
@Preview
fun ButtonRow(
    fontLicenseButtonAlignmentToStart: Boolean = true,
    onFontLicenseButtonClicked: () -> Unit = {},
    focusedButtonColor: Color = Color.Blue,
    contentColor: Color = Color.White,
    onDonationButtonClicked: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .wrapContentHeight()
            .padding(start = 10.dp, end = 10.dp, top = 5.dp)
            .focusProperties { canFocus = false } // Otherwise focus will not propagate to the buttons below
    ) {
        if (fontLicenseButtonAlignmentToStart) {
            FontLicenseButton(
                onNavigateToFontLicenseDialog = onFontLicenseButtonClicked,
                focusedColor = focusedButtonColor,
                unfocusedColor = contentColor
            )

            Spacer(modifier = Modifier.weight(MIDDLE_SPRING_WEIGHT))

            DonationButton(
                onNavigateToDonationDialog = onDonationButtonClicked,
                focusedColor = focusedButtonColor,
                unfocusedColor = contentColor
            )
        } else {
            DonationButton(
                onNavigateToDonationDialog = onDonationButtonClicked,
                focusedColor = focusedButtonColor,
                unfocusedColor = contentColor
            )

            Spacer(modifier = Modifier.weight(MIDDLE_SPRING_WEIGHT))

            FontLicenseButton(
                onNavigateToFontLicenseDialog = onFontLicenseButtonClicked,
                focusedColor = focusedButtonColor,
                unfocusedColor = contentColor
            )
        }
    }
}
