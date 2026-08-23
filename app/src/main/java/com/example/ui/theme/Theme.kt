package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = LudoPrimary,
    secondary = LudoSecondary,
    tertiary = LudoTertiary,
    background = LudoBackground,
    surface = LudoSurface,
    onPrimary = LudoOnPrimary,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = LudoOnBackground,
    onSurface = LudoOnSurface
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

