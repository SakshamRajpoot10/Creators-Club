# Cost & Infrastructure Report (₹0 Budget Audit)
## Kreavilo Studio (Creators Club)

---

## 1. Zero-Cost Infrastructure Verification

A core non-negotiable requirement is **zero ongoing operational costs** and **no per-user API bills**.
The table below details every technological layer and confirms its permanent cost profile:

| Component | Technology Selected | Direct Cost | Free Limits / Auto-Billing Safeguards | Fallback Plan If Service Discontinued |
|---|---|---|---|---|
| **Development Toolchain** | Android Studio, Kotlin, Gradle | ₹0 | Unlimited free open-source tooling. | N/A |
| **Local Database** | Room (SQLite on-device) | ₹0 | Runs 100% on user's device storage. | N/A |
| **Media Processing** | AndroidX Media3, Compose Canvas, OpenCV | ₹0 | Uses user's smartphone CPU/GPU/DSP. | N/A |
| **Computer Vision / AI** | MediaPipe, ML Kit, LiteRT | ₹0 | On-device inference; zero cloud server calls. | Traditional algorithms (Sobel, template logic). |
| **Speech & Audio** | Android SpeechRecognizer, native TTS | ₹0 | Built-in system platform services. | Text-only fallback mode. |
| **Source Control & CI** | GitHub Free Public/Private Repo | ₹0 | Free unlimited private/public repositories. | Local Git backups. |
| **Public Website & Docs** | GitHub Pages | ₹0 | 100 GB/month bandwidth free for static HTML/CSS. | Cloudflare Pages (Free Tier). |
| **Monetization SDK** | Google AdMob | ₹0 | Free integration; developer earns ad revenue. | Unity Ads / AppLovin fallback. |
| **Marketplace Listing** | PhonePe Appstore | ₹0 (Current) | Free developer onboarding currently. | Samsung Galaxy Store (Free developer account). |
| **Optional Model Hosting** | Hugging Face Hub (Public Model weights) | ₹0 | Free bandwidth for open-weights distribution. | GitHub Releases / direct HTTPS mirrors. |

---

## 2. Strict Prohibition on Auto-Billing Infrastructure
1. **NO paid cloud AI APIs:** Never integrate OpenAI, Claude, Google Cloud Vision, or AWS Rekognition in the default app flow.
2. **NO cloud GPU servers:** Never deploy Colab, RunPod, Lambda Labs, or AWS EC2 backend dependencies.
3. **NO paid managed databases:** User projects reside solely on local flash storage. No Firebase Firestore / Supabase paid tier locks.
4. **NO credit card linked to cloud projects without explicit user authorization:** Prevents unexpected overage charges.
