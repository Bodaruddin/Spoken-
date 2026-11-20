package com.example.ui.screens.student

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BusinessCenter
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SpeakingPrompt
import com.example.data.model.SpeakingWeek
import com.example.ui.MainViewModel
import com.example.ui.StudentTab

private data class TopicCardVisualStyle(
    val ringGlow: Color,
    val innerStart: Color,
    val innerEnd: Color,
    val cardBorderTint: Color,
    val icon: ImageVector
)

private fun resolveTopicCardStyle(
    topic: SpeakingPrompt,
    index: Int,
    isFluencyBooster: Boolean
): TopicCardVisualStyle {
    val categoryLower = topic.category.lowercase()
    val semanticIcon = when {
        isFluencyBooster -> Icons.Rounded.Psychology
        categoryLower.contains("business") ||
            categoryLower.contains("career") ||
            categoryLower.contains("professional") ||
            categoryLower.contains("work") -> Icons.Rounded.BusinessCenter
        categoryLower.contains("college") ||
            categoryLower.contains("academic") ||
            categoryLower.contains("study") -> Icons.Rounded.School
        else -> Icons.AutoMirrored.Rounded.MenuBook
    }

    return when (index % 4) {
        0 -> TopicCardVisualStyle(
            ringGlow = Color(0xFF00E5FF),
            innerStart = Color(0xFF0EA5E9),
            innerEnd = Color(0xFF0284C7),
            cardBorderTint = Color(0xFF38BDF8),
            icon = semanticIcon
        )
        1 -> TopicCardVisualStyle(
            ringGlow = Color(0xFFA855F7),
            innerStart = Color(0xFF9333EA),
            innerEnd = Color(0xFF6D28D9),
            cardBorderTint = Color(0xFFA855F7),
            icon = semanticIcon
        )
        2 -> TopicCardVisualStyle(
            ringGlow = Color(0xFFF59E0B),
            innerStart = Color(0xFFF59E0B),
            innerEnd = Color(0xFFEA580C),
            cardBorderTint = Color(0xFFFBBF24),
            icon = semanticIcon
        )
        else -> TopicCardVisualStyle(
            ringGlow = Color(0xFF10B981),
            innerStart = Color(0xFF10B981),
            innerEnd = Color(0xFF0D9488),
            cardBorderTint = Color(0xFF2DD4BF),
            icon = semanticIcon
        )
    }
}

/**
 * ConversationTopicsRoadmapScreen
 *
 * Cosmic glass speaking roadmap matching the user's reference mockup:
 * - Transparent ambient overlay letting the cosmic purple-navy theme shine through.
 * - Top bar with streak counter, vertical divider, glowing Random Topic pill, and profile shortcut.
 * - Illuminated Section 1 / Week 1 hero card with Nova AI robot banner art.
 * - Full-width dark-glass topic cards with glowing neon circular icon badges and chevron buttons.
 * - Floating glowing "Nova AI" pill in the bottom-right corner.
 */
@Composable
fun ConversationTopicsRoadmapScreen(
    viewModel: MainViewModel,
    onTopicSelected: (SpeakingPrompt) -> Unit,
    onOpenCoachDirectly: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prompts by viewModel.speakingPrompts.collectAsState()
    val weeks by viewModel.speakingWeeks.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()

    var selectedWeekNumber by rememberSaveable { mutableIntStateOf(1) }
    LaunchedEffect(weeks) {
        if (weeks.isNotEmpty() && weeks.none { it.weekNumber == selectedWeekNumber }) {
            selectedWeekNumber = weeks.first().weekNumber
        }
    }

    val activeWeek = remember(weeks, selectedWeekNumber) {
        weeks.firstOrNull { it.weekNumber == selectedWeekNumber }
            ?: weeks.firstOrNull()
            ?: SpeakingWeek(weekNumber = 1, title = "Week 1")
    }
    val weekPrompts = remember(prompts, activeWeek) {
        prompts.filter { it.weekNumber == activeWeek.weekNumber }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF09061E).copy(alpha = 0.55f),
                        Color(0xFF0A0924).copy(alpha = 0.72f),
                        Color(0xFF060416).copy(alpha = 0.85f)
                    )
                )
            )
            .testTag("conversation_topics_roadmap_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .fillMaxSize()
            ) {
                Spacer(Modifier.height(8.dp))

                // 1. Top Header Bar: Streak | Random Topic | Profile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.testTag("streak_indicator")
                        ) {
                            Text(text = "🔥", fontSize = 22.sp)
                            Text(
                                text = "${studentProfile.streakDays}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFA726)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(22.dp)
                                .background(Color.White.copy(alpha = 0.16f))
                        )
                    }

                    // Glowing Random Topic Pill
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Transparent,
                        border = BorderStroke(
                            1.2.dp,
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFA855F7).copy(alpha = 0.85f),
                                    Color(0xFF6366F1).copy(alpha = 0.85f)
                                )
                            )
                        ),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .defaultMinSize(minHeight = 40.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable {
                                val randomTopic = viewModel.selectRandomConversationTopic()
                                if (randomTopic != null) {
                                    onTopicSelected(randomTopic)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF2D156A).copy(alpha = 0.92f),
                                            Color(0xFF1B1048).copy(alpha = 0.95f)
                                        )
                                    )
                                )
                                .padding(horizontal = 16.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            Text("🎲", fontSize = 14.sp)
                            Text(
                                text = "Random Topic",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Profile Shortcut Circle
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF151A3B).copy(alpha = 0.9f),
                        border = BorderStroke(1.2.dp, Color(0xFF39467A)),
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable { viewModel.selectStudentTab(StudentTab.PROFILE) }
                            .testTag("roadmap_profile_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Profile",
                                tint = Color(0xFFE2E8F0),
                                modifier = Modifier.size(21.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))

                // 2. Hero Section Banner Card: "SECTION 1 / Week 1" + Nova AI Waving Robot
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = Color(0xFF170B3D),
                    border = BorderStroke(
                        1.4.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFC084FC).copy(alpha = 0.88f),
                                Color(0xFF6366F1).copy(alpha = 0.7f),
                                Color(0xFFEC4899).copy(alpha = 0.72f)
                            )
                        )
                    ),
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(132.dp)
                        .testTag("roadmap_section_card")
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_week1_nova_hero),
                            contentDescription = "Nova AI Coach Banner",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Left-to-right scrim ensuring crisp text readability over artwork
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        0.0f to Color(0xFF140736).copy(alpha = 0.86f),
                                        0.58f to Color(0xFF180A42).copy(alpha = 0.62f),
                                        1.0f to Color.Transparent
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.68f)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.dp, Color(0xFFD8B4FE).copy(alpha = 0.55f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF9333EA), Color(0xFF6366F1))
                                            )
                                        )
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = activeWeek.sectionLabel.ifBlank { "SECTION ${activeWeek.weekNumber}" },
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        letterSpacing = 0.8.sp
                                    )
                                }
                            }

                            Text(
                                text = activeWeek.title.ifBlank { "Week ${activeWeek.weekNumber}" },
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "🚀 ${activeWeek.subtitle.ifBlank { "Tap any topic to speak with Nova AI Coach" }}",
                                fontSize = 11.5.sp,
                                color = Color(0xFFE2E8F0),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Dynamic Week Challenge Switcher (appears when teacher adds Week 2, Week 3, etc.)
                if (weeks.size > 1) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .testTag("student_week_selector_row"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        weeks.forEach { wk ->
                            val isSelected = wk.weekNumber == activeWeek.weekNumber
                            val countInWeek = prompts.count { it.weekNumber == wk.weekNumber }
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color.Transparent,
                                border = BorderStroke(
                                    width = 1.2.dp,
                                    color = if (isSelected) Color(0xFFFACC15).copy(alpha = 0.85f) else Color(0xFF313E75)
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectedWeekNumber = wk.weekNumber }
                                    .testTag("student_week_chip_${wk.weekNumber}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .background(
                                            if (isSelected) {
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF4F46E5), Color(0xFF7C3AED))
                                                )
                                            } else {
                                                Brush.horizontalGradient(
                                                    listOf(
                                                        Color(0xFF12183A).copy(alpha = 0.9f),
                                                        Color(0xFF0C1028).copy(alpha = 0.9f)
                                                    )
                                                )
                                            }
                                        )
                                        .padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = wk.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = Color.White,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) Color(0xFFFACC15).copy(alpha = 0.22f) else Color.White.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "$countInWeek",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color(0xFFFDE047) else Color(0xFFCBD5E1),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 3. Vertical List of Cosmic Glass Topic Cards
                if (weekPrompts.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF13193B).copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🚀", fontSize = 28.sp)
                            Text(
                                text = "Topics Coming Soon for ${activeWeek.title}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Your teacher hasn't published topics to this week yet.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(bottom = 86.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(
                            weekPrompts,
                            key = { _, item -> "${item.id}_${item.title}_${item.question}" }
                        ) { index, topic ->
                            RoadmapTopicGlassCard(
                                topic = topic,
                                index = index,
                                onCardClick = {
                                    onTopicSelected(topic)
                                }
                            )
                        }
                    }
                }
            }
        }

        // 5. Floating "✨ Nova AI" Pill at Bottom Right
        FloatingNovaAiPillButton(
            onClick = onOpenCoachDirectly,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 14.dp)
        )
    }
}

@Composable
private fun RoadmapTopicGlassCard(
    topic: SpeakingPrompt,
    index: Int,
    onCardClick: () -> Unit
) {
    val isFluencyBooster = index % 2 == 1 && index > 0
    val prefix = if (isFluencyBooster) "Fluency Booster:" else "Learn:"
    val style = resolveTopicCardStyle(topic, index, isFluencyBooster)

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.1.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    style.cardBorderTint.copy(alpha = 0.55f),
                    Color(0xFF334180).copy(alpha = 0.45f),
                    Color(0xFF1E2952).copy(alpha = 0.45f)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 78.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onCardClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF151C42).copy(alpha = 0.88f),
                            Color(0xFF0E1433).copy(alpha = 0.92f)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Glowing Neon Ring Icon Circle
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    style.ringGlow.copy(alpha = 0.48f),
                                    style.ringGlow.copy(alpha = 0.14f),
                                    Color.Transparent
                                )
                            ),
                            radius = size.minDimension * 0.64f
                        )
                    }
                    .border(
                        width = 1.8.dp,
                        color = style.ringGlow.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
                    .padding(5.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(style.innerStart, style.innerEnd)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = topic.title,
                    tint = Color.White,
                    modifier = Modifier.size(23.dp)
                )
            }

            // Title & Metadata
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "$prefix ${topic.title}",
                    fontSize = 15.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                Text(
                    text = "${topic.category} • ${topic.difficultyLevel}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Right Chevron Circle
            Surface(
                shape = CircleShape,
                color = Color(0xFF172048),
                border = BorderStroke(1.dp, Color(0xFF33437D)),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = "Open ${topic.title}",
                        tint = Color(0xFFE2E8F0),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Sleek glowing "✨ Nova AI" floating pill button at bottom-right.
 */
@Composable
fun FloatingCoachAvatar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FloatingNovaAiPillButton(onClick = onClick, modifier = modifier)
}

@Composable
private fun FloatingNovaAiPillButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coach_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.Transparent,
        border = BorderStroke(
            width = 1.4.dp,
            brush = Brush.horizontalGradient(
                listOf(
                    Color(0xFFD8B4FE).copy(alpha = glowAlpha),
                    Color(0xFF38BDF8).copy(alpha = glowAlpha)
                )
            )
        ),
        shadowElevation = 10.dp,
        modifier = modifier
            .defaultMinSize(minHeight = 44.dp)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0xFF9333EA),
                spotColor = Color(0xFF9333EA)
            )
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .testTag("floating_coach_avatar")
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF9333EA),
                            Color(0xFF7C3AED),
                            Color(0xFF6366F1)
                        )
                    )
                )
                .padding(horizontal = 15.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = "Nova AI Coach",
                tint = Color(0xFFFDE047),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Nova AI",
                color = Color.White,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
