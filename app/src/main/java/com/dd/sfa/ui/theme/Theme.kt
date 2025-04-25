package com.dd.sfa.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF1E1E1E),         // Dark primary color
    onPrimary = Color(0xFFECEDEE),       // Text / Icons
    secondary = Color(0xFF333333),       // Primary color
    onSecondary = Color(0xFFBABABA),
    tertiary = Color(0xFF446A7C),        // Accent color
    onTertiary = Color(0xFFECEDEE),
    background = Color(0xFF000000),
    onBackground = Color(0xFFECEDEE),
    surface = Color(0xFF333333),
    onSurface = Color(0xFFECEDEE),
    outline = Color(0xFF808080),          // Divider color
    error = Color(0xFFD32F2F)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F4F4F),         // Light primary color
    onPrimary = Color(0xFFE4E4E4),        // Primary text
    secondary = Color(0xFF333333),        // Primary color
    onSecondary = Color(0xFFBABABA),       // Secondary text
    tertiary = Color(0xFF446A7C),         // Accent color
    onTertiary = Color(0xFFE4E4E4),
    background = Color(0xFF4F4F4F),
    onBackground = Color(0xFFE4E4E4),
    surface = Color(0xFF333333),
    onSurface = Color(0xFFE4E4E4),
    outline = Color(0xFF808080),          // Divider color
    error = Color(0xFFD32F2F)
)

@Composable
fun SFATheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDarkTheme = when (themeMode) {
        // DarkTheme is default, use isSystemInDarkTheme() for dynamic theme
        ThemeMode.SYSTEM -> true //isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (isDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        isDarkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
