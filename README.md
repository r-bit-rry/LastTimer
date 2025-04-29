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
  - Clear separation of concerns with UI, business logic, and data layers
  - Improved testability through decoupled components
  - Enhanced lifecycle management with ViewModels
- **Repository Pattern** for data management
  - Single source of truth for all data operations
  - Abstracts data sources from the rest of the application
  - Implements `ITimerRepository` interface for better testability
- **Single Activity** with multiple fragments using **Jetpack Navigation**
  - More efficient resource utilization
  - Simplified app lifecycle
  - Type-safe navigation with SafeArgs
- **Reactive Programming Model**
  - Leverages Kotlin Flow for reactive state updates
  - Ensures UI is always synchronized with underlying data

### Tech Stack

#### Core Components
- **Kotlin** (1.9.0+) as primary language
  - Extension functions for enhanced readability
  - Type safety and null safety features
  - Coroutines for asynchronous programming
- **Jetpack Compose** for modern declarative UI development
  - Composable functions for all UI elements
  - State hoisting and remember for state management
  - Custom theming with Material 3 implementation

#### Data Management
- **Room Database** for local data persistence
  - Type converters for complex data types
  - Relationship management between entities
  - DAO pattern for database access
- **DataStore** for preferences
  - Type-safe preferences using Kotlin Flow
  - Reactive updates for settings changes
  - Separate module for isolation of settings logic

#### Concurrency & Async
- **Kotlin Coroutines** for asynchronous operations
  - CoroutineScope for lifecycle-aware operations
  - SupervisorJob for error isolation
  - Dispatchers for appropriate threading
- **Flow** for reactive streams
  - Used throughout the app for reactive updates
  - StateFlow for UI state management
  - SharedFlow for events

#### Dependency Injection
- **Hilt** for dependency injection
  - Application-level and Activity-level scopes
  - EntryPoint for injection into non-injectable classes
  - ViewModelInject for ViewModels

#### UI Framework
- **Material Design 3** with dynamic color support
  - Custom theme implementation with MaterialTheme
  - Adaptive layouts for different screen sizes
  - Dynamic color support on Android 12+
- **Animation & Transitions**
  - Compose animations for smooth UI interactions
  - Material motion patterns for cohesive experience

#### Background Processing
- **Foreground Service** for timer management
  - Ensures timers continue running when app is in background
  - Provides user notification for active timers
  - WakeLock implementation for device sleep handling
- **BroadcastReceivers** for system events
  - Boot completed receiver for restoring timers after device restart
  - Action receivers for notification interactions

#### Performance Optimization
- **Custom Performance Monitoring**
  - Memory usage tracking
  - UI render time measurement
  - File system optimization
- **StrictMode** for development debugging
  - Detect and log potential performance issues
  - Thread and VM policy enforcement

### Data Structure

#### Timer System
- **Timer Entity**: Base class for all timer types
  - Unified model for different timer types (Countdown, Stopwatch, DateCountdown)
  - Status tracking (Running, Paused, Completed, Idle)
  - Rich metadata (creation time, last used time, category)
  - Support for template functionality
  - Configuration for sound and vibration per timer

- **TimerGroup Entity**: For cascading and sequential timers
  - Group level configuration (autoStartNext, repeatGroup)
  - Ordered timer execution with position tracking
  - Flexible duration overrides for timers within groups
  - Group-level metadata and statistics

- **TimerLap Entity**: For stopwatch lap tracking
  - Timestamp and elapsed time storage
  - Association with parent timer
  - Sequential lap numbering

#### Preference System
- **Settings Preferences**: User configuration storage
  - Theme preferences (Light, Dark, System)
  - Sound and vibration settings
  - Screen behavior settings

### Component Design

1. **Core Timer Engine** (`TimerService.kt`):
   - Foreground service implementation for background operation
   - Wake lock management for device sleep handling
   - CountDownTimer implementation for accurate timing
   - Support for all timer types with specialized handling
   - Notification management with media controls
   - Timer group orchestration with sequential execution

2. **Timer Interaction System**:
   - `TimerActionReceiver` for handling timer events
   - Dependency injection with Hilt EntryPoints
   - Platform-specific implementations (vibration, sound)
   - Boot restoration with `BootCompletedReceiver`

3. **Data Access Layer**:
   - Room Database with type converters
   - Repository pattern implementation
   - Reactive data access with Kotlin Flow
   - CRUD operations for all entity types

4. **Performance & Monitoring**:
   - Custom monitoring tools for memory and performance
   - Image loading optimization
   - File system management
   - Time utility functions for consistent formatting

## Development Roadmap & Implementation Notes

### Phase 1: Core Architecture & Foundation ✅
- [x] Project initialization and repository setup
- [x] Architecture design (MVVM + Repository pattern)
- [x] Room database implementation with entity relationships
- [x] Dependency injection setup with Hilt
- [x] Core timer service implementation:
  - Foreground service with notifications
  - Wake lock management
  - System reboot recovery

### Phase 2: Timer Types & Core Functionality ✅
- [x] Base Timer model with shared functionality
- [x] Countdown timer implementation
  - Accurate time tracking with CountDownTimer
  - Pause, resume, and reset functionality
  - Database persistence
- [x] Stopwatch implementation
  - Elapsed time tracking with System.elapsedRealtime()
  - Lap recording functionality
  - Unlimited duration support
- [x] Date countdown implementation
  - Target date visualization
  - Dynamic remaining time calculation
  - Date picker integration

### Phase 3: Advanced Timer Features ✅
- [x] Timer groups implementation
  - Sequential timer execution
  - Position tracking within groups
  - Automatic progression between timers
- [x] Repeating timers
  - Individual timer repeat
  - Group repeat functionality
  - Custom repeat count option
- [x] Timer templates system
  - Save common configurations
  - Quick template application
  - Template management UI
- [x] Timer categorization
  - Custom categories
  - Filtering by category
  - Category management

### Phase 4: UX & Platform Integration ✅
- [x] Notification system
  - Media controls in notifications
  - Lock screen controls
  - Expandable notification design
- [x] Theme system implementation
  - Material 3 design system
  - Dynamic color support
  - Dark/light/system theme options
- [x] Settings framework
  - DataStore preferences implementation
  - Sound and vibration options
  - Display preferences
- [x] System integration
  - Boot completed receiver
  - Power management optimizations
  - Android lifecycle handling

### Phase 5: Performance & Optimization ✅
- [x] Memory optimization
  - Custom memory monitoring
  - Resource cleanup
  - Image loading optimization
- [x] Battery usage optimization
  - Wake lock management
  - Background processing efficiency
  - Timer state restoration
- [x] UI performance
  - Compose optimization techniques
  - Render time monitoring
  - Recomposition minimization

### Phase 6: Polish & Release ✅
- [x] Final UI refinement
  - Animation polish
  - Typography consistency
  - Accessibility improvements
- [x] Comprehensive testing
  - Unit test implementation
  - UI testing with Compose testing framework
  - Device compatibility testing
- [x] Production preparation
  - Crash reporting implementation
  - Analytics setup
  - Play Store assets preparation

### Current Development Focus
- [ ] Enhanced widget functionality
  - Multiple widget styles
  - Direct timer control from widgets
  - Customizable widget appearance
- [ ] Backup and restore
  - Export/import timer configurations
  - Cloud backup integration
  - Migration utilities
- [ ] Advanced scheduling
  - Calendar integration
  - Recurring timer schedules
  - Time-of-day triggers

## Implementation Details & Design Decisions

### Key Architectural Decisions

#### 1. Foreground Service for Timer Operations
We chose to implement timers using a foreground service (`TimerService.kt`) rather than background tasks or WorkManager for several reasons:
- **Reliability**: Foreground services are less likely to be killed by the system
- **Accuracy**: Direct control over timing mechanisms
- **User Visibility**: Provides persistent notification for active timers
- **Lock Screen Controls**: Enables media-style controls when device is locked

#### 2. Database Schema Design
Our Room database implementation includes several key design decisions:
- **Three Core Entities**: Timer, TimerGroup, and TimerLap
- **Junction Tables**: Used for group-timer relationships
- **Type Converters**: Custom converters for complex types (dates, enums)
- **Normalized Structure**: Minimizes redundancy while maintaining query efficiency

#### 3. Sound & Vibration Implementation
The notification system for timer completion includes:
- **Device-Specific Vibration**: Adaptation for different Android versions
- **Default Sound Selection**: Using system notification sounds
- **User-Configurable Options**: Per-timer and global settings
- **Error Handling**: Graceful fallbacks if sound/vibration unavailable

#### 4. Reactive UI Architecture
The UI layer implements a fully reactive approach:
- **StateFlow for UI State**: Single source of truth for each screen
- **Unidirectional Data Flow**: Events flow down, state flows up
- **Recomposition Optimization**: Minimized UI updates
- **ViewModel State Hoisting**: Clear separation of UI and business logic

#### 5. Performance Optimization Strategy
Performance is optimized through:
- **Custom Monitoring Tools**: Track memory usage and render times
- **Image Loading Efficiency**: Caching and scaling
- **File System Management**: Cleaning temporary files
- **StrictMode Implementation**: Early detection of performance issues

### Testing Strategy
- **Unit Tests**: Focus on repository and ViewModel logic
- **Integration Tests**: Room database and service interactions
- **UI Tests**: Compose UI testing with test tags
- **Manual Test Plan**: Comprehensive scenarios for common use cases

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
   git clone https://github.com/yourusername/LastTimer.git
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

To switch between build variants:
1. Open the Build Variants panel in Android Studio
2. Select the desired build variant for the app module

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

### Performance Profiling
The app includes custom performance monitoring tools:
1. Enable monitoring in debug builds: `PerformanceOptimizationManager.configure(performanceTracking = true)`
2. Generate reports: `PerformanceOptimizationManager.generatePerformanceReport()`
3. Analyze reports for memory usage and render times

### Debugging Tips
- Use Logcat with tag filter: `LastTimer`
- Check timer service status: `adb shell dumpsys activity services com.lasttimer.app.service.TimerService`
- Monitor memory: `adb shell dumpsys meminfo com.lasttimer.app`

## Release Process

### CI/CD Pipeline
The project uses GitHub Actions for continuous integration and deployment:
- **Pull Request Validation**: Runs tests and lint checks on every PR
- **Nightly Builds**: Creates debug builds for internal testing
- **Release Builds**: Triggered manually for production releases

### Release Process Workflow
1. **Version Bump**:
   - Update version code and name in `app/build.gradle`:
     ```gradle
     android {
         defaultConfig {
             versionCode 10  // Increment for each release
             versionName "1.2.0"  // Semantic versioning
         }
     }
     ```

2. **Pre-release Checklist**:
   - Run the full test suite: `./gradlew test connectedAndroidTest`
   - Perform lint checks: `./gradlew lint`
   - Check ProGuard configuration: `./gradlew checkReleaseProguard`
   - Update `CHANGELOG.md` with release notes

3. **Release Build Generation**:
   - Generate a signed release bundle:
     ```bash
     ./store_assets/scripts/prepare_release.sh
     ```
   - This creates an App Bundle (AAB) in the `app/build/outputs/bundle/release/` directory

4. **Release Testing**:
   - Upload to Play Store internal testing track
   - Verify core functionality on multiple devices
   - Check upgrade path from previous version
   - Validate all features on target API levels

5. **Play Store Deployment**:
   - Update store listing with new screenshots if needed
   - Update release notes in Play Console
   - Upload the signed AAB
   - Publish to production (or staged rollout)

### Store Assets Management
The `store_assets` directory contains all assets needed for Play Store listing:

- **metadata/**: 
  - `description.txt`: Full app description (formatted for Play Store)
  - `privacy_policy.txt`: Privacy policy document
  - `release_notes.txt`: Current version release notes
  - `keywords.txt`: SEO keywords for store listing
  - `submission_checklist.txt`: Pre-submission verification items

- **graphics/**: 
  - Feature graphic (1024×500)
  - App icon (512×512)
  - Promo graphics

- **screenshots/**: 
  - Phone screenshots (16:9 and 18:9 ratios)
  - Tablet screenshots (16:10 ratio)
  - Specialized screenshots showing key features

- **scripts/**: 
  - `prepare_release.sh`: Automates release bundle creation
  - Handles signing and Bundle Tool optimization

## Contributing

### Development Workflow
1. **Fork the repository** on GitHub
2. **Create a feature branch** from `develop`:
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```
3. **Implement your changes** following the guidelines below
4. **Commit changes** with clear, descriptive messages:
   ```bash
   git commit -m "feat: add new timer animation"
   ```
5. **Push your branch** to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```
6. **Submit a Pull Request** against the `develop` branch

### Code Style Guidelines
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Document public APIs with KDoc comments
- Maximum line length: 100 characters
- Use 4 spaces for indentation (no tabs)

### Architecture Guidelines
- **MVVM Pattern**:
  - ViewModels should not have Android dependencies
  - Use LiveData or StateFlow for observable data
  - UiState classes for view state representation
- **Repository Pattern**:
  - All data access through repository interfaces
  - Implement caching where appropriate
  - Expose data as Flow for reactive updates
- **Dependency Injection**:
  - Use constructor injection where possible
  - Provide bindings in appropriate Hilt modules
  - Use qualifiers to disambiguate similar types

### Testing Requirements
- **Unit Tests**: Required for ViewModels and Repositories
- **UI Tests**: Recommended for key user flows
- **Test Coverage**: Aim for minimum 70% code coverage
- **TDD Approach**: Write tests before implementation where possible

### Pull Request Checklist
- [ ] Code follows style guidelines
- [ ] Tests added/updated for new functionality
- [ ] Documentation updated
- [ ] Verified on multiple API levels
- [ ] No lint warnings introduced
- [ ] Performance impact considered

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
1. Check existing [Issues](https://github.com/yourusername/LastTimer/issues) before creating new ones
2. Use the [Discussions](https://github.com/yourusername/LastTimer/discussions) tab for general questions
3. Contact the maintainers at: email@example.com
