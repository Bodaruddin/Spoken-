package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Premium International EdTech & Language Palette - Elevated & Distinctive
// Deep Sapphire & Electric Royal Blue
val RoyalBlue = Color(0xFF3B82F6)
val RoyalBlueDark = Color(0xFF1D4ED8)
val ElectricIndigo = Color(0xFF4F46E5)
val DeepNavy = Color(0xFF060919)
val NavySurface = Color(0xFF0F1535)
val NavyLight = Color(0xFF1E264F)

// Vibrant Accent Colors (Luminous & Harmonious)
val CoralAccent = Color(0xFFF43F5E)
val RoseDark = Color(0xFFBE123C)
val OrangeVibrant = Color(0xFFEA580C)
val AmberGold = Color(0xFFF59E0B)
val AmberDark = Color(0xFFD97706)
val EmeraldSuccess = Color(0xFF10B981)
val EmeraldDark = Color(0xFF059669)
val MintLight = Color(0xFFD1FAE5)
val MintContainer = Color(0xFFECFDF5)
val PurpleAccent = Color(0xFF8B5CF6)
val PurpleDark = Color(0xFF6D28D9)
val VioletContainer = Color(0xFFF5F3FF)
val SoftPink = Color(0xFFEC4899)
val CyanAccent = Color(0xFF06B6D4)
val CyanContainer = Color(0xFFE0F2FE)

// Light & Glass Surfaces
val SoftCream = Color(0xFFF8FAFC)
val SoftCreamSurface = Color(0xFFFFFFFF)
val MutedGray = Color(0xFF94A3B8)
val LightBorder = Color(0xFF263366)
val DarkBorder = Color(0xFF263366)

// Gamification Level Colors
val LeagueBronze = Color(0xFFCD7F32)
val LeagueSilver = Color(0xFF94A3B8)
val LeagueGold = Color(0xFFF59E0B)
val LeagueEmerald = Color(0xFF10B981)
val LeagueDiamond = Color(0xFF38BDF8)

// Premium Design Gradients
val PrimaryGrad = Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF2563EB)))
val RoyalSkyGrad = Brush.linearGradient(listOf(Color(0xFF2563EB), Color(0xFF06B6D4)))
val PurpleIrisGrad = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6366F1)))
val EmeraldTealGrad = Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
val AmberSunsetGrad = Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C)))
val RoseCoralGrad = Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFE11D48)))
val CyanGrad = Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF0284C7)))
val SlateCardGrad = Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)))
val WarmGlowGrad = Brush.linearGradient(listOf(Color(0xFFFEF3C7), Color(0xFFFDE68A)))

// Premium Cyber & Midnight Indigo Palette - Enhanced Contrast & Depth
val CyberMidnightBg = Color(0xFF090E26)
val CyberNavySurface = Color(0xFF141F48)
val CyberCardBg = Color(0xFF162352)
val CyberCardBorder = Color(0xFF2C3E75)
val CyberCardBorderBright = Color(0xFF38BDF8)

// Screenshot Card Gradients - 4 Quadrants
val AISpeakingCardGrad = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF027DFE), // Vibrant Electric Azure Blue
        Color(0xFF0152D6),
        Color(0xFF0369E8)
    )
)
val RoleplaysCardGrad = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF8C1BFA), // Deep Radiant Magenta-Purple
        Color(0xFF5D12D9),
        Color(0xFF700AE6)
    )
)
val PhoneticsCardGrad = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFF9500), // Vibrant Bright Amber-Orange
        Color(0xFFFD5E3A),
        Color(0xFFFF5252)
    )
)
val MiniGamesCardGrad = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF04C097), // Luminous Emerald Teal
        Color(0xFF028A7A),
        Color(0xFF04BA9A)
    )
)

val HeroBannerGrad = Brush.linearGradient(
    colors = listOf(
        Color(0xFF5A15C8).copy(alpha = 0.90f),
        Color(0xFF8020E8).copy(alpha = 0.85f),
        Color(0xFF5814C4).copy(alpha = 0.90f)
    )
)
val HeroBannerBorder = Color(0xFFB565FF).copy(alpha = 0.8f)

val TasksContainerGrad = Brush.linearGradient(
    colors = listOf(
        Color(0xFF111E58).copy(alpha = 0.85f),
        Color(0xFF0E1A4E).copy(alpha = 0.85f)
    )
)
val TasksContainerBorder = Color(0xFF3860C8).copy(alpha = 0.7f)

val BottomBarGrad = Brush.verticalGradient(
    listOf(Color(0xFF18154E).copy(alpha = 0.95f), Color(0xFF0C0A2E).copy(alpha = 0.98f))
)
val BottomBarBorder = Color(0xFF5046B8).copy(alpha = 0.7f)

val NeonBlueCardBg = Color(0xFF12234D)
val NeonBlueBorder = Color(0xFF38BDF8)
val NeonBlueGrad = Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF2563EB)))

val NeonPurpleCardBg = Color(0xFF24184C)
val NeonPurpleBorder = Color(0xFFA855F7)
val NeonPurpleGrad = Brush.linearGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED)))

val NeonGreenCardBg = Color(0xFF0C2B24)
val NeonGreenBorder = Color(0xFF10B981)
val NeonGreenGrad = Brush.linearGradient(listOf(Color(0xFF34D399), Color(0xFF059669)))

val NeonAmberCardBg = Color(0xFF2E2010)
val NeonAmberBorder = Color(0xFFF59E0B)
val NeonAmberGrad = Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFEA580C)))

val NeonCyanCardBg = Color(0xFF0F2746)
val NeonCyanBorder = Color(0xFF06B6D4)
val NeonCyanGrad = Brush.linearGradient(listOf(Color(0xFF22D3EE), Color(0xFF0284C7)))

val NeonPinkCardBg = Color(0xFF2E1329)
val NeonPinkBorder = Color(0xFFEC4899)
val NeonPinkGrad = Brush.linearGradient(listOf(Color(0xFFF472B6), Color(0xFFBE185D)))
