package com.example.ui.timeline

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleProjectRepository
import com.example.model.ClipType
import com.example.model.TransitionType
import com.example.ui.theme.*
import com.example.viewmodel.VideoEditorViewModel

@Composable
fun TimelineToolbar(
    viewModel: VideoEditorViewModel,
    hasSelectedClip: Boolean,
    timelineZoom: Float,
    modifier: Modifier = Modifier
) {
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showTransitionDialog by remember { mutableStateOf(false) }
    var showAddMediaDialog by remember { mutableStateOf(false) }
    var showAddTextDialog by remember { mutableStateOf(false) }
    var showTransformDialog by remember { mutableStateOf(false) }

    // Android Zero-Permission Photo/Video Picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            viewModel.addMediaClip(
                mediaUri = uri.toString(),
                drawableRes = null,
                title = "Imported Video",
                clipType = ClipType.VIDEO,
                durationMs = 5000L
            )
        }
    }

    Surface(
        color = StudioSurface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Split (Razor Blade) Tool
            FilledTonalButton(
                onClick = { viewModel.splitClipAtPlayhead() },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = StudioSurfaceElevated,
                    contentColor = CyanAccent
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("split_clip_button")
            ) {
                Icon(imageVector = Icons.Default.ContentCut, contentDescription = "Split clip", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Split", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            // Duplicate Tool
            IconButton(
                onClick = { viewModel.duplicateSelectedClip() },
                enabled = hasSelectedClip,
                modifier = Modifier.testTag("duplicate_clip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Duplicate clip",
                    tint = if (hasSelectedClip) TextPrimary else TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Delete Tool
            IconButton(
                onClick = { viewModel.deleteSelectedClip() },
                enabled = hasSelectedClip,
                modifier = Modifier.testTag("delete_clip_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete clip",
                    tint = if (hasSelectedClip) AmberAccent else TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
            }

            VerticalDivider(modifier = Modifier.height(20.dp), color = StudioBorder)

            // Speed Dialog Button
            TextButton(
                onClick = { showSpeedDialog = true },
                enabled = hasSelectedClip,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Speed", fontSize = 11.sp)
            }

            // Transition Dialog Button
            TextButton(
                onClick = { showTransitionDialog = true },
                enabled = hasSelectedClip,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Transition", fontSize = 11.sp)
            }

            // Transform Dialog Button
            TextButton(
                onClick = { showTransformDialog = true },
                enabled = hasSelectedClip,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.CropRotate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Transform", fontSize = 11.sp)
            }

            VerticalDivider(modifier = Modifier.height(20.dp), color = StudioBorder)

            // Add Media Button (+ Video)
            FilledTonalButton(
                onClick = { showAddMediaDialog = true },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = StudioSurfaceElevated,
                    contentColor = TextPrimary
                ),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_media_button")
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Media", fontSize = 12.sp)
            }

            // Add Text / Title
            TextButton(
                onClick = { showAddTextDialog = true },
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.Title, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ Title", fontSize = 11.sp)
            }

            VerticalDivider(modifier = Modifier.height(20.dp), color = StudioBorder)

            // Timeline Zoom Controls
            IconButton(
                onClick = { viewModel.setTimelineZoom(timelineZoom - 30f) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.ZoomOut, contentDescription = "Zoom out timeline", modifier = Modifier.size(16.dp))
            }
            IconButton(
                onClick = { viewModel.setTimelineZoom(timelineZoom + 30f) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.ZoomIn, contentDescription = "Zoom in timeline", modifier = Modifier.size(16.dp))
            }
        }
    }

    // --- Dialogs ---

    // Speed Selection Dialog
    if (showSpeedDialog) {
        val speeds = listOf(0.25f, 0.5f, 0.75f, 1.0f, 1.5f, 2.0f, 4.0f)
        val selectedClip = viewModel.uiState.collectAsState().value.project.tracks
            .flatMap { it.clips }
            .find { it.id == viewModel.uiState.value.selectedClipId }

        AlertDialog(
            onDismissRequest = { showSpeedDialog = false },
            title = { Text("Clip Playback Speed") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    speeds.forEach { spd ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedClip?.speed == spd,
                                onClick = {
                                    if (selectedClip != null) {
                                        viewModel.updateClipSpeed(selectedClip.id, spd)
                                        showSpeedDialog = false
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (spd) {
                                    0.25f -> "0.25x (Super Slow Motion)"
                                    0.5f -> "0.5x (Slow Motion)"
                                    1.0f -> "1.0x (Normal Speed)"
                                    2.0f -> "2.0x (Fast Forward)"
                                    4.0f -> "4.0x (Hyperlapse)"
                                    else -> "${spd}x"
                                },
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSpeedDialog = false }) { Text("Close") }
            }
        )
    }

    // Transition Selection Dialog
    if (showTransitionDialog) {
        val selectedClip = viewModel.uiState.collectAsState().value.project.tracks
            .flatMap { it.clips }
            .find { it.id == viewModel.uiState.value.selectedClipId }

        AlertDialog(
            onDismissRequest = { showTransitionDialog = false },
            title = { Text("Choose In-Transition") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TransitionType.values().forEach { trans ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedClip?.transitionIn == trans,
                                onClick = {
                                    if (selectedClip != null) {
                                        viewModel.updateClipTransition(selectedClip.id, trans)
                                        showTransitionDialog = false
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(trans.label, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTransitionDialog = false }) { Text("Close") }
            }
        )
    }

    // Add Media Dialog
    if (showAddMediaDialog) {
        AlertDialog(
            onDismissRequest = { showAddMediaDialog = false },
            title = { Text("Add Footage to Timeline") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select sample footage or import from your gallery:", fontSize = 13.sp, color = TextSecondary)

                    // Photo/Video picker from device
                    Button(
                        onClick = {
                            showAddMediaDialog = false
                            videoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyanAccent, contentColor = StudioBackground),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.VideoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pick Video From Gallery")
                    }

                    HorizontalDivider(color = StudioBorder)

                    Text("Cinematic Sample Assets:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    SampleProjectRepository.sampleMediaClips.forEach { sample ->
                        Surface(
                            onClick = {
                                viewModel.addMediaClip(
                                    mediaUri = null,
                                    drawableRes = sample.drawableRes,
                                    title = sample.title,
                                    clipType = sample.type,
                                    durationMs = sample.durationMs
                                )
                                showAddMediaDialog = false
                            },
                            color = StudioSurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Icon(Icons.Default.Movie, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(sample.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                    Text("${sample.category} • ${sample.durationMs / 1000f}s", fontSize = 11.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddMediaDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Add Text / Title Dialog
    if (showAddTextDialog) {
        var titleText by remember { mutableStateOf("CINEMATIC TITLE") }
        var fontSize by remember { mutableFloatStateOf(28f) }

        AlertDialog(
            onDismissRequest = { showAddTextDialog = false },
            title = { Text("Add Title / Lower Third") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = titleText,
                        onValueChange = { titleText = it },
                        label = { Text("Title Text") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Font Size: ${fontSize.toInt()}sp", fontSize = 12.sp, color = TextSecondary)
                    Slider(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 16f..48f
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val playhead = viewModel.uiState.value.currentPositionMs
                        viewModel.addMediaClip(
                            mediaUri = null,
                            drawableRes = null,
                            title = "Title: $titleText",
                            clipType = ClipType.TEXT,
                            durationMs = 3500L
                        )
                        showAddTextDialog = false
                    }
                ) {
                    Text("Add Title")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTextDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Transform Dialog (Scale, Position, Rotation)
    if (showTransformDialog) {
        val selectedClip = viewModel.uiState.collectAsState().value.project.tracks
            .flatMap { it.clips }
            .find { it.id == viewModel.uiState.value.selectedClipId }

        var scale by remember(selectedClip) { mutableFloatStateOf(selectedClip?.scale ?: 1.0f) }
        var posX by remember(selectedClip) { mutableFloatStateOf(selectedClip?.positionX ?: 0.0f) }
        var posY by remember(selectedClip) { mutableFloatStateOf(selectedClip?.positionY ?: 0.0f) }
        var rotation by remember(selectedClip) { mutableFloatStateOf(selectedClip?.rotation ?: 0.0f) }

        AlertDialog(
            onDismissRequest = { showTransformDialog = false },
            title = { Text("Transform & Layout") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Scale: ${(scale * 100).toInt()}%", fontSize = 12.sp)
                    Slider(value = scale, onValueChange = { scale = it }, valueRange = 0.2f..2.5f)

                    Text("Position X: ${(posX * 100).toInt()}%", fontSize = 12.sp)
                    Slider(value = posX, onValueChange = { posX = it }, valueRange = -1f..1f)

                    Text("Position Y: ${(posY * 100).toInt()}%", fontSize = 12.sp)
                    Slider(value = posY, onValueChange = { posY = it }, valueRange = -1f..1f)

                    Text("Rotation: ${rotation.toInt()}°", fontSize = 12.sp)
                    Slider(value = rotation, onValueChange = { rotation = it }, valueRange = -180f..180f)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedClip != null) {
                            viewModel.updateClipTransform(selectedClip.id, scale, posX, posY, rotation)
                        }
                        showTransformDialog = false
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransformDialog = false }) { Text("Close") }
            }
        )
    }
}
