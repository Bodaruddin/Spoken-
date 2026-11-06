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
    primaryContainer = Color(0xFF1E2E68),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary = PurpleAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3E2375),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = AmberGold,
    onTertiary = Color.Black,
    background = CyberMidnightBg,
    onBackground = Color.White,
    surface = CyberNavySurface,
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1A285A),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = CyberCardBorder,
    outlineVariant = Color(0xFF233260)
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
 * Premium cyber-glass ambient background modifier.
 * Creates deep midnight blue space with luminous wave gradients and subtle ambient highlights.
 */
fun Modifier.premiumAppBackground(isDark: Boolean = true): Modifier = this.drawBehind {
    val width = size.width
    val height = size.height

    // Deep rich midnight sapphire & royal slate vertical gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF090E26), // Top: rich deep atmospheric navy
                Color(0xFF0D173C), // Upper-mid: deep midnight sapphire
                Color(0xFF111E4C), // Center: luminous royal slate
                Color(0xFF0B112D)  // Bottom: grounded deep indigo
            )
        )
    )
    // Soft luminous royal sapphire aura in upper right
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF2563EB).copy(alpha = 0.22f), Color.Transparent),
            center = Offset(width * 0.90f, height * 0.05f),
            radius = width * 0.95f
        )
    )
    // Soft electric purple bloom in top left
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF6366F1).copy(alpha = 0.16f), Color.Transparent),
            center = Offset(width * 0.10f, height * 0.20f),
            radius = width * 0.85f
        )
    )
    // Delicate cyan glow along mid-left edge
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF0EA5E9).copy(alpha = 0.12f), Color.Transparent),
            center = Offset(width * 0.05f, height * 0.55f),
            radius = width * 0.75f
        )
    )
    // Subtle magenta warmth in lower right
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFEC4899).copy(alpha = 0.10f), Color.Transparent),
            center = Offset(width * 0.92f, height * 0.75f),
            radius = width * 0.70f
        )
    )
}

@Composable
fun Modifier.premiumAppBackground(): Modifier = this.premiumAppBackground(isDark = true)
