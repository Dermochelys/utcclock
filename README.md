# UTC Clock

A simple, offline-only Android application that displays UTC (Coordinated Universal Time) with support for phones, tablets and Android TVs.

## Overview

- This is a single-module Android application built with Kotlin that provides an immersive fullscreen UTC time display.
- It features a hybrid UI approach combining traditional Android Views with Jetpack Compose components.
  - In the future, when [Jetpack Navigation 3](https://developer.android.com/guide/navigation/navigation-3) is out of alpha it will allow removal of Fragment use and a pure Compose architecture.  

## Features

- **Universal Time Display**: Clean, easy-to-read UTC clock
- **Multi-Platform Support**:
  - Android TV (leanback) optimized interface
  - Mobile devices with touchscreen support
- **Immersive Experience**: Edge-to-edge fullscreen display with system UI hiding
- **Modern Architecture**: Built with latest Android development best practices
- **Hybrid UI**: Combines ViewBinding and Jetpack Compose for optimal flexibility

## Requirements

- **Minimum SDK**: Android 6.0 (API 23)
- **Target SDK**: Android 15+ (API 36)
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
├── landing/                       # Entry point fragment
├── view/
│   ├── clock/                     # Main UTC clock display
│   ├── disclaimer/                # Legal disclaimer view
│   ├── fontlicense/               # Font licensing dialog
│   └── common/                    # Shared UI utilities
├── repository/                    # Data layer
└── di/                           # Dependency injection modules
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
# Run all tests
./gradlew test

# Run unit tests only
./gradlew testDebugUnitTest

# Run instrumented tests (requires emulator/device)
./gradlew connectedAndroidTest
```

### Testing Strategy

- **Unit Tests**: Fast, isolated tests using MockK for mocking
- **Instrumentation Tests**: Integration tests with Hilt for full dependency injection testing

## Continuous Integration

The project uses GitHub Actions for automated testing on every push and pull request to the main branch.

### CI Pipeline

The CI workflow runs tests on **two Android emulators** using a matrix strategy:

- **API 23** (minSdk) - Validates compatibility with the minimum supported Android version
- **API 36** (targetSdk) - Tests against the target Android version

Both emulators use:
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
- **Compile/Target SDK**: 36 (Android 15+)
- **Kotlin**: 2.2.21
- **Compose Compiler**: 1.5.15
- **Compose BOM**: 2025.11.01
- **Hilt Version**: 2.57.2
- **Kotlin Symbol Processing (KSP)**: 2.2.21-2.0.4
- **Android Gradle Plugin**: 8.13.1
- **Gradle**: 9.2.1
- **Build Features**:
  - R8 minification and resource shrinking enabled for release builds
  - Java compiler warnings treated as errors
  - Lint warnings treated as errors (with specific exceptions)

## License

- [GPL 3.0](gpl-3.0.md)
