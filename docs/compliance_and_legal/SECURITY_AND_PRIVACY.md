# Security, Privacy & Content Safety Specification
## Kreavilo Studio (Creators Club)

---

## 1. Privacy by Design Policy

Kreavilo Studio operates under the strict principle of **user sovereignty over personal media**:
1. **Zero Media Uploads:** User photographs, video recordings, audio tracks, drawings, and written scripts remain strictly on the user's physical device storage.
2. **Zero Mandatory Accounts:** No user registration, email harvesting, phone number verification, or password creation is required to use any creative studio.
3. **No Hidden Telemetry:** Diagnostics and analytics (if enabled via Firebase) collect only anonymized operational metrics (crash logs, device model, Android OS version, feature usage counts). No image contents, file names, text prompts, or GPS location data are ever transmitted.
4. **Explicit Permissions Just-in-Time:** Permissions are requested only when a specific feature is activated:
   - Camera: Only when launching AR Tracing or taking a direct photo.
   - Microphone: Only when recording voiceover or utilizing speech input.
   - Storage/Media: Only via standard Android Scoped Storage pickers.

---

## 2. Application Security Safeguards

### 2.1 File System & Scoped Storage
- Strict usage of Android Scoped Storage (`MediaStore` API on Android 10+ and Storage Access Framework).
- App-specific private directories (`context.getExternalFilesDir()`) used for working `.krv` caches and temporary frame renders.
- Automatic cache eviction on project closure and manual "Clear Cache" button in Settings.

### 2.2 Path Traversal & Archive Extraction Defense
When importing `.krv` project files or external asset zip archives:
- Every extracted zip entry is validated to ensure canonical paths do not escape the destination target folder (mitigating Zip Slip vulnerability).
- Symbolic links inside imported archives are rejected.

### 2.3 Decompression Bomb Protection
- High-resolution user images are pre-decoded with `inJustDecodeBounds = true` to check byte dimensions before memory allocation.
- Hard safety cap: Images exceeding `64 Megapixels` (e.g., 8192 x 8192) are automatically downsampled using `inSampleSize` to protect against Out-Of-Memory (OOM) crashes and malicious image bombs.

### 2.4 AI Model Security
- Models downloaded via the Model Manager must match pre-computed SHA-256 hashes.
- Model files are loaded from protected internal storage (`context.filesDir`) to prevent tampering by other apps.
- No downloaded file is ever executed as executable binary code; files are strictly parsed as data weights by verified inference engines.

---

## 3. Content Safety & Fair AI Disclosures
- **Honest Labeling:** Standard color filters, vignette, and HSL adjustments are never marketed deceptively as "Generative AI".
- **Deepfake Prevention:** No tools are provided for deceptive celebrity voice cloning or unauthorized face-swapping.
- **Clean Export Guarantee:** Kreavilo Studio never injects promotional watermarks or hidden user tracking metadata into exported files. Exif metadata stripping is available to protect user privacy before sharing online.
