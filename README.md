<img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp" width="96" alt="Fluid Notes Icon" />

# Fluid Notes

A simple, native notes app for Android 12+, built with Kotlin and Jetpack Compose.

## Features

- **Split pane view** on large displays: note list on the left, editor on the right
- **Automatic URL detection** in notes
- **Cursor mode**
- **Frosted, blurred UI** for the top bar and menus

## Limitations

Some features are unavailable on low-RAM devices (those that report `ActivityManager.isLowRamDevice`, such as Android Go devices):

- The frosted glass (Haze) blur effect is turned off to keep the UI smooth.
- Split pane view is unavailable because the system doesn't support it on low-RAM devices.

## Download

Grab the latest APK from the [Releases](../../releases) page.
Requires Android 12 (API 31) or higher.

## Build from source

1. Clone the repo:

```
git clone https://github.com/Chung1045/FluidNotes.git
```

2. Open it in Android Studio.
3. Let Gradle sync, then run the `app` configuration on a device or emulator.

## Built with

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose) and Material 3
- [Haze](https://github.com/chrisbanes/haze) for blur effects

## License
Released under the [MIT License](LICENSE).
