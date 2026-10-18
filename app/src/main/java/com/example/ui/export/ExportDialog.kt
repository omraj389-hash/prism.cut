package com.example.ui.export

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.viewmodel.ExportConfig
import com.example.viewmodel.VideoEditorViewModel

@Composable
fun ExportDialog(
    viewModel: VideoEditorViewModel,
    isExporting: Boolean,
    exportProgress: Float,
    isCompleted: Boolean,
    exportConfig: ExportConfig,
    onDismiss: () -> Unit
) {
    var selectedRes by remember { mutableStateOf(exportConfig.resolution) }
    var selectedFps by remember { mutableIntStateOf(exportConfig.fps) }
    var selectedBitrate by remember { mutableStateOf(exportConfig.bitrate) }

    val resolutions = listOf(
        "720p HD (1280x720)",
        "1080p Full HD (1920x1080)",
        "4K Ultra HD (3840x2160)"
    )
    val fpsOptions = listOf(24, 30, 60)
    val bitrates = listOf("Medium (10 Mbps)", "High (16 Mbps)", "Master (32 Mbps)")

    AlertDialog(
        onDismissRequest = { if (!isExporting) onDismiss() },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCompleted) Icons.Default.CheckCircle else Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = if (isCompleted) EmeraldAccent else CyanAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCompleted) "Export Complete!" else if (isExporting) "Rendering Master Video..." else "Export Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            if (isCompleted) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Text("Your video has been rendered and color graded successfully with hardware acceleration.", fontSize = 13.sp, color = TextPrimary)
                    Surface(
                        color = StudioSurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Output Specifications:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Format: MP4 (H.264 / AAC Audio)", fontSize = 12.sp, color = TextPrimary)
                            Text("• Resolution: $selectedRes", fontSize = 12.sp, color = TextPrimary)
                            Text("• Frame Rate: $selectedFps FPS", fontSize = 12.sp, color = TextPrimary)
                            Text("• Color Matrix: Rec.709 with LUT grading", fontSize = 12.sp, color = TextPrimary)
                            Text("• File Size: ~48.2 MB", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }
            } else if (isExporting) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text("Compositing layers and applying real-time LUT grading...", fontSize = 13.sp, color = TextSecondary)

                    LinearProgressIndicator(
                        progress = { exportProgress },
                        color = CyanAccent,
                        trackColor = StudioSurfaceElevated,
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${(exportProgress * 100).toInt()}% Rendered", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        Text("Frame ${(exportProgress * 450).toInt()} / 450", fontSize = 11.sp, color = TextSecondary)
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Resolution
                    Text("Resolution:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    resolutions.forEach { res ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedRes == res,
                                onClick = { selectedRes = res }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(res, fontSize = 13.sp, color = TextPrimary)
                        }
                    }

                    // FPS
                    Text("Frame Rate:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        fpsOptions.forEach { fps ->
                            FilterChip(
                                selected = selectedFps == fps,
                                onClick = { selectedFps = fps },
                                label = { Text("$fps FPS", fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioSurfaceElevated,
                                    selectedLabelColor = CyanAccent
                                )
                            )
                        }
                    }

                    // Bitrate
                    Text("Encoding Bitrate:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        bitrates.forEach { bit ->
                            FilterChip(
                                selected = selectedBitrate == bit,
                                onClick = { selectedBitrate = bit },
                                label = { Text(bit.split(" ").first(), fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = StudioSurfaceElevated,
                                    selectedLabelColor = AmberAccent
                                )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isCompleted) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = StudioBackground)
                ) {
                    Text("Done")
                }
            } else if (!isExporting) {
                Button(
                    onClick = {
                        viewModel.updateExportConfig(
                            ExportConfig(resolution = selectedRes, fps = selectedFps, bitrate = selectedBitrate)
                        )
                        viewModel.startExport()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = StudioBackground),
                    modifier = Modifier.testTag("start_render_button")
                ) {
                    Icon(Icons.Default.MovieCreation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start Render")
                }
            }
        },
        dismissButton = {
            if (!isExporting) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
            }
        }
    )
}
