# Implementation Plan — Kreavilo Studio (Creators Club)
## Phased Technical Roadmap & Execution Log

**Current Phase:** Phase 0 (Research, Technical Feasibility & System Architecture)  
**Target Platform:** Android (API 26 to API 35+)  
**Primary Language:** Kotlin + Jetpack Compose  
**Architect:** Antigravity Engineering Team & Saksham Rajpoot  

---

## Phase 0: Research, Technical Feasibility & Environment Audit (IN PROGRESS)

### Objective:
Establish the complete hardware, toolchain, competitive, licensing, and structural baseline. Create all mandatory architectural specifications before writing code.

### Completed Milestones:
- [x] **Hardware & OS Audit:**
  - System: AMD Ryzen 7 7735HS (8 Cores, 16 Threads), 16 GB RAM (13.95 GB visible), Radeon Graphics.
  - Storage: C: Drive (74.2 GB free), D: Drive (131.6 GB free - designated project root).
  - OS: Windows 11 Home 64-bit.
  - Analysis: Machine has strong multi-threaded CPU power. GPU is integrated Radeon (Vulkan/OpenGL ES 3.1+ compatible). RAM requires conservative model allocation (no local models >2.5 GB running simultaneously with Android Studio/emulator).
- [x] **Toolchain & SDK Audit:**
  - Git: `2.52.0.windows.1` active.
  - Android SDK: Located at `C:\Users\315082\AppData\Local\Android\Sdk`.
  - Build-tools installed: `35.0.0`, `36.0.0`, `36.1.0`, `37.0.0`.
  - Android platforms installed: `android-30`, `android-36`, `android-37.0`.
  - JDK: JDK 21 LTS installed via Android Studio JBR at `C:\Program Files\Android\Android Studio1\jbr` (ideal for Gradle 8.x/9.x and AGP).
- [x] **Master Plan Integration:** Saved comprehensive requirements into `MASTER_IMPLEMENTATION_PLAN.md`.
- [x] **Core Documentation Set Created:**
  - `PRODUCT_REQUIREMENTS.md`
  - `MARKET_RESEARCH.md`
  - `COMPETITOR_ANALYSIS.md`
  - `ARCHITECTURE.md`
  - `FEATURE_MATRIX.md`
  - `DESIGN_SYSTEM.md`
  - `COST_REPORT.md`
  - `DEPENDENCY_REGISTER.md`
  - `MODEL_LICENSE_REGISTER.md`
  - `SECURITY_AND_PRIVACY.md`
  - `README.md`

---

## Phase 1: Application Foundation & Modular Backbone

### Goal:
Initialize the multi-module Android project with Gradle Kotlin DSL, dependency injection (Hilt), Room database, Navigation 2.8+ Compose, Design System tokens, and the unified Project Library (`.krv`).

### Modules to Create:
1. `:app` — Application entry point, Application class, MainActivity, dynamic feature coordinators.
2. `:core:designsystem` — Material 3 theme, colors, typography, shapes, icons, and reusable components.
3. `:core:navigation` — Type-safe Jetpack Compose navigation routes.
4. `:core:database` — Room Database, DAOs, entities (ProjectEntity, AssetEntity, ToolHistoryEntity, AssistantMemoryEntity).
5. `:core:storage` — Project serializer/deserializer (`.krv` archive format), scoped storage manager, cache cleaner.
6. `:core:media` — Memory-safe bitmap decoders, sampled image loaders, orientation handlers.
7. `:core:common` — Coroutine dispatchers, Result monad, base ViewModels.
8. `:engine:ai` — Feature Registry with allowlisted actions, hardware capability detector.
9. `:feature:home` — Home screen with dynamic cards, quick actions, studio grid, recent projects.
10. `:feature:projects` — Unified project browser, filters, search, favorites, duplicate, delete.

### Acceptance Criteria:
- App compiles cleanly with JDK 21 and latest stable AGP.
- Home screen renders the exact design defined in `DESIGN_SYSTEM.md` (#405BC7 primary, #F7F8FC background).
- Project can be created, saved, retrieved from Room, and exported.
- Zero crashes on launch and smooth 60/120 FPS navigation.

---

## Phase 2: Core Image, Design & Product Studios

### Goal:
Build the primary visual creation engines: Photo Studio, Graphic Design Canvas, and Product Photography Studio.

### Modules & Features:
1. `:engine:image` — Non-destructive pipeline:
   - Geometry: Crop (preset aspect ratios, freeform), rotate, flip, straighten.
   - Color Adjustments: Exposure, brightness, contrast, saturation, vibrance, highlights/shadows, temperature, tint, RGB curves, HSL sliders.
   - GPU Filters: Cinematic, Vintage, BW, Portrait, Film, Warm, Cool, Matte via GL shaders.
2. `:engine:canvas` — Vector and raster layer composition engine:
   - Multi-touch transformation (pan, zoom, rotate), layer reordering, opacity, blend modes.
   - Typography Engine: Rich text rendering, custom fonts (Inter, Manrope, Devanagari fonts), curved text, shadows.
   - 100+ Free JSON-based template parser and renderer.
3. `:feature:photo` — Full Photo Studio editing UI with interactive before-and-after comparison.
4. `:feature:design` — Canva-style drag-and-drop design editor for social posts, posters, flyers, banners.
5. `:feature:product` — Product Photography Studio:
   - On-device subject segmentation (MediaPipe Interactive Segmentation).
   - Solid, gradient, and studio background staging.
   - Artificial contact shadow generator.
   - Batch resizing for Amazon, Flipkart, Instagram marketplace specs.

---

## Phase 3: Digital Drawing & Camera AR Studios

### Goal:
Provide natural drawing capabilities and beginner-friendly camera tracing.

### Modules & Features:
1. `:engine:canvas` (Drawing extension) — Low-latency drawing pipeline:
   - Brushes: Pencil, ink pen, marker, airbrush, watercolor, texture brushes.
   - Smooth stroke interpolation (Bézier curves), pressure simulation, palm rejection.
   - Symmetry drawing (vertical, horizontal, radial mandala/rangoli).
2. `:feature:drawing` — Complete painting canvas with layers, color wheel, eyedropper, undo/redo.
3. `:feature:ar_drawing` — Camera-assisted tracing:
   - CameraX live preview with transparent overlay.
   - Sobel/Canny edge-detection filter to convert reference photos into clean sketch outlines.
   - Opacity slider, screen lock, flashlight toggle.

---

## Phase 4: Video & Audio Studios

### Goal:
Deliver a timeline-based mobile video editor and standalone audio production suite.

### Modules & Features:
1. `:engine:video` — AndroidX Media3 (Transformer + ExoPlayer):
   - Multi-track timeline (video, overlay, audio, text).
   - Trimming, splitting, merging, reordering, speed ramping (0.25x to 4x).
   - Transitions: Dissolve, crossfade, slide, wipe, zoom.
   - Hardware-accelerated MP4 (H.264/AAC) export with resolution selection (720p, 1080p).
2. `:engine:audio` — Audio Studio:
   - High-fidelity microphone voice recorder with real-time waveform display.
   - Trimming, normalization, fade in/out, volume envelope, silence detection.
   - Text-to-speech integration via Android native TTS engine.
3. `:feature:video` — Video Studio UI with timeline scrubbing, frame thumbnails, preview player.
4. `:feature:audio` — Audio Studio UI with track mixing and sound effects library.

---

## Phase 5: Interior & Writing Studios

### Goal:
Expand utility to 2D room visualization and content writing/teleprompting.

### Modules & Features:
1. `:feature:interior` — Interior Design Studio:
   - 2D floor-plan editor with wall drawing, dimension markers, door/window snapping.
   - Wall and floor color/texture replacement using photo perspective warping.
   - Furniture sticker library with scaling and rotation.
2. `:feature:writing` — Writing & Script Studio:
   - Markdown and rich-text note editor.
   - Script templates (YouTube video scripts, Instagram reels, hooks, product pitches).
   - Integrated floating teleprompter with adjustable scroll speed, font size, and mirror mode for camera filming.

---

## Phase 6: Creator AI Assistant Integration

### Goal:
Connect all studios into an intelligent, voice-operable creative companion.

### Modules & Features:
1. `:engine:ai` & `:feature:ai_assistant`:
   - **Level A (Universal Assistant):** Offline intent parser and SQLite search over the centralized Feature Registry. Understands English, Hindi, and Hinglish queries.
   - **Action Dispatcher:** Translates natural language into strictly allowlisted in-app actions (e.g., "Remove background from this photo", "Create YouTube thumbnail", "Open Video Studio with 9:16 aspect ratio").
   - **Voice I/O:** Android SpeechRecognizer for speech-to-text, native TextToSpeech for verbal responses.
   - **Contextual Creative Guidance:** Analyzes current image/video metadata (aspect ratio, resolution, dominant colors) and provides tailored recommendations.
   - **Model Manager:** Settings UI to inspect, download, verify SHA-256, and manage optional on-device AI weights.

---

## Phase 7: Animation & Storytelling Studio

### Goal:
Enable sequential visual storytelling and frame-by-frame animation without cloud dependencies.

### Modules & Features:
1. `:feature:animation`:
   - Storyboard editor linking script scenes to sketched frames.
   - Frame-by-frame flipbook animation with onion skinning.
   - Ken Burns pan-and-zoom photo animation.
   - Export to animated GIF and MP4 video.

---

## Phase 8: Advanced Local AI Research & Optimizations

### Goal:
Benchmark and validate advanced on-device ML on capable devices.

### Research Targets:
- Quantized LiteRT SLM (Gemma 2B / FunctionGemma) for conversational tool calling.
- NCNN Vulkan Real-ESRGAN super-resolution for high-end devices.
- Transparent fallback: Unsupported devices continue smoothly on Level A universal assistant.

---

## Phase 9: Monetization, Privacy & Security Hardening

### Goal:
Integrate non-disruptive AdMob ads and pass strict privacy and security audits.

### Milestones:
- Google AdMob official test IDs configured.
- Strict frequency caps (interstitials only on natural transitions, never over editing canvas).
- Scoped storage enforcement, file path traversal validation, decompression bomb prevention.
- Privacy policy, data safety declarations, and GDPR/CMP consent mechanism.

---

## Phase 10: Full Quality Assurance & Performance Optimization

### Goal:
Verify stability, memory safety, and 60 FPS performance across device profiles.

### Milestones:
- Unit tests for image algorithms, project serialization, and AI action dispatching.
- Media stress testing: 50MP images, 4K video clips, low-storage warning handling.
- ANR and memory leak detection using Android Studio Profiler.

---

## Phase 11: Release Preparation & Store Packaging

### Goal:
Prepare production release artifacts for PhonePe Appstore and secondary supported stores.

### Milestones:
- Signed Release APK / AAB.
- Bilingual store listing (English & Hindi) with original screenshots and promo banner.
- Responsive GitHub Pages static site with privacy policy and app-ads.txt.
