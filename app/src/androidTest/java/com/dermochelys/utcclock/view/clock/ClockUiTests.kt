package com.dermochelys.utcclock.view.clock

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.time.Duration.Companion.minutes

private const val TIME_FORMAT = "HH:mm\nz"

private const val DATE_FORMAT = "EEEE\nMMMM dd\nyyyy"

@RunWith(AndroidJUnit4::class)
class ClockUiTests {

    private val utc = TimeZone.getTimeZone("UTC")

    private val initialDate = Date()

    private var zonedDateTime by mutableStateOf(Pair(initialDate, utc))

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun clockDisplaysInitialTime_andUpdatesWhenTimeChanges() {
        composeTestRule.setContent {
            Clock(
                zonedDateTime = zonedDateTime,
                timeFormat = TIME_FORMAT,
                dateFormat = DATE_FORMAT,
            )
        }

        composeTestRule.waitForIdle()

        // Format the expected time string
        val timeFormat = SimpleDateFormat(TIME_FORMAT, Locale.US).also { it.timeZone = utc }
        val expectedTimeText = timeFormat.format(initialDate)

        // Format the expected date string
        val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.US).also { it.timeZone = utc }
        val expectedDateText = dateFormat.format(initialDate)

        // Verify initial time is displayed
        composeTestRule.onNodeWithText(expectedTimeText, substring = true)
            .assertIsDisplayed()

        // Verify initial date is displayed
        composeTestRule.onNodeWithText(expectedDateText, substring = true)
            .assertIsDisplayed()

        // Update time to one minute later
        val updatedTime = initialDate.time + 1.minutes.inWholeMilliseconds

        zonedDateTime = Pair(Date(updatedTime), utc)

        composeTestRule.waitForIdle()

        // Format the updated time string
        val expectedUpdatedTimeText = timeFormat.format(Date(updatedTime))

        // Verify updated time is displayed
        composeTestRule.onNodeWithText(expectedUpdatedTimeText, substring = true)
            .assertIsDisplayed()

        // Date should remain the same (same day)
        composeTestRule.onNodeWithText(expectedDateText, substring = true)
            .assertIsDisplayed()
    }
}
