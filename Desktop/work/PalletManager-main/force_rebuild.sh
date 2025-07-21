#!/bin/bash

echo "🔥 FORCING COMPLETE REBUILD OF PALLETMANAGER APP"
echo "================================================"

cd android-app

echo "1. Cleaning build directories..."
rm -rf app/build/
rm -rf build/
rm -rf .gradle/

echo "2. Cleaning Android Studio caches..."
rm -rf ~/.gradle/caches/
rm -rf ~/.android/build-cache/

echo "3. Removing any cached APKs..."
find . -name "*.apk" -delete
find . -name "*.aab" -delete

echo "4. Creating build directories..."
mkdir -p app/build
mkdir -p build

echo "5. Listing current MainScreen content..."
echo "Current MainScreen.kt first 10 lines:"
head -10 app/src/main/java/com/dollargeneral/palletmanager/ui/screens/MainScreen.kt

echo ""
echo "🚀 REBUILD COMPLETE!"
echo "================================================"
echo "Now in Android Studio:"
echo "1. File → Invalidate Caches and Restart"
echo "2. Build → Clean Project"
echo "3. Build → Rebuild Project"
echo "4. Run the app"
echo ""
echo "You should see:"
echo "- App name: '🚀 NEW STATION UI'"
echo "- Red primary colors"
echo "- Yellow backgrounds"
echo "- Header: '🎯 STATION LOOKUP'"
echo "- Large station input field"
echo ""
echo "If you still see the old UI, the issue is with Android Studio caching!"
