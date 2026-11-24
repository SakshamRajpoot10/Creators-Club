# Cost Register & Cloud Expenditure Analysis

**Application:** Kreavilo Studio (Creators Club)  
**Audited Period:** Perpetual Deployment  
**Cloud Billing Target:** Exactly $0.00 / INR 0.00  

---

## 1. Zero Cloud Cost Guarantee

Unlike traditional SaaS creative platforms (e.g. Canva, Adobe Creative Cloud, CapCut Pro) which route video rendering, image filters, AI background cutouts, and storage through expensive cloud servers:

**Kreavilo Studio executes 100% of all computational tasks locally on the user's mobile device.**

### 1.1 Localized Technology Substitution Matrix

| Expensive Cloud Service | Typical Cost | Kreavilo Studio Local Engine | Ongoing Cost to Developer |
| :--- | :--- | :--- | :--- |
| **AWS Rekognition / Cloud OCR** | $1.50 per 1,000 images | Google ML Kit (On-Device OCR) | **$0.00** |
| **Remove.bg / PhotoRoom API** | $0.20 per cutout | MediaPipe Interactive Segmenter (Local TFLite) | **$0.00** |
| **Cloud Video Rendering (Shotstack / FFmpeg API)** | $0.05 per minute | AndroidX Media3 Transformer (Local GPU OpenGL/MediaCodec) | **$0.00** |
| **AWS Polly / Google Cloud TTS** | $16.00 per 1M characters | Android Native TextToSpeech Engine (Offline Speech Synthesizer) | **$0.00** |
| **Cloud Database (Firebase Firestore / AWS DynamoDB)** | Variable read/write costs | SQLite via Android Room Database | **$0.00** |
| **Amazon S3 Asset Storage** | $0.023 per GB/mo | Android Scoped Internal Storage (`filesDir`) | **$0.00** |

---

## 2. Monetization & Net Profit Projection

Because operating expenses are **$0.00**, 100% of the ad revenue generated via Google AdMob (banner, interstitial, rewarded) flows directly as net profit to the developer, Saksham Rajpoot. This architecture guarantees the application can support millions of active users without ever incurring hosting debt.
