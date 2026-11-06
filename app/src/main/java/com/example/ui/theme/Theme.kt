package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFBFDBFE),
    secondary = PurpleAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF581C87),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = AmberGold,
    onTertiary = DeepNavy,
    background = Color(0xFF090D16),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF131A2B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF2E3E5C)
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = Color(0xFF312E81),
    secondary = PurpleAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFF5F3FF),
    onSecondaryContainer = Color(0xFF581C87),
    tertiary = CoralAccent,
    onTertiary = Color.White,
    background = Color(0xFFF4F6FB),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFEEF2F6),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFE2E8F0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set default to false to showcase our custom brand palette consistently
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

/**
 * Premium ambient lighting background modifier.
 * Creates an atmospheric multi-stop gradient with soft luminous ambient glows
 * at key focal points for a distinctive, high-end feel.
 */
fun Modifier.premiumAppBackground(isDark: Boolean = false): Modifier = this.drawBehind {
    val width = size.width
    val height = size.height

    if (isDark) {
        // Deep obsidian luxury midnight gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0D121F),
                    Color(0xFF090D16),
                    Color(0xFF05080E)
                )
            )
        )
        // Sapphire ambient aura (top-right)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF1E3A8A).copy(alpha = 0.40f), Color.Transparent),
                center = Offset(width * 0.85f, height * 0.08f),
                radius = width * 0.85f
            )
        )
        // Electric indigo ambient aura (top-left)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF4338CA).copy(alpha = 0.30f), Color.Transparent),
                center = Offset(width * 0.15f, height * 0.20f),
                radius = width * 0.70f
            )
        )
        // Soft violet ambient aura (bottom-left)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF581C87).copy(alpha = 0.22f), Color.Transparent),
                center = Offset(width * 0.10f, height * 0.75f),
                radius = width * 0.65f
            )
        )
    } else {
        // High-end porcelain silk gradient base
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFF1F5FD), // Radiant sky/indigo top glow
                    Color(0xFFF7F9FD), // Transition
                    Color(0xFFF4F6FB), // Mid-body luxury pearl
                    Color(0xFFF0F4FA)  // Soft grounding finish
                )
            )
        )
        // Luminous Electric Indigo aura in top-left
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF6366F1).copy(alpha = 0.15f), Color.Transparent),
                center = Offset(width * 0.12f, height * 0.04f),
                radius = width * 0.85f
            )
        )
        // Radiant Cyan/Sapphire ambient beam in top-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF38BDF8).copy(alpha = 0.15f), Color.Transparent),
                center = Offset(width * 0.90f, height * 0.14f),
                radius = width * 0.75f
            )
        )
        // Subtle warm rose/amber ambient kiss near center-right
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFBCFE8).copy(alpha = 0.15f), Color.Transparent),
                center = Offset(width * 0.85f, height * 0.55f),
                radius = width * 0.60f
            )
        )
    }
}

@Composable
fun Modifier.premiumAppBackground(): Modifier {
    val isDark = isSystemInDarkTheme()
    return this.premiumAppBackground(isDark)
}
