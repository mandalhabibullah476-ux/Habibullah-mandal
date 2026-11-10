package com.example.ui.theme

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
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = PrimaryOrangeDark,
    onPrimaryContainer = PrimaryOrangeLight,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = SecondaryTealDark,
    onSecondaryContainer = SecondaryTealLight,
    tertiary = AccentAmber,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color(0xFFF1F1F1),
    onSurface = Color(0xFFF1F1F1)
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryOrange,
    onPrimary = Color.White,
    primaryContainer = PrimaryOrangeLight,
    onPrimaryContainer = PrimaryOrangeDark,
    secondary = SecondaryTeal,
    onSecondary = Color.White,
    secondaryContainer = SecondaryTealLight,
    onSecondaryContainer = SecondaryTealDark,
    tertiary = AccentAmber,
    background = SurfaceWarm,
    surface = Color.White,
    surfaceVariant = NeutralLightGrey,
    onBackground = NeutralDark,
    onSurface = NeutralDark
)

@Composable
fun OmniFeastTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our signature food & grocery palette by default
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

