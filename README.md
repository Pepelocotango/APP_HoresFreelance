# HoresFreelance

A native Android app for freelancers to track their work hours and generate invoices.

## Features

- 📅 **Calendar View**: Visual calendar showing work days
- ⏱️ **Time Tracking**: Record work hours by concept/project
- 📊 **Hours Summary**: View work hours by week, month, or custom range
- 💾 **Local Storage**: All data stored offline on device
- 📥 **Export**: Export hours as CSV and PDF for invoicing

## Requirements

- Android 8.0+ (API level 26)
- JDK 17
- Android SDK 35
- Gradle 8.0+

## Building the App

### Prerequisites

1. Clone the repository:
```bash
git clone https://github.com/peplx/hores-freelance-android.git
cd hores-freelance-android
```

2. Ensure you have Android SDK installed and `ANDROID_SDK_ROOT` configured

### Build Debug APK

```bash
./gradlew assembleDebug
```

The debug APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK

```bash
./gradlew assembleRelease
```

The release APK will be located at: `app/build/outputs/apk/release/app-release-unsigned.apk`

## Installation

### Via Android Studio

1. Open the project in Android Studio
2. Connect an Android device or start an emulator
3. Click "Run" or press `Shift + F10`

### Via Command Line

```bash
# Install debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Or install release APK
adb install app/build/outputs/apk/release/app-release-unsigned.apk
```

## Running Tests

```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Project Structure

```
HoresFreelance/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/freelance/hores/
│   │   │   │   ├── HoresApp.kt              # Application class
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── db/                 # Room database
│   │   │   │   │   ├── repository/         # Data layer
│   │   │   │   │   └── export/             # CSV/PDF exporters
│   │   │   │   ├── domain/
│   │   │   │   │   └── model/              # Domain models
│   │   │   │   └── ui/
│   │   │   │       ├── screen/             # UI screens
│   │   │   │       ├── component/          # Reusable components
│   │   │   │       └── theme/              # Material 3 theme
│   │   │   └── res/                        # Resources
│   │   ├── test/                           # Unit tests
│   │   └── androidTest/                    # Instrumented tests
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── .github/workflows/
    └── android.yml                         # CI/CD workflow
```

## Architecture

The app uses **MVVM + Repository Pattern**:

- **Data Layer**: Room database, DAOs, Repository
- **Domain Layer**: Business models
- **UI Layer**: Jetpack Compose screens with ViewModels

## Technologies

- **UI**: Jetpack Compose with Material Design 3
- **Database**: Room 2.6
- **Architecture**: MVVM + Repository Pattern
- **DI**: Hilt
- **Navigation**: Navigation Compose
- **Coroutines**: Kotlin Coroutines + StateFlow

## CI/CD

GitHub Actions automatically:
- Runs unit tests on push/PR
- Builds debug and release APKs
- Uploads APK artifacts

Workflow file: `.github/workflows/android.yml`

## Usage

1. **Add Work Day**: Tap the "+" button to record a new work day
2. **Enter Hours**: Add concepts (projects) and time ranges
3. **View Summary**: Navigate to Summary tab to view hours by period
4. **Export**: Export work hours as CSV or PDF for invoicing

## License

MIT License - See LICENSE file for details

## Contributing

Contributions are welcome! Please:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Author

Created as a freelance hours tracking solution.

## Support

For issues, questions, or suggestions, please open an issue on GitHub.
