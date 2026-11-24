# Known Limitations & Technical Decisions
## Kreavilo Studio (Creators Club)

---

## 1. Technical Decisions & Trade-Offs

### 1.1 On-Device AI vs. Cloud GPU AI
- **Decision:** All default AI functionality (segmentation, OCR, barcode scanning, intent parsing) operates strictly on-device without cloud servers.
- **Reason:** Aligns with the non-negotiable ₹0 budget and protects user privacy.
- **Limitation:** Text-to-image and text-to-video diffusion models (like Midjourney, Runway, or Kling AI) cannot run natively on budget mobile hardware with 3-4 GB RAM. Generative video is intentionally excluded from the production core until lightweight mobile models mature.
- **Honest Labeling:** The app will never disguise an image slideshow as "Generative AI Video".

### 1.2 PhonePe Appstore & Google AdMob Store Policy
- **Decision:** Prepare dual-store release capability (PhonePe Appstore + Samsung Galaxy Store / OPPO / VIVO) with the identical package name `com.creatorsclub.studio`.
- **Reason:** PhonePe Appstore is not currently included on Google's published list of AdMob-supported app stores. Apps published solely on unlisted stores face restricted ad serving.
- **Mitigation:** Dual-publishing ensures 100% AdMob app-readiness approval while prioritizing Indian users on PhonePe Appstore.

### 1.3 Light Mode First Launch
- **Decision:** Launch version 1.0 with a fully refined, tokenized light theme (#405BC7 primary, #F7F8FC background). Dark theme tokens are defined but disabled in UI until thoroughly audited for visual balance.
- **Reason:** Prevents visual inconsistencies and ensures high legibility under direct sunlight.

### 1.4 Native Android vs. Cross-Platform Frameworks
- **Decision:** 100% Native Kotlin + Jetpack Compose.
- **Reason:** Cross-platform engines (React Native, Flutter) introduce severe frame drops during low-latency stylus drawing, multi-track video timeline scrubbing, and OpenGL/Vulkan shader composition.
