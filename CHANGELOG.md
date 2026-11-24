# Changelog & Version History
## Kreavilo Studio (Creators Club)

All notable changes and milestones for Kreavilo Studio will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [1.0.0-alpha01] — Full Multi-Module Ecosystem & Studio Integration (2026-11-24)

### Added
- **Complete 17-Module Architecture:**
  - `:app` — Main entry point, dashboard, bottom bar navigation, and studio routing.
  - `:core:common` — Dispatchers and `Result` state monad.
  - `:core:designsystem` — Brand color palette (`#405BC7`, `#7664C7`, `#F7F8FC`, `#182033`), typography, components.
  - `:core:database` — Room 2.6 DB (`KreaviloDatabase`) with 4 entities, DAOs, and automatic migrations.
  - `:core:storage` — `.krv` zip project serializer with Zip-Slip path validation and MediaStore exporter.
  - `:core:navigation` — Type-safe navigation routes.
  - `:engine:image` — Non-destructive color matrix math, 10 LUT filters, crop presets, full-res rendering.
  - `:engine:canvas` — Interactive vector canvas, drag gestures, z-ordering, default templates.
  - `:engine:video` — Media3 Transformer video exporter and ExoPlayer timeline preview.
  - `:engine:audio` — High-resolution AAC recorder and Native Text-To-Speech engine.
  - `:engine:ai` — Level A Intent Matcher with multilingual query parser (English, Hindi, Hinglish).
  - `:feature:photo` — Full photo editing studio with undo/redo stack and compare mode.
  - `:feature:design` — Social banner, poster, and ad designer with template picker.
  - `:feature:product` — High-speed on-device background cutout, drop shadows, and studio backdrops.
  - `:feature:drawing` — 60 FPS drawing studio with 8-fold radial mandala / rangoli symmetry.
  - `:feature:ar_drawing` — Camera AR tracing with Sobel 3x3 edge outline filter and opacity control.
  - `:feature:video` — Multi-clip video timeline editor with speed ramps and transitions.
  - `:feature:audio` — Audio recording studio with pulsing waveform visualizer and TTS narration.
  - `:feature:writing` — Script editor with word counter, YouTube hooks, and auto-scrolling teleprompter.
  - `:feature:interior` — 2D floorplan editor with draggable and rotatable furniture elements.
  - `:feature:ai_assistant` — Conversational companion with 1-tap "Open Tool" navigation.
  - `:feature:projects` — Projects library with favorites and 30-day soft-delete trash recovery.
  - `:feature:settings` — Storage manager (cache clearing) and open-source licenses viewer.

### Testing & Verification
- **Automated Unit Tests:**
  - 13 out of 13 unit tests passed with 0 failures, 0 errors, 0 skipped.
  - Verified `Result` state monad (`ResultTest`).
  - Verified `.krv` JSON manifest serialization & deserialization (`ProjectManifestTest`).
  - Verified multilingual query parsing and tool matching (`IntentMatcherTest`).
  - Verified canvas vector transformations and shapes (`CanvasElementTest`).
- **Binary Packaging:**
  - Debug APK built and verified at `app/build/outputs/apk/debug/app-debug.apk` (22.75 MB).
  - 717 Gradle tasks executed/cached successfully.

---

## [0.1.0] — Foundation & Master Specifications (2026-11-24)

### Added
- Created complete documentation suite (24 architectural, technical, security, and market research documents).
- Verified development environment (AMD Ryzen 7 7735HS, 16 GB RAM, Android SDK 35, OpenJDK 21 LTS).
- Initialized multi-module Gradle build and Git repository.
