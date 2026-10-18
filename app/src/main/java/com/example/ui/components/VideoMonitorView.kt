package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.*
import com.example.ui.theme.*
import com.example.util.ColorGradingEngine
import com.example.viewmodel.VideoEditorViewModel

@Composable
fun VideoMonitorView(
    project: Project,
    currentPositionMs: Long,
    colorGrade: ColorGradeSettings,
    isSplitCompareActive: Boolean,
    splitComparePosition: Float,
    onSplitComparePositionChange: (Float) -> Unit,
    showSafeAreas: Boolean,
    modifier: Modifier = Modifier
) {
    // Determine active clips at current playhead
    val activeV1Clip = project.tracks
        .find { it.type == TrackType.VIDEO_MAIN && it.isVisible }
        ?.clips
        ?.find { currentPositionMs >= it.startTimeMs && currentPositionMs < it.endTimeMs }

    val activeV2Clip = project.tracks
        .find { it.type == TrackType.VIDEO_OVERLAY && it.isVisible }
        ?.clips
        ?.find { currentPositionMs >= it.startTimeMs && currentPositionMs < it.endTimeMs }

    val activeTextClip = project.tracks
        .find { it.type == TrackType.TEXT_TITLE && it.isVisible }
        ?.clips
        ?.find { currentPositionMs >= it.startTimeMs && currentPositionMs < it.endTimeMs }

    // Active color grade matrix (hardware accelerated via Compose ColorFilter)
    val colorMatrix = remember(colorGrade) {
        ColorGradingEngine.createColorMatrix(colorGrade)
    }
    val gradedFilter = remember(colorMatrix) { ColorFilter.colorMatrix(colorMatrix) }

    BoxWithConstraints(
        modifier = modifier
            .background(StudioBackground)
            .padding(8.dp)
            .testTag("video_monitor_container"),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = maxWidth
        val containerHeight = maxHeight

        val targetRatio = project.aspectRatio.ratio
        val displayWidth: androidx.compose.ui.unit.Dp
        val displayHeight: androidx.compose.ui.unit.Dp

        if (containerWidth / containerHeight > targetRatio) {
            displayHeight = containerHeight
            displayWidth = containerHeight * targetRatio
        } else {
            displayWidth = containerWidth
            displayHeight = containerWidth / targetRatio
        }

        Box(
            modifier = Modifier
                .width(displayWidth)
                .height(displayHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
                .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
                .clipToBounds()
        ) {
            if (!isSplitCompareActive) {
                // Normal Preview with real-time Color Grading applied
                MonitorLayers(
                    v1Clip = activeV1Clip,
                    v2Clip = activeV2Clip,
                    textClip = activeTextClip,
                    colorFilter = gradedFilter
                )
            } else {
                // Interactive A/B Split Screen: Original on Left, Graded on Right
                Box(modifier = Modifier.fillMaxSize()) {
                    // Right side: Graded
                    MonitorLayers(
                        v1Clip = activeV1Clip,
                        v2Clip = activeV2Clip,
                        textClip = activeTextClip,
                        colorFilter = gradedFilter
                    )

                    // Left side: Original (unfiltered) clipped to split width
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(splitComparePosition)
                            .clipToBounds()
                    ) {
                        Box(modifier = Modifier.width(displayWidth).fillMaxHeight()) {
                            MonitorLayers(
                                v1Clip = activeV1Clip,
                                v2Clip = activeV2Clip,
                                textClip = activeTextClip,
                                colorFilter = null
                            )
                        }
                    }

                    // Draggable split divider line
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth()
                            .pointerInput(Unit) {
                                detectDragGestures { change, _ ->
                                    val newPos = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                                    onSplitComparePositionChange(newPos)
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val x = size.width * splitComparePosition
                            drawLine(
                                color = CyanAccent,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 2.dp.toPx()
                            )
                            drawCircle(
                                color = CyanAccent,
                                radius = 7.dp.toPx(),
                                center = Offset(x, size.height / 2f)
                            )
                            drawCircle(
                                color = StudioBackground,
                                radius = 3.dp.toPx(),
                                center = Offset(x, size.height / 2f)
                            )
                        }

                        // Badges: "ORIGINAL" vs "GRADED"
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.75f),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "RAW / RE.709",
                                color = TextSecondary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CyanAccent.copy(alpha = 0.9f),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            Text(
                                text = "GRADED",
                                color = StudioBackground,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Safe Margins Overlay (Action Safe 90% and Title Safe 80%)
            if (showSafeAreas) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Action Safe (90%)
                    val actionSafeW = w * 0.9f
                    val actionSafeH = h * 0.9f
                    drawRect(
                        color = Color.Yellow.copy(alpha = 0.5f),
                        topLeft = Offset((w - actionSafeW) / 2f, (h - actionSafeH) / 2f),
                        size = Size(actionSafeW, actionSafeH),
                        style = Stroke(width = 1.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                    )

                    // Title Safe (80%)
                    val titleSafeW = w * 0.8f
                    val titleSafeH = h * 0.8f
                    drawRect(
                        color = CyanAccent.copy(alpha = 0.6f),
                        topLeft = Offset((w - titleSafeW) / 2f, (h - titleSafeH) / 2f),
                        size = Size(titleSafeW, titleSafeH),
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Center Crosshair
                    val cx = w / 2f
                    val cy = h / 2f
                    val arm = 12.dp.toPx()
                    drawLine(Color.White.copy(alpha = 0.5f), Offset(cx - arm, cy), Offset(cx + arm, cy), 1.dp.toPx())
                    drawLine(Color.White.copy(alpha = 0.5f), Offset(cx, cy - arm), Offset(cx, cy + arm), 1.dp.toPx())
                }
            }

            // Timecode Overlay in bottom right corner
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
            ) {
                Text(
                    text = VideoEditorViewModel.formatTimecode(currentPositionMs),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Aspect Ratio badge in bottom left corner
            Surface(
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
            ) {
                Text(
                    text = "${project.aspectRatio.label.split(" ").first()} • ${project.fps} FPS",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun MonitorLayers(
    v1Clip: Clip?,
    v2Clip: Clip?,
    textClip: Clip?,
    colorFilter: ColorFilter?
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // V1 Primary Video Layer
        if (v1Clip != null) {
            if (v1Clip.drawableRes != null) {
                Image(
                    painter = painterResource(id = v1Clip.drawableRes),
                    contentDescription = v1Clip.title,
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = v1Clip.scale
                            scaleY = v1Clip.scale
                            translationX = v1Clip.positionX * size.width
                            translationY = v1Clip.positionY * size.height
                            rotationZ = v1Clip.rotation
                        }
                )
            } else if (!v1Clip.mediaUri.isNullOrEmpty()) {
                AsyncImage(
                    model = v1Clip.mediaUri,
                    contentDescription = v1Clip.title,
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter,
                    modifier = Modifier.fillMaxSize()
                )
            }
        } else {
            // Black video leader / empty track
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF08090C)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "NO SIGNAL / PLAYHEAD OUT OF RANGE",
                    color = TextTertiary,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // V2 Overlay / Picture-in-Picture Layer
        if (v2Clip != null && v2Clip.drawableRes != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = v2Clip.scale
                        scaleY = v2Clip.scale
                        translationX = v2Clip.positionX * size.width
                        translationY = v2Clip.positionY * size.height
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = v2Clip.drawableRes),
                    contentDescription = v2Clip.title,
                    contentScale = ContentScale.Crop,
                    colorFilter = colorFilter,
                    modifier = Modifier
                        .fillMaxSize(0.8f)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.5.dp, CyanAccent.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                )
            }
        }

        // Text & Title Overlay Layer
        if (textClip != null && textClip.textContent.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = textClip.positionX * size.width
                        translationY = textClip.positionY * size.height
                    },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    color = Color(textClip.textBackgroundColor),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = textClip.textContent,
                        color = Color(textClip.textColor),
                        fontSize = textClip.textFontSize.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
