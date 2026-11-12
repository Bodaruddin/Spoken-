package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.service.GeminiSpeechEvaluator
import com.example.ui.MainViewModel

@Composable
fun AiApiKeyIntegrationDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val isConfigured by viewModel.isGeminiConfigured.collectAsState()
    val keySource by viewModel.apiKeySource.collectAsState()
    val savedKey by viewModel.geminiApiKey.collectAsState()
    val activeModel by viewModel.activeAiModel.collectAsState()
    val voiceReplyEnabled by viewModel.aiVoiceReplyEnabled.collectAsState()
    val isTesting by viewModel.isTestingKey.collectAsState()
    val testResult by viewModel.aiKeyTestResult.collectAsState()

    val providerType by viewModel.aiProviderType.collectAsState()
    val customUrl by viewModel.customEndpointUrl.collectAsState()
    val customModel by viewModel.customModelName.collectAsState()

    var selectedTab by remember(providerType) { mutableStateOf(if (providerType == "CUSTOM_ENDPOINT") 1 else 0) }
    var inputKey by remember(savedKey) { mutableStateOf(savedKey) }
    var inputCustomUrl by remember(customUrl) { mutableStateOf(customUrl) }
    var inputCustomModel by remember(customModel) { mutableStateOf(customModel) }
    var isKeyVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.5.dp, Color(0xFF8B5CF6)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("ai_api_key_dialog")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF1E1B4B),
                                Color(0xFF0F172A),
                                Color(0xFF090D1A)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF4338CA).copy(alpha = 0.6f),
                                border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "AI Speech Integration",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Nova Real-Time Voice & CEFR Scoring",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("close_api_key_dialog_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    HorizontalDivider(color = Color(0xFF334155))

                    // Status Banner Card
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isConfigured) Color(0xFF064E3B).copy(alpha = 0.5f) else Color(0xFF451A03).copy(alpha = 0.5f),
                        border = BorderStroke(1.2.dp, if (isConfigured) Color(0xFF10B981) else Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (isConfigured) Icons.Rounded.CheckCircle else Icons.Rounded.Key,
                                contentDescription = null,
                                tint = if (isConfigured) Color(0xFF34D399) else Color(0xFFFBBF24),
                                modifier = Modifier.size(24.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isConfigured) "AI Voice Coach Connected" else "API Key Configuration Required",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Status: $keySource",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    // Mode Selection Tabs: Google Gemini vs Custom Endpoint
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color(0xFF1E293B),
                        contentColor = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = {
                                selectedTab = 0
                                viewModel.setAiProviderType("GEMINI")
                            },
                            text = { Text("Google Gemini", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = {
                                selectedTab = 1
                                viewModel.setAiProviderType("CUSTOM_ENDPOINT")
                            },
                            text = { Text("Custom / Proxy Endpoint", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        )
                    }

                    if (selectedTab == 0) {
                        // --- GOOGLE GEMINI MODE ---
                        Text(
                            text = "Connect Google Gemini to power real-time conversational speaking practice with Nova. Free API keys can be generated at https://aistudio.google.com/app/apikey (starts with 'AIzaSy...').",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp
                        )

                        // Smart Key Format Notice
                        if (inputKey.trim().startsWith("cc_")) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF78350F).copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Warning,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Non-Gemini Key Format Detected",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFEF3C7)
                                        )
                                        Text(
                                            text = "Your key starts with 'cc_'. Google Gemini API keys start with 'AIzaSy...'. If this key is for an OpenAI-compatible proxy, switch to the 'Custom / Proxy Endpoint' tab above.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFFDE68A),
                                            lineHeight = 15.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Gemini Key Input
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Gemini API Key",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                            OutlinedTextField(
                                value = inputKey,
                                onValueChange = { inputKey = it },
                                placeholder = {
                                    Text("AIzaSy...", color = Color(0xFF64748B), fontSize = 13.sp)
                                },
                                singleLine = true,
                                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                        Icon(
                                            imageVector = if (isKeyVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = "Toggle key visibility",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                                    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("api_key_input_field")
                            )
                        }

                        // Model Selection
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Gemini Model",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                            GeminiSpeechEvaluator.AVAILABLE_MODELS.forEach { (modelId, label) ->
                                val isSelected = activeModel == modelId
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) Color(0xFF4F46E5).copy(alpha = 0.4f) else Color(0xFF1E293B).copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, if (isSelected) Color(0xFF818CF8) else Color(0xFF334155)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.setAiModel(modelId) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        RadioButton(
                                            selected = isSelected,
                                            onClick = { viewModel.setAiModel(modelId) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color(0xFF38BDF8),
                                                unselectedColor = Color(0xFF64748B)
                                            )
                                        )
                                        Column {
                                            Text(
                                                text = label,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = Color.White
                                            )
                                            Text(
                                                text = modelId,
                                                fontSize = 10.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Test Gemini Button
                        Button(
                            onClick = { viewModel.testGeminiApiKey(inputKey) },
                            enabled = !isTesting && (inputKey.isNotBlank() || isConfigured),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF3B82F6),
                                disabledContainerColor = Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_api_key_btn")
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Testing Connection...", fontSize = 13.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Speed,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Test Connection to Gemini", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // --- CUSTOM / PROXY ENDPOINT MODE ---
                        Text(
                            text = "Connect any OpenAI-compatible API endpoint or proxy server (works with custom keys including 'cc_...' and 'sk-...').",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8),
                            lineHeight = 16.sp
                        )

                        // Endpoint URL
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Base URL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                            OutlinedTextField(
                                value = inputCustomUrl,
                                onValueChange = { inputCustomUrl = it },
                                placeholder = {
                                    Text("https://api.openai.com/v1", color = Color(0xFF64748B), fontSize = 13.sp)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                                    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_endpoint_url_input")
                            )
                        }

                        // API Key
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "API Key / Bearer Token",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                            OutlinedTextField(
                                value = inputKey,
                                onValueChange = { inputKey = it },
                                placeholder = {
                                    Text("cc_... or sk-...", color = Color(0xFF64748B), fontSize = 13.sp)
                                },
                                singleLine = true,
                                visualTransformation = if (isKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isKeyVisible = !isKeyVisible }) {
                                        Icon(
                                            imageVector = if (isKeyVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                            contentDescription = "Toggle key visibility",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                                    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_endpoint_key_input")
                            )
                        }

                        // Model Name
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Model Name",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE2E8F0)
                            )
                            OutlinedTextField(
                                value = inputCustomModel,
                                onValueChange = { inputCustomModel = it },
                                placeholder = {
                                    Text("gpt-4o-mini", color = Color(0xFF64748B), fontSize = 13.sp)
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF475569),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.7f),
                                    unfocusedContainerColor = Color(0xFF1E293B).copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("custom_endpoint_model_input")
                            )
                        }

                        // Test Custom Endpoint Button
                        Button(
                            onClick = {
                                viewModel.testCustomEndpoint(inputCustomUrl, inputKey, inputCustomModel)
                            },
                            enabled = !isTesting && inputCustomUrl.isNotBlank() && inputKey.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8B5CF6),
                                disabledContainerColor = Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("test_custom_endpoint_btn")
                        ) {
                            if (isTesting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Testing Custom Endpoint...", fontSize = 13.sp)
                            } else {
                                Icon(
                                    imageVector = Icons.Rounded.Lan,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("Test Custom Endpoint", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Test result banner
                    AnimatedVisibility(visible = testResult != null) {
                        testResult?.let { msg ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (msg.startsWith("✅")) Color(0xFF064E3B).copy(alpha = 0.7f) else Color(0xFF451A03).copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, if (msg.startsWith("✅")) Color(0xFF10B981) else Color(0xFFF59E0B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = msg,
                                    fontSize = 11.5.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(12.dp),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Voice Reply Toggle
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1E293B).copy(alpha = 0.6f),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = "Nova Conversational Voice",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Nova speaks replies aloud automatically",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                            Switch(
                                checked = voiceReplyEnabled,
                                onCheckedChange = { viewModel.setAiVoiceReplyEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF6366F1),
                                    uncheckedThumbColor = Color(0xFF94A3B8),
                                    uncheckedTrackColor = Color(0xFF334155)
                                )
                            )
                        }
                    }

                    // Save and Clear Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (savedKey.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.clearCustomGeminiApiKey()
                                    inputKey = ""
                                },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFFF87171)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("clear_api_key_btn")
                            ) {
                                Text("Clear Key", fontSize = 12.5.sp)
                            }
                        }

                        Button(
                            onClick = {
                                viewModel.saveCustomGeminiApiKey(inputKey)
                                if (selectedTab == 1) {
                                    viewModel.setCustomEndpointUrl(inputCustomUrl)
                                    viewModel.setCustomModelName(inputCustomModel)
                                    viewModel.setAiProviderType("CUSTOM_ENDPOINT")
                                } else {
                                    viewModel.setAiProviderType("GEMINI")
                                }
                                onDismiss()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1)
                            ),
                            modifier = Modifier
                                .weight(if (savedKey.isNotBlank()) 1.4f else 1f)
                                .testTag("save_api_key_btn")
                        ) {
                            Text("Save Configuration", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
