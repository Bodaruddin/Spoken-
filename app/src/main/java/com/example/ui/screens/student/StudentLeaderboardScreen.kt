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
    val currentUser by viewModel.currentUser.collectAsState()
    val leaderboard by viewModel.leaderboard.collectAsState()
    val academicClasses by viewModel.academicClasses.collectAsState()
    var selectedCohort by remember { mutableStateOf("All Classes") }
    var searchQuery by remember { mutableStateOf("") }

    val cohortTabs = remember(academicClasses) {
        listOf("All Classes") + academicClasses
    }

    val filteredLeaderboard = remember(leaderboard, selectedCohort, searchQuery) {
        val cohortFiltered = if (selectedCohort == "All Classes") {
            leaderboard
        } else {
            leaderboard.filter {
                it.studentClass.equals(selectedCohort, ignoreCase = true) ||
                it.studentClass.contains(selectedCohort, ignoreCase = true)
            }
        }.mapIndexed { idx, user ->
            user.copy(rank = idx + 1)
        }

        if (searchQuery.isBlank()) cohortFiltered
        else cohortFiltered.filter {
            it.name.contains(searchQuery.trim(), ignoreCase = true) ||
            it.studentClass.contains(searchQuery.trim(), ignoreCase = true)
        }
    }

    val top1 = filteredLeaderboard.getOrNull(0)
    val top2 = filteredLeaderboard.getOrNull(1)
    val top3 = filteredLeaderboard.getOrNull(2)

    val myIndex = filteredLeaderboard.indexOfFirst { it.isCurrentUser }
    val myEntry = if (myIndex >= 0) filteredLeaderboard[myIndex] else null
    val myRank = if (myIndex >= 0) myIndex + 1 else null

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
                            border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.6f)),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🏆", fontSize = 14.sp)
                                Text(
                                    "ACADEMY FLUENCY LEADERBOARD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFFEF08A),
                                    letterSpacing = 0.5.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

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
                                    "${filteredLeaderboard.size} Scholars",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    softWrap = false,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Fluency & Practice Leaderboard",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = Color.White
                        )
                        Text(
                            text = "Real-time rankings by XP and daily practice streaks across academic classes.",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    // Dynamic Class Switcher Tabs
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(cohortTabs) { cohort ->
                            val isSelected = selectedCohort == cohort
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) RoyalBlue else Color(0xFF1E293B),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFF334155)),
                                modifier = Modifier
                                    .clickable { selectedCohort = cohort }
                                    .testTag("cohort_tab_${cohort.lowercase().replace(" ", "_")}")
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
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

        // 2. Top 3 Champions Podium
        if (filteredLeaderboard.isNotEmpty()) {
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
                            if (top2 != null) {
                                LeaderboardPodiumPillar(
                                    rank = 2,
                                    name = top2.name,
                                    emoji = top2.avatarEmoji,
                                    xp = "${top2.xp} XP",
                                    streak = "${top2.streak}d",
                                    medal = "🥈",
                                    league = top2.league,
                                    badgeColor = Color(0xFFE2E8F0),
                                    containerColor = Color(0xFF0F172A),
                                    borderColor = Color(0xFF94A3B8),
                                    isCurrentUser = top2.isCurrentUser,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }

                            // 1st Place: Gold (Elevated)
                            if (top1 != null) {
                                LeaderboardPodiumPillar(
                                    rank = 1,
                                    name = top1.name,
                                    emoji = top1.avatarEmoji,
                                    xp = "${top1.xp} XP",
                                    streak = "${top1.streak}d",
                                    medal = "👑",
                                    league = top1.league,
                                    badgeColor = Color(0xFFFDE047),
                                    containerColor = Color(0xFF1E1B4B),
                                    borderColor = Color(0xFFFACC15),
                                    isFirst = true,
                                    isCurrentUser = top1.isCurrentUser,
                                    modifier = Modifier.weight(1.15f)
                                )
                            }

                            // 3rd Place: Bronze
                            if (top3 != null) {
                                LeaderboardPodiumPillar(
                                    rank = 3,
                                    name = top3.name,
                                    emoji = top3.avatarEmoji,
                                    xp = "${top3.xp} XP",
                                    streak = "${top3.streak}d",
                                    medal = "🥉",
                                    league = top3.league,
                                    badgeColor = Color(0xFFFED7AA),
                                    containerColor = Color(0xFF0F172A),
                                    borderColor = Color(0xFFFB923C),
                                    isCurrentUser = top3.isCurrentUser,
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // 3. User's Personal Standing Alert Card (Dynamic to logged-in student)
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
                                if (myRank != null && myEntry != null) {
                                    Text(
                                        text = "Your Standing: Rank #$myRank in $selectedCohort",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    if (myRank == 1) {
                                        Text(
                                            text = "🏆 You are leading this cohort! Keep practicing to stay #1.",
                                            fontSize = 11.sp,
                                            color = EmeraldSuccess
                                        )
                                    } else {
                                        val studentAbove = filteredLeaderboard[myIndex - 1]
                                        val diff = maxOf(10, (studentAbove.xp - myEntry.xp) + 10)
                                        Text(
                                            text = "Only $diff XP needed to overtake #${studentAbove.rank} ${studentAbove.name.replace(" (You)", "")}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF93C5FD)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "Your Cohort: ${currentUser.studentClass}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Select '${currentUser.studentClass}' or 'All Classes' to view your standing",
                                        fontSize = 11.sp,
                                        color = Color(0xFF93C5FD)
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RoyalBlue
                        ) {
                            Text(
                                text = if (myRank != null) "Rank #$myRank" else profile.league,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Progress Track toward Rank Progress
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Current Score: ${profile.xpPoints} XP • 🔥 ${profile.streakDays}d Streak", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            Text(profile.league, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                        }
                        val xpProgress = (profile.xpPoints % 1000) / 1000f
                        LinearProgressIndicator(
                            progress = { xpProgress.coerceIn(0.1f, 1f) },
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
                placeholder = { Text("Search classmates by name or class...", fontSize = 13.sp) },
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
                color = MaterialTheme.colorScheme.onSurface,
                softWrap = false,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        items(filteredLeaderboard) { student ->
            val isUser = student.isCurrentUser
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
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val cleanName = student.name.replace(" (You)", "").replace("(You)", "").trim()
                                Text(
                                    text = cleanName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isUser) RoyalBlue else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
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
                                            softWrap = false,
                                            maxLines = 1,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "🔥 ${student.streak}d streak • ${student.studentClass} • CEFR: ${student.cefrLevel}",
                                fontSize = 11.sp,
                                color = MutedGray,
                                maxLines = 1,
                                softWrap = false,
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
            val cleanName = name.replace(" (You)", "").replace("(You)", "").trim()
            Text(
                text = if (isCurrentUser) "$cleanName (You)" else cleanName,
                fontSize = if (isFirst) 12.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isCurrentUser) Color(0xFF93C5FD) else Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                softWrap = false,
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
