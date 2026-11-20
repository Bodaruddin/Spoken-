package com.example.ui.screens.student

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.util.NovaAttachment
import com.example.util.NovaAttachmentHelper
import kotlinx.coroutines.launch

@Composable
fun NovaGeminiAssistantScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentUser by viewModel.currentUser.collectAsState()
    val messages by viewModel.novaGeminiMessages.collectAsState()
    val isThinking by viewModel.isNovaGeminiThinking.collectAsState()
    val activeMode by viewModel.novaGeminiMode.collectAsState()
    val isGeminiConfigured by viewModel.isGeminiConfigured.collectAsState()
    val voiceReplyEnabled by viewModel.aiVoiceReplyEnabled.collectAsState()
    val selectedPersona by viewModel.novaVoicePersona.collectAsState()
    val selectedSpeed by viewModel.voiceSpeedPreset.collectAsState()
    val activeVoiceLabel by viewModel.activeVoiceLabel.collectAsState()
    val cloudHumanVoiceEnabled by viewModel.cloudHumanVoiceEnabled.collectAsState()
    val speechState by viewModel.speechManager.speechState.collectAsState()
    val isTtsSpeaking by viewModel.ttsManager.isSpeaking.collectAsState()
    val pendingAttachments by viewModel.pendingNovaAttachments.collectAsState()
    val messageThumbnails by viewModel.novaAttachmentThumbnails.collectAsState()

    var inputText by remember { mutableStateOf("") }
    var activeSpeakingMessageId by remember { mutableStateOf<String?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessingAttachment by remember { mutableStateOf(false) }
    var showAttachmentOptions by remember { mutableStateOf(false) }
    var showVoiceStudio by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    BackHandler { viewModel.selectStudentTab(StudentTab.HOME) }

    LaunchedEffect(Unit) { viewModel.loadNovaGeminiSession() }

    LaunchedEffect(messages.size, isThinking) {
        val total = messages.size + if (isThinking) 1 else 0
        if (total > 0) listState.animateScrollToItem(total - 1)
    }

    LaunchedEffect(isTtsSpeaking) {
        if (!isTtsSpeaking) activeSpeakingMessageId = null
    }

    fun handlePickedUri(uri: Uri, forceImage: Boolean = false, overrideName: String? = null) {
        coroutineScope.launch {
            isProcessingAttachment = true
            val result = NovaAttachmentHelper.loadAttachmentFromUri(
                context = context,
                uri = uri,
                forceImage = forceImage,
                overrideName = overrideName
            )
            isProcessingAttachment = false
            result.onSuccess { attachment ->
                viewModel.addNovaAttachment(attachment)
            }.onFailure { err ->
                Toast.makeText(context, err.message ?: "Could not attach file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) viewModel.startSpeakingPractice()
        else Toast.makeText(context, "Microphone permission is needed for voice prompts", Toast.LENGTH_SHORT).show()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) handlePickedUri(uri, forceImage = true)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) handlePickedUri(uri, forceImage = false)
    }

    val cameraCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val captured = pendingCameraUri
        pendingCameraUri = null
        if (success && captured != null) {
            handlePickedUri(
                uri = captured,
                forceImage = true,
                overrideName = "Camera_${System.currentTimeMillis() % 10000}.jpg"
            )
        }
    }

    fun launchCameraCapture() {
        try {
            val uri = NovaAttachmentHelper.createCameraImageUri(context)
            pendingCameraUri = uri
            cameraCaptureLauncher.launch(uri)
        } catch (e: Exception) {
            Toast.makeText(context, "Camera unavailable on this device", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCameraCapture()
        else Toast.makeText(context, "Camera permission is needed to take a photo", Toast.LENGTH_SHORT).show()
    }

    val isListening = speechState is SpeechState.Listening
    val modes = remember {
        listOf(
            "General AI" to "✨",
            "Roleplay" to "🎭",
            "Speaking Tutor" to "🗣️",
            "Grammar Fix" to "✍️",
            "Translator" to "🔤",
            "Writing Coach" to "📝"
        )
    }

    val starterPrompts = remember {
        listOf(
            StarterPrompt("🎭 Café Roleplay", "Let's do a fun café order roleplay!"),
            StarterPrompt("✨ Upgrade My English", "Teach me 5 natural phrases to replace boring English!"),
            StarterPrompt("🗣️ Tongue Twister", "Give me a fun Indian-friendly tongue twister for V and W!"),
            StarterPrompt("🔤 Assamese & Hindi", "Translate 'How are you preparing for your English exam today?' into Assamese and Hindi.")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("nova_gemini_assistant_screen")
    ) {
        // 1. Top Header
        NovaAssistantTopBar(
            voicePersonaName = selectedPersona.displayName,
            voiceAccentLabel = selectedPersona.shortAccentLabel,
            isSpeaking = isTtsSpeaking,
            voiceReplyEnabled = voiceReplyEnabled,
            onOpenVoiceStudio = { showVoiceStudio = true },
            onToggleVoice = { viewModel.setAiVoiceReplyEnabled(!voiceReplyEnabled) },
            onNewChat = { viewModel.clearNovaGeminiSession() }
        )

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

        // 3. Conversation Stream
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
                    NovaGeminiUserBubble(
                        message = msg,
                        thumbnail = messageThumbnails[msg.id]
                    )
                }
            }

            if (isThinking) {
                item { NovaThinkingIndicator() }
            }
        }

        // 4. Expandable Camera / Image / File Sheet when '+' is tapped + Pending Attachments
        AnimatedVisibility(visible = showAttachmentOptions) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF140D32),
                border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("nova_gemini_attachment_menu")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ComposerActionChip(
                        icon = Icons.Rounded.PhotoCamera,
                        label = "Camera",
                        accentColor = Color(0xFF38BDF8),
                        testTag = "nova_gemini_camera_btn",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showAttachmentOptions = false
                            val hasCam = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasCam) launchCameraCapture()
                            else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    )
                    ComposerActionChip(
                        icon = Icons.Rounded.Image,
                        label = "Image",
                        accentColor = Color(0xFFC084FC),
                        testTag = "nova_gemini_image_btn",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showAttachmentOptions = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    )
                    ComposerActionChip(
                        icon = Icons.Rounded.AttachFile,
                        label = "File",
                        accentColor = Color(0xFF34D399),
                        testTag = "nova_gemini_file_btn",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            showAttachmentOptions = false
                            filePickerLauncher.launch(
                                arrayOf(
                                    "application/pdf",
                                    "text/*",
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "image/*",
                                    "*/*"
                                )
                            )
                        }
                    )
                }
            }
        }

        AnimatedVisibility(visible = pendingAttachments.isNotEmpty() || isProcessingAttachment) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(pendingAttachments, key = { it.id }) { att ->
                    PendingAttachmentChip(
                        attachment = att,
                        onRemove = { viewModel.removeNovaAttachment(att.id) }
                    )
                }
                if (isProcessingAttachment) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E1B4B),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CircularProgressIndicator(strokeWidth = 2.dp, color = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                Text("Preparing...", fontSize = 11.sp, color = Color(0xFFBAE6FD))
                            }
                        }
                    }
                }
            }
        }

        // 5. Sleek Single-Row Input Bar: [+] [Mic]  [Ask Nova AI anything...]  [Send]
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF140D32),
            border = BorderStroke(
                1.4.dp,
                Brush.horizontalGradient(listOf(Color(0xFF06B6D4), Color(0xFF6366F1), Color(0xFFEC4899)))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("nova_gemini_input_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // '+' Plus Icon Button (toggles Camera, Image, File options)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (showAttachmentOptions) Color(0xFF4F46E5).copy(alpha = 0.65f)
                            else Color(0xFF312E81).copy(alpha = 0.55f)
                        )
                        .clickable { showAttachmentOptions = !showAttachmentOptions }
                        .testTag("nova_gemini_plus_btn")
                ) {
                    Icon(
                        imageVector = if (showAttachmentOptions) Icons.Rounded.Close else Icons.Rounded.Add,
                        contentDescription = if (showAttachmentOptions) "Close attachments" else "Attach camera, image or file",
                        tint = Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(Modifier.width(5.dp))

                // Mic Icon Button placed near '+' Plus icon
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) Color(0xFFDC2626).copy(alpha = 0.25f)
                            else Color(0xFF312E81).copy(alpha = 0.55f)
                        )
                        .clickable {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (isListening) viewModel.stopSpeakingPractice()
                            else if (hasPerm) viewModel.startSpeakingPractice()
                            else micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                        .testTag("nova_gemini_mic_btn")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Rounded.Stop else Icons.Rounded.Mic,
                        contentDescription = if (isListening) "Stop Listening" else "Voice Prompt",
                        tint = if (isListening) Color(0xFFF87171) else Color(0xFF67E8F9),
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 5.dp)
                ) {
                    if (inputText.isEmpty()) {
                        Text(
                            text = if (isListening) "Listening... speak your question" else "Ask Nova AI anything...",
                            color = if (isListening) Color(0xFF67E8F9) else Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    BasicTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        textStyle = TextStyle(color = Color.White, fontSize = 14.sp, lineHeight = 20.sp),
                        maxLines = 4,
                        cursorBrush = SolidColor(Color(0xFF38BDF8)),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if ((inputText.isNotBlank() || pendingAttachments.isNotEmpty()) && !isThinking) {
                                    val prompt = inputText.trim()
                                    inputText = ""
                                    showAttachmentOptions = false
                                    viewModel.sendNovaGeminiMessage(prompt)
                                }
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("nova_gemini_text_field")
                    )
                }

                Spacer(Modifier.width(6.dp))

                val canSend = (inputText.isNotBlank() || pendingAttachments.isNotEmpty()) && !isThinking && !isProcessingAttachment
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (canSend) Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF7C3AED)))
                            else SolidColor(Color(0xFF1E293B))
                        )
                        .clickable(enabled = canSend) {
                            val prompt = inputText.trim()
                            inputText = ""
                            showAttachmentOptions = false
                            viewModel.sendNovaGeminiMessage(prompt)
                        }
                        .testTag("nova_gemini_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = "Send Prompt",
                        tint = if (canSend) Color.White else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
        // 6. Indian Female Voice Studio Dialog
        if (showVoiceStudio) {
            com.example.ui.components.NovaVoiceStudioDialog(
                selectedPersona = selectedPersona,
                activeVoiceLabel = activeVoiceLabel,
                voiceReplyEnabled = voiceReplyEnabled,
                cloudHumanVoiceEnabled = cloudHumanVoiceEnabled,
                isSpeaking = isTtsSpeaking,
                onSelectPersona = { viewModel.setNovaVoicePersona(it) },
                onToggleVoiceReply = { viewModel.setAiVoiceReplyEnabled(it) },
                onToggleCloudHumanVoice = { viewModel.setCloudHumanVoiceEnabled(it) },
                onPreviewVoice = { viewModel.previewNovaVoice() },
                onStopVoice = { viewModel.ttsManager.stop() },
                onDismiss = { showVoiceStudio = false },
                selectedSpeed = selectedSpeed,
                onSelectSpeed = { viewModel.setVoiceSpeedPreset(it) }
            )
        }
    }
}

private data class StarterPrompt(val title: String, val prompt: String)

@Composable
private fun NovaAssistantTopBar(
    voicePersonaName: String,
    voiceAccentLabel: String,
    isSpeaking: Boolean,
    voiceReplyEnabled: Boolean,
    onOpenVoiceStudio: () -> Unit,
    onToggleVoice: () -> Unit,
    onNewChat: () -> Unit
) {
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
                            if (isSpeaking) listOf(Color(0xFFEC4899), Color(0xFF38BDF8), Color(0xFFA855F7))
                            else listOf(Color(0xFF06B6D4), Color(0xFF6366F1), Color(0xFFEC4899))
                        )
                    )
                    .padding(2.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_nova_ai_robot),
                    contentDescription = "Nova AI",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Nova AI • $voicePersonaName", fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E1644),
                    border = BorderStroke(1.dp, Color(0xFFEC4899).copy(alpha = 0.45f)),
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clickable { onOpenVoiceStudio() }
                        .testTag("nova_voice_selector_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RecordVoiceOver,
                            contentDescription = "Change Female Voice",
                            tint = Color(0xFFF472B6),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = if (isSpeaking) "$voicePersonaName speaking..." else "$voiceAccentLabel (Female) ▾",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFFCE7F3),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            IconButton(onClick = onToggleVoice, modifier = Modifier.size(48.dp).testTag("nova_gemini_voice_toggle_btn")) {
                Icon(if (voiceReplyEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff, if (voiceReplyEnabled) "Disable Voice Reply" else "Enable Voice Reply", tint = if (voiceReplyEnabled) Color(0xFF67E8F9) else Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onNewChat, modifier = Modifier.size(48.dp).testTag("nova_gemini_new_chat_btn")) {
                Icon(Icons.Rounded.AddComment, "New Chat", tint = Color(0xFFE0E7FF), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun ComposerActionChip(
    icon: ImageVector,
    label: String,
    accentColor: Color,
    testTag: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1644),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f)),
        modifier = modifier
            .defaultMinSize(minHeight = 38.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = accentColor, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text(text = label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun PendingAttachmentChip(
    attachment: NovaAttachment,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1F164B),
        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (attachment.previewBitmap != null) {
                Image(
                    bitmap = attachment.previewBitmap.asImageBitmap(),
                    contentDescription = attachment.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(34.dp).clip(RoundedCornerShape(8.dp))
                )
            } else {
                Icon(Icons.Rounded.Description, attachment.displayName, tint = Color(0xFF34D399), modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.widthIn(max = 120.dp)) {
                Text(attachment.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (attachment.sizeLabel.isNotBlank()) {
                    Text(attachment.sizeLabel, fontSize = 9.5.sp, color = Color(0xFF94A3B8), maxLines = 1)
                }
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(22.dp)) {
                Icon(Icons.Rounded.Close, "Remove attachment", tint = Color(0xFFF87171), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun NovaGeminiHeroCard(
    studentName: String,
    starterPrompts: List<StarterPrompt>,
    onPromptClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF150E36).copy(alpha = 0.92f),
        border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFFA855F7)))),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                Text("Hello $studentName, try asking Nova AI", fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                starterPrompts.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1F164B),
                        border = BorderStroke(1.dp, Color(0xFF4338CA).copy(alpha = 0.7f)),
                        modifier = Modifier.fillMaxWidth().clickable { onPromptClick(item.prompt) }
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.title, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF67E8F9))
                                Text(item.prompt, fontSize = 11.5.sp, color = Color(0xFFCBD5E1), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Rounded.ArrowForwardIos, null, tint = Color(0xFF818CF8), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NovaGeminiUserBubble(message: NovaChatMessage, thumbnail: Bitmap?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        Surface(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 6.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
            color = Color(0xFF3730A3),
            border = BorderStroke(1.dp, Color(0xFF6366F1)),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (thumbnail != null) {
                    Image(
                        bitmap = thumbnail.asImageBitmap(),
                        contentDescription = message.attachmentLabel ?: "Attached photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().heightIn(max = 160.dp).clip(RoundedCornerShape(12.dp))
                    )
                }
                if (!message.attachmentLabel.isNullOrBlank()) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF1E1B4B)) {
                        Text(
                            text = message.attachmentLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF67E8F9),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                SelectionContainer {
                    Text(text = message.text, fontSize = 14.sp, color = Color.White, lineHeight = 20.sp)
                }
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
        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 20.dp),
        color = Color(0xFF171133),
        border = BorderStroke(1.dp, Color(0xFF4C2A85)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.AutoAwesome, null, tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                Text("Nova AI", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF67E8F9))
                if (!message.hint.isNullOrBlank()) {
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF281A54)) {
                        Text(message.hint, fontSize = 10.sp, color = Color(0xFFC4B5FD), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            SelectionContainer {
                Text(text = message.text, fontSize = 14.sp, color = Color(0xFFF1F5F9), lineHeight = 21.sp)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF23184A),
                    border = BorderStroke(1.dp, Color(0xFF5B21B6).copy(alpha = 0.7f)),
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp).clickable { onToggleAudio() }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(if (isPlayingAudio) Icons.Rounded.Stop else Icons.Rounded.VolumeUp, if (isPlayingAudio) "Stop Audio" else "Listen", tint = Color(0xFF67E8F9), modifier = Modifier.size(15.dp))
                        Text(if (isPlayingAudio) "Stop" else "Listen", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE0E7FF))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF23184A),
                    border = BorderStroke(1.dp, Color(0xFF5B21B6).copy(alpha = 0.7f)),
                    modifier = Modifier.defaultMinSize(minHeight = 36.dp).clickable { onCopy() }
                ) {
                    Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Icon(Icons.Rounded.ContentCopy, "Copy response", tint = Color(0xFFC4B5FD), modifier = Modifier.size(14.dp))
                        Text("Copy", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE0E7FF))
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
        animationSpec = infiniteRepeatable(animation = tween(650, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
        label = "thinking_alpha"
    )
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF171133),
        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = alpha * 0.7f)),
        modifier = Modifier.fillMaxWidth(0.75f)
    ) {
        Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CircularProgressIndicator(strokeWidth = 2.dp, color = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
            Text("Nova AI is thinking...", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFFBAE6FD).copy(alpha = alpha))
        }
    }
}
