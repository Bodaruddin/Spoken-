package com.example.ui.components

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.service.NovaVoicePersona
import com.example.service.VoiceSpeedPreset

@Composable
fun NovaVoiceStudioDialog(
    selectedPersona: NovaVoicePersona,
    activeVoiceLabel: String,
    voiceReplyEnabled: Boolean,
    cloudHumanVoiceEnabled: Boolean,
    isSpeaking: Boolean,
    onSelectPersona: (NovaVoicePersona) -> Unit,
    onToggleVoiceReply: (Boolean) -> Unit,
    onToggleCloudHumanVoice: (Boolean) -> Unit,
    onPreviewVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onDismiss: () -> Unit,
    selectedSpeed: VoiceSpeedPreset = VoiceSpeedPreset.NATURAL,
    onSelectSpeed: (VoiceSpeedPreset) -> Unit = {}
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF130D2E),
            border = BorderStroke(
                1.5.dp,
                Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFFA855F7), Color(0xFFEC4899)))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 20.dp)
                .heightIn(max = 640.dp)
                .testTag("nova_voice_studio_dialog")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Pinned Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6))))
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RecordVoiceOver,
                            contentDescription = "Female Voice Studio",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Nova Voice Studio",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = activeVoiceLabel,
                            fontSize = 11.5.sp,
                            color = Color(0xFF67E8F9),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp).testTag("close_voice_studio_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close Voice Studio",
                            tint = Color(0xFFCBD5E1)
                        )
                    }
                }

                // Scrollable Voice List & Toggles
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val assamesePersonas = NovaVoicePersona.entries.filter { it.isAssameseVoice }
                    val indianPersonas = NovaVoicePersona.entries.filter { it.isIndianVoice }
                    val globalEnglishPersonas = NovaVoicePersona.entries.filter { !it.isIndianVoice && !it.isAssameseVoice }

                    Text(
                        text = "Assamese Female Voices (অসমীয়া)",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5EEAD4)
                    )

                    assamesePersonas.forEach { persona ->
                        VoicePersonaOptionCard(
                            persona = persona,
                            isSelected = persona == selectedPersona,
                            onSelect = { onSelectPersona(persona) }
                        )
                    }

                    Text(
                        text = "Indian English Female Voices",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC4B5FD),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    indianPersonas.forEach { persona ->
                        VoicePersonaOptionCard(
                            persona = persona,
                            isSelected = persona == selectedPersona,
                            onSelect = { onSelectPersona(persona) }
                        )
                    }

                    Text(
                        text = "English Female Voices (UK • US • AU)",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF67E8F9),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    globalEnglishPersonas.forEach { persona ->
                        VoicePersonaOptionCard(
                            persona = persona,
                            isSelected = persona == selectedPersona,
                            onSelect = { onSelectPersona(persona) }
                        )
                    }

                    // Speaking Pace Selector
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1B133D),
                        border = BorderStroke(1.dp, Color(0xFF3B2A70)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Speaking Pace",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                VoiceSpeedPreset.entries.forEach { preset ->
                                    val isSpeedSelected = preset == selectedSpeed
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSpeedSelected) Color(0xFF281956) else Color(0xFF130D2E),
                                        border = BorderStroke(
                                            1.2.dp,
                                            if (isSpeedSelected) Color(0xFF38BDF8) else Color(0xFF3B2A70)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .clickable { onSelectSpeed(preset) }
                                            .testTag("voice_speed_${preset.id.lowercase()}")
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                                        ) {
                                            Text(
                                                text = preset.label,
                                                fontSize = 11.5.sp,
                                                fontWeight = if (isSpeedSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSpeedSelected) Color(0xFF67E8F9) else Color(0xFFCBD5E1),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Toggles
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1B133D),
                        border = BorderStroke(1.dp, Color(0xFF3B2A70)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Auto-Speak Replies", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text("Nova automatically reads replies aloud", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Switch(
                                    checked = voiceReplyEnabled,
                                    onCheckedChange = onToggleVoiceReply
                                )
                            }
                            HorizontalDivider(color = Color(0xFF2D2059))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Ultra-Natural Human Audio", fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                    Text("Uses HD studio female voice when active, with natural on-device fallback", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                                Switch(
                                    checked = cloudHumanVoiceEnabled,
                                    onCheckedChange = onToggleCloudHumanVoice
                                )
                            }
                        }
                    }
                }

                // Pinned Preview & Done Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { if (isSpeaking) onStopVoice() else onPreviewVoice() },
                        border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("preview_nova_voice_btn")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.Rounded.PlayArrow,
                            contentDescription = if (isSpeaking) "Stop Preview" else "Preview Voice",
                            tint = Color(0xFF67E8F9),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isSpeaking) "Stop" else "Hear ${selectedPersona.displayName}",
                            color = Color(0xFF67E8F9),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B5CF6)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = "Done",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoicePersonaOptionCard(
    persona: NovaVoicePersona,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF281956) else Color(0xFF1B133D),
        border = BorderStroke(
            1.3.dp,
            if (isSelected) Color(0xFF38BDF8) else Color(0xFF3B2A70)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 56.dp)
            .clickable { onSelect() }
            .testTag("voice_persona_${persona.id.lowercase()}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isSelected && persona.isAssameseVoice -> Brush.linearGradient(listOf(Color(0xFF10B981), Color(0xFF06B6D4)))
                            isSelected -> Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFFA855F7)))
                            persona.isAssameseVoice -> Brush.linearGradient(listOf(Color(0xFF065F46), Color(0xFF0E7490)))
                            persona.isIndianVoice -> Brush.linearGradient(listOf(Color(0xFF312E81), Color(0xFF4C1D95)))
                            else -> Brush.linearGradient(listOf(Color(0xFF0E7490), Color(0xFF4338CA)))
                        }
                    )
            ) {
                Text(
                    text = persona.displayName.first().toString(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = if (persona.nativeName.isNotBlank()) "${persona.displayName} • ${persona.nativeName}" else persona.displayName,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when {
                            persona.isAssameseVoice -> Color(0xFF064E3B)
                            persona.isIndianVoice -> Color(0xFF1E3A5F)
                            else -> Color(0xFF134E4A)
                        }
                    ) {
                        Text(
                            text = persona.localeBadge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                persona.isAssameseVoice -> Color(0xFF6EE7B7)
                                persona.isIndianVoice -> Color(0xFF67E8F9)
                                else -> Color(0xFF5EEAD4)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = persona.subtitle,
                    fontSize = 12.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
