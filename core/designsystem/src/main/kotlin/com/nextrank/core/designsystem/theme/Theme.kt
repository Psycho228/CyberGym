@file:Suppress("MagicNumber")

package com.nextrank.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val PrimaryLight = Color(0xFF00E5FF)
val PrimaryDark = Color(0xFF00E5FF)
val PrimaryContainerLight = Color(0xFF0A3A46)
val PrimaryContainerDark = Color(0xFF053640)

val BackgroundLight = Color(0xFF071018)
val BackgroundDark = Color(0xFF071018)
val SurfaceLight = Color(0xFF0F1A24)
val SurfaceDark = Color(0xFF0F1A24)

val TextPrimaryLight = Color(0xFFEAF7FF)
val TextPrimaryDark = Color(0xFFEAF7FF)
val TextSecondaryLight = Color(0xFF93A8B8)
val TextSecondaryDark = Color(0xFF93A8B8)

val NeonLime = Color(0xFFB6FF3B)
val NeonPink = Color(0xFFFF3DF2)
val CombatOrange = Color(0xFFFF9F1C)
val HudLine = Color(0xFF244152)
val PanelDark = Color(0xFF0B141D)

val SuccessLight = Color(0xFF2ECC71)
val SuccessDark = Color(0xFF27AE60)
val ErrorLight = Color(0xFFE74C3C)
val ErrorDark = Color(0xFFC0392B)
val WarningLight = Color(0xFFF39C12)
val WarningDark = Color(0xFFE67E22)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = Color(0xFF001B22),
    primaryContainer = PrimaryContainerLight,
    onPrimaryContainer = TextPrimaryLight,
    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFF172838),
    onSurfaceVariant = TextSecondaryLight,
    error = ErrorLight,
    onError = Color.White,
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = Color(0xFF001B22),
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = TextPrimaryDark,
    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF172838),
    onSurfaceVariant = TextSecondaryDark,
    error = ErrorDark,
    onError = Color.White,
)

/**
 * CyberGym app theme.
 *
 * Material 3 with a neon gaming palette.
 */
@Composable
fun CyberGymTheme(
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
