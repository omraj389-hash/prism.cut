package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AspectRatioOption
import com.example.ui.audiomixer.AudioMixerPanel
import com.example.ui.colorgrade.ColorGradingPanel
import com.example.ui.components.VideoMonitorView
import com.example.ui.export.ExportDialog
import com.example.ui.theme.*
import com.example.ui.timeline.MultiTrackTimelineView
import com.example.ui.timeline.TimelineToolbar
import com.example.viewmodel.VideoEditorViewModel
import com.example.viewmodel.WorkspaceTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoEditorScreen(
    viewModel: VideoEditorViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val activeColorGrade by viewModel.activeColorGrade.collectAsStateWithLifecycle()
    val histogramData by viewModel.histogramData.collectAsStateWithLifecycle()

    var showAspectRatioMenu by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = CyanAccent,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.MovieFilter,
                                    contentDescription = null,
                                    tint = StudioBackground,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "PrismCut Studio",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = uiState.project.title,
                                fontSize = 10.sp,
                                color = TextSecondary
                            )
                        }
                    }
                },
                actions = {
                    // Aspect Ratio Selector
                    Box {
                        FilledTonalButton(
                            onClick = { showAspectRatioMenu = true },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = StudioSurfaceElevated,
                                contentColor = TextPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp).testTag("aspect_ratio_button")
                        ) {
                            Icon(Icons.Default.AspectRatio, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(uiState.project.aspectRatio.widthRatio.toString() + ":" + uiState.project.aspectRatio.heightRatio, fontSize = 11.sp)
                        }

                        DropdownMenu(
                            expanded = showAspectRatioMenu,
                            onDismissRequest = { showAspectRatioMenu = false },
                            modifier = Modifier.background(StudioSurfaceElevated)
                        ) {
                            AspectRatioOption.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label, fontSize = 12.sp) },
                                    onClick = {
                                        viewModel.setAspectRatio(option)
                                        showAspectRatioMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Safe Areas Toggle
                    IconButton(
                        onClick = { viewModel.toggleSafeAreas() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridOn,
                            contentDescription = "Safe Areas",
                            tint = if (uiState.showSafeAreas) CyanAccent else TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // A/B Split Compare Toggle
                    IconButton(
                        onClick = { viewModel.toggleSplitCompare() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "A/B Compare",
                            tint = if (uiState.isSplitCompareActive) CyanAccent else TextTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Export Button
                    Button(
                        onClick = { viewModel.openExportDialog() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyanAccent,
                            contentColor = StudioBackground
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("export_button")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = StudioSurface,
                    titleContentColor = TextPrimary
                )
            )
        },
        containerColor = StudioBackground,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Monitor View (Top Half)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.44f)
                    .background(Color.Black)
            ) {
                VideoMonitorView(
                    project = uiState.project,
                    currentPositionMs = uiState.currentPositionMs,
                    colorGrade = activeColorGrade,
                    isSplitCompareActive = uiState.isSplitCompareActive,
                    splitComparePosition = uiState.splitComparePosition,
                    onSplitComparePositionChange = { viewModel.setSplitComparePosition(it) },
                    showSafeAreas = uiState.showSafeAreas,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Transport Playback Controls Bar
            Surface(
                color = StudioSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Timecode Readout
                    Text(
                        text = "${VideoEditorViewModel.formatTimecode(uiState.currentPositionMs)} / ${VideoEditorViewModel.formatTimecode(uiState.project.durationMs)}",
                        color = CyanAccent,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )

                    // Transport Buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.jumpToStart() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Jump to start", tint = TextPrimary, modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = { viewModel.stepFrame(forward = false) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.FastRewind, contentDescription = "Step frame back", tint = TextPrimary, modifier = Modifier.size(20.dp))
                        }

                        // Play / Pause FAB
                        FilledIconButton(
                            onClick = { viewModel.togglePlayPause() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = CyanAccent,
                                contentColor = StudioBackground
                            ),
                            modifier = Modifier.size(40.dp).testTag("play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.stepFrame(forward = true) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.FastForward, contentDescription = "Step frame forward", tint = TextPrimary, modifier = Modifier.size(20.dp))
                        }
                        IconButton(
                            onClick = { viewModel.jumpToEnd() },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Jump to end", tint = TextPrimary, modifier = Modifier.size(20.dp))
                        }
                    }

                    // FPS & Render badge
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = StudioSurfaceElevated
                    ) {
                        Text(
                            text = "${uiState.project.fps} FPS",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Workspace Tabs Selector (Edit, Color, Audio)
            PrimaryTabRow(
                selectedTabIndex = uiState.activeWorkspace.ordinal,
                containerColor = StudioSurface,
                contentColor = CyanAccent,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(uiState.activeWorkspace.ordinal),
                        color = CyanAccent
                    )
                }
            ) {
                WorkspaceTab.values().take(3).forEach { tab ->
                    Tab(
                        selected = uiState.activeWorkspace == tab,
                        onClick = { viewModel.setWorkspace(tab) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (tab) {
                                        WorkspaceTab.EDIT -> Icons.Default.ContentCut
                                        WorkspaceTab.COLOR -> Icons.Default.Palette
                                        WorkspaceTab.AUDIO -> Icons.Default.GraphicEq
                                        WorkspaceTab.EXPORT -> Icons.Default.CloudUpload
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (uiState.activeWorkspace == tab) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )
                }
            }

            // Bottom Workspace Area (Timeline / Color Grading / Audio Mixer)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.56f)
                    .background(StudioBackground)
            ) {
                when (uiState.activeWorkspace) {
                    WorkspaceTab.EDIT -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Timeline Toolbar (Split, Duplicate, Delete, Speed, Transform, + Media)
                            TimelineToolbar(
                                viewModel = viewModel,
                                hasSelectedClip = uiState.selectedClipId != null,
                                timelineZoom = uiState.timelineZoom
                            )

                            // Multi-track Timeline View
                            MultiTrackTimelineView(
                                project = uiState.project,
                                currentPositionMs = uiState.currentPositionMs,
                                selectedTrackId = uiState.selectedTrackId,
                                selectedClipId = uiState.selectedClipId,
                                zoomDpPerSec = uiState.timelineZoom,
                                onClipSelect = { tId, cId -> viewModel.selectClip(tId, cId) },
                                onClipTrim = { cId, start, dur -> viewModel.trimClip(cId, start, dur) },
                                onSeek = { pos -> viewModel.seekTo(pos) },
                                onToggleMute = { tId -> viewModel.toggleTrackMute(tId) },
                                onToggleLock = { tId -> viewModel.toggleTrackLock(tId) },
                                onToggleVisibility = { tId -> viewModel.toggleTrackVisibility(tId) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    WorkspaceTab.COLOR -> {
                        ColorGradingPanel(
                            viewModel = viewModel,
                            colorGrade = activeColorGrade,
                            histogramData = histogramData,
                            isSplitActive = uiState.isSplitCompareActive,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    WorkspaceTab.AUDIO -> {
                        AudioMixerPanel(
                            viewModel = viewModel,
                            projectTracks = uiState.project.tracks,
                            isPlaying = uiState.isPlaying,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    WorkspaceTab.EXPORT -> {
                        // Handled via dialog
                    }
                }
            }
        }
    }

    // Export Dialog
    if (uiState.showExportDialog) {
        ExportDialog(
            viewModel = viewModel,
            isExporting = uiState.isExporting,
            exportProgress = uiState.exportProgress,
            isCompleted = uiState.exportCompleted,
            exportConfig = uiState.exportConfig,
            onDismiss = { viewModel.closeExportDialog() }
        )
    }
}
