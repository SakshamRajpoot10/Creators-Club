# Product Requirements Document (PRD)
## Kreavilo Studio (Internal: Creators Club)

**Version:** 1.0.0-PROD  
**Document Owner:** Saksham Rajpoot & Antigravity Engineering  
**Target Release:** PhonePe Appstore & Android Marketplaces  

---

## 1. Product Overview & Problem Statement

### 1.1 The Mobile Creator's Dilemma
Smartphone content creators, local shop owners, students, and freelancers face significant friction:
1. **App Fragmentation:** A single piece of content requires switching between 4–7 different applications (Snapseed for color grading, Photoroom for product cutouts, Canva for layout, CapCut for timeline editing, Lexis for audio cleanup, and Notion for scripts).
2. **Predatory Subscriptions:** Competitor apps aggressively paywall essential tools ($60 to $120/year), trap users with auto-renewing trials, or demand paid "AI credits".
3. **Export Watermarks:** Free tiers stamp large brand logos onto the user's work, destroying commercial viability for small businesses.
4. **Mandatory Cloud Lock-in & Privacy Violations:** Many apps force user media onto external servers and require mandatory account creation.

### 1.2 Kreavilo Studio Solution
Kreavilo Studio provides an **all-in-one, 100% free, offline-first mobile creative studio**:
- 10 connected creative studios in one lightweight native APK.
- Unified `.krv` project ecosystem allowing seamless cross-studio workflows.
- Clean, watermark-free exports across image, video, document, and audio formats.
- **Creator AI**: An intelligent on-device companion that routes users to the right tool and executes workflows via text or voice.
- Zero budget, zero per-user infrastructure cost, fully sustained by non-disruptive AdMob ads.

---

## 2. User Personas

| Persona | Needs & Goals | Core Studios Used | Key Features Needed |
|---|---|---|---|
| **Rohan (Local Shopkeeper / E-commerce Seller)** | Wants clean product photos for WhatsApp Catalog, Amazon, and Flipkart without hiring a photographer. | Product Studio, Design Studio, Photo Studio | Automatic background removal, white studio backdrops, price tags, offer banners. |
| **Priya (Instagram & YouTube Shorts Creator)** | Needs quick reel creation, trendy transitions, subtitles, and aesthetic cover thumbnails. | Video Studio, Photo Studio, Audio Studio, Writing Studio | Multi-track timeline, speed ramp, voiceover, auto-captions, 9:16 export, thumbnail generator. |
| **Ananya (Digital Artist & Student)** | Wants to sketch, paint, create mandalas/rangoli, and learn drawing. | Drawing Studio, AR Drawing Studio | Low-latency canvas, layers, symmetry tools, CameraX tracing over paper. |
| **Vikram (Freelance Marketer & Content Writer)** | Writes scripts, creates client posters, business cards, and brochures on the go. | Design Studio, Writing Studio, Document Studio | 100+ business templates, typography with Hindi support, teleprompter, PDF export. |

---

## 3. Product Features & Studio Specifications

### 3.1 Unified Creative Workspace
- **Project Structure (`.krv` format):** JSON manifest bundling layers, edits, transformations, and relative asset references.
- **Non-Destructive Pipeline:** Original media files are never overwritten. Edits are recorded as parametric operations and rendered on demand.
- **Project Management:** Create, open, rename, duplicate, favorite, folder grouping, trash bin with 30-day restore, automated autosave on pause, crash recovery state.
- **Cross-Studio Pipeline:** A photo edited in Photo Studio can be inserted into Design Studio canvas, animated in Video Studio, and exported as MP4 without file re-importing.

### 3.2 Studio Breakdown
1. **Photo Studio:** Crop, rotate, perspective, brightness, contrast, highlights, shadows, HSL, RGB curves, LUTs, 20+ artistic filters, blemish healing, clone stamp.
2. **Graphic Design Studio:** Freeform canvas with snapping, alignment guides, layers, opacity, blend modes, 100+ templates (Instagram, YouTube, Flyers, Cards), Hindi & English typography.
3. **Video Studio:** Multi-clip timeline, trimming, splitting, speed ramps, picture-in-picture, transitions (crossfade, slide, wipe, zoom), audio ducking, text overlays, 720p/1080p MP4 export.
4. **Product Photography Studio:** AI foreground cutout, white/gradient/studio backdrops, contact shadow simulation, batch resizing for e-commerce.
5. **Digital Drawing Studio:** Compose Canvas with pencil, pen, airbrush, watercolor brushes, smoothing, symmetry (mandala, rangoli, mirror), layer opacity and merge.
6. **AR Drawing Studio:** CameraX camera overlay, live transparency, edge detection (converts any photo into an outline for pencil tracing on paper), flashlight switch.
7. **Interior Design Studio:** 2D floorplan drawing with dimension tags, wall/floor color preview, furniture sticker staging.
8. **Writing & Script Studio:** Markdown notes, YouTube script templates, teleprompter with variable scroll speed.
9. **Audio Studio:** High-res voice recording, waveform trimmer, volume booster, silence stripper, text-to-speech.
10. **Animation Studio:** Frame-by-frame flipbook, onion skinning, pan-and-zoom storytelling, GIF and MP4 export.
11. **Document & Utility Studio:** Document scanner with perspective warp, OCR text extractor, QR/Barcode generator & scanner, image compressor, format converter.

### 3.3 Creator AI Assistant
- Integrated bottom-sheet and full-screen companion.
- **Registry-driven:** Backed by an immutable local database of all app capabilities (`featureId`, keywords, route, input/output).
- **Voice & Text I/O:** Bi-directional voice communication (SpeechRecognizer + TTS).
- **Action Execution:** Can open tools directly, apply preset adjustments, or orchestrate multi-step pipelines with user confirmation.

---

## 4. Non-Functional Requirements

### 4.1 Performance & Memory
- Cold startup time: `< 1.8 seconds` on mid-range hardware.
- UI responsiveness: Consistent `60 FPS` during canvas pan/zoom and timeline scrub.
- Memory consumption: Base app `< 180 MB` RAM; editing canvas `< 350 MB` RAM with bitmap recycling and subsampling.
- Download size: Target APK `< 50 MB` baseline; optional AI weights downloaded on user demand.

### 4.2 Security & Data Privacy
- 100% offline-first default operation.
- No mandatory user sign-up or profile creation.
- Android Scoped Storage compliance (`MediaStore` and Storage Access Framework).
- Path traversal sanitization on all import/export paths.
- Defense against decompression bombs (max bitmap dimension and memory limits).

### 4.3 Platform & Device Compatibility
- Minimum SDK: Android 8.0 (API level 26).
- Target SDK: Android 15 (API level 35).
- Hardware profiles: Basic (2-3 GB RAM), Mid-range (4-6 GB RAM), High (8+ GB RAM) with graceful feature adaptation.
