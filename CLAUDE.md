# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

UTC Clock is a single-module Android application that displays UTC time with support for phones, tablets, and Android TVs. It uses a hybrid UI approach combining traditional Android Views (with ViewBinding) and Jetpack Compose components.

## Build Commands

```bash
# Build the project (includes lint checks and tests)
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK (with R8 minification and resource shrinking)
./gradlew assembleRelease

# Install debug build to connected device
./gradlew installDebug

# Clean build
./gradlew clean build
```

## Development Workflow

**IMPORTANT: Before considering any task complete:**

1. **Always compile the code** to verify there are no compilation errors:
   - For production code changes: `./gradlew assembleDebug`
   - For UI test code changes: `./gradlew compileDebugAndroidTestKotlin`
   - For unit test code changes: `./gradlew compileDebugUnitTestKotlin`

2. **Always ask the user to run tests** before marking a task as complete:
   - For unit test changes: Ask user to run `./gradlew test`
   - For UI test changes: Ask user to run the relevant instrumented tests on a device/emulator
   - Never assume tests pass without user confirmation

3. **Never mark a task as complete** until both compilation succeeds AND the user confirms tests pass.

## Testing

```bash
# Run all tests
./gradlew test

# Run unit tests only
./gradlew testDebugUnitTest

# Run instrumented tests on connected device/emulator
./gradlew connectedAndroidTest

# Run specific instrumented test class
./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=<package.name.MyTestClass>

# Run specific instrumented test method
./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=<package.name.MyTestClass>#<testMethodName>

# Example: Run only widget UI tests
./gradlew app:connectedAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.dermochelys.utcclock.widget.WidgetUiTests

# Run both unit and instrumented tests
./gradlew clean build connectedCheck
```

### Testing Infrastructure

- **Unit Tests**: Use JUnit 4 and MockK for mocking. Located in `app/src/test/`.
- **Instrumented Tests**: Use Hilt for dependency injection testing with a custom `HiltTestRunner` (app/src/androidTest/java/com/dermochelys/utcclock/HiltTestRunner.kt).
- **Test DI Modules**: Test modules in `app/src/androidTest/java/com/dermochelys/utcclock/di/` use `@TestInstallIn` to replace production modules.
- **Test JVM Args**: Tests require `-XX:+EnableDynamicAgentLoading` for MockK compatibility (configured in app/build.gradle.kts).

### Widget UI Testing

Widget UI tests have API version-specific nuances due to launcher UI changes across Android versions as well as platform feature support differences.

**API Version Differences:**

- **Widget Addition**:
  - **API 23-30**: Long press on the widget preview, then release to place on home screen (no expansion needed)
  - **API 31**: Tap app name to expand (chevrons appear), long press on the widget preview, then release to place on home screen, then press HOME to complete placement
  - **API 32+**: Tap app name to expand (chevrons appear), long press on the widget preview and drag upwards to place on the home screen, then press HOME to complete placement
  - **Chevrons**: Chevrons appear in the widget picker on **API 31+** for expanding/collapsing widget options. The test helper taps the app name text to expand rather than looking for the chevron, as the chevron's resource ID/type changes across Android versions.
  - **Widget Identification**: Widgets are identified by looking for `LauncherAppWidgetHostView` with contentDescription "UTC Clock". Falls back to checking for rootView FrameLayout if contentDescription is not found.
  - **Widget State Verification**: Widget state is verified by checking TextView text inside the widget (the widget always includes invisible Text components with fontSize 0.sp for accessibility). Looks for "Tap to begin" or time format (HH:mm) with "UTC" indicator.

- **Widget Removal**:
  - **API 23-28**: Widget removal is not supported because the gesture is complex and shell commands for this complex gesture don't work properly until API 29+ (method returns early without attempting removal)
  - **API 29+**: Widget removal is supported
  - **API 29 & 30**: Removal drag is slower, performed in several very small steps
  - **API 31+**: Smoother and faster drag motion

**Test Infrastructure:**

Widget test utilities are organized in `app/src/androidTest/java/com/dermochelys/utcclock/widget/util/`:
- **Constants.kt**: Shared constants (text strings, resource IDs, class names)
- **AppTasks.kt**: Common helper functions (finding elements, scrolling, etc.)
- **Gestures.kt**: Gesture utilities (home screen, widget panel, long-press-drag)
- **Finding.kt**: Widget finding and verification functions
- **Adding.kt**: Widget addition logic (Api 31+)
- **AddingLegacy.kt**: Widget addition logic (Before API 31)
- **Removing.kt**: Widget removal logic

**TODO's:**

- Investigate widget loading failures seen on emulator during UI test runs - Sometimes the widget can say  "Failed to load widget" when first placing it on the home screen. Need to determine root cause and add retry logic or better error handling.
- Further refine widget UI test helpers for increased brevity, understandability, reliability

## Code Quality & Linting

```bash
# Run lint analysis
./gradlew lint

# Run lint vital checks for release
./gradlew lintVitalRelease

# Check for dependency updates
./gradlew dependencyUpdates
```

### Lint Configuration

- Lint warnings are treated as errors
- Exceptions: `NewerVersionAvailable` and `GradleDependency` are disabled (needed when Kotlin versions are ahead of KSP)
- Java compiler warnings are also treated as errors (`-Werror`)

## Architecture

### Navigation Flow

The app uses the Android Navigation Component with a single Activity pattern:

1. **Activity** (Activity.kt) - Single activity with edge-to-edge display and system UI hiding
2. **LandingFragment** (start destination) - Entry point that handles initial setup
3. **DisclaimerFragment** - Legal disclaimer (TV-optimized and non-TV variants)
4. **ClockFragment** - Main UTC clock display
5. **FontLicenseDialogFragment** - Font licensing information dialog
6. **DonationDialogFragment** - Donation information dialog

Navigation graph: `app/src/main/res/navigation/nav_graph.xml`

### Dependency Injection (Hilt)

DI modules in `app/src/main/java/com/dermochelys/utcclock/di/`:

- **RepositoryModule**: Provides repository implementations
- **DispatcherModule**: Provides coroutine dispatchers
- **CoroutineModule**: Provides ViewModelScoped coroutine scopes
- **DataStoreModule**: Provides DataStore for preferences

### Data Layer

Repository pattern with interfaces in `app/src/main/java/com/dermochelys/utcclock/repository/`:

- **ZonedDateRepository**: Provides Flow of current Date and TimeZone (UTC), handles time updates
- **DisclaimerRepository**: Manages disclaimer acceptance state via DataStore

Implementations in `repository/internal/` package.

### UI Layer

**ViewModels** use `@HiltViewModel` and are scoped to ViewModelComponent:
- Injected with CoroutineScope (ViewModelScoped) for lifecycle-aware coroutine management
- ViewModels manually call `coroutineScope.cancel()` in `onCleared()`

**UI Pattern**: Fragments host Compose content while still using ViewBinding for the fragment layout. This hybrid approach will transition to pure Compose when Jetpack Navigation 3 is stable.

**State Management**:
- ViewModels use Compose `mutableStateOf` for UI state
- State can be saved/restored via `Bundle` in `onSaveInstanceState`/`onLoadInstanceState` methods when needed

### Platform Detection

The app detects Android TV using utilities in `view/common/TvUtils.kt` and provides TV-optimized UI variants (e.g., `TvDisclaimer.kt` vs `NonTvDisclaimer.kt`).

### Immersive Display

- Edge-to-edge display enabled in Activity
- System UI hiding via `window.hideSystemUi()` (view/common/WindowExt.kt)
- Window insets are consumed to achieve full-screen display

### Widget Implementation

The app provides a Glance-based home screen widget (`UtcClockGlanceAppWidget`) that displays the current UTC time.

**CRITICAL ALARM SCHEDULING REQUIREMENT:**
- **NEVER use `*Idle` alarm methods** (`setExactAndAllowWhileIdle`, `setAndAllowWhileIdle`)
- These methods interfere with Android Doze mode optimization and battery management
- Always use `setExact()` or `set()` without the `*Idle` variants
- Use `AlarmManager.RTC` (not `RTC_WAKEUP`) to avoid waking the device unnecessarily

**Widget Lifecycle:**
- Widget uses Hilt for dependency injection via `@AndroidEntryPoint`
- `onEnabled()` is called only when the first widget instance is added
- `onDisabled()` is called when the last widget instance is removed
- `MY_PACKAGE_REPLACED` broadcast is handled to reschedule alarms after app reinstall/update
  - Separate `BroadcastReceiver` class handles `MY_PACKAGE_REPLACED`
  - Receiver is dynamically enabled/disabled based on widget state (enabled when widgets exist, disabled when no widgets)
  - Only reschedules alarms if disclaimer is already accepted
- Alarms are automatically cleared by Android on app uninstall and force stop

**Implementation Details:**
- Widget receiver: `GlanceAppWidgetReceiver` (extends `GlanceAppWidgetReceiver`)
- Package replaced receiver: `BroadcastReceiver` (handles `MY_PACKAGE_REPLACED`, dynamically enabled/disabled)
- Widget implementation: `GlanceAppWidget` (implements `GlanceAppWidget`)
- Update mechanism: Broadcasts to internal `ACTION_UPDATE_WIDGET` trigger updates
- Alarm scheduling: Updates scheduled for exact minute boundaries using `AlarmManager.setExact()`

**TODO - Force Stop Recovery:**
- When user taps widget after force stop, Activity launches but alarms remain cancelled
- Need to detect missing alarms in LandingViewModel and reschedule them (when disclaimer is accepted)
- Use `PendingIntent.FLAG_NO_CREATE` to check if alarm exists, reschedule if not
- Only reschedule if: (1) disclaimer accepted AND (2) widgets exist AND (3) alarms not scheduled

## Development Requirements

- **JDK**: 21 (JVM toolchain version 21)
- **Target SDK**: 36 (Android 15+)
- **Min SDK**: 23 (Android 6.0)
- **NDK**: 29.0.14206865
- **Build Tools**: 36.0.0
- **Current Version**: 2.0.0+41

## Dependency Management

Dependencies are managed via Gradle version catalogs in `gradle/libs.versions.toml`. When updating dependencies, prefer using the `dependencyUpdates` task to find stable versions only (non-stable versions are rejected unless the current version is also non-stable).

### Key Dependency Versions

- **Kotlin**: 2.2.21
- **Android Gradle Plugin**: 8.13.1
- **Gradle**: 9.2.1
- **Compose BOM**: 2025.11.01
- **Hilt**: 2.57.2
- **KSP**: 2.2.21-2.0.4
- **Navigation**: 2.9.6
- **Lifecycle**: 2.9.4 (held at this version as 2.10.0+ require minApi 24+)
- **DataStore Preferences**: 1.1.7 (held at this version as 1.2.0+ require minApi 24+)
- **Activity Compose**: 1.11.0 (held at this version as 1.12.0+ require minApi 24+)

Note: Several AndroidX dependencies are intentionally held back to maintain compatibility with API 23.

## Build Configuration

### Release Builds

- R8 minification enabled
- Resource shrinking enabled
- ProGuard rules in `app/proguard-rules.pro` (currently using defaults)
- Full debug symbol level for NDK

### Compose

- Compose Compiler Extension: 1.5.15
- Using Compose BOM for version management
- Compose is enabled alongside ViewBinding

## Version Management

Version code and version name are managed in `app/build.gradle.kts`:
- Version code: Monotonically increasing integer
- Version name: SemVer format with version code suffix (e.g., "1.9.1+36")

## CI/CD

GitHub Actions workflow (`.github/workflows/android.yml`) runs on push/PR to main branch:
- Sets up JDK 21
- Enables KVM for emulator
- Caches AVD and Gradle
- Checks for dependency updates with `./gradlew dependencyUpdates --refresh-dependencies --no-parallel`
- Runs tests on **two emulators using matrix strategy**:
  - **API 23** (minSdk) - tests minimum supported Android version
  - **API 36** (targetSdk) - tests target Android version
  - Both emulators use Google APIs, x86_64 architecture, Nexus 6 profile
  - **IMPORTANT**: When updating minSdk or targetSdk in `app/build.gradle.kts`, the matrix API levels in `.github/workflows/android.yml` must be updated manually to match
- Runs `./gradlew build connectedCheck` on both emulators
- Checks Android ELF alignment
