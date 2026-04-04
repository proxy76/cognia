package com.cognia.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Neon Purple Palette ──────────────────────────────────────────────
val NeonPurple = Color(0xFFBB86FC)
val NeonPurpleBright = Color(0xFFD0BCFF)
val NeonPurpleDark = Color(0xFF7C4DFF)
val NeonViolet = Color(0xFF6200EE)
val NeonPink = Color(0xFFCF6679)
val NeonCyan = Color(0xFF03DAC6)
val NeonCyanDark = Color(0xFF018786)

// ── Extended Palette ────────────────────────────────────────────────
val NeonPurpleGlow = Color(0x40BB86FC)
val NeonPurpleSubtle = Color(0xFF9B6FD9)
val GlassSurface = Color(0x1AFFFFFF)
val GlassSurfaceStrong = Color(0x33FFFFFF)
val GlassBorder = Color(0x1AFFFFFF)
val VideoScrimTop = Color(0x66000000)
val VideoScrimBottom = Color(0xCC000000)

val SurfaceDark = Color(0xFF121220)
val SurfaceDarkElevated = Color(0xFF1C1C30)
val SurfaceDarkCard = Color(0xFF1E1E36)
val BackgroundDark = Color(0xFF0D0D1A)
val OnSurfaceLight = Color(0xFFE6E1E5)
val OnSurfaceDim = Color(0xFF938F99)
val OutlineDim = Color(0xFF2E2E48)

private val CogniaDarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = Color(0xFF1F0040),
    primaryContainer = Color(0xFF3700B3),
    onPrimaryContainer = NeonPurpleBright,
    secondary = NeonCyan,
    onSecondary = Color(0xFF003731),
    secondaryContainer = Color(0xFF004D43),
    onSecondaryContainer = Color(0xFF70F7EB),
    tertiary = NeonPink,
    onTertiary = Color(0xFF492532),
    tertiaryContainer = Color(0xFF633B48),
    onTertiaryContainer = Color(0xFFFFD8E4),
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF601410),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC),
    background = BackgroundDark,
    onBackground = OnSurfaceLight,
    surface = SurfaceDark,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceDarkCard,
    onSurfaceVariant = OnSurfaceDim,
    outline = OutlineDim,
    outlineVariant = Color(0xFF49454F),
    inverseSurface = Color(0xFFE6E1E5),
    inverseOnSurface = Color(0xFF1C1B1F),
    inversePrimary = NeonViolet,
    surfaceTint = NeonPurple,
)

private val CogniaShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

private val CogniaTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.25).sp,
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 42.sp,
    ),
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
)

@Composable
fun CogniaTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CogniaDarkColorScheme,
        shapes = CogniaShapes,
        typography = CogniaTypography,
        content = content
    )
}
