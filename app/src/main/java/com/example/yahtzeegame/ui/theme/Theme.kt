package com.example.yahtzeegame.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val Light = lightColorScheme(
    primary = PrimaryLight,
    onPrimary = TextPrimaryLight,
    primaryContainer = SurfaceLight,
    onPrimaryContainer = TextPrimaryLight,

    secondary = SecondaryLight,
    onSecondary = TextPrimaryLight,
    tertiary = AccentLight,
    onTertiary = TextPrimaryLight,

    background = BackgroundLight,
    onBackground = TextPrimaryLight,
    surface = SurfaceLight,
    onSurface = TextPrimaryLight
)

private val Dark = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = TextPrimaryDark,
    primaryContainer = SurfaceDark,
    onPrimaryContainer = TextPrimaryDark,

    secondary = SecondaryDark,
    onSecondary = TextPrimaryDark,
    tertiary = AccentDark,
    onTertiary = TextPrimaryDark,

    background = BackgroundDark,
    onBackground = TextPrimaryDark,
    surface = SurfaceDark,
    onSurface = TextPrimaryDark
)

@Composable
fun YahtzeeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> Dark
        else -> Light
    }
    MaterialTheme(
        colorScheme = colorScheme, typography = YahtzeeTypography, content = content
    )
}