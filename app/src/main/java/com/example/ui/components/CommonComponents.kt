package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Role
import com.example.data.model.SpeakingEvaluation
import com.example.service.SpeechState
import com.example.ui.theme.*

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFFA855F7), Color(0xFF4F46E5))))
        )
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            color = Color.White
        )
    }
}

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
    onAdminCredentialsClick: () -> Unit = {},
    isAdminSession: Boolean = false,
    onReturnToAdminClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isPillTopBar = role == Role.ADMIN || role == Role.TEACHER
    Surface(
        color = if (isPillTopBar) Color(0xFF0B1033).copy(alpha = 0.95f) else Color(0xFF0C1434).copy(alpha = 0.95f),
        shape = if (isPillTopBar) RoundedCornerShape(26.dp) else RoundedCornerShape(0.dp),
        tonalElevation = 0.dp,
        border = BorderStroke(
            1.2.dp,
            if (isPillTopBar) Color(0xFF6366F1).copy(alpha = 0.65f) else Color(0xFF283B70).copy(alpha = 0.5f)
        ),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .then(if (isPillTopBar) Modifier.padding(horizontal = 14.dp, vertical = 6.dp) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.testTag("app_branding_header")
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(listOf(Color(0xFF4F46E5), Color(0xFF6366F1)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("SP", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                }
                Column {
                    Text(
                        text = "Speakly",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = when (role) {
                            Role.STUDENT -> "Student Portal"
                            Role.TEACHER -> "Teacher Portal"
                            Role.ADMIN -> "Admin Control"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Divider for Admin & Teacher
            if (isPillTopBar) {
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.2.dp)
                        .background(Color(0xFF283B70).copy(alpha = 0.7f))
                )
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
                        bgColor = Color(0xFF261A0C),
                        textColor = AmberGold,
                        testTag = "streak_pill"
                    )

                    // XP Pill
                    StatPill(
                        emoji = "⚡",
                        value = "$xp",
                        bgColor = Color(0xFF0B1736),
                        textColor = Color(0xFF60A5FA),
                        testTag = "xp_pill"
                    )

                    // Coins Pill
                    StatPill(
                        emoji = "🪙",
                        value = "$coins",
                        bgColor = Color(0xFF261E08),
                        textColor = AmberGold,
                        testTag = "coins_pill"
                    )
                }

                // Supabase Cloud DB Sync Icon Button (Icon only - tap to manage database)
                if (role == Role.ADMIN) {
                    Surface(
                        shape = CircleShape,
                        color = if (isCloudConnected) Color(0xFF04261B) else Color(0xFF0A182E),
                        border = BorderStroke(1.dp, if (isCloudConnected) Color(0xFF10B981) else Color(0xFF1E3A5F)),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("cloud_connect_button")
                            .clickable { onCloudClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isCloudConnected) Icons.Filled.CloudDone else Icons.Rounded.CloudQueue,
                                contentDescription = "Cloud Database Sync",
                                modifier = Modifier.size(17.dp),
                                tint = if (isCloudConnected) Color(0xFF10B981) else Color(0xFF64748B)
                            )
                        }
                    }

                    // Admin Credentials Icon Button (Icon only - tap to manage credentials)
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF052338),
                        border = BorderStroke(1.dp, Color(0xFF06B6D4)),
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("admin_credentials_header_btn")
                            .clickable { onAdminCredentialsClick() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Filled.Key,
                                contentDescription = "Manage Admin Credentials",
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFF06B6D4)
                            )
                        }
                    }
                }

                // Switch Role Pill (Matching Screenshot: Crown ADMIN with golden gradient)
                val (roleGradient, roleBorderColor, roleEmoji) = when (role) {
                    Role.ADMIN -> Triple(
                        Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
                        Color(0xFFFDE68A),
                        "👑"
                    )
                    Role.TEACHER -> Triple(
                        Brush.horizontalGradient(listOf(Color(0xFFA855F7), Color(0xFF7C3AED))),
                        Color(0xFFE9D5FF),
                        "👩‍🏫"
                    )
                    Role.STUDENT -> Triple(
                        Brush.horizontalGradient(listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))),
                        Color(0xFFBFDBFE),
                        "🎓"
                    )
                }
                // Quick return to Admin panel if switched from Admin
                if (isAdminSession && role != Role.ADMIN) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFF59E0B),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier
                            .testTag("topbar_back_to_admin_button")
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onReturnToAdminClick?.invoke() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Return to Admin Panel",
                                modifier = Modifier.size(13.dp),
                                tint = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Admin",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A),
                                maxLines = 1
                            )
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, roleBorderColor.copy(alpha = 0.85f)),
                    modifier = Modifier
                        .testTag("role_switch_button")
                        .clip(RoundedCornerShape(20.dp))
                        .background(roleGradient)
                        .clickable { onRoleClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(roleEmoji, fontSize = 12.sp)
                        Text(
                            text = role.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                // Logout Button
                IconButton(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("app_bar_logout_button")
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Logout,
                        contentDescription = "Log Out",
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(20.dp)
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
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.5f)),
        modifier = Modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Text(
                value,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
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
                color = Color(0xFF0F1E42),
                border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.5f)),
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
                            color = Color(0xFF60A5FA)
                        )
                        Text(
                            evaluation.suggestedImprovement,
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0)
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
fun AdminPreviewBanner(
    currentRole: Role,
    onBackToAdminClick: () -> Unit,
    onSwitchRoleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F172A).copy(alpha = 0.96f),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("admin_preview_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706)))),
                    contentAlignment = Alignment.Center
                ) {
                    Text("👑", fontSize = 16.sp)
                }
                Column {
                    Text(
                        text = "Admin Session",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFFDE68A)
                    )
                    Text(
                        text = "Viewing as ${currentRole.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Switch role
                IconButton(
                    onClick = onSwitchRoleClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        Icons.Rounded.SwapHoriz,
                        contentDescription = "Switch Role",
                        tint = Color(0xFFA5B4FC),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Return to Admin Panel
                Button(
                    onClick = onBackToAdminClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF59E0B),
                        contentColor = Color(0xFF0F172A)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("banner_back_to_admin_button")
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF0F172A)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "Back to Admin",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun RoleSwitcherDialog(
    currentRole: Role,
    onRoleSelected: (Role) -> Unit,
    onDismiss: () -> Unit,
    onVerifyPin: (Role, String) -> Boolean = { _, _ -> true },
    onLogoutClick: (() -> Unit)? = null,
    isAdminSession: Boolean = false
) {
    var pendingRole by remember { mutableStateOf<Role?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    if (pendingRole != null) {
        val target = pendingRole!!
        AlertDialog(
            onDismissRequest = {
                pendingRole = null
                pinInput = ""
                pinError = false
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (target == Role.ADMIN) "🛡️" else "👩‍🏫", fontSize = 22.sp)
                    Text("Staff Verification", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter the security PIN or password to unlock the ${target.name.lowercase().replaceFirstChar { it.uppercase() }} Portal:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it
                            pinError = false
                        },
                        label = { Text("Security PIN") },
                        placeholder = { Text("Enter security PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        isError = pinError,
                        supportingText = if (pinError) {
                            { Text("Incorrect PIN. Access denied.", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (onVerifyPin(target, pinInput)) {
                            onRoleSelected(target)
                            pendingRole = null
                            pinInput = ""
                            pinError = false
                        } else {
                            pinError = true
                        }
                    }
                ) {
                    Text("Unlock & Switch")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    pendingRole = null
                    pinInput = ""
                    pinError = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }

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
                    title = if (currentRole == Role.ADMIN || isAdminSession) "Teacher Portal (Direct Access • No PIN)" else "Teacher Portal (Protected)",
                    subtitle = if (currentRole == Role.ADMIN || isAdminSession) "Instant switch to faculty workspace without security PIN" else "Prof. Eleanor Vance • Classes 9 & 10 • Faculty Verification",
                    emoji = "👩‍🏫",
                    isSelected = currentRole == Role.TEACHER,
                    onClick = {
                        if (currentRole == Role.TEACHER) {
                            onDismiss()
                        } else if (currentRole == Role.ADMIN || isAdminSession) {
                            // Admin can switch to Teacher panel without security PIN
                            onRoleSelected(Role.TEACHER)
                            onDismiss()
                        } else {
                            pendingRole = Role.TEACHER
                        }
                    }
                )

                RoleSelectionItem(
                    title = if (isAdminSession) "Admin Portal (Direct Access • No PIN)" else "Admin Portal (Protected)",
                    subtitle = if (isAdminSession) "Dean Arthur Pendelton • Instant return to institutional controls" else "Dean Arthur Pendelton • Institutional Controls • Admin Verification",
                    emoji = "🛡️",
                    isSelected = currentRole == Role.ADMIN,
                    onClick = {
                        if (currentRole == Role.ADMIN) {
                            onDismiss()
                        } else if (isAdminSession) {
                            // Admin returning to Admin panel without security PIN!
                            onRoleSelected(Role.ADMIN)
                            onDismiss()
                        } else {
                            pendingRole = Role.ADMIN
                        }
                    }
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
        containerColor = Color(0xFF131D3F),
        titleContentColor = Color.White,
        textContentColor = Color(0xFFE2E8F0),
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                Text("Continue Learning!", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🎉", fontSize = 42.sp)
                Spacer(Modifier.height(6.dp))
                Text("Challenge Completed!", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, textAlign = TextAlign.Center, color = Color.White)
            }
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "You demonstrated great spoken English fluency and accuracy!",
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp,
                    color = Color(0xFFCBD5E1)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatPill("⚡", "+$xp XP", Color(0xFFE0F2FE), Color(0xFF0369A1), "reward_xp")
                    StatPill("🪙", "+$coins Coins", Color(0xFFFEF3C7), Color(0xFFB45309), "reward_coins")
                }
            }
        }
    )
}
