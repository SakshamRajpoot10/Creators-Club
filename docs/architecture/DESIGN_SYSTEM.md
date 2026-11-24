# Design System & Visual Specification
## Kreavilo Studio (Creators Club)

---

## 1. Visual Identity & Brand Philosophy

Kreavilo Studio embodies **empowering creativity, calm focus, and modern elegance**.
The user interface avoids overwhelming the creator with visual noise. It uses a **clean light theme** with deliberate contrast, generous touch targets, and tactile Material 3 depth.

---

## 2. Color Palette & Design Tokens

### 2.1 Core Light Palette
```kotlin
object KreaviloColors {
    // Brand Primaries
    val Primary = Color(0xFF405BC7)           // Deep Creative Blue
    val PrimaryVariant = Color(0xFF2E449E)    // Darker interactive state
    val PrimaryContainer = Color(0xFFE4E9FA)  // Soft tint for selection pills
    val OnPrimary = Color(0xFFFFFFFF)

    // Brand Accents
    val Accent = Color(0xFF7664C7)            // Royal Studio Purple
    val AccentContainer = Color(0xFFEDE9F8)   // Light purple highlight
    val OnAccent = Color(0xFFFFFFFF)

    // Surfaces & Backgrounds
    val Background = Color(0xFFF7F8FC)        // Crisp, modern cool-off-white
    val Surface = Color(0xFFFFFFFF)           // Pure white card surfaces
    val SurfaceVariant = Color(0xFFEEF1F7)    // Subtle secondary panel
    val SurfaceContainerHigh = Color(0xFFE8ECF4)

    // Text & Hierarchy
    val TextPrimary = Color(0xFF182033)       // Deep charcoal ink
    val TextSecondary = Color(0xFF586174)     // Muted slate gray
    val TextTertiary = Color(0xFF8B95A5)      // Placeholder & disabled

    // Borders & Dividers
    val Border = Color(0xFFDFE4ED)            // Hairline divider
    val BorderLight = Color(0xFFEEF1F7)

    // Functional State Colors
    val Success = Color(0xFF237A57)           // Forest Emerald
    val Warning = Color(0xFFA16A17)           // Amber Ochre
    val Error = Color(0xFFB83C49)             // Muted Crimson
    val Info = Color(0xFF2B6CB0)

    // Studio Category Accent Badges
    val PhotoStudioBadge = Color(0xFF3B82F6)   // Blue
    val VideoStudioBadge = Color(0xFF8B5CF6)   // Purple
    val DesignStudioBadge = Color(0xFFF59E0B)  // Amber
    val DrawingStudioBadge = Color(0xFF10B981) // Green
    val ProductStudioBadge = Color(0xFFEC4899) // Pink
    val InteriorStudioBadge = Color(0xFF06B6D4)// Teal
    val WritingStudioBadge = Color(0xFF6366F1) // Indigo
    val AudioStudioBadge = Color(0xFF14B8A6)   // Mint
}
```

---

## 3. Typography Hierarchy

Fonts: **Manrope** (Headings & Brand Title) + **Inter** (Body & UI Controls) + **Noto Sans Devanagari** (Hindi shaping).

| Style | Font | Size (sp) | Weight | Line Height (sp) | Usage |
|---|---|---|---|---|---|
| **Display Large** | Manrope | 30 | Bold (700) | 36 | Welcome banner ("What will you create today?") |
| **Headline Medium**| Manrope | 22 | SemiBold (600) | 28 | Studio titles, section headers |
| **Title Medium**   | Inter | 16 | SemiBold (600) | 22 | Tool names, button labels, card headers |
| **Body Large**     | Inter | 14 | Regular (400) | 20 | Descriptions, prompts, assistant responses |
| **Body Medium**    | Inter | 12 | Regular (400) | 16 | Metadata, slider labels, tool hints |
| **Label Small**    | Inter | 10 | Medium (500) | 14 | Badges, tags, timeline timestamps |

---

## 4. UI Layout Specifications

### 4.1 Home Screen Structure
1. **Top Bar:**
   - Kreavilo Brand Icon & Wordmark (`Kreavilo`).
   - Notification & Updates Bell icon.
2. **Hero Greeting Section:**
   - Header: `"What will you create today?"`
   - Subhead: `"Your imagination starts here."`
   - Primary CTA: `"+ Create project"` button in `#405BC7` with rounded pill shape.
3. **Global Search Bar:**
   - Rounded pill input field with Search Icon: `"Find any creative tool"`.
   - Dynamic auto-complete connected to Creator AI's Feature Registry.
4. **Studio Grid (2 Columns, 8 Studio Cards):**
   - Photo Studio (`#E8F0FE` background, `#3B82F6` icon)
   - Video Studio (`#F3E8FF` background, `#8B5CF6` icon)
   - Design Studio (`#FEF3C7` background, `#F59E0B` icon)
   - Drawing Studio (`#D1FAE5` background, `#10B981` icon)
   - Product Studio (`#FCE7F3` background, `#EC4899` icon)
   - Interior Studio (`#CFFAFE` background, `#06B6D4` icon)
   - Writing Studio (`#E0E7FF` background, `#6366F1` icon)
   - Audio Studio (`#CCFBF1` background, `#14B8A6` icon)
5. **Creator AI Quick Card:**
   - Sparkle icon in `#7664C7`, text: `"Creator AI — Ask me anything"`.
   - Trailing Microphone button for 1-tap voice interaction.
6. **Bottom Navigation Bar (5 Items):**
   - `Home` (Dashboard)
   - `Studios` (Categorized tool catalogue)
   - `Create` (Center elevated primary action)
   - `Projects` (Project library and recents)
   - `Settings` (Preferences, Model Manager, Storage)

### 4.2 Creator AI Interface
- Header with back button, `"Creator AI"`, and audio speaker toggle.
- Avatar with glowing sparkle icon: `"Your creative companion — Create, explore and learn"`.
- Conversational cards displaying clean Markdown guidance.
- Interactive Action Pills (e.g., `[Open Product Studio]`, `[Remove Background]`, `[Apply 9:16 Crop]`).
- Voice transcription wave indicator during active speech recording.
- Bottom input bar with text field, send arrow, and microphone toggle.

---

## 5. Touch Targets & Accessibility
- Minimum touch target: `48 x 48 dp` for all interactive buttons and sliders.
- Screen reader accessibility: Full `contentDescription` on all icons and visual elements.
- Dynamic font scaling: Layouts adapt gracefully up to `200%` system font scaling.
- Color contrast: All text exceeds WCAG AA `4.5:1` ratio against backgrounds.
