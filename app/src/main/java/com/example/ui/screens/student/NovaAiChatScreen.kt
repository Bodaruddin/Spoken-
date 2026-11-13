package com.example.ui.screens.student

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.SpeakingEvaluation
import com.example.data.model.SpeakingPrompt
import com.example.service.SpeechState
import com.example.ui.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * NovaAiChatScreen
 *
 * Pixel-perfect implementation matching the user-provided screenshot:
 * 1. Deep midnight purple background with warm horizon glow.
 * 2. Top bar: Back arrow, Nova Avatar, "Nova AI Chat" / "English Speaking Practice", Flame Streak badge (🔥 0).
 * 3. Search & Filter Bar: "Search lessons, topics or practice..." with circular purple gradient filter button.
 * 4. Lesson Banner Card: "Learn: Time Words (In, On, At)", "Practice real conversations with AI", Target bullseye button.
 * 5. Question Progress Indicator: "Question 1 of 5", 5-segment glowing purple progress bar, "20%" badge.
 * 6. Chat Bubbles:
 *    - Nova Greeting with robot avatar
 *    - Nova Question with robot avatar + "Tap to listen" audio player & waveform visualizer
 *    - Student responses with CEFR score badges
 * 7. Glowing Pulsating Microphone: Waveform lines on left/right, multi-ring neon aura, "Tap to speak".
 * 8. Bottom Search Input Bar: "Search here..." with send button.
 * 9. Action Buttons: 💡 Hint, 📖 Examples, 🔤 Translate, 🔄 Next.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun NovaAiChatScreen(
    viewModel: MainViewModel,
    speechState: SpeechState,
    evaluation: SpeakingEvaluation?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val studentProfile by viewModel.studentProfile.collectAsState()
    val allTopics by viewModel.speakingPrompts.collectAsState()
    val activeTopic by viewModel.activeConversationTopic.collectAsState()
    val isAiEvaluating by viewModel.isAiEvaluating.collectAsState()

    // Handle system back navigation
    BackHandler {
        onBack()
    }

    // Audio recording permission launcher
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startSpeakingPractice()
        } else {
            Toast.makeText(context, "Microphone permission is needed to speak with Nova", Toast.LENGTH_SHORT).show()
        }
    }

    // Always resolve the freshest SpeakingPrompt from allTopics matching activeTopic.id
    val currentTopic = remember(activeTopic, allTopics) {
        val targetId = activeTopic?.id ?: allTopics.firstOrNull()?.id
        allTopics.firstOrNull { it.id == targetId }
            ?: activeTopic
            ?: allTopics.firstOrNull()
            ?: SpeakingPrompt(
                id = "topic_time_words",
                title = "Time Words (In, On, At)",
                category = "Grammar & Speaking",
                question = "When do you usually go to school?",
                followUpTip = "Practice using time prepositions: 'at 8 o'clock', 'in the morning', or 'on weekdays'.",
                recommendedKeywords = listOf("in the morning", "at 8:00 AM", "on weekdays", "usually", "schedule"),
                minSpeakingSeconds = 20,
                emoji = "🕒",
                difficultyLevel = "Beginner",
                createdBy = "Faculty"
            )
    }

    val chatMessages by viewModel.novaChatMessages.collectAsState()
    val turnCount by viewModel.novaTurnCount.collectAsState()
    val currentHint by viewModel.novaCurrentHint.collectAsState()
    val currentExamples by viewModel.novaCurrentExamples.collectAsState()

    // Load or restore persistent conversation session for this student and topic
    LaunchedEffect(currentTopic.id) {
        viewModel.loadNovaSession(currentTopic)
    }

    // Modals state
    var showHintDialog by remember { mutableStateOf(false) }
    var showExamplesDialog by remember { mutableStateOf(false) }
    var showTranslateDialog by remember { mutableStateOf(false) }
    var showRestartConfirmDialog by remember { mutableStateOf(false) }
    var showTopicPickerDialog by remember { mutableStateOf(false) }
    var showTopicFilterSheet by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var bottomInputText by remember { mutableStateOf("") }
    val isTtsEngineSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    var manualTtsPlaying by remember { mutableStateOf(false) }
    val isTtsPlaying = isTtsEngineSpeaking || manualTtsPlaying

    val listState = rememberLazyListState()

    // Auto-scroll chat to latest message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    // Pulsing animation for microphone button
    val isListening = speechState is SpeechState.Listening
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.22f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 700 else 1800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val auraAlpha by infiniteTransition.animateFloat(
        initialValue = if (isListening) 0.6f else 0.25f,
        targetValue = if (isListening) 0.95f else 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (isListening) 700 else 1800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "aura_alpha"
    )

    // Background gradient: dark night purple into glowing horizon
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF09061A), // deep space purple
                        Color(0xFF0F0B28),
                        Color(0xFF190E38),
                        Color(0xFF28114E), // horizon glow
                        Color(0xFF120826)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("nova_ai_chat_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(4.dp))

            // =========================================================================
            // 1. TOP HEADER BAR matching screenshot
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .testTag("nova_chat_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(Modifier.width(4.dp))

                // Robot Avatar with glowing neon ring
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(46.dp)
                ) {
                    // Soft neon circle
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        Color(0xFF8B5CF6).copy(alpha = 0.5f),
                                        Color(0xFF4C1D95).copy(alpha = 0.2f)
                                    )
                                )
                            )
                    )
                    Image(
                        painter = painterResource(id = R.drawable.img_nova_ai_robot),
                        contentDescription = "Nova AI",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFF818CF8), CircleShape)
                    )
                }

                Spacer(Modifier.width(10.dp))

                // Header Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Nova AI Chat",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        letterSpacing = 0.2.sp
                    )
                    Text(
                        text = "English Speaking Practice",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFC4B5FD)
                    )
                }

                // Restart Conversation Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E1438),
                    border = BorderStroke(1.dp, Color(0xFF8B5CF6).copy(alpha = 0.5f)),
                    modifier = Modifier
                        .height(34.dp)
                        .clickable { showRestartConfirmDialog = true }
                        .testTag("restart_conversation_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Restart Conversation",
                            tint = Color(0xFFC084FC),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Restart",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE9D5FF)
                        )
                    }
                }

                Spacer(Modifier.width(6.dp))

                // Flame Streak Capsule (🔥 0)
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1E1438),
                    border = BorderStroke(1.dp, Color(0xFF432C6E)),
                    modifier = Modifier.height(34.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🔥", fontSize = 15.sp)
                        Text(
                            text = "${studentProfile.streakDays}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFB74D)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            val globalMilestone by viewModel.novaMilestoneTarget.collectAsState()
            val milestoneTurns = remember(currentTopic, globalMilestone) {
                if (currentTopic.milestoneTurns > 0) currentTopic.milestoneTurns else globalMilestone
            }
            val currentTurnDisplay = turnCount.coerceAtLeast(1)
            val percentVal = ((currentTurnDisplay.coerceAtMost(milestoneTurns) * 100) / milestoneTurns)

            // =========================================================================
            // 2. COMPACT TOPIC & PROGRESS BANNER (Space-optimized for chat visibility)
            // =========================================================================
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1B113B).copy(alpha = 0.95f),
                border = BorderStroke(
                    1.2.dp,
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFA855F7),
                            Color(0xFF6366F1),
                            Color(0xFF38BDF8)
                        )
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("nova_active_lesson_card")
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currentTopic.emoji.ifBlank { "⏰" },
                            fontSize = 20.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Topic: ${currentTopic.title}",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Dynamic Multi-Turn Conversation",
                                fontSize = 11.sp,
                                color = Color(0xFFC4B5FD)
                            )
                        }
                        // Change Topic Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2E1B5B),
                            border = BorderStroke(1.dp, Color(0xFF818CF8)),
                            modifier = Modifier
                                .clickable { showTopicPickerDialog = true }
                                .testTag("topic_bullseye_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("Topics", fontSize = 11.sp, color = Color(0xFFE0E7FF), fontWeight = FontWeight.SemiBold)
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = "Change Topic",
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }

                    // Progress row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (currentTurnDisplay >= milestoneTurns) "Turn $currentTurnDisplay • Milestone 🏆" else "Turn $currentTurnDisplay • Live Dialogue",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE2E8F0)
                        )
                        // Dynamic segmented bars matching milestoneTurns
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(if (milestoneTurns > 10) 1.5.dp else 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (i in 0 until milestoneTurns) {
                                val isFilled = i < currentTurnDisplay
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (isFilled) {
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFFC084FC), Color(0xFFA855F7))
                                                )
                                            } else {
                                                SolidColor(Color(0xFF332060))
                                            }
                                        )
                                )
                            }
                        }
                        Text(
                            text = if (currentTurnDisplay >= milestoneTurns) "Goal Met!" else "$percentVal%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA7F3D0)
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // =========================================================================
            // 5. CHAT MESSAGES AREA (Scrollable)
            // =========================================================================
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(chatMessages, key = { it.id }) { msg ->
                    if (msg.isNova) {
                        NovaChatBubble(
                            message = msg,
                            isPlayingAudio = isTtsPlaying,
                            onPlayAudio = { text ->
                                manualTtsPlaying = true
                                viewModel.speakAudio(text)
                                coroutineScope.launch {
                                    delay(700)
                                    manualTtsPlaying = false
                                }
                            }
                        )
                    } else {
                        StudentChatBubble(message = msg)
                    }
                }

                // Analyzing indicator when student speech is being evaluated
                if (isAiEvaluating) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(start = 48.dp, top = 4.dp)
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = Color(0xFFC084FC),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Nova AI is evaluating your speech...",
                                fontSize = 12.sp,
                                color = Color(0xFFC4B5FD)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // =========================================================================
            // 6. GLOWING NEON MICROPHONE SECTION with Pulsing Waveforms
            // =========================================================================
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Left Waveform Bars
                    ChatWaveformVisualizer(
                        modifier = Modifier.weight(1f),
                        isActive = isListening || isTtsPlaying
                    )

                    Spacer(Modifier.width(8.dp))

                    // Multi-Ring Glowing Neon Microphone Button
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(86.dp)
                            .scale(pulseScale)
                            .testTag("nova_mic_button")
                    ) {
                        // Outermost diffused neon aura
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            Color(0xFF8B5CF6).copy(alpha = auraAlpha),
                                            Color(0xFF38BDF8).copy(alpha = auraAlpha * 0.4f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Middle neon ring
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .border(
                                    2.dp,
                                    Brush.sweepGradient(
                                        listOf(
                                            Color(0xFF60A5FA),
                                            Color(0xFFC084FC),
                                            Color(0xFFA855F7),
                                            Color(0xFF38BDF8)
                                        )
                                    ),
                                    CircleShape
                                )
                        )

                        // Inner circular action button
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color(0xFF38BDF8),
                                            Color(0xFF4F46E5),
                                            Color(0xFF7C3AED)
                                        )
                                    )
                                )
                                .clickable {
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
                                }
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                                contentDescription = if (isListening) "Stop Speaking" else "Tap to Speak",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Right Waveform Bars
                    ChatWaveformVisualizer(
                        modifier = Modifier.weight(1f),
                        isActive = isListening || isTtsPlaying
                    )
                }

                Spacer(Modifier.height(4.dp))

                // Microphone Status / Action Label
                Text(
                    text = when {
                        isListening -> "Listening... speak now 🎙️"
                        isAiEvaluating -> "Nova AI is evaluating..."
                        else -> "Tap to speak"
                    },
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isListening) Color(0xFF67E8F9) else Color(0xFFE2E8F0),
                    modifier = Modifier.clickable {
                        // Quick simulation option for emulator users
                        if (!isListening && !isAiEvaluating) {
                            val activeQ = viewModel.activeCoachQuestion.value.ifBlank { currentTopic.question }
                            val usableCurrent = currentExamples.firstOrNull()?.takeIf {
                                !com.example.util.ExampleAnswerGenerator.isLegacyGenericExampleSet(listOf(it))
                            }
                            val sample = usableCurrent
                                ?: com.example.util.ExampleAnswerGenerator.generateForQuestion(activeQ, currentTopic).first()
                            viewModel.evaluateInputSpeech(sample)
                        }
                    }
                )
            }

            Spacer(Modifier.height(10.dp))

            // =========================================================================
            // 7. BOTTOM SEARCH / QUICK TEXT INPUT FIELD
            // =========================================================================
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF140D32),
                border = BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF7C3AED),
                            Color(0xFFA855F7),
                            Color(0xFF6366F1)
                        )
                    )
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("nova_bottom_input")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChatBubbleOutline,
                        contentDescription = "Type response",
                        tint = Color(0xFFC4B5FD),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(Modifier.width(8.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        if (bottomInputText.isEmpty()) {
                            Text(
                                text = "Type your response or tap mic...",
                                color = Color(0xFF8B7FA8),
                                fontSize = 13.sp
                            )
                        }
                        BasicTextField(
                            value = bottomInputText,
                            onValueChange = { bottomInputText = it },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 13.sp
                            ),
                            singleLine = true,
                            cursorBrush = SolidColor(Color(0xFFC084FC)),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (bottomInputText.isNotBlank()) {
                                        val typed = bottomInputText.trim()
                                        bottomInputText = ""
                                        viewModel.evaluateInputSpeech(typed)
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (bottomInputText.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val typed = bottomInputText.trim()
                                bottomInputText = ""
                                viewModel.evaluateInputSpeech(typed)
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Send,
                                contentDescription = "Send",
                                tint = Color(0xFFC084FC),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            // =========================================================================
            // 8. BOTTOM 4 ACTION PILLS: 💡 Hint, 📖 Examples, 🔤 Translate, 🔄 Next
            // =========================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionPillButton(
                    icon = "💡",
                    label = "Hint",
                    borderColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f),
                    onClick = { showHintDialog = true }
                )

                ActionPillButton(
                    icon = "📖",
                    label = "Examples",
                    borderColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1.1f),
                    onClick = { showExamplesDialog = true }
                )

                ActionPillButton(
                    icon = "🔤",
                    label = "Translate",
                    borderColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1.1f),
                    onClick = { showTranslateDialog = true }
                )

                ActionPillButton(
                    icon = "🔄",
                    label = "Next",
                    borderColor = Color(0xFF7C3AED),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.advanceToNextNovaPrompt()
                    }
                )
            }
        }
    }

    // =========================================================================
    // MODALS: Hint, Examples, Translate, Topic Switcher
    // =========================================================================

    // 1. Hint Dialog
    if (showHintDialog) {
        Dialog(onDismissRequest = { showHintDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1B133B),
                border = BorderStroke(1.5.dp, Color(0xFFA855F7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💡", fontSize = 24.sp)
                        Text(
                            "Speaking & Grammar Hint",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Text(
                        text = currentHint.ifBlank { currentTopic.followUpTip.ifBlank { "Express your thoughts clearly using complete sentences and natural pronunciation." } },
                        fontSize = 14.sp,
                        color = Color(0xFFDDD6FE),
                        lineHeight = 20.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF281A54),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Target Vocabulary / Phrases:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF67E8F9)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                currentTopic.recommendedKeywords.joinToString(" • "),
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }

                    Button(
                        onClick = { showHintDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Got it!", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 2. Examples Dialog
    if (showExamplesDialog) {
        Dialog(onDismissRequest = { showExamplesDialog = false }) {
            val activeCoachQuestion by viewModel.activeCoachQuestion.collectAsState()
            val rawQuestion = activeCoachQuestion.ifBlank { currentTopic.question }
            val cleanQuestion = remember(rawQuestion) {
                com.example.util.AssameseTranslator.extractCleanQuestion(rawQuestion)
            }
            val curatedMatch = remember(cleanQuestion, currentTopic) {
                com.example.util.ExampleAnswerGenerator.matchCuratedExamples(cleanQuestion, currentTopic)
            }
            val lastNovaHasLiveAiEval = remember(chatMessages) {
                chatMessages.lastOrNull { it.isNova }?.evaluation != null
            }
            val isGeminiConfigured by viewModel.isGeminiConfigured.collectAsState()
            var isExamplesAiLoading by remember { mutableStateOf(false) }

            var modelExamples by remember(cleanQuestion, currentTopic.id, currentExamples) {
                val usableCurrent = currentExamples.takeIf {
                    it.isNotEmpty() && !com.example.util.ExampleAnswerGenerator.isLegacyGenericExampleSet(it)
                }
                val initial = when {
                    curatedMatch != null -> curatedMatch
                    lastNovaHasLiveAiEval && usableCurrent != null -> usableCurrent
                    usableCurrent != null -> usableCurrent
                    else -> com.example.util.ExampleAnswerGenerator.generateForQuestion(cleanQuestion, currentTopic)
                }
                mutableStateOf(initial)
            }

            // For custom / open-ended questions without a curated match or live turn eval, fetch Gemini AI examples if configured
            LaunchedEffect(cleanQuestion, currentTopic.id, isGeminiConfigured) {
                if (isGeminiConfigured && curatedMatch == null && !lastNovaHasLiveAiEval) {
                    isExamplesAiLoading = true
                    try {
                        val aiList = viewModel.generateExamplesForCurrentQuestion(cleanQuestion, currentTopic)
                        if (aiList.isNotEmpty()) {
                            modelExamples = aiList
                        }
                    } catch (_: Throwable) {
                    } finally {
                        isExamplesAiLoading = false
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1B133B),
                border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📖", fontSize = 24.sp)
                            Text(
                                "Example Model Answers",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        if (isExamplesAiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color(0xFF38BDF8),
                                strokeWidth = 2.dp
                            )
                        }
                    }

                    Text(
                        "Here are native conversational responses you can speak:",
                        fontSize = 12.5.sp,
                        color = Color(0xFFCBD5E1)
                    )

                    modelExamples.forEachIndexed { idx, ex ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF24184E),
                            border = BorderStroke(1.dp, Color(0xFF4C3080)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        "Example ${idx + 1}:",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "“$ex”",
                                        fontSize = 13.5.sp,
                                        color = Color.White
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.speakAudio(ex) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.VolumeUp,
                                        contentDescription = "Listen Example",
                                        tint = Color(0xFF67E8F9),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showExamplesDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Practice Speaking This", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 3. Translate Dialog
    if (showTranslateDialog) {
        Dialog(onDismissRequest = { showTranslateDialog = false }) {
            val activeCoachQuestion by viewModel.activeCoachQuestion.collectAsState()
            val rawQuestion = activeCoachQuestion.ifBlank { currentTopic.question }
            val cleanQuestion = remember(rawQuestion) {
                com.example.util.AssameseTranslator.extractCleanQuestion(rawQuestion)
            }
            val isTimeWordsTopic = currentTopic.title.contains("Time Words", ignoreCase = true) ||
                    currentTopic.id == "topic_time_words"
            val lessonQuestions = remember(currentTopic) { getQuestionsForTopic(currentTopic) }
            val matchingLessonQ = remember(cleanQuestion, rawQuestion, lessonQuestions) {
                lessonQuestions.firstOrNull {
                    it.questionText.trim().equals(cleanQuestion.trim(), ignoreCase = true) ||
                            rawQuestion.contains(it.questionText.trim(), ignoreCase = true)
                }
            }
            val bespokeAssamese = remember(matchingLessonQ, isTimeWordsTopic) {
                if (matchingLessonQ != null && (isTimeWordsTopic || matchingLessonQ.questionNumber != 1)) {
                    matchingLessonQ.assameseTranslation
                } else {
                    null
                }
            }

            var translationResult by remember(cleanQuestion, bespokeAssamese) {
                mutableStateOf(
                    com.example.util.AssameseTranslator.translate(
                        cleanQuestion,
                        bespokeAssamese
                    )
                )
            }
            var isAiLoading by remember { mutableStateOf(false) }
            val isGeminiConfigured by viewModel.isGeminiConfigured.collectAsState()
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            var copiedNotification by remember { mutableStateOf(false) }

            // If Gemini AI is active and not already a bespoke pre-curated lesson question, fetch live AI translation
            LaunchedEffect(cleanQuestion, isGeminiConfigured, bespokeAssamese) {
                if (isGeminiConfigured && bespokeAssamese == null) {
                    isAiLoading = true
                    try {
                        val aiResult = viewModel.translateToAssamese(cleanQuestion)
                        if (aiResult.assameseText.isNotBlank()) {
                            translationResult = aiResult
                        }
                    } catch (_: Throwable) {
                    } finally {
                        isAiLoading = false
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1B133B),
                border = BorderStroke(1.5.dp, Color(0xFFC084FC)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("nova_translate_dialog")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🔤", fontSize = 24.sp)
                            Column {
                                Text(
                                    "Question Translation",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "অসমীয়া ভাষাত অৰ্থ আৰু উচ্চাৰণ",
                                    fontSize = 11.sp,
                                    color = Color(0xFFC4B5FD)
                                )
                            }
                        }
                        if (isAiLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Color(0xFF38BDF8),
                                strokeWidth = 2.dp
                            )
                        } else if (isGeminiConfigured) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8))
                            ) {
                                Text(
                                    "✨ AI Active",
                                    fontSize = 10.sp,
                                    color = Color(0xFF7DD3FC),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    // English Question Prompt
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF281A54),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("English Question / Prompt:", fontSize = 11.sp, color = Color(0xFFC4B5FD), fontWeight = FontWeight.SemiBold)
                                IconButton(
                                    onClick = { viewModel.speakAudio(cleanQuestion) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.VolumeUp,
                                        contentDescription = "Listen English",
                                        tint = Color(0xFFC084FC),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(cleanQuestion, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }

                    // Assamese Translation Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF201347),
                        border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("অসমীয়া (Assamese):", fontSize = 11.5.sp, color = Color(0xFF67E8F9), fontWeight = FontWeight.Bold)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(translationResult.assameseText))
                                            copiedNotification = true
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ContentCopy,
                                            contentDescription = "Copy Translation",
                                            tint = Color(0xFF67E8F9),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = translationResult.assameseText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 22.sp
                            )
                            if (translationResult.transliteration.isNotBlank()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    text = "উচ্চাৰণ: ${translationResult.transliteration}",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFFA7F3D0),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (copiedNotification) {
                                Spacer(Modifier.height(4.dp))
                                Text("অনুবাদ কপি কৰা হ'ল (Copied!)", fontSize = 10.5.sp, color = Color(0xFF34D399))
                            }
                        }
                    }

                    // Word breakdown if available
                    if (translationResult.wordBreakdown.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF281A54),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("শব্দাৰ্থ (Key Words Breakdown):", fontSize = 11.sp, color = Color(0xFFFDE68A), fontWeight = FontWeight.SemiBold)
                                Spacer(Modifier.height(6.dp))
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    translationResult.wordBreakdown.take(6).forEach { (word, meaning) ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF1E1038),
                                            border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.5f))
                                        ) {
                                            Text(
                                                text = "$word: $meaning",
                                                fontSize = 11.sp,
                                                color = Color(0xFFE2E8F0),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bengali / Hindi Guidance
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF281A54),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("বাংলা / हिन्दी (Guidance):", fontSize = 10.5.sp, color = Color(0xFFF472B6), fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            val guideText = matchingLessonQ?.bengaliHindiTranslation
                                ?: "এই প্রশ্নের উত্তরে আপনার স্বাভাবিক অভিজ্ঞতা বা অভ্যাস সম্পূর্ণ বাক্যে বলুন। / इस प्रश्न के उत्तर में अपना अनुभव पूरे वाक्य में बोलें।"
                            Text(guideText, fontSize = 12.sp, color = Color(0xFFF1F5F9))
                        }
                    }

                    Button(
                        onClick = { showTranslateDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("close_translation_dialog_btn")
                    ) {
                        Text("বুজি পালোঁ (Understood)", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // 3.5 Restart Conversation Confirmation Dialog
    if (showRestartConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showRestartConfirmDialog = false },
            title = {
                Text("Restart Conversation?", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "This will clear the current dialogue history for “${currentTopic.title}” and start fresh with Nova. Your profile XP and coins remain saved.",
                    color = Color(0xFFDDD6FE),
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestartConfirmDialog = false
                        viewModel.restartNovaSession(currentTopic)
                        Toast.makeText(context, "Conversation restarted with Nova", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6))
                ) {
                    Text("Restart", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartConfirmDialog = false }) {
                    Text("Cancel", color = Color(0xFFC4B5FD))
                }
            },
            containerColor = Color(0xFF1B133B),
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 4. Topic Picker / Filter Modal
    if (showTopicPickerDialog) {
        Dialog(onDismissRequest = { showTopicPickerDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF170E35),
                border = BorderStroke(1.5.dp, Color(0xFFA855F7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Choose Practice Topic",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(
                            onClick = { showTopicPickerDialog = false },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Rounded.Close, "Close", tint = Color.White)
                        }
                    }

                    // Search input within dialog
                    OutlinedTextField(
                        value = searchText,
                        onValueChange = { searchText = it },
                        placeholder = { Text("Filter by keyword...", fontSize = 13.sp, color = Color(0xFF8B7FA8)) },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Rounded.Search, null, tint = Color(0xFFC084FC)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFA855F7),
                            unfocusedBorderColor = Color(0xFF4C2A85),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    val filteredTopics = allTopics.filter {
                        searchText.isBlank() || it.title.contains(searchText, ignoreCase = true) ||
                                it.category.contains(searchText, ignoreCase = true)
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredTopics, key = { it.id }) { topic ->
                            val isSelected = topic.id == currentTopic.id
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) Color(0xFF3B1A6E) else Color(0xFF22164A),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF38BDF8) else Color(0xFF3B2568)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.selectConversationTopic(topic)
                                        showTopicPickerDialog = false
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(topic.emoji.ifBlank { "💬" }, fontSize = 24.sp)
                                    Spacer(Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            topic.title,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            "${topic.category} • ${topic.difficultyLevel}",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFFDDD6FE)
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Rounded.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// SUB-COMPONENTS
// =========================================================================

/**
 * Nova Chat Bubble with Robot Avatar on left
 */
@Composable
fun NovaChatBubble(
    message: ChatMessage,
    isPlayingAudio: Boolean,
    onPlayAudio: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // Nova Robot Avatar
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(40.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_nova_ai_robot),
                contentDescription = "Nova",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, Color(0xFF818CF8), CircleShape)
            )
        }

        Spacer(Modifier.width(10.dp))

        // Dark Violet Speech Bubble with subtle neon border
        Surface(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 20.dp,
                bottomEnd = 20.dp,
                bottomStart = 20.dp
            ),
            color = Color(0xFF1C133B),
            border = BorderStroke(1.2.dp, Color(0xFF4C2A85)),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                if (message.evaluation != null) {
                    val eval = message.evaluation
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF311A60),
                        border = BorderStroke(1.dp, Color(0xFF6D28D9)),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            text = "Score: ${eval.overallScore}/100 • CEFR ${eval.cefrLevel} • Fluency ${eval.fluencyScore}%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF67E8F9),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = message.text,
                    fontSize = 14.5.sp,
                    color = Color.White,
                    lineHeight = 22.sp,
                    fontWeight = if (message.isQuestion) FontWeight.Medium else FontWeight.Normal
                )

                // If audio prompt is available, render clean audio button
                if (!message.audioPrompt.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF26194E),
                        border = BorderStroke(1.dp, Color(0xFF5B21B6)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPlayAudio(message.audioPrompt) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color(0xFF8B5CF6),
                                                Color(0xFF6366F1)
                                            )
                                        )
                                    )
                            ) {
                                Icon(
                                    imageVector = if (isPlayingAudio) Icons.Rounded.GraphicEq else Icons.Rounded.VolumeUp,
                                    contentDescription = "Tap to listen",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            Text(
                                text = if (isPlayingAudio) "Nova is speaking..." else "Tap to hear Nova speak",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFC4B5FD)
                            )

                            Spacer(Modifier.weight(1f))

                            AudioBarWaveformMini(isActive = isPlayingAudio)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Student Chat Bubble aligned right
 */
@Composable
fun StudentChatBubble(message: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 4.dp,
                bottomEnd = 20.dp,
                bottomStart = 20.dp
            ),
            color = Color(0xFF4338CA),
            border = BorderStroke(1.dp, Color(0xFF818CF8)),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp
                )
                if (message.evaluation != null) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "🎯 CEFR: ${message.evaluation.cefrLevel} (${message.evaluation.overallScore}%)",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF67E8F9)
                    )
                }
            }
        }
    }
}

/**
 * Audio waveform visualizer lines
 */
@Composable
fun ChatWaveformVisualizer(
    modifier: Modifier = Modifier,
    isActive: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val heights = listOf(8, 16, 24, 12, 28, 18, 10, 22, 14, 8)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        heights.forEachIndexed { idx, baseH ->
            val animFactor by infiniteTransition.animateFloat(
                initialValue = 0.6f,
                targetValue = 1.4f,
                animationSpec = infiniteRepeatable(
                    animation = tween(400 + idx * 80, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$idx"
            )

            val currentH = if (isActive) (baseH * animFactor).coerceIn(6f, 32f) else baseH.toFloat()

            Box(
                modifier = Modifier
                    .padding(horizontal = 1.5.dp)
                    .width(3.dp)
                    .height(currentH.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFFC4B5FD).copy(alpha = if (isActive) 0.9f else 0.45f))
            )
        }
    }
}

/**
 * Mini audio bar waveform inside speech bubble
 */
@Composable
fun AudioBarWaveformMini(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "mini_wave")
    val heights = listOf(6, 12, 18, 14, 22, 16, 8, 14, 10, 6)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(1.5.dp)
    ) {
        heights.forEachIndexed { idx, h ->
            val animFactor by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1.3f,
                animationSpec = infiniteRepeatable(
                    animation = tween(350 + idx * 60, easing = EaseInOutSine),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "mini_bar_$idx"
            )

            val currentH = if (isActive) (h * animFactor).coerceIn(4f, 22f) else h.toFloat()

            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(currentH.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(Color(0xFFC4B5FD))
            )
        }
    }
}

/**
 * Bottom 4 Action Pill Button
 */
@Composable
fun ActionPillButton(
    icon: String,
    label: String,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF170F35),
        border = BorderStroke(1.2.dp, Color(0xFF5B358F)),
        modifier = modifier
            .height(42.dp)
            .clickable(onClick = onClick)
            .testTag("action_pill_${label.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = icon, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

// =========================================================================
// DATA MODELS & QUESTION REPOSITORY
// =========================================================================

typealias ChatMessage = com.example.data.model.NovaChatMessage

data class TopicLessonQuestion(
    val questionNumber: Int,
    val questionText: String,
    val hintText: String,
    val exampleAnswers: List<String>,
    val assameseTranslation: String,
    val bengaliHindiTranslation: String
)

fun getQuestionsForTopic(topic: SpeakingPrompt): List<TopicLessonQuestion> {
    val q1Text = topic.question.ifBlank { "When do you usually go to school?" }
    val q1Tip = topic.followUpTip.ifBlank { "Use 'at' for clock times and 'in the morning' or 'on weekdays'." }
    return when {
        topic.title.contains("Time Words", ignoreCase = true) || topic.id == "topic_time_words" -> {
            listOf(
                TopicLessonQuestion(
                    questionNumber = 1,
                    questionText = q1Text,
                    hintText = q1Tip,
                    exampleAnswers = listOf(
                        "I usually go to school at 8:00 AM on weekdays.",
                        "I leave for school at 8:30 in the morning."
                    ),
                    assameseTranslation = "তুমি সাধাৰণতে কেতিয়া স্কুললৈ যোৱা?",
                    bengaliHindiTranslation = "তুমি সাধারণত কখন স্কুলে যাও? / आप आमतौर पर स्कूल कब जाते हैं?"
                ),
                TopicLessonQuestion(
                    questionNumber = 2,
                    questionText = "What time do you wake up in the morning?",
                    hintText = "Use 'in the morning' and 'at [time]': 'I wake up at 6:30 AM.'",
                    exampleAnswers = listOf(
                        "I usually wake up at 6:30 in the morning every day.",
                        "On weekends, I wake up a bit later at 8:00 AM."
                    ),
                    assameseTranslation = "তুমি ৰাতিপুৱা কেইবজাত সাৰ পোৱা?",
                    bengaliHindiTranslation = "তুমি সকালে কখন ওঠো? / आप सुबह कितने बजे उठते हैं?"
                ),
                TopicLessonQuestion(
                    questionNumber = 3,
                    questionText = "What activities do you like doing on Sundays?",
                    hintText = "Use 'on' with days of the week: 'On Sundays, I like to...'",
                    exampleAnswers = listOf(
                        "On Sundays, I enjoy playing football with my friends in the evening.",
                        "On Sundays, I spend time reading books and helping my parents."
                    ),
                    assameseTranslation = "দেওবাৰে তুমি কি কৰিবলৈ ভাল পোৱা?",
                    bengaliHindiTranslation = "রবিবারে তুমি কী করতে ভালোবাসো? / रविवार को आप क्या करना पसंद करते हैं?"
                ),
                TopicLessonQuestion(
                    questionNumber = 4,
                    questionText = "In which month is your birthday, and what do you do on that day?",
                    hintText = "Use 'in' for months and 'on' for days: 'My birthday is in October. On that day...'",
                    exampleAnswers = listOf(
                        "My birthday is in March. On that day, I celebrate with my family.",
                        "It is in November, and we always share sweets with neighbors."
                    ),
                    assameseTranslation = "তোমাৰ জন্মদিন কোন মাহত, আৰু সেইদিনা তুমি কি কৰা?",
                    bengaliHindiTranslation = "তোমার জন্মদিন কোন মাসে? / आपका जन्मदिन किस महीने में है?"
                ),
                TopicLessonQuestion(
                    questionNumber = 5,
                    questionText = "Where will you be at midnight on New Year's Eve?",
                    hintText = "Use 'at midnight' and 'in January': 'At midnight, I will be...'",
                    exampleAnswers = listOf(
                        "At midnight, I will be watching the fireworks countdown with my family.",
                        "I'll be at home enjoying dinner and wishing my friends a Happy New Year."
                    ),
                    assameseTranslation = "নতুন বছৰৰ নিশা ১২ বজাত তুমি ক'ত থাকিবা?",
                    bengaliHindiTranslation = "নতুন বছরের মধ্যরাতে তুমি কোথায় থাকবে? / नए साल की आधी रात को आप कहाँ होंगे?"
                )
            )
        }
        else -> {
            // Generates 5 structured progressive questions for any chosen topic
            listOf(
                TopicLessonQuestion(
                    questionNumber = 1,
                    questionText = topic.question,
                    hintText = topic.followUpTip.ifBlank { "Express your core thoughts clearly using complete sentences." },
                    exampleAnswers = com.example.util.ExampleAnswerGenerator.generateForQuestion(topic.question, topic),
                    assameseTranslation = com.example.util.AssameseTranslator.translate(topic.question).assameseText,
                    bengaliHindiTranslation = "এই বিষয়ে আপনার মতামত জানান।"
                ),
                TopicLessonQuestion(
                    questionNumber = 2,
                    questionText = "Why is ${topic.title} important to you personally?",
                    hintText = "Use connecting phrases like 'Firstly', 'In addition', or 'Because of this'.",
                    exampleAnswers = listOf(
                        "It is very important to me because it broadens my perspective.",
                        "Firstly, it allows me to stay productive and connect with people."
                    ),
                    assameseTranslation = "এইটো তোমাৰ বাবে কিয় গুৰুত্বপূৰ্ণ?",
                    bengaliHindiTranslation = "এটা আপনার কাছে কেন গুরুত্বপূর্ণ?"
                ),
                TopicLessonQuestion(
                    questionNumber = 3,
                    questionText = "Can you share a specific memorable moment related to this?",
                    hintText = "Use past tense verbs: 'I visited', 'I experienced', 'We organized'.",
                    exampleAnswers = listOf(
                        "A few months ago, I had a great opportunity to explore this directly.",
                        "One memorable moment was last summer when I participated with my classmates."
                    ),
                    assameseTranslation = "এই সন্দৰ্ভত এটা স্মৰণীয় মুহূৰ্ত ক'ব পাৰিবানে?",
                    bengaliHindiTranslation = "এই বিষয়ে একটি স্মরণীয় ঘটনা বলুন।"
                ),
                TopicLessonQuestion(
                    questionNumber = 4,
                    questionText = "What challenges do beginners face with ${topic.title}, and how can they improve?",
                    hintText = "Use suggestion phrases: 'In my view', 'They should practice regularly'.",
                    exampleAnswers = listOf(
                        "Many people feel hesitant at first, but daily practice builds confidence.",
                        "The primary hurdle is vocabulary, which can be improved by reading and speaking."
                    ),
                    assameseTranslation = "নতুনসকলে কি কি অসুবিধাৰ সন্মুখীন হয়?",
                    bengaliHindiTranslation = "নতুনদের কী কী সমস্যা হতে পারে?"
                ),
                TopicLessonQuestion(
                    questionNumber = 5,
                    questionText = "What are your future goals or plans related to ${topic.title}?",
                    hintText = "Use future transitions: 'In the coming year, I intend to...', 'My ultimate goal is...'",
                    exampleAnswers = listOf(
                        "In the coming year, I aim to achieve complete conversational fluency.",
                        "My goal is to master this skill so I can collaborate internationally."
                    ),
                    assameseTranslation = "ভৱিষ্যতে তোমাৰ পৰিকল্পনা কি?",
                    bengaliHindiTranslation = "ভবিষ্যতে আপনার পরিকল্পনা কী?"
                )
            )
        }
    }
}
