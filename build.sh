#!/usr/bin/env bash
# Usage : bash build.sh [URL_RAW_DE_url.txt]
# Même principe que Free My TV : aapt2 -> javac -> d8 -> zipalign -> apksigner, sans Gradle.
set -euo pipefail
cd "$(dirname "$0")"

CONFIG_URL="${1:-}"
SDK="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}"
[ -n "$SDK" ] || { echo "ANDROID_HOME non défini"; exit 1; }
BT="$(ls -d "$SDK"/build-tools/* | sort -V | tail -1)"
JAR="$(ls -d "$SDK"/platforms/android-*/ | sort -V | tail -1)android.jar"
echo "build-tools: $BT"; echo "android.jar: $JAR"

rm -rf build && mkdir -p build/obj
if [ -n "$CONFIG_URL" ]; then
  sed "s#__CONFIG_URL__#${CONFIG_URL}#" src/io/cinejoy/launcher/MainActivity.java > build/MainActivity.java
else
  cp src/io/cinejoy/launcher/MainActivity.java build/MainActivity.java
fi

"$BT/aapt2" compile --dir res -o build/res.zip
"$BT/aapt2" link -o build/base.apk -I "$JAR" --manifest AndroidManifest.xml \
  --min-sdk-version 21 --target-sdk-version 34 build/res.zip

javac --release 8 -classpath "$JAR" -d build/obj build/MainActivity.java
"$BT/d8" --release --lib "$JAR" --min-api 21 --output build $(find build/obj -name '*.class')

(cd build && zip -q base.apk classes.dex)
"$BT/zipalign" -f -p 4 build/base.apk build/aligned.apk

keytool -genkeypair -keystore build/ks.jks -storepass android -keypass android \
  -alias k -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=CinejoyLauncher" >/dev/null 2>&1
"$BT/apksigner" sign --ks build/ks.jks --ks-pass pass:android --key-pass pass:android \
  --out CinejoyLauncher.apk build/aligned.apk

echo "OK -> CinejoyLauncher.apk"
