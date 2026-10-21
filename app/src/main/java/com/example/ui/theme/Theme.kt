package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val FrameXDarkColorScheme = darkColorScheme(
    primary = PitchEmerald,
    onPrimary = ObsidianBg,
    primaryContainer = BroadcastCardElevated,
    onPrimaryContainer = PitchEmerald,
    secondary = StreamCyan,
    onSecondary = ObsidianBg,
    secondaryContainer = BroadcastCard,
    onSecondaryContainer = StreamCyan,
    tertiary = MiamiPink,
    onTertiary = TextPrimary,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = MidnightSurface,
    onSurface = TextPrimary,
    surfaceVariant = BroadcastCard,
    onSurfaceVariant = TextSecondary,
    outline = DividerSubtle,
    error = LiveCrimson,
    onError = TextPrimary
)

@Composable
fun FrameXTVTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FrameXDarkColorScheme,
        typography = Typography,
        content = content
    )
}
