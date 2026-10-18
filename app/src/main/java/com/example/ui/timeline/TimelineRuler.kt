package com.example.ui.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioSurfaceVariant

@Composable
fun TimelineRuler(
    totalDurationMs: Long,
    currentPositionMs: Long,
    zoomDpPerSec: Float,
    scrollOffsetPx: Float,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalSeconds = (totalDurationMs / 1000f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .background(StudioSurfaceVariant)
            .pointerInput(zoomDpPerSec, scrollOffsetPx) {
                detectTapGestures { offset ->
                    val x = offset.x + scrollOffsetPx
                    val sec = x / (zoomDpPerSec * density)
                    val ms = (sec * 1000L).toLong()
                    onSeek(ms.coerceIn(0L, totalDurationMs))
                }
            }
            .pointerInput(zoomDpPerSec, scrollOffsetPx) {
                detectDragGestures { change, _ ->
                    val x = change.position.x + scrollOffsetPx
                    val sec = x / (zoomDpPerSec * density)
                    val ms = (sec * 1000L).toLong()
                    onSeek(ms.coerceIn(0L, totalDurationMs))
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val pxPerSec = zoomDpPerSec * density
            val startSec = (scrollOffsetPx / pxPerSec).toInt().coerceAtLeast(0)
            val endSec = ((scrollOffsetPx + size.width) / pxPerSec).toInt() + 1

            // Horizontal bottom baseline
            drawLine(
                color = StudioBorder,
                start = Offset(0f, size.height),
                end = Offset(size.width, size.height),
                strokeWidth = 1.dp.toPx()
            )

            // Tick marks & labels
            for (sec in startSec..endSec) {
                val secX = (sec * pxPerSec) - scrollOffsetPx

                // Second major tick
                if (secX in 0f..size.width) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.5f),
                        start = Offset(secX, size.height - 14.dp.toPx()),
                        end = Offset(secX, size.height),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Draw time text e.g. 00:01
                    val mins = sec / 60
                    val s = sec % 60
                    val timeStr = "%02d:%02d".format(mins, s)
                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(180, 160, 175, 195)
                            textSize = 22f
                            typeface = android.graphics.Typeface.MONOSPACE
                            isAntiAlias = true
                        }
                        drawText(timeStr, secX + 4.dp.toPx(), size.height - 12.dp.toPx(), paint)
                    }
                }

                // Sub-second minor ticks (frames / quarter seconds)
                for (sub in 1..3) {
                    val subX = secX + (sub * (pxPerSec / 4f))
                    if (subX in 0f..size.width) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.2f),
                            start = Offset(subX, size.height - 6.dp.toPx()),
                            end = Offset(subX, size.height),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }

            // Playhead indicator on ruler
            val playheadX = ((currentPositionMs / 1000f) * pxPerSec) - scrollOffsetPx
            if (playheadX in -20f..size.width + 20f) {
                // Playhead head inverted triangle
                val headPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(playheadX - 6.dp.toPx(), 0f)
                    lineTo(playheadX + 6.dp.toPx(), 0f)
                    lineTo(playheadX, 10.dp.toPx())
                    close()
                }
                drawPath(headPath, CyanAccent)
                drawLine(
                    color = CyanAccent,
                    start = Offset(playheadX, 10.dp.toPx()),
                    end = Offset(playheadX, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
    }
}
