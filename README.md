# LastTimer

A modern Android timer application with a minimalist design that provides powerful timing capabilities.

## Features

- **Multiple Timer Types**:
  - 🕒 Countdown Timers: Set timers for specific durations
  - ⏱️ Stopwatch: Track elapsed time with lap functionality
  - 📅 Date Countdown: Count down to specific dates and times

- **Advanced Timer Features**:
  - ⛓️ Cascading Timers: Chain multiple timers to run sequentially (perfect for workouts)
  - 🔄 Repeating Timers: Set timers that automatically restart
  - 📝 Custom Timer Names: Name and categorize your timers
  - 🔖 Timer Templates: Save and reuse common timer configurations

- **UI/UX**:
  - 🧩 Android Widgets: Control timers from your home screen
  - 🔒 Lock Screen Controls: Manage timers without unlocking your device
  - 🌓 Dark/Light Theme: Toggle between dark and light mode
  - 📱 Tab-based Navigation: Separate tabs for different timer types

## Technical Architecture Overview

### App Architecture
- **MVVM (Model-View-ViewModel)** architecture pattern
- **Repository Pattern** for data management
- **Single Activity** with multiple fragments using **Jetpack Navigation**
- **Reactive Programming Model** with Kotlin Flow

### Tech Stack

- **Core**: Kotlin, Jetpack Compose, Material Design 3
- **Data**: Room Database, DataStore
- **Async**: Kotlin Coroutines, Flow
- **DI**: Hilt
- **Background**: Foreground Service, BroadcastReceivers

For detailed technical information, implementation decisions, and current development status, please refer to the [Ledger.md](Ledger.md) file.

## Getting Started

### Prerequisites
- **Android Studio**: Arctic Fox (2021.3.1) or newer (Iguana 2023.2.1 recommended)
- **Kotlin**: 1.9.0 or newer with Coroutines support
- **Gradle**: 8.0 or newer with Kotlin DSL
- **Android SDK**: 
  - Minimum SDK: 26 (Android 8.0 Oreo)
  - Target SDK: 34 (Android 14)
- **Java Development Kit**: JDK 17
- **Git**: For version control

### Development Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/r-bit-rry/LastTimer.git
   cd LastTimer
   ```

2. Open the project in Android Studio:
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory
   
3. Configure local properties:
   - Create a `local.properties` file in the root directory if not automatically created
   - Add SDK location: `sdk.dir=/path/to/your/Android/sdk`
   
4. Sync Gradle files:
   - Click the "Sync Project with Gradle Files" button in Android Studio
   - Wait for the sync to complete and indexing to finish
   
5. Setup environment:
   - Ensure you have Android SDK 34 installed via SDK Manager
   - Install any missing Android SDK components when prompted

### Build Variants
The project includes several build variants:
- **debug**: Development build with logging and StrictMode enabled
- **release**: Production build with optimizations
- **benchmark**: Special build for performance testing

### Running Tests
- **Unit Tests**: `./gradlew test`
- **Instrumented Tests**: `./gradlew connectedAndroidTest`
- **All Tests**: `./gradlew testDebugUnitTest connectedDebugAndroidTest`

### Building the Application
To build a debug APK:
```bash
./gradlew assembleDebug
```

To build a release APK:
```bash
./gradlew assembleRelease
```

The built APKs can be found in:
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

### Debugging Tips
- Use Logcat with tag filter: `LastTimer`
- Check timer service status: `adb shell dumpsys activity services com.lasttimer.app.service.TimerService`
- Monitor memory: `adb shell dumpsys meminfo com.lasttimer.app`

## Release Process Overview

The project uses GitHub Actions for CI/CD with:
- Pull Request Validation
- Nightly Builds
- Manual Release Builds

For detailed information about the release process, please see the [Ledger.md](Ledger.md) file.

## Contributing

We welcome contributions to LastTimer! Before contributing, please read our [Contributing Guidelines](CONTRIBUTING.md) for information on our development workflow, coding standards, and pull request process.

## Project Documentation

- [README.md](README.md): Project overview and getting started (this file)
- [CONTRIBUTING.md](CONTRIBUTING.md): Guidelines for contributing to the project
- [Ledger.md](Ledger.md): Detailed technical information, development roadmap, and current tasks

## License & Attribution
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

### Third-Party Libraries
- **Room**: Database ORM from Android Architecture Components
- **Hilt**: Dependency injection by Google
- **Material Components**: UI components following Material Design
- **Kotlin Coroutines**: Asynchronous programming library
- **Compose**: Declarative UI toolkit

## Support & Contact
For questions, issues, or contributions, please:
1. Check existing [Issues](https://github.com/r-bit-rry/LastTimer/issues) before creating new ones
2. Use the [Discussions](https://github.com/r-bit-rry/LastTimer/discussions) tab for general questions
