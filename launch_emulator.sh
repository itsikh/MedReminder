#!/bin/bash
# launch_emulator.sh — Run this from YOUR terminal to get the emulator window.
# Double-click in Finder or run: ./launch_emulator.sh

SDK_DIR="/Users/itsik-personal/Library/Android/sdk"
ADB="$SDK_DIR/platform-tools/adb"

# Kill any existing headless emulator
pkill -f "qemu-system-aarch64-headless" 2>/dev/null
pkill -f "qemu-system-aarch64 " 2>/dev/null
sleep 1

export ANDROID_HOME="$SDK_DIR"
export ANDROID_SDK_ROOT="$SDK_DIR"

echo "Starting Pixel 6 emulator (API 35)..."
"$SDK_DIR/emulator/emulator" \
  -avd Pixel6_API35 \
  -no-snapshot-load \
  -no-audio \
  -gpu swiftshader_indirect &

EMU_PID=$!
echo "Emulator PID: $EMU_PID"
echo "Waiting for boot..."

# Wait for ADB connection
for i in $(seq 1 30); do
  DEVICE=$("$ADB" devices 2>/dev/null | grep "emulator" | grep "device$")
  if [ -n "$DEVICE" ]; then
    echo "Connected: $DEVICE"
    break
  fi
  sleep 3
done

# Wait for full boot
"$ADB" wait-for-device shell \
  'while [[ -z $(getprop sys.boot_completed) ]]; do sleep 2; done; echo "Boot complete!"'

echo ""
echo "Installing app..."
"$ADB" install -r "$(dirname "$0")/app/build/outputs/apk/debug/app-debug.apk"

echo "Launching app..."
"$ADB" shell am start -n "com.itsikh.medreminder/.MainActivity"
echo "Done!"
