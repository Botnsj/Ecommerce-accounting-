package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val PrivateDarkColorScheme = darkColorScheme(
    primary = DeepBlue,
    secondary = EmeraldGreen,
    tertiary = MetallicCyan,
    background = SlateMidnight,
    surface = DarkCharcoal,
    onPrimary = OffWhite,
    onSecondary = OffWhite,
    onBackground = OffWhite,
    onSurface = OffWhite,
    error = CoralRed
)

private val PrivateLightColorScheme = lightColorScheme(
    primary = DeepBlue,
    secondary = EmeraldGreen,
    tertiary = MetallicCyan,
    background = OffWhite,
    surface = androidx.compose.ui.graphics.Color.White,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onBackground = SlateMidnight,
    onSurface = SlateMidnight,
    error = CoralRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to gorgeous dark mode as requested for Tally + Stripe feel
    dynamicColor: Boolean = false, // Set to false to enforce our stunning branded SaaS styling
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) PrivateDarkColorScheme else PrivateLightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
