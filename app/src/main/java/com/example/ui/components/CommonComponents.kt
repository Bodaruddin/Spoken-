package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Role
import com.example.data.model.SpeakingEvaluation
import com.example.service.SpeechState
import com.example.ui.theme.*

@Composable
fun AppTopBar(
    role: Role,
    streak: Int,
    xp: Int,
    coins: Int,
    onRoleClick: () -> Unit,
    onCloudClick: () -> Unit,
    isCloudConnected: Boolean,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("app_branding_header")
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(listOf(RoyalBlue, PurpleAccent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SP", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                }
                Column {
                    Text(
                        text = "Speakly",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (role) {
                            Role.STUDENT -> "Student Portal"
                            else -> "Admin Control"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Stat pills & Role switcher button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (role == Role.STUDENT) {
                    // Streak Pill
                    StatPill(
                        emoji = "🔥",
                        value = "$streak",
                        bgColor = Color(0xFFFFF7ED),
                        textColor = OrangeVibrant,
                        testTag = "streak_pill"
                    )

                    // XP Pill
                    StatPill(
                        emoji = "⚡",
                        value = "$xp",
                        bgColor = Color(0xFFEFF6FF),
                        textColor = RoyalBlue,
                        testTag = "xp_pill"
                    )

                    // Coins Pill
                    StatPill(
                        emoji = "🪙",
                        value = "$coins",
                        bgColor = Color(0xFFFEFCE8),
                        textColor = AmberGold,
                        testTag = "coins_pill"
                    )
                }

                // Supabase Cloud DB Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = if (isCloudConnected) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .testTag("cloud_connect_button")
                        .clickable { onCloudClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isCloudConnected) Icons.Filled.CloudDone else Icons.Rounded.CloudQueue,
                            contentDescription = "Cloud Database",
                            modifier = Modifier.size(16.dp),
                            tint = if (isCloudConnected) EmeraldSuccess else MutedGray
                        )
                        Text(
                            text = if (isCloudConnected) "Sync" else "DB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCloudConnected) EmeraldSuccess else MutedGray
                        )
                    }
                }

                // Switch Role Button
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .testTag("role_switch_button")
                        .clickable { onRoleClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = "Switch Role",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = role.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Logout Button
                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("app_bar_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Logout,
                        contentDescription = "Log Out",
                        tint = MutedGray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StatPill(
    emoji: String,
    value: String,
    bgColor: Color,
    textColor: Color,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(emoji, fontSize = 12.sp)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
fun MicrophoneButton(
    speechState: SpeechState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onSimulate: (String) -> Unit,
    samplePrompt: String? = null,
    modifier: Modifier = Modifier
) {
    val isListening = speechState is SpeechState.Listening

    // Pulsing animation when recording
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.18f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(80.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(
                    if (isListening)
                        Brush.radialGradient(listOf(CoralAccent, OrangeVibrant))
                    else
                        Brush.radialGradient(listOf(RoyalBlue, RoyalBlueDark))
                )
                .clickable {
                    if (isListening) onStop() else onStart()
                }
                .testTag("microphone_button")
        ) {
            Icon(
                imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                contentDescription = if (isListening) "Stop Recording" else "Start Speaking",
                tint = Color.White,
                modifier = Modifier.size(36.dp)
            )
        }

        Text(
            text = when (speechState) {
                is SpeechState.Idle -> "Tap to Speak"
                is SpeechState.Listening -> "Listening... Speak clearly"
                is SpeechState.Processing -> "Evaluating speech..."
                is SpeechState.Success -> "Voice captured!"
                is SpeechState.Error -> speechState.message
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (speechState is SpeechState.Listening) CoralAccent else MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Quick Simulated Voice Button (for rapid test / emulator without audio mic)
        samplePrompt?.let { sample ->
            TextButton(
                onClick = { onSimulate(sample) },
                modifier = Modifier.testTag("simulate_speech_button")
            ) {
                Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Test with Sample Voice", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun SpeakingEvaluationCard(
    evaluation: SpeakingEvaluation,
    onAudioPlayExpected: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("speaking_evaluation_card")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with score badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                if (evaluation.overallScore >= 80) EmeraldSuccess else AmberGold
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${evaluation.overallScore}%",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                    }
                    Column {
                        Text(
                            "Speaking Assessment",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            if (evaluation.overallScore >= 85) "Excellent Fluency!" else "Good Effort, Keep Practicing!",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (onAudioPlayExpected != null) {
                    IconButton(
                        onClick = onAudioPlayExpected,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .testTag("play_expected_audio_button")
                    ) {
                        Icon(
                            Icons.Rounded.VolumeUp,
                            contentDescription = "Listen to Native Audio",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Sub-scores row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScoreChip("Pronunciation", evaluation.pronunciationScore)
                ScoreChip("Fluency", evaluation.fluencyScore)
                ScoreChip("Grammar", evaluation.grammarScore)
                ScoreChip("Vocabulary", evaluation.vocabularyScore)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // What you said
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("What we heard:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedGray)
                Text(
                    "\"${evaluation.spokenText}\"",
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Coach suggestion
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text("💡", fontSize = 16.sp)
                    Column {
                        Text(
                            "Coach Recommendation:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = RoyalBlueDark
                        )
                        Text(
                            evaluation.suggestedImprovement,
                            fontSize = 12.sp,
                            color = DeepNavy
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ScoreChip(label: String, score: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            "$score%",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = when {
                score >= 85 -> EmeraldSuccess
                score >= 70 -> RoyalBlue
                else -> OrangeVibrant
            }
        )
        Text(
            label,
            fontSize = 10.sp,
            color = MutedGray
        )
    }
}

@Composable
fun RoleSwitcherDialog(
    currentRole: Role,
    onRoleSelected: (Role) -> Unit,
    onDismiss: () -> Unit,
    onLogoutClick: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Select User Persona",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Switch between the available portals of Speakly:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                RoleSelectionItem(
                    title = "Student Portal",
                    subtitle = "Alex Morgan • Level A2 • 7-day streak",
                    emoji = "🦉",
                    isSelected = currentRole == Role.STUDENT,
                    onClick = { onRoleSelected(Role.STUDENT) }
                )

                RoleSelectionItem(
                    title = "Admin Portal",
                    subtitle = "Dean Arthur Pendelton • Faculty, Course & Database Management",
                    emoji = "🛡️",
                    isSelected = currentRole == Role.ADMIN,
                    onClick = { onRoleSelected(Role.ADMIN) }
                )

                if (onLogoutClick != null) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    OutlinedButton(
                        onClick = onLogoutClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Rounded.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Log Out & Go to Login Screen", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun RoleSelectionItem(
    title: String,
    subtitle: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (isSelected) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun CelebrationDialog(
    xp: Int,
    coins: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                Text("Continue Learning!", fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🎉", fontSize = 42.sp)
                Spacer(Modifier.height(6.dp))
                Text("Challenge Completed!", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, textAlign = TextAlign.Center)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "You demonstrated great spoken English fluency and accuracy!",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatPill("⚡", "+$xp XP", Color(0xFFEFF6FF), RoyalBlue, "reward_xp")
                    StatPill("🪙", "+$coins Coins", Color(0xFFFEFCE8), AmberGold, "reward_coins")
                }
            }
        }
    )
}
