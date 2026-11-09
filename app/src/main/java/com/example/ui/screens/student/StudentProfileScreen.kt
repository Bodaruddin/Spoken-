package com.example.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementBadge
import com.example.data.model.ExamSubmission
import com.example.data.model.LeaderboardUser
import com.example.data.remote.SupabaseConnectionState
import com.example.ui.MainViewModel
import com.example.ui.components.OfficialSpeaklyCertificateCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.SpeaklyCertificatePreviewDialog
import com.example.ui.theme.*
import com.example.util.CertificatePdfGenerator

@Composable
fun StudentProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val profile by viewModel.studentProfile.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val vocabulary by viewModel.vocabularyWords.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    val isAdminSession by viewModel.isAdminSession.collectAsState()
    val examSubmissions by viewModel.examSubmissions.collectAsState()

    var showVocabNotebook by remember { mutableStateOf(false) }
    var previewSubmissionForCert by remember { mutableStateOf<ExamSubmission?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("student_profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. VIP Executive Identity & Scholar Header
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0F172A), // Deep Midnight
                                Color(0xFF1E1B4B), // Luxury Indigo
                                Color(0xFF312E81)  // Royal Violet
                            )
                        )
                    )
                    .testTag("executive_profile_header")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Bar: Branding & Executive Utility Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // VIP Tier Tag
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2E1065).copy(alpha = 0.8f),
                            border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("👑", fontSize = 12.sp)
                                Text(
                                    "PREMIUM SCHOLAR",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFEF08A),
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        // Utility Actions (Switch Role + Logout)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Student Role Badge
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🦉", fontSize = 12.sp)
                                    Text("Student", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                }
                            }

                            // If in Admin Session, direct 1-tap return to Admin Panel without security PIN
                            if (isAdminSession) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF59E0B),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                    modifier = Modifier
                                        .testTag("student_profile_back_to_admin_button")
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.returnToAdminPanel() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🛡️", fontSize = 12.sp)
                                        Text("Back to Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    }
                                }
                            } else {
                                // Staff Portal Access for Faculty/Admin to unlock staff portal
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1E293B),
                                    border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .testTag("student_profile_staff_switch_button")
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.toggleRoleSwitcher(true) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text("🛡️", fontSize = 12.sp)
                                        Text("Staff Access", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFA855F7))
                                    }
                                }
                            }

                            // Logout
                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Filled.Logout, contentDescription = "Logout", tint = CoralAccent, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Main User Profile Showcase
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Glowing Profile Avatar
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(
                                                Color(0xFFF59E0B),
                                                Color(0xFF818CF8),
                                                Color(0xFF38BDF8),
                                                Color(0xFFF59E0B)
                                            )
                                        )
                                    )
                                    .padding(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F172A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(user.avatarEmoji, fontSize = 38.sp)
                                }
                            }

                            // Online status badge
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A))
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(EmeraldSuccess)
                                )
                            }
                        }

                        // Identity details
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = user.name,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 21.sp,
                                color = Color.White
                            )

                            Text(
                                text = user.email,
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )

                            Spacer(Modifier.height(4.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "${profile.currentLevel.displayName} (${profile.currentLevel.cefr})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7DD3FC),
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "🏫 ${user.studentClass}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF86EFAC),
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = profile.league,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFCD34D),
                                        maxLines = 1,
                                        softWrap = false,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Daily Practice Streaks & Metrics Section (Shifted from Home Screen)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_profile_streaks_metrics"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SectionHeader("Daily Practice Streaks & Metrics")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StudentStatTile(
                        title = "Streak",
                        value = "${profile.streakDays}d",
                        emoji = "🔥",
                        cardBg = NeonAmberCardBg,
                        borderColor = NeonAmberBorder,
                        gradient = NeonAmberGrad,
                        modifier = Modifier.weight(1f)
                    )
                    StudentStatTile(
                        title = "XP Earned",
                        value = "${profile.xpPoints}",
                        emoji = "⚡",
                        cardBg = NeonBlueCardBg,
                        borderColor = NeonBlueBorder,
                        gradient = NeonBlueGrad,
                        modifier = Modifier.weight(1f)
                    )
                    StudentStatTile(
                        title = "Speaking",
                        value = "${profile.speakingScore}%",
                        emoji = "🎙️",
                        cardBg = NeonGreenCardBg,
                        borderColor = NeonGreenBorder,
                        gradient = NeonGreenGrad,
                        modifier = Modifier.weight(1f)
                    )
                    StudentStatTile(
                        title = "CEFR",
                        value = profile.currentLevel.cefr,
                        emoji = "🏆",
                        cardBg = NeonPurpleCardBg,
                        borderColor = NeonPurpleBorder,
                        gradient = NeonPurpleGrad,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dynamic Practice Metrics Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("⏱️ Practice Time", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                            Text("${profile.speakingPracticeMinutes} mins", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗺️ Lessons Cleared", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                            Text("${profile.completedStagesCount} stages", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🏆 Overall CEFR", fontSize = 10.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Medium)
                            Text("${profile.currentLevel.cefr} • ${profile.overallScore}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                        }
                    }
                }
            }
        }

        // 3. Today's Daily Missions (Dynamic according to student practice)
        item {
            val isSpokenDone = profile.speakingPracticeMinutes > 0
            val isPronunciationDone = profile.pronunciationScore >= 80
            val isGrammarDone = profile.grammarScore >= 80
            val completedCount = (if (isSpokenDone) 1 else 0) + (if (isPronunciationDone) 1 else 0) + (if (isGrammarDone) 1 else 0)

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.dp, CyberCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_profile_today_missions")
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🎯", fontSize = 18.sp)
                            Text(
                                "Today's Missions",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                        Text("$completedCount / 3 Done", fontSize = 12.sp, color = if (completedCount == 3) EmeraldSuccess else RoyalBlue, fontWeight = FontWeight.Bold)
                    }

                    MissionItem("Practice 1 Speaking Roleplay / Conversation", "50 XP", if (isSpokenDone) 1f else 0.5f, isSpokenDone)
                    MissionItem("Score >80% on Pronunciation Drill", "40 XP", (profile.pronunciationScore / 100f).coerceIn(0f, 1f), isPronunciationDone)
                    MissionItem("Vocabulary & Grammar Precision Challenge", "60 XP", (profile.grammarScore / 100f).coerceIn(0f, 1f), isGrammarDone)

                    Button(
                        onClick = { viewModel.incrementDailyPracticeStreak() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (completedCount > 0) EmeraldSuccess else Color(0xFF1E293B)),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Text("🔥 Claim Daily Practice Streak Bonus (+30 XP)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }

        // 3. Shifted to Dedicated Leaderboard Screen Shortcut Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leaderboard_shifted_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFFEF08A), Color(0xFFF59E0B), Color(0xFFD97706))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 24.sp)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Academy Fluency Leaderboard",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Rankings shifted to dedicated Leaderboard page! View podium, cohorts & standing.",
                                fontSize = 11.sp,
                                color = MutedGray
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.selectStudentTab(com.example.ui.StudentTab.LEADERBOARD) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("View Ranks", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Official Exam Certificates & Credentials
        val passedCertificates = examSubmissions.filter { it.isPassed }
        if (passedCertificates.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFD4AF37)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF1E1B4B),
                                    Color(0xFF2E1065)
                                )
                            )
                        )
                        .testTag("student_earned_certificates_section")
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🏆", fontSize = 20.sp)
                                Column {
                                    Text(
                                        "Official Verified Certificates",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        "${passedCertificates.size} Accredited Credential(s) Earned",
                                        fontSize = 11.sp,
                                        color = Color(0xFFFDE047)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF065F46)
                            ) {
                                Text(
                                    "VERIFIED",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA7F3D0),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Display top certificate preview
                        val primaryCert = passedCertificates.firstOrNull { it.studentName.contains("Fariha", ignoreCase = true) }
                            ?: passedCertificates.first()

                        OfficialSpeaklyCertificateCard(
                            submission = primaryCert,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { previewSubmissionForCert = primaryCert }
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = primaryCert.studentName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "${primaryCert.examTitle} • Grade ${primaryCert.grade} (${primaryCert.percentage}%)",
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                            Button(
                                onClick = { previewSubmissionForCert = primaryCert },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text("View Full 🏆", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                            }
                        }
                    }
                }
            }
        }

        // 5. Comprehensive CEFR Spoken Proficiency Matrix
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = "CEFR Spoken Proficiency Radar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Real-time AI diagnostic scores (Level ${profile.currentLevel.cefr} • ${profile.currentLevel.displayName})",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8))
                        ) {
                            Text(
                                text = "${profile.overallScore}% Overall • Level ${profile.currentLevel.cefr}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color(0xFF38BDF8),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // CEFR Scale Framework Roadmap
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val levels = listOf("A1" to "Beg", "A2" to "Elem", "B1" to "Pre-Int", "B2" to "Int", "C1" to "Upper", "C2" to "Adv")
                        levels.forEach { (code, desc) ->
                            val isActive = profile.currentLevel.cefr.equals(code, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isActive) Color(0xFF2563EB) else Color.Transparent,
                                border = if (isActive) BorderStroke(1.dp, Color(0xFF60A5FA)) else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        code,
                                        fontSize = 11.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isActive) Color.White else Color(0xFF64748B)
                                    )
                                    Text(
                                        desc,
                                        fontSize = 8.sp,
                                        color = if (isActive) Color(0xFFBFDBFE) else Color(0xFF475569)
                                    )
                                }
                            }
                        }
                    }

                    SkillBar("🎙️ Spoken Fluency", profile.fluencyScore, RoyalBlue)
                    SkillBar("🎯 Pronunciation Accuracy", profile.pronunciationScore, EmeraldSuccess)
                    SkillBar("📖 Vocabulary Breadth", profile.vocabularyScore, PurpleAccent)
                    SkillBar("⚡ Grammar Precision", profile.grammarScore, OrangeVibrant)
                    SkillBar("🎧 Listening Comprehension", profile.listeningScore, CyanAccent)
                    SkillBar("📚 Reading Comprehension", profile.readingScore, SoftPink)
                }
            }
        }

        // 5. Personal Vocabulary Notebook Vault (Audio Flashcards)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg, contentColor = Color.White),
                border = BorderStroke(1.2.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showVocabNotebook = !showVocabNotebook }
                    .testTag("student_vocab_vault_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0F382B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📚", fontSize = 24.sp)
                        }
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "Personal Vocabulary Vault",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White
                            )
                            Text(
                                text = "${vocabulary.size} words saved • Audio flashcards & Assamese",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                    Icon(
                        if (showVocabNotebook) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = "Expand Vocabulary",
                        tint = Color(0xFF60A5FA)
                    )
                }
            }
        }

        if (showVocabNotebook) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    vocabulary.forEach { word ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = CyberCardBg, contentColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFF263366)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(word.word, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                                        Text(word.phonetic, fontSize = 12.sp, color = RoyalBlue, fontWeight = FontWeight.Medium)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                word.partOfSpeech,
                                                fontSize = 10.sp,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = word.meaning,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "\"${word.exampleSentence}\"",
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = MutedGray
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.speakAudio("${word.word}. ${word.meaning}") },
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEFF6FF))
                                ) {
                                    Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen", tint = RoyalBlue)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 6. Prestige Achievement Badges & Honors Cabinet
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🎖️", fontSize = 20.sp)
                            Text(
                                text = "Trophy & Honors Cabinet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        val unlockedCount = badges.count { it.isUnlocked }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "$unlockedCount / ${badges.size} Unlocked",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberGold,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    badges.forEach { badge ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (badge.isUnlocked) Color(0xFFFAF5FF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                1.dp,
                                if (badge.isUnlocked) Color(0xFFE9D5FF) else Color(0xFFE2E8F0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (badge.isUnlocked) Color(0xFFF3E8FF) else Color(0xFFF1F5F9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(badge.iconEmoji, fontSize = 22.sp)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = badge.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (badge.isUnlocked) DeepNavy else MutedGray
                                    )
                                    Text(badge.description, fontSize = 11.sp, color = MutedGray)
                                }

                                if (badge.isUnlocked) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = EmeraldSuccess.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            "UNLOCKED",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            color = EmeraldSuccess,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        "${(badge.progress * 100).toInt()}%",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MutedGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Official Certificate Preview Dialog Modal
    previewSubmissionForCert?.let { sub ->
        SpeaklyCertificatePreviewDialog(
            initialSubmission = sub,
            allSubmissions = examSubmissions,
            onDismiss = { previewSubmissionForCert = null },
            onDownloadPdf = { submissionToDownload ->
                CertificatePdfGenerator.generateCertificatePdf(
                    context = context,
                    submission = submissionToDownload,
                    onSuccess = { file ->
                        Toast.makeText(context, "Certificate PDF downloaded: ${file.name}", Toast.LENGTH_LONG).show()
                        CertificatePdfGenerator.openOrSharePdf(context, file)
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

@Composable
fun PremiumStatCard(
    title: String,
    value: String,
    sub: String,
    accent: Color,
    bg: Color,
    border: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = BorderStroke(1.dp, border),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, fontSize = 10.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.White, maxLines = 1, softWrap = false)
            Text(sub, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accent, maxLines = 1, softWrap = false)
        }
    }
}

@Composable
fun PremiumPodiumPillar(
    rank: Int,
    name: String,
    emoji: String,
    xp: String,
    streak: String,
    medal: String,
    league: String,
    badgeColor: Color,
    containerColor: Color,
    borderColor: Color,
    isFirst: Boolean = false,
    isCurrentUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = BorderStroke(if (isFirst || isCurrentUser) 1.8.dp else 1.dp, borderColor),
        shadowElevation = if (isFirst) 4.dp else 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = if (isFirst) 14.dp else 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(medal, fontSize = if (isFirst) 24.sp else 18.sp)

            Box(
                modifier = Modifier
                    .size(if (isFirst) 50.dp else 40.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = if (isFirst) 26.sp else 20.sp)
            }

            Text(
                text = if (isCurrentUser) "$name (You)" else name,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = if (isCurrentUser) RoyalBlue else DeepNavy
            )

            Text(
                text = xp,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                maxLines = 1,
                softWrap = false,
                color = if (isFirst) AmberGold else if (isCurrentUser) RoyalBlue else MutedGray
            )

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.White,
                border = BorderStroke(0.8.dp, Color(0xFFE2E8F0))
            ) {
                Text(
                    text = "🔥 $streak",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeVibrant,
                    maxLines = 1,
                    softWrap = false,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun SkillBar(label: String, score: Int, color: Color) {
    val cefrBadge = when {
        score >= 92 -> "C2"
        score >= 85 -> "C1"
        score >= 75 -> "B2"
        score >= 60 -> "B1"
        score >= 45 -> "A2"
        else -> "A1"
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = color.copy(alpha = 0.2f),
                    border = BorderStroke(0.5.dp, color.copy(alpha = 0.5f))
                ) {
                    Text(cefrBadge, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                }
                Text("$score%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
            }
        }
        LinearProgressIndicator(
            progress = { (score / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFF1E293B)
        )
    }
}

@Composable
fun MissionItem(title: String, reward: String, progress: Float, isDone: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (isDone) Icons.Filled.CheckCircle else Icons.Rounded.RadioButtonUnchecked,
                    contentDescription = null,
                    tint = if (isDone) EmeraldSuccess else MutedGray,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(reward, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = if (isDone) EmeraldSuccess else RoyalBlue,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun StudentStatTile(
    title: String,
    value: String,
    emoji: String,
    gradient: Brush,
    modifier: Modifier = Modifier,
    cardBg: Color = CyberCardBg,
    borderColor: Color = NeonBlueBorder
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.2.dp, borderColor.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = 20.sp)
            }
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}
