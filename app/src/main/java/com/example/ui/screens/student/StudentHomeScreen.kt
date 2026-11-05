package com.example.ui.screens.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.LeaderboardUser
import com.example.ui.ActiveGameType
import com.example.ui.MainViewModel
import com.example.ui.SpeakingSubTab
import com.example.ui.StudentTab
import com.example.ui.theme.*

@Composable
fun StudentHomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val profile by viewModel.studentProfile.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    var showFullLeaderboard by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("student_home_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 0. Students Leaderboard Showcase
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("students_leaderboard_home_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Leaderboard Title & View Full Board Trigger
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
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(listOf(Color(0xFFFEF08A), Color(0xFFF59E0B)))
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏆", fontSize = 22.sp)
                            }
                            Column {
                                Text(
                                    text = "Students Leaderboard",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Weekly Spoken Fluency Championship",
                                    fontSize = 11.sp,
                                    color = MutedGray
                                )
                            }
                        }

                        Button(
                            onClick = { showFullLeaderboard = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Full Board ↗", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Top 3 Podium
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 2nd Place (Silver)
                        PodiumStudentCard(
                            rank = 2,
                            name = "David Chen",
                            emoji = "🐯",
                            xp = "2,420 XP",
                            streak = "14d",
                            medal = "🥈",
                            badgeColor = Color(0xFFE2E8F0),
                            containerColor = Color(0xFFF8FAFC),
                            modifier = Modifier.weight(1f)
                        )

                        // 1st Place (Gold - Elevated)
                        PodiumStudentCard(
                            rank = 1,
                            name = "Elena Rostova",
                            emoji = "🦄",
                            xp = "2,850 XP",
                            streak = "19d",
                            medal = "👑",
                            badgeColor = Color(0xFFFDE047),
                            containerColor = Color(0xFFFEFCE8),
                            isFirst = true,
                            modifier = Modifier.weight(1.15f)
                        )

                        // 3rd Place (Bronze - Current User Alex Morgan)
                        PodiumStudentCard(
                            rank = 3,
                            name = "Alex Morgan",
                            emoji = "🦉",
                            xp = "${profile.xpPoints} XP",
                            streak = "${profile.streakDays}d",
                            medal = "🥉",
                            badgeColor = Color(0xFFFED7AA),
                            containerColor = Color(0xFFFFF7ED),
                            isCurrentUser = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // User Standing Alert
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFullLeaderboard = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("⭐", fontSize = 16.sp)
                                Column {
                                    Text(
                                        text = "Your Standing: Rank #3 in Batch Alpha",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = DeepNavy
                                    )
                                    Text(
                                        text = "Only 751 XP to overtake #2 David Chen!",
                                        fontSize = 10.sp,
                                        color = RoyalBlue
                                    )
                                }
                            }
                            Text("Standings ➔", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                        }
                    }
                }
            }
        }

        // 1. Hero Mascot Welcome Card
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_mascot_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Friendly Mascot Image
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_mascot),
                            contentDescription = "Echo the Mascot",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "Daily English Adventure",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "Hello, ${user.name}! 👋",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Ready to boost your speaking fluency today? Your 7-day streak is blazing hot!",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // 2. Primary "Continue Learning" Action Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(RoyalBlue, PurpleDark)
                        )
                    )
                    .clickable { viewModel.selectStudentTab(StudentTab.LEARN) }
                    .testTag("continue_learning_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "CONTINUE JOURNEY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFBFDBFE),
                            letterSpacing = 1.sp
                        )
                        Text(
                            "World 2: Stage 4",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Airport Check-in • Spoken Dialogue Practice",
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Start Lesson",
                            tint = RoyalBlue,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // 3. Today's Daily Missions
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🎯", fontSize = 18.sp)
                            Text("Today's Missions", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Text("2 / 3 Done", fontSize = 12.sp, color = RoyalBlue, fontWeight = FontWeight.Bold)
                    }

                    MissionItem("Practice 1 Speaking Roleplay", "50 XP", 1f, true)
                    MissionItem("Score >80% on Pronunciation Drill", "40 XP", 1f, true)
                    MissionItem("Win 1 Vocabulary Game Challenge", "60 XP", 0.4f, false)
                }
            }
        }

        // 4. Quick Practice Hub Launchers
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Quick Learning Hubs",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickHubCard(
                        title = "AI Speaking",
                        subtitle = "Voice Partner",
                        emoji = "🎙️",
                        color = Color(0xFFEFF6FF),
                        accent = RoyalBlue,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectSpeakingSubTab(SpeakingSubTab.AI_PARTNER)
                            viewModel.selectStudentTab(StudentTab.PRACTICE)
                        }
                    )

                    QuickHubCard(
                        title = "Roleplays",
                        subtitle = "Real Life Dialogues",
                        emoji = "🎭",
                        color = Color(0xFFFDF4FF),
                        accent = PurpleAccent,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectSpeakingSubTab(SpeakingSubTab.ROLEPLAY)
                            viewModel.selectStudentTab(StudentTab.PRACTICE)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickHubCard(
                        title = "Phonetics",
                        subtitle = "Pronunciation Coach",
                        emoji = "🎯",
                        color = Color(0xFFFFFBEB),
                        accent = AmberGold,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectSpeakingSubTab(SpeakingSubTab.PRONUNCIATION_COACH)
                            viewModel.selectStudentTab(StudentTab.PRACTICE)
                        }
                    )

                    QuickHubCard(
                        title = "Mini Games",
                        subtitle = "12 Gamified Drills",
                        emoji = "🎮",
                        color = Color(0xFFECFDF5),
                        accent = EmeraldSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectStudentTab(StudentTab.GAMES)
                        }
                    )
                }
            }
        }

        // 5. Recent Announcements
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📢", fontSize = 16.sp)
                        Text("Campus Announcements", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    announcements.firstOrNull()?.let { note ->
                        Text(
                            text = note.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = note.message,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (showFullLeaderboard) {
        FullStudentsLeaderboardDialog(
            leaderboard = leaderboard,
            currentUserName = user.name,
            currentXp = profile.xpPoints,
            currentStreak = profile.streakDays,
            onDismiss = { showFullLeaderboard = false }
        )
    }
}

@Composable
fun PodiumStudentCard(
    rank: Int,
    name: String,
    emoji: String,
    xp: String,
    streak: String,
    medal: String,
    badgeColor: Color,
    containerColor: Color,
    isFirst: Boolean = false,
    isCurrentUser: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(
            if (isFirst || isCurrentUser) 1.5.dp else 1.dp,
            if (isCurrentUser) RoyalBlue else if (isFirst) AmberGold else Color(0xFFCBD5E1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFirst) 4.dp else 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = if (isFirst) 14.dp else 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(medal, fontSize = if (isFirst) 22.sp else 16.sp)

            Box(
                modifier = Modifier
                    .size(if (isFirst) 46.dp else 38.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = if (isFirst) 24.sp else 20.sp)
            }

            Text(
                text = if (isCurrentUser) "$name (You)" else name,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                maxLines = 1,
                color = if (isCurrentUser) RoyalBlue else MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = xp,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
                color = if (isFirst) AmberGold else if (isCurrentUser) RoyalBlue else MutedGray
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color.White
            ) {
                Text(
                    text = "🔥 $streak",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = OrangeVibrant,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun FullStudentsLeaderboardDialog(
    leaderboard: List<LeaderboardUser>,
    currentUserName: String,
    currentXp: Int,
    currentStreak: Int,
    onDismiss: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All Academy") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🏆", fontSize = 24.sp)
                    Column {
                        Text("Academy Leaderboard", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Weekly Spoken English Rankings", fontSize = 11.sp, color = MutedGray)
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Filter Tabs
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All Academy", "Batch Alpha", "Batch Beta").forEach { tab ->
                        val isSelected = selectedFilter == tab
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = tab },
                            label = { Text(tab, fontSize = 11.sp) }
                        )
                    }
                }

                // Rankings List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(leaderboard) { student ->
                        val isUser = student.name.contains("Alex Morgan") || student.isCurrentUser
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isUser) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            ),
                            border = if (isUser) BorderStroke(1.5.dp, RoyalBlue) else null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (student.rank) {
                                                    1 -> Color(0xFFFEF08A)
                                                    2 -> Color(0xFFE2E8F0)
                                                    3 -> Color(0xFFFFEDD5)
                                                    else -> Color(0xFFF1F5F9)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (student.rank) {
                                                1 -> "🥇"
                                                2 -> "🥈"
                                                3 -> "🥉"
                                                else -> "#${student.rank}"
                                            },
                                            fontSize = if (student.rank <= 3) 16.sp else 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepNavy
                                        )
                                    }

                                    Text(student.avatarEmoji, fontSize = 22.sp)

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = student.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = if (isUser) RoyalBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isUser) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = RoyalBlue
                                                ) {
                                                    Text("YOU", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                }
                                            }
                                        }
                                        Text(
                                            text = "${student.league} • 🔥 ${if (isUser) currentStreak else student.streak} days",
                                            fontSize = 11.sp,
                                            color = MutedGray
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isUser) RoyalBlue else Color.White
                                ) {
                                    Text(
                                        text = "${if (isUser) currentXp else student.xp} XP",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.sp,
                                        color = if (isUser) Color.White else DeepNavy,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                Text("Close")
            }
        }
    )
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
fun QuickHubCard(
    title: String,
    subtitle: String,
    emoji: String,
    color: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier
            .clickable { onClick() }
            .testTag("quick_hub_${title.lowercase().replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(emoji, fontSize = 24.sp)
            Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepNavy)
            Text(subtitle, fontSize = 11.sp, color = MutedGray)
        }
    }
}
