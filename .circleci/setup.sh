#!/usr/bin/env bash
echo -e "sdk.dir=${ANDROID_HOME}" > local.properties

mkdir -p ${ANDROID_HOME}/licenses || true
rm ${ANDROID_HOME}/licenses/* || true

echo -e "8933bad161af4178b1185d1a37fbf41ea5269c55\c" > ${ANDROID_HOME}/licenses/android-sdk-license
echo -e "79120722343a6f314e0719f863036c702b0e6b2a\n84831b9409646a918e30573bab4c9c91346d8abd\c" > ${ANDROID_HOME}/licenses/android-sdk-preview-license

${ANDROID_HOME}/tools/bin/sdkmanager "extras;google;m2repository"

./gradlew --no-daemon dependencies || true
./gradlew --no-daemon clean