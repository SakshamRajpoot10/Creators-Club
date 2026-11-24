# Dependency & Open-Source License Register
## Kreavilo Studio (Creators Club)

Every dependency included in Kreavilo Studio must be legally compatible with commercial distribution, modification, and redistribution without requiring the application's proprietary source code to be disclosed under copyleft licenses.

---

## 1. Permitted License Classifications
- **Permitted:** Apache 2.0, MIT, BSD 2-Clause, BSD 3-Clause, SIL Open Font License (OFL), ISC, Unlicense.
- **Prohibited / Strict Review:** GPL v2/v3, AGPL v3 (Strong copyleft: viral source disclosure requirement).
- **Conditional (Dynamic Linking Only):** LGPL v2.1/v3 (Allowed only if dynamically linked as shared C/C++ libraries without modification, or verified permissible for standard Android NDK usage).

---

## 2. Approved Dependency Register

| Library / Component | Source Repository | License | Purpose | Commercial Use Permitted? |
|---|---|---|---|---|
| **AndroidX Lifecycle & Compose** | `androidx.*` | Apache 2.0 | UI framework, ViewModels, navigation | Yes |
| **AndroidX Media3** | `androidx.media3:*` | Apache 2.0 | Video player & transformer engine | Yes |
| **Room Persistence Library** | `androidx.room:*` | Apache 2.0 | SQLite local database ORM | Yes |
| **Google Hilt** | `com.google.dagger:hilt-android` | Apache 2.0 | Dependency injection | Yes |
| **Kotlin Coroutines & Flow** | `org.jetbrains.kotlinx:kotlinx-coroutines-*` | Apache 2.0 | Asynchronous programming | Yes |
| **Kotlinx Serialization** | `org.jetbrains.kotlinx:kotlinx-serialization-json` | Apache 2.0 | JSON project parsing (`.krv`) | Yes |
| **Google MediaPipe** | `com.google.mediapipe:tasks-vision` | Apache 2.0 | On-device interactive image segmentation | Yes |
| **Google ML Kit Vision** | `com.google.mlkit:text-recognition` | Apache 2.0 / Android SDK TOS | On-device OCR & Barcode scanning | Yes |
| **OpenCV for Android** | `org.opencv:opencv` | Apache 2.0 | Edge detection & perspective warps | Yes |
| **ZXing Android Embedded** | `com.journeyapps:zxing-android-embedded` | Apache 2.0 | QR code generation & camera scan | Yes |
| **CommonMark Java** | `org.commonmark:commonmark` | BSD 2-Clause | Markdown parsing in Writing Studio | Yes |
| **Google Mobile Ads (AdMob)** | `com.google.android.gms:play-services-ads` | Android SDK TOS | Monetization | Yes |
| **Lucide Icons** | `lucide-icons/lucide` | ISC | Vector iconography | Yes |
| **Inter & Manrope Fonts** | `google/fonts` | SIL Open Font License 1.1 | Application UI & typography | Yes |
| **Noto Sans Devanagari** | `google/fonts` | SIL Open Font License 1.1 | Hindi text shaping | Yes |

---

## 3. Audited Reference Repositories (Architecture & Algorithm References Only)
The following repositories are utilized for architectural understanding and algorithm benchmarks without copying proprietary or copyleft code:
- **Image Toolbox (Apache 2.0):** Studied for modular image-processing filters and Exif handling.
- **PhotoEditor (Apache 2.0):** Studied for overlay matrix composition.
- **Open Video Editor (GPL v3):** Referenced strictly as a conceptual timeline architecture pattern; **NO CODE COPIED** to preserve license compliance.
- **Real-ESRGAN / NCNN Vulkan (BSD 3-Clause):** Studied for lightweight Vulkan-based upscaling.
