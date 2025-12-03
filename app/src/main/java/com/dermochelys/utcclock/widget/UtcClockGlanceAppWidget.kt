package com.dermochelys.utcclock.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.glance.text.TextAlign.Companion.Center
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.dermochelys.utcclock.Activity
import com.dermochelys.utcclock.R
import com.dermochelys.utcclock.di.WidgetEntryPoint
import com.dermochelys.utcclock.widget.internal.renderTextToBitmap
import com.dermochelys.utcclock.widget.internal.wouldExceedMemoryLimit
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


object UtcClockGlanceAppWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext, WidgetEntryPoint::class.java
        )

        val shouldShowDisclaimerFlow = hiltEntryPoint.disclaimerRepository().shouldShowDisclaimer()
        val zonedDateFlow = hiltEntryPoint.zonedDateRepository().zonedDateFlow()
        // Sync permission state with system on each provideGlance call so alarm-triggered
        // updates pick up permission changes (e.g. user granted via system settings from widget)
        hiltEntryPoint.permissionsRepository().onExactAlarmPermissionChanged()
        val canScheduleExactAlarmsFlow = hiltEntryPoint.permissionsRepository().canScheduleExactAlarms()

        // Wait for initial values before providing content.
        // In steady-state operation, the widget is stateless so to avoid a temporary loading state
        // just wait for the values to load before proceeding with the view update.
        val disclaimerState = shouldShowDisclaimerFlow.first()
        val dateTimeState = zonedDateFlow.first()
        val canScheduleExactState = canScheduleExactAlarmsFlow.first()

        // On widget first creation, however, the widget is allowed to stay alive temporarily and
        // actually must do its own updating as Widget#updateAll will not trigger a #provideGlance
        // call while it is still alive.  Hence this hybrid approach where flows are collected
        // inside provideContent to enable recomposition when values change.

        provideContent {
            GlanceTheme {
                WidgetContent(
                    shouldShowDisclaimerFlow,
                    zonedDateFlow,
                    canScheduleExactAlarmsFlow,
                    disclaimerState,
                    dateTimeState,
                    canScheduleExactState)
            }
        }
    }

    override val sizeMode: SizeMode
        get() = SizeMode.Exact
}

@Composable
private fun WidgetContent(
    shouldShowDisclaimer: Flow<Boolean>,
    zonedDateTime: Flow<Pair<Date, TimeZone>>,
    canScheduleExactAlarms: Flow<Boolean>,
    initialDisclaimerState: Boolean,
    initialDateTimeState: Pair<Date, TimeZone>,
    initialCanScheduleExact: Boolean,
) {
    // Continue observing flows for updates (tracked state — triggers recomposition on change)
    val disclaimerState by shouldShowDisclaimer.collectAsState(initialDisclaimerState)
    val dateTimeState by zonedDateTime.collectAsState(initialDateTimeState)
    val canScheduleExact by canScheduleExactAlarms.collectAsState(initialCanScheduleExact)

    if (disclaimerState) {
        DisclaimerPrompt()
    } else if (!canScheduleExact) {
        ExactAlarmPermissionPrompt()
    } else {
        ClockContent(dateTimeState)
    }
}

@Composable
private fun DisclaimerPrompt() {
    val context = LocalContext.current
    val intent = android.content.Intent(context, Activity::class.java)
    val size = LocalSize.current
    val marginsDp = getMarginDp(size)

    val widthPx = context.dpToPx(size.width)
    val heightPx = context.dpToPx(size.height)
    val marginsPx = context.dpToPx(marginsDp)
    val textColor = context.getWidgetTextColor()
    val text = context.getString(com.dermochelys.utcclock.R.string.widget_tap_to_begin)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .clickable(actionStartActivity(intent)),
        contentAlignment = Alignment.Center,
    ) {
        if (wouldExceedMemoryLimit(widthPx.toInt(), heightPx.toInt())) {
            BitmapErrorText(textColor)
        } else {
            val bitmap = renderTextToBitmap(
                context = context,
                text = text,
                marginPx = marginsPx,
                widthPx = widthPx.toInt(),
                heightPx = heightPx.toInt(),
                textColor = textColor,
                textAlign = TextAlign.Center,
            )
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = text,
                modifier = GlanceModifier.fillMaxSize()
            )
            // Add invisible Text component with content description for API 31+ accessibility
            // This ensures content description is accessible even if Image's doesn't propagate
            Text(
                text = text,
                style = TextStyle(fontSize = 0.sp),
                modifier = GlanceModifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun ExactAlarmPermissionPrompt() {
    val context = LocalContext.current
    val size = LocalSize.current
    val marginsDp = getMarginDp(size)

    val widthPx = context.dpToPx(size.width)
    val heightPx = context.dpToPx(size.height)
    val marginsPx = context.dpToPx(marginsDp)
    val textColor = context.getWidgetTextColor()
    val text = context.getString(R.string.widget_alarm_permission_required)

    val settingsIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    } else {
        Intent(context, Activity::class.java)
    }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .clickable(actionStartActivity(settingsIntent)),
        contentAlignment = Alignment.Center,
    ) {
        if (wouldExceedMemoryLimit(widthPx.toInt(), heightPx.toInt())) {
            BitmapErrorText(textColor)
        } else {
            val bitmap = renderTextToBitmap(
                context = context,
                text = text,
                marginPx = marginsPx,
                widthPx = widthPx.toInt(),
                heightPx = heightPx.toInt(),
                textColor = textColor,
                textAlign = TextAlign.Center,
            )
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = text,
                modifier = GlanceModifier.fillMaxSize()
            )
            Text(
                text = text,
                style = TextStyle(fontSize = 0.sp),
                modifier = GlanceModifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun BitmapErrorText(textColor: Color) {
    val errorText = "Widget is too large to be displayed.\n\nPlease resize it to a smaller size."

    Text(
        text = errorText,
        style = TextStyle(
            color = colorProvider(textColor),
            fontSize = 24.sp,
            textAlign = Center,
        ),
        modifier = GlanceModifier
    )
}

/** This appears to be an FP warning as the function is not marked as restricted */
@SuppressLint("RestrictedApi")
@Composable
private fun colorProvider(textColor: Color): ColorProvider = ColorProvider(textColor)

@Composable
private fun ClockContent(zonedDateTime: Pair<Date, TimeZone>) {
    val size = LocalSize.current
    val context = LocalContext.current
    val intent = android.content.Intent(context, Activity::class.java)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .clickable(actionStartActivity(intent))
    ) {
        LandscapeLayout(zonedDateTime, size)
    }
}

@Composable
private fun Context.dpToPx(margins: DpSize) = Size(
    dpToPx(margins.width), dpToPx(margins.height)
)

@Composable
private fun LandscapeLayout(zonedDateTime: Pair<Date, TimeZone>,
                            size: DpSize,
                            ) {
    val context = LocalContext.current
    val marginDp = getMarginDp(size)

    val format = "HH:mm z\nEEEE\nMMMM dd yyyy"
    val text = formatTime(zonedDateTime, format)
    val textColor = context.getWidgetTextColor()
    val widthPx = context.dpToPx(size.width)
    val heightPx = context.dpToPx(size.height)
    val marginsPx = context.dpToPx(marginDp)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        if (wouldExceedMemoryLimit(widthPx.toInt(), heightPx.toInt())) {
            BitmapErrorText(textColor)
        } else {
            val bitmap = renderTextToBitmap(
                context = context,
                text = text,
                marginPx = marginsPx,
                widthPx = widthPx.toInt(),
                heightPx = heightPx.toInt(),
                textColor = textColor,
                textAlign = TextAlign.Start,
            )
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = text,
                modifier = GlanceModifier.fillMaxSize()
            )

            // Add an invisible Text component to ensure accessibility to screen readers and automated UI testing
            Text(
                text = text,
                style = TextStyle(fontSize = 0.sp,),
                modifier = GlanceModifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun getMarginDp(size: DpSize): DpSize {
    val horizontalMargin = if (size.width < 80.dp) 2.dp else 8.dp
    val verticalMargin = if (size.height < 80.dp) 2.dp else 8.dp
    return DpSize(horizontalMargin, verticalMargin)
}

private fun formatTime(zonedDateTime: Pair<Date, TimeZone>, pattern: String): String {
    val formatter = SimpleDateFormat(pattern, Locale.US).apply { timeZone = zonedDateTime.second }
    return formatter.format(zonedDateTime.first)
}

private fun Context.dpToPx(dp: Dp) = (dp.value * resources.displayMetrics.density)

private fun Context.getWidgetTextColor(): Color {
    val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    return if (isDarkMode) { Color.White } else { Color.Black }
}
