#!/bin/bash
echo "🚀 Downloading OpenCV 4.12.0..."
curl -L -o opencv.zip https://github.com/opencv/opencv/releases/download/4.12.0/opencv-4.12.0-android-sdk.zip

echo "📦 Unzipping..."
unzip -q opencv.zip

echo "📂 Renaming folder..."
rm -rf opencv
mv OpenCV-android-sdk opencv
rm opencv.zip

echo "🔧 Fixing build.gradle..."
cat <<EOF > opencv/sdk/java/build.gradle
apply plugin: 'com.android.library'

android {
    namespace 'org.opencv'
    compileSdkVersion 34
    defaultConfig {
        minSdkVersion 24
        targetSdkVersion 34
    }
    buildTypes {
        release {
            minifyEnabled false
        }
    }
    sourceSets {
        main {
            jniLibs.srcDirs = ['../native/libs']
            java.srcDirs = ['src']
            res.srcDirs = ['res']
            manifest.srcFile 'AndroidManifest.xml'
        }
    }
}
EOF
echo "✅ Done! Open Android Studio and Sync."
