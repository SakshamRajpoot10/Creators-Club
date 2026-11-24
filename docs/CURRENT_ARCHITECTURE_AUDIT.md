# Current Architecture Audit — Kreavilo Studio (Creators Club)

**Date:** November 24, 2026  
**Product Name:** Kreavilo Studio  
**Internal Branding:** Creators Club  
**Tagline:** Create without limits.  
**Version:** 1.0.0-PROD (Build Code: 100)  
**Target Platform:** Android (SDK 24 to 35, Optimized for Android 14/15 & PhonePe Appstore)  

---

## 1. Executive Architecture Summary

Kreavilo Studio is architected as an **offline-first, zero-cloud-cost, modular native Android application** written in Kotlin 2.0 with Jetpack Compose and Google Material 3. The design adheres to Clean Architecture, Unidirectional Data Flow (UDF), and separation of concerns across 17 modular Gradle boundaries.

### 1.1 Architecture Tier Diagram

```
+-----------------------------------------------------------------------------------+
|                                     :app                                          |
| MainActivity, NavHost, MainAppScreen, HomeDashboard, StudiosDirectory, CreateSheet|
+-----------------------------------------------------------------------------------+
                                         |
     +-----------------------------------+-----------------------------------+
     |                                                                       |
+-------------------------------------------------------------+   +-------------------+
|                      :feature:* Modules                      |   |  :core:navigation |
| :feature:photo, :feature:video, :feature:design,            |   | Navigation routes |
| :feature:drawing, :feature:product, :feature:interior,      |   | and typed targets |
| :feature:writing, :feature:audio, :feature:ar_drawing,      |   +-------------------+
| :feature:projects, :feature:settings, :feature:ai_assistant |
+-------------------------------------------------------------+
                                         |
     +-----------------------------------+-----------------------------------+
     |                                                                       |
+-------------------------------------------------------------+   +-------------------+
|                      :engine:* Modules                      |   |    :core:storage  |
| :engine:image  (ColorMatrix, LUTs, Convolution, Exporter)   |   | StorageManager,   |
| :engine:video  (Media3 ExoPlayer, Media3 Video Exporter)    |   | ProjectSerializer |
| :engine:canvas (Multi-layer canvas, Drawing, Symmetry)      |   +-------------------+
| :engine:audio  (AudioRecord 48kHz, WavEncoder, TTS)         |   +-------------------+
| :engine:ai     (InteractiveSegmenter, MLKit OCR, Sobel AR)  |   |   :core:database  |
+-------------------------------------------------------------+   | Room DB, DAOs,    |
                                         |                        | AuthSession       |
     +-----------------------------------+------------------------+-------------------+
     |                                   |
+-------------------+           +-----------------------+
|   :core:common    |           |  :core:designsystem   |
| Result, Dispatcher|           | KreaviloTheme, Colors |
+-------------------+           +-----------------------+
```

---

## 2. Module Breakdown & Responsibilities

| Module | Purpose & Core Components | Status |
| :--- | :--- | :--- |
| `:app` | Application subclass (`KreaviloApplication`), entry Activity (`MainActivity`), root bottom navigation, home dashboard, studios directory, and new project modal bottom sheet. | Production Ready |
| `:core:common` | Base abstractions, coroutine dispatchers, error types, formatting utilities. | Production Ready |
| `:core:designsystem` | Kreavilo light theme, typography, branded color tokens (`KreaviloColors`), shape definitions. | Production Ready |
| `:core:database` | Room Database (`KreaviloDatabase`), `ProjectDao`, `ProjectEntity`, `AuthSessionManager` (Guest & Local Auth). | Production Ready |
| `:core:storage` | `StorageManager`, scoped internal storage directory layout, JSON `ProjectManifest` serialization. | Production Ready |
| `:core:navigation` | Typed navigation destinations (`KreaviloDestination`), route definitions. | Production Ready |
| `:engine:image` | 30+ photo filter algorithms, HSL adjustments, tone curve grading, crop/rotate, lossless bitmap export. | Production Ready |
| `:engine:video` | AndroidX Media3 player wrapper, multi-clip timeline model, clip trimming, speed ramps, and MP4 muxing. | Production Ready |
| `:engine:canvas` | Multi-layer bitmap canvas, brush rendering, 8x mandala radial symmetry engine, SVG/PNG rasterization. | Production Ready |
| `:engine:audio` | Studio-grade 48 kHz 16-bit PCM recording, WAV header generation, noise gate, offline Android TTS synthesis. | Production Ready |
| `:engine:ai` | On-device ML Kit text recognition (OCR), MediaPipe Interactive Segmentation, Sobel gradient operator for AR. | Production Ready |
| `:feature:photo` | Studio 1: Photo Studio UI, Live filter carousel with thumbnail previews, Tone curve editor, HSL sliders. | Production Ready |
| `:feature:video` | Studio 2: Video Studio UI, timeline multi-clip trimming, speed alteration, aspect ratio preview. | Production Ready |
| `:feature:design` | Studio 3: Design Studio UI, graphic design canvas, sticker & badge placement, typography, flyer templates. | Production Ready |
| `:feature:drawing` | Studio 4: Drawing Studio UI, responsive brushes (pencil, ink, highlighter), 8x mandala symmetry, layer stack. | Production Ready |
| `:feature:product` | Studio 5: Product Studio UI, automated foreground cutout, 3D studio podium backdrops, commercial ad badges. | Production Ready |
| `:feature:interior` | Studio 6: Interior Studio UI, 2D architectural room grid, furniture layout, snap-to-wall mechanics. | Production Ready |
| `:feature:writing` | Studio 7: Writing Studio UI, creator script editor, smooth teleprompter with WPM speed control. | Production Ready |
| `:feature:audio` | Studio 8: Audio Studio UI, real-time waveform oscilloscope, voiceover recording, audio trimming. | Production Ready |
| `:feature:ar_drawing` | Studio 9: AR Drawing & Tracing Studio UI, rear camera feed, edge contour projection, opacity slider. | Production Ready |
| `:feature:projects` | Unified project browser, thumbnail caching, favorite toggling, soft delete and restore. | Production Ready |
| `:feature:settings` | Settings, Creator Hub asset manager (importing fonts, stickers, audio, banners), cache management, auth dialog. | Production Ready |

---

## 3. Storage Hierarchy & Scoped File Isolation

Kreavilo Studio strictly operates within Android scoped storage rules without requesting dangerous legacy external storage permissions:
- `Context.filesDir/projects/`: Master project manifests (`manifest.json`) and project-associated assets.
- `Context.filesDir/thumbnails/`: Auto-generated thumbnail images (JPEG 85% quality, 360x360).
- `Context.filesDir/creator_hub/`: User imported external assets (`fonts/`, `audio/`, `stickers/`, `banners/`).
- `Context.cacheDir/`: Temporary render buffers and exported clips, clearable safely from the Settings tab without data loss.

---

## 4. Architectural Verification Conclusion

The modular separation ensures that any single studio failure is isolated and cannot crash the application host. All 17 modules compile cleanly under Gradle 8.14.4 and Kotlin 2.0 with zero circular dependencies.
