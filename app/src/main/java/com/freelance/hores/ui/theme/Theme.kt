package com.freelance.hores.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF1F88E5),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E4FF),
    onPrimaryContainer = Color(0xFF001A3F),
    secondary = Color(0xFF5D5E7F),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE3E0FF),
    onSecondaryContainer = Color(0xFF1A1B38),
    tertiary = Color(0xFF785900),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDCC0),
    onTertiaryContainer = Color(0xFF2C1F00),
    error = Color(0xFFB3261E),
    onError = Color.White,
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B),
    outline = Color(0xFF79747E),
    background = Color(0xFFFFFBFE),
    onBackground = Color(0xFF1C1B1F),
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1C1B1F),
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFB3D9FF),
    onPrimary = Color(0xFF003066),
    primaryContainer = Color(0xFF004A94),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFFC7C7E8),
    onSecondary = Color(0xFF2E2F4E),
    secondaryContainer = Color(0xFF454667),
    onSecondaryContainer = Color(0xFFE3E0FF),
    tertiary = Color(0xFFFFB873),
    onTertiary = Color(0xFF462900),
    tertiaryContainer = Color(0xFF643D00),
    onTertiaryContainer = Color(0xFFFFDCC0),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    outline = Color(0xFF9A9AA9),
    background = Color(0xFF1C1B1F),
    onBackground = Color(0xFFE7E0E6),
    surface = Color(0xFF1C1B1F),
    onSurface = Color(0xFFE7E0E6),
)

@Composable
fun HoresFreelanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
