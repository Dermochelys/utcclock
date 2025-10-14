package com.dermochelys.utcclock.view.disclaimer

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dermochelys.utcclock.Activity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DisclaimerUiTests {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<Activity>()

    @Inject
    lateinit var dataStore: DataStore<Preferences>

    @Before
    fun setup() {
        hiltRule.inject()

        // In case there is leftover state from a previous execution
        clearDataStore()
    }

    @After
    fun tearDown() {
        clearDataStore()
    }

    @Test
    fun firstLauch_showsDisclaimer_andAcceptanceShowsClock_andRelauchSkipsDisclaimer() {
        composeTestRule.waitForIdle()

        // Verify disclaimer is displayed (look for text containing "DISCLAIMER")
        composeTestRule.onNodeWithText("DISCLAIMER", substring = true)
            .assertIsDisplayed()

        // Accept disclaimer on first launch
        composeTestRule.onNodeWithText("I Agree")
            .assertIsDisplayed()
            .performClick()

        // Wait for navigation
        composeTestRule.waitForIdle()

        // Verify we're on the clock screen
        composeTestRule.onNodeWithText("UTC", substring = true)
            .assertIsDisplayed()

        // Simulate app restart by recreating the activity
        composeTestRule.activityRule.scenario.recreate()

        // Wait for the recreated activity to settle
        composeTestRule.waitForIdle()

        // Verify we go directly to clock screen (disclaimer should not appear)
        // The disclaimer state should be persisted, so we should see UTC immediately
        composeTestRule.onNodeWithText("UTC", substring = true)
            .assertIsDisplayed()
    }

    // Helpers

    private fun clearDataStore() {
        runBlocking { dataStore.edit { it.clear() } }
    }
}
