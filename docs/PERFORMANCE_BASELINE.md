# Performance Baseline & Hardware Optimization

**Application:** Kreavilo Studio  
**Target Device Tier:** Budget to Flagship Android Smartphones (2GB RAM to 16GB RAM)  

---

## 1. Memory Optimization & Bitmap Management

High-resolution creative editing on mobile devices can easily cause `OutOfMemoryError` (OOM) if unmanaged. Kreavilo Studio implements strict memory mitigation safeguards:

1. **Automatic Subsampling & Downsampling**:
   - Camera and imported photos exceeding 4096px are downsampled to a maximum canvas dimension of 2560px on high-end devices and 1920px on low-RAM devices during editing.
   - Filter preview thumbnails are generated at an ultra-compact 160x160 resolution, consuming less than 100 KB RAM per thumbnail.

2. **Explicit Bitmap Recycling**:
   - `Bitmap.recycle()` is called deterministically in ViewModel `onCleared()` and when temporary image processing buffers are rotated out of active memory.

3. **In-Memory Cache Boundaries**:
   - Coil Compose uses a bounded memory cache (25% of available JVM heap), evicting LRU bitmaps automatically under memory pressure.

---

## 2. Rendering & UI Thread Fluidity

- **60 FPS Hardware Acceleration**:
  - Jetpack Compose canvas drawing utilizes native hardware acceleration with `Modifier.drawWithCache` and `graphicsLayer` rendering to prevent unnecessary recompositions.
- **Background Thread Offloading**:
  - Image convolution, Tone Curve interpolation, Media3 video clipping, and Room DB operations are dispatched exclusively to `Dispatchers.Default` and `Dispatchers.IO`, keeping the main UI thread completely unblocked.
