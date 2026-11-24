# User-Reported Defects & Corrective Action Plan

**Date:** November 24, 2026  
**Application:** Kreavilo Studio (Creators Club)  
**Investigation Lead:** Senior QA & Mobile Architecture Committee  

---

## 1. Inventory of Reported Defects

The following defects were reported from user testing of earlier APK builds:

| Defect ID | Severity | Category | Description | Root Cause Hypothesis |
| :--- | :--- | :--- | :--- | :--- |
| **DEF-01** | **P0** | Stability | **Settings Screen crash or abrupt closure**: Opening Settings or clicking Cache/Auth on physical devices causes crash or navigation back. | Missing lifecycle bounds, null pointer in uninitialized storage paths, or unhandled coroutine exception when reading cache directory. |
| **DEF-02** | **P0** | UI/Navigation | **Navigation Safe-Area Inset clipping**: Navigation bars and bottom sheets cut off lower elements on devices with gesture navigation. | Missing `navigationBarsPadding()` or nested overlapping scaffolds in modal sheets and dashboard. |
| **DEF-03** | **P1** | Feature Completeness | **Missing Studio 10 (Banner & Thumbnail Studio)**: 9 studios exist; users require a dedicated 10th studio for YouTube thumbnails and channel banners with safe-zone guides. | Studio 10 was not previously declared as an independent top-level studio in navigation and directory. |
| **DEF-04** | **P1** | Photo Engine | **Generic Static Filter Thumbnails**: Filter carousel in Photo Studio previously used placeholder icons instead of live previews generated from user photo. | Thumbnails were hardcoded or static vectors rather than rendered off the downsampled imported bitmap. |
| **DEF-05** | **P1** | Video Studio | **Timeline speed changes do not alter clip duration**: Altering clip playback speed (e.g. 0.5x, 2.0x) does not update clip length or timeline sync. | Speed factor was stored as a metadata tag but omitted from duration calculation in the timeline UI and exporter clipping configuration. |
| **DEF-06** | **P1** | Audio Studio | **Audio Trimming Pipeline**: Need complete in-memory and file-based PCM trimming for exported voiceovers. | Audio recorder captured PCM/WAV cleanly, but trimming handles were not connected to a destructive export pipeline. |
| **DEF-07** | **P1** | AR Tracing | **Camera stream opacity & flashlight lock**: Flashlight toggling and edge contour opacity must maintain camera feed without freezing. | CameraX preview needs robust lifecycle binding and thread-safe surface provider attachment. |
| **DEF-08** | **P2** | Packaging | **APK Filename Inconsistency**: APK files must strictly reflect the official application name (`KreaviloStudio.apk`). | Build outputs generated `app-debug.apk` instead of properly branded release/debug distribution artifacts. |

---

## 2. Corrective Resolution Plan

1. **DEF-01 (Settings Crash)**: Fully inspect `SettingsViewModel` and `SettingsScreen`. Safeguard file walking with `try-catch` blocks, ensure safe state updates, and handle empty directory cases gracefully.
2. **DEF-02 (Safe-Area Insets)**: Enforce `.navigationBarsPadding()` and `.statusBarsPadding()` consistently across `MainActivity`, `StudiosDirectoryScreen`, `CreateProjectSheet`, and all 10 studio screens.
3. **DEF-03 (Banner & Thumbnail Studio)**: Implement `BannerThumbnailStudioScreen` with YouTube Thumbnail (1280x720), YouTube Banner (2560x1440) with 1235x338 Safe Zone guide, Instagram (1:1, 4:5, 9:16), X Header (1500x500), high-contrast stroke text, and badge presets. Register across navigation, directory, and bottom sheet.
4. **DEF-04 (Filter Thumbnails)**: Implement dynamic 160x160 downsampled preview generation in `PhotoStudioViewModel` so every filter card displays an accurate live miniature of the loaded photo with the filter applied.
5. **DEF-05 (Video Speed Duration)**: Update clip effective duration formula: `effectiveDurationMs = (originalDurationMs / speedMultiplier)`. Ensure `Media3VideoExporter` respects this during clipping and concat.
6. **DEF-06 (Audio Trimming)**: Connect audio waveform trimmer to crop WAV byte arrays and export trimmed files to user projects.
7. **DEF-07 (AR Camera)**: Ensure CameraX binds to `ProcessCameraProvider` safely, supporting torch toggle and contour threshold adjustment.
8. **DEF-08 (APK Packaging)**: Configure copy task to output `KreaviloStudio-v1.0.0.apk` and `KreaviloStudio.apk` into the `APK/` directory upon build completion.
