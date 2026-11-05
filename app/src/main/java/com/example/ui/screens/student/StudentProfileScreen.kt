package com.example.ui.screens.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementBadge
import com.example.data.remote.SupabaseConnectionState
import com.example.ui.MainViewModel
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
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    var showVocabNotebook by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("student_profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Shifted Student Portal Header & Quick Actions Bar
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("shifted_student_header_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top Bar: Branding & Executive Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        Brush.linearGradient(listOf(RoyalBlue, PurpleAccent))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("SP", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                            Column {
                                Text(
                                    text = "Speakly",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Student Portal",
                                    fontSize = 11.sp,
                                    color = RoyalBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Action Buttons: Cloud DB + Switch Role + Logout
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Cloud DB connection
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (supabaseStatus is SupabaseConnectionState.Connected) Color(0xFFDCFCE7) else Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, if (supabaseStatus is SupabaseConnectionState.Connected) EmeraldSuccess else Color(0xFFCBD5E1)),
                                modifier = Modifier.clickable { viewModel.toggleSupabaseModal(true) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("☁", fontSize = 12.sp)
                                    Text(
                                        "DB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (supabaseStatus is SupabaseConnectionState.Connected) EmeraldSuccess else MutedGray
                                    )
                                }
                            }

                            // Role Switcher
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { viewModel.toggleRoleSwitcher(true) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.SwapHoriz, contentDescription = "Switch Role", tint = RoyalBlue, modifier = Modifier.size(15.dp))
                                    Text("Role", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
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

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // Shifted Real-time Stat Pills (Streak, XP, Coins)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Streak Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFF7ED),
                            border = BorderStroke(1.dp, OrangeVibrant.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🔥", fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text("${profile.streakDays} Days", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = OrangeVibrant)
                                    Text("Streak", fontSize = 9.sp, color = MutedGray)
                                }
                            }
                        }

                        // XP Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEFF6FF),
                            border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("⚡", fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text("${profile.xpPoints}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = RoyalBlue)
                                    Text("Fluency XP", fontSize = 9.sp, color = MutedGray)
                                }
                            }
                        }

                        // Coins Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEFCE8),
                            border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🪙", fontSize = 16.sp)
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text("${profile.coins}", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = AmberGold)
                                    Text("Coins", fontSize = 9.sp, color = MutedGray)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Leaderboard Standing Card on Profile
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
                border = BorderStroke(1.2.dp, Color(0xFFE9D5FF)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF3E8FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏆", fontSize = 22.sp)
                        }
                        Column {
                            Text("Academy Leaderboard Standing", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepNavy)
                            Text("Rank #3 of 48 in Cohort • Gold League", fontSize = 11.sp, color = MutedGray)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PurpleAccent
                    ) {
                        Text("Rank #3", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                    }
                }
            }
        }

        // 1. User Avatar & Identity Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(user.avatarEmoji, fontSize = 36.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(user.email, fontSize = 12.sp, color = MutedGray)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${profile.currentLevel.displayName} (${profile.currentLevel.cefr})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = profile.league,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AmberGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Comprehensive Skill Proficiency Breakdown
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
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
                        Text("Proficiency Scores (CEFR A2)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${profile.overallScore}% Overall", fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = RoyalBlue)
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

        // 3. Learning Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Practice Time",
                    value = "${profile.speakingPracticeMinutes} min",
                    emoji = "⏱️",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Stages Passed",
                    value = "${profile.completedStagesCount}",
                    emoji = "🏆",
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Current Streak",
                    value = "${profile.streakDays} Days",
                    emoji = "🔥",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Vocabulary Notebook Shortcut
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showVocabNotebook = !showVocabNotebook }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("📚", fontSize = 26.sp)
                        Column {
                            Text("Personal Vocabulary Notebook", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepNavy)
                            Text("${vocabulary.size} words saved • Audio flashcards", fontSize = 12.sp, color = MutedGray)
                        }
                    }
                    Icon(
                        if (showVocabNotebook) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
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
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(word.word, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                        Text(word.phonetic, fontSize = 12.sp, color = RoyalBlue)
                                        Text(word.partOfSpeech, fontSize = 11.sp, color = MutedGray)
                                    }
                                    Text(word.meaning, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("\"${word.exampleSentence}\"", fontSize = 11.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, color = MutedGray)
                                }
                                IconButton(onClick = { viewModel.speakAudio("${word.word}. ${word.meaning}") }) {
                                    Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen", tint = RoyalBlue)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. Achievement Badges Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Achievement Badges", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                badges.forEach { badge ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                            Text(badge.iconEmoji, fontSize = 28.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(badge.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(badge.description, fontSize = 11.sp, color = MutedGray)
                            }
                            if (badge.isUnlocked) {
                                Text("UNLOCKED", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = EmeraldSuccess)
                            } else {
                                Text("${(badge.progress * 100).toInt()}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedGray)
                            }
                        }
                    }
                }
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
fun MetricCard(title: String, value: String, emoji: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(title, fontSize = 10.sp, color = MutedGray)
        }
    }
}
