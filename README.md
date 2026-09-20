# Audio Effects Studio

System-wide audio effects for Android. **Audio Effects Studio** applies a configurable DSP chain to the system output mix and active audio sessions, with a Compose-based control surface for tuning playback in real time.

> **Status:** Early-stage project. Audio-effect availability and behavior depend on the Android version, device audio stack, and manufacturer implementation.

## Features

- Master enable/disable switch and A/B bypass mode
- Master gain and stereo balance controls
- Five-band equalizer:
  - 60 Hz
  - 230 Hz
  - 910 Hz
  - 3.6 kHz
  - 14 kHz
- Bass boost
- Virtual surround / virtualizer
- Loudness normalization
- Compressor with threshold, ratio, attack, release, and makeup gain
- Limiter with threshold, ratio, attack, release, and post-gain controls
- Built-in audio preview with spectrum and VU-meter visualizations
- Built-in and user-created presets
- Persistent preset storage using Room
- Foreground service with a notification and quick master-effect toggle
- Audio-session receiver for attaching effects to newly opened audio sessions

## How it works

The app creates Android `AudioEffect` instances through `AudioEffectsEngine`. It keeps session `0` attached for the global output mix and can attach to other audio sessions as they are opened by applications.

The processing chain uses Android platform effects where supported:

- `Equalizer`
- `BassBoost`
- `Virtualizer`
- `LoudnessEnhancer`
- `DynamicsProcessing` for master gain, compression, and limiting

The effects are hosted by `AudioEffectsService`, which runs as a media-playback foreground service so processing can continue while the app is not in the foreground.

## Requirements

- Android 7.0 (API 24) or newer
- Android Studio with Kotlin and Jetpack Compose support
- JDK 11 or newer
- Android SDK 36 for the current build configuration

`DynamicsProcessing` is available from Android 9 (API 28) onward. On devices that do not expose a particular platform effect, the app logs the failure and continues with the effects that are available.

## Getting started

1. Clone the repository:

   ```bash
   git clone https://github.com/djshaji/android-audio-effects-manager.git
   cd android-audio-effects-manager
   ```

2. Open the project in Android Studio.

3. Allow Gradle to synchronize and install the required Android SDK components.

4. Run the `app` configuration on an Android device or emulator.

The application ID is `org.acoustixaudio.sysfx`.

## Build from the command line

Build a debug APK with the Gradle wrapper:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\\gradlew.bat assembleDebug
```

The resulting APK is generated under:

```text
app/build/outputs/apk/debug/app-debug.apk
```

A debug APK is also included in the repository as `AudioEffects-debug.apk` when available.

## Configuration and secrets

The project is configured to read local secrets from `.env`. Start from `.env.example` when a local Gemini API key is required:

```dotenv
GEMINI_API_KEY=your_key_here
```

Do not commit real API keys, signing credentials, or other secrets. Release signing is configured through environment variables:

- `KEYSTORE_PATH`
- `STORE_PASSWORD`
- `KEY_PASSWORD`

## Permissions

The app declares permissions required for audio processing and its foreground notification:

- `MODIFY_AUDIO_SETTINGS`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
- `POST_NOTIFICATIONS`

On supported Android versions, grant notification permission so the foreground-service status and quick control remain visible.

## Project structure

```text
app/src/main/java/com/example/
├── data/       Room database, preset entities, DAO, and repository
├── model/      Audio-effect configuration models
├── service/    DSP engine, foreground service, preview, and session receiver
├── ui/         Compose screens, view model, components, and theme
└── MainActivity.kt
```

## Testing

Run the unit tests with:

```bash
./gradlew test
```

Run Android instrumented tests on a connected device or emulator with:

```bash
./gradlew connectedAndroidTest
```

## Device compatibility notes

Android audio effects are implemented by the platform and device vendor. As a result:

- Some effects may be unavailable or behave differently on different devices.
- Certain audio outputs, Bluetooth routes, USB DACs, or vendor audio enhancements may bypass or alter processing.
- The global output mix session may require vendor-specific support.
- Processing behavior should be verified with the device's own speakers, wired output, and Bluetooth devices before relying on it for critical listening.

## Contributing

Issues and pull requests are welcome. When reporting a device-specific problem, include:

- Device model and Android version
- Audio output route (speaker, wired, Bluetooth, USB, etc.)
- Which effect or preset was active
- Whether the issue affects all apps or only a particular player
- Relevant Logcat output

## License

No license file is currently included in this repository. Until a license is added, all rights are reserved by the copyright holder.
