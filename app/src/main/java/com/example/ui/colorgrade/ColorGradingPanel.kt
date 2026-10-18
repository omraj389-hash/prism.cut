package com.example.ui.colorgrade

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ColorGradeSettings
import com.example.model.LutLibrary
import com.example.model.LutPreset
import com.example.ui.theme.*
import com.example.util.HistogramData
import com.example.viewmodel.VideoEditorViewModel

enum class GradingSubTab(val label: String) {
    WHEELS("Color Wheels"),
    PRIMARIES("Sliders"),
    LUTS("LUT Presets"),
    SCOPES("RGB Scope")
}

@Composable
fun ColorGradingPanel(
    viewModel: VideoEditorViewModel,
    colorGrade: ColorGradeSettings,
    histogramData: HistogramData,
    isSplitActive: Boolean,
    modifier: Modifier = Modifier
) {
    var activeSubTab by remember { mutableStateOf(GradingSubTab.WHEELS) }
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("color_grading_panel")
    ) {
        // Control Bar: Scope target, Split compare, Reset button
        Surface(
            color = StudioSurface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Scope selector (Clip vs Project)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = !uiState.isColorGradingClip,
                        onClick = { if (uiState.isColorGradingClip) viewModel.toggleGradeScopeTarget() },
                        label = { Text("Master Grade", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioSurfaceElevated,
                            selectedLabelColor = CyanAccent
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = uiState.isColorGradingClip,
                        onClick = { if (!uiState.isColorGradingClip) viewModel.toggleGradeScopeTarget() },
                        label = { Text("Clip Grade", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = StudioSurfaceElevated,
                            selectedLabelColor = AmberAccent
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Split compare button
                    IconButton(
                        onClick = { viewModel.toggleSplitCompare() },
                        modifier = Modifier.testTag("toggle_split_compare")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Toggle A/B Split Comparison",
                            tint = if (isSplitActive) CyanAccent else TextSecondary
                        )
                    }

                    // Reset Grade button
                    IconButton(
                        onClick = { viewModel.resetColorGrade() },
                        modifier = Modifier.testTag("reset_color_grade")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Color Grade",
                            tint = if (colorGrade.isModified) AmberAccent else TextTertiary
                        )
                    }
                }
            }
        }

        // Subtabs Navigation
        TabRow(
            selectedTabIndex = activeSubTab.ordinal,
            containerColor = StudioSurfaceVariant,
            contentColor = CyanAccent,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[activeSubTab.ordinal]),
                    color = CyanAccent
                )
            }
        ) {
            GradingSubTab.values().forEach { tab ->
                Tab(
                    selected = activeSubTab == tab,
                    onClick = { activeSubTab = tab },
                    text = {
                        Text(
                            text = tab.label,
                            fontSize = 11.sp,
                            fontWeight = if (activeSubTab == tab) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        // Content Area according to active subtab
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(StudioBackground)
        ) {
            when (activeSubTab) {
                GradingSubTab.WHEELS -> {
                    // 3-Way Wheels in horizontal scrolling container
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .horizontalScroll(rememberScrollState())
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ColorWheelView(
                            title = "LIFT",
                            subtitle = "Shadows",
                            setting = colorGrade.lift,
                            accentColor = ScopeBlue,
                            onSettingChange = { h, s, l -> viewModel.updateLiftWheel(h, s, l) },
                            modifier = Modifier.width(170.dp)
                        )
                        ColorWheelView(
                            title = "GAMMA",
                            subtitle = "Midtones",
                            setting = colorGrade.gamma,
                            accentColor = ScopeGreen,
                            onSettingChange = { h, s, l -> viewModel.updateGammaWheel(h, s, l) },
                            modifier = Modifier.width(170.dp)
                        )
                        ColorWheelView(
                            title = "GAIN",
                            subtitle = "Highlights",
                            setting = colorGrade.gain,
                            accentColor = AmberAccent,
                            onSettingChange = { h, s, l -> viewModel.updateGainWheel(h, s, l) },
                            modifier = Modifier.width(170.dp)
                        )
                        ColorWheelView(
                            title = "OFFSET",
                            subtitle = "Master",
                            setting = colorGrade.offset,
                            accentColor = CyanAccent,
                            onSettingChange = { h, s, l -> viewModel.updateOffsetWheel(h, s, l) },
                            modifier = Modifier.width(170.dp)
                        )
                    }
                }

                GradingSubTab.PRIMARIES -> {
                    // Primary sliders list
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GradingSliderItem(
                            label = "Exposure",
                            value = colorGrade.exposure,
                            valueRange = -100f..100f,
                            accentColor = AmberAccent,
                            onValueChange = { viewModel.updateExposure(it) }
                        )
                        GradingSliderItem(
                            label = "Contrast",
                            value = colorGrade.contrast,
                            valueRange = -100f..100f,
                            accentColor = CyanAccent,
                            onValueChange = { viewModel.updateContrast(it) }
                        )
                        GradingSliderItem(
                            label = "Saturation",
                            value = colorGrade.saturation,
                            valueRange = 0f..200f,
                            defaultValue = 100f,
                            accentColor = MagentaAccent,
                            onValueChange = { viewModel.updateSaturation(it) }
                        )
                        GradingSliderItem(
                            label = "Temperature (Warmth)",
                            value = colorGrade.temperature,
                            valueRange = -100f..100f,
                            accentColor = AmberAccent,
                            onValueChange = { viewModel.updateTemperature(it) }
                        )
                        GradingSliderItem(
                            label = "Tint (Green / Magenta)",
                            value = colorGrade.tint,
                            valueRange = -100f..100f,
                            accentColor = ScopeGreen,
                            onValueChange = { viewModel.updateTint(it) }
                        )
                        GradingSliderItem(
                            label = "Highlights Recovery",
                            value = colorGrade.highlights,
                            valueRange = -100f..100f,
                            accentColor = Color.White,
                            onValueChange = { viewModel.updateHighlights(it) }
                        )
                        GradingSliderItem(
                            label = "Shadows Lift",
                            value = colorGrade.shadows,
                            valueRange = -100f..100f,
                            accentColor = TextSecondary,
                            onValueChange = { viewModel.updateShadows(it) }
                        )
                        GradingSliderItem(
                            label = "Vignette",
                            value = colorGrade.vignette,
                            valueRange = 0f..100f,
                            accentColor = PurpleAccent,
                            onValueChange = { viewModel.updateVignette(it) }
                        )
                        GradingSliderItem(
                            label = "Hue Shift",
                            value = colorGrade.hueShift,
                            valueRange = -180f..180f,
                            accentColor = CyanAccent,
                            onValueChange = { viewModel.updateHueShift(it) }
                        )
                    }
                }

                GradingSubTab.LUTS -> {
                    // LUT Presets Gallery
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "CINEMATIC FILM & CREATIVE LUTS",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(LutLibrary.presets) { preset ->
                                LutPresetCard(
                                    preset = preset,
                                    isSelected = colorGrade.activeLutId == preset.id,
                                    onClick = { viewModel.applyLut(preset) }
                                )
                            }
                        }
                    }
                }

                GradingSubTab.SCOPES -> {
                    HistogramScopeView(
                        data = histogramData,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun GradingSliderItem(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    accentColor: Color,
    defaultValue: Float = 0f,
    onValueChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
            modifier = Modifier.width(135.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = accentColor,
                activeTrackColor = accentColor,
                inactiveTrackColor = StudioBorder
            ),
            modifier = Modifier
                .weight(1f)
                .height(26.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${value.toInt()}",
            color = if (value != defaultValue) accentColor else TextSecondary,
            fontSize = 11.sp,
            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
            modifier = Modifier.width(36.dp)
        )
    }
}

@Composable
private fun LutPresetCard(
    preset: LutPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) StudioSurfaceElevated else StudioSurfaceVariant
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(if (isSelected) CyanAccent else StudioBorder)
        ),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .width(140.dp)
            .height(130.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = if (isSelected) CyanAccent else StudioBorder
            ) {
                Text(
                    text = preset.category.uppercase(),
                    color = if (isSelected) StudioBackground else TextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Column {
                Text(
                    text = preset.name,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = preset.description,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    maxLines = 2
                )
            }

            if (isSelected) {
                Text(
                    text = "ACTIVE",
                    color = CyanAccent,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
