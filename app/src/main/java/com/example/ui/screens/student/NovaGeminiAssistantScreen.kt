package com.example.ui.screens.student

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.EaseInOutSine
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.NovaChatMessage
import com.example.service.SpeechState
import com.example.ui.MainViewModel
import com.example.ui.StudentTab

@Composable
fun NovaGeminiAssistantScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val messages by viewModel.novaGeminiMessages.collectAsState()
    val isThinking by viewModel.isNovaGeminiThinking.collectAsState()
    val activeMode by viewModel.novaGeminiMode.collectAsState()
    val isGeminiConfigured by viewModel.isGeminiConfigured.collectAsState()
    val activeAiModel by viewModel.activeAiModel.collectAsState()
    val voiceReplyEnabled by viewModel.aiVoiceReplyEnabled.collectAsState()
    val speechState by viewModel.speechManager.speechState.collectAsState()
    val isTtsSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var activeSpeakingMessageId by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    BackHandler {
        viewModel.selectStudentTab(StudentTab.HOME)
    }

    LaunchedEffect(Unit) {
        viewModel.loadNovaGeminiSession()
    }

    LaunchedEffect(messages.size, isThinking) {
        val total = messages.size + if (isThinking) 1 else 0
        if (total > 0) {
            listState.animateScrollToItem(total - 1)
        }
    }

    LaunchedEffect(isTtsSpeaking) {
        if (!isTtsSpeaking) {
            activeSpeakingMessageId = null
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startSpeakingPractice()
        } else {
            Toast.makeText(context, "Microphone permission is needed for voice prompts", Toast.LENGTH_SHORT).show()
        }
    }

    val isListening = speechState is SpeechState.Listening
    val modes = remember {
        listOf(
            "General AI" to "✨",
            "Grammar Fix" to "✍️",
            "Translator" to "🔤",
            "Writing Coach" to "📝",
            "Speaking Tutor" to "🗣️"
        )
    }

    val starterPrompts = remember {
        listOf(
            StarterPrompt("✍️ Fix Grammar", "Check and fix this sentence: I goes to school yesterday and meet my friend."),
            StarterPrompt("📖 Learn Tenses", "Explain Present Perfect vs Past Simple with 4 easy everyday examples."),
            StarterPrompt("🔤 Assamese Translate", "Translate 'How are you preparing for your English exam today?' into Assamese and Hindi."),
            StarterPrompt("🎤 Self Introduction", "Write a confident 6-sentence self-introduction for a college student.")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("nova_gemini_assistant_screen")
    ) {
        // 1. Top Gemini-inspired Nova AI Header
        Surface(
            color = Color(0xFF0D0826).copy(alpha = 0.90f),
            border = BorderStroke(1.dp, Color(0xFF312E81).copy(alpha = 0.65f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF06B6D4), Color(0xFF6366F1), Color(0xFFEC4899))
                            )
                        )
                        .padding(2.dp),
                    contentAlignment = Alignment.Center
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

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Nova AI",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isGeminiConfigured) Color(0xFF064E3B) else Color(0xFF451A03),
                            border = BorderStroke(
                                1.dp,
                                if (isGeminiConfigured) Color(0xFF10B981) else Color(0xFFF59E0B)
                            ),
                            modifier = Modifier
                                .clickable { viewModel.openAiKeyDialog() }
                                .testTag("nova_gemini_status_pill")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AutoAwesome,
                                    contentDescription = "AI Engine Status",
                                    tint = if (isGeminiConfigured) Color(0xFF34D399) else Color(0xFFFBBF24),
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = if (isGeminiConfigured) "Gemini Live" else "Setup Gemini",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isGeminiConfigured) Color(0xFFA7F3D0) else Color(0xFFFDE68A),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                    Text(
                        text = if (isGeminiConfigured) "Model: $activeAiModel • Ask anything" else "Tap 'Setup Gemini' or try On-Device Tutor",
                        fontSize = 11.sp,
                        color = Color(0xFFC4B5FD),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { viewModel.setAiVoiceReplyEnabled(!voiceReplyEnabled) },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("nova_gemini_voice_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (voiceReplyEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                        contentDescription = if (voiceReplyEnabled) "Disable Voice Reply" else "Enable Voice Reply",
                        tint = if (voiceReplyEnabled) Color(0xFF67E8F9) else Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.clearNovaGeminiSession() },
                    modifier = Modifier
                        .size(40.dp)
                        .testTag("nova_gemini_new_chat_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddComment,
                        contentDescription = "New Chat",
                        tint = Color(0xFFE0E7FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Mode Chips Strip
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(modes) { (modeName, emoji) ->
                val selected = activeMode == modeName
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (selected) Color(0xFF312E81) else Color(0xFF140D32),
                    border = BorderStroke(
                        1.dp,
                        if (selected) Color(0xFF38BDF8) else Color(0xFF4338CA).copy(alpha = 0.55f)
                    ),
                    modifier = Modifier
                        .clickable { viewModel.setNovaGeminiMode(modeName) }
                        .testTag("nova_mode_${modeName.lowercase().replace(" ", "_")}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(text = emoji, fontSize = 13.sp)
                        Text(
                            text = modeName,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        // 3. Conversation Stream + Starter Suggestions
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (messages.size <= 1) {
                item {
                    NovaGeminiHeroCard(
                        studentName = currentUser.name.ifBlank { "Friend" },
                        starterPrompts = starterPrompts,
                        onPromptClick = { viewModel.sendNovaGeminiMessage(it) }
                    )
                }
            }

            items(messages, key = { it.id }) { msg ->
                if (msg.isNova) {
                    val isThisPlaying = isTtsSpeaking && activeSpeakingMessageId == msg.id
                    NovaGeminiResponseCard(
                        message = msg,
                        isPlayingAudio = isThisPlaying,
                        onToggleAudio = {
                            if (isThisPlaying) {
                                viewModel.ttsManager.stop()
                                activeSpeakingMessageId = null
                            } else {
                                activeSpeakingMessageId = msg.id
                                viewModel.speakAudio(msg.text)
                            }
                        },
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Nova AI Response", msg.text))
                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    NovaGeminiUserBubble(text = msg.text)
                }
            }

            if (isThinking) {
                item {
                    NovaThinkingIndicator()
                }
            }
        }

        // 4. Gemini-style Prompt Composer Footer
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF140D32),
            border = BorderStroke(
                1.4.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF06B6D4), Color(0xFF6366F1), Color(0xFFEC4899))
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("nova_gemini_input_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        val hasPerm = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED
                        if (isListening) {
                            viewModel.stopSpeakingPractice()
                        } else if (hasPerm) {
                            viewModel.startSpeakingPractice()
                        } else {
                            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) Color(0xFFDC2626).copy(alpha = 0.25f)
                            else Color(0xFF312E81).copy(alpha = 0.55f)
                        )
                        .testTag("nova_gemini_mic_btn")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Voice Prompt",
                        tint = if (isListening) Color(0xFFF87171) else Color(0xFF67E8F9),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 6.dp)
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = if (isListening) "Listening... speak your question" else "Ask Nova AI anything...",
                            color = if (isListening) Color(0xFF67E8F9) else Color(0xFF94A3B8),
                            fontSize = 14.sp
                        )
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        maxLines = 4,
                        cursorBrush = SolidColor(Color(0xFF38BDF8)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (inputText.isNotBlank() && !isThinking) {
                                    val prompt = inputText.trim()
                                    inputText = ""
                                    viewModel.sendNovaGeminiMessage(prompt)
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nova_gemini_text_field")
                    )
                }

                Spacer(Modifier.width(8.dp))

                val canSend = inputText.isNotBlank() && !isThinking
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (canSend) {
                                Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF7C3AED)))
                            } else {
                                SolidColor(Color(0xFF1E293B))
                            }
                        )
                        .clickable(enabled = canSend) {
                            val prompt = inputText.trim()
                            inputText = ""
                            viewModel.sendNovaGeminiMessage(prompt)
                        }
                        .testTag("nova_gemini_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = "Send Prompt",
                        tint = if (canSend) Color.White else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

private data class StarterPrompt(
    val title: String,
    val prompt: String
)

@Composable
private fun NovaGeminiHeroCard(
    studentName: String,
    starterPrompts: List<StarterPrompt>,
    onPromptClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF150E36).copy(alpha = 0.92f),
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFFA855F7)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Hello $studentName, try asking Nova AI",
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                starterPrompts.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1F164B),
                        border = BorderStroke(1.dp, Color(0xFF4338CA).copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPromptClick(item.prompt) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.title,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF67E8F9)
                                )
                                Text(
                                    text = item.prompt,
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFCBD5E1),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Rounded.ArrowForwardIos,
                                contentDescription = null,
                                tint = Color(0xFF818CF8),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NovaGeminiUserBubble(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 6.dp,
                bottomStart = 20.dp,
                bottomEnd = 20.dp
            ),
            color = Color(0xFF3730A3),
            border = BorderStroke(1.dp, Color(0xFF6366F1)),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            SelectionContainer {
                Text(
                    text = text,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun NovaGeminiResponseCard(
    message: NovaChatMessage,
    isPlayingAudio: Boolean,
    onToggleAudio: () -> Unit,
    onCopy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(
            topStart = 6.dp,
            topEnd = 20.dp,
            bottomStart = 20.dp,
            bottomEnd = 20.dp
        ),
        color = Color(0xFF171133),
        border = BorderStroke(1.dp, Color(0xFF4C2A85)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Nova AI",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF67E8F9)
                )
                if (!message.hint.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF281A54)
                    ) {
                        Text(
                            text = message.hint,
                            fontSize = 10.sp,
                            color = Color(0xFFC4B5FD),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            SelectionContainer {
                Text(
                    text = message.text,
                    fontSize = 14.sp,
                    color = Color(0xFFF1F5F9),
                    lineHeight = 21.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF23184A),
                    border = BorderStroke(1.dp, Color(0xFF5B21B6).copy(alpha = 0.7f)),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 36.dp)
                        .clickable { onToggleAudio() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayingAudio) Icons.Rounded.Stop else Icons.Rounded.VolumeUp,
                            contentDescription = if (isPlayingAudio) "Stop Audio" else "Listen",
                            tint = Color(0xFF67E8F9),
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isPlayingAudio) "Stop" else "Listen",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE0E7FF)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF23184A),
                    border = BorderStroke(1.dp, Color(0xFF5B21B6).copy(alpha = 0.7f)),
                    modifier = Modifier
                        .defaultMinSize(minHeight = 36.dp)
                        .clickable { onCopy() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = "Copy response",
                            tint = Color(0xFFC4B5FD),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Copy",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFE0E7FF)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NovaThinkingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "nova_thinking")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "thinking_alpha"
    )
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF171133),
        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = alpha * 0.7f)),
        modifier = Modifier.fillMaxWidth(0.75f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = Color(0xFF38BDF8),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Nova AI is thinking...",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFBAE6FD).copy(alpha = alpha)
            )
        }
    }
}
