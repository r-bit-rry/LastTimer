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

## Technical Architecture

### App Architecture
- **MVVM (Model-View-ViewModel)** architecture pattern
- **Repository Pattern** for data management
- **Single Activity** with multiple fragments for different screens
- **Navigation Component** for handling navigation between screens

### Tech Stack
- **Kotlin** as primary language
- **Jetpack Compose** for modern UI development
- **Room Database** for local data persistence
- **Kotlin Coroutines** and **Flow** for asynchronous operations
- **Hilt** for dependency injection
- **Material Design 3** for UI components
- **DataStore** for preferences
- **Glance** for widget implementation
- **WorkManager** for background tasks and notifications

### Data Structure
- **Timer Entity**: Base class for all timer types
  - TimerType (Countdown, Stopwatch, DateCountdown)
  - Name
  - Duration/Target
  - Status (Running, Paused, Completed)
  - Creation Date
  - Last Used Date
  - Category/Tags

- **TimerGroup Entity**: For cascading and grouped timers
  - Name
  - List of Timer IDs
  - Order information
  - Group settings (auto-start next, etc.)

### Component Design
1. **Core Timer Engine**:
   - Handles timer logic and state management
   - Provides accurate timing across app and widget contexts
   - Manages timer notifications and alerts

2. **UI Components**:
   - Main timer display components
   - Timer creation and configuration screens
   - Theme management
   - Widget configuration

3. **Data Management**:
   - Local database for storing timer configurations
   - Import/export functionality
   - Backup and restore capabilities

4. **Widget System**:
   - Different widget sizes and configurations
   - Real-time timer updates in widgets
   - Widget-to-app communication

## Development Roadmap

### Phase 1: Project Setup and Core Functionality
- [x] Project initialization
- [x] Basic project structure and dependencies setup
- [x] Core timer engine implementation
- [x] Basic UI for single countdown timer
- [x] Settings framework

### Phase 2: Multiple Timer Types
- [x] Stopwatch implementation
- [x] Date countdown implementation
- [x] Tab-based navigation between timer types
- [x] Timer creation and configuration screens

### Phase 3: Advanced Timer Features
- [x] Cascading timers implementation
- [x] Repeating timers (single timers only, group repeating in progress)
- [x] Timer naming and categorization
- [x] Timer templates and presets

### Phase 4: UI Enhancements and Extensions
- [x] Theme implementation (dark/light)
- [x] Basic Android widgets
- [x] Lock screen controls
- [x] UI polish and animations

### Phase 5: Finalization and Publishing
- [x] Testing and bug fixing
- [x] Performance optimization
- [x] Play Store listing preparation
- [x] Initial release

## Getting Started

### Prerequisites
- Android Studio Iguana (2023.2.1) or newer
- Kotlin 1.9.0 or newer
- Gradle 8.0 or newer
- Android SDK 34 (min SDK 26)

### Building and Running
1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Build and run on an emulator or physical device

## Release Process

### Play Store Submission
The `store_assets` directory contains all necessary files for Play Store listing:

- **metadata/**: App descriptions, privacy policy, and release notes
- **graphics/**: Guidelines for feature graphics and screenshots
- **screenshots/**: App screenshots for different devices
- **scripts/**: Helper scripts for the release process

To generate a Play Store bundle:

```bash
./store_assets/scripts/prepare_release.sh
```

This will create an AAB file in the `release` directory ready for Play Store submission.

### Preparing a New Release
1. Update the version code and name in `app/build.gradle`
2. Update release notes in `store_assets/metadata/release_notes.txt`
3. Run tests to ensure everything works: `./gradlew test`
4. Generate the release bundle using the script above
5. Test the release bundle using Google Play's internal testing track
6. Submit for review following the submission checklist

## Contributing
- Follow MVVM architecture
- Add unit tests for new features
- Follow the Material Design guidelines
- Use Kotlin coding conventions

## License
This project is licensed under the MIT License - see the LICENSE file for details
