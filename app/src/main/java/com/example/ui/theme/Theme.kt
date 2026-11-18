package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val KotaKalongColorScheme = darkColorScheme(
    primary = MintAccent,
    onPrimary = OnMintAccent,
    primaryContainer = MintAccentContainer,
    onPrimaryContainer = MintAccent,
    secondary = AmberBadge,
    onSecondary = TextPrimary,
    secondaryContainer = CategoryPillBg,
    onSecondaryContainer = CategoryPillText,
    tertiary = AmberStar,
    background = DarkCanvas,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceBorder,
    outlineVariant = EmeraldPillBorder,
    error = DangerRed,
    onError = TextPrimary,
    errorContainer = DangerRedBg
)

@Composable
fun KotaKalongTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = KotaKalongColorScheme,
        typography = AppTypography,
        content = content
    )
}
