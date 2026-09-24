# Snapmaker J1 Screen App

Android touchscreen control application for the Snapmaker J1 dual-extruder (IDEX) 3D printer.

The app provides a native Android interface to control 3D printing workflows directly from the device touchscreen, including independent left/right extruder management for the J1's IDEX print head.

## Supported Devices

- **Snapmaker J1** — Dual-extruder IDEX 3D printer

## Features

- Dual-extruder (IDEX) printing with independent left/right extruder control
- Duplication and mirror printing modes
- Wi-Fi and USB file transfer
- Real-time machine status monitoring
- Remote control via HTTP API (compatible with Orca Slicer remote connect)
- Firmware over-the-air (OTA) updates
- Multi-language UI support

## Project Structure

```
apps/
  j1/            J1 main application
features/
  print/         3D print workflow (extruder adjustment, duplication/mirror modes)
  settings/      Device settings and firmware update
  machine-tools/ Machine control and calibration
  file-manager/  File browsing and transfer
  remote/        Remote control features
  home/          Home screen
  guide/         Onboarding and setup guide
  welcome/       First-time setup wizard
  add-ons/       Accessory management
platform/
  base/          Core platform layer (services, machine models, connection)
  core/          UI framework and shared components
  lib/           Utility libraries (serial port, checksum, etc.)
buildSrc/        Gradle dependency version catalog
ijkplayer_java/  Video player integration
```

## Dependencies

Essential third-party libraries:

- [ButterKnife](https://github.com/JakeWharton/butterknife) — view binding
- [RxJava](https://github.com/ReactiveX/RxJava) — reactive state management
- [RxAndroid](https://github.com/ReactiveX/RxAndroid) — Android main thread scheduler
- [Okio](https://github.com/square/okio) — byte array and I/O processing
- [AndServer](https://github.com/yanzhenjie/AndServer) — embedded HTTP server for remote commands
- [Retrofit 2](https://github.com/square/retrofit) — HTTP API client
- [ARouter](https://github.com/alibaba/ARouter) — in-app routing
- [Firebase Crashlytics](https://firebase.google.com/products/crashlytics) — crash reporting
- [ijkplayer](https://github.com/bilibili/ijkplayer) — video playback (native libraries not included, see note below)

> **Note on ijkplayer native libraries:** The prebuilt ijkplayer shared libraries (`libijkplayer.so`, `libijkffmpeg.so`, `libijksdl.so`) are not included in this repository. Video playback features require them — build them from the [ijkplayer](https://github.com/bilibili/ijkplayer) source for `armeabi-v7a` and place them under `platform/base/libs/armeabi-v7a/`. The project builds normally without them.

## Prerequisites

- **Android Studio** 3.5+
- **JDK** 1.8
- **Android NDK** 22.1.7171670
- **Gradle** 5.4.1+ (wrapper included)

## Getting Started

### 1. Clone the repository

```bash
git clone https://github.com/Snapmaker/Snapmaker-J1-Screen-App.git
cd Snapmaker-J1-Screen-App
```

### 2. Configure Firebase (optional)

Firebase Crashlytics and Analytics are used by default. If you want to use Firebase services, copy the example template and fill in your own Firebase project credentials:

```bash
cp apps/j1/google-services.json.example apps/j1/google-services.json
```

If you do not need Firebase, remove the `com.google.gms.google-services` and `com.google.firebase.crashlytics` plugins from `apps/j1/build.gradle`, and remove the Firebase dependency entries.

### 3. Configure signing (optional)

Debug builds do not require signing configuration. For release builds, create a keystore and configure signing credentials via environment variables or `gradle.properties`:

```bash
export KEYSTORE_PATH=/path/to/your.keystore
export KEYSTORE_PASSWORD=your_store_password
export KEY_ALIAS=your_key_alias
export KEY_PASSWORD=your_key_password
```

Alternatively, add these to your `~/.gradle/gradle.properties` or project-level `local.properties`:

```properties
KEYSTORE_PATH=/path/to/your.keystore
KEYSTORE_PASSWORD=your_store_password
KEY_ALIAS=your_key_alias
KEY_PASSWORD=your_key_password
```

### 4. Build

```bash
# Build the J1 debug APK
./gradlew :apps:j1:assembleDebug

# Build all variants
./gradlew assembleDebug
```

## Style Guide

```java
class FooFragment extends BaseFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // initialize
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_foo;
    }

    private void initView() {

    }

    private void initData() {

    }

    private void handleBar() {

    }

    @OnClick(R.id.btn_baz)
    void onClickBaz() {

    }
}
```

## Author

Snapmaker Software Team

## License

TBD
