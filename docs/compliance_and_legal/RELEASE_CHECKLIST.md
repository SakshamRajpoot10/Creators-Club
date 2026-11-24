# Release Readiness Checklist
## Kreavilo Studio (Creators Club)

---

## Release Verification Checklist (Target: Version 1.0.0)

### 1. Legal & Licensing Verification
- [ ] No GPL or AGPL copyleft code linked into closed/proprietary modules.
- [ ] All fonts licensed under SIL Open Font License 1.1.
- [ ] All icons licensed under permissive MIT / ISC / Apache 2.0.
- [ ] `THIRD_PARTY_NOTICES.md` fully populated and packaged in app Settings.
- [ ] Brand name "Kreavilo Studio" cleared of active trademark conflicts.

### 2. Functional & Studio Verification
- [ ] Photo Studio: Crop, rotate, HSL, curves, filters, and export verified.
- [ ] Design Studio: Drag-and-drop, layers, typography, and templates verified.
- [ ] Video Studio: Timeline, trimming, transitions, audio, and MP4 export verified.
- [ ] Product Studio: Background removal, studio staging, and drop shadows verified.
- [ ] Drawing Studio: Brushes, pressure sensitivity, layers, and symmetry verified.
- [ ] AR Drawing Studio: CameraX live tracing, edge detection, and flashlight verified.
- [ ] Audio Studio: Voice recording, waveform trimmer, and TTS verified.
- [ ] Writing Studio: Script notes and teleprompter verified.
- [ ] Creator AI: Universal intent matching, tool navigation, and voice I/O verified.

### 3. Monetization & Security Verification
- [ ] Google AdMob test IDs replaced with production ad unit IDs only at final release build.
- [ ] No ads placed over editing canvases, during recordings, or during exports.
- [ ] Scoped storage enforced; no sensitive files stored in world-readable paths.
- [ ] Decompression bomb safeguards verified for images > 64 Megapixels.

### 4. Store Assets & Packaging
- [ ] Production keystore generated and securely archived.
- [ ] Release APK / AAB compiled with Proguard / R8 optimization.
- [ ] App icon (512x512 PNG) and feature banner (1024x500 PNG) finalized.
- [ ] Bilingual store listing (English + Hindi) verified.
- [ ] Responsive GitHub Pages privacy policy website deployed.
- [ ] Final user review and explicit approval obtained from Saksham Rajpoot before store upload.
