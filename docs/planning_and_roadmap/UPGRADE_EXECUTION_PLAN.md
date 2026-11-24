# Creators Club — Master Upgrade Execution Plan

**Objective:** Transform Creators Club from an attractive prototype into a genuine, production-grade creative operating ecosystem across all 9 creative studios.

---

## Phased Execution Roadmap

### Stage 1: Critical Navigation, Responsive Layout & Safe Area Repairs (Immediate)
- **1.1 Project-Type Selector:**
  - Build `ProjectTypeSelectorModal` / `CreateProjectScreen` displaying all 9 studios with clear descriptions, icons, and specialized starting options (e.g. "Import photo", "Capture photo", "Blank canvas", "Choose template").
  - Wire "+ Create project" on Home and the Create bottom navigation tab to open this selector.
- **1.2 Dedicated Studios Directory Screen:**
  - Build `StudiosDirectoryScreen` showing all 9 creative studios in an organized layout with quick action buttons and feature badges.
  - Wire the bottom navigation "Studios" tab to navigate to this directory.
- **1.3 Responsive Insets & Safe Area Fixes:**
  - Apply `Modifier.navigationBarsPadding()` and `Modifier.statusBarsPadding()` across all screens and bottom toolbars so no controls appear behind Android system navigation bars.
  - Fix toolbar title wrapping using `maxLines = 1` and `TextOverflow.Ellipsis`.
- **1.4 AI Assistant Cleanup:**
  - Per Section 26, remove the incomplete AI Assistant card and buttons from the public home dashboard. Replace the hero banner with a dynamic Quick Creative Tools section.

### Stage 2: Authentication & Cloud Database Architecture
- **2.1 Supabase Integration & Architecture:**
  - Define Supabase configuration, authentication client, and repository interfaces.
  - Create database schema script (`profiles`, `projects`, `project_versions`, `saved_assets`, `favorite_tools`, `custom_presets`, `user_preferences`).
  - Implement Email/Password authentication, Google OAuth sign-in flow, and secure session management.
- **2.2 Offline-First Persistence & Sync:**
  - Connect Room database with Supabase cloud synchronizer so users can create and edit completely offline, with automatic sync when internet is connected.
- **2.3 Dedicated Permissions & Access Screen:**
  - Create a professional Permissions screen explaining Camera, Audio, and Storage usage with direct OS settings launcher.

### Stage 3: Universal Media Import & Shared Asset Library
- **3.1 Studio Entry Launchers:**
  - Provide direct Photo Picker, Video Picker, and File Picker dialogs upon entering each studio so users never start with an empty or synthetic screen.
- **3.2 Unified "My Assets" Screen:**
  - Centralize user imported photos, audio clips, fonts, and stickers in a shared gallery accessible across all 9 studios.

### Stage 4 to 12: Professional Studio Deepening
- **Stage 4 (Photo Studio):** Expand LUT presets (50+), add HSL selective color sliders, and user custom preset saving.
- **Stage 5 (Video Studio):** Add multi-track audio waveform timeline, audio ducking, and transition duration controls.
- **Stage 6 (Design Studio):** Add Google Fonts typography family selector, sticker library, and vector shapes.
- **Stage 7 (Drawing Studio):** Implement multi-layer drawing stack (hide/lock/opacity) and transparent PNG export.
- **Stage 8 (Product Studio):** Integrate real on-device foreground segmentation and shadow blur radius controls.
- **Stage 9 (Interior Studio):** Expand furniture catalogue (30+ items) and add room photo wall recoloring shader.
- **Stage 10 (Writing Studio):** Add rich-text formatting (bold, italic, headings, lists) and Markdown/PDF exporter.
- **Stage 11 (Audio Studio):** Add audio waveform slice/trim tool, noise reduction filter, and multi-track mixer.
- **Stage 12 (AR Tracing Studio):** Add flashlight toggle, grid alignment overlay, and screen touch lock.

### Stage 13: Creative Resource Library Population
- Embed curated CC0/Apache 2.0 fonts, design templates, sound effects, and drawing practice templates.

### Stage 14 to 18: Quality Assurance, Testing & Release Assembly
- Execute automated unit test suites and instrumentation checks.
- Build production debug APK in `APK/CreatorsClub-v1.0.0.apk`.
- Commit all changes cleanly to Git on `dev/master-upgrade`.
