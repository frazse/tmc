#!/bin/bash
set -e

# Configuration
NDK_PATH="/home/azu/Android/Sdk/ndk/30.0.14904198"
SDK_BUILD_TOOLS="/home/azu/Android/Sdk/build-tools/35.0.0"
BASE_APK="app-arm64-v8a-release.apk"
LIB_NAME="libmain.so"

# Architecture selection (default to x86_64 for emulator, can pass arm64-v8a)
ARCH="${1:-x86_64}"
OUTPUT_APK="tmc-$ARCH.apk"

echo "=== 1. Building libmain.so for Android ($ARCH) ==="
# Configure xmake for Android
xmake f -p android -a "$ARCH" --ndk="$NDK_PATH" -y

# Build the target
xmake build tmc_android

# Path to the built library
BUILT_LIB="build/android/libtmc_android.so"

if [ ! -f "$BUILT_LIB" ]; then
    echo "Error: Built library not found at $BUILT_LIB"
    exit 1
fi

echo "=== 2. Patching APK ==="
# Create temporary directory
TEMP_DIR="temp_apk_patch"
rm -rf "$TEMP_DIR"
mkdir -p "$TEMP_DIR"

# Copy base APK
cp "$BASE_APK" "$TEMP_DIR/base.apk"

# Update the library in the APK
# ZIP files can be updated by adding the file with the same relative path
mkdir -p "$TEMP_DIR/lib/$ARCH"
cp "$BUILT_LIB" "$TEMP_DIR/lib/$ARCH/$LIB_NAME"

cd "$TEMP_DIR"
# Remove old signature if present
zip -d base.apk "META-INF/*" || true
# Add the new library UNCOMPRESSED (required for Android 15+ if extractNativeLibs=false)
zip -0 -u base.apk "lib/$ARCH/$LIB_NAME"
cd ..

echo "=== 3. Signing APK ==="
# Generate debug keystore if missing
if [ ! -f "debug.keystore" ]; then
    echo "Generating debug keystore..."
    keytool -genkey -v -keystore debug.keystore -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=Android Debug,O=Android,C=US"
fi

# Align the APK
"$SDK_BUILD_TOOLS/zipalign" -v -f 4 "$TEMP_DIR/base.apk" "$OUTPUT_APK"

# Sign the APK
"$SDK_BUILD_TOOLS/apksigner" sign --ks debug.keystore --ks-pass pass:android --key-pass pass:android --out "$OUTPUT_APK" "$OUTPUT_APK"

echo "=== Done! Created $OUTPUT_APK ==="
echo "You can now install it with: adb install $OUTPUT_APK"

# Cleanup
rm -rf "$TEMP_DIR"
