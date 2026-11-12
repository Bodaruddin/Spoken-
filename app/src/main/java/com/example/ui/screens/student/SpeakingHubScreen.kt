package com.example.ui.screens.student

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.ui.components.AiApiKeyIntegrationDialog
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
        if (currentSubTab == SpeakingSubTab.ENGLISH_ADVENTURE) {
            viewModel.selectSpeakingSubTab(SpeakingSubTab.AI_PARTNER)
        } else {
            viewModel.selectStudentTab(StudentTab.HOME)
        }
    }

    if (currentSubTab == SpeakingSubTab.ENGLISH_ADVENTURE) {
        EnglishAdventureScreen(
            viewModel = viewModel,
            modifier = modifier.fillMaxSize()
        )
    } else {
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
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
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

            val isAiConfigured by viewModel.isGeminiConfigured.collectAsState()
            if (isAiConfigured) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0369A1).copy(alpha = 0.85f),
                    border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
                    modifier = Modifier.padding(start = 5.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text("⚡", fontSize = 11.sp)
                        Text(
                            text = "AI Online",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 10.5.sp,
                            color = Color(0xFFE0F2FE)
                        )
                    }
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
                    title = "Vocab & Assamese",
                    emoji = "📖",
                    isSelected = currentSubTab == SpeakingSubTab.VOCABULARY
                ) {
                    viewModel.selectSpeakingSubTab(SpeakingSubTab.VOCABULARY)
                }
            }
            item {
                SpeakingTabPill(
                    title = "English Adventure",
                    emoji = "🗺️",
                    isSelected = currentSubTab == SpeakingSubTab.ENGLISH_ADVENTURE
                ) {
                    viewModel.selectSpeakingSubTab(SpeakingSubTab.ENGLISH_ADVENTURE)
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
                SpeakingSubTab.VOCABULARY -> {
                    item {
                        StudentVocabularyExplorer(
                            viewModel = viewModel
                        )
                    }
                }
                SpeakingSubTab.ENGLISH_ADVENTURE -> {
                    // Handled at top level outside Speaking Practice bar
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
    val aiQuestion by viewModel.activeCoachQuestion.collectAsState()
    val isAiEvaluating by viewModel.isAiEvaluating.collectAsState()
    val isGeminiActive by viewModel.isGeminiConfigured.collectAsState()
    val activeModel by viewModel.activeAiModel.collectAsState()


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
                                    val q = "Could you tell me about your favorite hobbies and why you enjoy them?"
                                    viewModel.setActiveCoachQuestion(q)
                                    viewModel.speakAudio(q)
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
                                    val q = "What kind of job do you do, and what do you like most about your work?"
                                    viewModel.setActiveCoachQuestion(q)
                                    viewModel.speakAudio(q)
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
                text = when {
                    isAiEvaluating -> "Evaluating with Gemini AI..."
                    speechState is SpeechState.Idle -> "Tap to Speak"
                    speechState is SpeechState.Listening -> "Listening... Speak now"
                    speechState is SpeechState.Processing -> "Processing speech..."
                    speechState is SpeechState.Success -> "Great job! Processing feedback"
                    speechState is SpeechState.Error -> "Please try speaking again"
                    else -> "Tap to Speak"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = if (isAiEvaluating) "Analyzing pronunciation & fluency with $activeModel" else "Start a conversation with Nova",
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

        // AI Speaking Status Indicator (Informational only for student)
        if (isGeminiActive) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0C4A6E).copy(alpha = 0.5f),
                border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.7f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("✨", fontSize = 12.sp)
                    Text(
                        text = "Nova Real-Time AI Coach Active",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE0F2FE)
                    )
                }
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

        // Real-Time Evaluation Loading Indicator
        if (isAiEvaluating) {
            Spacer(Modifier.height(10.dp))
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E1B4B).copy(alpha = 0.9f),
                border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF38BDF8),
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Nova AI is evaluating your speech...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Analyzing pronunciation, rhythm, grammar & CEFR level",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
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
                        eval.expectedText ?: "My weekend was very relaxing. I went hiking with my friends and we cooked a delicious meal together."
                    )
                },
                onAudioPlayAiResponse = { reply ->
                    viewModel.speakAudio(reply)
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
                            color = Color(0xFF60A5FA),
                            modifier = Modifier
                                .clickable {
                                    queryInput = "Entrepreneur"
                                    viewModel.searchPhonetics("Entrepreneur")
                                }
                                .padding(4.dp)
                        )
                    }

                    // Trending & Teacher Vocabulary Words Capsules - Horizontally swipable
                    val vocabularyWords by viewModel.vocabularyWords.collectAsState()
                    val displayWords = remember(vocabularyWords) {
                        val teacherWords = vocabularyWords.map { it.word }
                        val curated = listOf(
                            "Comfortable",
                            "Thorough",
                            "Phenomenon",
                            "Entrepreneur",
                            "Specifically",
                            "Pronunciation",
                            "Mischievous",
                            "Schedule",
                            "Colleague",
                            "Worcestershire"
                        )
                        (teacherWords + curated).distinct()
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("trending_words_lazy_row"),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
                    ) {
                        items(displayWords) { word ->
                            val isTeacherAdded = vocabularyWords.any { it.word.equals(word, ignoreCase = true) }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isTeacherAdded) Color(0xFF064E3B).copy(alpha = 0.85f) else Color(0xFF4C1D95).copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, if (isTeacherAdded) Color(0xFF10B981) else Color(0xFFA855F7).copy(alpha = 0.8f)),
                                modifier = Modifier
                                    .clickable {
                                        queryInput = word
                                        viewModel.searchPhonetics(word)
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (isTeacherAdded) {
                                        Text("📖", fontSize = 11.sp)
                                    }
                                    Text(
                                        text = word,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isTeacherAdded) Color(0xFFFEF08A) else Color.White,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
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
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Glowing Square Mic Badge (Blue-Purple Gradient)
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.5.dp, Color(0xFFC084FC).copy(alpha = 0.9f)),
                                modifier = Modifier.size(56.dp)
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
                                        modifier = Modifier.size(28.dp)
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
                                        fontSize = 24.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    // Blue Verified Checkmark Pill
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                "✔",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = res.syllables,
                                    fontSize = 13.sp,
                                    color = Color(0xFFCBD5E1),
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
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
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF4C1D95).copy(alpha = 0.75f),
                                border = BorderStroke(1.5.dp, Color(0xFFA855F7)),
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Text(
                                    text = res.ipa,
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFF3E8FF),
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    // Target Icon + Focus Label & Description
                                    Row(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Target Icon
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFF0284C7).copy(alpha = 0.35f),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("🎯", fontSize = 16.sp)
                                            }
                                        }

                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f, fill = false)
                                        ) {
                                            Text(
                                                text = "Focus: ",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                softWrap = false
                                            )
                                            Text(
                                                text = res.audioFocus,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF38BDF8),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                softWrap = false
                                            )
                                        }
                                    }

                                    // Dedicated Level Badge Pill (prevents vertical text wrapping)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF064E3B).copy(alpha = 0.85f),
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.8f))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "Level:",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFFA7F3D0),
                                                softWrap = false
                                            )
                                            Text(
                                                text = res.difficulty,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF34D399),
                                                softWrap = false
                                            )
                                        }
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

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
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

        // 6. Section Header: "📚 Practice Drills & Accent Coach" with single-line badge
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
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E144E).copy(alpha = 0.9f),
                border = BorderStroke(1.2.dp, Color(0xFF8B5CF6))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Text("🎧", fontSize = 12.sp)
                    Text(
                        "${drills.size} drills available",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // 7. Practice Drill Cards - Horizontally Swipable LazyRow for all drills!
        val drillGradients = listOf(
            Pair(
                Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1), Color(0xFF075985))),
                Color(0xFF38BDF8)
            ),
            Pair(
                Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9), Color(0xFF581C87))),
                Color(0xFFC084FC)
            ),
            Pair(
                Brush.verticalGradient(listOf(Color(0xFFF97316), Color(0xFFEA580C), Color(0xFFC2410C))),
                Color(0xFFFDBA74)
            ),
            Pair(
                Brush.verticalGradient(listOf(Color(0xFF059669), Color(0xFF047857), Color(0xFF065F46))),
                Color(0xFF34D399)
            ),
            Pair(
                Brush.verticalGradient(listOf(Color(0xFF4F46E5), Color(0xFF4338CA), Color(0xFF3730A3))),
                Color(0xFF818CF8)
            ),
            Pair(
                Brush.verticalGradient(listOf(Color(0xFFE11D48), Color(0xFFBE123C), Color(0xFF9F1239))),
                Color(0xFFFB7185)
            ),
            Pair(
                Brush.verticalGradient(listOf(Color(0xFF0891B2), Color(0xFF0E7490), Color(0xFF155E75))),
                Color(0xFF22D3EE)
            )
        )

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("practice_drills_lazy_row"),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp, vertical = 4.dp)
        ) {
            itemsIndexed(drills) { index, drill ->
                val (grad, border) = drillGradients[index % drillGradients.size]
                PhoneticsDrillScreenshotCard(
                    word = drill.wordOrSentence,
                    ipa = drill.ipaPhonetic,
                    focus = if (drill.audioFocus.startsWith("Focus:", ignoreCase = true)) drill.audioFocus else "Focus: ${drill.audioFocus}",
                    difficulty = drill.difficulty,
                    gradient = grad,
                    borderColor = border,
                    modifier = Modifier.width(168.dp),
                    onPlay = { viewModel.speakAudio(drill.wordOrSentence) },
                    onClick = {
                        viewModel.startPronunciationDrill(drill)
                    }
                )
            }
        }

        val context = LocalContext.current
        val micPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                viewModel.startSpeakingPractice()
            } else {
                Toast.makeText(context, "Microphone permission is required to capture your voice", Toast.LENGTH_SHORT).show()
            }
        }

        // Active Drill Interactive Practice Card when a drill is selected or default
        val effectiveDrill = activeDrill ?: drills.find { it.wordOrSentence.equals("Specifically", ignoreCase = true) } ?: drills.firstOrNull()
        effectiveDrill?.let { drill ->
            ActivePronunciationDrillCard(
                drill = drill,
                speechState = speechState,
                evaluation = evaluation,
                onClose = { viewModel.closePronunciationDrill() },
                onPlayNormal = { viewModel.speakAudio(drill.wordOrSentence, slow = false) },
                onPlaySlow = { viewModel.speakAudio(drill.wordOrSentence, slow = true) },
                onToggleMic = {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (speechState is SpeechState.Listening) {
                        viewModel.stopSpeakingPractice()
                    } else {
                        if (hasPerm) {
                            viewModel.startSpeakingPractice()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    }
                },
                onTestSampleVoice = {
                    viewModel.speakAudio(drill.wordOrSentence, slow = false)
                    viewModel.simulateVoiceInput(drill.wordOrSentence)
                }
            )
        }

        Spacer(Modifier.height(14.dp))
    }
}

/**
 * Phonetics Drill Card:
 * Rounded card with gradient background, circular speaker in top-right,
 * bold word, IPA pronunciation below, and subtle focus label.
 * Swipable and clickable to practice.
 */
@Composable
fun PhoneticsDrillScreenshotCard(
    word: String,
    ipa: String,
    focus: String,
    difficulty: String = "",
    gradient: Brush,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onPlay: () -> Unit,
    onClick: () -> Unit = onPlay
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.2.dp, borderColor.copy(alpha = 0.75f)),
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
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
                // Top row with difficulty badge and circular Speaker button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (difficulty.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.28f),
                            border = BorderStroke(0.6.dp, Color.White.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = difficulty,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.95f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.28f),
                        border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onPlay() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.VolumeUp,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }

                Text(
                    text = word,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.5.sp,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = ipa,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.92f),
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = focus,
                    fontSize = 10.5.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Soundwave bars canvas for flanking the target practice word.
 * Renders multiple animated vertical glowing pills in magenta, purple, and cyan.
 */
@Composable
fun SoundwaveBarsCanvas(
    isLeft: Boolean,
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bars")
    val animFactor by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "animFactor"
    )

    val baseHeights = if (isLeft) {
        listOf(8.dp, 14.dp, 22.dp, 36.dp, 24.dp, 16.dp, 8.dp)
    } else {
        listOf(8.dp, 16.dp, 24.dp, 36.dp, 22.dp, 14.dp, 8.dp)
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        baseHeights.forEachIndexed { i, h ->
            val scale = if (isListening) (if (i % 2 == 0) animFactor else (2f - animFactor)) else 1f
            Box(
                modifier = Modifier
                    .width(3.2.dp)
                    .height(h * scale)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF8B5CF6),
                                Color(0xFFEC4899),
                                Color(0xFF00D2FF)
                            )
                        )
                    )
            )
        }
    }
}

/**
 * 3D Lightbulb squircle with radiant glowing rays and blue gradient container.
 */
@Composable
fun GlowingLightbulbSquircle(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(1.2.dp, Color(0xFF60A5FA)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF1E40AF), Color(0xFF3B82F6))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(34.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Radiant yellow-gold aura
                drawCircle(
                    brush = Brush.radialGradient(
                        listOf(
                            Color(0xFFFDE047).copy(alpha = 0.50f),
                            Color(0xFFF59E0B).copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = size.width * 0.55f
                    ),
                    radius = size.width * 0.55f,
                    center = center
                )

                // Light rays
                val rayLength = 4.5.dp.toPx()
                val rayOffset = 12.dp.toPx()
                val angles = listOf(0f, 45f, 90f, 135f, 180f, 225f, 270f, 315f)
                for (angle in angles) {
                    val rad = Math.toRadians(angle.toDouble())
                    val x1 = cx + (rayOffset * Math.cos(rad)).toFloat()
                    val y1 = cy + (rayOffset * Math.sin(rad)).toFloat()
                    val x2 = cx + ((rayOffset + rayLength) * Math.cos(rad)).toFloat()
                    val y2 = cy + ((rayOffset + rayLength) * Math.sin(rad)).toFloat()
                    drawLine(
                        color = Color(0xFFFDE047),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 2.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            Icon(
                imageVector = Icons.Rounded.Lightbulb,
                contentDescription = "Tip",
                tint = Color(0xFFFACC15),
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

/**
 * Interactive Active Pronunciation Drill Practice Card:
 * Redesigned to match reference UI screenshot with:
 * - Glowing multi-stop neon gradient border (cyan -> purple -> pink -> cyan)
 * - Active Practice badge and circular close button
 * - Target word with soundwave bars and verified checkmark badge
 * - IPA pill with speaker icon
 * - Slow (0.7x) & Normal audio speed gradient buttons
 * - Coaching Tip card with 3D lightbulb squircle & cyan divider
 * - Pulsating concentric radar rings around primary microphone button
 * - Wide "Test with Sample Voice" button with audio and instant evaluation
 */
@Composable
fun ActivePronunciationDrillCard(
    drill: PronunciationDrill,
    speechState: SpeechState,
    evaluation: com.example.data.model.SpeakingEvaluation?,
    onClose: () -> Unit,
    onPlayNormal: () -> Unit,
    onPlaySlow: () -> Unit,
    onToggleMic: () -> Unit,
    onTestSampleVoice: () -> Unit = onPlayNormal
) {
    val isListening = speechState is SpeechState.Listening

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            2.dp,
            Brush.linearGradient(
                listOf(
                    Color(0xFF00D2FF), // Electric cyan
                    Color(0xFF818CF8), // Indigo
                    Color(0xFFEC4899), // Hot pink
                    Color(0xFF8B5CF6), // Purple
                    Color(0xFF00D2FF)  // Electric cyan
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_drill_practice_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF090D2E),
                            Color(0xFF130D3E),
                            Color(0xFF07051E)
                        )
                    )
                )
        ) {
            // Smooth wavy fluid curves at the bottom matching reference screenshot
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.BottomCenter)
            ) {
                val w = size.width
                val h = size.height

                // Violet wave
                val wave1 = Path().apply {
                    moveTo(0f, h * 0.70f)
                    cubicTo(w * 0.35f, h * 0.45f, w * 0.65f, h * 0.95f, w, h * 0.58f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = wave1,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF8B5CF6).copy(alpha = 0.28f),
                            Color(0xFFEC4899).copy(alpha = 0.20f)
                        )
                    )
                )

                // Cyan wave
                val wave2 = Path().apply {
                    moveTo(0f, h * 0.88f)
                    cubicTo(w * 0.40f, h * 0.68f, w * 0.70f, h * 1.05f, w, h * 0.76f)
                    lineTo(w, h)
                    lineTo(0f, h)
                    close()
                }
                drawPath(
                    path = wave2,
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color(0xFF00D2FF).copy(alpha = 0.16f),
                            Color(0xFF6366F1).copy(alpha = 0.16f)
                        )
                    )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Top Header Row: Active Practice badge & circular close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color(0xFF031C38).copy(alpha = 0.9f),
                        border = BorderStroke(1.5.dp, Color(0xFF00D2FF)),
                        modifier = Modifier.testTag("active_practice_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Mic,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = "Active Practice",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1B1748).copy(alpha = 0.95f),
                        border = BorderStroke(1.2.dp, Color(0xFF6366F1).copy(alpha = 0.75f)),
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onClose() }
                            .testTag("active_practice_close_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Drill",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // 2. Word Section: Soundwave bars + Target Word + Checkmark + IPA pill
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SoundwaveBarsCanvas(isLeft = true, isListening = isListening)

                        Spacer(Modifier.width(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = drill.wordOrSentence,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.width(8.dp))
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF00B4D8),
                                modifier = Modifier.size(20.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = "Verified Drill",
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(10.dp))

                        SoundwaveBarsCanvas(isLeft = false, isListening = isListening)
                    }

                    // Centered Phonetics Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF26195E).copy(alpha = 0.92f),
                        border = BorderStroke(1.2.dp, Color(0xFFA855F7).copy(alpha = 0.8f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onPlayNormal() }
                            .testTag("active_drill_ipa_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.VolumeUp,
                                contentDescription = "Listen Phonetic",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = drill.ipaPhonetic,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // 3. Audio Playback Speed Buttons (Slow 0.7x & Normal)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Slow (0.7x) Button
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.2.dp, Color(0xFFC084FC).copy(alpha = 0.85f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { onPlaySlow() }
                            .testTag("active_drill_slow_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF6B21A8), Color(0xFF9333EA))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Speed,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                                Text(
                                    text = "Slow (0.7x)",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }

                    // Normal Button
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = Color.Transparent,
                        border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.85f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .clickable { onPlayNormal() }
                            .testTag("active_drill_normal_btn")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF0284C7), Color(0xFF00B4D8))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.VolumeUp,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                                Text(
                                    text = "Normal",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }
                    }
                }

                // 4. Coaching Tip Card
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF061838).copy(alpha = 0.92f),
                    border = BorderStroke(1.4.dp, Color(0xFF00D2FF).copy(alpha = 0.85f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        GlowingLightbulbSquircle(modifier = Modifier.size(54.dp))

                        Spacer(Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .width(2.5.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Color(0xFF00D2FF))
                        )

                        Spacer(Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "Coaching Tip",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00E5FF),
                                maxLines = 1
                            )
                            val tipText = drill.tips.ifBlank {
                                "Start with a crisp pronunciation, stress the key syllables, and end clearly."
                            }
                            Text(
                                text = tipText,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }

                // 5. Voice Capture / Recording Section with Concentric Pulsating Radar Rings
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val infiniteTransition = rememberInfiniteTransition(label = "mic_waves")
                    val wavePulse by infiniteTransition.animateFloat(
                        initialValue = 1.0f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "wavePulse"
                    )
                    val waveAlpha by infiniteTransition.animateFloat(
                        initialValue = 0.45f,
                        targetValue = 0.85f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "waveAlpha"
                    )

                    Box(
                        modifier = Modifier
                            .size(136.dp)
                            .testTag("active_drill_mic_radar_container"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer glowing radar circle 1
                        Box(
                            modifier = Modifier
                                .size(136.dp * if (isListening) wavePulse else 1f)
                                .clip(CircleShape)
                                .background(
                                    if (isListening) Color(0xFFEF4444).copy(alpha = 0.12f)
                                    else Color(0xFF6366F1).copy(alpha = 0.15f)
                                )
                                .border(
                                    BorderStroke(
                                        1.dp,
                                        if (isListening) Color(0xFFEF4444).copy(alpha = 0.35f)
                                        else Color(0xFF6366F1).copy(alpha = 0.25f)
                                    ),
                                    CircleShape
                                )
                        )

                        // Outer glowing radar circle 2
                        Box(
                            modifier = Modifier
                                .size(110.dp * if (isListening) (wavePulse * 0.95f) else 1f)
                                .clip(CircleShape)
                                .background(
                                    if (isListening) Color(0xFFDC2626).copy(alpha = 0.18f)
                                    else Color(0xFF4F46E5).copy(alpha = 0.18f)
                                )
                                .border(
                                    BorderStroke(
                                        1.2.dp,
                                        if (isListening) Color(0xFFDC2626).copy(alpha = 0.55f)
                                        else Color(0xFF818CF8).copy(alpha = 0.40f)
                                    ),
                                    CircleShape
                                )
                        )

                        // Inner glowing halo ring 3
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(CircleShape)
                                .border(
                                    BorderStroke(
                                        1.5.dp,
                                        if (isListening) Color(0xFFFF416C).copy(alpha = waveAlpha)
                                        else Color(0xFF00D2FF).copy(alpha = waveAlpha)
                                    ),
                                    CircleShape
                                )
                        )

                        // Center Primary Microphone Button
                        Surface(
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = BorderStroke(2.5.dp, Color.White),
                            modifier = Modifier
                                .size(74.dp)
                                .clip(CircleShape)
                                .clickable { onToggleMic() }
                                .testTag("active_drill_mic_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            if (isListening) listOf(Color(0xFFFF416C), Color(0xFFFF4B2B), Color(0xFF990000))
                                            else listOf(Color(0xFF00F0FF), Color(0xFF0088FF), Color(0xFF1D4ED8))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                                    contentDescription = "Tap to Speak",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = when (speechState) {
                            is SpeechState.Listening -> "Listening... Tap to Stop"
                            is SpeechState.Processing -> "Analyzing Pronunciation..."
                            is SpeechState.Success -> "Tap to Speak Again"
                            is SpeechState.Error -> "Tap to Retry Speaking"
                            else -> "Tap to Speak"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }

                // 6. Test with Sample Voice Button
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF041838).copy(alpha = 0.95f),
                    border = BorderStroke(1.5.dp, Color(0xFF00D2FF)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .clickable { onTestSampleVoice() }
                        .testTag("active_drill_sample_voice_btn")
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.VolumeUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Test with Sample Voice",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // 7. Error or Guidance Hint if Speech encountered an issue
                if (speechState is SpeechState.Error) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF450A0A).copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = null,
                                tint = Color(0xFFFCA5A5),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = speechState.message,
                                fontSize = 12.sp,
                                color = Color(0xFFFEE2E2),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 8. Live Evaluation Results Card
                evaluation?.let { eval ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = Color(0xFF063A30).copy(alpha = 0.95f),
                        border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("active_drill_evaluation_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🎯", fontSize = 16.sp)
                                    Text(
                                        "Pronunciation Score: ${eval.overallScore}%",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color(0xFF34D399)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF047857),
                                    border = BorderStroke(1.dp, Color(0xFF6EE7B7))
                                ) {
                                    Text(
                                        text = if (eval.overallScore >= 75) "EXCELLENT" else "GOOD EFFORT",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (eval.spokenText.isNotBlank()) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🎙️ Heard:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                    Text(
                                        "\"${eval.spokenText}\"",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            if (eval.feedback.isNotBlank()) {
                                Text(
                                    text = eval.feedback,
                                    fontSize = 12.sp,
                                    color = Color(0xFFA7F3D0),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
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
