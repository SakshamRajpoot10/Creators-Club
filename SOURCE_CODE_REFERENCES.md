# Open-Source Source Code References
## Kreavilo Studio (Creators Club)

---

## 1. Consolidated Reference Directory

| Repository URL | Primary Purpose | License | Suitability for Mobile Kreavilo Architecture |
|---|---|---|---|
| [ImageToolbox](https://github.com/T8RIN/ImageToolbox) | Image manipulation, filters, Exif, crop | Apache 2.0 | High. Architectural reference for Compose-based image tools. |
| [ImageToolboxLibs](https://github.com/T8RIN/ImageToolboxLibs) | Reusable C++ / Kotlin image utilities | Apache 2.0 | High. Clean helper routines for color conversion and scaling. |
| [PhotoEditor](https://github.com/burhanrashid52/PhotoEditor) | Canvas overlay matrix transforms | Apache 2.0 | High. Reference for layer positioning and text/sticker dragging. |
| [uCrop](https://github.com/Yalantis/uCrop) | Aspect ratio cropping & rotation | Apache 2.0 | High. Standard reference for gesture-based image framing. |
| [uCrop-Compose](https://github.com/T8RIN/uCrop-Compose) | Jetpack Compose native cropping | Apache 2.0 | High. Directly applicable to Photo Studio crop mechanics. |
| [android-gpuimage](https://github.com/cats-oss/android-gpuimage) | OpenGL ES image filter shaders | Apache 2.0 | High. Reusable GLSL shader kernels for artistic LUT filters. |
| [OpenCV](https://github.com/opencv/opencv) | Computer vision & edge detection | Apache 2.0 | High. Sobel/Canny filters for AR tracing outline generator. |
| [MediaPipe Samples](https://github.com/google-ai-edge/mediapipe-samples) | Mobile segmentation & detection | Apache 2.0 | High. Official Google implementation for interactive segmentation. |
| [AndroidX Media](https://github.com/androidx/media) | Video playback & Transformer export | Apache 2.0 | Essential. Official Google Media3 video editing foundation. |
| [Android Media Samples](https://github.com/android/media-samples) | Media3 composition examples | Apache 2.0 | High. Reference for multi-track composition and audio ducking. |
| [PxerStudio](https://github.com/BennyKok/PxerStudio) | Pixel-art drawing canvas | Apache 2.0 | High. Reference for low-latency grid and layer rendering. |
| [Camera Samples](https://github.com/android/camera-samples) | CameraX integration patterns | Apache 2.0 | High. Reference for CameraX preview overlay and flashlight control. |
| [Google AI Edge Gallery](https://github.com/google-ai-edge/gallery) | On-device AI examples | Apache 2.0 | High. Reference for on-device TTS/STT and LiteRT model loading. |
| [LiteRT-LM](https://github.com/google-ai-edge/LiteRT-LM) | Mobile SLM inference | Apache 2.0 | Medium-High. Reference for optional Level B local AI assistant. |
| [CommonMark Java](https://github.com/commonmark/commonmark-java) | Markdown parsing | BSD 2-Clause | High. Lightweight parser for Writing Studio script drafts. |
| [ZXing](https://github.com/zxing/zxing) | Barcode & QR decoding | Apache 2.0 | High. QR generation and camera decoding in Document Studio. |
| [Lucide Icons](https://github.com/lucide-icons/lucide) | Vector iconography | ISC | High. Clean, permissive icons for creative studio tools. |
| [Google Fonts](https://github.com/google/fonts) | Inter, Manrope, Noto Devanagari | SIL OFL 1.1 | High. Redistributable typography for UI and design templates. |

---

## 2. Copyleft Quarantine Protocol
The following repositories are audited for conceptual patterns only; **NO CODE COPIED**:
- **Open Video Editor (GPL v3):** Kept in copyleft quarantine. Timeline composition will use pure AndroidX Media3 APIs.
- **Krita (GPL v3) & Paintroid (GPL v3):** Kept in copyleft quarantine. Drawing canvas will be built from scratch using native Jetpack Compose `Canvas` and `Path` smoothing.
