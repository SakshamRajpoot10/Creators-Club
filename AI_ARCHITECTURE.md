# AI Architecture & Creator AI System Specification
## Kreavilo Studio (Creators Club)

---

## 1. Architectural Mission of Creator AI

**Creator AI** is not an external chatbot or a gimmick. It is the **connective nervous system of Kreavilo Studio**:
1. It maintains a live, indexed registry of every studio, tool, preset, and workflow.
2. It translates user voice and text queries in English, Hindi, and Hinglish into concrete application actions.
3. It guides users through complex multi-studio workflows (e.g., product photo cutout ➔ sale poster ➔ 15-second promotional video).
4. It operates on a strict **zero-budget, 100% offline-first hierarchy**.

---

## 2. Three-Tier Operating Architecture

```
┌────────────────────────────────────────────────────────────────────────┐
│                        User Prompt (Voice or Text)                     │
│               "Mujhe is photo ka background remove karna hai"          │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                    Tier A: Universal Intelligence                      │
│             - Runs 100% locally on ALL Android devices                │
│             - Zero model download required (0 MB footprint)            │
│             - SQLite FTS5 Full-Text Match + Levenshtein Fuzzy Search    │
│             - Multilingual Intent Parser (Hindi, English, Hinglish)    │
│             - Returns instant routing, tool buttons & step guidance    │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
          ┌─────────────────────────┴─────────────────────────┐
          │ Optional Model Installed & High-RAM Device?       │
          ▼ (YES)                                             ▼ (NO)
┌───────────────────────────────────────┐         ┌──────────────────────────────┐
│ Tier B: Local Quantized SLM           │         │ Fallback to Tier A Action    │
│ - LiteRT-LM runtime                   │         │ - Structured Action Card     │
│ - FunctionGemma / Compact Gemma SLM   │         │ - 1-Tap "Open Tool" Button   │
│ - Contextual multi-step execution     │         │ - Direct Workflow Dispatch   │
└───────────────────┬───────────────────┘         └──────────────┬───────────────┘
                    │                                            │
                    └─────────────────────┬──────────────────────┘
                                          │
                                          ▼
┌────────────────────────────────────────────────────────────────────────┐
│                      Allowlisted Action Dispatcher                     │
│          - Verifies current state, permissions, and arguments          │
│          - NEVER executes arbitrary code or unsanitized routes         │
│          - Asks user confirmation before destructive actions           │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│                        Target Studio Action Execution                  │
│                   (e.g., Opens Product Studio with Cutout)             │
└────────────────────────────────────────────────────────────────────────┘
```

---

## 3. Structured Feature Registry Schema

Every tool in Kreavilo Studio is registered in SQLite with the following structure:

```json
{
  "featureId": "product.background_removal",
  "displayName": "Remove Background",
  "displayNameHindi": "पृष्ठभूमि हटाएं",
  "aliases": [
    "background remover",
    "cutout",
    "transparent png",
    "photo cutout",
    "background hataye",
    "remove photo bg"
  ],
  "studio": "PRODUCT_STUDIO",
  "navigationRoute": "studio/product/cutout",
  "requiredInputs": ["IMAGE_URI"],
  "outputType": "TRANSPARENT_BITMAP",
  "minMemoryTier": "BASIC",
  "isOffline": true,
  "actionCommand": "ACTION_EXECUTE_CUTOUT",
  "stepGuide": [
    "1. Import your product or subject photo.",
    "2. AI detects foreground subject automatically.",
    "3. Refine edges if desired, then choose a studio background."
  ]
}
```

---

## 4. Voice Processing Engine
- **Speech-to-Text (STT):** Utilizes Android `SpeechRecognizer` with automatic fallback to offline language packs.
- **Text-to-Speech (TTS):** Utilizes Android native `TextToSpeech` engine, configured for natural cadence in both Indian English (`en-IN`) and Hindi (`hi-IN`).
- **User Control:** Microphone indicator turns off immediately upon speech completion; user can toggle voice feedback off at any time.
