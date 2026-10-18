package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val StudioColorScheme = darkColorScheme(
    primary = CyanAccent,
    onPrimary = StudioBackground,
    primaryContainer = StudioSurfaceElevated,
    onPrimaryContainer = CyanAccent,
    secondary = AmberAccent,
    onSecondary = StudioBackground,
    secondaryContainer = StudioSurfaceElevated,
    onSecondaryContainer = AmberAccent,
    tertiary = MagentaAccent,
    onTertiary = StudioBackground,
    background = StudioBackground,
    onBackground = TextPrimary,
    surface = StudioSurface,
    onSurface = TextPrimary,
    surfaceVariant = StudioSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = StudioBorder,
    outlineVariant = StudioSurfaceElevated
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudioColorScheme,
        typography = Typography,
        content = content
    )
}
