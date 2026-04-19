package com.dermochelys.utcclock

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation3.runtime.NavBackStack
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.dermochelys.utcclock.repository.PermissionsRepository
import com.dermochelys.utcclock.view.NavigationAction
import com.dermochelys.utcclock.view.ClockRoute
import com.dermochelys.utcclock.view.DisclaimerRoute
import com.dermochelys.utcclock.view.DonationRoute
import com.dermochelys.utcclock.view.ExactAlarmPermissionRoute
import com.dermochelys.utcclock.view.FontLicenseRoute
import com.dermochelys.utcclock.view.LandingRoute
import com.dermochelys.utcclock.view.clock.Clock
import com.dermochelys.utcclock.view.clock.ClockViewModel
import com.dermochelys.utcclock.view.common.AutoNavBackViewModel
import com.dermochelys.utcclock.view.common.hideSystemUi
import com.dermochelys.utcclock.view.common.isRunningOnTv
import com.dermochelys.utcclock.view.common.toColor
import com.dermochelys.utcclock.view.common.vectorToBitmap
import com.dermochelys.utcclock.view.disclaimer.DisclaimerViewModel
import com.dermochelys.utcclock.view.disclaimer.NonTvDisclaimer
import com.dermochelys.utcclock.view.disclaimer.TvDisclaimer
import com.dermochelys.utcclock.view.donation.Donation
import com.dermochelys.utcclock.view.exactalarmpermission.ExactAlarmPermission
import com.dermochelys.utcclock.view.fontlicense.FontLicense
import com.dermochelys.utcclock.view.landing.Landing
import com.dermochelys.utcclock.view.landing.LandingViewModel
import com.dermochelys.utcclock.widget.GlanceAppWidgetReceiver
import com.dermochelys.utcclock.widget.canScheduleExactAlarms
import com.dermochelys.utcclock.widget.scheduleNextUpdate
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Activity : AppCompatActivity() {

    @Inject
    lateinit var permissionsRepository: PermissionsRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            AppNavigation()
        }
    }

    override fun onResume() {
        super.onResume()
        window.hideSystemUi()
    }

    @Composable
    private fun AppNavigation() {
        val backStack = rememberNavBackStack(LandingRoute)

        NavDisplay(
            backStack = backStack,
            onBack = { backStack.removeLastOrNull() },
            entryDecorators = listOf(
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<LandingRoute> {
                    LandingScreen(backStack)
                }

                entry<DisclaimerRoute> {
                    DisclaimerScreen(backStack)
                }

                entry<ClockRoute> {
                    ClockScreen(backStack)
                }

                entry<ExactAlarmPermissionRoute> {
                    ExactAlarmPermissionScreen()
                }

                entry<FontLicenseRoute> {
                    FontLicenseScreen(backStack)
                }

                entry<DonationRoute> {
                    DonationScreen(backStack)
                }
            }
        )
    }

    @Composable
    private fun LandingScreen(backStack: NavBackStack<NavKey>) {
        val viewModel = hiltViewModel<LandingViewModel>()

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(viewModel) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getNavigationActions().collect { action ->
                    if (action == NavigationAction.NONE) return@collect

                    backStack.clear()
                    when (action) {
                        NavigationAction.SHOW_DISCLAIMER -> backStack.add(DisclaimerRoute)
                        NavigationAction.SHOW_CLOCK -> {
                            val route = interceptForExactAlarmPermission(ClockRoute)
                            backStack.add(route)
                        }
                        else -> {}
                    }
                }
            }
        }

        Landing()
    }

    @Composable
    private fun DisclaimerScreen(backStack: NavBackStack<NavKey>) {
        val viewModel = hiltViewModel<DisclaimerViewModel>()
        val context = LocalContext.current
        val overlayBitmap = context.vectorToBitmap(R.drawable.overlay)

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(viewModel) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getNavigationActions().collect { action ->
                    if (action == NavigationAction.NONE) return@collect

                    backStack.clear()
                    val route = interceptForExactAlarmPermission(ClockRoute)
                    backStack.add(route)
                }
            }
        }

        if (context.isRunningOnTv()) {
            TvDisclaimer(
                onDisclaimerAgreeClick = viewModel::onDisclaimerAgreeClicked,
                overlayPositionShift = viewModel.overlayPositionShift,
                overlayBitmap = overlayBitmap,
            )
        } else {
            NonTvDisclaimer(
                onDisclaimerAgreeClick = viewModel::onDisclaimerAgreeClicked,
                overlayPositionShift = viewModel.overlayPositionShift,
                overlayBitmap = overlayBitmap,
            )
        }
    }

    @Composable
    private fun ClockScreen(backStack: NavBackStack<NavKey>) {
        val viewModel = hiltViewModel<ClockViewModel>()
        val context = LocalContext.current

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(viewModel) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getNavigationActions().collect { action ->
                    when (action) {
                        NavigationAction.SHOW_FONT_LICENSE -> backStack.add(FontLicenseRoute)
                        NavigationAction.SHOW_DONATION -> backStack.add(DonationRoute)
                        else -> {}
                    }
                }
            }
        }

        DisposableEffect(Unit) {
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(ctx: Context?, intent: Intent?) {
                    viewModel.onTimeUpdated()
                }
            }
            context.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIME_TICK))
            context.registerReceiver(receiver, IntentFilter(Intent.ACTION_TIME_CHANGED))

            onDispose {
                context.unregisterReceiver(receiver)
            }
        }

        Clock(
            onFontLicenseButtonClicked = viewModel::onFontLicenseButtonClicked,
            onDonationButtonClicked = viewModel::onDonationButtonClicked,
            overlayPositionShift = viewModel.overlayPositionShift,
            fontLicenseButtonAlignmentToStart = viewModel.fontLicenseButtonAlignToStart,
            dateTextAlignToStart = viewModel.dateTextAlignToStart,
            buttonRowTop = viewModel.buttonRowTop,
            contentColor = viewModel.contentColor,
            zonedDateTime = viewModel.zonedDateTime,
            focusedButtonColor = ContextCompat.getColor(context, R.color.blue).toColor(),
            textOrderDateFirst = viewModel.textOrderDateFirst,
            overlayBitmap = context.vectorToBitmap(R.drawable.overlay),
        )
    }

    @Composable
    private fun ExactAlarmPermissionScreen() {
        val context = LocalContext.current

        val lifecycleOwner = LocalLifecycleOwner.current
        LaunchedEffect(Unit) {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                if (canScheduleExactAlarms(context)) {
                    scheduleNextUpdate(context)
                    permissionsRepository.onExactAlarmPermissionChanged()
                    this@Activity.finish()
                }
            }
        }

        ExactAlarmPermission(
            onOpenSettingsClicked = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    startActivity(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }
            },
            onNotNowClicked = { this@Activity.finish() },
        )
    }

    @Composable
    private fun FontLicenseScreen(backStack: NavBackStack<NavKey>) {
        val viewModel = hiltViewModel<AutoNavBackViewModel>()

        LaunchedEffect(viewModel) {
            viewModel.getNavigationActions().collect {
                backStack.removeLastOrNull()
            }
        }

        FontLicense()

        DisposableEffect(Unit) {
            onDispose { window.hideSystemUi() }
        }
    }

    @Composable
    private fun DonationScreen(backStack: NavBackStack<NavKey>) {
        val viewModel = hiltViewModel<AutoNavBackViewModel>()

        LaunchedEffect(viewModel) {
            viewModel.getNavigationActions().collect {
                backStack.removeLastOrNull()
            }
        }

        Donation()

        DisposableEffect(Unit) {
            onDispose { window.hideSystemUi() }
        }
    }

    // Helpers

    private fun interceptForExactAlarmPermission(defaultRoute: NavKey): NavKey {
        if (canScheduleExactAlarms(this)) return defaultRoute
        if (!hasWidgets()) return defaultRoute
        return ExactAlarmPermissionRoute
    }

    private fun hasWidgets(): Boolean {
        val manager = AppWidgetManager.getInstance(this)
        val component = ComponentName(this, GlanceAppWidgetReceiver::class.java)
        return manager.getAppWidgetIds(component).isNotEmpty()
    }
}
