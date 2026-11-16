package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = Color.Black,
    primaryContainer = GoldDark,
    onPrimaryContainer = Color.White,
    secondary = PinkGem,
    onSecondary = Color.White,
    secondaryContainer = BlueDiamond,
    onSecondaryContainer = Color.Black,
    tertiary = GreenHashRate,
    onTertiary = Color.Black,
    background = CaveDarkBg,
    onBackground = TextWhite,
    surface = CaveCardBg,
    onSurface = TextWhite,
    surfaceVariant = CaveCardStroke,
    onSurfaceVariant = TextMuted
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GameColorScheme,
        typography = Typography,
        content = content
    )
}
