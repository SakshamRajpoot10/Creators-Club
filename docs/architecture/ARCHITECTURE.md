# System Architecture Specification
## Kreavilo Studio (Creators Club)

---

## 1. Architectural Principles

1. **Modular Offline-First Architecture:** Every studio operates independently while sharing a common core and engine layer.
2. **Non-Destructive Media Pipeline:** Original source assets are never overwritten. Edits are stored as an acyclic graph of operations inside `.krv` files and rendered dynamically.
3. **Hardware Acceleration by Default:** UI rendered via Jetpack Compose; image effects rendered via OpenGL ES / custom Canvas shaders; video rendered via Media3 hardware codecs.
4. **Strict Device Profiling:** Dynamic feature scalability according to device RAM and SoC capabilities (Basic, Mid, High tiers).
5. **Decoupled AI Engine:** Core creative tools remain 100% functional even if optional neural models are not downloaded or if the device lacks NPU acceleration.

---

## 2. Multi-Module Hierarchy

```
                    ┌────────────────────────┐
                    │          :app          │
                    └───────────┬────────────┘
                                │
        ┌───────────────────────┴───────────────────────┐
        ▼                                               ▼
┌───────────────┐                               ┌───────────────┐
│ :feature:*    │                               │   :engine:*   │
│ - home        │                               │ - image       │
│ - projects    │                               │ - video       │
│ - photo       │──────────────────────────────>│ - audio       │
│ - design      │                               │ - canvas      │
│ - video       │                               │ - ai          │
│ - drawing     │                               │ - export      │
│ - ar_drawing  │                               └───────┬───────┘
│ - product     │                                       │
│ - interior    │                                       │
│ - writing     │                                       │
│ - animation   │                                       │
│ - ai_assistant│                                       │
│ - documents   │                                       │
│ - settings    │                                       │
└───────┬───────┘                                       │
        │                                               │
        └───────────────────────┬───────────────────────┘
                                │
                                ▼
                        ┌───────────────┐
                        │    :core:*    │
                        │ - designsystem│
                        │ - navigation  │
                        │ - database    │
                        │ - storage     │
                        │ - media       │
                        │ - permissions │
                        │ - common      │
                        └───────────────┘
```

---

## 3. Data Architecture & Unified Project Format (`.krv`)

### 3.1 Project File Container
A `.krv` file is a compressed zip archive containing:
1. `manifest.json` — Metadata, schema version, canvas dimensions, frame rate, audio tracks, and layer definitions.
2. `thumbnail.webp` — 512x512 preview image for instant loading in the Project Library.
3. `assets/` — Embedded local copies of imported images/audio (or scoped content URI pointers).

### 3.2 Manifest Schema Example
```json
{
  "formatVersion": 1,
  "projectId": "proj_9b1deb4d3b7d",
  "name": "Summer Shoe Sale Ad",
  "studioType": "DESIGN",
  "createdAt": 1732429600000,
  "modifiedAt": 1732431200000,
  "canvas": {
    "width": 1080,
    "height": 1350,
    "aspectRatio": "4:5",
    "backgroundColor": "#FFFFFF"
  },
  "layers": [
    {
      "layerId": "layer_01",
      "type": "IMAGE",
      "assetRef": "assets/product_cutout.png",
      "transform": {
        "x": 120.0,
        "y": 250.0,
        "scale": 1.15,
        "rotation": 0.0,
        "opacity": 1.0
      },
      "adjustments": {
        "brightness": 0.05,
        "contrast": 1.10,
        "saturation": 1.20
      }
    },
    {
      "layerId": "layer_02",
      "type": "TEXT",
      "text": "FLAT 40% OFF",
      "fontFamily": "Inter-Bold",
      "fontSize": 48.0,
      "color": "#405BC7",
      "transform": {
        "x": 200.0,
        "y": 950.0,
        "scale": 1.0,
        "rotation": -5.0,
        "opacity": 1.0
      }
    }
  ]
}
```

---

## 4. Creator AI Assistant Engine Architecture

The AI subsystem operates in three strict tiers:

```
┌─────────────────────────────────────────────────────────────┐
│                    User Voice / Text Query                  │
└──────────────────────────────┬──────────────────────────────┘
                               │
                               ▼
            ┌────────────────────────────────────┐
            │   Level A: Universal Rule Engine   │
            │   - Keyword & Intent Matcher       │
            │   - SQLite FTS5 Feature Registry   │
            │   - 100% Offline, Zero Overhead    │
            └──────────────────┬─────────────────┘
                               │
        ┌──────────────────────┴──────────────────────┐
        │ Model Available?                            │
        ▼ (YES)                                       ▼ (NO)
┌────────────────────────────────┐         ┌─────────────────────────────────┐
│ Level B: Local LiteRT SLM      │         │ Level A Direct Action           │
│ - Compact FunctionGemma / SLM  │         │ - Exact Navigation Link         │
│ - Structured JSON Action Call  │         │ - Workflow Step Breakdown       │
└───────────────┬────────────────┘         └────────────────┬────────────────┘
                │                                           │
                └──────────────────────┬────────────────────┘
                                       │
                                       ▼
                    ┌────────────────────────────────────┐
                    │      Allowlisted Action Router     │
                    │      - Validate Arguments          │
                    │      - Permission Check            │
                    │      - Safety Confirmation         │
                    └──────────────────┬─────────────────┘
                                       │
                                       ▼
                    ┌────────────────────────────────────┐
                    │      Target Studio / Tool UI       │
                    └────────────────────────────────────┘
```

---

## 5. Monetization & Ad Placement Architecture

To maintain user trust and avoid disruptive interruptions, ads strictly follow these architectural placement rules:

1. **Native Card Ads:** Displayed only in browsing lists (Home feed between template categories, Projects library bottom shelf).
2. **Banner Ads:** Placed exclusively on non-canvas screens (Settings, Help, Document tools).
3. **Interstitial Ads:** Triggered strictly on natural user completion points (e.g., after an export completes and the user clicks "Done"), with a maximum frequency of 1 per 6 minutes.
4. **Never Placed In:**
   - Over active photo, design, video, or drawing canvases.
   - During active audio recording or speech input.
   - During video rendering/export processing.
   - To unlock normal core editing tools or restore user projects.
