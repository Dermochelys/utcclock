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

## Testing

```bash
# Run all tests
./gradlew test

# Run unit tests only
./gradlew testDebugUnitTest

# Run instrumented tests on connected device/emulator
./gradlew connectedAndroidTest

# Run both unit and instrumented tests
./gradlew clean build connectedCheck
```

### Testing Infrastructure

- **Unit Tests**: Use JUnit 4 and MockK for mocking. Located in `app/src/test/`.
- **Instrumented Tests**: Use Hilt for dependency injection testing with a custom `HiltTestRunner` (app/src/androidTest/java/com/dermochelys/utcclock/HiltTestRunner.kt).
- **Test DI Modules**: Test modules in `app/src/androidTest/java/com/dermochelys/utcclock/di/` use `@TestInstallIn` to replace production modules.
- **Test JVM Args**: Tests require `-XX:+EnableDynamicAgentLoading` for MockK compatibility (configured in app/build.gradle.kts).

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

## Development Requirements

- **JDK**: 21 (JVM toolchain version 21)
- **Target SDK**: 36 (Android 15+)
- **Min SDK**: 21 (Android 5.0)
- **NDK**: 29.0.14206865
- **Build Tools**: 36.0.0
- **Current Version**: 1.10.0+40

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
- **Lifecycle**: 2.9.4 (held at this version as 2.10.0+ require minApi 23+)
- **DataStore Preferences**: 1.1.7 (held at this version as 1.2.0+ require minApi 23+)
- **Activity Compose**: 1.11.0 (held at this version as 1.12.0+ require minApi 23+)

Note: Several AndroidX dependencies are intentionally held back to maintain compatibility with API 21+.

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
- Runs tests on API 36 emulator with Google APIs
- Runs `./gradlew build connectedCheck`
- Checks Android ELF alignment
