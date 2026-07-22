package edu.lzjtu.environment.oildepot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Teal700,
    onPrimary = White,
    primaryContainer = Color(0xFFD2ECEC),
    onPrimaryContainer = Color(0xFF00363A),
    secondary = Green600,
    onSecondary = White,
    secondaryContainer = Color(0xFFD6EBDD),
    onSecondaryContainer = Color(0xFF0D3522),
    tertiary = Amber600,
    tertiaryContainer = Color(0xFFFFDDB8),
    error = Red700,
    background = Mist100,
    onBackground = Ink900,
    surface = White,
    onSurface = Ink900,
    surfaceVariant = Color(0xFFE7EFEF),
    onSurfaceVariant = Ink600,
    outline = Color(0xFF78898C),
    outlineVariant = Line200,
)

@Composable
fun OilDepotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = AppTypography,
        content = content,
    )
}

