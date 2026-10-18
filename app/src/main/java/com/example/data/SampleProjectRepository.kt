package com.example.data

import com.example.R
import com.example.model.*
import java.util.UUID

object SampleProjectRepository {

    val sampleMediaClips = listOf(
        SampleMedia(
            id = "media_alpine",
            title = "Alpine Dawn 4K",
            drawableRes = R.drawable.sample_cinematic_landscape_1792326183643,
            durationMs = 6000L,
            type = ClipType.VIDEO,
            category = "Landscape"
        ),
        SampleMedia(
            id = "media_tokyo",
            title = "Tokyo Rain Reflections",
            drawableRes = R.drawable.sample_cyberpunk_city_1792326196049,
            durationMs = 6500L,
            type = ClipType.VIDEO,
            category = "Urban"
        ),
        SampleMedia(
            id = "media_portrait",
            title = "Golden Coast Portrait",
            drawableRes = R.drawable.sample_portrait_sunset_1792326211234,
            durationMs = 5500L,
            type = ClipType.VIDEO,
            category = "Portrait"
        )
    )

    val sampleAudioClips = listOf(
        SampleAudio(
            id = "audio_synth",
            title = "Midnight Synthwave",
            artist = "Neon Dreamer",
            durationMs = 15000L,
            genre = "Electronic",
            bpm = 120
        ),
        SampleAudio(
            id = "audio_acoustic",
            title = "Golden Memories",
            artist = "Acoustic Sun",
            durationMs = 12000L,
            genre = "Cinematic Acoustic",
            bpm = 95
        ),
        SampleAudio(
            id = "audio_impact",
            title = "Cinematic Sub Impact",
            artist = "Trailer SFX",
            durationMs = 2500L,
            genre = "Sound FX",
            bpm = 0
        ),
        SampleAudio(
            id = "audio_whoosh",
            title = "Whip Pan Whoosh",
            artist = "Transition SFX",
            durationMs = 1200L,
            genre = "Sound FX",
            bpm = 0
        ),
        SampleAudio(
            id = "audio_waves",
            title = "Ambient Ocean Waves",
            artist = "Field Recording",
            durationMs = 8000L,
            genre = "Foley & Ambience",
            bpm = 0
        )
    )

    fun createInitialProject(): Project {
        val v1TrackId = "track_v1"
        val v2TrackId = "track_v2"
        val a1TrackId = "track_a1"
        val a2TrackId = "track_a2"
        val t1TrackId = "track_t1"
        val fxTrackId = "track_fx"

        val v1Clips = listOf(
            Clip(
                id = "clip_v1_1",
                trackId = v1TrackId,
                title = "Alpine Dawn 4K",
                clipType = ClipType.VIDEO,
                drawableRes = R.drawable.sample_cinematic_landscape_1792326183643,
                startTimeMs = 0L,
                durationMs = 5000L,
                originalDurationMs = 6000L,
                transitionIn = TransitionType.DISSOLVE,
                transitionDurationMs = 500L
            ),
            Clip(
                id = "clip_v1_2",
                trackId = v1TrackId,
                title = "Tokyo Rain Reflections",
                clipType = ClipType.VIDEO,
                drawableRes = R.drawable.sample_cyberpunk_city_1792326196049,
                startTimeMs = 5000L,
                durationMs = 5500L,
                originalDurationMs = 6500L,
                transitionIn = TransitionType.SLIDE_LEFT,
                transitionDurationMs = 400L
            ),
            Clip(
                id = "clip_v1_3",
                trackId = v1TrackId,
                title = "Golden Coast Portrait",
                clipType = ClipType.VIDEO,
                drawableRes = R.drawable.sample_portrait_sunset_1792326211234,
                startTimeMs = 10500L,
                durationMs = 4500L,
                originalDurationMs = 5500L,
                transitionIn = TransitionType.ZOOM_IN,
                transitionDurationMs = 450L
            )
        )

        val v2Clips = listOf(
            Clip(
                id = "clip_v2_1",
                trackId = v2TrackId,
                title = "Neon Overlay PiP",
                clipType = ClipType.VIDEO,
                drawableRes = R.drawable.sample_cyberpunk_city_1792326196049,
                startTimeMs = 2000L,
                durationMs = 3500L,
                scale = 0.42f,
                positionX = 0.48f,
                positionY = -0.42f,
                transitionIn = TransitionType.DISSOLVE
            )
        )

        val a1Clips = listOf(
            Clip(
                id = "clip_a1_1",
                trackId = a1TrackId,
                title = "Midnight Synthwave",
                clipType = ClipType.AUDIO,
                startTimeMs = 0L,
                durationMs = 15000L,
                volume = 0.85f
            )
        )

        val a2Clips = listOf(
            Clip(
                id = "clip_a2_1",
                trackId = a2TrackId,
                title = "Sub Impact Boom",
                clipType = ClipType.AUDIO,
                startTimeMs = 0L,
                durationMs = 2500L,
                volume = 1.0f
            ),
            Clip(
                id = "clip_a2_2",
                trackId = a2TrackId,
                title = "Whip Pan Whoosh",
                clipType = ClipType.AUDIO,
                startTimeMs = 4800L,
                durationMs = 1200L,
                volume = 0.9f
            ),
            Clip(
                id = "clip_a2_3",
                trackId = a2TrackId,
                title = "Ocean Ambience",
                clipType = ClipType.AUDIO,
                startTimeMs = 10500L,
                durationMs = 4500L,
                volume = 0.7f
            )
        )

        val t1Clips = listOf(
            Clip(
                id = "clip_t1_1",
                trackId = t1TrackId,
                title = "Title: LUMACUT",
                clipType = ClipType.TEXT,
                startTimeMs = 300L,
                durationMs = 3200L,
                textContent = "PRISMCUT CINEMA",
                textColor = 0xFF00E5FF,
                textFontSize = 32,
                transitionIn = TransitionType.DISSOLVE
            ),
            Clip(
                id = "clip_t1_2",
                trackId = t1TrackId,
                title = "Lower 3rd: Shibuya",
                clipType = ClipType.TEXT,
                startTimeMs = 5200L,
                durationMs = 3200L,
                textContent = "SHIBUYA CROSSING • 02:45 AM",
                textColor = 0xFFFFFFFF,
                textFontSize = 20,
                positionY = 0.7f,
                transitionIn = TransitionType.SLIDE_LEFT
            ),
            Clip(
                id = "clip_t1_3",
                trackId = t1TrackId,
                title = "Title: Golden Hour",
                clipType = ClipType.TEXT,
                startTimeMs = 10800L,
                durationMs = 3500L,
                textContent = "PACIFIC GOLDEN HOUR",
                textColor = 0xFFFFB300,
                textFontSize = 26,
                positionY = -0.6f,
                transitionIn = TransitionType.ZOOM_IN
            )
        )

        val fxClips = listOf(
            Clip(
                id = "clip_fx_1",
                trackId = fxTrackId,
                title = "Teal & Orange Grade",
                clipType = ClipType.COLOR_MATTE,
                startTimeMs = 0L,
                durationMs = 15000L,
                colorGrade = LutLibrary.presets[1].settings // Teal & Orange preset
            )
        )

        val tracks = listOf(
            Track(id = v1TrackId, name = "V1 Primary", type = TrackType.VIDEO_MAIN, clips = v1Clips),
            Track(id = v2TrackId, name = "V2 Overlay / PiP", type = TrackType.VIDEO_OVERLAY, clips = v2Clips),
            Track(id = a1TrackId, name = "A1 Music", type = TrackType.AUDIO_MUSIC, clips = a1Clips),
            Track(id = a2TrackId, name = "A2 Voice & SFX", type = TrackType.AUDIO_SFX, clips = a2Clips),
            Track(id = t1TrackId, name = "T1 Titles", type = TrackType.TEXT_TITLE, clips = t1Clips),
            Track(id = fxTrackId, name = "FX Adjustment", type = TrackType.ADJUSTMENT_COLOR, clips = fxClips)
        )

        return Project(
            id = "proj_cinematic_showreel",
            title = "Cinematic Tokyo & Alps Showreel",
            aspectRatio = AspectRatioOption.RATIO_16_9,
            fps = 30,
            durationMs = 15000L,
            tracks = tracks,
            globalColorGrade = LutLibrary.presets[1].settings // Default Hollywood Teal & Orange
        )
    }
}

data class SampleMedia(
    val id: String,
    val title: String,
    val drawableRes: Int,
    val durationMs: Long,
    val type: ClipType,
    val category: String
)

data class SampleAudio(
    val id: String,
    val title: String,
    val artist: String,
    val durationMs: Long,
    val genre: String,
    val bpm: Int
)
