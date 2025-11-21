package com.dermochelys.utcclock.widget

import android.app.Instrumentation
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import com.dermochelys.utcclock.widget.util.acceptDisclaimer
import com.dermochelys.utcclock.widget.util.addUtcClockWidget
import com.dermochelys.utcclock.widget.util.closeApp
import com.dermochelys.utcclock.widget.util.goToHomeScreen
import com.dermochelys.utcclock.widget.util.launchApp
import com.dermochelys.utcclock.widget.util.removeUtcClockWidget
import com.dermochelys.utcclock.widget.util.tapAnyUtcClockWidget
import com.dermochelys.utcclock.widget.util.verifyWidgetShowsTapToBegin
import com.dermochelys.utcclock.widget.util.verifyWidgetShowsUtcTime
import com.dermochelys.utcclock.widget.util.waitForWidgetUpdate
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
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
 * 1. Adding widget before accepting disclaimer
 * 2. Adding widget after accepting disclaimer
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
        // Remove widgets
        removeUtcClockWidget(device, instrumentation)

        // Clear data store
        clearDataStore()
    }

    /**
     * Test 1: Add widget to home screen before accepting disclaimer.
     * - Widget should show "Tap to begin"
     * - Tapping widget opens the app
     * - After accepting disclaimer and closing app, widget should update to show UTC time
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

        // Step 6: Wait for widget to update (1-2s as per requirements)
        waitForWidgetUpdate(device, timeoutMs = 2000L)

        // Step 7: Verify widget now shows UTC time
        assertTrue(
            "Widget should show UTC time after disclaimer is accepted",
            verifyWidgetShowsUtcTime(device)
        )
    }

    /**
     * Test 2: Add widget to home screen after accepting disclaimer.
     * - Launch app and accept disclaimer first
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

        // Step 4: Add widget to home screen
        addUtcClockWidget(device, instrumentation)

        // Step 5: Wait for widget to update (1-2s as per requirements)
        waitForWidgetUpdate(device, timeoutMs = 2000L)

        // Step 6: Verify widget shows UTC time
        assertTrue(
            "Widget should show UTC time when disclaimer was already accepted",
            verifyWidgetShowsUtcTime(device)
        )
    }

    // Helper methods

    private fun clearDataStore() {
        runBlocking { dataStore.edit { it.clear() } }
    }
}
