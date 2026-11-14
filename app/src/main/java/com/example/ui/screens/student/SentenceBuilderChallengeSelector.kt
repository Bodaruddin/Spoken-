package com.example.ui.screens.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SentenceBuilderChallenge
import com.example.data.model.SentenceBuilderPack

private data class SentenceTierVisualStyle(
    val accentPrimary: Color,
    val accentSecondary: Color,
    val cardSurfaceStart: Color,
    val cardSurfaceEnd: Color,
    val iconPodBg: Color,
    val tierLabel: String,
    val icon: ImageVector,
    val starCount: Int
)

private fun resolveSentenceTierVisualStyle(
    pack: SentenceBuilderPack,
    index: Int
): SentenceTierVisualStyle {
    return when (pack.difficulty.trim().lowercase()) {
        "easy" -> SentenceTierVisualStyle(
            accentPrimary = Color(0xFF34D399),
            accentSecondary = Color(0xFF059669),
            cardSurfaceStart = Color(0xFF0D2D2A),
            cardSurfaceEnd = Color(0xFF140D34),
            iconPodBg = Color(0xFF093B31),
            tierLabel = "EASY",
            icon = Icons.Rounded.AutoAwesome,
            starCount = 1
        )
        "medium" -> SentenceTierVisualStyle(
            accentPrimary = Color(0xFF38BDF8),
            accentSecondary = Color(0xFF2563EB),
            cardSurfaceStart = Color(0xFF0D2349),
            cardSurfaceEnd = Color(0xFF150E39),
            iconPodBg = Color(0xFF0C315E),
            tierLabel = "MEDIUM",
            icon = Icons.Rounded.Bolt,
            starCount = 2
        )
        "hard" -> SentenceTierVisualStyle(
            accentPrimary = Color(0xFFC084FC),
            accentSecondary = Color(0xFF9333EA),
            cardSurfaceStart = Color(0xFF2B124D),
            cardSurfaceEnd = Color(0xFF150B33),
            iconPodBg = Color(0xFF3B1569),
            tierLabel = "HARD",
            icon = Icons.Rounded.WorkspacePremium,
            starCount = 3
        )
        else -> when (index % 3) {
            0 -> SentenceTierVisualStyle(
                accentPrimary = Color(0xFF2DD4BF),
                accentSecondary = Color(0xFF0D9488),
                cardSurfaceStart = Color(0xFF0E2C30),
                cardSurfaceEnd = Color(0xFF140D34),
                iconPodBg = Color(0xFF0C3B3E),
                tierLabel = pack.difficulty.ifBlank { "CUSTOM" }.uppercase(),
                icon = Icons.Rounded.AutoAwesome,
                starCount = 1
            )
            1 -> SentenceTierVisualStyle(
                accentPrimary = Color(0xFFFBBF24),
                accentSecondary = Color(0xFFEA580C),
                cardSurfaceStart = Color(0xFF2E1E16),
                cardSurfaceEnd = Color(0xFF160E35),
                iconPodBg = Color(0xFF422713),
                tierLabel = pack.difficulty.ifBlank { "SPECIAL" }.uppercase(),
                icon = Icons.Rounded.Bolt,
                starCount = 2
            )
            else -> SentenceTierVisualStyle(
                accentPrimary = Color(0xFFF472B6),
                accentSecondary = Color(0xFFDB2777),
                cardSurfaceStart = Color(0xFF311234),
                cardSurfaceEnd = Color(0xFF160B33),
                iconPodBg = Color(0xFF461547),
                tierLabel = pack.difficulty.ifBlank { "MASTER" }.uppercase(),
                icon = Icons.Rounded.WorkspacePremium,
                starCount = 3
            )
        }
    }
}

@Composable
fun SentenceBuilderChallengeSelector(
    packs: List<SentenceBuilderPack>,
    allChallenges: List<SentenceBuilderChallenge>,
    xpPoints: Int,
    onBack: () -> Unit,
    onSelectPack: (SentenceBuilderPack) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 640.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Clean Top Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1F1246),
                        border = BorderStroke(1.2.dp, Color(0xFF8B5CF6).copy(alpha = 0.6f)),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onBack() }
                            .testTag("sb_back_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back to Games Hub",
                                tint = Color.White,
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "Sentence Builder",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Grammar & Word Order",
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = Color(0xFFC4B5FD),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Sleek XP Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1A0F3D),
                    border = BorderStroke(1.2.dp, Color(0xFFFACC15).copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = Color(0xFFFACC15),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "$xpPoints XP",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // 2. Unified Compact Cosmic Glass Hero Card
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent,
                border = BorderStroke(
                    width = 1.4.dp,
                    brush = Brush.linearGradient(
                        listOf(
                            Color(0xFF8B5CF6).copy(alpha = 0.75f),
                            Color(0xFF06B6D4).copy(alpha = 0.65f),
                            Color(0xFFEC4899).copy(alpha = 0.6f)
                        )
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF23135A).copy(alpha = 0.94f),
                                    Color(0xFF150B3B).copy(alpha = 0.96f),
                                    Color(0xFF0E0829).copy(alpha = 0.98f)
                                )
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Glowing Puzzle Emblem
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF7C3AED),
                                        Color(0xFF06B6D4)
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = Color.White.copy(alpha = 0.28f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Extension,
                            contentDescription = "Sentence Builder Puzzle",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFACC15).copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.55f)),
                                modifier = Modifier.testTag("sb_choose_challenge_badge")
                            ) {
                                Text(
                                    text = "Choose a Challenge",
                                    color = Color(0xFFFDE047),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }

                            Text(
                                text = "${packs.size} ${if (packs.size == 1) "Tier" else "Tiers"} • ${allChallenges.size} Sentences",
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = "Build Perfect Sentences",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "Arrange scrambled words into natural English to earn XP.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 16.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 3. Section Header
            if (packs.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHALLENGE TIERS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Color(0xFFA78BFA)
                    )
                    Text(
                        text = "Tap any card to play",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            // 4. Challenge Cards or Empty State
            if (packs.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF170F38)),
                    border = BorderStroke(1.2.dp, Color(0xFF6366F1).copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF28175A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Extension,
                                contentDescription = null,
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Text(
                            text = "No Sentence Challenges Created Yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Teachers can add challenges and sentences from the Teacher Panel.",
                            fontSize = 12.5.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .testTag("sb_challenge_packs_list")
                ) {
                    packs.forEachIndexed { index, pack ->
                        val sentencesCount = allChallenges.count { it.packId == pack.id }
                        val style = resolveSentenceTierVisualStyle(pack, index)

                        SentenceChallengeTierCard(
                            pack = pack,
                            sentencesCount = sentencesCount,
                            style = style,
                            onClick = { onSelectPack(pack) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SentenceChallengeTierCard(
    pack: SentenceBuilderPack,
    sentencesCount: Int,
    style: SentenceTierVisualStyle,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.3.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    style.accentPrimary.copy(alpha = 0.85f),
                    style.accentSecondary.copy(alpha = 0.4f),
                    Color.White.copy(alpha = 0.14f)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 96.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("sb_pack_card_${pack.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            style.cardSurfaceStart,
                            style.cardSurfaceEnd
                        )
                    )
                )
        ) {
            // Crisp left vertical neon tier bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        Brush.verticalGradient(
                            listOf(style.accentPrimary, style.accentSecondary)
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                // Tier Icon Pod
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = style.iconPodBg,
                    border = BorderStroke(1.2.dp, style.accentPrimary.copy(alpha = 0.5f)),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = style.icon,
                            contentDescription = null,
                            tint = style.accentPrimary,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }

                // Center Details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Difficulty badge + 3-star meter
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = style.accentPrimary.copy(alpha = 0.16f),
                            border = BorderStroke(0.8.dp, style.accentPrimary.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = style.tierLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.6.sp,
                                color = style.accentPrimary,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            for (starIdx in 0 until 3) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = null,
                                    tint = if (starIdx < style.starCount) {
                                        Color(0xFFFACC15)
                                    } else {
                                        Color.White.copy(alpha = 0.2f)
                                    },
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = pack.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.5.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = pack.description,
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )

                    // Metadata chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 3.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF09061C).copy(alpha = 0.65f),
                            border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.14f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.FormatListBulleted,
                                    contentDescription = null,
                                    tint = style.accentPrimary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "$sentencesCount ${if (sentencesCount == 1) "Sentence" else "Sentences"}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF09061C).copy(alpha = 0.65f),
                            border = BorderStroke(0.8.dp, Color(0xFFFACC15).copy(alpha = 0.38f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFFACC15),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "+${pack.xpReward} XP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDE047)
                                )
                            }
                        }
                    }
                }

                // Right Gradient CTA Circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(style.accentPrimary, style.accentSecondary)
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.28f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                        contentDescription = "Start ${pack.title}",
                        tint = Color.White,
                        modifier = Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}
