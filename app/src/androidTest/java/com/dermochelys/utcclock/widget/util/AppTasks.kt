package com.dermochelys.utcclock.widget.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import com.dermochelys.utcclock.widget.canScheduleExactAlarms
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.SearchCondition
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObject2
import androidx.test.uiautomator.Until
import junit.framework.TestCase
import java.util.regex.Pattern
import java.util.regex.Pattern.CASE_INSENSITIVE

/**
 * App management utilities for widget UI testing.
 */

/**
 * Launches the UTC Clock app.
 */
fun launchApp(device: UiDevice,
              context: Context) {
    Log.d(TAG, "launchApp")
    val intent = context.packageManager.getLaunchIntentForPackage(APP_PACKAGE)
    intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(intent)
    device.wait(Until.hasObject(By.pkg(APP_PACKAGE)), TIMEOUT_5S_IN_MS)

    // Check for and dismiss "Got it" button if present
    dismissGotItOverlay(device)
}

/**
 * Accepts the disclaimer in the app.
 */
fun acceptDisclaimer(device: UiDevice) {
    Log.d(TAG, "acceptDisclaimer")
    val agreeButton = waitForText(device, I_AGREE, TIMEOUT_5S_IN_MS)
    agreeButton?.click() ?: TestCase.fail("$I_AGREE button not found")
    device.waitForIdle()
}

/**
 * Closes the current app and returns to home screen.
 */
fun closeApp(device: UiDevice) {
    Log.d(TAG, "closeApp")
    device.pressHome()
    device.waitForIdle()
}

/**
 * Dismisses the "Got it" overlay that appears on some Android versions when entering full screen mode.
 */
private fun dismissGotItOverlay(device: UiDevice) {
    Log.d(TAG, "dismissGotItOverlay")
    device.waitForIdle()

    // Look for "Got it" button by text
    val gotItButton = waitForText(device, GOT_IT, 500L) ?: return

    Log.d(TAG, "dismissGotItOverlay found $GOT_IT, will click it...")
    gotItButton.click()
    device.waitForIdle()
}

/**
 * Returns true if the device requires an explicit exact alarm permission prompt.
 * API 34+ (Android 14+) does not pre-grant SCHEDULE_EXACT_ALARM to apps.
 * API 31-33 pre-grants it automatically when declared in the manifest.
 * API < 31 doesn't need it at all (setExact works without permission).
 */
fun needsExactAlarmPermissionPrompt(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE

/**
 * Grants the SCHEDULE_EXACT_ALARM permission by opening the system settings
 * screen and tapping the toggle. Only has effect on API 34+ where the
 * permission is not pre-granted.
 */
fun grantExactAlarmPermission(device: UiDevice, context: Context) {
    if (!needsExactAlarmPermissionPrompt()) return
    openExactAlarmSettings(context)
    setExactAlarmToggle(device, enabled = true)
    device.pressBack()
    device.waitForIdle()
    Thread.sleep(500)
}

/**
 * Grants the exact alarm permission through the widget UI flow.
 *
 * Follows the real user path:
 * 1. Tap widget → ACTION_REQUEST_SCHEDULE_EXACT_ALARM opens system settings directly
 * 2. Tap the toggle in the settings screen to grant permission
 * 3. Press back → returns to home screen, SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED
 *    broadcast updates repository and widgets recompose
 */
fun grantExactAlarmPermissionViaUi(device: UiDevice) {
    if (!needsExactAlarmPermissionPrompt()) return

    // Tap widget — on API 31+ this opens ACTION_REQUEST_SCHEDULE_EXACT_ALARM directly
    tapAnyUtcClockWidget(device)
    device.waitForIdle()
    Thread.sleep(1000)

    // Tap the real settings toggle to grant permission
    setExactAlarmToggle(device, enabled = true)

    // Press back to return to home screen. The OS broadcasts
    // ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED which the app's
    // BroadcastReceiver handles to update the widget.
    device.pressBack()
    device.waitForIdle()
    Thread.sleep(1000)
}

/**
 * Opens the per-app "Alarms & reminders" settings screen for UTC Clock.
 * This is the same screen that the widget opens when tapped while the
 * permission is not yet granted.
 */
private fun openExactAlarmSettings(context: Context) {
    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
        data = Uri.fromParts("package", APP_PACKAGE, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
}

/**
 * Finds the main toggle on the exact-alarm permission settings screen and
 * taps it if its current state doesn't match the target state.
 *
 * The settings screen only contains a single checkable element (the main
 * switch), so matching by `checkable(true)` is reliable.
 */
private fun setExactAlarmToggle(device: UiDevice, enabled: Boolean) {
    val toggle = device.wait(
        Until.findObject(By.checkable(true)),
        TIMEOUT_5S_IN_MS,
    ) ?: throw AssertionError("Exact-alarm permission toggle not found in settings")

    if (toggle.isChecked != enabled) {
        toggle.click()
        device.waitForIdle()
        Thread.sleep(500)
    }
}

fun waitForText(device: UiDevice, text: String, timeout: Long): UiObject2? =
    device.wait(caseInsensitiveFind(text), timeout)

fun findText(device: UiDevice, text: String): UiObject2? =
    device.findObject(caseInsensitiveSelector(text))

fun findAllText(device: UiDevice, text: String): List<UiObject2> =
    device.findObjects(caseInsensitiveSelector(text))

/**
 * Waits for elements with the specified resource ID to appear.
 * @param device The UiDevice instance
 * @param res The resource ID (without package prefix)
 * @param isLauncher Whether the resource is in the launcher package
 * @param timeout Timeout in milliseconds
 * @return List of matching elements, or null if timeout
 */
fun waitForRes(device: UiDevice, res: String, isLauncher: Boolean, timeout: Long): List<UiObject2>? =
    device.wait(Until.findObjects(By.res(getQualifiedResId(res, isLauncher))), timeout)

/**
 * Waits for elements with the specified class name to appear.
 * @param device The UiDevice instance
 * @param className The fully qualified class name (e.g., "com.android.launcher3.widget.WidgetCell")
 * @param timeout Timeout in milliseconds
 * @return List of matching elements, or null if timeout
 */
fun waitForLauncherClass(device: UiDevice, className: String, timeout: Long): List<UiObject2>? =
    device.wait(Until.findObjects(By.clazz(className)), timeout)

/**
 * Waits for elements with a content description containing the specified text.
 * @param device The UiDevice instance
 * @param descText The text to search for in content descriptions (e.g., "UTC Clock")
 * @param timeout Timeout in milliseconds
 * @return List of matching elements, or null if timeout
 */
fun waitForContentDescription(device: UiDevice, descText: String, timeout: Long): List<UiObject2>? {
    val pattern = Pattern.compile(".*${Pattern.quote(descText)}.*", CASE_INSENSITIVE)
    return device.wait(Until.findObjects(By.desc(pattern)), timeout)
}

/**
 * Finds a single element by resource ID.
 * @param device The UiDevice instance
 * @param res The resource ID (without package prefix)
 * @param packageName The package containing the resource (defaults to launcher package for widget picker/home screen)
 */
fun findRes(device: UiDevice, res: String, isLauncher: Boolean): UiObject2? =
    device.findObject(By.res(getQualifiedResId(res, isLauncher)))

/**
 * Finds all elements by resource ID.
 * @param device The UiDevice instance
 * @param res The resource ID (without package prefix)
 * @param packageName The package containing the resource (defaults to launcher package for widget picker/home screen)
 */
fun findAllRes(device: UiDevice, res: String, isLauncher: Boolean): List<UiObject2> =
    device.findObjects(By.res(getQualifiedResId(res, isLauncher)))

/**
 * Returns the properly qualified resource ID based on API level and package.
 * - API 23-24: Use resource ID directly (e.g., "widget_name")
 * - API 25+: Must prefix with package (e.g., "com.google.android.apps.nexuslauncher:id/widget_name")
 *
 * @param res The resource ID without package prefix
 * @param packageName The package name (LAUNCHER_PACKAGE for widget picker/home screen, APP_PACKAGE for app UI)
 */
fun getQualifiedResId(res: String, isLauncher: Boolean): String {
    val packageName = if (isLauncher) getLauncherPackageName() else APP_PACKAGE
    return "$packageName:id/$res"
}

private fun getLauncherPackageName() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) LAUNCHER_PACKAGE_LEGACY else LAUNCHER_PACKAGE

fun findClass(device: UiDevice, className: String): List<UiObject2> =
    device.findObjects(By.clazz(className))

/**
 * Finds element by text and filters by resource name containing the specified string.
 */
fun findTextWithResName(device: UiDevice, text: String, resNameContains: String): UiObject2? =
    findAllText(device, text).firstOrNull { it.resourceName?.contains(resNameContains) == true }

/**
 * Scrolls down in the widget picker (swipe bottom-to-top).
 * This moves content upward on screen, revealing content below and
 * moving items into the safe viewing area above the navigation bar.
 */
fun scrollDownInPicker(device: UiDevice) {
    val displayHeight = device.displayHeight
    val displayWidth = device.displayWidth
    device.swipe(
        displayWidth / 2,
        displayHeight * 3 / 4,
        displayWidth / 2,
        displayHeight / 4,
        20
    )
    device.waitForIdle()
    Thread.sleep(250)
}

/**
 * Checks if a resource name is a section header.
 */
fun isSectionHeader(resourceName: String?): Boolean =
    resourceName?.contains(RES_SECTION) == true || resourceName?.contains(RES_APP_TITLE) == true

private fun caseInsensitiveFind(text: String): SearchCondition<UiObject2> =
    Until.findObject(caseInsensitiveSelector(text))

private fun caseInsensitiveSelector(text: String): BySelector =
    By.text(caseInsensitivePattern(text))

private fun caseInsensitivePattern(text: String) =
    Pattern.compile(text, CASE_INSENSITIVE)

/**
 * Opens the widgets panel on the home screen.
 * This typically involves long-pressing on the home screen.
 */
fun openWidgetsPanel(device: UiDevice) {
    val displayWidth = device.displayWidth
    val displayHeight = device.displayHeight

    // Long press below center to avoid hitting existing widgets, but above the dock/favorites tray.
    // Retry the gesture — the launcher sometimes doesn't respond on first attempt.
    val pressX = displayWidth * 3 / 4
    val pressY = displayHeight * 3 / 5

    repeat(3) { attempt ->
        Log.d(TAG, "openWidgetsPanel: long press attempt ${attempt + 1} at ($pressX, $pressY)")
        device.swipe(pressX, pressY, pressX, pressY, 100)

        if (waitForText(device, WIDGETS, TIMEOUT_5S_IN_MS) != null) return@repeat
    }

    // Find and click the Widgets button
    val widgetsButton = findText(device, WIDGETS)
        ?: throw AssertionError("Could not find '$WIDGETS' button after long press")

    widgetsButton.click()
    device.waitForIdle()
    Thread.sleep(1000)

    // API 36+: Widget picker may show a "Browse" button to access the full widget list
    val browseButton = waitForText(device, BROWSE, 1000L)
    if (browseButton != null) {
        browseButton.click()
        device.waitForIdle()
        Thread.sleep(1000)
    }
}
