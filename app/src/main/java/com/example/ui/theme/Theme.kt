package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EdenColorScheme = darkColorScheme(
    primary = ElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = CosmicPurple,
    onPrimaryContainer = StarlightWhite,
    secondary = CelestialCyan,
    onSecondary = Color.Black,
    secondaryContainer = SurfaceDark,
    onSecondaryContainer = RadiantAqua,
    tertiary = NebulaViolet,
    onTertiary = Color.White,
    background = SpaceBlack,
    onBackground = StarlightWhite,
    surface = DeepNavy,
    onSurface = StarlightWhite,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = CosmicMuted,
    outline = SurfaceGlassBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EdenColorScheme,
        typography = Typography,
        content = content
    )
}
