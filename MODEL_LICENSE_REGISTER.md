# AI Model License & Weights Register
## Kreavilo Studio (Creators Club)

Every AI model integrated or made available for optional download in Kreavilo Studio is audited for commercial viability, dataset ethics, memory footprint, and redistribution legality.

---

## 1. Model Verification Standards
1. **Commercial Use Authorization:** Must be licensed under Apache 2.0, MIT, or open-weight commercial permissions (e.g., Google Gemma Open Model License).
2. **On-Device Viability:** Must execute on Android CPUs, GPUs (Vulkan/OpenCL), or NPUs without thermal degradation or excessive battery drain.
3. **Optional Distribution:** Multi-megabyte model weights must never bloat the initial base APK download; they are installed on user request via the in-app **Model Manager**.
4. **Integrity Checks:** Every downloaded weight file is verified against a strict pre-compiled SHA-256 hash before initialization.

---

## 2. Model Audit Register

| Model Identifier | Primary Feature | Framework / Format | License | Download Size | Minimum Device RAM | Verification Status |
|---|---|---|---|---|---|---|
| **MediaPipe Image Segmenter** (Selfie & Interactive) | Photo & Product Studio (Background Removal) | TFLite / LiteRT (`.tflite`) | Apache 2.0 | ~9.6 MB | 3 GB | **Validated** (Shipped in core or optional download) |
| **ML Kit Text Recognition v2** | Document Studio (OCR) | ML Kit Bundled | Google APIs TOS (Free commercial use) | Dynamic module | 2 GB | **Validated** |
| **ML Kit Barcode Scanning** | Utility Studio (QR Scanner) | ML Kit Bundled | Google APIs TOS (Free commercial use) | Dynamic module | 2 GB | **Validated** |
| **Sobel Edge Filter (Mathematical)** | AR Drawing Studio (Outline tracing) | OpenCV / C++ NDK | Apache 2.0 | 0 MB (Code algorithm) | 2 GB | **Validated** (Zero memory overhead) |
| **Sherpa-ONNX Hindi/English ASR** (Optional) | Video Captions & Audio Studio | ONNX (`.onnx`) | Apache 2.0 | ~42 MB | 4 GB | **Research Validated** (Optional in Model Manager) |
| **Real-ESRGAN Compact NCNN** (Optional) | Photo Studio (AI Super Resolution 2x/4x) | NCNN Vulkan bin/param | BSD 3-Clause | ~18 MB | 6 GB | **Research Validated** (High-Tier devices only) |
| **Gemma 2B / FunctionGemma LiteRT** (Experimental) | Creator AI Level B (Local Generative) | LiteRT-LM (`.bin`) | Gemma Open License | ~1.4 GB | 8 GB | **Experimental** (Gated behind High-Tier opt-in) |

---

## 3. Fallback Guarantee
If an AI model fails to initialize, encounters insufficient RAM, or is skipped by the user, Kreavilo Studio **never displays a broken screen or error deadlock**. The application automatically falls back to deterministic algorithms:
- Background Removal ➔ Manual polygon/brush eraser.
- Edge Outline ➔ Sobel/Laplacian matrix edge operator.
- AI Assistant ➔ Level A universal SQLite keyword/intent registry.
