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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementBadge
import com.example.data.model.LeaderboardUser
import com.example.data.remote.SupabaseConnectionState
import com.example.ui.MainViewModel
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@Composable
fun StudentProfileScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val profile by viewModel.studentProfile.collectAsState()
    val badges by viewModel.badges.collectAsState()
    val vocabulary by viewModel.vocabularyWords.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()

    var showVocabNotebook by remember { mutableStateOf(false) }

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
                        value = "86%",
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
            }
        }

        // 3. Today's Daily Missions (Shifted from Home Screen)
        item {
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
                        Text("2 / 3 Done", fontSize = 12.sp, color = RoyalBlue, fontWeight = FontWeight.Bold)
                    }

                    MissionItem("Practice 1 Speaking Roleplay", "50 XP", 1f, true)
                    MissionItem("Score >80% on Pronunciation Drill", "40 XP", 1f, true)
                    MissionItem("Win 1 Vocabulary Game Challenge", "60 XP", 0.4f, false)
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

        // 4. Comprehensive CEFR Spoken Proficiency Matrix
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
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = "CEFR Spoken Proficiency Radar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Real-time AI diagnostic scores (Level A2)",
                                fontSize = 11.sp,
                                color = MutedGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${profile.overallScore}% Overall",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
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
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.2.dp, Color(0xFFBBF7D0)),
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
                                .background(Color(0xFFDCFCE7)),
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
                                color = DeepNavy
                            )
                            Text(
                                text = "${vocabulary.size} words saved • Audio flashcards & Assamese",
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MutedGray
                            )
                        }
                    }
                    Icon(
                        if (showVocabNotebook) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = "Expand Vocabulary",
                        tint = DeepNavy
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
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                                        Text(word.word, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = DeepNavy)
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
            Text(title, fontSize = 10.sp, color = MutedGray, fontWeight = FontWeight.Medium, maxLines = 1, softWrap = false)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = DeepNavy, maxLines = 1, softWrap = false)
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
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text("$score%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
        }
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
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
