package com.example.ui.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun MultiTrackTimelineView(
    project: Project,
    currentPositionMs: Long,
    selectedTrackId: String?,
    selectedClipId: String?,
    zoomDpPerSec: Float,
    onClipSelect: (trackId: String, clipId: String) -> Unit,
    onClipTrim: (clipId: String, newStartMs: Long, newDurationMs: Long) -> Unit,
    onSeek: (Long) -> Unit,
    onToggleMute: (trackId: String) -> Unit,
    onToggleLock: (trackId: String) -> Unit,
    onToggleVisibility: (trackId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // Total timeline width in dp based on project duration + trailing buffer
    val totalSeconds = project.durationMs / 1000f
    val timelineWidthDp = (totalSeconds * zoomDpPerSec).dp + 200.dp

    Row(modifier = modifier.fillMaxWidth()) {
        // Left Column: Track Headers
        Column(
            modifier = Modifier
                .width(100.dp)
                .background(StudioSurface)
                .border(1.dp, StudioBorder)
        ) {
            // Header blank area matching timeline ruler height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .background(StudioSurfaceVariant),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "TRACKS",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // Track header rows
            project.tracks.forEach { track ->
                TrackHeaderRow(
                    track = track,
                    isSelected = track.id == selectedTrackId,
                    onToggleMute = { onToggleMute(track.id) },
                    onToggleLock = { onToggleLock(track.id) },
                    onToggleVisibility = { onToggleVisibility(track.id) }
                )
            }
        }

        // Right Area: Scrollable Tracks & Ruler & Playhead
        Box(
            modifier = Modifier
                .weight(1f)
                .background(StudioBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
            ) {
                // Time Ruler
                TimelineRuler(
                    totalDurationMs = project.durationMs,
                    currentPositionMs = currentPositionMs,
                    zoomDpPerSec = zoomDpPerSec,
                    scrollOffsetPx = 0f,
                    onSeek = onSeek,
                    modifier = Modifier.width(timelineWidthDp)
                )

                // Track Lanes
                Box(modifier = Modifier.width(timelineWidthDp)) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        project.tracks.forEach { track ->
                            TrackLaneRow(
                                track = track,
                                selectedClipId = selectedClipId,
                                zoomDpPerSec = zoomDpPerSec,
                                onClipSelect = { clipId -> onClipSelect(track.id, clipId) },
                                onClipTrim = onClipTrim
                            )
                        }
                    }

                    // Playhead Line traversing through all tracks
                    val playheadOffsetDp = ((currentPositionMs / 1000f) * zoomDpPerSec).dp
                    Box(
                        modifier = Modifier
                            .offset(x = playheadOffsetDp)
                            .width(2.dp)
                            .fillMaxHeight()
                            .background(CyanAccent)
                    )
                }
            }
        }
    }
}

@Composable
private fun TrackHeaderRow(
    track: Track,
    isSelected: Boolean,
    onToggleMute: () -> Unit,
    onToggleLock: () -> Unit,
    onToggleVisibility: () -> Unit
) {
    val trackColor = when (track.type) {
        TrackType.VIDEO_MAIN -> VideoTrackColor
        TrackType.VIDEO_OVERLAY -> OverlayTrackColor
        TrackType.AUDIO_MUSIC -> AudioTrackColor
        TrackType.AUDIO_SFX -> AudioTrackColor
        TrackType.TEXT_TITLE -> TextTrackColor
        TrackType.ADJUSTMENT_COLOR -> EffectTrackColor
    }

    Surface(
        color = if (isSelected) StudioSurfaceElevated else StudioSurface,
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp)
        ) {
            // Track Color Pill
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .background(trackColor, RoundedCornerShape(2.dp))
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.name,
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    // Mute button
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = if (track.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "Mute track",
                            tint = if (track.isMuted) AmberAccent else TextTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // Lock button
                    IconButton(
                        onClick = onToggleLock,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = if (track.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = "Lock track",
                            tint = if (track.isLocked) AmberAccent else TextTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    // Visibility button
                    IconButton(
                        onClick = onToggleVisibility,
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = if (track.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Visibility track",
                            tint = if (track.isVisible) TextSecondary else TextTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackLaneRow(
    track: Track,
    selectedClipId: String?,
    zoomDpPerSec: Float,
    onClipSelect: (String) -> Unit,
    onClipTrim: (clipId: String, newStartMs: Long, newDurationMs: Long) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(if (track.isLocked) StudioBackground.copy(alpha = 0.5f) else StudioSurfaceVariant.copy(alpha = 0.4f))
            .border(0.5.dp, StudioBorder)
    ) {
        track.clips.forEach { clip ->
            ClipItem(
                clip = clip,
                trackType = track.type,
                isSelected = clip.id == selectedClipId,
                zoomDpPerSec = zoomDpPerSec,
                onClick = { onClipSelect(clip.id) },
                onTrim = { newStart, newDur -> onClipTrim(clip.id, newStart, newDur) }
            )
        }
    }
}

@Composable
private fun ClipItem(
    clip: Clip,
    trackType: TrackType,
    isSelected: Boolean,
    zoomDpPerSec: Float,
    onClick: () -> Unit,
    onTrim: (Long, Long) -> Unit
) {
    val startSec = clip.startTimeMs / 1000f
    val durationSec = clip.durationMs / 1000f

    val startOffsetDp = (startSec * zoomDpPerSec).dp
    val clipWidthDp = (durationSec * zoomDpPerSec).dp.coerceAtLeast(36.dp)

    val (bgColor, borderColor) = when (trackType) {
        TrackType.VIDEO_MAIN -> Pair(VideoTrackColor, VideoTrackBorder)
        TrackType.VIDEO_OVERLAY -> Pair(OverlayTrackColor, OverlayTrackBorder)
        TrackType.AUDIO_MUSIC, TrackType.AUDIO_SFX -> Pair(AudioTrackColor, AudioTrackBorder)
        TrackType.TEXT_TITLE -> Pair(TextTrackColor, TextTrackBorder)
        TrackType.ADJUSTMENT_COLOR -> Pair(EffectTrackColor, EffectTrackBorder)
    }

    Box(
        modifier = Modifier
            .offset(x = startOffsetDp)
            .width(clipWidthDp)
            .fillMaxHeight()
            .padding(vertical = 4.dp, horizontal = 1.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor.copy(alpha = 0.85f))
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CyanAccent else borderColor,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Trim Handle (active when selected)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .fillMaxHeight()
                        .background(CyanAccent)
                        .pointerInput(clip.id) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaSec = (dragAmount.x / density) / zoomDpPerSec
                                val deltaMs = (deltaSec * 1000f).toLong()
                                val newStart = (clip.startTimeMs + deltaMs).coerceAtLeast(0L)
                                val newDur = (clip.durationMs - deltaMs).coerceAtLeast(300L)
                                onTrim(newStart, newDur)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(2.dp).height(14.dp).background(Color.Black))
                }
            }

            // Clip Body Preview
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Thumbnail or Waveform Icon
                if (clip.drawableRes != null) {
                    Image(
                        painter = painterResource(id = clip.drawableRes),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                } else if (clip.clipType == ClipType.AUDIO) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
                        val barCount = (size.width / 4.dp.toPx()).toInt().coerceIn(4, 80)
                        val barWidth = 2.dp.toPx()
                        for (i in 0 until barCount) {
                            val h = size.height * (0.3f + 0.6f * kotlin.math.sin(i * 0.45f).coerceAtLeast(0f))
                            val x = i * (barWidth + 2.dp.toPx())
                            drawLine(
                                color = AudioTrackBorder.copy(alpha = 0.8f),
                                start = Offset(x, (size.height - h) / 2f),
                                end = Offset(x, (size.height + h) / 2f),
                                strokeWidth = barWidth
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = clip.title,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${clip.durationMs / 1000f}s",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 9.sp
                        )
                        if (clip.speed != 1.0f) {
                            Text(
                                text = "${clip.speed}x",
                                color = AmberAccent,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (clip.transitionIn != TransitionType.NONE) {
                            Text(
                                text = "⮑ ${clip.transitionIn.label.take(6)}",
                                color = CyanAccent,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // Right Trim Handle (active when selected)
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(10.dp)
                        .fillMaxHeight()
                        .background(CyanAccent)
                        .pointerInput(clip.id) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                val deltaSec = (dragAmount.x / density) / zoomDpPerSec
                                val deltaMs = (deltaSec * 1000f).toLong()
                                val newDur = (clip.durationMs + deltaMs).coerceAtLeast(300L)
                                onTrim(clip.startTimeMs, newDur)
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.width(2.dp).height(14.dp).background(Color.Black))
                }
            }
        }
    }
}
