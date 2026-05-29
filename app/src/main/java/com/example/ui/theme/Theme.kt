package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DeepPrimary,
    onPrimary = Color.White,
    secondary = BrightAccent,
    onSecondary = Color.Black,
    tertiary = AccentGold,
    background = SlateDarkBackground,
    onBackground = Color(0xFFECEFF1),
    surface = SlateDarkSurface,
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = SlateDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFECEFF1)
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    secondary = LightAccent,
    onSecondary = Color.White,
    tertiary = AccentGold,
    background = LightBg,
    onBackground = Color(0xFF1E293B),
    surface = LightSurface,
    onSurface = Color(0xFF1E293B),
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = Color(0xFF1E293B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Defaults to Dark Theme as per user request
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
