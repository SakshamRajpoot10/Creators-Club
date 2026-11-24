# Security & Data Safety Review

**Application:** Kreavilo Studio  
**Target:** Android 14 / 15 & PhonePe Indus Appstore Policy Compliance  
**Date:** November 24, 2026  

---

## 1. Scoped Storage & Sandbox Enforcement

1. **Zero External Storage Invasions**:
   Kreavilo Studio does **not** declare or request `READ_EXTERNAL_STORAGE` or `WRITE_EXTERNAL_STORAGE`. All project manifests, state files, and rendered assets are isolated within the application's private sandbox directory (`Context.filesDir`), ensuring complete protection from unauthorized external app snooping.

2. **Scoped Media Access**:
   When users import their own photos, videos, or audio into the creative studios, access is facilitated exclusively via the Android Photo Picker (`ActivityResultContracts.PickVisualMedia`) and Storage Access Framework (`ActivityResultContracts.GetContent`). Temporary read URIs are consumed safely without requesting global filesystem permissions.

---

## 2. Camera & Microphone Hardware Safety

- **CameraX**: Requested only in AR Tracing Studio. The camera stream is bound directly to the hardware preview surface and processed exclusively in RAM using the Sobel edge filter. Frames are never recorded, transmitted, or uploaded to any remote server.
- **AudioRecord**: Requested only in Audio Studio when the user explicitly taps "Record Voiceover". Audio is captured as raw PCM samples directly to internal storage (`audio_recording.wav`). The microphone is released immediately when recording stops.

---

## 3. Network & Data Safety Declarations

- **Data Collected**: Zero personally identifiable information (PII). No GPS coordinates, no contacts, no biometric data.
- **Data Shared**: None. All project creations belong 100% to the user and remain on their device.
- **Third-Party Telemetry**: Only Google AdMob standard SDK diagnostics for non-personalized advertisement display.
