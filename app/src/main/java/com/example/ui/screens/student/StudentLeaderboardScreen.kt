package com.example.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LeaderboardUser
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun StudentLeaderboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.studentProfile.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    var selectedCohort by remember { mutableStateOf("Batch Alpha") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredLeaderboard = remember(leaderboard, selectedCohort, searchQuery) {
        val cohortFiltered = when (selectedCohort) {
            "Batch Alpha" -> leaderboard.filter { it.rank in listOf(1, 3, 4, 6) }
            "Batch Beta" -> leaderboard.filter { it.rank in listOf(2, 5, 7, 8) }
            else -> leaderboard
        }
        if (searchQuery.isBlank()) cohortFiltered
        else cohortFiltered.filter { it.name.contains(searchQuery.trim(), ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("student_leaderboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Leaderboard Championship Banner
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
                                Color(0xFF0F172A), // Deep Navy
                                Color(0xFF1E1B4B), // Royal Violet
                                Color(0xFF312E81)  // Luxury Indigo
                            )
                        )
                    )
                    .testTag("leaderboard_hero_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF2E1065).copy(alpha = 0.85f),
                            border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🏆", fontSize = 14.sp)
                                Text(
                                    "WEEKLY CHAMPIONSHIP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFEF08A),
                                    letterSpacing = 1.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("⚡", fontSize = 12.sp)
                                Text(
                                    "Resets in 3d 14h",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Academy Fluency Leaderboard",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Compete with fellow scholars, speak fluently, and rise through the diamond leagues.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Cohort Switcher Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Batch Alpha", "All Academy", "Batch Beta").forEach { cohort ->
                            val isSelected = selectedCohort == cohort
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) RoyalBlue else Color(0xFF1E293B),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier
                                    .clickable { selectedCohort = cohort }
                                    .weight(1f)
                                    .testTag("cohort_tab_${cohort.lowercase().replace(" ", "_")}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 10.dp)
                                ) {
                                    Text(
                                        text = cohort,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Top 3 Podium Showcase
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "👑 Champions Podium ($selectedCohort)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 2nd Place: Silver
                        LeaderboardPodiumPillar(
                            rank = 2,
                            name = "David Chen",
                            emoji = "🐯",
                            xp = "2,420 XP",
                            streak = "14d",
                            medal = "🥈",
                            league = "Diamond",
                            badgeColor = Color(0xFFE2E8F0),
                            containerColor = Color(0xFF0F172A),
                            borderColor = Color(0xFF94A3B8),
                            modifier = Modifier.weight(1f)
                        )

                        // 1st Place: Gold (Elevated)
                        LeaderboardPodiumPillar(
                            rank = 1,
                            name = "Elena Rostova",
                            emoji = "🦄",
                            xp = "2,850 XP",
                            streak = "19d",
                            medal = "👑",
                            league = "Diamond",
                            badgeColor = Color(0xFFFDE047),
                            containerColor = Color(0xFF1E1B4B),
                            borderColor = Color(0xFFFACC15),
                            isFirst = true,
                            modifier = Modifier.weight(1.15f)
                        )

                        // 3rd Place: Bronze (Current User)
                        LeaderboardPodiumPillar(
                            rank = 3,
                            name = "Alex Morgan",
                            emoji = "🦉",
                            xp = "${profile.xpPoints} XP",
                            streak = "${profile.streakDays}d",
                            medal = "🥉",
                            league = "Gold",
                            badgeColor = Color(0xFFFED7AA),
                            containerColor = Color(0xFF0F172A),
                            borderColor = Color(0xFFFB923C),
                            isCurrentUser = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 3. User's Personal Standing Alert Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(1.2.dp, Color(0xFF3B82F6).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1E3A8A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⭐", fontSize = 20.sp)
                            }
                            Column {
                                Text(
                                    text = "Your Standing: Rank #3 in $selectedCohort",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "Only 970 XP needed to overtake #2 David Chen",
                                    fontSize = 11.sp,
                                    color = Color(0xFF93C5FD)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RoyalBlue
                        ) {
                            Text(
                                text = "Top 5%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Progress Track toward Rank 2
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Season XP Target: 2,500 XP", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text("60% Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                        }
                        LinearProgressIndicator(
                            progress = { 0.60f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = RoyalBlue,
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }
            }
        }

        // 4. Search Filter for Classmates
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search classmates by name...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("leaderboard_search_input")
            )
        }

        // 5. Full Ranks List
        item {
            Text(
                text = "All Academy Ranks (${filteredLeaderboard.size} scholars)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        items(filteredLeaderboard) { student ->
            val isUser = student.name.contains("Alex Morgan") || student.isCurrentUser
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isUser) Color(0xFF1E3A8A).copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    if (isUser) 1.5.dp else 1.dp,
                    if (isUser) RoyalBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_rank_${student.rank}")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
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
                        // Rank Icon
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when (student.rank) {
                                        1 -> Color(0xFFFEF08A)
                                        2 -> Color(0xFFE2E8F0)
                                        3 -> Color(0xFFFFEDD5)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
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
                                color = if (student.rank <= 3) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E293B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(student.avatarEmoji, fontSize = 22.sp)
                        }

                        // Info
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = student.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isUser) RoyalBlue else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isUser) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = RoyalBlue.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            "YOU",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = RoyalBlue,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "🔥 ${student.streak}d streak • Rank #${student.rank}",
                                fontSize = 11.sp,
                                color = MutedGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // XP Score Badge
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AmberGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${student.xp} XP",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = AmberGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = student.league,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardPodiumPillar(
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
    modifier: Modifier = Modifier,
    isFirst: Boolean = false,
    isCurrentUser: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(if (isFirst || isCurrentUser) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFirst) 6.dp else 2.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (isFirst) 14.dp else 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            // Medal Banner
            Text(medal, fontSize = if (isFirst) 24.sp else 18.sp)

            // Avatar Box
            Box(
                modifier = Modifier
                    .size(if (isFirst) 54.dp else 44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
                    .border(2.dp, borderColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(emoji, fontSize = if (isFirst) 28.sp else 22.sp)
            }

            // Name
            Text(
                text = name,
                fontSize = if (isFirst) 12.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // XP Badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = badgeColor.copy(alpha = 0.25f)
            ) {
                Text(
                    text = xp,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeColor,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Streak & League
            Text(
                text = "🔥 $streak",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}
