# Changelog & Version History
## Kreavilo Studio (Creators Club)

All notable changes and milestones for Kreavilo Studio will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

---

## [Unreleased] — Phase 0 & Phase 1 Execution (2026-11-24)

### Added
- **Multi-Module Architecture & First Production Build:**
  - Built and verified `:app`, `:core:common`, `:core:designsystem`, `:core:database`, `:core:storage`, `:core:navigation`, `:engine:image`, and `:feature:photo`.
  - Compiled and packaged release-ready debug APK `app-debug.apk` (16.5 MB) via Gradle 8.14.4 & OpenJDK 21.
- **Room Database & Persistence (`:core:database`):**
  - Implemented Room 2.6 database `KreaviloDatabase` with entities `ProjectEntity`, `AssetEntity`, `FeatureRegistryEntity`, and `AssistantChatEntity`.
  - Created full DAOs (`ProjectDao`, `AssetDao`, `FeatureRegistryDao`, `AssistantChatDao`) supporting active projects, favorites, soft delete (30-day trash), and instant query search.
- **Unified Project Storage & `.krv` Packaging (`:core:storage`):**
  - Created `ProjectSerializer` for non-destructive packaging into `.krv` archive (zip container with `manifest.json`, `thumbnail.webp`, and `assets/`).
  - Added strict Zip Slip directory traversal security verification.
  - Implemented `StorageManager` with Android Scoped Storage MediaStore export to public Pictures/DCIM gallery without root paths.
- **Type-Safe Navigation (`:core:navigation`):**
  - Implemented Kotlinx Serialization type-safe routes (`KreaviloDestination`) for Home, Studios, Create, Projects, Settings, PhotoStudio, DesignStudio, VideoStudio, DrawingStudio, ARDrawingStudio, ProductStudio, InteriorStudio, WritingStudio, AudioStudio, and CreatorAI.
- **Non-Destructive Photo Engine (`:engine:image`):**
  - Implemented `ColorMatrixProcessor` for 60 FPS real-time Compose filter previews (Brightness, Contrast, Saturation, Exposure, Warmth).
  - Implemented 10 artistic LUT presets: Original, Cinematic, Vintage, B&W, Portrait, Film, Warm, Cool, Matte, Dramatic.
  - Implemented `CropTransformState` supporting 8 aspect ratios (1:1, 4:5, 9:16, 16:9, 3:4, 2:3, Free, Original), 90° rotation, and horizontal/vertical flips.
  - Implemented `BitmapProcessor` for full-resolution background rendering during export.
- **Interactive Photo Studio UI (`:feature:photo`):**
  - Built `PhotoStudioScreen` and `PhotoStudioViewModel` with undo/redo stack, before-and-after comparison button, filter carousel, granular sliders, and direct gallery export.
- **Adaptive Launcher Icons:**
  - Created custom Kreavilo star vector icons (`ic_launcher.xml` and `ic_launcher_round.xml`).
- **Master Implementation Plan (`MASTER_IMPLEMENTATION_PLAN.md`):** Complete 37-section specification established and saved in project root.
- **Hardware & Toolchain Environment Audit:**
  - Verified AMD Ryzen 7 7735HS (8C/16T), 16 GB RAM, Radeon Graphics on Windows 11.
  - Verified Android SDK at `C:\Users\315082\AppData\Local\Android\Sdk` (build-tools 35.0.0, 36.0.0, 37.0.0; platforms android-30, android-36, android-37.0).
  - Verified OpenJDK 21 LTS at `C:\Program Files\Android\Android Studio1\jbr\bin\java.exe`.
  - Initialized Git repository on branch `main`.
- **Complete Core Project Documentation Suite:**
  - `README.md`
  - `PRODUCT_REQUIREMENTS.md` (Comprehensive PRD)
  - `IMPLEMENTATION_PLAN.md` (Phased technical roadmap)
  - `MARKET_RESEARCH.md` & `COMPETITOR_ANALYSIS.md` (Snapseed, Canva, CapCut, Photoroom, ibis Paint X analysis)
  - `ARCHITECTURE.md` (Multi-module architecture, `.krv` container schema, data flow)
  - `DESIGN_SYSTEM.md` (Material 3 tokens, #405BC7 light-theme palette, Manrope/Inter typography, Home & Creator AI layout specifications)
  - `FEATURE_MATRIX.md` (30+ feature mappings across 10 studios)
  - `SOURCE_CODE_REFERENCES.md` (Audited open-source reference library & copyleft quarantine protocol)
  - `AI_ARCHITECTURE.md` (Creator AI 3-tier system: Level A universal SQLite FTS5 registry, Level B local SLM, allowlisted action dispatcher)
  - `DATABASE_SCHEMA.md` (Room database entities, indices, and scoped storage hierarchy)
  - `DEPENDENCY_REGISTER.md` (Permissive license catalog: Apache 2.0, MIT, BSD, SIL OFL)
  - `MODEL_LICENSE_REGISTER.md` (AI model audit & integrity checks)
  - `SECURITY_AND_PRIVACY.md` (Offline-first privacy by design, decompression bomb protection)
  - `THIRD_PARTY_NOTICES.md` & `ASSET_LICENSE_REGISTER.md` (Attribution & clean asset registry)
  - `COST_REPORT.md` (Strict ₹0 budget infrastructure verification)
  - `TEST_PLAN.md` & `TEST_RESULTS.md` (Phase 0 environment tests passed 8/8)
  - `PERFORMANCE_REPORT.md` (Hardware profiling tiers: Basic, Mid, High)
  - `KNOWN_LIMITATIONS.md` (Technical trade-offs, PhonePe Appstore dual-store strategy)
  - `RELEASE_CHECKLIST.md` (Release criteria and quality gates)
  - `STORE_LISTING.md` (English & Hindi store listings ready for PhonePe Appstore)
