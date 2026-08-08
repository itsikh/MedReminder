# MedReminder

An Android app for keeping track of medications and taking them on time.

- Schedule medications and get reminder notifications, with snooze support
- Track remaining stock and get low / critical stock warnings
- Review a history log of doses taken, skipped or missed
- Back up and restore your data, and report bugs from inside the app

Built with Kotlin, Jetpack Compose (Material 3), Hilt and Room.
Requires Android 8.0 (API 26) or newer.

The app needs notification permission to deliver reminders.

## Building

Building requires JDK 17.

```sh
./gradlew assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

The release APK (`./gradlew assembleRelease`) is written to `app/build/outputs/apk/release/app-release.apk`.

`build.sh` and `install.sh` are convenience wrappers that set `JAVA_HOME` first.

## Source layout

All application code lives in the `app/` module:

- `app/src/main/java/com/itsikh/medreminder/` — Kotlin sources
- `app/src/main/java/com/itsikh/medreminder/AppConfig.kt` — central app config
- `app/src/main/res/` — resources
