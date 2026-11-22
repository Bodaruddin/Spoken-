@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SentenceBuilderChallenge
import com.example.data.model.SentenceBuilderPack
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.util.AssameseTranslator

private data class SentenceBuilderToken(val id: Int, val text: String)

@Composable
fun SentenceBuilderPlayScreen(
    activePack: SentenceBuilderPack,
    packChallenges: List<SentenceBuilderChallenge>,
    sessionChallenges: List<SentenceBuilderChallenge>,
    safeIndex: Int,
    roundNumber: Int,
    completedSentenceIds: Set<String>,
    onBackToChallenges: () -> Unit,
    onSpeakAudio: (String) -> Unit,
    onSentenceCompleted: (SentenceBuilderChallenge) -> Unit,
    onRewardWin: (xp: Int, coins: Int) -> Unit,
    onNextSentence: () -> Unit,
    onPlayAgain: () -> Unit,
    onTranslateSentence: (suspend (String) -> AssameseTranslator.TranslationResult)? = null,
    modifier: Modifier = Modifier
) {
    val current = sessionChallenges.getOrNull(safeIndex)
    val scrollState = rememberScrollState()

    LaunchedEffect(roundNumber, safeIndex) {
        scrollState.animateScrollTo(0)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF060828),
                        Color(0xFF0B1148),
                        Color(0xFF0B0B3B),
                        Color(0xFF05041E)
                    )
                )
            )
    ) {
        // Ambient cosmic ribbon & luminous violet/cyan corner glows
        ArcadeBackgroundWaves()
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF9333EA).copy(alpha = 0.28f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.14f, size.height * 0.12f),
                    radius = size.width * 0.56f
                ),
                radius = size.width * 0.56f,
                center = Offset(size.width * 0.14f, size.height * 0.12f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF2563EB).copy(alpha = 0.28f),
                        Color(0xFF06B6D4).copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.90f, size.height * 0.20f),
                    radius = size.width * 0.60f
                ),
                radius = size.width * 0.60f,
                center = Offset(size.width * 0.90f, size.height * 0.20f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF7C3AED).copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.22f, size.height * 0.85f),
                    radius = size.width * 0.54f
                ),
                radius = size.width * 0.54f,
                center = Offset(size.width * 0.22f, size.height * 0.85f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(scrollState)
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar: Glowing Challenges Back Pill + Center Title + Glowing XP Pill
            SentenceBuilderPlayHeader(
                packTitle = activePack.title,
                sentenceCount = sessionChallenges.size,
                difficulty = activePack.difficulty,
                xpReward = activePack.xpReward,
                onBackClick = onBackToChallenges
            )

            if (current == null) {
                SentenceBuilderEmptyPackCard(
                    packTitle = activePack.title,
                    onChooseAnother = onBackToChallenges
                )
            } else {
                // Tokenize target sentence with unique IDs to handle duplicate words cleanly
                val initialTokens = remember(roundNumber, current.id) {
                    current.targetSentence.trim().split(Regex("\\s+")).mapIndexed { index, word ->
                        SentenceBuilderToken(index, word)
                    }
                }
                val shuffledChips = remember(roundNumber, current.id) {
                    val shuffled = initialTokens.shuffled()
                    if (initialTokens.size > 1 && shuffled.map { it.text } == initialTokens.map { it.text }) {
                        shuffled.reversed()
                    } else {
                        shuffled
                    }
                }

                var selectedChips by remember(roundNumber, current.id) { mutableStateOf(listOf<SentenceBuilderToken>()) }
                var isCorrect by remember(roundNumber, current.id) { mutableStateOf<Boolean?>(null) }
                var hasAwarded by remember(roundNumber, current.id) { mutableStateOf(false) }

                val remainingChips = remember(shuffledChips, selectedChips) {
                    val selectedIds = selectedChips.map { it.id }.toSet()
                    shuffledChips.filterNot { selectedIds.contains(it.id) }
                }

                // Main Glowing Neon Glass Card
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(
                        1.8.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFFC084FC),
                                Color(0xFF6366F1),
                                Color(0xFF38BDF8),
                                Color(0xFF3B82F6),
                                Color(0xFF9333EA)
                            )
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1).copy(alpha = 0.24f),
                                        Color(0xFF38BDF8).copy(alpha = 0.12f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width * 0.5f, size.height * 0.45f),
                                    radius = size.maxDimension * 0.65f
                                ),
                                cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
                            )
                        }
                        .testTag("sb_main_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF0A1244).copy(alpha = 0.94f),
                                        Color(0xFF0A144A).copy(alpha = 0.95f),
                                        Color(0xFF090E3C).copy(alpha = 0.96f)
                                    )
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Card Header: Purple List Icon Badge + Title + Cyan Counter
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
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFA855F7), Color(0xFF6366F1))
                                            )
                                        )
                                        .border(
                                            1.1.dp,
                                            Color(0xFFE9D5FF).copy(alpha = 0.7f),
                                            RoundedCornerShape(11.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.FormatListBulleted,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }

                                Text(
                                    text = "Assemble Sentence Syntax:",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Sentence ${safeIndex + 1}/${sessionChallenges.size}",
                                    fontSize = 13.sp,
                                    color = Color(0xFF22D3EE),
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                                if (current.createdBy.isNotBlank() && current.createdBy != "System") {
                                    Text(
                                        text = "By ${current.createdBy}",
                                        fontSize = 10.sp,
                                        color = Color(0xFFFCA5A5),
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Glowing Sapphire Hint Banner
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.Transparent,
                            border = BorderStroke(
                                1.3.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF38BDF8).copy(alpha = 0.85f),
                                        Color(0xFF60A5FA).copy(alpha = 0.7f),
                                        Color(0xFF3B82F6).copy(alpha = 0.85f)
                                    )
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onSpeakAudio(current.hint) }
                                .testTag("sb_hint_banner")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF0D205A).copy(alpha = 0.92f),
                                                Color(0xFF102A70).copy(alpha = 0.92f),
                                                Color(0xFF0B1C50).copy(alpha = 0.92f)
                                            )
                                        )
                                    )
                                    .padding(horizontal = 14.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Lightbulb,
                                    contentDescription = "Hint",
                                    tint = Color(0xFFFACC15),
                                    modifier = Modifier.size(22.dp)
                                )
                                Text(
                                    text = buildAnnotatedString {
                                        withStyle(
                                            SpanStyle(
                                                color = Color(0xFFFACC15),
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        ) {
                                            append("Hint:  ")
                                        }
                                        withStyle(
                                            SpanStyle(
                                                color = Color.White,
                                                fontWeight = FontWeight.Medium
                                            )
                                        ) {
                                            append(current.hint)
                                        }
                                    },
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ChevronRight,
                                    contentDescription = "Listen to hint",
                                    tint = Color(0xFF93C5FD),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Assembled Sentence Section
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                SentenceBuilderSectionTitle(title = "Your Assembled Sentence:")
                                if (selectedChips.isNotEmpty()) {
                                    TextButton(
                                        onClick = {
                                            selectedChips = emptyList()
                                            isCorrect = null
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.testTag("sb_clear_all_btn")
                                    ) {
                                        Text(
                                            text = "Clear All",
                                            fontSize = 12.sp,
                                            color = Color(0xFFFCA5A5),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Dashed-Border Neon Assembly Tray
                            val trayBorderColor = when {
                                isCorrect == true -> EmeraldSuccess
                                isCorrect == false -> CoralAccent
                                selectedChips.isNotEmpty() -> Color(0xFF38BDF8)
                                else -> Color(0xFF38BDF8).copy(alpha = 0.68f)
                            }
                            val trayCornerRadius = 16.dp

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 96.dp)
                                    .clip(RoundedCornerShape(trayCornerRadius))
                                    .background(Color(0xFF070F38).copy(alpha = 0.85f))
                                    .drawBehind {
                                        val strokeWidth = 1.5.dp.toPx()
                                        val cornerPx = trayCornerRadius.toPx()
                                        drawRoundRect(
                                            color = trayBorderColor,
                                            topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                                            size = size.copy(
                                                width = size.width - strokeWidth,
                                                height = size.height - strokeWidth
                                            ),
                                            cornerRadius = CornerRadius(cornerPx, cornerPx),
                                            style = Stroke(
                                                width = strokeWidth,
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 9f), 0f)
                                            )
                                        )
                                    }
                                    .padding(horizontal = 14.dp, vertical = 14.dp)
                                    .testTag("sb_assembled_tray"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (selectedChips.isEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Edit,
                                            contentDescription = null,
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Tap the word chips below in grammatical order...",
                                            fontSize = 13.5.sp,
                                            color = Color(0xFFCBD5E1),
                                            lineHeight = 19.sp
                                        )
                                    }
                                } else {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        selectedChips.forEachIndexed { index, chip ->
                                            Surface(
                                                shape = RoundedCornerShape(13.dp),
                                                color = Color.Transparent,
                                                border = BorderStroke(
                                                    1.3.dp,
                                                    Brush.horizontalGradient(
                                                        listOf(Color(0xFFE9D5FF), Color(0xFF67E8F9))
                                                    )
                                                ),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(13.dp))
                                                    .clickable {
                                                        selectedChips = selectedChips.filterIndexed { i, _ -> i != index }
                                                        isCorrect = null
                                                    }
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                listOf(Color(0xFF7C3AED), Color(0xFF2563EB))
                                                            )
                                                        )
                                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = chip.text,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Spacer(Modifier.width(5.dp))
                                                    Icon(
                                                        imageVector = Icons.Rounded.Close,
                                                        contentDescription = "Remove ${chip.text}",
                                                        tint = Color.White.copy(alpha = 0.85f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Available Words Section
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SentenceBuilderSectionTitle(title = "Available Words:")

                            if (remainingChips.isEmpty()) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFF0A1A48).copy(alpha = 0.85f),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "✓ All words placed! Tap 'Verify Sentence Syntax' below.",
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF93C5FD),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    remainingChips.forEach { chip ->
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = Color.Transparent,
                                            border = BorderStroke(
                                                1.5.dp,
                                                Brush.linearGradient(
                                                    listOf(
                                                        Color(0xFF60A5FA),
                                                        Color(0xFF38BDF8),
                                                        Color(0xFF3B82F6)
                                                    )
                                                )
                                            ),
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 44.dp)
                                                .drawBehind {
                                                    drawRoundRect(
                                                        color = Color(0xFF2563EB).copy(alpha = 0.24f),
                                                        cornerRadius = CornerRadius(15.dp.toPx(), 15.dp.toPx())
                                                    )
                                                }
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable {
                                                    selectedChips = selectedChips + chip
                                                    isCorrect = null
                                                    onSpeakAudio(chip.text)
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .background(
                                                        Brush.verticalGradient(
                                                            listOf(
                                                                Color(0xFF0F205C),
                                                                Color(0xFF09143D)
                                                            )
                                                        )
                                                    )
                                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = chip.text,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.5.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Glowing Magenta-to-Cyan Primary CTA Button ("Verify Sentence Syntax")
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = Color.Transparent,
                            border = BorderStroke(
                                1.4.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFF5D0FE),
                                        Color(0xFF93C5FD),
                                        Color(0xFFA5F3FC)
                                    )
                                )
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(54.dp)
                                .drawBehind {
                                    drawRoundRect(
                                        brush = Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFD946EF).copy(alpha = 0.35f),
                                                Color(0xFF3B82F6).copy(alpha = 0.30f),
                                                Color(0xFF06B6D4).copy(alpha = 0.35f)
                                            )
                                        ),
                                        cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx())
                                    )
                                }
                                .clip(RoundedCornerShape(18.dp))
                                .clickable {
                                    val constructed = selectedChips.joinToString(" ") { it.text }
                                    fun normalize(s: String) = s.replace(Regex("[.,!?;:]"), "").trim().lowercase()
                                    if (normalize(constructed) == normalize(current.targetSentence)) {
                                        isCorrect = true
                                        onSentenceCompleted(current)
                                        if (!hasAwarded) {
                                            hasAwarded = true
                                            onRewardWin(activePack.xpReward, 20)
                                        }
                                        onSpeakAudio(current.targetSentence)
                                    } else {
                                        isCorrect = false
                                    }
                                }
                                .testTag("sb_verify_syntax_btn")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFC026D3),
                                                Color(0xFF8B5CF6),
                                                Color(0xFF3B82F6),
                                                Color(0xFF00C6FF)
                                            )
                                        )
                                    ),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF4F46E5),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "Verify Sentence Syntax",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.5.sp
                                )
                            }
                        }

                        // Incorrect Feedback Banner
                        if (isCorrect == false) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF450A0A).copy(alpha = 0.85f),
                                border = BorderStroke(1.2.dp, CoralAccent.copy(alpha = 0.85f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "❌ Not quite right yet. Tap words to remove from tray and re-arrange into correct syntax.",
                                    color = Color(0xFFFCA5A5),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Assamese Translation & Round Progression
                        if (isCorrect == true) {
                            var translation by remember(current.id) {
                                mutableStateOf(
                                    AssameseTranslator.translate(current.targetSentence, current.assameseTranslation)
                                )
                            }
                            var isTranslatingWithAi by remember(current.id) {
                                mutableStateOf(onTranslateSentence != null)
                            }
                            LaunchedEffect(current.id, onTranslateSentence) {
                                if (onTranslateSentence != null) {
                                    isTranslatingWithAi = true
                                    try {
                                        val aiTrans = onTranslateSentence(current.targetSentence)
                                        if (aiTrans.assameseText.isNotBlank()) {
                                            translation = aiTrans
                                        }
                                    } catch (_: Throwable) {
                                    } finally {
                                        isTranslatingWithAi = false
                                    }
                                }
                            }

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF072222).copy(alpha = 0.92f)),
                                border = BorderStroke(1.4.dp, Color(0xFF10B981).copy(alpha = 0.65f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "অসমীয়া অনুবাদ (Assamese Translation):",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF86EFAC)
                                        )
                                        if (isTranslatingWithAi) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(14.dp),
                                                color = Color(0xFF38BDF8),
                                                strokeWidth = 2.dp
                                            )
                                        } else if (translation.isFromAi) {
                                            Text(
                                                text = "✨ Gemini AI",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF7DD3FC)
                                            )
                                        }
                                    }
                                    Text(
                                        text = translation.assameseText,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Text(
                                        text = "শব্দার্থ (Word Meanings):",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFCBD5E1)
                                    )
                                    FlowRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        translation.wordBreakdown.forEach { (en, asWord) ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFF0B1B38),
                                                border = BorderStroke(0.9.dp, Color(0xFF38BDF8).copy(alpha = 0.45f))
                                            ) {
                                                Text(
                                                    text = "$en: $asWord",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF93C5FD),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            val isLastSentence = safeIndex >= sessionChallenges.lastIndex
                            if (isLastSentence && completedSentenceIds.size >= sessionChallenges.size) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF131444).copy(alpha = 0.94f),
                                    border = BorderStroke(1.3.dp, Color(0xFFFACC15).copy(alpha = 0.7f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "🏆 ${activePack.title} Completed!",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.5.sp,
                                            color = Color(0xFFFDE047)
                                        )
                                        Text(
                                            text = if (packChallenges.size > sessionChallenges.size) {
                                                "Finished all ${sessionChallenges.size} sentences! Tap Play Again for a fresh set."
                                            } else {
                                                "Finished all ${sessionChallenges.size} sentences! Tap Play Again for a reshuffled round."
                                            },
                                            fontSize = 12.sp,
                                            color = Color(0xFFCBD5E1),
                                            textAlign = TextAlign.Center
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = Color.Transparent,
                                                border = BorderStroke(1.2.dp, Color(0xFF7DD3FC).copy(alpha = 0.85f)),
                                                modifier = Modifier
                                                    .weight(1.1f)
                                                    .height(48.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .clickable { onPlayAgain() }
                                                    .testTag("sb_play_again_btn")
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.horizontalGradient(
                                                                listOf(Color(0xFF0077FF), Color(0xFF00C6FF))
                                                            )
                                                        ),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Refresh,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        text = "Play Again",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 13.5.sp,
                                                        color = Color.White
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = Color(0xFF0A1238),
                                                border = BorderStroke(1.2.dp, Color(0xFF94A3B8).copy(alpha = 0.75f)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(48.dp)
                                                    .clip(RoundedCornerShape(50))
                                                    .clickable { onBackToChallenges() }
                                                    .testTag("sb_other_challenges_btn")
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "Challenges",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.5.sp,
                                                        color = Color.White
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.Transparent,
                                    border = BorderStroke(1.2.dp, Color(0xFF6EE7B7).copy(alpha = 0.75f)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable {
                                            selectedChips = emptyList()
                                            isCorrect = null
                                            hasAwarded = false
                                            onNextSentence()
                                        }
                                        .testTag("sb_next_sentence_btn")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.horizontalGradient(
                                                    listOf(Color(0xFF10B981), Color(0xFF059669))
                                                )
                                            ),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Next Sentence",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.5.sp,
                                            color = Color.White
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun SentenceBuilderPlayHeader(
    packTitle: String,
    sentenceCount: Int,
    difficulty: String,
    xpReward: Int,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Glowing Violet Challenges Back Pill
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.Transparent,
            border = BorderStroke(
                1.4.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFFD8B4FE), Color(0xFF818CF8))
                )
            ),
            modifier = Modifier
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(18.dp))
                .clickable { onBackClick() }
                .testTag("sb_back_to_challenges_btn")
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF6D28D9),
                                Color(0xFF4F46E5),
                                Color(0xFF3730A3)
                            )
                        )
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Back to Challenges",
                    tint = Color.White,
                    modifier = Modifier.size(19.dp)
                )
                Text(
                    text = "Challenges",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp)
        ) {
            Text(
                text = packTitle,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$sentenceCount ${if (sentenceCount == 1) "Sentence" else "Sentences"} • $difficulty",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFCBD5E1),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Glowing Indigo + Gold XP Pill
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color.Transparent,
            border = BorderStroke(
                1.3.dp,
                Brush.horizontalGradient(
                    listOf(Color(0xFF60A5FA).copy(alpha = 0.85f), Color(0xFF6366F1).copy(alpha = 0.85f))
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF151952).copy(alpha = 0.94f),
                                Color(0xFF0E133D).copy(alpha = 0.94f)
                            )
                        )
                    )
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = null,
                    tint = Color(0xFFFACC15),
                    modifier = Modifier.size(17.dp)
                )
                Text(
                    text = "+$xpReward XP",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFDE047),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun SentenceBuilderSectionTitle(title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFD8B4FE), Color(0xFF8B5CF6))
                    )
                )
        )
        Text(
            text = title,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun SentenceBuilderEmptyPackCard(
    packTitle: String,
    onChooseAnother: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            1.5.dp,
            Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFFA855F7)))
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF090F2E), Color(0xFF141142)))
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "No Sentences in This Challenge",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
            Text(
                text = "Ask your teacher to add sentences to '$packTitle', or select another challenge.",
                fontSize = 12.5.sp,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center
            )
            Button(
                onClick = onChooseAnother,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
            ) {
                Text("Choose Another Challenge", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}
