package com.example.ui.colorgrade

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ColorWheelSetting
import com.example.ui.theme.*
import kotlin.math.*

@Composable
fun ColorWheelView(
    title: String,
    subtitle: String,
    setting: ColorWheelSetting,
    accentColor: Color,
    onSettingChange: (hue: Float, saturation: Float, luminance: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(StudioBorder, StudioSurface))),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.padding(4.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(10.dp)
        ) {
            // Header: Title & Reset button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 10.sp
                    )
                }
                IconButton(
                    onClick = { onSettingChange(0f, 0f, 0f) },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset $title Wheel",
                        tint = if (setting.isDefault) TextTertiary else CyanAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2D Polar Color Wheel Canvas
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectDragGestures { change, _ ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touch = change.position
                            val dx = touch.x - center.x
                            val dy = touch.y - center.y
                            val radius = size.width / 2f

                            val dist = sqrt(dx * dx + dy * dy)
                            val sat = (dist / radius).coerceIn(0f, 1f)
                            var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                            if (angle < 0) angle += 360f

                            onSettingChange(angle, sat, setting.luminance)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.width / 2f
                    val center = Offset(radius, radius)

                    // Draw circular gradient wheel (approximate hue disc)
                    val sweepColors = listOf(
                        Color(0xFFFF3B30), // Red 0°
                        Color(0xFFFF9500), // Orange 30°
                        Color(0xFFFFCC00), // Yellow 60°
                        Color(0xFF34C759), // Green 120°
                        Color(0xFF00C7BE), // Cyan 180°
                        Color(0xFF007AFF), // Blue 240°
                        Color(0xFF5856D6), // Purple 280°
                        Color(0xFFAF52DE), // Magenta 300°
                        Color(0xFFFF2D55), // Pink 330°
                        Color(0xFFFF3B30)  // Red 360°
                    )
                    drawCircle(
                        brush = Brush.sweepGradient(sweepColors, center),
                        radius = radius,
                        center = center
                    )

                    // Desaturate center towards dark neutral
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(Color(0xFF1E2430), Color(0xFF1E2430).copy(alpha = 0.7f), Color.Transparent),
                            center = center,
                            radius = radius
                        ),
                        radius = radius,
                        center = center
                    )

                    // Border ring
                    drawCircle(
                        color = StudioBorder,
                        radius = radius - 1f,
                        center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Crosshair guides
                    drawLine(
                        color = Color.White.copy(alpha = 0.2f),
                        start = Offset(center.x - 8.dp.toPx(), center.y),
                        end = Offset(center.x + 8.dp.toPx(), center.y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.White.copy(alpha = 0.2f),
                        start = Offset(center.x, center.y - 8.dp.toPx()),
                        end = Offset(center.x, center.y + 8.dp.toPx()),
                        strokeWidth = 1.dp.toPx()
                    )

                    // Puck position
                    val angleRad = Math.toRadians(setting.hue.toDouble())
                    val puckDist = setting.saturation * (radius - 8.dp.toPx())
                    val puckX = center.x + (cos(angleRad) * puckDist).toFloat()
                    val puckY = center.y + (sin(angleRad) * puckDist).toFloat()

                    drawCircle(
                        color = Color.Black.copy(alpha = 0.6f),
                        radius = 7.dp.toPx(),
                        center = Offset(puckX, puckY)
                    )
                    drawCircle(
                        color = accentColor,
                        radius = 5.dp.toPx(),
                        center = Offset(puckX, puckY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx(),
                        center = Offset(puckX, puckY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Luminance Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Y",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(14.dp)
                )
                Slider(
                    value = setting.luminance,
                    onValueChange = { newLum ->
                        onSettingChange(setting.hue, setting.saturation, newLum)
                    },
                    valueRange = -1f..1f,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = StudioBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(28.dp)
                )
                Text(
                    text = "${(setting.luminance * 100).toInt()}",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    modifier = Modifier.width(26.dp)
                )
            }
        }
    }
}
