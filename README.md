HassDroid - a Home Assistant Client for Android
===============================================

[![Platform Android](https://img.shields.io/badge/Platform-Android-6ab344.svg?style=flat)](https://developer.android.com/)
[![CircleCI](https://circleci.com/gh/Maxr1998/home-assistant-Android.svg?style=svg)](https://circleci.com/gh/Maxr1998/home-assistant-Android)
[![GitHub issues](https://img.shields.io/github/issues/Maxr1998/home-assistant-Android.svg)](https://github.com/Maxr1998/home-assistant-Android/issues)
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/home-assistant-android/localized.svg)](https://crowdin.com/project/home-assistant-android)
[![License GPLv3](https://img.shields.io/badge/license-GPLv3-red.svg?style=flat)](https://github.com/Maxr1998/home-assistant-Android/blob/master/LICENSE)

## PROJECT STATUS
This project has been inactive for quite some time now, and was de-facto already abandoned for a few months. Too many changes to the Home Assistant software, the API and finally the introduction of the custom "Lovelace" UI made supporting this app again more and more unlikely. Since there's an official Android app now, which wraps the web client, there's also no real need for this project anymore.

## Downloads
[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/app/io.homeassistant.android)

- [GitHub releases page](https://github.com/Maxr1998/home-assistant-Android/releases)

- [Latest CI build](https://circleci-tkn.rhcloud.com/api/v1/project/Maxr1998/home-assistant-Android/tree/master/latest/artifacts/hass-gms.apk)

## Getting Started

Build in terminal with `./gradlew assembleRelease` or import into Android Studio and build there (recommended).

## Dependencies

### Model & UI

- [Android support libraries](https://developer.android.com/topic/libraries/support-library/index.html): Used for a lot of classes, views, etc.
- [Ason](https://github.com/afollestad/ason): A really awesome JSON library building on top of Android's integrated JSON classes.
- Google Play services: Used for acquiring your location for the device_tracker feature and configuring the Wearable app.
- [Mapzen Lost](https://mapzen.com/blog/lets-get-lost/): alternative location provider used in the F-Droid builds, replacing Google Play services.
- JetBrains' Kotlin build tools and stdlib

### Networking

- [OkHttp](http://square.github.io/okhttp/): Used for WebSocket integration in an elegant and fast way.

## App signature

The official releases are signed with my personal keys, but gradle will use debug keys if the signing config/keys aren't found.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

## LICENSE

GPLv3

## Credits

The format and some content of this README.md comes from the [home-assistant-iOS](https://github.com/home-assistant/home-assistant-iOS) repository.
