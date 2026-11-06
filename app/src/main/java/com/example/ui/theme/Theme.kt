package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val CyberDarkColorScheme = darkColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E2856),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary = PurpleAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF381E68),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = AmberGold,
    onTertiary = DeepNavy,
    background = CyberMidnightBg,
    onBackground = Color.White,
    surface = CyberNavySurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF161F48),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = CyberCardBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CyberDarkColorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Premium cyber-glass ambient background modifier directly matched to screenshot.
 * Creates deep midnight blue space with luminous wave gradients and subtle ambient highlights.
 */
fun Modifier.premiumAppBackground(isDark: Boolean = true): Modifier = this.drawBehind {
    val width = size.width
    val height = size.height

    // Deep midnight base gradient (#060919 to #0B112C to #070A1E)
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF060919),
                Color(0xFF090E26),
                Color(0xFF0B112C),
                Color(0xFF070A1E)
            )
        )
    )
    // Luminous Electric Sapphire glow in upper right
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF1D4ED8).copy(alpha = 0.45f), Color.Transparent),
            center = Offset(width * 0.90f, height * 0.05f),
            radius = width * 0.90f
        )
    )
    // Deep Violet/Indigo ambient bloom in top-left
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF4F46E5).copy(alpha = 0.35f), Color.Transparent),
            center = Offset(width * 0.10f, height * 0.18f),
            radius = width * 0.80f
        )
    )
    // Cyan soft aura along center-left
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.20f), Color.Transparent),
            center = Offset(width * 0.05f, height * 0.50f),
            radius = width * 0.70f
        )
    )
    // Magenta/Rose glow in lower right
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFEC4899).copy(alpha = 0.18f), Color.Transparent),
            center = Offset(width * 0.92f, height * 0.72f),
            radius = width * 0.70f
        )
    )
}

@Composable
fun Modifier.premiumAppBackground(): Modifier = this.premiumAppBackground(isDark = true)
