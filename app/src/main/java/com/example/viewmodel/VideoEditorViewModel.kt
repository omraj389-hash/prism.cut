package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SampleProjectRepository
import com.example.model.*
import com.example.util.ColorGradingEngine
import com.example.util.HistogramData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class WorkspaceTab(val title: String) {
    EDIT("Edit / Cut"),
    COLOR("Color Grade"),
    AUDIO("Audio Mixer"),
    EXPORT("Export")
}

data class ExportConfig(
    val resolution: String = "1080p Full HD (1920x1080)",
    val fps: Int = 30,
    val bitrate: String = "High (16 Mbps)",
    val format: String = "MP4 (H.264 / AAC)"
)

data class VideoEditorUiState(
    val project: Project = SampleProjectRepository.createInitialProject(),
    val currentPositionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val selectedTrackId: String? = "track_v1",
    val selectedClipId: String? = "clip_v1_1",
    val activeWorkspace: WorkspaceTab = WorkspaceTab.EDIT,
    val timelineZoom: Float = 120f, // dp per second
    val isSplitCompareActive: Boolean = false,
    val splitComparePosition: Float = 0.5f,
    val showSafeAreas: Boolean = false,
    val isColorGradingClip: Boolean = false, // false = project global grade, true = selected clip grade
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val showExportDialog: Boolean = false,
    val exportCompleted: Boolean = false,
    val exportConfig: ExportConfig = ExportConfig(),
    val statusMessage: String? = null
)

class VideoEditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VideoEditorUiState())
    val uiState: StateFlow<VideoEditorUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null

    val activeColorGrade: StateFlow<ColorGradeSettings> = _uiState.map { state ->
        if (state.isColorGradingClip) {
            val clip = state.project.tracks
                .flatMap { it.clips }
                .find { it.id == state.selectedClipId }
            clip?.colorGrade ?: state.project.globalColorGrade
        } else {
            state.project.globalColorGrade
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ColorGradeSettings())

    val histogramData: StateFlow<HistogramData> = activeColorGrade.map { settings ->
        ColorGradingEngine.computeSimulatedHistogram(settings)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, ColorGradingEngine.computeSimulatedHistogram(ColorGradeSettings()))

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = true) }
        playbackJob = viewModelScope.launch {
            val stepIntervalMs = 33L // ~30 fps update
            while (isActive) {
                delay(stepIntervalMs)
                _uiState.update { current ->
                    val nextPos = current.currentPositionMs + stepIntervalMs
                    if (nextPos >= current.project.durationMs) {
                        current.copy(currentPositionMs = 0L, isPlaying = true) // Loop
                    } else {
                        current.copy(currentPositionMs = nextPos)
                    }
                }
            }
        }
    }

    fun pause() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun seekTo(positionMs: Long) {
        val bounded = positionMs.coerceIn(0L, _uiState.value.project.durationMs)
        _uiState.update { it.copy(currentPositionMs = bounded) }
    }

    fun stepFrame(forward: Boolean) {
        pause()
        val frameMs = (1000L / _uiState.value.project.fps).coerceAtLeast(16L)
        val delta = if (forward) frameMs else -frameMs
        seekTo(_uiState.value.currentPositionMs + delta)
    }

    fun jumpToStart() {
        seekTo(0L)
    }

    fun jumpToEnd() {
        seekTo(_uiState.value.project.durationMs)
    }

    fun setWorkspace(tab: WorkspaceTab) {
        _uiState.update { it.copy(activeWorkspace = tab) }
    }

    fun selectClip(trackId: String, clipId: String) {
        _uiState.update {
            it.copy(
                selectedTrackId = trackId,
                selectedClipId = clipId
            )
        }
    }

    fun clearSelection() {
        _uiState.update {
            it.copy(selectedClipId = null)
        }
    }

    fun setTimelineZoom(zoom: Float) {
        _uiState.update { it.copy(timelineZoom = zoom.coerceIn(40f, 400f)) }
    }

    fun toggleSplitCompare() {
        _uiState.update { it.copy(isSplitCompareActive = !it.isSplitCompareActive) }
    }

    fun setSplitComparePosition(pos: Float) {
        _uiState.update { it.copy(splitComparePosition = pos.coerceIn(0.05f, 0.95f)) }
    }

    fun toggleSafeAreas() {
        _uiState.update { it.copy(showSafeAreas = !it.showSafeAreas) }
    }

    fun setAspectRatio(ratio: AspectRatioOption) {
        _uiState.update {
            it.copy(
                project = it.project.copy(aspectRatio = ratio),
                statusMessage = "Canvas set to ${ratio.label}"
            )
        }
    }

    // ==========================================
    // Multi-Track Timeline Operations
    // ==========================================

    /**
     * Splits the active clip at the current playhead (Razor Blade tool)
     */
    fun splitClipAtPlayhead() {
        val state = _uiState.value
        val playhead = state.currentPositionMs
        val selectedId = state.selectedClipId

        var clipToSplit: Clip? = null
        var targetTrackId: String? = null

        if (selectedId != null) {
            for (track in state.project.tracks) {
                val found = track.clips.find { it.id == selectedId }
                if (found != null && playhead > found.startTimeMs && playhead < found.endTimeMs) {
                    clipToSplit = found
                    targetTrackId = track.id
                    break
                }
            }
        }

        // If no selected clip matches playhead, find the primary video clip at playhead
        if (clipToSplit == null) {
            for (track in state.project.tracks) {
                val found = track.clips.find { playhead > it.startTimeMs && playhead < it.endTimeMs }
                if (found != null) {
                    clipToSplit = found
                    targetTrackId = track.id
                    break
                }
            }
        }

        if (clipToSplit != null && targetTrackId != null) {
            val originalStart = clipToSplit.startTimeMs
            val originalDuration = clipToSplit.durationMs
            val firstPartDuration = playhead - originalStart
            val secondPartDuration = originalDuration - firstPartDuration

            val part1 = clipToSplit.copy(
                durationMs = firstPartDuration
            )
            val part2 = clipToSplit.copy(
                id = UUID.randomUUID().toString(),
                startTimeMs = playhead,
                durationMs = secondPartDuration,
                sourceOffsetMs = clipToSplit.sourceOffsetMs + firstPartDuration,
                transitionIn = TransitionType.NONE
            )

            val updatedTracks = state.project.tracks.map { track ->
                if (track.id == targetTrackId) {
                    val newClips = track.clips.flatMap { c ->
                        if (c.id == clipToSplit.id) listOf(part1, part2) else listOf(c)
                    }
                    track.copy(clips = newClips)
                } else {
                    track
                }
            }

            _uiState.update {
                it.copy(
                    project = it.project.copy(tracks = updatedTracks),
                    selectedClipId = part2.id,
                    statusMessage = "Clip split at ${formatTimecode(playhead)}"
                )
            }
        } else {
            _uiState.update { it.copy(statusMessage = "Place playhead inside a clip to split") }
        }
    }

    fun trimClip(clipId: String, newStartMs: Long, newDurationMs: Long) {
        val safeDuration = newDurationMs.coerceAtLeast(300L)
        val updatedTracks = _uiState.value.project.tracks.map { track ->
            val hasClip = track.clips.any { it.id == clipId }
            if (hasClip) {
                val updatedClips = track.clips.map { clip ->
                    if (clip.id == clipId) {
                        clip.copy(
                            startTimeMs = newStartMs.coerceAtLeast(0L),
                            durationMs = safeDuration
                        )
                    } else clip
                }
                track.copy(clips = updatedClips)
            } else track
        }
        _uiState.update { it.copy(project = it.project.copy(tracks = updatedTracks)) }
    }

    fun deleteSelectedClip() {
        val clipId = _uiState.value.selectedClipId ?: return
        val updatedTracks = _uiState.value.project.tracks.map { track ->
            track.copy(clips = track.clips.filterNot { it.id == clipId })
        }
        _uiState.update {
            it.copy(
                project = it.project.copy(tracks = updatedTracks),
                selectedClipId = null,
                statusMessage = "Clip deleted"
            )
        }
    }

    fun duplicateSelectedClip() {
        val clipId = _uiState.value.selectedClipId ?: return
        var duplicatedClip: Clip? = null
        var targetTrackId: String? = null

        for (track in _uiState.value.project.tracks) {
            val found = track.clips.find { it.id == clipId }
            if (found != null) {
                val newStart = found.endTimeMs + 100L
                duplicatedClip = found.copy(
                    id = UUID.randomUUID().toString(),
                    startTimeMs = newStart,
                    title = "${found.title} (Copy)"
                )
                targetTrackId = track.id
                break
            }
        }

        if (duplicatedClip != null && targetTrackId != null) {
            val updatedTracks = _uiState.value.project.tracks.map { track ->
                if (track.id == targetTrackId) {
                    track.copy(clips = track.clips + duplicatedClip)
                } else track
            }
            _uiState.update {
                it.copy(
                    project = it.project.copy(tracks = updatedTracks),
                    selectedClipId = duplicatedClip.id,
                    statusMessage = "Clip duplicated"
                )
            }
        }
    }

    fun updateClipSpeed(clipId: String, speed: Float) {
        val updatedTracks = _uiState.value.project.tracks.map { track ->
            track.copy(clips = track.clips.map { clip ->
                if (clip.id == clipId) {
                    val oldSpeed = clip.speed
                    val newDuration = (clip.durationMs * (oldSpeed / speed)).toLong().coerceAtLeast(200L)
                    clip.copy(speed = speed, durationMs = newDuration)
                } else clip
            })
        }
        _uiState.update {
            it.copy(
                project = it.project.copy(tracks = updatedTracks),
                statusMessage = "Speed changed to ${speed}x"
            )
        }
    }

    fun updateClipVolume(clipId: String, volume: Float) {
        val updatedTracks = _uiState.value.project.tracks.map { track ->
            track.copy(clips = track.clips.map { clip ->
                if (clip.id == clipId) clip.copy(volume = volume) else clip
            })
        }
        _uiState.update { it.copy(project = it.project.copy(tracks = updatedTracks)) }
    }

    fun updateClipTransform(clipId: String, scale: Float, posX: Float, posY: Float, rotation: Float) {
        val updatedTracks = _uiState.value.project.tracks.map { track ->
            track.copy(clips = track.clips.map { clip ->
                if (clip.id == clipId) {
                    clip.copy(scale = scale, positionX = posX, positionY = posY, rotation = rotation)
                } else clip
            })
        }
        _uiState.update { it.copy(project = it.project.copy(tracks = updatedTracks)) }
    }

    fun updateClipTransition(clipId: String, transition: TransitionType) {
        val updatedTracks = _uiState.value.project.tracks.map { track ->
            track.copy(clips = track.clips.map { clip ->
                if (clip.id == clipId) clip.copy(transitionIn = transition) else clip
            })
        }
        _uiState.update {
            it.copy(
                project = it.project.copy(tracks = updatedTracks),
                statusMessage = "Transition set to ${transition.label}"
            )
        }
    }

    fun updateClipText(clipId: String, text: String, color: Long, fontSize: Int) {
        val updatedTracks = _uiState.value.project.tracks.map { track ->
            track.copy(clips = track.clips.map { clip ->
                if (clip.id == clipId) {
                    clip.copy(textContent = text, textColor = color, textFontSize = fontSize)
                } else clip
            })
        }
        _uiState.update { it.copy(project = it.project.copy(tracks = updatedTracks)) }
    }

    fun toggleTrackMute(trackId: String) {
        val updatedTracks = _uiState.value.project.tracks.map { track ->
            if (track.id == trackId) track.copy(isMuted = !track.isMuted) else track
        }
        _uiState.update { it.copy(project = it.project.copy(tracks = updatedTracks)) }
    }

    fun toggleTrackLock(trackId: String) {
        val updatedTracks = _uiState.value.project.tracks.map { track ->
            if (track.id == trackId) track.copy(isLocked = !track.isLocked) else track
        }
        _uiState.update { it.copy(project = it.project.copy(tracks = updatedTracks)) }
    }

    fun toggleTrackVisibility(trackId: String) {
        val updatedTracks = _uiState.value.project.tracks.map { track ->
            if (track.id == trackId) track.copy(isVisible = !track.isVisible) else track
        }
        _uiState.update { it.copy(project = it.project.copy(tracks = updatedTracks)) }
    }

    fun addMediaClip(mediaUri: String?, drawableRes: Int?, title: String, clipType: ClipType, durationMs: Long = 4000L) {
        val playhead = _uiState.value.currentPositionMs
        val targetTrackType = when (clipType) {
            ClipType.AUDIO -> TrackType.AUDIO_MUSIC
            ClipType.TEXT -> TrackType.TEXT_TITLE
            ClipType.COLOR_MATTE -> TrackType.ADJUSTMENT_COLOR
            else -> TrackType.VIDEO_MAIN
        }

        var targetTrack = _uiState.value.project.tracks.find { it.type == targetTrackType }
        val newClip = Clip(
            id = UUID.randomUUID().toString(),
            trackId = targetTrack?.id ?: "track_v1",
            title = title,
            clipType = clipType,
            mediaUri = mediaUri,
            drawableRes = drawableRes,
            startTimeMs = playhead,
            durationMs = durationMs,
            originalDurationMs = durationMs
        )

        val updatedTracks = _uiState.value.project.tracks.map { track ->
            if (track.id == newClip.trackId) {
                track.copy(clips = track.clips + newClip)
            } else track
        }

        _uiState.update {
            it.copy(
                project = it.project.copy(tracks = updatedTracks),
                selectedClipId = newClip.id,
                statusMessage = "Added '$title' to timeline"
            )
        }
    }

    // ==========================================
    // Real-Time Color Grading Operations
    // ==========================================

    fun updateColorGrade(transform: (ColorGradeSettings) -> ColorGradeSettings) {
        val currentSettings = activeColorGrade.value
        val newSettings = transform(currentSettings)

        if (_uiState.value.isColorGradingClip && _uiState.value.selectedClipId != null) {
            val clipId = _uiState.value.selectedClipId!!
            val updatedTracks = _uiState.value.project.tracks.map { track ->
                track.copy(clips = track.clips.map { clip ->
                    if (clip.id == clipId) clip.copy(colorGrade = newSettings) else clip
                })
            }
            _uiState.update { it.copy(project = it.project.copy(tracks = updatedTracks)) }
        } else {
            _uiState.update { it.copy(project = it.project.copy(globalColorGrade = newSettings)) }
        }
    }

    fun updateLiftWheel(hue: Float, saturation: Float, luminance: Float) {
        updateColorGrade { it.copy(lift = ColorWheelSetting(hue, saturation.coerceIn(0f, 1f), luminance.coerceIn(-1f, 1f))) }
    }

    fun updateGammaWheel(hue: Float, saturation: Float, luminance: Float) {
        updateColorGrade { it.copy(gamma = ColorWheelSetting(hue, saturation.coerceIn(0f, 1f), luminance.coerceIn(-1f, 1f))) }
    }

    fun updateGainWheel(hue: Float, saturation: Float, luminance: Float) {
        updateColorGrade { it.copy(gain = ColorWheelSetting(hue, saturation.coerceIn(0f, 1f), luminance.coerceIn(-1f, 1f))) }
    }

    fun updateOffsetWheel(hue: Float, saturation: Float, luminance: Float) {
        updateColorGrade { it.copy(offset = ColorWheelSetting(hue, saturation.coerceIn(0f, 1f), luminance.coerceIn(-1f, 1f))) }
    }

    fun updateExposure(v: Float) = updateColorGrade { it.copy(exposure = v) }
    fun updateContrast(v: Float) = updateColorGrade { it.copy(contrast = v) }
    fun updateSaturation(v: Float) = updateColorGrade { it.copy(saturation = v) }
    fun updateTemperature(v: Float) = updateColorGrade { it.copy(temperature = v) }
    fun updateTint(v: Float) = updateColorGrade { it.copy(tint = v) }
    fun updateHighlights(v: Float) = updateColorGrade { it.copy(highlights = v) }
    fun updateShadows(v: Float) = updateColorGrade { it.copy(shadows = v) }
    fun updateVibrance(v: Float) = updateColorGrade { it.copy(vibrance = v) }
    fun updateVignette(v: Float) = updateColorGrade { it.copy(vignette = v) }
    fun updateHueShift(v: Float) = updateColorGrade { it.copy(hueShift = v) }

    fun applyLut(preset: LutPreset) {
        updateColorGrade { preset.settings.copy(activeLutId = preset.id) }
        _uiState.update { it.copy(statusMessage = "Applied LUT: ${preset.name}") }
    }

    fun resetColorGrade() {
        updateColorGrade { ColorGradeSettings() }
        _uiState.update { it.copy(statusMessage = "Color grade reset to neutral") }
    }

    fun toggleGradeScopeTarget() {
        _uiState.update {
            val next = !it.isColorGradingClip
            it.copy(
                isColorGradingClip = next,
                statusMessage = if (next) "Grading selected clip only" else "Grading master project"
            )
        }
    }

    // ==========================================
    // Export Operations
    // ==========================================

    fun openExportDialog() {
        pause()
        _uiState.update { it.copy(showExportDialog = true, exportCompleted = false, exportProgress = 0f) }
    }

    fun closeExportDialog() {
        _uiState.update { it.copy(showExportDialog = false, isExporting = false) }
    }

    fun updateExportConfig(config: ExportConfig) {
        _uiState.update { it.copy(exportConfig = config) }
    }

    fun startExport() {
        _uiState.update { it.copy(isExporting = true, exportProgress = 0.05f) }
        viewModelScope.launch {
            for (step in 1..20) {
                delay(120)
                val progress = step / 20f
                _uiState.update { it.copy(exportProgress = progress) }
            }
            _uiState.update {
                it.copy(
                    isExporting = false,
                    exportCompleted = true,
                    statusMessage = "Render complete! Exported in ${it.exportConfig.resolution}"
                )
            }
        }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    companion object {
        fun formatTimecode(ms: Long): String {
            val totalSeconds = ms / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val frames = ((ms % 1000) / 33).coerceIn(0, 29)
            return "%02d:%02d:%02d".format(minutes, seconds, frames)
        }
    }
}
