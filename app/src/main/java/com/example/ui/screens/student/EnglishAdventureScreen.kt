@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens.student

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

private enum class AdventureTab(val title: String, val emoji: String) {
    WORLDS("World Map", "🗺️"),
    LEADERBOARD("Leaderboard", "🏆"),
    BADGES("Badges & Rewards", "🎖️")
}

@Composable
fun EnglishAdventureScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val worlds by viewModel.adventureWorlds.collectAsState()
    val progress by viewModel.adventureProgress.collectAsState()
    val badges by viewModel.adventureBadges.collectAsState()
    val leaderboard by viewModel.adventureLeaderboard.collectAsState()

    val selectedWorld by viewModel.selectedAdventureWorld.collectAsState()
    val activeLevel by viewModel.activeAdventureLevel.collectAsState()
    val activeMission by viewModel.activeAdventureMission.collectAsState()

    var currentTab by remember { mutableStateOf(AdventureTab.WORLDS) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF090A1A))
    ) {
        // TOP 3D GAMING HEADER BAR
        AdventureHeaderBar(
            totalStars = progress.totalStars,
            totalCoins = progress.totalCoins,
            totalXp = progress.totalXp,
            selectedTab = currentTab,
            onTabSelected = { currentTab = it }
        )

        // MAIN CONTENT ROUTER
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                // 1. ACTIVE MISSION PLAYER (Interactive Quest)
                activeMission != null && activeLevel != null && selectedWorld != null -> {
                    AdventureMissionPlayer(
                        world = selectedWorld!!,
                        level = activeLevel!!,
                        mission = activeMission!!,
                        viewModel = viewModel,
                        onClose = { viewModel.exitAdventureMission() },
                        onNextMission = {
                            val nextM = activeLevel!!.missions.getOrNull(
                                activeLevel!!.missions.indexOfFirst { it.id == activeMission!!.id } + 1
                            )
                            if (nextM != null) {
                                viewModel.startAdventureMission(nextM)
                            } else {
                                viewModel.exitAdventureMission()
                            }
                        }
                    )
                }

                // 2. LEVEL MAP VIEW (Inside Chosen World)
                selectedWorld != null -> {
                    AdventureLevelMapView(
                        world = selectedWorld!!,
                        progress = progress,
                        onBack = { viewModel.selectAdventureWorld(null) },
                        onSelectLevel = { level ->
                            viewModel.selectAdventureLevel(level)
                        }
                    )
                }

                // 3. TAB VIEWS
                else -> {
                    when (currentTab) {
                        AdventureTab.WORLDS -> {
                            AdventureWorldMapView(
                                worlds = worlds,
                                progress = progress,
                                onSelectWorld = { world ->
                                    if (world.isUnlocked) {
                                        viewModel.selectAdventureWorld(world)
                                    }
                                }
                            )
                        }
                        AdventureTab.LEADERBOARD -> {
                            AdventureLeaderboardView(leaderboard = leaderboard)
                        }
                        AdventureTab.BADGES -> {
                            AdventureBadgesView(badges = badges, totalStars = progress.totalStars)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. HEADER BAR WITH 3D STAT CAPSULES & TAB CHIPS
// -------------------------------------------------------------
@Composable
private fun AdventureHeaderBar(
    totalStars: Int,
    totalCoins: Int,
    totalXp: Int,
    selectedTab: AdventureTab,
    onTabSelected: (AdventureTab) -> Unit
) {
    Surface(
        color = Color(0xFF11142F),
        border = BorderStroke(1.dp, Color(0xFF283060)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Stats Row: Stars, Coins, XP
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🗺️", fontSize = 24.sp)
                    Column {
                        Text(
                            "English Adventure",
                            fontWeight = FontWeight.Black,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                        Text(
                            "5 3D Interactive Worlds",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // Gaming Currency Pills
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Stars
                    CurrencyPill(icon = "⭐", value = "$totalStars", color = Color(0xFFFACC15))
                    // Coins
                    CurrencyPill(icon = "🪙", value = "$totalCoins", color = Color(0xFFF59E0B))
                    // XP
                    CurrencyPill(icon = "⚡", value = "$totalXp", color = Color(0xFF38BDF8))
                }
            }

            // Sub Tab Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AdventureTab.values().forEach { tab ->
                    val isSelected = tab == selectedTab
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF191D3C),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFFA855F7) else Color(0xFF2C3565)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onTabSelected(tab) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tab.emoji, fontSize = 14.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = tab.title,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyPill(icon: String, value: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1B2044),
        border = BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icon, fontSize = 12.sp)
            Text(
                text = value,
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
        }
    }
}

// -------------------------------------------------------------
// 2. WORLD MAP VIEW (The 5 3D Islands / Kingdoms)
// -------------------------------------------------------------
@Composable
private fun AdventureWorldMapView(
    worlds: List<AdventureWorld>,
    progress: AdventureProgress,
    onSelectWorld: (AdventureWorld) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Hero Intro Banner
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF13173B),
                border = BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(Color(0xFFEC4899), Color(0xFF8B5CF6), Color(0xFF06B6D4))
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "⚔️ Your English Odyssey",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color(0xFFFACC15)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Conquer quizzes, decipher listening audio, solve word puzzles, and speak to advance to the Fluent English Castle!",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            lineHeight = 17.sp
                        )
                    }
                    Text("🏰", fontSize = 42.sp)
                }
            }
        }

        items(worlds.sortedBy { it.order }) { world ->
            WorldCardItem(
                world = world,
                totalProgressStars = progress.totalStars,
                onClick = { onSelectWorld(world) }
            )
        }

        item {
            Spacer(Modifier.height(30.dp))
        }
    }
}

@Composable
private fun WorldCardItem(
    world: AdventureWorld,
    totalProgressStars: Int,
    onClick: () -> Unit
) {
    val isUnlocked = world.isUnlocked || totalProgressStars >= world.requiredTotalStars
    val completedLevelsCount = world.levels.count { it.isCompleted }
    val totalLevelsCount = world.levels.size
    val worldStars = world.levels.sumOf { it.starsEarned }
    val maxWorldStars = world.levels.size * 3

    val gradientBrush = Brush.linearGradient(
        listOf(
            Color(world.gradientStartHex),
            Color(world.gradientEndHex)
        )
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF101435),
        border = BorderStroke(
            1.5.dp,
            if (isUnlocked) Brush.linearGradient(listOf(Color(world.themeColorHex), Color(0xFF38BDF8)))
            else Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF1E293B)))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked) { onClick() }
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Color Strip with World Emoji & Stars
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (isUnlocked) gradientBrush else Brush.linearGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A))))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0x33000000),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(world.emoji, fontSize = 24.sp)
                            }
                        }
                        Column {
                            Text(
                                text = "World ${world.order}: ${world.name}",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = world.tagline,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // Unlock Badge or Star Count
                    if (isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x33000000),
                            border = BorderStroke(1.dp, Color(0x55FFFFFF))
                        ) {
                            Text(
                                text = "⭐ $worldStars / $maxWorldStars",
                                color = Color(0xFFFACC15),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0x55000000),
                            border = BorderStroke(1.dp, Color(0xFF64748B))
                        ) {
                            Text(
                                text = "🔒 Need ${world.requiredTotalStars} ⭐",
                                color = Color(0xFFCBD5E1),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Description and Progress bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = world.description,
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 17.sp
                )

                // Level Progress
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isUnlocked) "Levels: $completedLevelsCount / $totalLevelsCount Completed" else "Locked Territory",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${world.levels.size} Interactive Missions",
                        fontSize = 11.sp,
                        color = Color(world.themeColorHex),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Progress Indicator
                val progressFraction = if (totalLevelsCount > 0) completedLevelsCount.toFloat() / totalLevelsCount else 0f
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(world.themeColorHex),
                    trackColor = Color(0xFF1E293B)
                )

                // Action Button
                if (isUnlocked) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(gradientBrush, RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Enter ${world.name}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. LEVEL MAP VIEW (Winding 3D Level Nodes inside World)
// -------------------------------------------------------------
@Composable
private fun AdventureLevelMapView(
    world: AdventureWorld,
    progress: AdventureProgress,
    onBack: () -> Unit,
    onSelectLevel: (AdventureLevel) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Return & World Hero
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFF1E244D), CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${world.emoji} ${world.name}",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Complete missions to earn stars and unlock next realm",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Level Nodes List
        items(world.levels) { level ->
            val isUnlocked = level.isUnlocked || level.levelNumber == 1
            val isCompleted = level.isCompleted

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF11163A),
                border = BorderStroke(
                    1.dp,
                    if (isCompleted) Color(0xFF4ADE80)
                    else if (isUnlocked) Color(world.themeColorHex)
                    else Color(0xFF334155)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = isUnlocked) { onSelectLevel(level) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Level Number Circle with 3D feel
                    Surface(
                        shape = CircleShape,
                        color = if (isCompleted) Color(0xFF166534) else if (isUnlocked) Color(world.themeColorHex) else Color(0xFF1E293B),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isCompleted) {
                                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White)
                            } else if (isUnlocked) {
                                Text(level.iconEmoji, fontSize = 24.sp)
                            } else {
                                Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF94A3B8))
                            }
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                "Level ${level.levelNumber}: ${level.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = level.description,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(6.dp))
                        // Stars Rating
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (i in 1..3) {
                                val filled = i <= level.starsEarned
                                Text(
                                    text = if (filled) "⭐" else "☆",
                                    fontSize = 14.sp,
                                    color = if (filled) Color(0xFFFACC15) else Color(0xFF64748B)
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "+${level.xpReward} XP • +${level.coinsReward} 🪙",
                                fontSize = 11.sp,
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Arrow
                    if (isUnlocked) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = "Start Level",
                            tint = Color(world.themeColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. ADVENTURE MISSION PLAYER (Interactive Quests)
// -------------------------------------------------------------
@Composable
private fun AdventureMissionPlayer(
    world: AdventureWorld,
    level: AdventureLevel,
    mission: AdventureMission,
    viewModel: MainViewModel,
    onClose: () -> Unit,
    onNextMission: () -> Unit
) {
    val speechState by viewModel.speechManager.speechState.collectAsState()
    val isListening = speechState is com.example.service.SpeechState.Listening
    val lastEval by viewModel.lastEvaluation.collectAsState()

    var selectedQuizOption by remember(mission.id) { mutableStateOf<String?>(null) }
    var quizSubmitted by remember(mission.id) { mutableStateOf(false) }

    // Word puzzle & sentence builder states
    var selectedWords by remember(mission.id) { mutableStateOf<List<String>>(emptyList()) }
    var availableWords by remember(mission.id) {
        mutableStateOf(mission.scrambledWords)
    }

    var missionCompletedSuccess by remember(mission.id) { mutableStateOf(false) }
    var earnedStars by remember(mission.id) { mutableIntStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Mission Top Navigation Bar
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF13173D),
                border = BorderStroke(1.dp, Color(0xFF2B3466)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(world.emoji, fontSize = 20.sp)
                        Column {
                            Text(
                                "Level ${level.levelNumber} • ${mission.type.displayName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                            Text(
                                mission.title,
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color(0xFF212850), CircleShape)
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }

        // Mission Prompt Card
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0F1335),
                border = BorderStroke(1.5.dp, Color(world.themeColorHex)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(world.themeColorHex).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(world.themeColorHex))
                    ) {
                        Text(
                            text = "${mission.type.emoji} ${mission.type.displayName}",
                            color = Color(world.themeColorHex),
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = mission.description,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    if (mission.clue.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E2652),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 Clue: ${mission.clue}",
                                color = Color(0xFFFBBF24),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // INTERACTIVE ENGINE PER TYPE
        when (mission.type) {
            // -------------------------------------------------------------
            // TYPE 1: QUIZ
            // -------------------------------------------------------------
            AdventureMissionType.QUIZ -> {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        mission.options.forEach { option ->
                            val isSelected = selectedQuizOption == option
                            val isCorrect = option == mission.correctAnswer

                            val containerColor = when {
                                quizSubmitted && isCorrect -> Color(0xFF166534)
                                quizSubmitted && isSelected && !isCorrect -> Color(0xFF991B1B)
                                isSelected -> Color(0xFF7C3AED)
                                else -> Color(0xFF13173D)
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = containerColor,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected || (quizSubmitted && isCorrect)) Color.White else Color(0xFF2C3565)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !quizSubmitted) {
                                        selectedQuizOption = option
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = option,
                                        color = Color.White,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (quizSubmitted && isCorrect) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF4ADE80))
                                    } else if (quizSubmitted && isSelected && !isCorrect) {
                                        Icon(Icons.Filled.Cancel, contentDescription = null, tint = Color(0xFFF87171))
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        if (!quizSubmitted) {
                            Button(
                                onClick = {
                                    quizSubmitted = true
                                    val isRight = selectedQuizOption == mission.correctAnswer
                                    val score = if (isRight) 100 else 40
                                    val result = viewModel.completeAdventureMission(world.id, level.id, mission.id, score)
                                    earnedStars = result.first
                                    missionCompletedSuccess = true
                                },
                                enabled = selectedQuizOption != null,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                            ) {
                                Text("Submit Answer", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        } else {
                            if (mission.explanation.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF141A40),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "📖 Explanation: ${mission.explanation}",
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // TYPE 2: WORD PUZZLE & TYPE 3: SENTENCE BUILDER
            // -------------------------------------------------------------
            AdventureMissionType.WORD_PUZZLE,
            AdventureMissionType.SENTENCE_BUILDER -> {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Word Target Tray
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF0B0E2A),
                            border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Tap chips below to assemble in order:",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )

                                if (selectedWords.isEmpty()) {
                                    Text(
                                        "--- [ Tap items below to place here ] ---",
                                        color = Color(0xFF64748B),
                                        fontSize = 13.sp,
                                        modifier = Modifier.padding(vertical = 12.dp)
                                    )
                                } else {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        selectedWords.forEachIndexed { idx, word ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFF7C3AED),
                                                modifier = Modifier
                                                    .padding(4.dp)
                                                    .clickable {
                                                        // Return to available words
                                                        selectedWords = selectedWords.filterIndexed { i, _ -> i != idx }
                                                        availableWords = availableWords + word
                                                    }
                                            ) {
                                                Text(
                                                    text = word,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Available Word Chips
                        Text("Available Word Pieces:", fontSize = 12.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Bold)

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            availableWords.forEachIndexed { idx, word ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF1E2652),
                                    border = BorderStroke(1.dp, Color(0xFF3B487A)),
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clickable {
                                            selectedWords = selectedWords + word
                                            availableWords = availableWords.filterIndexed { i, _ -> i != idx }
                                        }
                                ) {
                                    Text(
                                        text = word,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        // Actions: Reset & Verify
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    selectedWords = emptyList()
                                    availableWords = mission.scrambledWords
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset")
                            }

                            Button(
                                onClick = {
                                    val constructed = if (mission.type == AdventureMissionType.WORD_PUZZLE) {
                                        selectedWords.joinToString("")
                                    } else {
                                        selectedWords.joinToString(" ")
                                    }

                                    val isMatch = constructed.trim().equals(mission.correctAnswer.trim(), ignoreCase = true)
                                    val score = if (isMatch) 100 else 50
                                    val result = viewModel.completeAdventureMission(world.id, level.id, mission.id, score)
                                    earnedStars = result.first
                                    missionCompletedSuccess = true
                                },
                                enabled = selectedWords.isNotEmpty(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text("Verify & Claim", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // TYPE 4: LISTENING CHALLENGE
            // -------------------------------------------------------------
            AdventureMissionType.LISTENING_CHALLENGE -> {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Native Audio Speaker Trigger
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF151B44),
                            border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Rounded.VolumeUp, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(36.dp))
                                Text("Listen to Native Speaker Audio", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                                Button(
                                    onClick = { viewModel.speakAudio(mission.audioPrompt) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                                ) {
                                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                                    Spacer(Modifier.width(6.dp))
                                    Text("Play Audio Stream")
                                }
                            }
                        }

                        // Answer Options
                        mission.options.forEach { option ->
                            val isSelected = selectedQuizOption == option
                            val isCorrect = option == mission.correctAnswer

                            val bg = when {
                                quizSubmitted && isCorrect -> Color(0xFF166534)
                                quizSubmitted && isSelected && !isCorrect -> Color(0xFF991B1B)
                                isSelected -> Color(0xFF0284C7)
                                else -> Color(0xFF13173D)
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = bg,
                                border = BorderStroke(1.dp, if (isSelected) Color.White else Color(0xFF2A3462)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = !quizSubmitted) {
                                        selectedQuizOption = option
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(option, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                    if (quizSubmitted && isCorrect) {
                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF4ADE80))
                                    }
                                }
                            }
                        }

                        if (!quizSubmitted) {
                            Button(
                                onClick = {
                                    quizSubmitted = true
                                    val isRight = selectedQuizOption == mission.correctAnswer
                                    val score = if (isRight) 100 else 40
                                    val result = viewModel.completeAdventureMission(world.id, level.id, mission.id, score)
                                    earnedStars = result.first
                                    missionCompletedSuccess = true
                                },
                                enabled = selectedQuizOption != null,
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text("Confirm Answer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // -------------------------------------------------------------
            // TYPE 5: AI-POWERED SPEAKING CHALLENGE
            // -------------------------------------------------------------
            AdventureMissionType.SPEAKING_CHALLENGE -> {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Spoken Target Sentence
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color(0xFF11173F),
                            border = BorderStroke(1.5.dp, Color(0xFFA855F7)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("Speak This Sentence Loud & Clear:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "\"${mission.targetSentence}\"",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(Modifier.height(4.dp))

                                OutlinedButton(
                                    onClick = { viewModel.speakAudio(mission.audioPrompt) },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Listen to Native Pronunciation", fontSize = 12.sp)
                                }
                            }
                        }

                        // Microphone Orb
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0C1033),
                            border = BorderStroke(
                                3.dp,
                                Brush.sweepGradient(listOf(Color(0xFF00E5FF), Color(0xFF7C3AED), Color(0xFFEC4899), Color(0xFF00E5FF)))
                            ),
                            modifier = Modifier
                                .size(100.dp)
                                .clickable {
                                    if (isListening) viewModel.stopSpeakingPractice()
                                    else viewModel.startSpeakingPractice()
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                                    contentDescription = "Mic",
                                    tint = Color.White,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isListening) "Listening... Speak Now!" else "Tap Mic to Speak",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        // Sample voice button
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1A1F45),
                            border = BorderStroke(1.dp, Color(0xFF3B487A)),
                            modifier = Modifier.clickable {
                                viewModel.simulateVoiceInput(mission.targetSentence)
                            }
                        ) {
                            Text(
                                "🔊 Test with Sample Voice",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }

                        // Speech Evaluation Feedback
                        lastEval?.let { eval ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (eval.pronunciationScore >= 75) Color(0xFF14532D) else Color(0xFF78350F),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        "Pronunciation Score: ${eval.pronunciationScore}%",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        eval.feedback,
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    val result = viewModel.completeAdventureMission(world.id, level.id, mission.id, eval.pronunciationScore)
                                    earnedStars = result.first
                                    missionCompletedSuccess = true
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                                modifier = Modifier.fillMaxWidth().height(48.dp)
                            ) {
                                Text("Claim Spoken Reward ⭐", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // VICTORY CELEBRATION BOX
        if (missionCompletedSuccess) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF0F2E1E),
                    border = BorderStroke(1.5.dp, Color(0xFF4ADE80)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎉 Mission Conquered!", fontWeight = FontWeight.Black, fontSize = 18.sp, color = Color(0xFF4ADE80))
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            for (i in 1..earnedStars) {
                                Text("⭐", fontSize = 24.sp)
                            }
                        }
                        Text("+${mission.xp} XP • +${mission.coins} Coins Credited!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                        Spacer(Modifier.height(4.dp))

                        Button(
                            onClick = onNextMission,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next Quest ➡️", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. LEADERBOARD VIEW
// -------------------------------------------------------------
@Composable
private fun AdventureLeaderboardView(
    leaderboard: List<AdventureLeaderboardEntry>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF141944),
                border = BorderStroke(1.dp, Color(0xFF3B487A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🏆", fontSize = 32.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Adventure Hall of Fame",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            "Top adventurers ranked by stars, coins and XP",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        items(leaderboard) { entry ->
            val rankIcon = when (entry.rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "#${entry.rank}"
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (entry.isCurrentUser) Color(0xFF261858) else Color(0xFF111538),
                border = BorderStroke(
                    1.dp,
                    if (entry.isCurrentUser) Color(0xFFA855F7) else Color(0xFF242C5B)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rankIcon,
                        fontSize = if (entry.rank <= 3) 22.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.width(36.dp)
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF1E2652),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(entry.avatar, fontSize = 20.sp)
                        }
                    }

                    Spacer(Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = entry.studentName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            if (entry.isCurrentUser) {
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF7C3AED)
                                ) {
                                    Text(
                                        "YOU",
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${entry.className} • ${entry.currentWorld}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "⭐ ${entry.totalStars}",
                            color = Color(0xFFFACC15),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${entry.xp} XP",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. BADGES & REWARDS VIEW
// -------------------------------------------------------------
@Composable
private fun AdventureBadgesView(
    badges: List<AdventureBadge>,
    totalStars: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF141A40),
                border = BorderStroke(1.dp, Color(0xFF3B487A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🎖️", fontSize = 32.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Adventure Trophy Cabinet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Text(
                            "Earn badges by conquering all 5 worlds and collecting stars",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }

        items(badges) { badge ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = if (badge.isUnlocked) Color(0xFF121B40) else Color(0xFF0F1228),
                border = BorderStroke(
                    1.dp,
                    if (badge.isUnlocked) Color(0xFFFACC15) else Color(0xFF283058)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (badge.isUnlocked) Color(0xFF261D52) else Color(0xFF181C38),
                        border = BorderStroke(
                            1.dp,
                            if (badge.isUnlocked) Color(0xFFFACC15) else Color(0xFF3B487A)
                        ),
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = badge.emoji,
                                fontSize = 26.sp,
                                modifier = Modifier.then(if (!badge.isUnlocked) Modifier else Modifier)
                            )
                        }
                    }

                    Spacer(Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = badge.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            if (badge.isUnlocked) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF166534)
                                ) {
                                    Text(
                                        "UNLOCKED",
                                        color = Color(0xFF4ADE80),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = badge.worldName,
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = badge.description,
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }
        }
    }
}
