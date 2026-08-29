package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val SleekColorScheme = darkColorScheme(
    primary = Blue400,
    onPrimary = SleekBg,
    primaryContainer = SleekElevated,
    onPrimaryContainer = Blue400,
    secondary = Indigo600,
    onSecondary = Slate100,
    secondaryContainer = SleekSurfaceVariant,
    onSecondaryContainer = IceBlue,
    tertiary = SleekMint,
    onTertiary = SleekBg,
    background = SleekBg,
    onBackground = Slate100,
    surface = SleekSurface,
    onSurface = Slate100,
    surfaceVariant = SleekSurfaceVariant,
    onSurfaceVariant = Slate400,
    error = SleekErrorRed,
    onError = Slate100
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SleekColorScheme,
        typography = Typography,
        content = content
    )
}


