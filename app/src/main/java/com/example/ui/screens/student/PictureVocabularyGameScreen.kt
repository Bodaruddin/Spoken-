package com.example.ui.screens.student

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.EmeraldSuccess
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

private data class PictureVocabUiItem(
    val id: String,
    val emoji: String,
    val correctWord: String,
    val phonetic: String,
    val clue: String,
    val options: List<String>
)

@DrawableRes
private fun resolvePictureVocabDrawable(word: String): Int? {
    val key = word.trim().lowercase()
    return when {
        key.contains("stethoscope") -> R.drawable.img_vocab_stethoscope
        key.contains("airplane") || key.contains("aeroplane") || key == "plane" -> R.drawable.img_vocab_airplane
        key.contains("library") -> R.drawable.img_vocab_library
        key.contains("cooking") || key == "cook" -> R.drawable.img_vocab_cooking
        key.contains("guitar") -> R.drawable.img_vocab_guitar
        else -> null
    }
}

private fun buildHighlightedClue(clue: String): AnnotatedString {
    val cleanClue = clue.trim().let { if (it.endsWith(".") || it.endsWith("!") || it.endsWith("?")) it else "$it." }

    // Highlight key descriptive phrases in warm gold to match the visual hierarchy
    val curatedPhrases = listOf(
        "medical doctors",
        "listen to heart and lungs",
        "flying vehicle",
        "fixed wings",
        "containing books",
        "reading or study",
        "preparing food",
        "combining ingredients",
        "six strings",
        "fingers or pick",
        "full speed",
        "short distance"
    )

    val ranges = mutableListOf<IntRange>()
    val lowerClue = cleanClue.lowercase()
    for (phrase in curatedPhrases) {
        val idx = lowerClue.indexOf(phrase)
        if (idx >= 0) {
            ranges.add(idx until (idx + phrase.length))
        }
    }

    // Generic smart fallback for custom teacher-added clues: emphasize the action/detail after "to " or "for " or "with "
    if (ranges.isEmpty()) {
        val markers = listOf(" to ", " for ", " with ", " used by ", " that ")
        for (marker in markers) {
            val idx = lowerClue.indexOf(marker)
            if (idx > 0 && idx + marker.length < cleanClue.length - 3) {
                val start = idx + marker.length
                val end = cleanClue.length.let { if (cleanClue.endsWith(".")) it - 1 else it }
                if (end > start) {
                    ranges.add(start until end)
                    break
                }
            }
        }
    }

    if (ranges.isEmpty()) {
        return AnnotatedString(cleanClue)
    }

    val sortedRanges = ranges.sortedBy { it.first }
    return buildAnnotatedString {
        var cursor = 0
        for (range in sortedRanges) {
            if (range.first < cursor) continue
            if (range.first > cursor) {
                append(cleanClue.substring(cursor, range.first))
            }
            withStyle(
                SpanStyle(
                    color = Color(0xFFFCD34D),
                    fontWeight = FontWeight.Bold
                )
            ) {
                append(cleanClue.substring(range.first, range.last + 1))
            }
            cursor = range.last + 1
        }
        if (cursor < cleanClue.length) {
            append(cleanClue.substring(cursor))
        }
    }
}

@Composable
fun PictureVocabGame(
    viewModel: MainViewModel,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dynamicChallenges by viewModel.pictureVocabChallenges.collectAsState()
    val items = remember(dynamicChallenges) {
        if (dynamicChallenges.isNotEmpty()) {
            dynamicChallenges.map {
                PictureVocabUiItem(
                    id = it.id,
                    emoji = it.emoji,
                    correctWord = it.word,
                    phonetic = it.phonetic,
                    clue = it.clue,
                    options = if (it.options.contains(it.word)) it.options else (it.options + it.word)
                )
            }
        } else {
            listOf(
                PictureVocabUiItem(
                    "pv_1",
                    "🩺",
                    "Stethoscope",
                    "/ˈsteθ.ə.skəʊp/",
                    "An instrument used by medical doctors to listen to heart and lungs",
                    listOf("Stethoscope", "Microscope", "Thermometer", "Telescope")
                ),
                PictureVocabUiItem(
                    "pv_2",
                    "✈️",
                    "Airplane",
                    "/ˈeə.pleɪn/",
                    "A powered flying vehicle with fixed wings",
                    listOf("Airplane", "Helicopter", "Submarine", "Spaceship")
                ),
                PictureVocabUiItem(
                    "pv_3",
                    "📚",
                    "Library",
                    "/ˈlaɪ.brər.i/",
                    "A quiet room or building containing books for reading or study",
                    listOf("Library", "Bakery", "Auditorium", "Laboratory")
                ),
                PictureVocabUiItem(
                    "pv_4",
                    "🍳",
                    "Cooking",
                    "/ˈkʊk.ɪŋ/",
                    "The practice or skill of preparing food by combining ingredients",
                    listOf("Cooking", "Gardening", "Sewing", "Painting")
                ),
                PictureVocabUiItem(
                    "pv_5",
                    "🎸",
                    "Guitar",
                    "/ɡɪˈtɑːr/",
                    "A musical instrument having six strings played with fingers or pick",
                    listOf("Guitar", "Violin", "Drum", "Saxophone")
                ),
                PictureVocabUiItem(
                    "pv_6",
                    "🏃",
                    "Sprinting",
                    "/ˈsprɪn.tɪŋ/",
                    "Running at full speed over a short distance",
                    listOf("Sprinting", "Swimming", "Jumping", "Climbing")
                )
            )
        }
    }

    BackHandler {
        onClose()
    }

    var itemIndex by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF070A2C),
                        Color(0xFF0C1245),
                        Color(0xFF130B44),
                        Color(0xFF070524)
                    )
                )
            )
    ) {
        // Ambient cosmic streaks & radiant corner glows
        ArcadeBackgroundWaves()
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF9333EA).copy(alpha = 0.24f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.12f, size.height * 0.12f),
                    radius = size.width * 0.55f
                ),
                radius = size.width * 0.55f,
                center = Offset(size.width * 0.12f, size.height * 0.12f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF06B6D4).copy(alpha = 0.20f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.90f, size.height * 0.84f),
                    radius = size.width * 0.58f
                ),
                radius = size.width * 0.58f,
                center = Offset(size.width * 0.90f, size.height * 0.84f)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 600.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Picture Vocabulary Header Row
            PictureVocabTopHeader(onClose = onClose)

            if (items.isEmpty()) {
                PictureVocabEmptyStateCard()
            } else {
                val safeIndex = itemIndex % items.size
                val current = items[safeIndex]
                val drawableRes = remember(current.correctWord) {
                    resolvePictureVocabDrawable(current.correctWord)
                }

                var selectedOption by remember(current.id) { mutableStateOf<String?>(null) }
                var isVerified by remember(current.id) { mutableStateOf<Boolean?>(null) }
                var showSelectPrompt by remember(current.id) { mutableStateOf(false) }
                var hasAwarded by remember(current.id) { mutableStateOf(false) }
                var isWaveformPlaying by remember(current.id) { mutableStateOf(false) }
                var showExpandedImage by remember(current.id) { mutableStateOf(false) }

                LaunchedEffect(isWaveformPlaying) {
                    if (isWaveformPlaying) {
                        delay(2800L)
                        isWaveformPlaying = false
                    }
                }

                // Main Glowing Neon Glass Quiz Card
                Card(
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(
                        1.8.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF9333EA),
                                Color(0xFF38BDF8),
                                Color(0xFF6366F1),
                                Color(0xFFA855F7),
                                Color(0xFF22D3EE)
                            )
                        )
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            drawRoundRect(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1).copy(alpha = 0.25f),
                                        Color(0xFF38BDF8).copy(alpha = 0.10f),
                                        Color.Transparent
                                    ),
                                    center = Offset(size.width / 2f, size.height / 2f),
                                    radius = size.maxDimension * 0.65f
                                ),
                                cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
                            )
                        }
                        .testTag("picture_vocab_main_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF0A113E).copy(alpha = 0.96f),
                                        Color(0xFF0D154A).copy(alpha = 0.96f),
                                        Color(0xFF100E42).copy(alpha = 0.97f)
                                    )
                                )
                            )
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Quiz Title Row + Counter Pill ("1 / 7")
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
                                // Glowing Purple Open-Book Badge
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFFC084FC), Color(0xFF7C3AED))
                                            )
                                        )
                                        .border(
                                            1.1.dp,
                                            Color(0xFFE9D5FF).copy(alpha = 0.75f),
                                            RoundedCornerShape(10.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }

                                Text(
                                    text = "Picture Vocabulary Quiz",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(Modifier.width(8.dp))

                            // Glowing Violet Counter Pill
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.Transparent,
                                border = BorderStroke(
                                    1.2.dp,
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFE9D5FF), Color(0xFFA855F7))
                                    )
                                ),
                                modifier = Modifier.testTag("picture_vocab_counter_pill")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF9333EA), Color(0xFF6366F1))
                                            )
                                        )
                                        .padding(horizontal = 13.dp, vertical = 5.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${safeIndex + 1} / ${items.size}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Subtle Luminous Divider
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color.Transparent,
                                            Color(0xFF38BDF8).copy(alpha = 0.55f),
                                            Color(0xFFA855F7).copy(alpha = 0.45f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )

                        // Visual Showcase Card with Real Studio Image + Expand Button
                        PictureVocabVisualCard(
                            drawableRes = drawableRes,
                            emoji = current.emoji,
                            contentDescription = "Vocabulary visual clue",
                            onExpandClick = { showExpandedImage = true }
                        )

                        // Audio Waveform Player Strip ("Listen Carefully")
                        PictureVocabAudioBar(
                            isPlaying = isWaveformPlaying,
                            onPlayClick = {
                                isWaveformPlaying = true
                                val spokenText = if (isVerified == true) {
                                    "${current.correctWord}. ${current.clue}"
                                } else {
                                    current.clue
                                }
                                viewModel.speakAudio(spokenText, slow = false)
                            }
                        )

                        // Glowing Lightbulb Clue Card with Gold Keyword Highlights
                        PictureVocabClueBox(
                            clue = current.clue
                        )

                        // 4 Radio Option Pills
                        Column(
                            verticalArrangement = Arrangement.spacedBy(9.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            current.options.forEachIndexed { idx, opt ->
                                val isSelected = selectedOption == opt
                                val isCorrectAnswer = opt.equals(current.correctWord, ignoreCase = true)
                                PictureVocabOptionRow(
                                    label = opt,
                                    index = idx,
                                    isSelected = isSelected,
                                    isCorrectAnswer = isCorrectAnswer,
                                    verifiedState = isVerified,
                                    onClick = {
                                        if (isVerified != true) {
                                            selectedOption = opt
                                            showSelectPrompt = false
                                            if (isVerified == false) {
                                                isVerified = null
                                            }
                                        }
                                    }
                                )
                            }
                        }

                        // Inline Feedback Banner (on verification or missing selection)
                        AnimatedVisibility(
                            visible = showSelectPrompt || isVerified != null,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            val bannerBorder = when {
                                isVerified == true -> Color(0xFF34D399)
                                isVerified == false -> Color(0xFFF87171)
                                else -> Color(0xFFFBBF24)
                            }
                            val bannerBg = when {
                                isVerified == true -> Color(0xFF064E3B).copy(alpha = 0.72f)
                                isVerified == false -> Color(0xFF7F1D1D).copy(alpha = 0.65f)
                                else -> Color(0xFF451A03).copy(alpha = 0.65f)
                            }
                            val bannerText = when {
                                isVerified == true -> {
                                    val phoneticPart = current.phonetic.takeIf { it.isNotBlank() }?.let { "  •  $it" }.orEmpty()
                                    "Correct! ${current.correctWord}$phoneticPart  (+40 XP)"
                                }
                                isVerified == false -> "Not quite right — select another option or tap Clear All to retry."
                                else -> "Please select an option before verifying your answer."
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = bannerBg,
                                border = BorderStroke(1.dp, bannerBorder.copy(alpha = 0.85f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("picture_vocab_feedback_banner")
                            ) {
                                Text(
                                    text = bannerText,
                                    color = Color.White,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                                )
                            }
                        }

                        // Action Buttons Row: Clear All + Verify Answer / Next Item
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Left Button: Clear All
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.Transparent,
                                border = BorderStroke(
                                    1.3.dp,
                                    Brush.horizontalGradient(
                                        listOf(
                                            Color(0xFF60A5FA).copy(alpha = 0.7f),
                                            Color(0xFF475569).copy(alpha = 0.8f),
                                            Color(0xFF6366F1).copy(alpha = 0.7f)
                                        )
                                    )
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        selectedOption = null
                                        isVerified = null
                                        showSelectPrompt = false
                                    }
                                    .testTag("picture_vocab_clear_all_btn")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF0A1238), Color(0xFF101948))
                                            )
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Refresh,
                                        contentDescription = "Clear Selection",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                    Spacer(Modifier.width(7.dp))
                                    Text(
                                        text = "Clear All",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            // Right Button: Verify Answer (transitions to Next Item once verified correct)
                            val isReadyForNext = isVerified == true
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color.Transparent,
                                border = BorderStroke(
                                    1.4.dp,
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFF0ABFC), Color(0xFF67E8F9))
                                    )
                                ),
                                modifier = Modifier
                                    .weight(1.28f)
                                    .height(48.dp)
                                    .clip(RoundedCornerShape(50))
                                    .clickable {
                                        if (isReadyForNext) {
                                            itemIndex = (safeIndex + 1) % items.size
                                            selectedOption = null
                                            isVerified = null
                                            showSelectPrompt = false
                                            hasAwarded = false
                                        } else {
                                            val chosen = selectedOption
                                            if (chosen == null) {
                                                showSelectPrompt = true
                                            } else {
                                                val correct = chosen.equals(current.correctWord, ignoreCase = true)
                                                isVerified = correct
                                                showSelectPrompt = false
                                                if (correct) {
                                                    if (!hasAwarded) {
                                                        hasAwarded = true
                                                        viewModel.rewardGameWin(40, 15)
                                                    }
                                                    viewModel.speakAudio(current.correctWord)
                                                }
                                            }
                                        }
                                    }
                                    .testTag("picture_vocab_verify_btn")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                if (isReadyForNext) {
                                                    listOf(
                                                        Color(0xFF10B981),
                                                        Color(0xFF06B6D4),
                                                        Color(0xFF3B82F6)
                                                    )
                                                } else {
                                                    listOf(
                                                        Color(0xFFC026D3),
                                                        Color(0xFF8B5CF6),
                                                        Color(0xFF3B82F6),
                                                        Color(0xFF06B6D4)
                                                    )
                                                }
                                            )
                                        ),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (isReadyForNext) Icons.AutoMirrored.Rounded.ArrowForward else Icons.Rounded.Check,
                                        contentDescription = if (isReadyForNext) "Next Item" else "Verify Answer",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(7.dp))
                                    Text(
                                        text = if (isReadyForNext) "Next Picture" else "Verify Answer",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.5.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }

                // Expanded Image Zoom Modal
                if (showExpandedImage) {
                    PictureVocabExpandedDialog(
                        drawableRes = drawableRes,
                        emoji = current.emoji,
                        clue = current.clue,
                        phonetic = if (isVerified == true) current.phonetic else null,
                        onPlayAudio = {
                            isWaveformPlaying = true
                            viewModel.speakAudio(
                                if (isVerified == true) "${current.correctWord}. ${current.clue}" else current.clue
                            )
                        },
                        onDismiss = { showExpandedImage = false }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PictureVocabTopHeader(
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .testTag("picture_vocab_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Glowing Purple Rounded-Square Picture Icon Tile
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .drawBehind {
                        drawRoundRect(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFA855F7).copy(alpha = 0.55f),
                                    Color(0xFF6366F1).copy(alpha = 0.25f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.width * 0.9f
                            ),
                            cornerRadius = CornerRadius(18.dp.toPx(), 18.dp.toPx())
                        )
                    }
                    .clip(RoundedCornerShape(15.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF9333EA), Color(0xFF4F46E5))
                        )
                    )
                    .border(
                        1.6.dp,
                        Brush.linearGradient(
                            listOf(Color(0xFFE9D5FF), Color(0xFF818CF8))
                        ),
                        RoundedCornerShape(15.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Crisp Landscape Picture Tile Canvas
                Canvas(modifier = Modifier.size(30.dp)) {
                    val w = size.width
                    val h = size.height
                    val pad = 2.dp.toPx()

                    // White outer photo frame
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(0f, 0f),
                        size = Size(w, h),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                    // Bright sky gradient backdrop
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF7DD3FC), Color(0xFFBAE6FD))
                        ),
                        topLeft = Offset(pad, pad),
                        size = Size(w - pad * 2f, h - pad * 2f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    // Warm golden sun
                    drawCircle(
                        color = Color(0xFFFACC15),
                        radius = w * 0.12f,
                        center = Offset(w * 0.32f, h * 0.34f)
                    )
                    // Secondary right sun/cloud highlight
                    drawCircle(
                        color = Color(0xFFFDE047).copy(alpha = 0.65f),
                        radius = w * 0.08f,
                        center = Offset(w * 0.68f, h * 0.34f)
                    )
                    // Back cyan mountain
                    val backMountain = Path().apply {
                        moveTo(pad, h - pad)
                        lineTo(w * 0.36f, h * 0.50f)
                        lineTo(w * 0.64f, h - pad)
                        close()
                    }
                    drawPath(backMountain, color = Color(0xFF38BDF8))

                    // Front royal blue mountain
                    val frontMountain = Path().apply {
                        moveTo(w * 0.38f, h - pad)
                        lineTo(w * 0.70f, h * 0.44f)
                        lineTo(w - pad, h - pad)
                        close()
                    }
                    drawPath(frontMountain, color = Color(0xFF2563EB))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Picture Vocabulary",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Interactive Learning Challenge",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFCBD5E1),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Glowing Circular Close Button (48dp touch target)
        Surface(
            shape = CircleShape,
            color = Color(0xFF0E164B).copy(alpha = 0.92f),
            border = BorderStroke(
                1.6.dp,
                Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF818CF8)))
            ),
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .clickable { onClose() }
                .testTag("close_game_button")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close Picture Vocabulary",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun PictureVocabVisualCard(
    @DrawableRes drawableRes: Int?,
    emoji: String,
    contentDescription: String,
    onExpandClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFEAF2FF),
        border = BorderStroke(
            1.6.dp,
            Brush.linearGradient(
                listOf(
                    Color(0xFFE0F2FE),
                    Color(0xFF93C5FD),
                    Color(0xFFBAE6FD)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(178.dp)
            .testTag("picture_vocab_image_card")
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (drawableRes != null) {
                Image(
                    painter = painterResource(id = drawableRes),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Polished studio fallback for custom/additional vocabulary items
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    Color(0xFFF8FAFC),
                                    Color(0xFFE2ECFD),
                                    Color(0xFFCFE0FA)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = Color(0xFF3B82F6).copy(alpha = 0.08f),
                            radius = size.minDimension * 0.42f
                        )
                        drawCircle(
                            color = Color(0xFF6366F1).copy(alpha = 0.06f),
                            radius = size.minDimension * 0.30f
                        )
                    }
                    Text(
                        text = emoji,
                        fontSize = 76.sp
                    )
                }
            }

            // Top-Right Expand / Fullscreen Button (48dp touch target)
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(48.dp)
                    .clickable { onExpandClick() }
                    .testTag("picture_vocab_expand_btn"),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.78f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.OpenInFull,
                            contentDescription = "Expand Picture",
                            tint = Color.White,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PictureVocabAudioBar(
    isPlaying: Boolean,
    onPlayClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "vocab_waveform")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    val barHeights = remember {
        listOf(
            0.22f, 0.42f, 0.30f, 0.64f, 0.44f,
            0.80f, 0.54f, 0.96f, 0.68f, 0.88f,
            0.48f, 0.76f, 0.58f, 0.40f, 0.62f,
            0.34f, 0.24f
        )
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF818CF8).copy(alpha = 0.65f),
                    Color(0xFF38BDF8).copy(alpha = 0.55f),
                    Color(0xFF6366F1).copy(alpha = 0.65f)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable { onPlayClick() }
            .testTag("picture_vocab_audio_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF10184D).copy(alpha = 0.95f),
                            Color(0xFF0E1B52).copy(alpha = 0.94f),
                            Color(0xFF12184A).copy(alpha = 0.95f)
                        )
                    )
                )
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Glowing Circular Purple Play Button
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFFA855F7), Color(0xFF6366F1))
                        )
                    )
                    .border(
                        1.6.dp,
                        Color(0xFFE9D5FF).copy(alpha = 0.85f),
                        CircleShape
                    )
                    .clickable { onPlayClick() }
                    .testTag("picture_vocab_play_btn"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Rounded.GraphicEq else Icons.Rounded.PlayArrow,
                    contentDescription = "Listen Carefully",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Subtle Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(26.dp)
                    .background(Color.White.copy(alpha = 0.16f))
            )

            // Neon Gradient Audio Waveform
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
            ) {
                val count = barHeights.size
                val barW = (size.width / (count * 2.0f)).coerceIn(2.2.dp.toPx(), 4.dp.toPx())
                val totalW = count * barW
                val gap = ((size.width - totalW) / maxOf(1, count - 1)).coerceAtLeast(2.dp.toPx())
                val waveBrush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFC084FC),
                        Color(0xFF8B5CF6),
                        Color(0xFF38BDF8),
                        Color(0xFF60A5FA)
                    )
                )

                barHeights.forEachIndexed { i, base ->
                    val dynamicScale = if (isPlaying) {
                        val wave = sin(wavePhase + i * 0.55f)
                        (0.65f + 0.35f * wave).coerceIn(0.32f, 1.05f)
                    } else {
                        1f
                    }
                    val h = (size.height * base * dynamicScale).coerceIn(4.dp.toPx(), size.height)
                    val x = i * (barW + gap)
                    val y = (size.height - h) / 2f
                    drawRoundRect(
                        brush = waveBrush,
                        topLeft = Offset(x, y),
                        size = Size(barW, h),
                        cornerRadius = CornerRadius(barW / 2f, barW / 2f),
                        alpha = if (i == 0 || i == count - 1) 0.55f else 0.96f
                    )
                }
            }

            // Subtle Vertical Divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(26.dp)
                    .background(Color.White.copy(alpha = 0.16f))
            )

            // Speaker Icon + "Listen Carefully" Label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E3A8A).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF60A5FA).copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = "Listen Carefully",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE2E8F0),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PictureVocabClueBox(
    clue: String
) {
    val styledClue = remember(clue) { buildHighlightedClue(clue) }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(
            1.15.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF3B82F6).copy(alpha = 0.65f),
                    Color(0xFF38BDF8).copy(alpha = 0.55f),
                    Color(0xFF6366F1).copy(alpha = 0.65f)
                )
            )
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("picture_vocab_clue_box")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF0A1340).copy(alpha = 0.95f),
                            Color(0xFF0E1B54).copy(alpha = 0.92f)
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Glowing Golden Lightbulb Icon
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFFACC15).copy(alpha = 0.45f),
                                    Color(0xFFF59E0B).copy(alpha = 0.14f),
                                    Color.Transparent
                                ),
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.width * 0.72f
                            )
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lightbulb,
                    contentDescription = "Clue Hint",
                    tint = Color(0xFFFDE047),
                    modifier = Modifier.size(26.dp)
                )
            }

            Text(
                text = styledClue,
                fontSize = 13.5.sp,
                lineHeight = 19.sp,
                color = Color(0xFFF1F5F9),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PictureVocabOptionRow(
    label: String,
    index: Int,
    isSelected: Boolean,
    isCorrectAnswer: Boolean,
    verifiedState: Boolean?,
    onClick: () -> Unit
) {
    val isVerifiedCorrect = verifiedState != null && isCorrectAnswer
    val isVerifiedWrong = verifiedState == false && isSelected && !isCorrectAnswer

    val rowBackground = when {
        isVerifiedCorrect -> Brush.horizontalGradient(
            listOf(Color(0xFF065F46).copy(alpha = 0.92f), Color(0xFF047857).copy(alpha = 0.90f))
        )
        isVerifiedWrong -> Brush.horizontalGradient(
            listOf(Color(0xFF7F1D1D).copy(alpha = 0.92f), Color(0xFF991B1B).copy(alpha = 0.88f))
        )
        isSelected -> Brush.horizontalGradient(
            listOf(Color(0xFF0D3C9E), Color(0xFF164EC2), Color(0xFF0E3C9E))
        )
        else -> Brush.horizontalGradient(
            listOf(
                Color(0xFF121E5A).copy(alpha = 0.94f),
                Color(0xFF192970).copy(alpha = 0.92f)
            )
        )
    }

    val rowBorder = when {
        isVerifiedCorrect -> Brush.horizontalGradient(
            listOf(EmeraldSuccess, Color(0xFF6EE7B7))
        )
        isVerifiedWrong -> Brush.horizontalGradient(
            listOf(CoralAccent, Color(0xFFFCA5A5))
        )
        isSelected -> Brush.horizontalGradient(
            listOf(Color(0xFF22D3EE), Color(0xFF38BDF8), Color(0xFF60A5FA))
        )
        else -> Brush.horizontalGradient(
            listOf(
                Color(0xFF4F67BA).copy(alpha = 0.72f),
                Color(0xFF3B53A8).copy(alpha = 0.65f)
            )
        )
    }

    val borderWidth = if (isSelected || isVerifiedCorrect || isVerifiedWrong) 2.dp else 1.2.dp

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.Transparent,
        border = BorderStroke(borderWidth, rowBorder),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 50.dp)
            .then(
                if (isSelected && verifiedState == null) {
                    Modifier.drawBehind {
                        drawRoundRect(
                            color = Color(0xFF22D3EE).copy(alpha = 0.24f),
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        )
                    }
                } else {
                    Modifier
                }
            )
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = verifiedState != true, onClick = onClick)
            .testTag("picture_vocab_option_$index")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBackground)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Circular Radio Ring / Status Indicator
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isVerifiedCorrect -> EmeraldSuccess
                            isVerifiedWrong -> CoralAccent
                            else -> Color.Transparent
                        }
                    )
                    .border(
                        width = if (isSelected) 2.2.dp else 1.6.dp,
                        color = when {
                            isVerifiedCorrect -> Color(0xFF6EE7B7)
                            isVerifiedWrong -> Color(0xFFFCA5A5)
                            isSelected -> Color(0xFF38BDF8)
                            else -> Color(0xFF8CB4F5).copy(alpha = 0.85f)
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isVerifiedCorrect -> {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Correct",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    isVerifiedWrong -> {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Incorrect",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    isSelected -> {
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF38BDF8))
                        )
                    }
                }
            }

            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 15.5.sp,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            if (isVerifiedCorrect) {
                Text(
                    text = "Correct",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6EE7B7)
                )
            } else if (isVerifiedWrong) {
                Text(
                    text = "Try Again",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFCA5A5)
                )
            }
        }
    }
}

@Composable
private fun PictureVocabExpandedDialog(
    @DrawableRes drawableRes: Int?,
    emoji: String,
    clue: String,
    phonetic: String?,
    onPlayAudio: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0A123E),
            border = BorderStroke(
                1.6.dp,
                Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFFA855F7)))
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .testTag("picture_vocab_expanded_dialog")
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Visual Clue Preview",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF162055),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.7f)),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable { onDismiss() }
                            .testTag("picture_vocab_close_preview_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close Preview",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color(0xFFEAF2FF),
                    border = BorderStroke(1.4.dp, Color(0xFF93C5FD)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    if (drawableRes != null) {
                        Image(
                            painter = painterResource(id = drawableRes),
                            contentDescription = "Expanded vocabulary photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 104.sp)
                        }
                    }
                }

                if (!phonetic.isNullOrBlank()) {
                    Text(
                        text = "Phonetic: $phonetic",
                        color = Color(0xFF93C5FD),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Text(
                    text = buildHighlightedClue(clue),
                    color = Color.White,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.Transparent,
                    border = BorderStroke(1.2.dp, Color(0xFF67E8F9)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(50))
                        .clickable { onPlayAudio() }
                        .testTag("picture_vocab_preview_listen_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF9333EA), Color(0xFF3B82F6), Color(0xFF06B6D4))
                                )
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = "Listen to clue",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "Listen Carefully",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PictureVocabEmptyStateCard() {
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
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = null,
                tint = Color(0xFF38BDF8),
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "No Picture Vocabulary challenges found.",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
            Text(
                text = "Teachers can add new picture quizzes from the Teacher Panel.",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
