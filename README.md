App-Macros
---
Application to control third party applications using ADB broadcast commands.

## Features
- Can control supported applications (Tiktok, Instagram and Novinky.cz)
- Can return values from the applications
- Somewhat extensible

## Requirements
- Android device (App is built for sdk version 31+, it did work on 33 aswell but It might not in the future) with usb debugging enabled
- ADB installed on your computer
- Compatible JDK version (I have tested it with 24.0.1, but it should work with 17+)


## Installation
1. Clone the repository
2. Build the project
```bash
# might need to be given execute permissions
# chmod +x gradlew
./gradlew assembleDebug
```
3. Connect your Android device to your computer and make sure USB debugging is enabled
4. Run the project
```bash
# might fail if you have not set up the environment variables for adb
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Tested environment
- Xiaomi Redmi Note 9s (I did also test it on Samsung A54 but It was less reliable)
- Android 13
- ADB 1.0.41
- JDK 24.0.1
- Arch Linux with kernel Linux 6.14.6-arch1-1 (however it did also work on Windows 10 and Ubuntu 22.04 in WSL)


## Usage
- It is recommended to use with https://github.com/Aneeeesu/Ad-Analyzer-Server
- You can use the app without the server, but you will have to send broadcasts manually
- Available broadcasts are listed when you start up the service in adb logcat