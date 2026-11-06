package com.example.ui.screens.student

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.LessonStage
import com.example.data.model.StageType
import com.example.data.model.World
import com.example.ui.MainViewModel
import com.example.ui.StudentTab
import com.example.ui.theme.*

@Composable
fun LearningJourneyScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val worlds by viewModel.worlds.collectAsState()
    val stages by viewModel.stages.collectAsState()
    var selectedWorldId by remember { mutableIntStateOf(1) }

    val activeWorld = worlds.find { it.id == selectedWorldId } ?: worlds.first()
    val currentStages = stages.filter { it.worldId == selectedWorldId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("learning_journey_screen"),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Whimsical Learning Map Banner
        item {
            Card(
                shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_learning_map_banner),
                        contentDescription = "Whimsical Learning World Map",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color.Transparent, DeepNavy.copy(alpha = 0.85f))
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "${activeWorld.iconEmoji} ${activeWorld.name}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        Text(
                            text = activeWorld.description,
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }
        }

        // 2. World Carousel Tabs
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(worlds) { world ->
                    val isSelected = world.id == selectedWorldId
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(world.themeColorHex) else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable {
                                if (world.isUnlocked) selectedWorldId = world.id
                            }
                            .testTag("world_pill_${world.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(world.iconEmoji, fontSize = 16.sp)
                            Text(
                                text = "World ${world.id}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            if (!world.isUnlocked) {
                                Icon(
                                    Icons.Filled.Lock,
                                    contentDescription = "Locked",
                                    tint = MutedGray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Connected Stage Adventure Path
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                currentStages.forEachIndexed { index, stage ->
                    StagePathNode(
                        stage = stage,
                        isLast = index == currentStages.size - 1,
                        onStageClick = {
                            if (stage.isUnlocked) {
                                // Launch practice & reward
                                viewModel.completeStage(stage.id, 3, stage.xpReward, stage.coinReward)
                                viewModel.speakAudio("Starting ${stage.title}. ${stage.topicDescription}")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StagePathNode(
    stage: LessonStage,
    isLast: Boolean,
    onStageClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Stage Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    stage.isCompleted -> Color(0xFFF0FDF4)
                    stage.isUnlocked -> MaterialTheme.colorScheme.surface
                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ),
            border = if (stage.isCompleted)
                androidx.compose.foundation.BorderStroke(2.dp, EmeraldSuccess)
            else if (stage.isUnlocked)
                androidx.compose.foundation.BorderStroke(1.5.dp, RoyalBlue)
            else null,
            elevation = CardDefaults.cardElevation(defaultElevation = if (stage.isUnlocked) 3.dp else 0.dp),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clickable(enabled = stage.isUnlocked) { onStageClick() }
                .testTag("stage_card_${stage.id}")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Stage Number & Icon Circle
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                stage.isCompleted -> EmeraldSuccess
                                stage.isUnlocked -> RoyalBlue
                                else -> MutedGray
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (stage.isCompleted) {
                        Icon(Icons.Filled.Check, contentDescription = "Completed", tint = Color.White, modifier = Modifier.size(28.dp))
                    } else if (stage.isUnlocked) {
                        Text("${stage.stageNumber}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = Color.White)
                    } else {
                        Icon(Icons.Filled.Lock, contentDescription = "Locked", tint = Color.White, modifier = Modifier.size(22.dp))
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (stage.type) {
                                StageType.SPEAKING -> Color(0xFFEFF6FF)
                                StageType.VOCABULARY -> Color(0xFFFDF4FF)
                                StageType.GRAMMAR -> Color(0xFFECFDF5)
                                StageType.PRONUNCIATION -> Color(0xFFFFFBEB)
                                else -> Color(0xFFF1F5F9)
                            }
                        ) {
                            Text(
                                text = stage.type.name,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (stage.type) {
                                    StageType.SPEAKING -> RoyalBlue
                                    StageType.VOCABULARY -> PurpleDark
                                    StageType.GRAMMAR -> EmeraldSuccess
                                    StageType.PRONUNCIATION -> AmberGold
                                    else -> DeepNavy
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        // Star rating
                        if (stage.isCompleted) {
                            Text("⭐".repeat(stage.stars.coerceAtLeast(1)), fontSize = 12.sp)
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = stage.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stage.topicDescription,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Reward badges
                Column(horizontalAlignment = Alignment.End) {
                    Text("+${stage.xpReward} XP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    Text("+${stage.coinReward} 🪙", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                }
            }
        }

        // Connecting Pathway line
        if (!isLast) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .background(
                        if (stage.isCompleted) EmeraldSuccess else MaterialTheme.colorScheme.outline
                    )
            )
        }
    }
}
