# Performance Benchmark & Device Profile Report
## Kreavilo Studio (Creators Club)

---

## 1. Device Hardware Profiles

To ensure smooth performance across affordable and flagship Android smartphones in India, the application adheres to three hardware runtime profiles:

| Profile | RAM Target | Processor / GPU Baseline | Enabled Features | Memory Budget |
|---|---|---|---|---|
| **Tier 1: Basic Profile** | 2 GB – 3 GB | Quad/Octa-core ARM Cortex-A53 / A55, Adreno 500/600, Mali-G52 | Photo Studio (up to 12MP), Design Studio, Drawing Studio, AR Tracing, Documents, Level A Assistant, Basic Video (720p). | Max 180 MB Heap |
| **Tier 2: Mid-Range Profile** | 4 GB – 6 GB | Snapdragon 6/7 series, MediaTek Dimensity 6000/7000 | All Tier 1 + MediaPipe Background Cutout, 1080p Video Timeline, Complex Shaders, Audio Studio, Level A Assistant. | Max 350 MB Heap |
| **Tier 3: High Profile** | 8 GB+ | Snapdragon 8 series, Dimensity 8000/9000 | All Tier 2 + Super Resolution (Real-ESRGAN), Optional LiteRT SLM, 4K Video Export, Multimodal analysis. | Max 600 MB Heap |

---

## 2. Measured Baseline Metrics (Development Host Machine)
- Host CPU: AMD Ryzen 7 7735HS (8 Cores, 16 Threads)
- Host RAM: 16 GB DDR5
- Disk IO: NVMe SSD (>2500 MB/s read/write)
- Gradle Build Cache: Local SSD storage
