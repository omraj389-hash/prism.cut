package com.example.model

/**
 * Represents a 3-way color wheel parameter (Lift, Gamma, Gain, Offset)
 * @param hue Hue angle in degrees [0..360)
 * @param saturation Saturation strength [0..1]
 * @param luminance Luminance adjustment [-1..1]
 */
data class ColorWheelSetting(
    val hue: Float = 0f,
    val saturation: Float = 0f,
    val luminance: Float = 0f
) {
    val isDefault: Boolean
        get() = saturation == 0f && luminance == 0f
}

/**
 * Professional Color Grading parameters matching industry NLE standards (e.g. DaVinci Resolve)
 */
data class ColorGradeSettings(
    // 3-Way Color Wheels
    val lift: ColorWheelSetting = ColorWheelSetting(),       // Shadows
    val gamma: ColorWheelSetting = ColorWheelSetting(),      // Midtones
    val gain: ColorWheelSetting = ColorWheelSetting(),       // Highlights
    val offset: ColorWheelSetting = ColorWheelSetting(),     // Global Master Offset

    // Basic Corrections
    val exposure: Float = 0f,      // -100 to +100 (ev shift)
    val contrast: Float = 0f,      // -100 to +100
    val saturation: Float = 100f,  // 0 to 200 (100 = 1.0x normal)
    val temperature: Float = 0f,   // -100 (Cool/Blue) to +100 (Warm/Amber)
    val tint: Float = 0f,          // -100 (Green) to +100 (Magenta)
    val highlights: Float = 0f,    // -100 to +100
    val shadows: Float = 0f,       // -100 to +100
    val vibrance: Float = 0f,      // -100 to +100
    val vignette: Float = 0f,      // 0 to 100
    val hueShift: Float = 0f,      // -180 to +180 degrees
    val sharpen: Float = 0f,       // 0 to 100

    // Applied LUT preset
    val activeLutId: String = "standard"
) {
    val isModified: Boolean
        get() = !lift.isDefault || !gamma.isDefault || !gain.isDefault || !offset.isDefault ||
                exposure != 0f || contrast != 0f || saturation != 100f ||
                temperature != 0f || tint != 0f || highlights != 0f ||
                shadows != 0f || vibrance != 0f || vignette != 0f ||
                hueShift != 0f || activeLutId != "standard"
}

data class LutPreset(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val settings: ColorGradeSettings
)

object LutLibrary {
    val presets = listOf(
        LutPreset(
            id = "standard",
            name = "Rec.709 Standard",
            description = "Natural true-to-life broadcast color profile",
            category = "Standard",
            settings = ColorGradeSettings(activeLutId = "standard")
        ),
        LutPreset(
            id = "teal_orange",
            name = "Teal & Orange",
            description = "Iconic Hollywood blockbuster look: cool shadows & warm skin tones",
            category = "Cinematic",
            settings = ColorGradeSettings(
                lift = ColorWheelSetting(hue = 195f, saturation = 0.35f, luminance = -0.05f),
                gain = ColorWheelSetting(hue = 38f, saturation = 0.40f, luminance = 0.08f),
                contrast = 18f,
                saturation = 115f,
                temperature = 10f,
                shadows = -12f,
                highlights = 8f,
                activeLutId = "teal_orange"
            )
        ),
        LutPreset(
            id = "cyberpunk",
            name = "Cyber Neon",
            description = "High-voltage electric cyan shadows with hot neon magenta highlights",
            category = "Stylized",
            settings = ColorGradeSettings(
                lift = ColorWheelSetting(hue = 180f, saturation = 0.45f, luminance = -0.1f),
                gain = ColorWheelSetting(hue = 320f, saturation = 0.45f, luminance = 0.12f),
                gamma = ColorWheelSetting(hue = 260f, saturation = 0.2f, luminance = 0f),
                contrast = 28f,
                saturation = 135f,
                temperature = -20f,
                tint = 25f,
                vignette = 30f,
                activeLutId = "cyberpunk"
            )
        ),
        LutPreset(
            id = "vintage_35mm",
            name = "Kodak 35mm",
            description = "Soft warm nostalgic highlights, lifted blacks, and gentle greens",
            category = "Vintage",
            settings = ColorGradeSettings(
                lift = ColorWheelSetting(hue = 45f, saturation = 0.15f, luminance = 0.12f),
                gamma = ColorWheelSetting(hue = 30f, saturation = 0.15f, luminance = 0.02f),
                gain = ColorWheelSetting(hue = 50f, saturation = 0.20f, luminance = -0.05f),
                contrast = -10f,
                saturation = 90f,
                temperature = 18f,
                tint = -5f,
                vignette = 25f,
                activeLutId = "vintage_35mm"
            )
        ),
        LutPreset(
            id = "noir_contrast",
            name = "Classic Noir",
            description = "Dramatic monochromatic black & white with deep velvety shadows",
            category = "Black & White",
            settings = ColorGradeSettings(
                contrast = 45f,
                saturation = 0f,
                exposure = 5f,
                highlights = 20f,
                shadows = -30f,
                vignette = 45f,
                activeLutId = "noir_contrast"
            )
        ),
        LutPreset(
            id = "golden_hour",
            name = "Golden Glow",
            description = "Lush sunset amber warmth with dreamy soft contrast",
            category = "Warm",
            settings = ColorGradeSettings(
                lift = ColorWheelSetting(hue = 28f, saturation = 0.20f, luminance = -0.02f),
                gain = ColorWheelSetting(hue = 42f, saturation = 0.35f, luminance = 0.10f),
                temperature = 35f,
                saturation = 110f,
                contrast = 8f,
                highlights = 15f,
                activeLutId = "golden_hour"
            )
        ),
        LutPreset(
            id = "bleach_bypass",
            name = "Bleach Bypass",
            description = "Silver retention film process: high contrast, desaturated grit",
            category = "Cinematic",
            settings = ColorGradeSettings(
                contrast = 40f,
                saturation = 60f,
                highlights = 25f,
                shadows = -25f,
                temperature = -10f,
                activeLutId = "bleach_bypass"
            )
        ),
        LutPreset(
            id = "forest_emerald",
            name = "Nordic Pine",
            description = "Deep moody emeralds, muted reds, atmospheric cool tones",
            category = "Moody",
            settings = ColorGradeSettings(
                lift = ColorWheelSetting(hue = 150f, saturation = 0.25f, luminance = -0.08f),
                gain = ColorWheelSetting(hue = 80f, saturation = 0.15f, luminance = 0.05f),
                temperature = -15f,
                tint = -15f,
                saturation = 95f,
                contrast = 15f,
                activeLutId = "forest_emerald"
            )
        )
    )
}
