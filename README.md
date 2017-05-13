home-assistant-Android
======================

[![Platform Android](https://img.shields.io/badge/Platform-Android-6ab344.svg?style=flat)](https://developer.android.com/)
[![CircleCI](https://circleci.com/gh/Maxr1998/home-assistant-Android.svg?style=svg)](https://circleci.com/gh/Maxr1998/home-assistant-Android)
[![GitHub issues](https://img.shields.io/github/issues/Maxr1998/home-assistant-Android.svg)](https://github.com/Maxr1998/home-assistant-Android/issues)
[![Crowdin](https://d322cqt584bo4o.cloudfront.net/home-assistant-android/localized.svg)](https://crowdin.com/project/home-assistant-android)
[![License GPLv3](https://img.shields.io/badge/license-GPLv3-red.svg?style=flat)](https://github.com/Maxr1998/home-assistant-Android/blob/master/LICENSE)
[![Twitter](https://img.shields.io/twitter/url/https/twitter.com/home_assistant.svg?style=social)](https://twitter.com/home_assistant)


## Downloads
[<img src="https://f-droid.org/badge/get-it-on.png"
      alt="Get it on F-Droid"
      height="80">](https://f-droid.org/app/io.homeassistant.android)

- [GitHub releases page](https://github.com/Maxr1998/home-assistant-Android/releases)

- [Latest CI build](https://circleci.com/api/v1.1/project/github/Maxr1998/home-assistant-Android/latest/artifacts/0/$CIRCLE_ARTIFACTS/hass-ci-unsigned.apk)

## Getting Started

Build in terminal with `./gradlew assembleRelease` or import into Android Studio and build there (recommended).

## Dependencies

### Model & UI

- [Android support libraries](https://developer.android.com/topic/libraries/support-library/index.html): Used for a lot of classes, views, etc.
- [Ason](https://github.com/afollestad/ason): A really awesome JSON library building on top of Android's integrated JSON classes.

### Networking

- [OkHttp](http://square.github.io/okhttp/): Used for WebSocket integration in an elegant and fast way.

## App signature

Currently, the app is configured to be signed with my personal keys on my computer, but it will use debug keys if the signing config isn't found.
Depending on future maintainance, there will be different keys used for this.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md)

## LICENSE

GPLv3

## Credits

The format and some content of this README.md comes from the [home-assistant-iOS](https://github.com/home-assistant/home-assistant-iOS) repository.
