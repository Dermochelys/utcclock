package com.dermochelys.utcclock.widget.util

import android.app.Instrumentation
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.annotation.CheckResult
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Gesture utilities for widget UI testing.
 */

/**
 * Press home button to go to launcher.
 */
fun goToHomeScreen(device: UiDevice) {
    device.pressHome()
    device.wait(Until.hasObject(By.pkg(LAUNCHER_PACKAGE).depth(0)), TIMEOUT_5S_IN_MS)
}

/**
 * Performs a long-press and drag gesture using `input motionevent` commands.
 * This creates a continuous gesture:
 * 1. DOWN at start position
 * 2. Hold for 2.5 seconds (long-press)
 * 3. MOVE events to drag to end position
 * 4. UP at end position
 */
fun performLongPressAndDrag(device: UiDevice,
                            instrumentation: Instrumentation,
                            startX: Int, startY: Int,
                            endX: Int, endY: Int) {
    Log.d(TAG, "performLongPressAndDrag: Starting gesture from ($startX, $startY) to ($endX, $endY)")

    // Step 1: DOWN - Press down at start position
    val downCommand = "input motionevent DOWN $startX $startY"
    executeShellCommand(instrumentation, downCommand)

    // Step 2: Hold for long-press duration (finger stays down)
    Thread.sleep(2500)

    // Step 3: MOVE - Drag to end position with multiple move events
    val steps = 20

    for (i in 1 until steps) {
        val progress = i.toFloat() / steps
        val currentX = (startX + (endX - startX) * progress).toInt()
        val currentY = (startY + (endY - startY) * progress).toInt()

        val moveCommand = "input motionevent MOVE $currentX $currentY"
        executeShellCommand(instrumentation, moveCommand)
        Thread.sleep(10)
    }

    // Final move to exact end position
    val finalMoveCommand = "input motionevent MOVE $endX $endY"
    executeShellCommand(instrumentation, finalMoveCommand)

    // Small delay before releasing
    Thread.sleep(100)

    // Step 4: UP - Release at end position
    val upCommand = "input motionevent UP $endX $endY"
    executeShellCommand(instrumentation, upCommand)

    // Step 5: Press home button to complete widget placement
    Thread.sleep(300)
    device.pressHome()
    device.waitForIdle()
}

/**
 * Executes a shell command and returns the output.
 */
fun executeShellCommand(instrumentation: Instrumentation, command: String) {
    val parcelFileDescriptor = instrumentation.uiAutomation.executeShellCommand(command)

    val output = ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor).use { inputStream ->
        BufferedReader(InputStreamReader(inputStream)).use { reader ->
            reader.readText().trim()
        }
    }

    if (output.contains("error", ignoreCase = true) || output.contains("failed", ignoreCase = true)) {
        throw Exception("shell command failed: $output")
    }
}
