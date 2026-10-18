package com.example.model

import java.util.UUID

enum class TrackType(val label: String, val iconName: String) {
    VIDEO_MAIN("V1 (Primary)", "movie"),
    VIDEO_OVERLAY("V2 (Overlay / B-Roll)", "layers"),
    AUDIO_MUSIC("A1 (Music)", "music_note"),
    AUDIO_SFX("A2 (Voice / SFX)", "mic"),
    TEXT_TITLE("T1 (Titles & Lower 3rds)", "title"),
    ADJUSTMENT_COLOR("FX (Adjustment Layer)", "auto_fix_high")
}

enum class ClipType {
    VIDEO,
    IMAGE,
    AUDIO,
    TEXT,
    COLOR_MATTE
}

enum class TransitionType(val label: String) {
    NONE("Cut"),
    DISSOLVE("Cross Dissolve"),
    FADE_BLACK("Dip to Black"),
    SLIDE_LEFT("Slide Left"),
    SLIDE_RIGHT("Slide Right"),
    ZOOM_IN("Zoom In"),
    GLITCH("Glitch Transition")
}

enum class AspectRatioOption(val label: String, val ratio: Float, val widthRatio: Int, val heightRatio: Int) {
    RATIO_16_9("16:9 Landscape (YouTube)", 16f / 9f, 16, 9),
    RATIO_9_16("9:16 Vertical (Shorts / Reels)", 9f / 16f, 9, 16),
    RATIO_1_1("1:1 Square (Feed)", 1f, 1, 1),
    RATIO_4_5("4:5 Portrait (Social)", 4f / 5f, 4, 5),
    RATIO_21_9("21:9 Cinematic (Anamorphic)", 21f / 9f, 21, 9)
}

data class Clip(
    val id: String = UUID.randomUUID().toString(),
    val trackId: String,
    val title: String,
    val clipType: ClipType,
    val mediaUri: String? = null,
    val drawableRes: Int? = null,
    val startTimeMs: Long,
    val durationMs: Long,
    val sourceOffsetMs: Long = 0L,
    val originalDurationMs: Long = durationMs,

    // Audio & Speed
    val volume: Float = 1.0f,
    val speed: Float = 1.0f,
    val isMuted: Boolean = false,

    // Spatial Transformations
    val scale: Float = 1.0f,
    val positionX: Float = 0f,
    val positionY: Float = 0f,
    val rotation: Float = 0f,
    val flipH: Boolean = false,
    val flipV: Boolean = false,

    // Text attributes (if clipType == TEXT)
    val textContent: String = "",
    val textColor: Long = 0xFFFFFFFF,
    val textFontSize: Int = 28,
    val textBackgroundColor: Long = 0x88000000,

    // Transitions
    val transitionIn: TransitionType = TransitionType.NONE,
    val transitionDurationMs: Long = 400L,

    // Per-clip color grade override (if null, uses project global or default)
    val colorGrade: ColorGradeSettings? = null
) {
    val endTimeMs: Long
        get() = startTimeMs + durationMs
}

data class Track(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val type: TrackType,
    val isMuted: Boolean = false,
    val isLocked: Boolean = false,
    val isSolo: Boolean = false,
    val isVisible: Boolean = true,
    val clips: List<Clip> = emptyList()
)

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Cinematic Edit",
    val aspectRatio: AspectRatioOption = AspectRatioOption.RATIO_16_9,
    val fps: Int = 30,
    val durationMs: Long = 15000L,
    val tracks: List<Track> = emptyList(),
    val globalColorGrade: ColorGradeSettings = ColorGradeSettings()
)
