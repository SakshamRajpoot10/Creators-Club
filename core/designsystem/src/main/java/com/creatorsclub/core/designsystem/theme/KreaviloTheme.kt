package com.creatorsclub.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val KreaviloLightColorScheme = lightColorScheme(
    primary = KreaviloColors.Primary,
    onPrimary = KreaviloColors.OnPrimary,
    primaryContainer = KreaviloColors.PrimaryContainer,
    onPrimaryContainer = KreaviloColors.Primary,
    secondary = KreaviloColors.Accent,
    onSecondary = KreaviloColors.OnAccent,
    secondaryContainer = KreaviloColors.AccentContainer,
    onSecondaryContainer = KreaviloColors.Accent,
    background = KreaviloColors.Background,
    onBackground = KreaviloColors.TextPrimary,
    surface = KreaviloColors.Surface,
    onSurface = KreaviloColors.TextPrimary,
    surfaceVariant = KreaviloColors.SurfaceVariant,
    onSurfaceVariant = KreaviloColors.TextSecondary,
    outline = KreaviloColors.Border,
    error = KreaviloColors.Error
)

@Composable
fun KreaviloTheme(
    content: @Composable () -> Unit
) {
    // Initial public release is strictly Light Theme as mandated by non-negotiable rule #22
    MaterialTheme(
        colorScheme = KreaviloLightColorScheme,
        typography = KreaviloTypography,
        content = content
    )
}
