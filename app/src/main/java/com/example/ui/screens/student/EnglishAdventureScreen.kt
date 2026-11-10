@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens.student

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.*
import com.example.data.repository.AdventureDataDefaults
import com.example.ui.MainViewModel
import com.example.ui.SpeakingSubTab
import com.example.ui.theme.*

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

    // Default to World 1 (Vocabulary Island) if none selected
    val currentWorld = selectedWorld ?: worlds.firstOrNull { it.id == "world_1" } ?: worlds.firstOrNull() ?: AdventureDataDefaults.createDefaultWorlds().first()

    // Modal dialog controls
    var showWorldMapDialog by remember { mutableStateOf(false) }
    var showLeaderboardDialog by remember { mutableStateOf(false) }
    var showBadgesDialog by remember { mutableStateOf(false) }
    var showWorldInfoDialog by remember { mutableStateOf(false) }
    var showChestRewardDialog by remember { mutableStateOf(false) }
    var lockedSnackbarMsg by remember { mutableStateOf<String?>(null) }

    BackHandler {
        if (activeMission != null) {
            viewModel.exitAdventureMission()
        } else if (showWorldMapDialog || showLeaderboardDialog || showBadgesDialog || showWorldInfoDialog || showChestRewardDialog) {
            showWorldMapDialog = false
            showLeaderboardDialog = false
            showBadgesDialog = false
            showWorldInfoDialog = false
            showChestRewardDialog = false
        } else {
            viewModel.selectSpeakingSubTab(SpeakingSubTab.AI_PARTNER)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("english_adventure_screen")
    ) {
        // If an active quest is running, show the Interactive Mission Player
        if (activeMission != null && activeLevel != null && selectedWorld != null) {
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
        } else {
            // MAIN SCREEN MATCHING SCREENSHOT EXACTLY
            AdventureMainView(
                currentWorld = currentWorld,
                progress = progress,
                onBack = { viewModel.selectSpeakingSubTab(SpeakingSubTab.AI_PARTNER) },
                onOpenWorldMap = { showWorldMapDialog = true },
                onOpenLeaderboard = { showLeaderboardDialog = true },
                onOpenBadges = { showBadgesDialog = true },
                onOpenWorldInfo = { showWorldInfoDialog = true },
                onOpenChest = { showChestRewardDialog = true },
                onLaunchLevel = { level ->
                    if (level.isUnlocked || level.levelNumber == 1) {
                        viewModel.selectAdventureWorld(currentWorld)
                        viewModel.selectAdventureLevel(level)
                        level.missions.firstOrNull()?.let { viewModel.startAdventureMission(it) }
                    } else {
                        lockedSnackbarMsg = "Complete Level ${level.levelNumber - 1} to unlock ${level.title}!"
                    }
                }
            )
        }

        // World Map Selection Dialog
        if (showWorldMapDialog) {
            Dialog(
                onDismissRequest = { showWorldMapDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(2.dp, Color(0xFFD97706)),
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .fillMaxHeight(0.85f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🗺️", fontSize = 24.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "English Adventure Worlds",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = { showWorldMapDialog = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(worlds.sortedBy { it.order }) { world ->
                                WorldCardItem(
                                    world = world,
                                    totalProgressStars = progress.totalStars,
                                    onClick = {
                                        viewModel.selectAdventureWorld(world)
                                        showWorldMapDialog = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Leaderboard Dialog
        if (showLeaderboardDialog) {
            Dialog(
                onDismissRequest = { showLeaderboardDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(2.dp, Color(0xFFA855F7)),
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .fillMaxHeight(0.85f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🏆", fontSize = 24.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Adventure Leaderboard",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = { showLeaderboardDialog = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            AdventureLeaderboardView(leaderboard = leaderboard)
                        }
                    }
                }
            }
        }

        // Badges & Rewards Dialog
        if (showBadgesDialog) {
            Dialog(
                onDismissRequest = { showBadgesDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(2.dp, Color(0xFFF59E0B)),
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .fillMaxHeight(0.85f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎖️", fontSize = 24.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Badges & Trophies",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                            IconButton(onClick = { showBadgesDialog = false }) {
                                Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.White)
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            AdventureBadgesView(badges = badges, totalStars = progress.totalStars)
                        }
                    }
                }
            }
        }

        // World Info Dialog
        if (showWorldInfoDialog) {
            AlertDialog(
                onDismissRequest = { showWorldInfoDialog = false },
                icon = { Text(currentWorld.emoji, fontSize = 36.sp) },
                title = {
                    Text(
                        currentWorld.name,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            currentWorld.description,
                            color = Color(0xFFCBD5E1),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                        HorizontalDivider(color = Color(0xFF334155))
                        Text(
                            "🎯 Realm Goal:",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            fontSize = 13.sp
                        )
                        Text(
                            "Master all levels to earn 9 stars, win exclusive badges, and unlock the next gaming realm!",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showWorldInfoDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                    ) {
                        Text("Ready to Explore!")
                    }
                },
                containerColor = Color(0xFF1E293B)
            )
        }

        // Chest Loot Reward Dialog
        if (showChestRewardDialog) {
            AlertDialog(
                onDismissRequest = { showChestRewardDialog = false },
                icon = {
                    Image(
                        painter = painterResource(id = R.drawable.ic_adventure_chest),
                        contentDescription = "Chest",
                        modifier = Modifier.size(80.dp)
                    )
                },
                title = {
                    Text(
                        "🌴 Island Treasure Unlocked!",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFACC15),
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "You discovered hidden captain's loot buried on the beach sands!",
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🪙 +25 Coins", fontWeight = FontWeight.Bold, color = Color(0xFFFACC15), fontSize = 13.sp)
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("⚡ +50 XP", fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8), fontSize = 13.sp)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showChestRewardDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                    ) {
                        Text("Claim Loot!")
                    }
                },
                containerColor = Color(0xFF0F172A)
            )
        }

        // Locked Level Toast Snackbar
        lockedSnackbarMsg?.let { msg ->
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(2600)
                lockedSnackbarMsg = null
            }
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xEE1E293B),
                border = BorderStroke(1.dp, Color(0xFFE11D48)),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 70.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🔒", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(msg, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Main 3D Adventure Screen matching file_0000000088b48208b8c63b7a5b0feb87_cf80ca8f.png exactly
 */
@Composable
private fun AdventureMainView(
    currentWorld: AdventureWorld,
    progress: AdventureProgress,
    onBack: () -> Unit,
    onOpenWorldMap: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenBadges: () -> Unit,
    onOpenWorldInfo: () -> Unit,
    onOpenChest: () -> Unit,
    onLaunchLevel: (AdventureLevel) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. FULL BACKGROUND IMAGE (Tropical Island, Lagoon, Palms, Beach)
        Image(
            painter = painterResource(id = R.drawable.bg_adventure_beach_full),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. BACK BUTTON (Top-Left)
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(start = 12.dp, top = 8.dp)
                .size(40.dp)
                .background(Color(0xAA0A1128), CircleShape)
                .border(1.2.dp, Color(0x66FFFFFF), CircleShape)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }

        // 3. SCROLLABLE ADVENTURE CONTENT
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // A. HERO SIGNBOARD (English Adventure with Parrot, Compasses, Wood banner)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_adventure_banner),
                        contentDescription = "English Adventure - Explore Learn Play Win",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(125.dp)
                    )
                }
            }

            // B. RESOURCE STAT CAPSULES: ⭐ 3 | 🪙 60 | ⚡ 120
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Stars Capsule
                        AdventureStatCapsule(
                            icon = "⭐",
                            value = "${progress.totalStars.coerceAtLeast(3)}",
                            iconColor = Color(0xFFFACC15)
                        )
                        // Coins Capsule
                        AdventureStatCapsule(
                            icon = "🪙",
                            value = "${progress.totalCoins.coerceAtLeast(60)}",
                            iconColor = Color(0xFFF59E0B)
                        )
                        // Energy / XP Capsule
                        AdventureStatCapsule(
                            icon = "⚡",
                            value = "${progress.totalXp.coerceAtLeast(120)}",
                            iconColor = Color(0xFF38BDF8)
                        )
                    }
                }
            }

            // C. THREE WOOD / BAMBOO ACTION BUTTONS: [ 🗺️ World Map ] [ 🏆 Leaderboard ] [ 🎖️ Badges & Rewards ]
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // World Map Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0C1736),
                        border = BorderStroke(
                            2.dp,
                            Brush.linearGradient(
                                listOf(Color(0xFFD97706), Color(0xFF78350F), Color(0xFFD97706))
                            )
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clickable { onOpenWorldMap() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🗺️", fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "World Map",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    // Leaderboard Button (Selected style with rich purple gradient & golden frame)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        border = BorderStroke(
                            2.dp,
                            Brush.linearGradient(
                                listOf(Color(0xFFFDE047), Color(0xFFCA8A04), Color(0xFFFDE047))
                            )
                        ),
                        modifier = Modifier
                            .weight(1.15f)
                            .height(44.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFFA855F7))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6D28D9), Color(0xFF9333EA), Color(0xFFA855F7))
                                ),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable { onOpenLeaderboard() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🏆", fontSize = 14.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Leaderboard",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Badges & Rewards Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF0C1736),
                        border = BorderStroke(
                            2.dp,
                            Brush.linearGradient(
                                listOf(Color(0xFFD97706), Color(0xFF78350F), Color(0xFFD97706))
                            )
                        ),
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .clickable { onOpenBadges() }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("🎖️", fontSize = 14.sp)
                            Spacer(Modifier.width(3.dp))
                            Text(
                                "Badges & Rewards",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.5.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // D. PARCHMENT SCROLL BANNER (🌴 Vocabulary Island + ( i ) Info Button)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Scroll Box
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_parchment_scroll),
                                contentDescription = null,
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier.fillMaxSize()
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(horizontal = 28.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = currentWorld.emoji,
                                    fontSize = 19.sp
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = currentWorld.name,
                                    color = Color(0xFF381403),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily(Font(R.font.cinzel, FontWeight.Bold)),
                                    fontSize = 16.5.sp,
                                    letterSpacing = 0.5.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Blue ( i ) Info Circle Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2563EB),
                            border = BorderStroke(1.5.dp, Color(0xFF60A5FA)),
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { onOpenWorldInfo() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "i",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 17.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0x77000000),
                        border = BorderStroke(0.8.dp, Color(0x33FFFFFF)),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "Complete missions to earn stars and unlock next realm",
                            color = Color(0xFFE2E8F0),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.5.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // E. LEVEL 1: Tropical Bay Market
            item {
                val level1 = currentWorld.levels.getOrNull(0) ?: AdventureLevel(
                    id = "lvl_1_1",
                    worldId = currentWorld.id,
                    levelNumber = 1,
                    title = "Tropical Bay Market",
                    description = "Identify exotic island goods, spices, and vibrant descriptive adjectives.",
                    iconEmoji = "🥥",
                    starsEarned = 3,
                    isUnlocked = true,
                    isCompleted = true,
                    xpReward = 60,
                    coinsReward = 25,
                    missions = emptyList()
                )

                AdventureLevelCardExact(
                    levelNumber = 1,
                    title = "Level 1: Tropical Bay Market",
                    description = "Identify exotic island goods, spices, and vibrant descriptive adjectives.",
                    drawableRes = R.drawable.ic_level1_market,
                    hasCheckmark = true,
                    isLocked = false,
                    starsEarned = 3,
                    xp = 60,
                    coins = 25,
                    isGlowingBorder = true,
                    onClick = { onLaunchLevel(level1) }
                )
            }

            // F. LEVEL 2: Coral Reef Sanctuary
            item {
                val level2 = currentWorld.levels.getOrNull(1) ?: AdventureLevel(
                    id = "lvl_1_2",
                    worldId = currentWorld.id,
                    levelNumber = 2,
                    title = "Coral Reef Sanctuary",
                    description = "Explore maritime vocabulary, sea creatures, and navigation phrases.",
                    iconEmoji = "🐠",
                    starsEarned = 0,
                    isUnlocked = false,
                    isCompleted = false,
                    xpReward = 80,
                    coinsReward = 35,
                    missions = emptyList()
                )

                AdventureLevelCardExact(
                    levelNumber = 2,
                    title = "Level 2: Coral Reef Sanctuary",
                    description = "Explore maritime vocabulary, sea creatures, and navigation phrases.",
                    drawableRes = R.drawable.ic_level2_reef,
                    hasCheckmark = false,
                    isLocked = true,
                    starsEarned = 0,
                    xp = 80,
                    coins = 35,
                    isGlowingBorder = false,
                    onClick = { onLaunchLevel(level2) }
                )
            }

            // G. LEVEL 3: The Pirate Captain's Vault
            item {
                val level3 = currentWorld.levels.getOrNull(2) ?: AdventureLevel(
                    id = "lvl_1_3",
                    worldId = currentWorld.id,
                    levelNumber = 3,
                    title = "The Pirate Captain's Vault",
                    description = "Discover treasure words, action verbs, and adventure expressions.",
                    iconEmoji = "🏴‍☠️",
                    starsEarned = 0,
                    isUnlocked = false,
                    isCompleted = false,
                    xpReward = 100,
                    coinsReward = 50,
                    missions = emptyList()
                )

                AdventureLevelCardExact(
                    levelNumber = 3,
                    title = "Level 3: The Pirate Captain's Vault",
                    description = "Discover treasure words, action verbs, and adventure expressions.",
                    drawableRes = R.drawable.ic_level3_pirate,
                    hasCheckmark = false,
                    isLocked = true,
                    starsEarned = 0,
                    xp = 100,
                    coins = 50,
                    isGlowingBorder = false,
                    onClick = { onLaunchLevel(level3) }
                )
            }

            // H. BOTTOM OPEN GOLDEN TREASURE CHEST
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp, bottom = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { onOpenChest() }
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_adventure_chest),
                            contentDescription = "Golden Treasure Chest with Gems",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(width = 175.dp, height = 115.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x99000000),
                            border = BorderStroke(1.dp, Color(0xFFFBBF24)),
                            shadowElevation = 4.dp
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text("✨", fontSize = 12.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "Tap to Claim Island Rewards",
                                    color = Color(0xFFFDE047),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Capsule Pill for Star, Coin, Energy / XP counter
 */
@Composable
private fun AdventureStatCapsule(
    icon: String,
    value: String,
    iconColor: Color
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF081433),
        border = BorderStroke(1.5.dp, Color(0xFF1E3A8A)),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 15.sp)
            Text(
                text = value,
                color = Color.White,
                fontWeight = FontWeight.Black,
                fontSize = 14.sp
            )
        }
    }
}

/**
 * Level Card matching file_0000000088b48208b8c63b7a5b0feb87_cf80ca8f.png exactly:
 * - Rounded dark navy surface
 * - Glowing cyan/emerald border on active level 1
 * - Thumbnail with checkmark badge or padlock
 * - XP and Coin capsules
 * - Glossy green 3D play button or slate metallic lock button
 */
@Composable
private fun AdventureLevelCardExact(
    levelNumber: Int,
    title: String,
    description: String,
    drawableRes: Int,
    hasCheckmark: Boolean,
    isLocked: Boolean,
    starsEarned: Int,
    xp: Int,
    coins: Int,
    isGlowingBorder: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFF07112C),
        border = BorderStroke(
            if (isGlowingBorder) 2.dp else 1.5.dp,
            if (isGlowingBorder) Brush.linearGradient(listOf(Color(0xFF06B6D4), Color(0xFF10B981)))
            else Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF172554)))
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .then(
                if (isGlowingBorder) Modifier.shadow(8.dp, RoundedCornerShape(22.dp), spotColor = Color(0xFF06B6D4))
                else Modifier
            )
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT THUMBNAIL
            Box(
                modifier = Modifier
                    .size(width = 84.dp, height = 66.dp)
            ) {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                )

                if (hasCheckmark) {
                    // Green checkmark badge overlapping right edge
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF22C55E),
                        border = BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.CenterEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Completed",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                if (isLocked) {
                    // Center padlock on thumbnail
                    Surface(
                        shape = CircleShape,
                        color = Color(0x990A1128),
                        border = BorderStroke(1.dp, Color(0xFF94A3B8)),
                        modifier = Modifier
                            .size(26.dp)
                            .align(Alignment.Center)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = "Locked",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            // CENTER TEXT DETAILS
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color.White,
                    maxLines = 2,
                    lineHeight = 16.sp,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = description,
                    fontSize = 10.5.sp,
                    color = Color(0xFF94A3B8),
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(3.dp))

                // Bottom row: Stars + XP pill + Coins pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Stars
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        for (i in 1..3) {
                            val filled = i <= starsEarned
                            Text(
                                text = if (filled) "⭐" else "☆",
                                fontSize = 11.sp,
                                color = if (filled) Color(0xFFFACC15) else Color(0xFF64748B)
                            )
                        }
                    }

                    // XP Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0C223E),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚡", fontSize = 9.sp)
                            Spacer(Modifier.width(2.dp))
                            Text(
                                "+$xp XP",
                                color = Color(0xFF38BDF8),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Coins Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF261D0B),
                        border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🪙", fontSize = 9.sp)
                            Spacer(Modifier.width(2.dp))
                            Text(
                                "+$coins",
                                color = Color(0xFFFACC15),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // RIGHT ACTION BUTTON
            if (!isLocked) {
                // Bright 3D glossy neon green play button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                    modifier = Modifier
                        .size(width = 52.dp, height = 46.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp), spotColor = Color(0xFF22C55E))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF4ADE80), Color(0xFF16A34A), Color(0xFF15803D))
                            ),
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.PlayArrow,
                            contentDescription = "Play Level",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            } else {
                // Slate metallic lock button
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.5.dp, Color(0xFF475569)),
                    modifier = Modifier.size(width = 52.dp, height = 46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
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
