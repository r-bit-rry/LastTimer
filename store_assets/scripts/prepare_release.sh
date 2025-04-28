#!/bin/bash
# Script to build and prepare LastTimer for Play Store submission

echo "=== LastTimer Play Store Preparation ==="
echo "Building release bundle..."

# Navigate to project directory
cd "$(dirname "$0")/.."

# Clean the project
./gradlew clean

# Build release app bundle
./gradlew bundleRelease

# Check if build was successful
if [ $? -eq 0 ]; then
  echo "App bundle created successfully!"
  
  # Find the AAB file
  AAB_PATH=$(find ./app/build/outputs/bundle/release -name "*.aab" -type f | head -n 1)
  
  if [ -n "$AAB_PATH" ]; then
    # Create release directory if it doesn't exist
    RELEASE_DIR="./release"
    mkdir -p "$RELEASE_DIR"
    
    # Copy to release directory
    cp "$AAB_PATH" "$RELEASE_DIR/"
    RELEASE_AAB=$(basename "$AAB_PATH")
    
    echo "App bundle copied to $RELEASE_DIR/$RELEASE_AAB"
    echo "Ready for Play Store submission!"
  else
    echo "Couldn't find the built AAB file."
    exit 1
  fi
else
  echo "Build failed! Check the logs above for errors."
  exit 1
fi
