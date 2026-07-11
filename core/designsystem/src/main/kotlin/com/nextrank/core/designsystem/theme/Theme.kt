package com.nextrank.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

// --- Colors ---

val PrimaryLight = Color(0xFF6C63FF)
val PrimaryDark = Color(0xFF8B83FF)
val PrimaryContainerLight = Color(0xFFE8E6FF)
val PrimaryContainerDark = Color(0xFF2A2660)

val BackgroundLight = Color(0xFFF8F9FF)
val BackgroundDark = Color(0xFF121218)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E1E28)

val TextPrimaryLight = Color(0xFF1A1A2E)
val TextPrimaryDark = Color(0xFFEAEAFF)
val TextSecondaryLight = Color(0xFF6B6B8D)
val TextSecondaryDark = Color(0xFF9999B3)

val SuccessLight = Color(0xFF2ECC71)
val SuccessDark = Color(0xFF27AE60)
val ErrorLight = Color(0xFFE74C3C)
val ErrorDark = Color(0xFFC0392B)
val WarningLight = Color(0xFFF39C12)
val WarningDark = Color(0xFFE67E22)

// --- Color Schemes ---

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color.White,
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = Color(0xFF2A2660),
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF0F0F8),
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorLight,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF1A1A2E),
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = Color(0xFFD4D0FF),
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF2A2A38),
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorDark,
    onError = Color.White,
)

/**
 * Тема приложения NextRank.
 * Material 3 с кастомной цветовой палитрой в стиле CS2.
 */
@Composable
fun NextRankTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
