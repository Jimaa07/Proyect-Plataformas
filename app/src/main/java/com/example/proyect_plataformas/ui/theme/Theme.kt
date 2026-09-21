package com.example.proyect_plataformas.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = LocalHandsGreen,
    onPrimary = Color.White,

    primaryContainer = LocalHandsMint,
    onPrimaryContainer = LocalHandsGreen,

    secondary = LocalHandsGreenLight,
    onSecondary = Color.White,

    secondaryContainer = LocalHandsMintSoft,
    onSecondaryContainer = LocalHandsGreen,

    background = LocalHandsBackground,
    onBackground = LocalHandsText,

    surface = LocalHandsSurface,
    onSurface = LocalHandsText,

    surfaceVariant = LocalHandsSurfaceVariant,
    onSurfaceVariant = LocalHandsTextSecondary,

    outline = LocalHandsOutline,

    error = LocalHandsError
)

private val DarkColorScheme = darkColorScheme(
    primary = LocalHandsMint,
    secondary = LocalHandsMint,
    background = Color(0xFF10201C),
    surface = Color(0xFF162823)
)

@Composable
fun ProyectplataformasTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}