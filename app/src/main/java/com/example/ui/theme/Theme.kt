package com.example.ui.theme

import android.graphics.BitmapFactory
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.example.R
import kotlin.math.roundToInt

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
    background = Color.Transparent,
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
        typography = Typography
    ) {
        CompositionLocalProvider(
            LocalContentColor provides Color.White
        ) {
            Surface(
                color = Color.Transparent,
                contentColor = Color.White,
                content = content
            )
        }
    }
}

/**
 * Premium purple-navy cyber-glass ambient background modifier.
 * Draws the user's exact uploaded wallpaper image with vibrant neon violet waves and deep navy gradient.
 */
@Composable
fun Modifier.premiumAppBackground(): Modifier {
    val context = LocalContext.current
    val imageBitmap = remember {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.premium_purple_navy_bg)
        bitmap?.asImageBitmap()
    }

    return this.drawBehind {
        val w = size.width
        val h = size.height

        if (imageBitmap != null) {
            // Draw wallpaper with ContentScale.Crop logic
            val srcW = imageBitmap.width.toFloat()
            val srcH = imageBitmap.height.toFloat()
            val scale = maxOf(w / srcW, h / srcH)
            val scaledW = srcW * scale
            val scaledH = srcH * scale
            val offsetX = (w - scaledW) / 2f
            val offsetY = (h - scaledH) / 2f

            drawImage(
                image = imageBitmap,
                dstOffset = IntOffset(offsetX.roundToInt(), offsetY.roundToInt()),
                dstSize = IntSize(scaledW.roundToInt(), scaledH.roundToInt())
            )
        } else {
            // High-fidelity fallback gradient matching the exact purple navy artwork
            drawRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF16004B),
                        Color(0xFF080D36),
                        Color(0xFF020B2E),
                        Color(0xFF100044)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(w, h)
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8A00FF).copy(alpha = 0.85f),
                        Color(0xFF5200B8).copy(alpha = 0.45f),
                        Color.Transparent
                    ),
                    center = Offset(0f, 0f),
                    radius = w * 0.85f
                )
            )
            drawRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8700FF).copy(alpha = 0.8f),
                        Color(0xFF4A00A8).copy(alpha = 0.35f),
                        Color.Transparent
                    ),
                    center = Offset(w, h),
                    radius = w * 0.9f
                )
            )
        }
    }
}

fun Modifier.premiumAppBackground(isDark: Boolean): Modifier = this
