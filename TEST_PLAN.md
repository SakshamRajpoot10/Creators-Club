# Quality Assurance & Testing Plan
## Kreavilo Studio (Creators Club)

---

## 1. Testing Philosophy

A feature is considered complete **only when it performs its actual function under real mobile constraints**. No mock features or visual placebos are permitted.

---

## 2. Testing Levels & Acceptance Gates

### 2.1 Level 1: Unit & Algorithm Tests (JVM)
- **Image Math Tests:** Crop bounding boxes, affine transform matrix calculations, HSL-to-RGB conversion accuracy.
- **Serialization Tests:** Save and reload `.krv` project manifest with 100% roundtrip fidelity.
- **Feature Registry Tests:** Intent matcher correctness across English, Hindi, and Hinglish queries.
- **Security Tests:** Path traversal rejection, max megapixel decompression guard.

### 2.2 Level 2: Instrumentation & Media Pipeline Tests (Android Device/Emulator)
- **Bitmap Memory Safety:** Load 20MP, 40MP, and 50MP images without triggering `OutOfMemoryError`.
- **Media3 Video Composition:** Assemble 3 video clips with a crossfade transition and verify exported MP4 playback with audio-video sync.
- **Room Database Integrity:** Atomic saves, migrations, soft deletes, and search queries.

### 2.3 Level 3: Cross-Studio End-to-End Integration Workflows
1. **The Product Seller Workflow:**
   - Import real product photo ➔ Run MediaPipe background cutout ➔ Add studio pedestal backdrop ➔ Add "Special Offer ₹499" text in Design Studio ➔ Export clean JPEG ➔ Open in Video Studio and create a 10s MP4 reel with voiceover.
2. **The Digital Artist Workflow:**
   - Open Drawing Studio ➔ Sketch with pencil brush ➔ Enable 8-fold radial symmetry (mandala) ➔ Add layer ➔ Export transparent PNG.
3. **The Beginner Tracing Workflow:**
   - Open AR Drawing Studio ➔ Choose reference outline ➔ Apply Sobel edge detection ➔ Adjust opacity to 40% ➔ Lock screen ➔ Trace on paper.

### 2.4 Level 4: Performance & Stress Benchmarks
- Cold start time `< 1.8s`.
- 60 FPS drawing stroke latency (`< 16ms` touch-to-render).
- Low storage warning test: Graceful failure notification when device disk space is `< 100 MB`.
- Background task restoration: App sent to background during export must continue cleanly via WorkManager.
