package com.creatorsclub.engine.ai.registry

data class RegisteredTool(
    val featureId: String,
    val nameEn: String,
    val nameHi: String,
    val keywords: List<String>,
    val targetStudioId: String,
    val actionCommand: String,
    val workflowSteps: List<String>
)

object FeatureCatalog {

    val allTools = listOf(
        RegisteredTool(
            featureId = "product.remove_bg",
            nameEn = "Remove Background (Product Studio)",
            nameHi = "बैकग्राउंड हटाएं (प्रोडक्ट स्टूडियो)",
            keywords = listOf("remove background", "background", "cutout", "transparent", "background hataye", "bg remove", "cut photo"),
            targetStudioId = "product",
            actionCommand = "OPEN_PRODUCT_STUDIO",
            workflowSteps = listOf(
                "1. Import your product or subject photo.",
                "2. AI detects foreground subject automatically.",
                "3. Select a white studio or pedestal backdrop, then export."
            )
        ),
        RegisteredTool(
            featureId = "photo.adjust",
            nameEn = "Photo Studio Color Adjustments",
            nameHi = "फोटो कलर और फिल्टर्स (फोटो स्टूडियो)",
            keywords = listOf("photo edit", "brightness", "contrast", "color grade", "filters", "cinematic", "hsl", "curves", "photo sudhare"),
            targetStudioId = "photo",
            actionCommand = "OPEN_PHOTO_STUDIO",
            workflowSteps = listOf(
                "1. Open Photo Studio with your photo.",
                "2. Apply 1-tap Cinematic, Vintage, or B&W filters.",
                "3. Fine-tune Brightness, Saturation, or Contrast sliders."
            )
        ),
        RegisteredTool(
            featureId = "design.poster",
            nameEn = "Create Sale Poster & Ad (Design Studio)",
            nameHi = "सेल पोस्टर और बैनर बनाएं (डिज़ाइन स्टूडियो)",
            keywords = listOf("poster", "banner", "flyer", "ad", "instagram post", "thumbnail", "sale banner", "poster banaye"),
            targetStudioId = "design",
            actionCommand = "OPEN_DESIGN_STUDIO",
            workflowSteps = listOf(
                "1. Open Design Studio.",
                "2. Pick an Instagram, YouTube, or Sale template.",
                "3. Customize text, shapes, and export in 1080p."
            )
        ),
        RegisteredTool(
            featureId = "video.reels",
            nameEn = "Video & Reel Timeline Editor",
            nameHi = "वीडियो और रील्स एडिट करें (वीडियो स्टूडियो)",
            keywords = listOf("video", "reel", "shorts", "trim video", "speed ramp", "transitions", "subtitles", "video banaye"),
            targetStudioId = "video",
            actionCommand = "OPEN_VIDEO_STUDIO",
            workflowSteps = listOf(
                "1. Import your video clips into Video Studio.",
                "2. Trim clips and set speed ramp (1x, 2x).",
                "3. Export high-definition MP4 without watermark."
            )
        ),
        RegisteredTool(
            featureId = "drawing.mandala",
            nameEn = "Digital Drawing & Mandala / Rangoli",
            nameHi = "ड्राइंग, रंगोली और मंडला आर्ट (ड्राइंग स्टूडियो)",
            keywords = listOf("draw", "drawing", "paint", "mandala", "rangoli", "sketch", "pencil", "rangoli banaye"),
            targetStudioId = "drawing",
            actionCommand = "OPEN_DRAWING_STUDIO",
            workflowSteps = listOf(
                "1. Open Drawing Studio.",
                "2. Select Mandala (8x) or Mirror symmetry mode.",
                "3. Draw with smooth pencil or ink brushes."
            )
        ),
        RegisteredTool(
            featureId = "ar.tracing",
            nameEn = "Camera AR Tracing on Paper",
            nameHi = "कैमरा AR ट्रेसिंग से ड्राइंग सीखें",
            keywords = listOf("ar drawing", "tracing", "trace", "sketch on paper", "draw on paper", "outline", "kagaz par drawing"),
            targetStudioId = "ar_drawing",
            actionCommand = "OPEN_AR_TRACING",
            workflowSteps = listOf(
                "1. Choose any reference picture.",
                "2. Filter converts it into a high-contrast outline.",
                "3. Lock screen and place phone above paper to trace."
            )
        ),
        RegisteredTool(
            featureId = "writing.teleprompter",
            nameEn = "Scriptwriting & Video Teleprompter",
            nameHi = "स्क्रिप्ट और वीडियो टेलीप्रॉम्प्टर",
            keywords = listOf("script", "write", "teleprompter", "youtube script", "hook", "camera prompter", "script likhe"),
            targetStudioId = "writing",
            actionCommand = "OPEN_WRITING_STUDIO",
            workflowSteps = listOf(
                "1. Choose a YouTube or Reel script template.",
                "2. Write your points.",
                "3. Launch floating Teleprompter while recording video."
            )
        ),
        RegisteredTool(
            featureId = "audio.voiceover",
            nameEn = "High-Quality Audio & Voice Recording",
            nameHi = "वॉइस रिकॉर्डिंग और ऑडियो स्टूडियो",
            keywords = listOf("audio", "record voice", "voiceover", "mic", "tts", "voice", "awaz record"),
            targetStudioId = "audio",
            actionCommand = "OPEN_AUDIO_STUDIO",
            workflowSteps = listOf(
                "1. Tap record button with live waveform visualizer.",
                "2. Or enter text for natural AI voice narration.",
                "3. Save and insert directly into your video."
            )
        ),
        RegisteredTool(
            featureId = "interior.floorplan",
            nameEn = "Interior Design & 2D Floorplan",
            nameHi = "इंटीरियर डिजाइन और रूम फ्लोरप्लान",
            keywords = listOf("interior", "room plan", "floorplan", "furniture", "room design", "ghar ka naksha"),
            targetStudioId = "interior",
            actionCommand = "OPEN_INTERIOR_STUDIO",
            workflowSteps = listOf(
                "1. Open Interior Studio.",
                "2. Drag furniture items like Sofa, Bed, or Wardrobe.",
                "3. Rotate and position furniture in your room."
            )
        )
    )
}
