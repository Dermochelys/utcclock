package com.dermochelys.utcclock.widget

import android.app.Instrumentation
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.dermochelys.utcclock.widget.util.acceptDisclaimer
import com.dermochelys.utcclock.widget.util.addUtcClockWidget
import com.dermochelys.utcclock.widget.util.allWidgetsShowTapToBegin
import com.dermochelys.utcclock.widget.util.allWidgetsShowUtcTime
import com.dermochelys.utcclock.widget.util.closeApp
import com.dermochelys.utcclock.widget.util.countUtcClockWidgets
import com.dermochelys.utcclock.widget.util.goToHomeScreen
import com.dermochelys.utcclock.widget.util.grantExactAlarmPermission
import com.dermochelys.utcclock.widget.util.grantExactAlarmPermissionViaUi
import com.dermochelys.utcclock.widget.util.launchApp
import com.dermochelys.utcclock.widget.util.needsExactAlarmPermissionPrompt
import com.dermochelys.utcclock.widget.util.removeUtcClockWidget
import com.dermochelys.utcclock.widget.util.tapAnyUtcClockWidget
import com.dermochelys.utcclock.widget.util.verifyWidgetShowsExactAlarmPrompt
import com.dermochelys.utcclock.widget.util.verifyWidgetShowsTapToBegin
import com.dermochelys.utcclock.widget.util.verifyWidgetShowsUtcTime
import com.dermochelys.utcclock.widget.util.waitForWidgetUpdate
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/**
 * UI tests for the UTC Clock widget using UI Automator.
 *
 * These tests verify the widget behavior in different scenarios:
 * 1. Adding widget before accepting disclaimer (full flow including permission on API 34+)
 * 2. Adding widget after accepting disclaimer and granting permission
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class WidgetUiTests {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    private lateinit var device: UiDevice

    private lateinit var instrumentation: Instrumentation

    @Before
    fun setup() {
        hiltRule.inject()

        // Clear any previous state
        clearDataStore()

        // Initialize UI device
        instrumentation = InstrumentationRegistry.getInstrumentation()
        device = UiDevice.getInstance(instrumentation)
    }

    @After
    fun tearDown() {
        // Remove widgets so the home screen is clean for the next test
        removeUtcClockWidget(device, instrumentation)

        // Clear data store (disclaimer state, etc.)
        clearDataStore()
    }

    /**
     * Test 1: Add widget to home screen before accepting disclaimer.
     * Full flow:
     * - Widget should show "Tap to begin"
     * - Tapping widget opens the app
     * - After accepting disclaimer and closing app, widget should update
     * - On API 34+: widget shows "Tap to enable precise updates", then grant permission via UI
     * - Widget should show UTC time
     */
    @Test
    fun addWidgetBeforeDisclaimer_showsTapToBegin_thenUpdatesToUtcTime() {
        // Step 1: Go to home screen and add widget
        goToHomeScreen(device)
        addUtcClockWidget(device, instrumentation)

        // Step 2: Verify widget shows "Tap to begin"
        assertTrue(
            "Widget should show 'Tap to begin' when disclaimer not accepted",
            verifyWidgetShowsTapToBegin(device)
        )

        // Step 3: Tap any widget to open app
        tapAnyUtcClockWidget(device)

        // Step 4: Accept disclaimer
        acceptDisclaimer(device)

        // Step 5: Close app and return to home screen
        closeApp(device)

        // Step 6: Wait for widget to update
        waitForWidgetUpdate(device, timeoutMs = 2000L)

        // Step 7: On API 34+, verify permission prompt then grant via UI flow
        if (needsExactAlarmPermissionPrompt()) {
            assertTrue(
                "Widget should show alarm permission prompt when permission not granted (API 34+)",
                verifyWidgetShowsExactAlarmPrompt(device)
            )

            // Follow the real UI flow: tap widget → system settings → grant → back
            grantExactAlarmPermissionViaUi(device)

            // Wait for widget to recompose
            waitForWidgetUpdate(device, timeoutMs = 2000L)
        }

        // Step 8: Verify widget now shows UTC time
        assertTrue(
            "Widget should show UTC time after disclaimer is accepted and permission is granted",
            verifyWidgetShowsUtcTime(device)
        )
    }

    /**
     * Test 2: Add widget to home screen after accepting disclaimer and granting permission.
     * - Launch app and accept disclaimer first
     * - Grant exact alarm permission (API 34+)
     * - Close app
     * - Add widget to home screen
     * - Widget should show UTC time immediately (after brief delay)
     */
    @Test
    fun addWidgetAfterDisclaimer_showsUtcTimeImmediately() {
        val context = instrumentation.targetContext

        // Step 1: Launch app
        launchApp(device, context)

        // Step 2: Accept disclaimer
        acceptDisclaimer(device)

        // Step 3: Close app
        closeApp(device)

        // Step 4: Grant exact alarm permission on API 34+
        grantExactAlarmPermission(device, context)

        // Step 5: Add widget to home screen
        addUtcClockWidget(device, instrumentation)

        // Step 6: Wait for widget to update (1-2s as per requirements)
        waitForWidgetUpdate(device, timeoutMs = 2000L)

        // Step 7: Verify widget shows UTC time
        assertTrue(
            "Widget should show UTC time when disclaimer was already accepted",
            verifyWidgetShowsUtcTime(device)
        )
    }

    /**
     * Test 3: Tap widget when disclaimer not accepted, then return home without accepting.
     * - Widget shows "Tap to begin"
     * - Tapping opens the app's disclaimer screen
     * - Pressing home (without tapping "I Agree") returns to home screen
     * - Widget should still show "Tap to begin" (state unchanged)
     */
    @Test
    fun addWidgetBeforeDisclaimer_tappingThenDismissingWithoutAccepting_widgetRemainsTapToBegin() {
        // Step 1: Add widget
        goToHomeScreen(device)
        addUtcClockWidget(device, instrumentation)

        // Step 2: Verify widget shows "Tap to begin"
        assertTrue(
            "Widget should show 'Tap to begin' initially",
            verifyWidgetShowsTapToBegin(device)
        )

        // Step 3: Tap widget to open app (lands on disclaimer screen)
        tapAnyUtcClockWidget(device)

        // Step 4: Close app without accepting (press home)
        goToHomeScreen(device)

        // Step 5: Verify widget still shows "Tap to begin"
        assertTrue(
            "Widget should still show 'Tap to begin' after dismissing without accepting",
            verifyWidgetShowsTapToBegin(device)
        )
        assertFalse(
            "Widget should not have transitioned to UTC time",
            verifyWidgetShowsUtcTime(device)
        )
    }

    /**
     * Test 4: Two widgets on the home screen both show the same state.
     * - Add two widgets before accepting the disclaimer — both show "Tap to begin"
     * - Accept disclaimer and grant permission
     * - Both widgets show UTC time
     */
    @Test
    fun addingTwoWidgets_bothShowSameState() {
        // Record baseline count — on API 23-28 widgets can't be removed, so
        // leftover widgets from previous tests may still be on the home screen.
        goToHomeScreen(device)
        val baselineCount = countUtcClockWidgets(device)

        // Step 1: Add first widget
        addUtcClockWidget(device, instrumentation)

        // Step 2: Add second widget
        addUtcClockWidget(device, instrumentation)

        // Step 3: Verify two NEW widgets were added and all show "Tap to begin"
        val afterAddCount = countUtcClockWidgets(device)
        assertEquals(
            "Should have two more widgets than baseline ($baselineCount)",
            baselineCount + 2,
            afterAddCount
        )
        assertTrue(
            "All widgets should show 'Tap to begin' before disclaimer accepted",
            allWidgetsShowTapToBegin(device)
        )

        // Step 4: Tap one widget to open app, accept disclaimer, close app
        tapAnyUtcClockWidget(device)
        acceptDisclaimer(device)
        closeApp(device)

        // Step 5: Wait for widgets to update
        waitForWidgetUpdate(device, timeoutMs = 2000L)

        // Step 6: On API 34+, grant exact alarm permission if not already granted
        if (needsExactAlarmPermissionPrompt()) {
            if (canScheduleExactAlarms(instrumentation.targetContext)) {
                Log.d("Widget", "Test4: SCHEDULE_EXACT_ALARM already granted, skipping permission flow")
            } else {
                grantExactAlarmPermissionViaUi(device)
            }
            waitForWidgetUpdate(device, timeoutMs = 2000L)
        }

        // Step 7: Verify widget count unchanged and ALL widgets show UTC time
        assertEquals(
            "Widget count should not have changed",
            afterAddCount,
            countUtcClockWidgets(device)
        )
        assertTrue(
            "All widgets should show UTC time after disclaimer + permission",
            allWidgetsShowUtcTime(device)
        )
    }

    // Note: a "revoke permission via OS settings" test is NOT possible from
    // instrumentation — Android kills the app process when SCHEDULE_EXACT_ALARM
    // is revoked, which also kills the test. See README.md "Future: Maestro
    // Tests" for flows that need an out-of-process test driver.

    // Helper methods

    private fun clearDataStore() {
        runBlocking { dataStore.edit { it.clear() } }
    }

}
