@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens.student

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
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
import com.example.data.model.VocabularyActivityType
import com.example.data.model.VocabularyChallengeProgress
import com.example.data.model.VocabularyModeConfig
import com.example.data.model.WordMatchChallenge
import com.example.ui.MainViewModel
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.EmeraldSuccess

private data class ActivityPalette(
    val borderColor: Color,
    val badgeBg: Color,
    val buttonGradient: List<Color>,
    val cardGradient: List<Color>,
    val accentText: Color
)

private fun paletteForActivity(type: VocabularyActivityType): ActivityPalette = when (type) {
    VocabularyActivityType.PICTURE_MATCHING -> ActivityPalette(
        borderColor = Color(0xFF38BDF8),
        badgeBg = Color(0xFF0369A1),
        buttonGradient = listOf(Color(0xFF00D2FF), Color(0xFF0284C7)),
        cardGradient = listOf(Color(0xFF0E295E), Color(0xFF091B42)),
        accentText = Color(0xFF7DD3FC)
    )
    VocabularyActivityType.MEANING_MATCHING -> ActivityPalette(
        borderColor = Color(0xFFC084FC),
        badgeBg = Color(0xFF6D28D9),
        buttonGradient = listOf(Color(0xFFA855F7), Color(0xFF7C3AED)),
        cardGradient = listOf(Color(0xFF281259), Color(0xFF180A3A)),
        accentText = Color(0xFFE9D5FF)
    )
    VocabularyActivityType.SYNONYMS -> ActivityPalette(
        borderColor = Color(0xFF34D399),
        badgeBg = Color(0xFF047857),
        buttonGradient = listOf(Color(0xFF10B981), Color(0xFF059669)),
        cardGradient = listOf(Color(0xFF083D32), Color(0xFF06251F)),
        accentText = Color(0xFF6EE7B7)
    )
    VocabularyActivityType.OPPOSITE_WORDS -> ActivityPalette(
        borderColor = Color(0xFFFBBF24),
        badgeBg = Color(0xFFB45309),
        buttonGradient = listOf(Color(0xFFF59E0B), Color(0xFFEA580C)),
        cardGradient = listOf(Color(0xFF472109), Color(0xFF2B1304)),
        accentText = Color(0xFFFDE68A)
    )
    VocabularyActivityType.WORD_SCRAMBLE -> ActivityPalette(
        borderColor = Color(0xFFF472B6),
        badgeBg = Color(0xFFBE185D),
        buttonGradient = listOf(Color(0xFFEC4899), Color(0xFFDB2777)),
        cardGradient = listOf(Color(0xFF4C0D38), Color(0xFF2C0721)),
        accentText = Color(0xFFFBCFE8)
    )
}

@Composable
fun VocabularyChallengeGame(
    viewModel: MainViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val challenges by viewModel.wordMatchChallenges.collectAsState()
    val modeConfigs by viewModel.vocabularyModeConfigs.collectAsState()
    val progress by viewModel.vocabularyChallengeProgress.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()

    var activeMode by remember { mutableStateOf<VocabularyModeConfig?>(null) }

    BackHandler {
        if (activeMode != null) {
            activeMode = null
        } else {
            onClose()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0A0622),
                        Color(0xFF160B3B),
                        Color(0xFF1B0E47),
                        Color(0xFF09051C)
                    )
                )
            )
            .testTag("vocabulary_challenge_root")
    ) {
        val selected = activeMode
        if (selected == null) {
            VocabularyChallengeSelectionView(
                xpPoints = studentProfile.xpPoints,
                modeConfigs = modeConfigs,
                allChallenges = challenges,
                progress = progress,
                onBack = onClose,
                onSelectMode = { config -> activeMode = config }
            )
        } else {
            val modeItems = remember(challenges, selected.activityType) {
                val filtered = challenges.filter {
                    VocabularyActivityType.fromName(it.activityType) == selected.activityType
                }
                if (filtered.isNotEmpty()) filtered else challenges
            }
            VocabularyActivityPlayView(
                config = selected,
                items = modeItems,
                allChallenges = challenges,
                onSpeak = { viewModel.speakAudio(it) },
                onBackToModes = { activeMode = null },
                onRoundFinished = { correct, total, xpAwarded ->
                    viewModel.recordVocabularyChallengeResult(
                        activityType = selected.activityType,
                        correctCount = correct,
                        totalCount = total,
                        xpAwarded = xpAwarded
                    )
                }
            )
        }
    }
}

@Composable
private fun VocabularyChallengeSelectionView(
    xpPoints: Int,
    modeConfigs: List<VocabularyModeConfig>,
    allChallenges: List<WordMatchChallenge>,
    progress: VocabularyChallengeProgress,
    onBack: () -> Unit,
    onSelectMode: (VocabularyModeConfig) -> Unit
) {
    val enabledConfigs = remember(modeConfigs) { modeConfigs.filter { it.isEnabled } }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1B1345),
                    border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.7f)),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onBack() }
                        .testTag("vocab_challenge_back_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to Arcade",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "🧩 Vocabulary Challenge",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "5 Interactive Word Mastery Modes",
                        fontSize = 11.5.sp,
                        color = Color(0xFF93C5FD),
                        maxLines = 1
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E1348),
                border = BorderStroke(1.2.dp, Color(0xFFFACC15).copy(alpha = 0.75f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("⭐", fontSize = 13.sp)
                    Text(
                        text = "$xpPoints XP",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFFFDE047)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("vocab_challenge_modes_list"),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cosmic Hero Banner
            item {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(
                        1.4.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFF38BDF8), Color(0xFFA855F7), Color(0xFFEC4899))
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF1E1250), Color(0xFF110B33))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Choose Your Vocabulary Activity",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Build mastery through Picture Matching, Meanings, Synonyms, Opposite Words & Word Scramble.",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            if (enabledConfigs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF151236)),
                        border = BorderStroke(1.dp, Color(0xFF475569)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🧩", fontSize = 36.sp)
                            Text(
                                text = "No activities are enabled right now",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Ask your teacher to enable Vocabulary Challenge activities in the Teacher Panel.",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(enabledConfigs, key = { _, cfg -> cfg.activityType.name }) { _, cfg ->
                    val palette = paletteForActivity(cfg.activityType)
                    val modeCount = remember(allChallenges, cfg.activityType) {
                        allChallenges.count {
                            VocabularyActivityType.fromName(it.activityType) == cfg.activityType
                        }
                    }
                    val isCompleted = cfg.activityType.name in progress.completedModes

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.4.dp, palette.borderColor.copy(alpha = 0.85f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = modeCount > 0 || allChallenges.isNotEmpty()) {
                                onSelectMode(cfg)
                            }
                            .testTag("vocab_mode_card_${cfg.activityType.name.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.horizontalGradient(palette.cardGradient))
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(palette.badgeBg.copy(alpha = 0.75f))
                                        .border(1.2.dp, palette.borderColor, RoundedCornerShape(16.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(cfg.activityType.emoji, fontSize = 26.sp)
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = cfg.title,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (isCompleted) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF065F46),
                                                border = BorderStroke(0.8.dp, Color(0xFF34D399))
                                            ) {
                                                Text(
                                                    text = "✓ Done",
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFA7F3D0),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }

                                    Text(
                                        text = cfg.subtitle,
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFE2E8F0),
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.Black.copy(alpha = 0.35f)
                                        ) {
                                            Text(
                                                text = "$modeCount Words",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = palette.accentText,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color.Black.copy(alpha = 0.35f)
                                        ) {
                                            Text(
                                                text = "⚡ +${cfg.xpReward} XP",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFFDE047),
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Brush.horizontalGradient(palette.buttonGradient))
                                        .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                                        .clickable { onSelectMode(cfg) }
                                        .padding(horizontal = 14.dp)
                                        .testTag("vocab_play_btn_${cfg.activityType.name.lowercase()}"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.PlayArrow,
                                            contentDescription = "Play ${cfg.title}",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Play",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.5.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Progress Card
            item {
                val clearedCount = enabledConfigs.count { it.activityType.name in progress.completedModes }
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF130E35)),
                    border = BorderStroke(1.2.dp, Color(0xFF7C3AED).copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vocab_challenge_progress_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "🏆 My Vocabulary Progress",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            ProgressMetric(
                                label = "Modes Cleared",
                                value = "$clearedCount / ${maxOf(1, enabledConfigs.size)}",
                                color = Color(0xFF38BDF8)
                            )
                            ProgressMetric(
                                label = "Total XP",
                                value = "${progress.totalXpEarned}",
                                color = Color(0xFFFACC15)
                            )
                            ProgressMetric(
                                label = "Best Score",
                                value = "${progress.bestAccuracyPercent}%",
                                color = Color(0xFF34D399)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
        Text(text = label, fontSize = 11.sp, color = Color(0xFFCBD5E1))
    }
}

@Composable
private fun VocabularyActivityPlayView(
    config: VocabularyModeConfig,
    items: List<WordMatchChallenge>,
    allChallenges: List<WordMatchChallenge>,
    onSpeak: (String) -> Unit,
    onBackToModes: () -> Unit,
    onRoundFinished: (correct: Int, total: Int, xpAwarded: Int) -> Unit
) {
    val palette = paletteForActivity(config.activityType)
    var roundSeed by remember(config.activityType) { mutableIntStateOf(0) }

    val roundItems = remember(items, config.activityType, config.questionsPerRound, roundSeed) {
        val count = config.questionsPerRound.coerceIn(1, maxOf(1, items.size))
        items.shuffled().take(count)
    }

    var currentIndex by remember(roundItems) { mutableIntStateOf(0) }
    var correctCount by remember(roundItems) { mutableIntStateOf(0) }
    var streak by remember(roundItems) { mutableIntStateOf(0) }
    var isRoundComplete by remember(roundItems) { mutableStateOf(false) }
    var rewardSent by remember(roundItems) { mutableStateOf(false) }

    LaunchedEffect(isRoundComplete) {
        if (isRoundComplete && !rewardSent && roundItems.isNotEmpty()) {
            rewardSent = true
            val accuracyRatio = correctCount.toFloat() / roundItems.size.toFloat()
            val earnedXp = (config.xpReward * maxOf(0.4f, accuracyRatio)).toInt().coerceAtLeast(15)
            onRoundFinished(correctCount, roundItems.size, earnedXp)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Active Round Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF1B1345),
                    border = BorderStroke(1.2.dp, palette.borderColor.copy(alpha = 0.8f)),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onBackToModes() }
                        .testTag("vocab_play_back_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back to Modes",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "${config.activityType.emoji} ${config.title}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Earn up to +${config.xpReward} XP",
                        fontSize = 11.sp,
                        color = palette.accentText
                    )
                }
            }

            if (streak > 0) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF431407),
                    border = BorderStroke(1.dp, Color(0xFFF97316))
                ) {
                    Text(
                        text = "🔥 $streak",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFFFDBA74),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        if (roundItems.isEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16113A)),
                border = BorderStroke(1.2.dp, palette.borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(config.activityType.emoji, fontSize = 42.sp)
                    Text("No questions available yet", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Text(
                        "Ask your teacher to add items for ${config.title}.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = onBackToModes) { Text("Back to Activities") }
                }
            }
            return@Column
        }

        if (isRoundComplete) {
            val total = roundItems.size
            val accuracy = ((correctCount * 100f) / total).toInt()
            val earnedXp = (config.xpReward * maxOf(0.4f, correctCount.toFloat() / total)).toInt().coerceAtLeast(15)
            val earnedCoins = (correctCount * 4).coerceAtLeast(5)

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF15103D)),
                border = BorderStroke(1.6.dp, palette.borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vocab_round_summary_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🏆", fontSize = 54.sp)
                    Text(
                        text = "${config.title} Complete!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$correctCount of $total Correct • $accuracy% Accuracy",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = palette.accentText
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        RewardChip(label = "+$earnedXp XP", bg = Color(0xFF312E81), border = Color(0xFF818CF8))
                        RewardChip(label = "+$earnedCoins Coins", bg = Color(0xFF422006), border = Color(0xFFFACC15))
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBackToModes,
                            border = BorderStroke(1.2.dp, palette.borderColor),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("All Modes", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { roundSeed += 1 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Text("Play Again", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        } else {
            val currentItem = roundItems[currentIndex]

            // Progress Indicator Row
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Question ${currentIndex + 1} of ${roundItems.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = "Score: $correctCount/${roundItems.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF34D399)
                    )
                }
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / roundItems.size.toFloat() },
                    color = palette.borderColor,
                    trackColor = Color(0xFF1E1B4B),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                )
            }

            if (config.activityType == VocabularyActivityType.WORD_SCRAMBLE) {
                WordScrambleQuestionCard(
                    item = currentItem,
                    palette = palette,
                    isLast = currentIndex == roundItems.lastIndex,
                    onSpeak = onSpeak,
                    onCompleted = { solvedOnFirstOrRetry ->
                        if (solvedOnFirstOrRetry) {
                            correctCount += 1
                            streak += 1
                        } else {
                            streak = 0
                        }
                        if (currentIndex < roundItems.lastIndex) {
                            currentIndex += 1
                        } else {
                            isRoundComplete = true
                        }
                    }
                )
            } else {
                McqVocabularyQuestionCard(
                    activityType = config.activityType,
                    item = currentItem,
                    allChallenges = allChallenges,
                    palette = palette,
                    isLast = currentIndex == roundItems.lastIndex,
                    onSpeak = onSpeak,
                    onAdvance = { wasCorrect ->
                        if (wasCorrect) {
                            correctCount += 1
                            streak += 1
                        } else {
                            streak = 0
                        }
                        if (currentIndex < roundItems.lastIndex) {
                            currentIndex += 1
                        } else {
                            isRoundComplete = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RewardChip(label: String, bg: Color, border: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bg,
        border = BorderStroke(1.dp, border)
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.5.sp,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun McqVocabularyQuestionCard(
    activityType: VocabularyActivityType,
    item: WordMatchChallenge,
    allChallenges: List<WordMatchChallenge>,
    palette: ActivityPalette,
    isLast: Boolean,
    onSpeak: (String) -> Unit,
    onAdvance: (wasCorrect: Boolean) -> Unit
) {
    val (correctAnswer, options) = remember(item, activityType, allChallenges) {
        buildMcqForActivity(item, activityType, allChallenges)
    }

    var selectedOption by remember(item, activityType) { mutableStateOf<String?>(null) }
    var showClue by remember(item, activityType) { mutableStateOf(false) }
    val isAnswered = selectedOption != null
    val isCorrect = selectedOption == correctAnswer

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Question Hero Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, palette.borderColor),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vocab_question_card")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(palette.cardGradient))
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = item.category.ifBlank { "General" },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.accentText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        if (activityType != VocabularyActivityType.PICTURE_MATCHING || isAnswered) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1E1B4B),
                                border = BorderStroke(1.dp, palette.borderColor),
                                modifier = Modifier
                                    .size(48.dp)
                                    .clickable { onSpeak(item.word) }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.VolumeUp,
                                        contentDescription = "Pronounce word",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    when (activityType) {
                        VocabularyActivityType.PICTURE_MATCHING -> {
                            Box(
                                modifier = Modifier
                                    .size(104.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.1f))
                                    .border(2.dp, palette.borderColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(item.pictureEmoji.ifBlank { "🖼️" }, fontSize = 56.sp)
                            }
                            Text(
                                text = "Which English word matches this picture?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                        VocabularyActivityType.MEANING_MATCHING -> {
                            Text(
                                text = "CHOOSE THE EXACT MEANING",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.accentText,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = item.word,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                        VocabularyActivityType.SYNONYMS -> {
                            Text(
                                text = "FIND THE SYNONYM (SIMILAR MEANING)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.accentText,
                                letterSpacing = 0.8.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = item.word,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                        VocabularyActivityType.OPPOSITE_WORDS -> {
                            Text(
                                text = "FIND THE OPPOSITE WORD (ANTONYM)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.accentText,
                                letterSpacing = 0.8.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = item.word,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                        VocabularyActivityType.WORD_SCRAMBLE -> {}
                    }

                    if (activityType != VocabularyActivityType.MEANING_MATCHING && item.definition.isNotBlank()) {
                        TextButton(onClick = { showClue = !showClue }) {
                            Text(
                                text = if (showClue) "Hide Clue" else "💡 Show Clue",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFDE047)
                            )
                        }
                        AnimatedVisibility(visible = showClue) {
                            Text(
                                text = item.definition,
                                fontSize = 12.sp,
                                color = Color(0xFFE2E8F0),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        // 4 Answer Options
        val optionLabels = listOf("A", "B", "C", "D")
        options.forEachIndexed { idx, opt ->
            val isThisSelected = selectedOption == opt
            val isThisCorrect = opt == correctAnswer
            val bgColor = when {
                !isAnswered -> Color(0xFF151238)
                isThisCorrect -> Color(0xFF064E3B)
                isThisSelected && !isThisCorrect -> Color(0xFF7F1D1D)
                else -> Color(0xFF12102C).copy(alpha = 0.65f)
            }
            val borderColor = when {
                !isAnswered -> Color(0xFF334155)
                isThisCorrect -> EmeraldSuccess
                isThisSelected && !isThisCorrect -> CoralAccent
                else -> Color(0xFF1E293B)
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = bgColor,
                border = BorderStroke(1.4.dp, borderColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 52.dp)
                    .clickable(enabled = !isAnswered) {
                        selectedOption = opt
                        onSpeak(item.word)
                    }
                    .testTag("vocab_option_$idx")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isAnswered && isThisCorrect) EmeraldSuccess else Color(0xFF2E1B69),
                        modifier = Modifier.size(30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = optionLabels.getOrElse(idx) { "?" },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color.White
                            )
                        }
                    }
                    Text(
                        text = opt,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    if (isAnswered && isThisCorrect) {
                        Icon(Icons.Rounded.CheckCircle, contentDescription = "Correct", tint = EmeraldSuccess)
                    } else if (isAnswered && isThisSelected) {
                        Icon(Icons.Rounded.Cancel, contentDescription = "Incorrect", tint = CoralAccent)
                    }
                }
            }
        }

        // Answer Feedback + Next Button
        AnimatedVisibility(visible = isAnswered) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCorrect) Color(0xFF064E3B) else Color(0xFF450A0A)
                ),
                border = BorderStroke(1.2.dp, if (isCorrect) EmeraldSuccess else CoralAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isCorrect) "✓ Spot on!" else "Answer: $correctAnswer",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    if (item.definition.isNotBlank()) {
                        Text(
                            text = "${item.word}: ${item.definition}",
                            fontSize = 12.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                    Button(
                        onClick = { onAdvance(isCorrect) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCorrect) EmeraldSuccess else Color(0xFF7C3AED)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("vocab_next_question_btn")
                    ) {
                        Text(
                            text = if (isLast) "Finish Challenge 🏆" else "Next Word →",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WordScrambleQuestionCard(
    item: WordMatchChallenge,
    palette: ActivityPalette,
    isLast: Boolean,
    onSpeak: (String) -> Unit,
    onCompleted: (solvedWithoutReveal: Boolean) -> Unit
) {
    val cleanTarget = remember(item) {
        item.word.trim().uppercase().filter { it.isLetter() }.ifBlank { "WORD" }
    }

    var scrambleSeed by remember(item) { mutableIntStateOf(0) }
    val letterTiles = remember(cleanTarget, scrambleSeed) {
        val indexed = cleanTarget.mapIndexed { idx, ch -> idx to ch }
        var shuffled = indexed.shuffled()
        if (cleanTarget.toSet().size > 1 && shuffled.map { it.second }.joinToString("") == cleanTarget) {
            shuffled = indexed.reversed()
        }
        shuffled
    }

    var placedIndices by remember(cleanTarget) { mutableStateOf<List<Int>>(emptyList()) }
    var checkedCorrect by remember(cleanTarget) { mutableStateOf<Boolean?>(null) }
    var revealedFullAnswer by remember(cleanTarget) { mutableStateOf(false) }

    val builtWord = placedIndices.map { tileIdx -> letterTiles[tileIdx].second }.joinToString("")
    val hintText = item.scrambleHint.ifBlank { item.definition }.ifBlank { "Unscramble the English word" }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Scramble Clue Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, palette.borderColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(palette.cardGradient))
                    .padding(18.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.35f)
                        ) {
                            Text(
                                text = "🔀 ${cleanTarget.length} Letters • ${item.category}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.accentText,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E1B4B),
                            border = BorderStroke(1.dp, palette.borderColor),
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { onSpeak(cleanTarget) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = "Hear pronunciation hint",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Text(item.pictureEmoji.ifBlank { "🔀" }, fontSize = 36.sp)
                    Text(
                        text = hintText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.5.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Answer Slots
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF140F36)),
            border = BorderStroke(
                1.2.dp,
                when (checkedCorrect) {
                    true -> EmeraldSuccess
                    false -> CoralAccent
                    null -> Color(0xFF4C1D95)
                }
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tap letters below to build the word (tap a slot to undo)",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (slot in 0 until cleanTarget.length) {
                        val tileIdx = placedIndices.getOrNull(slot)
                        val ch = tileIdx?.let { letterTiles[it].second.toString() } ?: ""
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (ch.isNotEmpty()) Color(0xFF4F46E5) else Color(0xFF0B0A22),
                            border = BorderStroke(
                                1.5.dp,
                                if (ch.isNotEmpty()) Color(0xFF38BDF8) else Color(0xFF475569)
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(enabled = tileIdx != null && checkedCorrect != true) {
                                    placedIndices = placedIndices.filterIndexed { i, _ -> i != slot }
                                    checkedCorrect = null
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = ch,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Scrambled Tile Bank
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            letterTiles.forEachIndexed { idx, (_, ch) ->
                val isUsed = idx in placedIndices
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isUsed) Color(0xFF1E293B).copy(alpha = 0.4f) else Color(0xFF3B0764),
                    border = BorderStroke(
                        1.2.dp,
                        if (isUsed) Color(0xFF334155) else palette.borderColor
                    ),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(enabled = !isUsed && checkedCorrect != true) {
                            placedIndices = placedIndices + idx
                            checkedCorrect = null
                        }
                        .testTag("scramble_tile_$idx")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (isUsed) "" else ch.toString(),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Controls Row: Clear, Shuffle, Hint
        if (checkedCorrect != true) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        placedIndices = emptyList()
                        checkedCorrect = null
                    },
                    border = BorderStroke(1.dp, Color(0xFF475569)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("↺ Clear", fontSize = 12.sp, color = Color.White)
                }
                OutlinedButton(
                    onClick = {
                        placedIndices = emptyList()
                        checkedCorrect = null
                        scrambleSeed += 1
                    },
                    border = BorderStroke(1.dp, Color(0xFF475569)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("🔀 Mix", fontSize = 12.sp, color = Color.White)
                }
                OutlinedButton(
                    onClick = {
                        // Reveal first mismatched or next letter
                        val nextPos = placedIndices.size.coerceAtMost(cleanTarget.length - 1)
                        val neededChar = cleanTarget[nextPos]
                        val matchIdx = letterTiles.indexOfFirst { (origIdx, ch) ->
                            ch == neededChar && letterTiles.indexOfFirst { it.first == origIdx } !in placedIndices
                        }
                        if (matchIdx >= 0 && placedIndices.size < cleanTarget.length) {
                            placedIndices = placedIndices + matchIdx
                            checkedCorrect = null
                        }
                    },
                    border = BorderStroke(1.dp, Color(0xFFFACC15)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("💡 Hint", fontSize = 12.sp, color = Color(0xFFFDE047))
                }
            }

            Button(
                onClick = {
                    val ok = builtWord.equals(cleanTarget, ignoreCase = true)
                    checkedCorrect = ok
                    if (ok) {
                        onSpeak(cleanTarget)
                    }
                },
                enabled = placedIndices.size == cleanTarget.length,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("scramble_check_btn")
            ) {
                Text("✓ Check Word", fontWeight = FontWeight.ExtraBold, color = Color.White)
            }
        }

        if (checkedCorrect == false) {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                border = BorderStroke(1.dp, CoralAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Not quite! Try rearranging tiles.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFECACA),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(
                        onClick = {
                            revealedFullAnswer = true
                            checkedCorrect = true
                            onSpeak(cleanTarget)
                        }
                    ) {
                        Text("Show Answer", color = Color(0xFFFDE047), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        AnimatedVisibility(visible = checkedCorrect == true) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF064E3B)),
                border = BorderStroke(1.2.dp, EmeraldSuccess),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (revealedFullAnswer) "Answer: $cleanTarget" else "✓ Unscrambled: $cleanTarget!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    if (item.definition.isNotBlank()) {
                        Text(item.definition, fontSize = 12.sp, color = Color(0xFFD1FAE5))
                    }
                    Button(
                        onClick = { onCompleted(!revealedFullAnswer) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("scramble_next_btn")
                    ) {
                        Text(
                            text = if (isLast) "Finish Challenge 🏆" else "Next Word →",
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

private fun buildMcqForActivity(
    item: WordMatchChallenge,
    activityType: VocabularyActivityType,
    allChallenges: List<WordMatchChallenge>
): Pair<String, List<String>> {
    val correct: String
    val poolFallback: List<String>

    when (activityType) {
        VocabularyActivityType.PICTURE_MATCHING -> {
            correct = item.word.ifBlank { "Word" }
            poolFallback = allChallenges.map { it.word } + listOf("Telescope", "Compass", "Volcano", "Microscope", "Trophy", "Lighthouse")
        }
        VocabularyActivityType.MEANING_MATCHING -> {
            correct = item.definition.ifBlank { "Expressive English term" }
            poolFallback = allChallenges.map { it.definition } + listOf(
                "Speaking fluently and clearly",
                "Recovering quickly from hardship",
                "Careful attention to every detail",
                "Practical rather than purely theoretical"
            )
        }
        VocabularyActivityType.SYNONYMS -> {
            correct = item.synonym.ifBlank { item.definition }.ifBlank { "Similar" }
            poolFallback = allChallenges.mapNotNull { it.synonym.takeIf { s -> s.isNotBlank() } } +
                allChallenges.map { it.word } +
                listOf("Lively", "Hardworking", "Concise", "Brave", "Authentic", "Immense")
        }
        VocabularyActivityType.OPPOSITE_WORDS -> {
            correct = item.antonym.ifBlank { "Opposite" }
            poolFallback = allChallenges.mapNotNull { it.antonym.takeIf { a -> a.isNotBlank() } } +
                allChallenges.map { it.word } +
                listOf("Modern", "Scarce", "Pessimistic", "Opaque", "Shrink", "Temporary")
        }
        VocabularyActivityType.WORD_SCRAMBLE -> {
            correct = item.word
            poolFallback = emptyList()
        }
    }

    val chosenDistractors = LinkedHashSet<String>()
    item.distractors.forEach { d ->
        val clean = d.trim()
        if (clean.isNotEmpty() && !clean.equals(correct, ignoreCase = true)) {
            chosenDistractors.add(clean)
        }
    }
    poolFallback.shuffled().forEach { candidate ->
        if (chosenDistractors.size < 3) {
            val clean = candidate.trim()
            if (clean.isNotEmpty() && !clean.equals(correct, ignoreCase = true) && !clean.equals(item.word, ignoreCase = true)) {
                chosenDistractors.add(clean)
            }
        }
    }

    val options = (listOf(correct) + chosenDistractors.take(3)).shuffled()
    return correct to options
}
