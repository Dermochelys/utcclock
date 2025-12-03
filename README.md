# UTC Clock

A simple, offline-only Android application that displays UTC (Coordinated Universal Time) with support for phones, tablets and Android TVs.

## Overview

- This is a single-module Android application built with Kotlin that provides an immersive fullscreen UTC time display.
- It features a hybrid UI approach combining traditional Android Views with Jetpack Compose components.
  - [Jetpack Navigation 3](https://developer.android.com/guide/navigation/navigation-3) is now stable and planned for integration, which will enable removal of Fragment use and transition to a pure Compose architecture.  

## Features

- **Universal Time Display**: Clean, easy-to-read UTC clock
- **Home Screen Widget**: Resizable widget for quick UTC time access
  - Note: Does not include anti-burn-in (image retention) prevention, as home screen display already exposes other static elements (icons, labels, etc.) to potential burn-in
- **Multi-Platform Support**:
  - Android TV (leanback) optimized interface
  - Mobile devices with touchscreen support
- **Immersive Experience**: Edge-to-edge fullscreen display with system UI hiding
- **Modern Architecture**: Built with latest Android development best practices
- **Hybrid UI**: Combines ViewBinding and Jetpack Compose for optimal flexibility

## Requirements

- **Minimum SDK**: Android 6.0 (API 23)
- **Target SDK**: Android 16 / Baklava (API 36)
- **Java/Kotlin**: JVM toolchain version 21

## Building the Project

### Prerequisites

- Android Studio or IntelliJ IDEA with Android plugin
- JDK 21
- Android SDK with API level 36
- NDK 29.0.14206865

### Build Commands

```bash
# Build the project
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Build release APK (with R8 minification)
./gradlew assembleRelease

# Install debug build to connected device
./gradlew installDebug
```

## Development

### Architecture

The app uses modern Android architecture components:

- **Navigation**: Android Navigation Component with single Activity pattern
- **Dependency Injection**: Hilt (Dagger-based DI)
- **UI**: Hybrid approach with ViewBinding + Jetpack Compose
- **Coroutines**: Kotlin Coroutines for asynchronous operations
- **Data Layer**: Repository pattern for data management

### Project Structure

```
app/src/main/java/com/dermochelys/utcclock/
├── Activity.kt                    # Main activity
├── Application.kt                 # Application class with Hilt setup
├── di/                            # Dependency injection modules
│   ├── WidgetEntryPoint.kt        # Hilt entry point for widget
│   ├── CoroutineModule.kt         # Coroutine scope providers
│   ├── DataStoreModule.kt         # DataStore providers
│   ├── DispatcherModule.kt        # Coroutine dispatcher providers
│   └── RepositoryModule.kt        # Repository providers
├── repository/                    # Data layer (Repository pattern)
│   ├── DisclaimerRepository.kt    # Disclaimer acceptance interface
│   ├── ZonedDateRepository.kt     # UTC time data interface
│   └── internal/                  # Repository implementations
│       ├── DisclaimerRepositoryImpl.kt
│       └── ZonedDateRepositoryImpl.kt
├── view/
│   ├── OverlayConst.kt            # Shared overlay constants
│   ├── landing/                   # Entry point fragment
│   ├── clock/                     # Main UTC clock display
│   ├── disclaimer/                # Legal disclaimer view
│   ├── donation/                  # Donation information dialog
│   ├── fontlicense/               # Font licensing dialog
│   └── common/                    # Shared UI utilities
└── widget/                        # Home screen widget (Glance-based)
    ├── UtcClockGlanceAppWidget.kt # Widget implementation
    ├── GlanceAppWidgetReceiver.kt # Widget receiver
    ├── BroadcastReceiver.kt       # Package replaced receiver
    ├── DisclaimerStateBroadcaster.kt # Disclaimer state sync
    ├── UpdateScheduler.kt         # Alarm scheduling utilities
    └── internal/                  # Widget internal implementations
        ├── AutoFontSize.kt        # Dynamic font sizing
        ├── BroadcastReceiverExt.kt # Receiver extensions
        └── TextToBitmapRenderer.kt # Text rendering utilities
```

### Code Quality

```bash
# Run lint analysis
./gradlew lint

# Lint vital checks for release
./gradlew lintVitalRelease
```

### Dependency Management

```bash
# Check for dependency updates
./gradlew dependencyUpdates
```

Dependencies are managed via `gradle/libs.versions.toml` using Gradle version catalogs.

## Testing

### Running Tests

```bash
# Run all unit tests (no emulator/device required)
./gradlew test

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

### Testing Strategy

- **Unit Tests**: Fast, isolated tests using MockK for mocking
- **Instrumentation Tests**: Integration tests with Hilt for full dependency injection testing

### Future: Maestro Tests

Some user flows can't be covered by Android instrumentation tests because they require an **out-of-process test driver**. Instrumentation runs inside the target app's process, so when the OS kills the app, the test dies with it. [Maestro](https://maestro.mobile.dev/) drives the device via ADB from outside the app process and survives these kills.

Flows that would benefit from Maestro coverage:

- **Revoke `SCHEDULE_EXACT_ALARM` via OS settings toggle** — Android kills the app process on revoke. The production path is correct (the OS restarts the app via the manifest-registered receiver to deliver `ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED`), but there's no way to observe the widget transition from within instrumentation.
- **Force-stop recovery** — Tapping the widget after force-stop launches the activity but alarms remain cancelled. A Maestro test could force-stop the app, tap the widget, and verify recovery.
- **Device reboot recovery** — Alarms are not persisted across reboots, so widgets go stale until something re-schedules. Testing reboot → widget updates requires driving the device through the reboot itself.
- **Real OS settings toggle for grant** — Today the grant helper opens the settings screen but flips the permission via `appops` shell rather than tapping the actual toggle. A Maestro flow could tap the real toggle, validating that the settings-screen integration still works across Android versions.

## Continuous Integration

The project uses GitHub Actions for automated testing on every push and pull request to the main branch.

### CI Pipeline

The CI workflow runs tests on **two Android emulators** using a matrix strategy:

- **API 23** (minSdk) - Validates compatibility with the minimum supported Android version
- **API 30** - Validates compatibility with an intermediate Android version
- **API 36** (targetSdk) - Tests against the target Android version

CI emulators use:
- Google APIs system image
- x86_64 architecture
- Nexus 6 device profile

### CI Tasks

1. **Dependency Updates Check**: Scans for available dependency updates
2. **Build & Test**: Runs `./gradlew build connectedCheck` on both API levels
3. **Android ELF Alignment Check**: Validates APK alignment

**Note**: When updating `minSdk` or `targetSdk` in `app/build.gradle.kts`, the matrix API levels in `.github/workflows/android.yml` must be manually updated to match.

## Technical Details

- **Current Version**: 2.0.0+41
- **Compile/Target SDK**: 36 (Android 16 / Baklava)

### Key Dependencies

For specific version numbers, see [`gradle/libs.versions.toml`](gradle/libs.versions.toml)

**Build Tools:**
- Android Gradle Plugin
- Kotlin
- Kotlin Compose Compiler Plugin
- Kotlin Symbol Processing (KSP)
- Gradle

**Core Libraries:**
- AndroidX Core KTX
- AndroidX AppCompat
- Material Design

**UI:**
- Jetpack Compose (BOM)
- Compose Material3
- Activity Compose
- AndroidX TV Material
- AndroidX TV Foundation

**Architecture:**
- Hilt (Dependency Injection)
- AndroidX Navigation
- AndroidX Lifecycle (ViewModel, LiveData, Runtime)
- AndroidX DataStore Preferences
- AndroidX Fragment KTX

**Widget:**
- AndroidX Glance (App Widget & Material3)

**Testing:**
- JUnit 4
- MockK
- Kotlinx Coroutines Test
- AndroidX JUnit
- Espresso Core
- UI Automator

### Build Features

- R8 minification and resource shrinking enabled for release builds
- Java compiler warnings treated as errors
- Lint warnings treated as errors (with specific exceptions)

## License

- [GPL 3.0](gpl-3.0.md)
