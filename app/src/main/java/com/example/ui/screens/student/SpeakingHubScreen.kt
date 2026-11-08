package com.example.ui.screens.student

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.PronunciationDrill
import com.example.data.model.RoleplayScenario
import com.example.data.model.SpeakingPrompt
import com.example.service.SpeechState
import com.example.ui.MainViewModel
import com.example.ui.SpeakingSubTab
import com.example.ui.StudentTab
import com.example.ui.components.SpeakingEvaluationCard
import com.example.ui.theme.*

@Composable
fun SpeakingHubScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentSubTab by viewModel.speakingSubTab.collectAsState()
    val speechState by viewModel.speechManager.speechState.collectAsState()
    val lastEvaluation by viewModel.lastEvaluation.collectAsState()
    val roleplays by viewModel.roleplays.collectAsState()
    val activeRoleplay by viewModel.activeRoleplay.collectAsState()
    val roleplayTurnIndex by viewModel.roleplayTurnIndex.collectAsState()
    val pronunciationDrills by viewModel.pronunciationDrills.collectAsState()
    val activeDrill by viewModel.activePronunciationDrill.collectAsState()
    val speakingPrompts by viewModel.speakingPrompts.collectAsState()

    BackHandler {
        viewModel.selectStudentTab(StudentTab.HOME)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("speaking_hub_screen")
    ) {
        // 1. Top Bar exactly matching screenshot
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.selectStudentTab(StudentTab.HOME) },
                modifier = Modifier
                    .size(38.dp)
                    .testTag("speaking_hub_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(2.dp))

            // Glowing microphone icon badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0x334F46E5),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Mic,
                        contentDescription = null,
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Speaking Practice & AI Coach",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
                Text(
                    text = if (currentSubTab == SpeakingSubTab.PRONUNCIATION_COACH) "Pronounce • Practice • Perfect" else "Practice • Get Feedback • Improve Fluency",
                    fontSize = 10.5.sp,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    softWrap = false
                )
            }

            // PRO Badge in golden pill
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF4A148C).copy(alpha = 0.85f),
                border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                modifier = Modifier.padding(start = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("👑", fontSize = 11.sp)
                    Text(
                        text = "PRO",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = Color(0xFFFDE68A)
                    )
                }
            }
        }

        // 2. Sub-tabs Pill Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SpeakingTabPill(
                    title = "AI Partner",
                    emoji = "🤖",
                    isSelected = currentSubTab == SpeakingSubTab.AI_PARTNER
                ) {
                    viewModel.selectSpeakingSubTab(SpeakingSubTab.AI_PARTNER)
                }
            }
            item {
                SpeakingTabPill(
                    title = "Daily Challenge",
                    emoji = "⚡",
                    isSelected = currentSubTab == SpeakingSubTab.DAILY_CHALLENGE
                ) {
                    viewModel.selectSpeakingSubTab(SpeakingSubTab.DAILY_CHALLENGE)
                }
            }
            item {
                SpeakingTabPill(
                    title = "Roleplay",
                    emoji = "🎭",
                    isSelected = currentSubTab == SpeakingSubTab.ROLEPLAY
                ) {
                    viewModel.selectSpeakingSubTab(SpeakingSubTab.ROLEPLAY)
                }
            }
            item {
                SpeakingTabPill(
                    title = "Pronunciation",
                    emoji = "🎯",
                    isSelected = currentSubTab == SpeakingSubTab.PRONUNCIATION_COACH
                ) {
                    viewModel.selectSpeakingSubTab(SpeakingSubTab.PRONUNCIATION_COACH)
                }
            }
            item {
                SpeakingTabPill(
                    title = "Picture Describe",
                    emoji = "🖼️",
                    isSelected = currentSubTab == SpeakingSubTab.PICTURE_DESCRIPTION
                ) {
                    viewModel.selectSpeakingSubTab(SpeakingSubTab.PICTURE_DESCRIPTION)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentSubTab) {
                SpeakingSubTab.AI_PARTNER -> {
                    item {
                        AiPartnerExactScreen(
                            viewModel = viewModel,
                            speechState = speechState,
                            evaluation = lastEvaluation
                        )
                    }
                }
                SpeakingSubTab.DAILY_CHALLENGE -> {
                    item {
                        DailyChallengeSection(
                            prompts = speakingPrompts,
                            viewModel = viewModel,
                            speechState = speechState,
                            evaluation = lastEvaluation
                        )
                    }
                }
                SpeakingSubTab.ROLEPLAY -> {
                    item {
                        RoleplaySection(
                            roleplays = roleplays,
                            activeRoleplay = activeRoleplay,
                            turnIndex = roleplayTurnIndex,
                            viewModel = viewModel,
                            speechState = speechState,
                            evaluation = lastEvaluation
                        )
                    }
                }
                SpeakingSubTab.PRONUNCIATION_COACH -> {
                    item {
                        PronunciationSection(
                            drills = pronunciationDrills,
                            activeDrill = activeDrill,
                            viewModel = viewModel,
                            speechState = speechState,
                            evaluation = lastEvaluation
                        )
                    }
                }
                SpeakingSubTab.PICTURE_DESCRIPTION -> {
                    item {
                        PictureDescriptionSection(
                            viewModel = viewModel,
                            speechState = speechState,
                            evaluation = lastEvaluation
                        )
                    }
                }
            }
        }
    }
}

/**
 * Custom Tab Pill matching screenshot styling:
 * Selected: Rich purple gradient with glowing border
 * Unselected: Translucent midnight dark container
 */
@Composable
fun SpeakingTabPill(
    title: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF131A3E).copy(alpha = 0.85f),
        border = BorderStroke(
            1.2.dp,
            if (isSelected) Color(0xFFC084FC) else Color(0xFF263366)
        ),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("speaking_tab_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else Color(0xFFCBD5E1)
            )
        }
    }
}

/**
 * EXACT REDESIGN OF AI PARTNER VIEW ACCORDING TO USER SCREENSHOT
 */
@Composable
fun AiPartnerExactScreen(
    viewModel: MainViewModel,
    speechState: SpeechState,
    evaluation: com.example.data.model.SpeakingEvaluation?
) {
    var aiQuestion by remember {
        mutableStateOf("Hi Alex! How was your weekend? Did you do anything fun or exciting?")
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Nova AI Coach Outer Glass Card
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, Color(0xFF8B5CF6).copy(alpha = 0.9f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("nova_ai_coach_card")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F1B4E).copy(alpha = 0.95f),
                                Color(0xFF161146).copy(alpha = 0.95f),
                                Color(0xFF0A1032).copy(alpha = 0.98f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header row: Bot Avatar on left, Titles middle, Speaker button right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Futuristic Robot Avatar matching exact user asset
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E265C),
                            border = BorderStroke(2.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.size(58.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_nova_ai_robot),
                                contentDescription = "Nova AI Robot",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Nova",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color(0xFFC084FC)
                                )
                                Text(
                                    text = " • AI Speaking Coach",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "Real-time voice conversation partner",
                                fontSize = 11.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // Circular Speaker Playback Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2563EB).copy(alpha = 0.85f),
                            border = BorderStroke(1.5.dp, Color(0xFF60A5FA)),
                            modifier = Modifier
                                .size(44.dp)
                                .clickable { viewModel.speakAudio(aiQuestion) }
                                .testTag("ai_listen_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.VolumeUp,
                                    contentDescription = "Listen to AI",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Inner Speech Dialogue Container with quote badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF071B48).copy(alpha = 0.95f),
                        border = BorderStroke(1.2.dp, Color(0xFF0284C7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Circular Quote Icon
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF2563EB).copy(alpha = 0.8f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "❝",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "“$aiQuestion”",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                lineHeight = 20.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Topic Suggestion Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Hobbies Topic Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF131D4A).copy(alpha = 0.85f),
                            border = BorderStroke(1.2.dp, Color(0xFFEC4899)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    aiQuestion = "Could you tell me about your favorite hobbies and why you enjoy them?"
                                    viewModel.speakAudio(aiQuestion)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💖", fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Hobbies Topic",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Career Topic Button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF131D4A).copy(alpha = 0.85f),
                            border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    aiQuestion = "What kind of job do you do, and what do you like most about your work?"
                                    viewModel.speakAudio(aiQuestion)
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("💼", fontSize = 14.sp)
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    "Career Topic",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        // 2. Center Glowing Microphone Button with Audio Waveform Visualizers
        val isListening = speechState is SpeechState.Listening
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = if (isListening) 1.15f else 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Audio Waveform bars
            AudioWaveformVisualizer(barColor = Color(0xFF38BDF8), modifier = Modifier.weight(1f))

            // Center Glowing Microphone button with concentric rings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(136.dp)
                    .scale(pulseScale)
            ) {
                // Outer subtle neon aura ring
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(1.5.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)),
                    modifier = Modifier.size(136.dp)
                ) {}

                // Mid glowing ring
                Surface(
                    shape = CircleShape,
                    color = Color(0x334F46E5),
                    border = BorderStroke(2.dp, Color(0xFFC084FC).copy(alpha = 0.8f)),
                    modifier = Modifier.size(118.dp)
                ) {}

                // Inner core gradient button
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (isListening) {
                                viewModel.stopSpeakingPractice()
                            } else {
                                viewModel.startSpeakingPractice()
                            }
                        }
                        .testTag("microphone_button")
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF00C6FF),
                                        Color(0xFF0072FF),
                                        Color(0xFF6B21A8)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                            contentDescription = if (isListening) "Stop Recording" else "Tap to Speak",
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }
                }
            }

            // Right Audio Waveform bars
            AudioWaveformVisualizer(barColor = Color(0xFFC084FC), modifier = Modifier.weight(1f))
        }

        // Tap to Speak Label
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = when (speechState) {
                    is SpeechState.Idle -> "Tap to Speak"
                    is SpeechState.Listening -> "Listening... Speak now"
                    is SpeechState.Processing -> "Evaluating your speech..."
                    is SpeechState.Success -> "Great job! Processing feedback"
                    is SpeechState.Error -> "Please try speaking again"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "Start a conversation with Nova",
                fontSize = 12.5.sp,
                color = Color(0xFF94A3B8)
            )
        }

        // 3. "Test with Sample Voice" pill button
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF1E3A8A).copy(alpha = 0.85f),
            border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
            modifier = Modifier
                .clickable {
                    viewModel.simulateVoiceInput(
                        "My weekend was really relaxing. I went hiking in the national park with my friends and cooked dinner."
                    )
                }
                .testTag("test_sample_voice_pill")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Test with Sample Voice",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // 4. Feature Highlights Footer Card (4 stats: Real Conversation, Instant Feedback, Improve Fluency, Build Confidence)
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.2.dp, Color(0xFF6366F1).copy(alpha = 0.65f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF1E144E).copy(alpha = 0.95f),
                                Color(0xFF0F1A44).copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(vertical = 14.dp, horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FeaturePillarItem(
                        icon = Icons.Rounded.ChatBubble,
                        line1 = "Real",
                        line2 = "Conversation",
                        tint = Color(0xFF38BDF8)
                    )

                    VerticalDividerPillar()

                    FeaturePillarItem(
                        icon = Icons.Rounded.BarChart,
                        line1 = "Instant",
                        line2 = "Feedback",
                        tint = Color(0xFFC084FC)
                    )

                    VerticalDividerPillar()

                    FeaturePillarItem(
                        icon = Icons.Rounded.TrackChanges,
                        line1 = "Improve",
                        line2 = "Fluency",
                        tint = Color(0xFFF472B6)
                    )

                    VerticalDividerPillar()

                    FeaturePillarItem(
                        icon = Icons.Rounded.Star,
                        line1 = "Build",
                        line2 = "Confidence",
                        tint = Color(0xFFFBBF24)
                    )
                }
            }
        }

        // Live Evaluation Display if speech evaluated
        evaluation?.let { eval ->
            Spacer(Modifier.height(10.dp))
            SpeakingEvaluationCard(
                evaluation = eval,
                onAudioPlayExpected = {
                    viewModel.speakAudio(
                        "My weekend was very relaxing. I went hiking with my friends and we cooked a delicious meal together."
                    )
                }
            )
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    barColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val heights = listOf(8.dp, 16.dp, 28.dp, 40.dp, 24.dp, 12.dp)
        heights.forEach { h ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(3.dp)
                    .height(h)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor.copy(alpha = 0.85f))
            )
        }
    }
}

@Composable
fun FeaturePillarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    line1: String,
    line2: String,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = line1,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = line2,
                fontSize = 11.sp,
                color = Color(0xFFCBD5E1)
            )
        }
    }
}

@Composable
fun VerticalDividerPillar() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(28.dp)
            .background(Color.White.copy(alpha = 0.18f))
    )
}

@Composable
fun DailyChallengeSection(
    prompts: List<SpeakingPrompt>,
    viewModel: MainViewModel,
    speechState: SpeechState,
    evaluation: com.example.data.model.SpeakingEvaluation?
) {
    var selectedPrompt by remember { mutableStateOf(prompts.first()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Select Speaking Topic", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(prompts) { p ->
                val isSel = p.id == selectedPrompt.id
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSel) RoyalBlue else Color(0xFF1E293B),
                    border = BorderStroke(1.dp, if (isSel) Color.White else Color(0xFF334155)),
                    modifier = Modifier.clickable { selectedPrompt = p }
                ) {
                    Text(
                        p.title,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF131D4A)),
            border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedPrompt.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    IconButton(onClick = { viewModel.speakAudio(selectedPrompt.question) }) {
                        Icon(Icons.Rounded.VolumeUp, contentDescription = "Play Prompt", tint = RoyalBlue)
                    }
                }
                Text(selectedPrompt.question, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun RoleplaySection(
    roleplays: List<RoleplayScenario>,
    activeRoleplay: RoleplayScenario?,
    turnIndex: Int,
    viewModel: MainViewModel,
    speechState: SpeechState,
    evaluation: com.example.data.model.SpeakingEvaluation?
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Roleplay Conversations", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(roleplays) { r ->
                val isSel = activeRoleplay?.id == r.id
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSel) Color(0xFF2E1065) else Color(0xFF131D4A)),
                    border = BorderStroke(1.2.dp, if (isSel) Color(0xFFC084FC) else Color(0xFF334155)),
                    modifier = Modifier
                        .width(180.dp)
                        .clickable { viewModel.startRoleplay(r) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(r.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text(r.situation, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1)
                    }
                }
            }
        }

        activeRoleplay?.let { scenario ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF131D4A)),
                border = BorderStroke(1.2.dp, Color(0xFF8B5CF6)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Scenario: ${scenario.situation}", fontWeight = FontWeight.Bold, color = Color(0xFFC084FC))
                    Text("Partner: ${scenario.partnerRole} • You: ${scenario.userRole}", fontSize = 13.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun PronunciationSection(
    drills: List<PronunciationDrill>,
    activeDrill: PronunciationDrill?,
    viewModel: MainViewModel,
    speechState: SpeechState,
    evaluation: com.example.data.model.SpeakingEvaluation?
) {
    var queryInput by remember { mutableStateOf("") }
    val searchedResult by viewModel.searchedPhoneticResult.collectAsState()
    val isSearching by viewModel.isSearchingPhonetics.collectAsState()
    var selectedCategory by remember { mutableStateOf("🔥 Trending") }
    var actionToast by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. AI Phonetics Word Search Hero Card
        Card(
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, Color(0xFFC084FC).copy(alpha = 0.9f)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_phonetics_search_card")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0F1B4E).copy(alpha = 0.95f),
                                Color(0xFF161146).copy(alpha = 0.95f),
                                Color(0xFF0A1032).copy(alpha = 0.98f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Robot Avatar with glowing cyber ring
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E265C),
                        border = BorderStroke(2.dp, Color(0xFF38BDF8)),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_nova_ai_robot),
                            contentDescription = "Nova AI",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Nova AI Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF4F46E5).copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, Color(0xFF818CF8))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("✨", fontSize = 11.sp)
                                Text(
                                    "Nova AI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Text(
                            text = "AI Phonetics Word Search",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )

                        Text(
                            text = "Type any word to look up IPA, syllables & native speech tips",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        // 2. Search Input & Analyze Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Search Input Pill
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0C1738).copy(alpha = 0.95f),
                border = BorderStroke(1.2.dp, Color(0xFF0284C7)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        if (queryInput.isEmpty()) {
                            Text(
                                "Search word (e.g. comfortable, though)...",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B),
                                maxLines = 1
                            )
                        }
                        androidx.compose.foundation.text.BasicTextField(
                            value = queryInput,
                            onValueChange = {
                                queryInput = it
                                viewModel.updatePhoneticSearchQuery(it)
                            },
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phonetic_search_text_field")
                        )
                    }

                    if (queryInput.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                queryInput = ""
                                viewModel.clearPhoneticsSearch()
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "Clear",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // Analyze Button Pill with Gradient
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent,
                border = BorderStroke(1.dp, Color(0xFFFF77E9)),
                modifier = Modifier
                    .height(48.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable {
                        if (queryInput.isNotBlank()) {
                            viewModel.searchPhonetics(queryInput)
                        }
                    }
                    .testTag("phonetic_search_button")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF8B1FA9), Color(0xFFD926A9))
                            )
                        )
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSearching) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("✨", fontSize = 12.sp)
                            Text(
                                "Analyze",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 3. Quick Category Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val categories = listOf("🔥 Trending", "🤫 Silent Letters", "🌷 Tricky 'TH'", "💼 More")
            items(categories) { cat ->
                val isSel = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSel) Color(0xFF9333EA) else Color(0xFF0F1E44).copy(alpha = 0.85f),
                    border = BorderStroke(
                        1.2.dp,
                        if (isSel) Color(0xFFD8B4FE) else Color(0xFF2563EB).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        cat,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // 4. Trending Words Container Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF0F1738).copy(alpha = 0.95f),
                                Color(0xFF090E26).copy(alpha = 0.95f)
                            )
                        )
                    )
                    .padding(14.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🔥", fontSize = 16.sp)
                            Text(
                                "Trending Words",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }

                        Text(
                            "View All →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF60A5FA)
                        )
                    }

                    // Trending Words Capsules
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val trendingWords = listOf("Comfortable", "Thorough", "Phenomenon", "Entrepreneur")
                        trendingWords.forEach { word ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF4C1D95).copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.8f)),
                                modifier = Modifier
                                    .clickable {
                                        queryInput = word
                                        viewModel.searchPhonetics(word)
                                    }
                            ) {
                                Text(
                                    word,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Searched Result Display (Exact Redesign matching user screenshot)
        searchedResult?.let { res ->
            Card(
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.8.dp, Color(0xFF8B5CF6).copy(alpha = 0.85f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("searched_phonetic_result_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0F1A4E).copy(alpha = 0.96f),
                                    Color(0xFF141042).copy(alpha = 0.96f),
                                    Color(0xFF090D2A).copy(alpha = 0.98f)
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Top Section: Square Glowing Mic Badge, Word + Syllables + Tag, IPA Pill
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Glowing Square Mic Badge (Blue-Purple Gradient)
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.5.dp, Color(0xFFC084FC).copy(alpha = 0.9f)),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(Color(0xFF3B82F6), Color(0xFF7C3AED), Color(0xFF9333EA))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Mic,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            // Middle: Word Title + checkmark + Syllables + Sound Tag
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = res.word,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 28.sp,
                                        color = Color.White
                                    )
                                    // Blue Verified Checkmark Pill
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                "✔",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = res.syllables,
                                    fontSize = 14.sp,
                                    color = Color(0xFFCBD5E1),
                                    fontWeight = FontWeight.Medium
                                )

                                // Stress Rhythm Pill (Teal-green Capsule)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF064E3B).copy(alpha = 0.85f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.8f)),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("•ı|•", fontSize = 10.sp, color = Color(0xFF34D399), fontWeight = FontWeight.ExtraBold)
                                        Text(
                                            text = res.soundTags.firstOrNull() ?: "Stress Rhythm",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF6EE7B7)
                                        )
                                    }
                                }
                            }

                            // Right: High-contrast IPA Transcription Capsule
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFF4C1D95).copy(alpha = 0.75f),
                                border = BorderStroke(1.5.dp, Color(0xFFA855F7)),
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Text(
                                    text = res.ipa,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFF3E8FF),
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }

                        // Middle Card 1: Focus & Level header + Speech Coach Card (Cyan border)
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFF0C2152).copy(alpha = 0.85f),
                            border = BorderStroke(1.5.dp, Color(0xFF0284C7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Focus & Level Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    // Target Icon
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF0284C7).copy(alpha = 0.35f),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("🎯", fontSize = 17.sp)
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Focus: ", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(res.audioFocus, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF38BDF8))
                                        Text("   |   ", fontSize = 13.5.sp, color = Color.White.copy(alpha = 0.4f))
                                        Text("Level: ", fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        Text(res.difficulty, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF34D399))
                                    }
                                }

                                // Speech Coach row with glowing lightbulb badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF0284C7).copy(alpha = 0.3f),
                                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("💡", fontSize = 22.sp)
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            "Speech Coach",
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            res.coachingTip,
                                            fontSize = 12.5.sp,
                                            lineHeight = 17.sp,
                                            color = Color(0xFFE2E8F0)
                                        )
                                    }
                                }
                            }
                        }

                        // Middle Card 2: Assamese Translation Card (Orange/Gold Glow)
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFF2E1A0F).copy(alpha = 0.85f),
                            border = BorderStroke(1.5.dp, Color(0xFFF59E0B)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Book Icon badge
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF78350F).copy(alpha = 0.8f),
                                    border = BorderStroke(1.dp, Color(0xFFFBBF24).copy(alpha = 0.7f)),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("📖", fontSize = 22.sp)
                                    }
                                }

                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        "অসমীয়া অৰ্থ (Assamese Meaning):",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFBBF24)
                                    )
                                    val displayAssamese = when {
                                        res.assameseTransliteration.isBlank() -> res.assameseMeaning
                                        res.assameseMeaning.contains("(") -> res.assameseMeaning
                                        res.assameseMeaning.contains(res.assameseTransliteration, ignoreCase = true) -> res.assameseMeaning
                                        else -> "${res.assameseMeaning} (${res.assameseTransliteration})"
                                    }
                                    Text(
                                        text = displayAssamese,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Action Buttons: Row 1 -> 0.7x Slow (Purple) & Normal Speed (Blue)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // 0.7x Slow Button
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { viewModel.speakAudio(res.word, slow = true) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF7C3AED), Color(0xFF9333EA))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("⏱️", fontSize = 16.sp)
                                        Text(
                                            "0.7× Slow",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Normal Speed Button
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable { viewModel.speakAudio(res.word, slow = false) }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF0284C7), Color(0xFF2563EB))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.VolumeUp,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            "Normal Speed",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Action Buttons: Row 2 -> + Add to Drills (Deep Blue) & 🎙 Practice Now (Neon Green)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // + Add to Drills Button
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFF172554),
                                border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable {
                                        viewModel.addSearchedWordToDrills(res)
                                        actionToast = "Saved '${res.word}' to practice drills!"
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "Add to Drills",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Practice Now Button
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .clickable {
                                        val drill = PronunciationDrill(
                                            id = "custom_${res.word}",
                                            wordOrSentence = res.word,
                                            ipaPhonetic = res.ipa,
                                            audioFocus = res.audioFocus,
                                            difficulty = res.difficulty,
                                            tips = res.coachingTip
                                        )
                                        viewModel.startPronunciationDrill(drill)
                                    }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF059669), Color(0xFF10B981))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.Mic,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            "Practice Now",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Action Toast Message
                        AnimatedVisibility(visible = actionToast != null) {
                            actionToast?.let { msg ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF064E3B),
                                    border = BorderStroke(1.dp, Color(0xFF10B981)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = msg,
                                        color = Color(0xFF6EE7B7),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Section Header: "📚 Practice Drills & Accent Coach" with "🎧 6 drills available" badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📚", fontSize = 18.sp)
                Text(
                    "Practice Drills & Accent Coach",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E144E).copy(alpha = 0.9f),
                border = BorderStroke(1.2.dp, Color(0xFF8B5CF6))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("🎧", fontSize = 12.sp)
                    Column {
                        Text(
                            "${drills.size} drills",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "available",
                            fontSize = 9.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        // 7. Practice Drill Cards (3 Colorful Gradient Cards side by side)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Comfortable (Vibrant Azure Blue)
            PhoneticsDrillScreenshotCard(
                word = "Comfortable",
                ipa = "/'kʌmftəbl/",
                focus = "Focus: Silent 'or'",
                gradient = Brush.verticalGradient(
                    listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF075985))
                ),
                borderColor = Color(0xFF38BDF8),
                modifier = Modifier.weight(1f),
                onPlay = { viewModel.speakAudio("Comfortable") }
            )

            // Card 2: Specifically (Radiant Magenta-Purple)
            PhoneticsDrillScreenshotCard(
                word = "Specifically",
                ipa = "/spə'sɪfɪkli/",
                focus = "Focus: Consonant cluster",
                gradient = Brush.verticalGradient(
                    listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9), Color(0xFF581C87))
                ),
                borderColor = Color(0xFFC084FC),
                modifier = Modifier.weight(1f),
                onPlay = { viewModel.speakAudio("Specifically") }
            )

            // Card 3: Thought (Vibrant Amber-Orange)
            PhoneticsDrillScreenshotCard(
                word = "Thought",
                ipa = "/θɔːtful/",
                focus = "Focus: Soft 'th'",
                gradient = Brush.verticalGradient(
                    listOf(Color(0xFFF97316), Color(0xFFEA580C), Color(0xFFC2410C))
                ),
                borderColor = Color(0xFFFDBA74),
                modifier = Modifier.weight(1f),
                onPlay = { viewModel.speakAudio("Thought") }
            )
        }

        Spacer(Modifier.height(14.dp))
    }
}

/**
 * Phonetics Drill Card matching screenshot design:
 * Rounded card with gradient background, circular speaker in top-right,
 * bold word, IPA pronunciation below, and subtle focus label.
 */
@Composable
fun PhoneticsDrillScreenshotCard(
    word: String,
    ipa: String,
    focus: String,
    gradient: Brush,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.2.dp, borderColor.copy(alpha = 0.7f)),
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onPlay() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top row with Speaker button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.25f),
                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.4f)),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.VolumeUp,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Text(
                    text = word,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = ipa,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )

                Text(
                    text = focus,
                    fontSize = 10.5.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    lineHeight = 14.sp
                )
            }
        }
    }
}

@Composable
fun PictureDescriptionSection(
    viewModel: MainViewModel,
    speechState: SpeechState,
    evaluation: com.example.data.model.SpeakingEvaluation?
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF131D4A)),
        border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Picture Description Challenge", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            Text(
                "Look at a real-world scenario and describe what you see, who is doing what, and what might happen next in full spoken sentences.",
                fontSize = 13.sp,
                color = Color(0xFFCBD5E1)
            )
        }
    }
}
