package com.dermochelys.utcclock.view

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dermochelys.utcclock.shared.R

@Composable
@Preview
fun FontLicenseButton(
    onNavigateToFontLicenseDialog: () -> Unit = {},
    focusedColor: Color = Color.Blue,
    unfocusedColor: Color = Color.White,
) {
    ClockButton(
        onButtonClicked = onNavigateToFontLicenseDialog,
        focusedColor = focusedColor,
        unfocusedColor = unfocusedColor,
        drawableId = R.drawable.ic_license,
        onClickDescriptionId = R.string.font_license_dialog_description,
        drawableDescriptionId = R.string.font_license_icon_description,
    )
}

@Composable
@Preview
fun DonationButton(
    onNavigateToDonationDialog: () -> Unit = {},
    focusedColor: Color = Color.DarkGray,
    unfocusedColor: Color = Color.White,
) {
    ClockButton(
        onButtonClicked = onNavigateToDonationDialog,
        focusedColor = focusedColor,
        unfocusedColor = unfocusedColor,
        drawableId = R.drawable.ic_turtle,
        onClickDescriptionId = R.string.donation_dialog_description,
        drawableDescriptionId = R.string.turtle_icon_description,
    )
}

// Helpers

@Composable
private fun ClockButton(onButtonClicked: () -> Unit,
                        focusedColor: Color,
                        unfocusedColor: Color,
                        @DrawableRes drawableId: Int,
                        @StringRes onClickDescriptionId: Int,
                        @StringRes drawableDescriptionId: Int,
) {
    val color = remember { mutableStateOf(unfocusedColor) }

    Icon(
        painter = painterResource(drawableId),
        contentDescription = stringResource(drawableDescriptionId),
        tint = color.value,
        modifier = Modifier
            .size(48.dp)
            .padding(8.dp)
            .clickable(
                enabled = true,
                onClickLabel = stringResource(onClickDescriptionId),
                role = Role.Button,
                onClick = onButtonClicked,
            )
            .onFocusChanged { color.value = if (it.isFocused) { focusedColor } else { unfocusedColor } }
            .focusable()
    )
}
