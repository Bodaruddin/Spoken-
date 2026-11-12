package com.example.ui.screens.student

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.SpeakingPrompt
import com.example.ui.MainViewModel
import com.example.ui.StudentTab

/**
 * ConversationTopicsRoadmapScreen
 *
 * Implements the vertical serpentine / winding conversation topics learning path
 * matching the user's provided UI design screenshot:
 * - Top header with streak counter (🔥 0) and student profile shortcut.
 * - Section header card ("SECTION 1 / Week 1").
 * - Winding road with circular avatar & fluency booster nodes.
 * - Glowing green ring with animated "START" callout bubble on the first node.
 * - Floating coach avatar on bottom right.
 * - Selecting any topic starts AI speaking conversation.
 */
@Composable
fun ConversationTopicsRoadmapScreen(
    viewModel: MainViewModel,
    onTopicSelected: (SpeakingPrompt) -> Unit,
    onOpenCoachDirectly: () -> Unit,
    modifier: Modifier = Modifier
) {
    val prompts by viewModel.speakingPrompts.collectAsState()
    val activeTopic by viewModel.activeConversationTopic.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()

    val darkBackground = Color(0xFF0B0F19)
    val cardBackground = Color(0xFF13192B)
    val cardBorder = Color(0xFF222C42)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(darkBackground)
            .testTag("conversation_topics_roadmap_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            // 1. Top Header Bar: Streak (🔥 0) on left, Profile shortcut on right
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Streak indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.testTag("streak_indicator")
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 22.sp
                    )
                    Text(
                        text = "${studentProfile.streakDays}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFF7A00)
                    )
                }

                // Random Topic pill in top bar
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E1B4B),
                    border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.6f)),
                    modifier = Modifier.clickable {
                        val randomTopic = viewModel.selectRandomConversationTopic()
                        if (randomTopic != null) {
                            onTopicSelected(randomTopic)
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("🎲", fontSize = 12.sp)
                        Text(
                            "Random Topic",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE9D5FF)
                        )
                    }
                }

                // Profile Avatar Button
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1E2538),
                    border = BorderStroke(1.2.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { viewModel.selectStudentTab(StudentTab.PROFILE) }
                        .testTag("roadmap_profile_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Profile",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 2. Section Card: "SECTION 1 / Week 1" matching screenshot
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                border = BorderStroke(1.dp, cardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("roadmap_section_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "SECTION 1",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFA855F7),
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Week 1",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tap any topic to speak with Nova AI Coach",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // 3. Winding Path of Conversation Topics
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                itemsIndexed(prompts, key = { _, item -> item.id }) { index, topic ->
                    val isFirstNode = index == 0
                    val isCurrentActive = activeTopic?.id == topic.id || (activeTopic == null && isFirstNode)

                    // Alternating serpentine alignment matching the winding path in screenshot
                    // Patterns: Left -> Center -> Right -> Center -> Left
                    val horizontalAlignment = when (index % 5) {
                        0 -> Alignment.Start
                        1 -> Alignment.CenterHorizontally
                        2 -> Alignment.End
                        3 -> Alignment.CenterHorizontally
                        else -> Alignment.Start
                    }

                    RoadmapTopicNodeItem(
                        topic = topic,
                        index = index,
                        isFirst = isFirstNode,
                        isActive = isCurrentActive,
                        horizontalAlignment = horizontalAlignment,
                        onNodeClick = {
                            viewModel.selectConversationTopic(topic)
                            onTopicSelected(topic)
                        }
                    )
                }
            }
        }

        // 4. Floating Coach Avatar at Bottom Right matching screenshot
        FloatingCoachAvatar(
            onClick = onOpenCoachDirectly,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        )
    }
}

/**
 * Single Node item along the winding roadmap path:
 * - Green ring with START speech bubble callout for the active/first node.
 * - Circular 3D avatar or Fluency Booster brain badge.
 * - Label to the right (or left): "Learn: <Title>" or "Fluency Booster: <Title>".
 */
@Composable
fun RoadmapTopicNodeItem(
    topic: SpeakingPrompt,
    index: Int,
    isFirst: Boolean,
    isActive: Boolean,
    horizontalAlignment: Alignment.Horizontal,
    onNodeClick: () -> Unit
) {
    // Pulse animation for the START callout badge
    val infiniteTransition = rememberInfiniteTransition(label = "start_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val isFluencyBooster = index % 2 == 1 && index > 0
    val prefix = if (isFluencyBooster) "Fluency Booster:" else "Learn:"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = horizontalAlignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onNodeClick() }
        ) {
            // If it's the active/first node, show the START speech bubble on the left
            if (isActive) {
                Box(
                    modifier = Modifier
                        .scale(pulseScale)
                        .testTag("start_topic_callout")
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0D1E2A),
                        border = BorderStroke(1.8.dp, Color(0xFF10B981))
                    ) {
                        Text(
                            text = "START",
                            color = Color(0xFF10B981),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Circular Topic Node with 3D drop-shadow ring
            Box(contentAlignment = Alignment.Center) {
                // 3D bottom bevel/shadow ring
                Surface(
                    shape = CircleShape,
                    color = if (isActive) Color(0xFF047857) else Color(0xFF1E293B),
                    modifier = Modifier
                        .size(70.dp)
                        .offset(y = 5.dp)
                ) {}

                // Main Node Circle
                Surface(
                    shape = CircleShape,
                    color = when {
                        isActive -> Color(0xFF064E3B)
                        isFluencyBooster -> Color(0xFF1E2538)
                        else -> Color(0xFF161E2E)
                    },
                    border = BorderStroke(
                        width = if (isActive) 3.dp else 2.dp,
                        color = when {
                            isActive -> Color(0xFF10B981)
                            isFluencyBooster -> Color(0xFF3B82F6).copy(alpha = 0.6f)
                            else -> Color(0xFF334155)
                        }
                    ),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isFluencyBooster) {
                            // Fluency Booster brain & voice symbol matching screenshot
                            Icon(
                                imageVector = Icons.Rounded.Psychology,
                                contentDescription = "Fluency Booster",
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(34.dp)
                            )
                        } else {
                            // Avatar / Icon Node matching screenshot
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = topic.emoji,
                                    fontSize = 28.sp
                                )
                            }
                        }
                    }
                }
            }

            // Title Label beside the node
            Column(
                modifier = Modifier
                    .widthIn(max = 200.dp)
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = "$prefix ${topic.title}",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else Color(0xFFCBD5E1),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${topic.category} • ${topic.difficultyLevel}",
                    fontSize = 11.5.sp,
                    color = Color(0xFF94A3B8)
                )
            }
        }
    }
}

/**
 * Floating Coach Avatar at Bottom Right:
 * Matches the student/teacher avatar with purple glowing ring in the user's screenshot.
 */
@Composable
fun FloatingCoachAvatar(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coach_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Surface(
        shape = CircleShape,
        color = Color(0xFF1E1B4B),
        border = BorderStroke(2.5.dp, Color(0xFFC084FC).copy(alpha = glowAlpha)),
        shadowElevation = 10.dp,
        modifier = modifier
            .size(66.dp)
            .clickable { onClick() }
            .testTag("floating_coach_avatar")
    ) {
        Box(contentAlignment = Alignment.Center) {
            Image(
                painter = painterResource(id = R.drawable.img_academic_laptop_books),
                contentDescription = "Nova Coach",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
            )

            // Small "Nova" badge overlay at bottom
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color(0xFF7C3AED),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .offset(y = (-2).dp)
            ) {
                Text(
                    text = "Nova",
                    color = Color.White,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                )
            }
        }
    }
}
