home-assistant-Android
======================

[![Platform Android](https://img.shields.io/badge/Platforms-Android-lightgray.svg?style=flat)](https://developer.apple.com/swift/)
[![GitHub issues](https://img.shields.io/github/issues/Maxr1998/home-assistant-Android.svg)](https://github.com/Maxr1998/home-assistant-Android/issues)
[![License MIT](https://img.shields.io/badge/license-GPLv3-red.svg?style=flat)](https://github.com/Maxr1998/home-assistant-Android/blob/master/LICENSE)
[![Twitter](https://img.shields.io/twitter/url/https/twitter.com/home_assistant.svg?style=social)](https://twitter.com/home_assistant)

## Getting Started

Build in terminal with `./gradlew build` or import into Android Studio (recommended).

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

See CONTRIBUTING.md

## LICENSE

GPLv3

## Credits

The format and some content of this README.md comes from the [home-assistant-iOS](https://github.com/home-assistant/home-assistant-iOS) repository.
