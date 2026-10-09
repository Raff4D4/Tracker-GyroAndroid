package com.example.tracker_gyro.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightCanvas = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightBorder = Color(0x4DFFFFFF)
val DarkCanvas = Color(0xFF0B0F17)
val DarkSurface = Color(0xFF1E293B)
val DarkBorder = Color(0x3394A3B8)
val DeepSlate = Color(0xFF334155)
val FrostedSilver = Color(0xFFE2E8F0)
val SubtleEmerald = Color(0xFF10B981)

private val LightColors = lightColorScheme(
    background = LightCanvas,
    surface = LightSurface,
    onBackground = DeepSlate,
    onSurface = DeepSlate,
    primary = DeepSlate,
    secondary = FrostedSilver,
    tertiary = SubtleEmerald,
    outline = LightBorder
)

private val DarkColors = darkColorScheme(
    background = DarkCanvas,
    surface = DarkSurface,
    onBackground = FrostedSilver,
    onSurface = FrostedSilver,
    primary = FrostedSilver,
    secondary = DeepSlate,
    tertiary = SubtleEmerald,
    outline = DarkBorder
)

@Composable
fun GlassTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
