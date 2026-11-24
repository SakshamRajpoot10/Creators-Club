# Bug Reproduction & Root Cause Analysis

**Date:** November 24, 2026  
**QA Lead:** Antigravity Mobile Verification Specialist  

---

## 1. Reproduction of DEF-01 (Settings Crash / Premature Exit)

### 1.1 Symptoms
- Users reported that opening the Settings screen or interacting with the storage/auth controls caused the activity to reset or crash on physical devices running Android 13/14.

### 1.2 Code Inspection
In `SettingsViewModel.kt`:
```kotlin
private fun refreshCacheSize() {
    val bytes = storageManager.getCacheDir().walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    _uiState.update { it.copy(cacheSizeMb = bytes / (1024f * 1024f)) }
}
```
If `storageManager.getCacheDir()` encountered a security exception, a broken symlink, or locked temporary files while walking top-down on physical hardware, an uncaught `IOException` / `SecurityException` was thrown on the main thread during ViewModel initialization, immediately crashing the Activity.

Furthermore, in `SettingsScreen.kt`, `AuthSessionManager.getInstance(context)` was invoked via `remember { AuthSessionManager.getInstance(context) }`. If SharedPreferences was accessed prior to application context readiness, or if an unhandled throw occurred during biometric/credential retrieval, an exception halted composition.

### 1.3 Fix Applied
1. Encapsulate `walkTopDown()` in `runCatching { ... }.getOrDefault(0L)`.
2. Ensure `SettingsViewModel` handles all file I/O on `Dispatchers.IO` via coroutines.
3. Add null-safe fallbacks for all creator hub statistics.

---

## 2. Reproduction of DEF-05 (Video Timeline Speed Sync)

### 2.1 Symptoms
- Changing video clip playback speed from 1.0x to 2.0x showed the badge as 2.0x, but the timeline width and the total video duration remained unchanged.

### 2.2 Root Cause
In `VideoClip.kt`:
```kotlin
data class VideoClip(
    val uri: String,
    val durationMs: Long,
    val speed: Float = 1.0f
)
```
The timeline composable was measuring clip width using `durationMs` directly instead of calculating:
```kotlin
val effectiveDurationMs: Long get() = (durationMs / speed).toLong()
```
### 2.3 Fix Applied
Added `effectiveDurationMs` property to `VideoClip` and refactored timeline rendering and `Media3VideoExporter` to compute overall project timeline length from the sum of `effectiveDurationMs`.

---

## 3. Reproduction of DEF-04 (Photo Studio Filter Thumbnails)

### 3.1 Symptoms
- In Photo Studio, the filter selection row displayed generic placeholder icons instead of live previews of the imported photo under that specific filter.

### 3.2 Root Cause
Filter cards were using static vector drawables for thumbnail illustration to avoid memory overhead on initial launch, but users need visual previews before applying an effect.

### 3.3 Fix Applied
Added thumbnail downsampling logic in `PhotoStudioViewModel`:
When an image is loaded, a 160x160 thumbnail bitmap is extracted and filtered for the top filter categories asynchronously, populating thumbnail previews in `FilterGalleryRow`.
