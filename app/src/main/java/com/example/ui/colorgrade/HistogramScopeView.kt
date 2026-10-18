package com.example.ui.colorgrade

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.util.HistogramData

enum class ScopeMode {
    RGB_OVERLAY,
    RGB_PARADE
}

@Composable
fun HistogramScopeView(
    data: HistogramData,
    modifier: Modifier = Modifier
) {
    var scopeMode by remember { mutableStateOf(ScopeMode.RGB_OVERLAY) }

    Card(
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "REAL-TIME COLOR SCOPE",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = StudioSurfaceElevated
                    ) {
                        Text(
                            text = "LIVE",
                            color = EmeraldAccent,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = scopeMode == ScopeMode.RGB_OVERLAY,
                        onClick = { scopeMode = ScopeMode.RGB_OVERLAY },
                        label = { Text("RGB Overlay", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioSurfaceElevated,
                            selectedLabelColor = CyanAccent
                        )
                    )
                    FilterChip(
                        selected = scopeMode == ScopeMode.RGB_PARADE,
                        onClick = { scopeMode = ScopeMode.RGB_PARADE },
                        label = { Text("Parade", fontSize = 10.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioSurfaceElevated,
                            selectedLabelColor = CyanAccent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Canvas Scope Display
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .background(Color(0xFF090B0E), RoundedCornerShape(8.dp))
                    .border(1.dp, StudioBorder, RoundedCornerShape(8.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 4.dp)) {
                    val w = size.width
                    val h = size.height

                    // Draw IRE Grid lines (0, 50, 100 IRE)
                    val ireLines = listOf(0.1f, 0.5f, 0.9f)
                    ireLines.forEach { frac ->
                        val y = h * frac
                        drawLine(
                            color = Color.White.copy(alpha = 0.08f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    if (scopeMode == ScopeMode.RGB_OVERLAY) {
                        // Overlay all 3 channels
                        drawChannelCurve(data.red, ScopeRed.copy(alpha = 0.85f), w, h)
                        drawChannelCurve(data.green, ScopeGreen.copy(alpha = 0.85f), w, h)
                        drawChannelCurve(data.blue, ScopeBlue.copy(alpha = 0.85f), w, h)
                    } else {
                        // Parade: 3 separate columns (R, G, B)
                        val colW = w / 3f
                        // Red col
                        drawChannelCurve(data.red, ScopeRed, colW, h, offsetX = 0f)
                        // Green col
                        drawChannelCurve(data.green, ScopeGreen, colW, h, offsetX = colW)
                        // Blue col
                        drawChannelCurve(data.blue, ScopeBlue, colW, h, offsetX = colW * 2f)

                        // Column dividers
                        drawLine(Color.White.copy(alpha = 0.15f), Offset(colW, 0f), Offset(colW, h), 1.dp.toPx())
                        drawLine(Color.White.copy(alpha = 0.15f), Offset(colW * 2f, 0f), Offset(colW * 2f, h), 1.dp.toPx())
                    }
                }

                // IRE Labels
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(start = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("100 IRE", color = TextTertiary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text("50 IRE", color = TextTertiary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                    Text("0 IRE", color = TextTertiary, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
                }
            }
        }
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawChannelCurve(
    bins: FloatArray,
    color: Color,
    width: Float,
    height: Float,
    offsetX: Float = 0f
) {
    if (bins.isEmpty()) return
    val path = Path()
    val step = width / (bins.size - 1)

    bins.forEachIndexed { i, value ->
        val x = offsetX + (i * step)
        val y = height - (value * height * 0.85f) - (height * 0.05f)
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(width = 1.5.dp.toPx())
    )
}
