# Feature Traceability Matrix — Kreavilo Studio (10 Studios)

**Version:** 1.0.0-PROD  
**Coverage:** 100% Core Creative Features  

---

| Studio # | Studio Name | Primary User Persona | Core Engines Backing | Input Media Types | Key Editing Tools | Export Formats |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | **Photo Studio** | Photographers, Influencers | `:engine:image` | JPG, PNG, WEBP, HEIC | Tone Curve Grading, 30+ Filters, HSL Sliders, Smart Vignette, Grain, Sharpen, Crop & 360° Rotate | Lossless PNG, JPEG (95%), WEBP |
| **2** | **Video Studio** | Reel Creators, YouTubers | `:engine:video`, AndroidX Media3 | MP4, MOV, MKV, 3GP | Multi-clip Timeline, Split, Trim Handles, 0.25x-4x Speed Ramps, Audio Dubbing, 9:16 / 16:9 / 1:1 Canvas | MP4 (H.264 / AAC, 1080p, 60fps) |
| **3** | **Design Studio** | Marketers, Designers | `:engine:canvas`, `:core:designsystem` | Vector Shapes, PNG Stickers, Photos | Multi-layer Canvas, Custom Fonts, Badges, Drop Shadows, Opacity Blending, Color Picker | High-Res PNG, SVG, PDF |
| **4** | **Drawing Studio** | Digital Artists, Illustrators | `:engine:canvas` | Stylus & Finger Touch | Responsive Pencil, Ink, Highlighter, 8x Mandala Radial Symmetry, Eraser, Smooth Smoothing | Transparent PNG, JPEG |
| **5** | **Product Studio** | E-commerce Sellers, Small Businesses | `:engine:ai`, `:engine:image` | Product Photos | AI Background Cutout (MediaPipe), 3D Podium Placement, Studio Softbox Lighting, Commercial Badges | Transparent PNG, E-commerce JPEG |
| **6** | **Interior Studio** | Architects, Home Decorators | `:engine:canvas` | Blueprint Grids, Room Photos | 2D Metric Wall Grid, Furniture Stencils, Snap-to-Wall, Room Dimensions, Drag & Rotate | Metric Plan PNG, Blueprint PDF |
| **7** | **Writing Studio** | Scriptwriters, Creators | Compose Native | Text Scripts, Markdown | Auto-Scrolling Teleprompter, WPM Speed Slider, Hook Generator, Script Structure Templates | Formatted TXT, Markdown (.md) |
| **8** | **Audio Studio** | Podcasters, Narrators | `:engine:audio` | Microphone Stream, WAV, MP3 | 48 kHz WAV Recording, Real-time Oscilloscope, Offline TTS Narration, Audio Trimmer | Studio WAV (16-bit 48kHz), MP3 |
| **9** | **AR Tracing Studio** | Students, Aspiring Artists | `:engine:ai`, CameraX | Reference Photos, Camera Stream | Real-time Sobel Contour Filter, Screen Opacity Slider, Flashlight Torch Lock, Freeze-Frame | On-paper Tracing Guide |
| **10** | **Banner & Thumbnail Studio** | YouTubers, Social Media Managers | `:engine:canvas`, `:engine:image` | Photos, Badges, Stickers | YouTube Thumbnail (1280x720), YouTube Banner (2560x1440 with 1235x338 Safe Zone), Instagram (1080x1080, 1080x1920), X Header (1500x500), High-Contrast Stroke Text, Safe Zone Grid | Exact Platform PNG / JPEG |

---

## Architectural Guarantees
- **No Cloud Dependency**: Every tool operates 100% on the local device hardware.
- **Zero Watermarks**: All exports across all 10 studios are completely watermark-free.
- **Unified Manifest**: Any project created in any of the 10 studios is persisted in the shared Room Database and `ProjectManifest` format.
