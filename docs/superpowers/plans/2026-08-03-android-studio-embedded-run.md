# Android Studio Embedded Run Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the demo variant, fix reproducible build errors, and launch the app in the currently connected Android Studio API 36 emulator.

**Architecture:** Keep the root Gradle project and existing Compose application unchanged unless the build identifies a concrete source or configuration defect. Use Gradle for reproducible verification, ADB for deployment verification, and the existing Android Studio `Running Devices` tool window for display.

**Tech Stack:** Kotlin, Jetpack Compose, Gradle 8.13, Android Gradle Plugin 8.13.2, ADB, Android API 36 emulator

## Global Constraints

- Preserve all pre-existing uncommitted user changes.
- Use the `demoDebug` variant with mock data.
- Do not add Compose Preview or open a separate display window.
- Fix only confirmed build root causes.

---

### Task 1: Reproduce and repair the build

**Files:**
- Modify only the exact Gradle or Kotlin files named by a reproducible build failure.
- Test: `app/src/test/java/edu/lzjtu/environment/oildepot/domain/AlertPolicyTest.kt`
- Test: `app/src/test/java/edu/lzjtu/environment/oildepot/domain/PredictionInputValidatorTest.kt`

**Interfaces:**
- Consumes: root Gradle wrapper and the `demoDebug` product flavor.
- Produces: a tested `app-demo-debug.apk`.

- [ ] **Step 1: Run the unit tests and debug APK build**

Run: `./gradlew testDemoDebugUnitTest assembleDemoDebug --stacktrace`

Expected: `BUILD SUCCESSFUL`; otherwise capture the first causal error and its file path.

- [ ] **Step 2: Apply the smallest root-cause fix if the build fails**

Change only the file implicated by Step 1, preserving unrelated edits in that file. If Step 1 succeeds, make no source change.

- [ ] **Step 3: Re-run the complete verification command**

Run: `./gradlew testDemoDebugUnitTest assembleDemoDebug --stacktrace`

Expected: `BUILD SUCCESSFUL` and `app/build/outputs/apk/demo/debug/app-demo-debug.apk` exists.

### Task 2: Deploy into the current Studio emulator

**Files:**
- Modify: `.idea/workspace.xml` only to select the existing `Android App.app` run configuration.

**Interfaces:**
- Consumes: `app-demo-debug.apk` and the online `emulator-5554` target.
- Produces: foreground activity `edu.lzjtu.environment.oildepot.demo/edu.lzjtu.environment.oildepot.MainActivity`.

- [ ] **Step 1: Select the application run configuration**

Set the existing `RunManager` selection from `Android App.androidTest` to `Android App.app`; do not alter other workspace state.

- [ ] **Step 2: Install the verified APK**

Run: `adb -s emulator-5554 install -r app/build/outputs/apk/demo/debug/app-demo-debug.apk`

Expected: `Success`.

- [ ] **Step 3: Start the main activity**

Run: `adb -s emulator-5554 shell am start -n edu.lzjtu.environment.oildepot.demo/edu.lzjtu.environment.oildepot.MainActivity`

Expected: `Starting: Intent` without an error.

- [ ] **Step 4: Verify the foreground application**

Run: `adb -s emulator-5554 shell dumpsys activity activities`

Expected: the resumed activity contains `edu.lzjtu.environment.oildepot.demo/edu.lzjtu.environment.oildepot.MainActivity`.
