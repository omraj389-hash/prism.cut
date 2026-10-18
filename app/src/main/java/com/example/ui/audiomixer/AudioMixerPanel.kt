package com.example.ui.audiomixer

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SampleAudio
import com.example.data.SampleProjectRepository
import com.example.model.ClipType
import com.example.model.Track
import com.example.model.TrackType
import com.example.ui.theme.*
import com.example.viewmodel.VideoEditorViewModel

@Composable
fun AudioMixerPanel(
    viewModel: VideoEditorViewModel,
    projectTracks: List<Track>,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Mixer Faders, 1: Music & SFX Library

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("audio_mixer_panel")
    ) {
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = StudioSurfaceVariant,
            contentColor = CyanAccent
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { activeTab = 0 },
                text = { Text("Multi-Track Faders", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { activeTab = 1 },
                text = { Text("Sound & Music Library", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(StudioBackground)
        ) {
            if (activeTab == 0) {
                // Audio Faders
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val audioRelevantTracks = projectTracks.filter {
                        it.type == TrackType.VIDEO_MAIN ||
                        it.type == TrackType.AUDIO_MUSIC ||
                        it.type == TrackType.AUDIO_SFX
                    }

                    audioRelevantTracks.forEach { track ->
                        TrackFaderStrip(
                            track = track,
                            isPlaying = isPlaying,
                            onMuteToggle = { viewModel.toggleTrackMute(track.id) },
                            onVolumeChange = { vol ->
                                track.clips.firstOrNull()?.let {
                                    viewModel.updateClipVolume(it.id, vol)
                                }
                            },
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        )
                    }
                }
            } else {
                // Sound & Music Library
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            text = "ROYALTY-FREE SOUNDTRACKS & FOLEY",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }

                    items(SampleProjectRepository.sampleAudioClips) { audio ->
                        AudioLibraryItem(
                            audio = audio,
                            onAddToTimeline = {
                                viewModel.addMediaClip(
                                    mediaUri = null,
                                    drawableRes = null,
                                    title = audio.title,
                                    clipType = ClipType.AUDIO,
                                    durationMs = audio.durationMs
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrackFaderStrip(
    track: Track,
    isPlaying: Boolean,
    onMuteToggle: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeClip = track.clips.firstOrNull()
    var volume by remember(activeClip?.volume) { mutableFloatStateOf(activeClip?.volume ?: 1.0f) }

    Card(
        colors = CardDefaults.cardColors(containerColor = StudioSurfaceVariant),
        border = CardDefaults.outlinedCardBorder(),
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Track label
            Text(
                text = track.name.take(10),
                color = TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            // VU Peak Meter + Fader slider
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // VU meter simulated LED ladder
                Canvas(
                    modifier = Modifier
                        .width(10.dp)
                        .fillMaxHeight()
                        .background(Color(0xFF090B0E), RoundedCornerShape(2.dp))
                ) {
                    val h = size.height
                    val w = size.width
                    val activeFraction = if (isPlaying && !track.isMuted) (volume * 0.7f + 0.15f * kotlin.math.sin(System.currentTimeMillis() / 150.0).toFloat()).coerceIn(0f, 1f) else 0f
                    val filledH = h * activeFraction

                    // Green lower, Yellow middle, Red peak
                    drawRect(
                        color = ScopeGreen,
                        topLeft = Offset(0f, h - filledH),
                        size = Size(w, filledH)
                    )
                    if (activeFraction > 0.75f) {
                        drawRect(
                            color = AmberAccent,
                            topLeft = Offset(0f, h * 0.25f),
                            size = Size(w, (h * 0.15f))
                        )
                    }
                    if (activeFraction > 0.9f) {
                        drawRect(
                            color = ScopeRed,
                            topLeft = Offset(0f, 0f),
                            size = Size(w, h * 0.10f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Fader Slider (Volume)
                Slider(
                    value = volume,
                    onValueChange = {
                        volume = it
                        onVolumeChange(it)
                    },
                    valueRange = 0f..2f,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanAccent,
                        activeTrackColor = CyanAccent,
                        inactiveTrackColor = StudioBorder
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            // dB Readout
            val dbText = if (volume <= 0.05f) "-∞ dB" else "${((volume - 1f) * 12).toInt()} dB"
            Text(
                text = dbText,
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )

            // Mute Button
            IconButton(
                onClick = onMuteToggle,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (track.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Mute track",
                    tint = if (track.isMuted) AmberAccent else TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun AudioLibraryItem(
    audio: SampleAudio,
    onAddToTimeline: () -> Unit
) {
    Surface(
        color = StudioSurfaceVariant,
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = AudioTrackColor,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.MusicNote, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(audio.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${audio.genre} • ${audio.durationMs / 1000f}s • ${if (audio.bpm > 0) "${audio.bpm} BPM" else "SFX"}", color = TextSecondary, fontSize = 11.sp)
                }
            }

            FilledTonalButton(
                onClick = onAddToTimeline,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = StudioSurfaceElevated,
                    contentColor = CyanAccent
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 11.sp)
            }
        }
    }
}
