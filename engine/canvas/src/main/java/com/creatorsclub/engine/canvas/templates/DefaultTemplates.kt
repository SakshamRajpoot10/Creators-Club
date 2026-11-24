package com.creatorsclub.engine.canvas.templates

import com.creatorsclub.engine.canvas.model.DesignTemplate
import com.creatorsclub.engine.canvas.model.TemplateCategory
import com.creatorsclub.engine.canvas.model.TemplateShapeSpec
import com.creatorsclub.engine.canvas.model.TemplateTextSpec

object DefaultTemplates {

    val allTemplates = listOf(
        DesignTemplate(
            templateId = "tmpl_festival_sale",
            title = "Mega Festival Sale 50% Off",
            category = TemplateCategory.INSTAGRAM_POST,
            canvasWidth = 1080,
            canvasHeight = 1080,
            backgroundColorHex = "#405BC7",
            rawShapeElements = listOf(
                TemplateShapeSpec("shape_bg_card", "ROUNDED_RECTANGLE", 80f, 80f, 920f, 920f, "#FFFFFF", 24f),
                TemplateShapeSpec("shape_pill", "ROUNDED_RECTANGLE", 290f, 680f, 500f, 100f, "#7664C7", 50f)
            ),
            rawTextElements = listOf(
                TemplateTextSpec("txt_title", "MEGA FESTIVAL", 540f, 260f, 44f, "#182033", isBold = true),
                TemplateTextSpec("txt_sub", "GRAND DHAMAKA SALE", 540f, 350f, 28f, "#405BC7", isBold = true),
                TemplateTextSpec("txt_discount", "FLAT 50% OFF", 540f, 480f, 62f, "#B83C49", isBold = true),
                TemplateTextSpec("txt_cta", "SHOP NOW", 540f, 730f, 26f, "#FFFFFF", isBold = true),
                TemplateTextSpec("txt_terms", "*Valid till stocks last", 540f, 840f, 16f, "#586174")
            )
        ),
        DesignTemplate(
            templateId = "tmpl_youtube_tech",
            title = "Pro Tech Review Thumbnail",
            category = TemplateCategory.YOUTUBE_THUMBNAIL,
            canvasWidth = 1280,
            canvasHeight = 720,
            backgroundColorHex = "#182033",
            rawShapeElements = listOf(
                TemplateShapeSpec("shape_badge", "ROUNDED_RECTANGLE", 100f, 420f, 360f, 90f, "#B83C49", 16f)
            ),
            rawTextElements = listOf(
                TemplateTextSpec("txt_headline", "DON'T BUY THIS!", 640f, 220f, 58f, "#FFFFFF", isBold = true),
                TemplateTextSpec("txt_sub", "HONEST REVIEW 2026", 640f, 320f, 36f, "#E4E9FA", isBold = true),
                TemplateTextSpec("txt_badge", "WATCH BEFORE BUYING", 280f, 465f, 20f, "#FFFFFF", isBold = true)
            )
        ),
        DesignTemplate(
            templateId = "tmpl_coffee_story",
            title = "Morning Coffee Vibe Story",
            category = TemplateCategory.INSTAGRAM_STORY,
            canvasWidth = 1080,
            canvasHeight = 1920,
            backgroundColorHex = "#F7F8FC",
            rawShapeElements = listOf(
                TemplateShapeSpec("shape_card", "ROUNDED_RECTANGLE", 100f, 400f, 880f, 1100f, "#FFFFFF", 32f),
                TemplateShapeSpec("shape_pill", "ROUNDED_RECTANGLE", 340f, 1200f, 400f, 90f, "#237A57", 45f)
            ),
            rawTextElements = listOf(
                TemplateTextSpec("txt_quote", "Start your morning with focus & creative energy.", 540f, 650f, 32f, "#182033", isBold = false, isItalic = true),
                TemplateTextSpec("txt_brand", "KREAVILO STUDIO", 540f, 850f, 22f, "#405BC7", isBold = true),
                TemplateTextSpec("txt_cta", "CREATE TODAY", 540f, 1245f, 22f, "#FFFFFF", isBold = true)
            )
        ),
        DesignTemplate(
            templateId = "tmpl_business_flyer",
            title = "Corporate Growth Flyer",
            category = TemplateCategory.BUSINESS_FLYER,
            canvasWidth = 1080,
            canvasHeight = 1440,
            backgroundColorHex = "#EEF1F7",
            rawShapeElements = listOf(
                TemplateShapeSpec("shape_header", "ROUNDED_RECTANGLE", 60f, 60f, 960f, 320f, "#405BC7", 20f),
                TemplateShapeSpec("shape_body", "ROUNDED_RECTANGLE", 60f, 420f, 960f, 940f, "#FFFFFF", 20f)
            ),
            rawTextElements = listOf(
                TemplateTextSpec("txt_hdr", "SCALE YOUR BUSINESS", 540f, 180f, 44f, "#FFFFFF", isBold = true),
                TemplateTextSpec("txt_subhdr", "Digital Marketing & Design Services", 540f, 260f, 22f, "#E4E9FA"),
                TemplateTextSpec("txt_point1", "✔ Professional Social Media Ad Creatives", 540f, 560f, 26f, "#182033", isBold = true),
                TemplateTextSpec("txt_point2", "✔ 4K Video Ads & Reel Editing", 540f, 680f, 26f, "#182033", isBold = true),
                TemplateTextSpec("txt_point3", "✔ Product Cutouts & E-Commerce Catalogs", 540f, 800f, 26f, "#182033", isBold = true),
                TemplateTextSpec("txt_contact", "Call / WhatsApp: +91 98765 43210", 540f, 1150f, 28f, "#405BC7", isBold = true)
            )
        ),
        DesignTemplate(
            templateId = "tmpl_youtube_banner",
            title = "Creator Channel Header Banner",
            category = TemplateCategory.YOUTUBE_BANNER,
            canvasWidth = 2560,
            canvasHeight = 1440,
            backgroundColorHex = "#12141C",
            rawShapeElements = listOf(
                TemplateShapeSpec("shape_safe_area", "ROUNDED_RECTANGLE", 510f, 500f, 1540f, 440f, "#1C2030", 24f),
                TemplateShapeSpec("shape_pill", "ROUNDED_RECTANGLE", 1080f, 790f, 400f, 90f, "#E11D48", 45f)
            ),
            rawTextElements = listOf(
                TemplateTextSpec("txt_title", "CREATORS CLUB", 1280f, 620f, 68f, "#FFFFFF", isBold = true),
                TemplateTextSpec("txt_sub", "NEW EPISODES EVERY TUESDAY & FRIDAY", 1280f, 720f, 26f, "#94A3B8", isBold = true),
                TemplateTextSpec("txt_cta", "SUBSCRIBE", 1280f, 835f, 24f, "#FFFFFF", isBold = true)
            )
        ),
        DesignTemplate(
            templateId = "tmpl_podcast_cover",
            title = "The Deep Work Mindset Podcast",
            category = TemplateCategory.PODCAST_COVER,
            canvasWidth = 1400,
            canvasHeight = 1400,
            backgroundColorHex = "#0F172A",
            rawShapeElements = listOf(
                TemplateShapeSpec("shape_frame", "ROUNDED_RECTANGLE", 100f, 100f, 1200f, 1200f, "#1E293B", 40f),
                TemplateShapeSpec("shape_tag", "ROUNDED_RECTANGLE", 450f, 320f, 500f, 100f, "#F59E0B", 50f)
            ),
            rawTextElements = listOf(
                TemplateTextSpec("txt_tag", "SEASON 03 • EPISODE 12", 700f, 370f, 26f, "#0F172A", isBold = true),
                TemplateTextSpec("txt_main", "THE ART OF DISCIPLINE", 700f, 620f, 54f, "#FFFFFF", isBold = true),
                TemplateTextSpec("txt_sub", "Building Habits That Stick Forever", 700f, 730f, 30f, "#94A3B8"),
                TemplateTextSpec("txt_host", "HOSTED BY SAKSHAM RAJPOOT", 700f, 1050f, 26f, "#38BDF8", isBold = true)
            )
        )
    )
}
