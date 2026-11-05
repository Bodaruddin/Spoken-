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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AchievementBadge
import com.example.data.model.LeaderboardUser
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
    val leaderboard by viewModel.leaderboard.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()

    var showVocabNotebook by remember { mutableStateOf(false) }
    var selectedLeaderboardFilter by remember { mutableStateOf("Batch Alpha") }
    var showAllRankings by remember { mutableStateOf(false) }

    val filteredLeaderboard = remember(leaderboard, selectedLeaderboardFilter) {
        when (selectedLeaderboardFilter) {
            "Batch Alpha" -> leaderboard.filter { it.rank in listOf(1, 3, 4) }
            "Batch Beta" -> leaderboard.filter { it.rank in listOf(2, 5, 7) }
            else -> leaderboard
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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

                        // Utility Actions (DB + Switch Role + Logout)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cloud DB connection status pill
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (supabaseStatus is SupabaseConnectionState.Connected) Color(0xFF064E3B) else Color(0xFF1E293B),
                                border = BorderStroke(1.dp, if (supabaseStatus is SupabaseConnectionState.Connected) EmeraldSuccess else Color(0xFF475569)),
                                modifier = Modifier.clickable { viewModel.toggleSupabaseModal(true) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("☁", fontSize = 11.sp, color = if (supabaseStatus is SupabaseConnectionState.Connected) EmeraldSuccess else Color.White)
                                    Text(
                                        "DB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (supabaseStatus is SupabaseConnectionState.Connected) EmeraldSuccess else Color(0xFFCBD5E1)
                                    )
                                }
                            }

                            // Role Switcher pill
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                                modifier = Modifier.clickable { viewModel.toggleRoleSwitcher(true) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.SwapHoriz, contentDescription = "Switch Role", tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                                    Text("Role", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
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
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Luxury Glassmorphic Performance Metrics Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PremiumStatCard(
                    title = "Streak",
                    value = "${profile.streakDays} Days",
                    sub = "🔥 Blazing",
                    accent = OrangeVibrant,
                    bg = Color(0xFFFFF7ED),
                    border = Color(0xFFFFEDD5),
                    modifier = Modifier.weight(1f)
                )

                PremiumStatCard(
                    title = "Fluency XP",
                    value = "${profile.xpPoints}",
                    sub = "⚡ Gold Rank",
                    accent = RoyalBlue,
                    bg = Color(0xFFEFF6FF),
                    border = Color(0xFFDBEAFE),
                    modifier = Modifier.weight(1f)
                )

                PremiumStatCard(
                    title = "Coins",
                    value = "${profile.coins}",
                    sub = "🪙 Balance",
                    accent = AmberGold,
                    bg = Color(0xFFFEFCE8),
                    border = Color(0xFFFEF08A),
                    modifier = Modifier.weight(1f)
                )

                PremiumStatCard(
                    title = "Speaking",
                    value = "${profile.speakingPracticeMinutes}m",
                    sub = "⏱️ Time",
                    accent = EmeraldSuccess,
                    bg = Color(0xFFECFDF5),
                    border = Color(0xFFA7F3D0),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 3. Shifted & Redesigned Premier Academy Leaderboard
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_profile_leaderboard_hub")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with Championship trophy & cohort filters
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

                            Column {
                                Text(
                                    text = "Academy Fluency Leaderboard",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Weekly Cohort Speaking Championship",
                                    fontSize = 11.sp,
                                    color = MutedGray
                                )
                            }
                        }

                        // Toggle View All / Podium
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.clickable { showAllRankings = !showAllRankings }
                        ) {
                            Text(
                                text = if (showAllRankings) "Podium 👑" else "Full List ➔",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Cohort Filter Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Batch Alpha", "All Academy", "Batch Beta").forEach { cohort ->
                            val isSelected = selectedLeaderboardFilter == cohort
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .clickable { selectedLeaderboardFilter = cohort }
                                    .weight(1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = cohort,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    // Top 3 Podium (Always visible or primary showcase)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // 2nd Place: Silver (David Chen)
                        PremiumPodiumPillar(
                            rank = 2,
                            name = "David Chen",
                            emoji = "🐯",
                            xp = "2,420 XP",
                            streak = "14d",
                            medal = "🥈",
                            league = "Diamond",
                            badgeColor = Color(0xFFE2E8F0),
                            containerColor = Color(0xFFF8FAFC),
                            borderColor = Color(0xFFCBD5E1),
                            modifier = Modifier.weight(1f)
                        )

                        // 1st Place: Gold (Elena Rostova - Elevated)
                        PremiumPodiumPillar(
                            rank = 1,
                            name = "Elena Rostova",
                            emoji = "🦄",
                            xp = "2,850 XP",
                            streak = "19d",
                            medal = "👑",
                            league = "Diamond",
                            badgeColor = Color(0xFFFDE047),
                            containerColor = Color(0xFFFEFCE8),
                            borderColor = Color(0xFFFACC15),
                            isFirst = true,
                            modifier = Modifier.weight(1.15f)
                        )

                        // 3rd Place: Bronze (Current User - Alex Morgan)
                        PremiumPodiumPillar(
                            rank = 3,
                            name = "Alex Morgan",
                            emoji = "🦉",
                            xp = "${profile.xpPoints} XP",
                            streak = "${profile.streakDays}d",
                            medal = "🥉",
                            league = "Gold",
                            badgeColor = Color(0xFFFED7AA),
                            containerColor = Color(0xFFFFF7ED),
                            borderColor = Color(0xFFFB923C),
                            isCurrentUser = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // User Personal Standing & Challenge Alert Banner
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.2.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    Text("⭐", fontSize = 18.sp)
                                    Column {
                                        Text(
                                            text = "Your Standing: Rank #3 in $selectedLeaderboardFilter",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = DeepNavy
                                        )
                                        Text(
                                            text = "970 XP needed to overtake #2 David Chen",
                                            fontSize = 11.sp,
                                            color = RoyalBlue
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = RoyalBlue
                                ) {
                                    Text(
                                        text = "Top 5%",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Progress Track toward Rank 2
                            LinearProgressIndicator(
                                progress = { 0.60f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = RoyalBlue,
                                trackColor = Color(0xFFDBEAFE)
                            )
                        }
                    }

                    // Full Roster Breakdown (Shown when toggled or expanded)
                    AnimatedVisibility(visible = showAllRankings) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "All Academy Ranks ($selectedLeaderboardFilter)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MutedGray
                            )

                            filteredLeaderboard.forEach { student ->
                                val isUser = student.name.contains("Alex Morgan") || student.isCurrentUser
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isUser) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
                                    border = BorderStroke(
                                        if (isUser) 1.5.dp else 1.dp,
                                        if (isUser) RoyalBlue else Color(0xFFE2E8F0)
                                    ),
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
                                            // Rank Icon
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
                                                    fontSize = if (student.rank <= 3) 15.sp else 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepNavy
                                                )
                                            }

                                            Text(student.avatarEmoji, fontSize = 22.sp)

                                            Column {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
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
                                                            Text(
                                                                "YOU",
                                                                fontSize = 9.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color.White,
                                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                }

                                                Text(
                                                    text = "${student.league} • 🔥 ${student.streak}d streak",
                                                    fontSize = 11.sp,
                                                    color = MutedGray
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isUser) RoyalBlue else Color.White,
                                            border = if (isUser) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
                                        ) {
                                            Text(
                                                text = "${student.xp} XP",
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
                }
            }
        }

        // Dedicated Student Panel Database Connection Option
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (supabaseStatus is SupabaseConnectionState.Connected) Color(0xFFF0FDF4) else Color(0xFFF8FAFC)
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (supabaseStatus is SupabaseConnectionState.Connected) EmeraldSuccess else Color(0xFFE2E8F0)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_database_connection_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header with icon and status chip
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
                                    .background(if (supabaseStatus is SupabaseConnectionState.Connected) EmeraldSuccess else RoyalBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (supabaseStatus is SupabaseConnectionState.Connected) "☁️" else "🗄️",
                                    fontSize = 20.sp
                                )
                            }
                            Column {
                                Text(
                                    text = "Database Connection & Sync",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = DeepNavy
                                )
                                Text(
                                    text = "PostgreSQL Supabase & Local SQLite",
                                    fontSize = 11.sp,
                                    color = MutedGray
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when (supabaseStatus) {
                                is SupabaseConnectionState.Connected -> Color(0xFFDCFCE7)
                                is SupabaseConnectionState.Connecting -> Color(0xFFFEF3C7)
                                is SupabaseConnectionState.Error -> Color(0xFFFEE2E2)
                                else -> Color(0xFFF1F5F9)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (supabaseStatus) {
                                                is SupabaseConnectionState.Connected -> EmeraldSuccess
                                                is SupabaseConnectionState.Connecting -> AmberGold
                                                is SupabaseConnectionState.Error -> CoralAccent
                                                else -> MutedGray
                                            }
                                        )
                                )
                                Text(
                                    text = when (supabaseStatus) {
                                        is SupabaseConnectionState.Connected -> "Online"
                                        is SupabaseConnectionState.Connecting -> "Connecting"
                                        is SupabaseConnectionState.Error -> "Offline"
                                        else -> "Local Cache"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (supabaseStatus) {
                                        is SupabaseConnectionState.Connected -> EmeraldSuccess
                                        is SupabaseConnectionState.Connecting -> Color(0xFFB45309)
                                        is SupabaseConnectionState.Error -> CoralAccent
                                        else -> MutedGray
                                    }
                                )
                            }
                        }
                    }

                    // Synced metrics preview row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Student XP", fontSize = 10.sp, color = MutedGray)
                                Text("${profile.xpPoints}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalBlue)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Coins Sync", fontSize = 10.sp, color = MutedGray)
                                Text("${profile.coins}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AmberGold)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Speech Log", fontSize = 10.sp, color = MutedGray)
                                Text("${profile.speakingPracticeMinutes}m", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = EmeraldSuccess)
                            }
                        }
                    }

                    // Admin-Only Permission Warning Notice
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🔒", fontSize = 16.sp)
                            Column {
                                Text(
                                    text = "Only Admin Can Connect Database",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = "Credentials & cloud sync are protected. Connect or test database using Admin credentials.",
                                    fontSize = 10.sp,
                                    color = DeepNavy
                                )
                            }
                        }
                    }

                    // Action Button to view / connect
                    Button(
                        onClick = { viewModel.toggleSupabaseModal(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("student_open_database_modal_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (supabaseStatus is SupabaseConnectionState.Connected) EmeraldSuccess else RoyalBlue
                        )
                    ) {
                        Icon(
                            imageVector = if (supabaseStatus is SupabaseConnectionState.Connected) Icons.Filled.CloudDone else Icons.Filled.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (supabaseStatus is SupabaseConnectionState.Connected) "View Database Status (Live)" else "Database Connection Options (Admin)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
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
                        Column {
                            Text(
                                text = "CEFR Spoken Proficiency Radar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Real-time AI diagnostic scores (Level A2)",
                                fontSize = 11.sp,
                                color = MutedGray
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
                        Column {
                            Text(
                                text = "Personal Vocabulary Vault",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = DeepNavy
                            )
                            Text(
                                text = "${vocabulary.size} words saved • Audio flashcards & Assamese",
                                fontSize = 11.sp,
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🎖️", fontSize = 20.sp)
                            Text("Trophy & Honors Cabinet", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(title, fontSize = 10.sp, color = MutedGray, fontWeight = FontWeight.Medium)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = DeepNavy)
            Text(sub, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = accent)
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
            modifier = Modifier.padding(vertical = if (isFirst) 14.dp else 10.dp, horizontal = 8.dp),
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
                color = if (isCurrentUser) RoyalBlue else DeepNavy
            )

            Text(
                text = xp,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 11.sp,
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
