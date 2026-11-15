package com.example.ui.screens.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.MainViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
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
    val isAdminSession by viewModel.isAdminSession.collectAsState()
    val isStreakBonusClaimedToday by viewModel.dailyStreakBonusClaimedToday.collectAsState()

    var showVocabNotebook by remember { mutableStateOf(false) }
    var trophyFilter by remember { mutableStateOf("ALL") }
    var selectedBadge by remember { mutableStateOf<AchievementBadge?>(null) }

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

                        // Utility Actions (Role Badge / Back to Admin + Logout)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (isAdminSession) {
                                // If in Admin Session, direct 1-tap return to Admin Panel
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
                            }

                            // Prominent, clearly visible Logout Button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFDC2626).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .testTag("student_profile_logout_button")
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.logout() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Logout,
                                        contentDescription = "Logout",
                                        tint = Color(0xFFF87171),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "Logout",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFCA5A5),
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
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

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "⭐ ${profile.activityLevelName}",
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
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                                    modifier = Modifier.testTag("student_profile_class_badge")
                                ) {
                                    Text(
                                        text = "🏫 ${user.studentClass.ifBlank { "Batch Alpha" }}",
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
                        title = "Level",
                        value = "Level ${profile.activityLevel}",
                        emoji = "⭐",
                        cardBg = NeonPurpleCardBg,
                        borderColor = NeonPurpleBorder,
                        gradient = NeonPurpleGrad,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Dynamic Practice Metrics Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Max),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "⏱️ Practice Time",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${profile.speakingPracticeMinutes} mins",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "🗺️ Lessons Cleared",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${profile.completedStagesCount} stages",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp, vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "🏆 Overall Avg",
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "${profile.overallScore}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberGold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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

                    MissionItem("Complete 1 English Adventure Mission", "50 XP", if (isSpokenDone) 1f else 0.5f, isSpokenDone)
                    MissionItem("Score >80% on Pronunciation Drill", "40 XP", (profile.pronunciationScore / 100f).coerceIn(0f, 1f), isPronunciationDone)
                    MissionItem("Vocabulary & Grammar Precision Challenge", "60 XP", (profile.grammarScore / 100f).coerceIn(0f, 1f), isGrammarDone)

                    Button(
                        onClick = {
                            if (completedCount == 0) {
                                Toast.makeText(
                                    context,
                                    "Complete at least 1 daily mission today to claim your +30 XP Streak Bonus!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (viewModel.incrementDailyPracticeStreak()) {
                                Toast.makeText(
                                    context,
                                    "🔥 Daily Streak Bonus Claimed! +30 XP & +10 Coins",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        enabled = !isStreakBonusClaimedToday,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("claim_daily_streak_bonus_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (completedCount > 0) EmeraldSuccess else Color(0xFF1E293B),
                            disabledContainerColor = Color(0xFF064E3B),
                            disabledContentColor = Color(0xFFA7F3D0)
                        ),
                        contentPadding = PaddingValues(vertical = 10.dp, horizontal = 12.dp)
                    ) {
                        Text(
                            text = when {
                                isStreakBonusClaimedToday -> "✅ Daily Streak Bonus Claimed Today (+30 XP)"
                                completedCount > 0 -> "🔥 Claim Daily Practice Streak Bonus (+30 XP)"
                                else -> "🔒 Complete 1 Mission to Unlock (+30 XP)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isStreakBonusClaimedToday) Color(0xFFA7F3D0) else Color.White
                        )
                    }
                }
            }
        }


        // 4. Comprehensive CEFR Spoken Proficiency Matrix
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
                                text = "Activity & Proficiency Level",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Dynamic learning progress (Level ${profile.activityLevel} • ${profile.xpPoints} XP)",
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
                                text = "${profile.overallScore}% Overall • Level ${profile.activityLevel}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color(0xFF38BDF8),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Level Framework Roadmap (Level 1, 2, 3 according to student activity)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 6.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val levelItems = listOf(
                            1 to "Lvl 1",
                            2 to "Lvl 2",
                            3 to "Lvl 3",
                            4 to "Lvl 4",
                            5 to "Lvl 5",
                            6 to "Lvl 6"
                        )
                        levelItems.forEach { (lvl, label) ->
                            val isActive = profile.activityLevel == lvl
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isActive) Color(0xFF2563EB) else Color.Transparent,
                                border = if (isActive) BorderStroke(1.dp, Color(0xFF60A5FA)) else null
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isActive) Color.White else Color(0xFF64748B)
                                    )
                                    Text(
                                        when (lvl) {
                                            1 -> "0-250 XP"
                                            2 -> "250+ XP"
                                            3 -> "800+ XP"
                                            4 -> "1.5k XP"
                                            5 -> "2.5k XP"
                                            else -> "4k+ XP"
                                        },
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
                                            color = Color(0xFF1E293B)
                                        ) {
                                            Text(
                                                word.partOfSpeech,
                                                fontSize = 10.sp,
                                                color = Color(0xFFCBD5E1),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    if (word.assameseMeaning.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFF064E3B).copy(alpha = 0.5f),
                                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.6f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text("অসমীয়া:", fontSize = 11.sp, color = Color(0xFF6EE7B7), fontWeight = FontWeight.Bold)
                                                Text(word.assameseMeaning, fontSize = 11.sp, color = Color(0xFFFEF08A), fontWeight = FontWeight.Bold)
                                                if (word.assameseTransliteration.isNotBlank()) {
                                                    Text("(${word.assameseTransliteration})", fontSize = 10.sp, color = Color(0xFFCBD5E1), fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                                }
                                            }
                                        }
                                    }
                                    Text(
                                        text = word.meaning,
                                        fontSize = 12.sp,
                                        color = Color(0xFFCBD5E1)
                                    )
                                    Text(
                                        text = "\"${word.exampleSentence}\"",
                                        fontSize = 11.sp,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                        color = Color(0xFF94A3B8)
                                    )
                                }

                                IconButton(
                                    onClick = { viewModel.speakAudio("${word.word}. ${word.meaning}") },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1E3A8A).copy(alpha = 0.55f))
                                ) {
                                    Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen", tint = Color(0xFF60A5FA))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Prestige Trophy & Honors Cabinet (Dynamic Progress & Cosmic Glass UI)
        item {
            val unlockedCount = badges.count { it.isUnlocked }
            val inProgressCount = badges.size - unlockedCount
            val filteredBadges = remember(badges, trophyFilter) {
                when (trophyFilter) {
                    "UNLOCKED" -> badges.filter { it.isUnlocked }
                    "IN_PROGRESS" -> badges.filter { !it.isUnlocked }
                    else -> badges.sortedByDescending { it.isUnlocked }
                }
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.2.dp, Color(0xFFF59E0B).copy(alpha = 0.45f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_trophy_honors_section")
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
                            Column {
                                Text(
                                    text = "Trophy & Honors",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Tap any honor to inspect milestone progress",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "$unlockedCount / ${badges.size} Unlocked",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFCD34D),
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Filter Tabs: All / Unlocked / In Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val filters = listOf(
                            Triple("ALL", "All (${badges.size})", Color(0xFF38BDF8)),
                            Triple("UNLOCKED", "Unlocked ($unlockedCount)", EmeraldSuccess),
                            Triple("IN_PROGRESS", "In Progress ($inProgressCount)", AmberGold)
                        )
                        filters.forEach { (key, label, accent) ->
                            val isSelected = trophyFilter == key
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) accent.copy(alpha = 0.22f) else Color(0xFF0F172A),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) accent else Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { trophyFilter = key }
                                    .testTag("trophy_filter_${key.lowercase()}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    if (filteredBadges.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🏆", fontSize = 28.sp)
                                Text(
                                    text = if (trophyFilter == "UNLOCKED") "No honors unlocked yet" else "All honors unlocked!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (trophyFilter == "UNLOCKED") {
                                        "Keep building your speaking streak, vocabulary, and stage progress to claim your first honor."
                                    } else {
                                        "Incredible work! You have mastered every milestone in your Trophy Cabinet."
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            filteredBadges.forEach { badge ->
                                val pct = (badge.progress * 100).toInt().coerceIn(0, 100)
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (badge.isUnlocked) Color(0xFF1E1B4B) else Color(0xFF0F172A),
                                    border = BorderStroke(
                                        1.dp,
                                        if (badge.isUnlocked) Color(0xFFF59E0B).copy(alpha = 0.55f) else Color(0xFF334155)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { selectedBadge = badge }
                                        .testTag("trophy_badge_${badge.id}")
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
                                                .size(46.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (badge.isUnlocked) {
                                                        Brush.linearGradient(
                                                            listOf(Color(0xFFFDE68A), Color(0xFFF59E0B))
                                                        )
                                                    } else {
                                                        Brush.linearGradient(
                                                            listOf(Color(0xFF1E293B), Color(0xFF334155))
                                                        )
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(badge.iconEmoji, fontSize = 22.sp)
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = badge.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f).padding(end = 6.dp)
                                                )

                                                if (badge.isUnlocked) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = EmeraldSuccess.copy(alpha = 0.2f),
                                                        border = BorderStroke(0.8.dp, EmeraldSuccess.copy(alpha = 0.6f))
                                                    ) {
                                                        Text(
                                                            "UNLOCKED",
                                                            fontWeight = FontWeight.ExtraBold,
                                                            fontSize = 9.sp,
                                                            color = Color(0xFF6EE7B7),
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                } else {
                                                    Text(
                                                        text = "$pct%",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFFFCD34D)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = badge.description,
                                                fontSize = 11.sp,
                                                color = Color(0xFF94A3B8),
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            if (!badge.isUnlocked) {
                                                LinearProgressIndicator(
                                                    progress = { badge.progress.coerceIn(0f, 1f) },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(5.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = AmberGold,
                                                    trackColor = Color(0xFF1E293B)
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
    }

    // Trophy & Honor Milestone Detail Dialog
    selectedBadge?.let { badge ->
        val pct = (badge.progress * 100).toInt().coerceIn(0, 100)
        AlertDialog(
            onDismissRequest = { selectedBadge = null },
            containerColor = Color(0xFF0F172A),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                if (badge.isUnlocked) {
                                    Brush.linearGradient(listOf(Color(0xFFFDE68A), Color(0xFFF59E0B)))
                                } else {
                                    Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF334155)))
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(badge.iconEmoji, fontSize = 24.sp)
                    }
                    Column {
                        Text(
                            text = badge.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            text = if (badge.isUnlocked) "Honor Unlocked" else "$pct% Progress Complete",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (badge.isUnlocked) Color(0xFF6EE7B7) else Color(0xFFFCD34D)
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = badge.description,
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1)
                    )
                    LinearProgressIndicator(
                        progress = { badge.progress.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = if (badge.isUnlocked) EmeraldSuccess else AmberGold,
                        trackColor = Color(0xFF1E293B)
                    )
                    Text(
                        text = if (badge.isUnlocked) {
                            "Congratulations! This honor is permanently displayed in your Scholar Cabinet."
                        } else {
                            "Complete AI Speaking drills, daily missions, and stages to push this honor to 100%."
                        },
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedBadge = null }) {
                    Text("Got it", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                }
            }
        )
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
