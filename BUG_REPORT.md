# Creators Club — Master Bug Report & Defects Register

**Date:** November 24, 2026  
**Status:** In Progress / Prioritized for Master Repair  
**Target:** Clean Resolution on branch `dev/master-upgrade`

---

## Defects Register

| Bug ID | Component | Severity | Description | Steps to Reproduce | Status |
|---|---|---|---|---|---|
| **BUG-001** | Navigation (`MainActivity.kt`) | **Critical** | Create Project button and bottom bar "Create" tab hardcoded to open Photo Studio directly. | Tap "+ Create project" on Home or tap Create in bottom bar. Photo Studio opens immediately. | Open |
| **BUG-002** | Navigation (`MainActivity.kt`) | **Major** | Bottom bar "Studios" tab does not open a dedicated Studios Directory. | Tap "Studios" in bottom navigation. Selection index changes, but dashboard remains visible. | Open |
| **BUG-003** | UI Insets (`core:designsystem`) | **Major** | Bottom editor bars and tool controls appear underneath Android 3-button system navigation bar. | Open any studio on a device with 3-button navigation enabled. Bottom tools touch or clip behind buttons. | Open |
| **BUG-004** | TopBar Titles (`All Studios`) | **Minor** | Long studio titles ("Camera AR Tracing", "Interior Design Studio") wrap into two lines or collide with actions. | Open AR Drawing on a 360dp width phone. Title wraps. | Open |
| **BUG-005** | Home (`MainActivity.kt`) | **Major** | Assistant companion card and TopBar AI action exposed despite incomplete online backend. | Launch app; "Creator AI" card is visible on Home. | Open |
| **BUG-006** | Photo Studio (`feature:photo`) | **Major** | Launches with synthetic procedural checkerboard/gradient sample instead of letting user pick a real photo. | Open Photo Studio. Sample image loads automatically. | Open |
| **BUG-007** | Video Studio (`feature:video`) | **Major** | Launches into empty timeline without an initial video import dialog or clip selector. | Open Video Studio. Empty timeline displayed with no guidance. | Open |
| **BUG-008** | Authentication (`core:database`) | **Critical** | No login, signup, or user session system exists. Reinstalling app results in total data loss. | Uninstall and reinstall app; all projects and settings vanish permanently. | Open |
| **BUG-009** | Cloud Sync (`core:database`) | **Major** | Projects and user assets stored strictly in local Room SQLite; no cloud synchronization with Supabase. | Create project; switch to another device; project not found. | Open |
| **BUG-010** | Drawing Studio (`feature:drawing`) | **Moderate** | Canvas lacks multi-layer support and transparent PNG export toggle. | Draw on canvas; only single flattened layer available. | Open |
| **BUG-011** | Permissions (`feature:settings`) | **Moderate** | No centralized Permissions & Access screen explaining why Camera, Audio, or Storage permissions are needed. | User denies camera; no clear recovery screen or guidance. | Open |
| **BUG-012** | Writing Studio (`feature:writing`) | **Moderate** | Editor is plain text only; lacks rich text headings, bold, bullet lists, and PDF export. | Type in Writing Studio; no formatting toolbar available. | Open |

---

## Resolution Targets & Verification Strategy
- **Stage 1 Fixes:** Resolve BUG-001, BUG-002, BUG-003, BUG-004, BUG-005 immediately.
- **Stage 2 Fixes:** Resolve BUG-008, BUG-009, BUG-011 with Supabase Auth and Cloud DB architecture.
- **Stage 3-12 Fixes:** Resolve BUG-006, BUG-007, BUG-010, BUG-012 across respective studio engines.
