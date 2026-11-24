# Source Code Register — Kreavilo Studio

**Repository:** `https://github.com/SakshamRajpoot10/Creators-Club.git`  
**Root Branch:** `dev/master-upgrade`  
**Primary Architect:** Saksham Rajpoot  

---

## 1. Directory Tree & Module Mapping

```
Creators Club/
├── app/                                 # Main Android Application Module
│   └── src/main/java/com/creatorsclub/studio/
│       ├── KreaviloApplication.kt       # Application class, Hilt setup, AdMob init
│       ├── MainActivity.kt              # Root activity, NavHost, MainAppScreen
│       └── ui/
│           ├── CreateProjectSheet.kt    # New project modal with all 10 studios
│           └── StudiosDirectoryScreen.kt# Searchable directory with category filter
├── core/
│   ├── common/                          # Result<T>, DispatchersProvider, Constants
│   ├── database/                        # Room Database, ProjectDao, AuthSessionManager
│   │   └── src/main/java/com/creatorsclub/core/database/
│   │       ├── KreaviloDatabase.kt
│   │       ├── auth/AuthSessionManager.kt
│   │       ├── dao/ProjectDao.kt
│   │       └── model/ProjectEntity.kt
│   ├── designsystem/                    # Material 3 Theme, KreaviloColors, Typography
│   ├── navigation/                      # KreaviloDestination sealed hierarchy
│   └── storage/                         # StorageManager, ProjectSerializer, Manifest
├── engine/
│   ├── ai/                              # MLKit OCR, MediaPipe Segmentation, Sobel Filter
│   ├── audio/                           # AudioRecord 48kHz, WavEncoder, TTS Engine
│   ├── canvas/                          # MultiLayerCanvas, BrushRenderer, MandalaEngine
│   ├── image/                           # 30+ Filters, ToneCurve, HSL, ImageExporter
│   └── video/                           # Media3 Player, Timeline, Media3VideoExporter
├── feature/
│   ├── photo/                           # Studio 1: Photo Studio UI & ViewModel
│   ├── video/                           # Studio 2: Video Studio UI & ViewModel
│   ├── design/                          # Studio 3: Design Studio UI & ViewModel
│   ├── drawing/                         # Studio 4: Drawing Studio UI & ViewModel
│   ├── product/                         # Studio 5: Product Studio UI & ViewModel
│   ├── interior/                        # Studio 6: Interior Studio UI & ViewModel
│   ├── writing/                         # Studio 7: Writing Studio UI & ViewModel
│   ├── audio/                           # Studio 8: Audio Studio UI & ViewModel
│   ├── ar_drawing/                      # Studio 9: AR Tracing UI & ViewModel
│   ├── banner/                          # Studio 10: Banner & Thumbnail Studio UI
│   ├── projects/                        # Projects Manager & Local Browser
│   ├── settings/                        # Settings & Creator Hub Asset Importer
│   └── ai_assistant/                    # Creator AI On-Device Assistant
└── docs/                                # Technical, Architectural, and Legal Records
```

---

## 2. Compilation Flags & Environment

- **Gradle:** 8.14.4
- **Android Gradle Plugin (AGP):** 8.8.2
- **Kotlin:** 2.0.21
- **Java Home:** OpenJDK 21 LTS (`C:\Program Files\Android\Android Studio1\jbr`)
- **Min SDK:** 24 (Android 7.0 Nougat)
- **Target SDK / Compile SDK:** 35 (Android 15)
