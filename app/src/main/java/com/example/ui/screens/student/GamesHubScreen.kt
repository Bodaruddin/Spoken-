@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens.student

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.*
import com.example.ui.ActiveGameType
import com.example.ui.MainViewModel
import com.example.ui.components.MicrophoneButton
import com.example.ui.theme.*
import com.example.util.AssameseTranslator
import kotlinx.coroutines.delay

@Composable
fun GamesHubScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeGame by viewModel.activeGame.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshGameChallengesFromCloud()
    }

    val sentenceBuilders by viewModel.sentenceBuilderChallenges.collectAsState()
    val wordMatches by viewModel.wordMatchChallenges.collectAsState()
    val missingLetters by viewModel.missingLettersChallenges.collectAsState()
    val listenChooses by viewModel.listenChooseChallenges.collectAsState()
    val pronunciationBattles by viewModel.pronunciationBattleChallenges.collectAsState()
    val fastVocabs by viewModel.fastVocabularyChallenges.collectAsState()
    val grammarQuestions by viewModel.grammarChallenges.collectAsState()

    if (activeGame != null) {
        ActiveGameRunner(
            gameType = activeGame!!,
            viewModel = viewModel,
            onClose = { viewModel.closeGame() },
            modifier = modifier
        )
    } else {
        // Redesigned English Learning Arcade Hub exactly matching screenshot
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF0F0624),
                            Color(0xFF160935),
                            Color(0xFF0C041E),
                            Color(0xFF050110)
                        )
                    )
                )
        ) {
            // Ambient neon wave lines in background
            ArcadeBackgroundWaves()

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 28.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("arcade_games_grid")
            ) {
                // 1. Hero Banner Card (Spanning 2 columns)
                item(span = { GridItemSpan(2) }) {
                    ArcadeHeroCard()
                }

                // 2. The 8 Arcade Game Cards matching screenshot in exact order
                items(arcadeGameCatalog) { config ->
                    val count = when (config.gameType) {
                        ActiveGameType.WORD_MATCH -> wordMatches.size
                        ActiveGameType.MISSING_LETTERS -> missingLetters.size
                        ActiveGameType.SENTENCE_BUILDER -> sentenceBuilders.size
                        ActiveGameType.LISTEN_CHOOSE -> listenChooses.size
                        ActiveGameType.PRONUNCIATION_BATTLE -> pronunciationBattles.size
                        ActiveGameType.FAST_VOCABULARY -> fastVocabs.size
                        ActiveGameType.GRAMMAR_CHALLENGE -> grammarQuestions.size
                        else -> null
                    }
                    ArcadeGameCard(
                        config = config,
                        challengeCount = count,
                        onClick = { viewModel.launchGame(config.gameType) }
                    )
                }
            }
        }
    }
}

/**
 * Top Hero Banner Card matching screenshot:
 * 🌿 🎮 🌿 English Learning Arcade, stack of books, golden trophy, and controller.
 */
@Composable
fun ArcadeHeroCard(modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(
            1.8.dp,
            Brush.linearGradient(
                listOf(
                    Color(0xFF8B5CF6),
                    Color(0xFF06B6D4),
                    Color(0xFFD946EF),
                    Color(0xFF8B5CF6)
                )
            )
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("arcade_hero_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF1B144E).copy(alpha = 0.95f),
                            Color(0xFF110B38).copy(alpha = 0.98f),
                            Color(0xFF090622).copy(alpha = 0.98f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Column: Title, Subtitle, and Pill Badge
                Column(
                    modifier = Modifier.weight(1.35f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Title row with laurel leaves & glowing gamepad
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("🌿", fontSize = 16.sp)
                        Text("🎮", fontSize = 26.sp)
                        Column {
                            Text(
                                text = "English Learning",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.5.sp,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Arcade",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text("🌿", fontSize = 16.sp)
                            }
                        }
                    }

                    // Subtitle
                    Text(
                        text = "Interactive games created by teachers & academy curriculum to accelerate spoken mastery!",
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )

                    // Pill Badge: [ 🎮 Learn • Play • Improve • Earn XP ]
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF241A5C).copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.85f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("🎮", fontSize = 12.sp)
                            Text(
                                text = "Learn • Play • Improve • Earn XP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDDD6FE),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Right: 3D Illustration Graphic of Golden Trophy, stack of books, and gamepad
                Box(
                    modifier = Modifier
                        .size(126.dp)
                        .clip(RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_arcade_trophy),
                        contentDescription = "Arcade Trophy & Gamepad",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

/**
 * Ambient glowing wave streaks across the background (matches screenshot aesthetic).
 */
@Composable
fun ArcadeBackgroundWaves(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Top-left glowing magenta/violet wave
        val topWave = Path().apply {
            moveTo(-50f, height * 0.08f)
            cubicTo(
                width * 0.25f, height * 0.02f,
                width * 0.6f, height * 0.04f,
                width * 0.95f, -30f
            )
        }
        drawPath(
            path = topWave,
            color = Color(0xFFC084FC).copy(alpha = 0.3f),
            style = Stroke(width = 6.dp.toPx())
        )
        drawPath(
            path = topWave,
            color = Color(0xFF9333EA).copy(alpha = 0.15f),
            style = Stroke(width = 16.dp.toPx())
        )

        // Bottom glowing neon streak
        val bottomWave = Path().apply {
            moveTo(width * 0.1f, height)
            cubicTo(
                width * 0.45f, height * 0.88f,
                width * 0.75f, height * 0.91f,
                width + 60f, height * 0.83f
            )
        }
        drawPath(
            path = bottomWave,
            color = Color(0xFF8B5CF6).copy(alpha = 0.35f),
            style = Stroke(width = 7.dp.toPx())
        )
        drawPath(
            path = bottomWave,
            color = Color(0xFFD946EF).copy(alpha = 0.15f),
            style = Stroke(width = 18.dp.toPx())
        )
    }
}

/**
 * Data specification for each Arcade Game Card matching exact screenshot colors
 */
data class ArcadeGameCardConfig(
    val gameType: ActiveGameType,
    val title: String,
    val desc: String,
    val borderColor: Color,
    val accentColor: Color,
    val glowColor: Color,
    val bgGradients: List<Color>,
    val pillBgColor: Color,
    val iconType: ArcadeIconType
)

enum class ArcadeIconType {
    PUZZLE_MATCH,
    LETTER_BLOCKS,
    NOTEPAD_PENCIL,
    HEADPHONES_SOUND,
    SWORDS_SHIELD,
    PICTURE_FRAME,
    LIGHTNING_BOLT,
    STOPWATCH_TIMER
}

/**
 * Catalog list representing all 8 cards with exact radiant colors from the screenshot.
 */
val arcadeGameCatalog = listOf(
    // Row 1 Left: Word Match (Electric Cyan-Blue)
    ArcadeGameCardConfig(
        gameType = ActiveGameType.WORD_MATCH,
        title = "Word Match",
        desc = "Match English words with correct definitions",
        borderColor = Color(0xFF0084FF),
        accentColor = Color(0xFF0070F3),
        glowColor = Color(0xFF00D2FF),
        bgGradients = listOf(
            Color(0xFF003896),
            Color(0xFF002266),
            Color(0xFF00123D)
        ),
        pillBgColor = Color(0xFF002266).copy(alpha = 0.85f),
        iconType = ArcadeIconType.PUZZLE_MATCH
    ),
    // Row 1 Right: Missing Letters (Luminous Violet-Purple)
    ArcadeGameCardConfig(
        gameType = ActiveGameType.MISSING_LETTERS,
        title = "Missing Letters",
        desc = "Complete the word spelling correctly",
        borderColor = Color(0xFFA855F7),
        accentColor = Color(0xFF9333EA),
        glowColor = Color(0xFFD946EF),
        bgGradients = listOf(
            Color(0xFF4A1A94),
            Color(0xFF2E136E),
            Color(0xFF1E0C4A)
        ),
        pillBgColor = Color(0xFF381072).copy(alpha = 0.85f),
        iconType = ArcadeIconType.LETTER_BLOCKS
    ),
    // Row 2 Left: Sentence Builder (Rich Amber-Bronze Orange)
    ArcadeGameCardConfig(
        gameType = ActiveGameType.SENTENCE_BUILDER,
        title = "Sentence Builder",
        desc = "Arrange words into grammatical syntax",
        borderColor = Color(0xFFF97316),
        accentColor = Color(0xFFEA580C),
        glowColor = Color(0xFFFB923C),
        bgGradients = listOf(
            Color(0xFF633223),
            Color(0xFF421E14),
            Color(0xFF280F08)
        ),
        pillBgColor = Color(0xFF4A1D0B).copy(alpha = 0.85f),
        iconType = ArcadeIconType.NOTEPAD_PENCIL
    ),
    // Row 2 Right: Listen & Choose (Deep Petrol Ocean Teal)
    ArcadeGameCardConfig(
        gameType = ActiveGameType.LISTEN_CHOOSE,
        title = "Listen & Choose",
        desc = "Listen to audio speech & select answer",
        borderColor = Color(0xFF06B6D4),
        accentColor = Color(0xFF0D9488),
        glowColor = Color(0xFF22D3EE),
        bgGradients = listOf(
            Color(0xFF014665),
            Color(0xFF002F44),
            Color(0xFF001A29)
        ),
        pillBgColor = Color(0xFF003046).copy(alpha = 0.85f),
        iconType = ArcadeIconType.HEADPHONES_SOUND
    ),
    // Row 3 Left: Pronunciation Battle (Hot Neon Magenta-Pink)
    ArcadeGameCardConfig(
        gameType = ActiveGameType.PRONUNCIATION_BATTLE,
        title = "Pronunciation Battle",
        desc = "Speak with high phoneme accuracy",
        borderColor = Color(0xFFEC4899),
        accentColor = Color(0xFFD946EF),
        glowColor = Color(0xFFF43F5E),
        bgGradients = listOf(
            Color(0xFF5A155D),
            Color(0xFF3D0E44),
            Color(0xFF260528)
        ),
        pillBgColor = Color(0xFF450D47).copy(alpha = 0.85f),
        iconType = ArcadeIconType.SWORDS_SHIELD
    ),
    // Row 3 Right: Picture Vocabulary (Electric Sapphire Royal Blue)
    ArcadeGameCardConfig(
        gameType = ActiveGameType.PICTURE_VOCAB,
        title = "Picture Vocabulary",
        desc = "Identify everyday objects & actions",
        borderColor = Color(0xFF0084FF),
        accentColor = Color(0xFF2563EB),
        glowColor = Color(0xFF38BDF8),
        bgGradients = listOf(
            Color(0xFF00348A),
            Color(0xFF002268),
            Color(0xFF001242)
        ),
        pillBgColor = Color(0xFF002568).copy(alpha = 0.85f),
        iconType = ArcadeIconType.PICTURE_FRAME
    ),
    // Row 4 Left: Grammar Challenge (Electric Violet-Indigo)
    ArcadeGameCardConfig(
        gameType = ActiveGameType.GRAMMAR_CHALLENGE,
        title = "Grammar Challenge",
        desc = "Rapid-fire tenses and prepositions quiz",
        borderColor = Color(0xFF8B5CF6),
        accentColor = Color(0xFF7C3AED),
        glowColor = Color(0xFFA855F7),
        bgGradients = listOf(
            Color(0xFF320D79),
            Color(0xFF22085A),
            Color(0xFF13043D)
        ),
        pillBgColor = Color(0xFF260960).copy(alpha = 0.85f),
        iconType = ArcadeIconType.LIGHTNING_BOLT
    ),
    // Row 4 Right: Fast Vocabulary (Luminous Emerald-Mint Green)
    ArcadeGameCardConfig(
        gameType = ActiveGameType.FAST_VOCABULARY,
        title = "Fast Vocabulary",
        desc = "30-second rapid word recognition blitz",
        borderColor = Color(0xFF10B981),
        accentColor = Color(0xFF059669),
        glowColor = Color(0xFF34D399),
        bgGradients = listOf(
            Color(0xFF004430),
            Color(0xFF002E20),
            Color(0xFF001A12)
        ),
        pillBgColor = Color(0xFF003324).copy(alpha = 0.85f),
        iconType = ArcadeIconType.STOPWATCH_TIMER
    )
)

/**
 * Individual Arcade Game Card:
 * Features the radiant gradient fill, glowing border, bottom fluid neon wave, and action button.
 */
@Composable
fun ArcadeGameCard(
    config: ArcadeGameCardConfig,
    challengeCount: Int? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.8.dp, config.borderColor),
        modifier = modifier
            .fillMaxWidth()
            .height(168.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .testTag("game_card_${config.gameType.name.lowercase()}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = config.bgGradients
                    )
                )
                .padding(13.dp)
        ) {
            // Luminous bottom-right fluid neon wave matching screenshot
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // Radial corner glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            config.glowColor.copy(alpha = 0.55f),
                            config.glowColor.copy(alpha = 0.22f),
                            Color.Transparent
                        ),
                        center = Offset(w * 0.95f, h * 0.95f),
                        radius = w * 0.70f
                    ),
                    radius = w * 0.70f,
                    center = Offset(w * 0.95f, h * 0.95f)
                )

                // Flowing bottom-right fluid wave shape
                val wave = Path().apply {
                    moveTo(w * 0.30f, h)
                    cubicTo(
                        w * 0.55f, h * 0.82f,
                        w * 0.80f, h * 0.86f,
                        w, h * 0.70f
                    )
                    lineTo(w, h)
                    close()
                }

                drawPath(
                    path = wave,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            config.glowColor.copy(alpha = 0.45f),
                            config.accentColor.copy(alpha = 0.75f)
                        ),
                        startY = h * 0.72f,
                        endY = h
                    )
                )
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Custom Icon + Star XP Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ArcadeGameIcon(config.iconType)

                    // +40 XP Pill matching card theme
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = config.pillBgColor,
                        border = BorderStroke(1.dp, config.borderColor.copy(alpha = 0.75f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text("⭐", fontSize = 10.sp)
                            Text(
                                "+40 XP",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Middle: Title & Description
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = config.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color.White,
                        maxLines = 1,
                        softWrap = false
                    )
                    Text(
                        text = config.desc,
                        fontSize = 11.sp,
                        color = Color(0xFFCBD5E1),
                        lineHeight = 14.5.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom Row: Drill count indicator & circular action button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (challengeCount != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.45f),
                            border = BorderStroke(0.8.dp, config.borderColor.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "🎮 $challengeCount Drills",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    } else {
                        Spacer(Modifier.width(1.dp))
                    }

                    Surface(
                        shape = CircleShape,
                        color = config.accentColor,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Play",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom graphical icons for each of the 8 arcade game cards matching screenshot graphics.
 */
@Composable
fun ArcadeGameIcon(type: ArcadeIconType, modifier: Modifier = Modifier) {
    when (type) {
        ArcadeIconType.PUZZLE_MATCH -> {
            // Two interlocking puzzle pieces (Orange & Cyan)
            Box(modifier = modifier.size(38.dp)) {
                Surface(
                    shape = RoundedCornerShape(topStart = 7.dp, bottomStart = 7.dp, bottomEnd = 7.dp),
                    color = Color(0xFFF59E0B),
                    border = BorderStroke(0.6.dp, Color(0xFFFBBF24)),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomStart)
                ) {}
                Surface(
                    shape = RoundedCornerShape(topEnd = 7.dp, topStart = 7.dp, bottomEnd = 7.dp),
                    color = Color(0xFF0284C7),
                    border = BorderStroke(0.6.dp, Color(0xFF38BDF8)),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                ) {}
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF59E0B))
                        .align(Alignment.Center)
                )
            }
        }

        ArcadeIconType.LETTER_BLOCKS -> {
            // 3 Colorful Letter Cubes: A (Magenta), B (Yellow), C (Cyan)
            Box(modifier = modifier.size(38.dp), contentAlignment = Alignment.Center) {
                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = Color(0xFFD946EF),
                    border = BorderStroke(0.8.dp, Color(0xFFF0ABFC)),
                    modifier = Modifier
                        .size(17.dp)
                        .align(Alignment.TopCenter)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("A", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = Color(0xFFFBBF24),
                    border = BorderStroke(0.8.dp, Color(0xFFFDE68A)),
                    modifier = Modifier
                        .size(17.dp)
                        .align(Alignment.BottomStart)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("B", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = Color(0xFF06B6D4),
                    border = BorderStroke(0.8.dp, Color(0xFF67E8F9)),
                    modifier = Modifier
                        .size(17.dp)
                        .align(Alignment.BottomEnd)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("C", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    }
                }
            }
        }

        ArcadeIconType.NOTEPAD_PENCIL -> {
            // Notepad sheet with angled pencil
            Box(modifier = modifier.size(38.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.CenterStart)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            repeat(4) {
                                Box(
                                    modifier = Modifier
                                        .size(3.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF64748B))
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(Color(0xFF94A3B8).copy(alpha = 0.5f))
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(2.dp)
                                .background(Color(0xFF94A3B8).copy(alpha = 0.5f))
                        )
                    }
                }
                Text(
                    "✏️",
                    fontSize = 18.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                )
            }
        }

        ArcadeIconType.HEADPHONES_SOUND -> {
            // Purple/Cyan headphones with central sound waves
            Box(modifier = modifier.size(38.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Headphones,
                    contentDescription = null,
                    tint = Color(0xFFC084FC),
                    modifier = Modifier.size(34.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.width(2.dp).height(8.dp).background(Color(0xFF38BDF8), RoundedCornerShape(1.dp)))
                    Box(modifier = Modifier.width(2.dp).height(14.dp).background(Color(0xFF67E8F9), RoundedCornerShape(1.dp)))
                    Box(modifier = Modifier.width(2.dp).height(9.dp).background(Color(0xFF38BDF8), RoundedCornerShape(1.dp)))
                }
            }
        }

        ArcadeIconType.SWORDS_SHIELD -> {
            // Crossed swords with golden crest shield
            Box(modifier = modifier.size(38.dp), contentAlignment = Alignment.Center) {
                Text("⚔️", fontSize = 28.sp)
                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = Color(0xFFF59E0B),
                    border = BorderStroke(0.8.dp, Color(0xFFFDE68A)),
                    modifier = Modifier
                        .size(16.dp)
                        .offset(x = 6.dp, y = 4.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🛡️", fontSize = 11.sp)
                    }
                }
            }
        }

        ArcadeIconType.PICTURE_FRAME -> {
            // Landscape picture frame (sky, sun, green hill)
            Surface(
                shape = RoundedCornerShape(9.dp),
                border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
                modifier = modifier.size(34.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0284C7), Color(0xFF38BDF8))
                            )
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFBBF24))
                            .align(Alignment.TopEnd)
                            .offset(x = (-4).dp, y = 4.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 6.dp))
                            .background(Color(0xFF10B981))
                            .align(Alignment.BottomCenter)
                    )
                }
            }
        }

        ArcadeIconType.LIGHTNING_BOLT -> {
            // Glowing yellow lightning bolt
            Box(modifier = modifier.size(36.dp), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.ElectricBolt,
                    contentDescription = null,
                    tint = Color(0xFFFACC15),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        ArcadeIconType.STOPWATCH_TIMER -> {
            // Cyan Stopwatch with top button & ticking hands
            Box(modifier = modifier.size(36.dp), contentAlignment = Alignment.Center) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF042F2C),
                    border = BorderStroke(2.dp, Color(0xFF2DD4BF)),
                    modifier = Modifier.size(30.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF5EEAD4),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(3.dp)
                        .background(Color(0xFF2DD4BF), RoundedCornerShape(1.dp))
                        .align(Alignment.TopCenter)
                        .offset(y = 1.dp)
                )
            }
        }
    }
}

@Composable
fun ActiveGameRunner(
    gameType: ActiveGameType,
    viewModel: MainViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept system Back button so student returns to catalog instead of exiting app
    // (SentenceBuilderGame manages its own 2-level BackHandler internally)
    if (gameType != ActiveGameType.SENTENCE_BUILDER) {
        BackHandler {
            onClose()
        }
    }

    if (gameType == ActiveGameType.SENTENCE_BUILDER) {
        SentenceBuilderGame(
            viewModel = viewModel,
            onClose = onClose,
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Game Header Bar (Custom integrated hero banner is rendered inside PronunciationBattleGame)
            if (gameType != ActiveGameType.PRONUNCIATION_BATTLE) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(gameType.emoji, fontSize = 28.sp)
                        Column {
                            Text(gameType.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                            Text("Interactive Learning Challenge", fontSize = 11.sp, color = MutedGray)
                        }
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_game_button")
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = "Close Game")
                    }
                }
            }

            // Dedicated games with real, validated logic
            when (gameType) {
                ActiveGameType.WORD_MATCH -> WordMatchGame(viewModel)
                ActiveGameType.MISSING_LETTERS -> MissingLettersGame(viewModel)
                ActiveGameType.SENTENCE_BUILDER -> SentenceBuilderGame(viewModel, onClose)
                ActiveGameType.LISTEN_CHOOSE -> ListenAndChooseGame(viewModel)
                ActiveGameType.PRONUNCIATION_BATTLE -> PronunciationBattleGame(viewModel, onClose)
                ActiveGameType.FAST_VOCABULARY -> FastVocabularyGame(viewModel)
                ActiveGameType.GRAMMAR_CHALLENGE -> GrammarChallengeGame(viewModel)
                ActiveGameType.PICTURE_VOCAB -> PictureVocabGame(viewModel)
                ActiveGameType.SPEAK_30_SECONDS -> Speak30SecondsGame(viewModel)
                ActiveGameType.STORY_BUILDER -> StoryBuilderGame(viewModel)
                ActiveGameType.CONVERSATION_CHALLENGE -> ConversationChallengeGame(viewModel)
                ActiveGameType.SPELLING_CHALLENGE -> SpellingBeeGame(viewModel)
            }
        }
    }
}

// =========================================================================
// 1. WORD MATCH GAME - Correct two-column matching logic
// =========================================================================
@Composable
fun WordMatchGame(viewModel: MainViewModel) {
    val challenges by viewModel.wordMatchChallenges.collectAsState()
    var roundOffset by remember { mutableIntStateOf(0) }

    // Take 4 challenges per round
    val activeBatch = remember(challenges, roundOffset) {
        val dropCount = (roundOffset * 4) % maxOf(1, challenges.size)
        val batch = challenges.drop(dropCount).take(4)
        if (batch.isEmpty() && challenges.isNotEmpty()) challenges.take(4) else batch
    }

    // Shuffled words & definitions
    val wordsList = remember(activeBatch) {
        activeBatch.map { it.id to it.word }.shuffled()
    }
    val defsList = remember(activeBatch) {
        activeBatch.map { it.id to it.definition }.shuffled()
    }

    var selectedWordId by remember(activeBatch) { mutableStateOf<String?>(null) }
    var selectedDefId by remember(activeBatch) { mutableStateOf<String?>(null) }
    var matchedIds by remember(activeBatch) { mutableStateOf(setOf<String>()) }
    var mismatchWordId by remember(activeBatch) { mutableStateOf<String?>(null) }
    var mismatchDefId by remember(activeBatch) { mutableStateOf<String?>(null) }
    var hasAwarded by remember(activeBatch) { mutableStateOf(false) }

    // Clear mismatch highlighting after a brief pause
    LaunchedEffect(mismatchWordId, mismatchDefId) {
        if (mismatchWordId != null || mismatchDefId != null) {
            delay(900)
            mismatchWordId = null
            mismatchDefId = null
        }
    }

    // Check match whenever both sides are selected
    LaunchedEffect(selectedWordId, selectedDefId) {
        val wId = selectedWordId
        val dId = selectedDefId
        if (wId != null && dId != null) {
            if (wId == dId) {
                // MATCH!
                matchedIds = matchedIds + wId
                val matchedWord = activeBatch.find { it.id == wId }?.word
                if (matchedWord != null) {
                    viewModel.speakAudio(matchedWord)
                }
                selectedWordId = null
                selectedDefId = null
                if (matchedIds.size == activeBatch.size && !hasAwarded && activeBatch.isNotEmpty()) {
                    hasAwarded = true
                    viewModel.rewardGameWin(50, 15)
                }
            } else {
                // Mismatch
                mismatchWordId = wId
                mismatchDefId = dId
                selectedWordId = null
                selectedDefId = null
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Match Words with Definitions:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    "${matchedIds.size}/${activeBatch.size} Matched",
                    fontSize = 12.sp,
                    color = RoyalBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                "Tap a word on the left, then tap its corresponding definition on the right.",
                fontSize = 12.sp,
                color = MutedGray
            )

            // Split into two interactive columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Left Column: Words
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Words", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalBlue)
                    wordsList.forEach { (id, word) ->
                        val isMatched = matchedIds.contains(id)
                        val isSelected = selectedWordId == id
                        val isMismatch = mismatchWordId == id

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isMatched -> Color(0xFFDCFCE7)
                                isMismatch -> Color(0xFFFEE2E2)
                                isSelected -> Color(0xFFDBEAFE)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = BorderStroke(
                                1.5.dp,
                                when {
                                    isMatched -> EmeraldSuccess
                                    isMismatch -> CoralAccent
                                    isSelected -> RoyalBlue
                                    else -> Color.Transparent
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isMatched) {
                                    if (selectedWordId == id) {
                                        selectedWordId = null
                                    } else {
                                        selectedWordId = id
                                        viewModel.speakAudio(word)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = word,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = when {
                                        isMatched -> Color(0xFF166534)
                                        isMismatch -> Color(0xFF991B1B)
                                        isSelected -> RoyalBlue
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                if (isMatched) {
                                    Text("✓", color = EmeraldSuccess, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                } else {
                                    IconButton(
                                        onClick = { viewModel.speakAudio(word) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.VolumeUp,
                                            contentDescription = "Pronounce",
                                            tint = RoyalBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Right Column: Definitions
                Column(
                    modifier = Modifier.weight(1.3f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Definitions", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalBlue)
                    defsList.forEach { (id, def) ->
                        val isMatched = matchedIds.contains(id)
                        val isSelected = selectedDefId == id
                        val isMismatch = mismatchDefId == id

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isMatched -> Color(0xFFDCFCE7)
                                isMismatch -> Color(0xFFFEE2E2)
                                isSelected -> Color(0xFFDBEAFE)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = BorderStroke(
                                1.5.dp,
                                when {
                                    isMatched -> EmeraldSuccess
                                    isMismatch -> CoralAccent
                                    isSelected -> RoyalBlue
                                    else -> Color.Transparent
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isMatched) {
                                    selectedDefId = if (selectedDefId == id) null else id
                                }
                        ) {
                            Text(
                                text = def,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = when {
                                    isMatched -> Color(0xFF166534)
                                    isMismatch -> Color(0xFF991B1B)
                                    isSelected -> RoyalBlue
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp)
                            )
                        }
                    }
                }
            }

            // Feedback Message
            if (mismatchWordId != null || mismatchDefId != null) {
                Text(
                    "❌ Not a match! Read definitions carefully and try again.",
                    fontSize = 12.sp,
                    color = CoralAccent,
                    fontWeight = FontWeight.Bold
                )
            }

            // Round Complete Banner
            if (matchedIds.size == activeBatch.size && activeBatch.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, EmeraldSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🎉", fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Round Completed!", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF166534))
                            Text("+50 XP & +15 Coins added to your profile", fontSize = 12.sp, color = Color(0xFF15803D))
                        }
                    }
                }

                Button(
                    onClick = {
                        selectedWordId = null
                        selectedDefId = null
                        matchedIds = emptySet()
                        hasAwarded = false
                        roundOffset = (roundOffset + 1) % maxOf(1, (challenges.size + 3) / 4)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Play Next Round ➡️")
                }
            }
        }
    }
}

// =========================================================================
// 2. MISSING LETTERS GAME - Proper undoable letter slots and choice pool
// =========================================================================
private data class LetterChoice(val id: Int, val char: Char, var isUsed: Boolean = false)

@Composable
fun MissingLettersGame(viewModel: MainViewModel) {
    val challenges by viewModel.missingLettersChallenges.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }

    // Collect all available categories dynamically from existing challenges
    val allCategories = remember(challenges) {
        val cats = challenges.map { it.category.trim() }.filter { it.isNotBlank() }
        (listOf("All", "Animals", "Vegetables", "Fruits", "General") + cats).distinct()
    }

    val filteredChallenges = remember(challenges, selectedCategory) {
        if (selectedCategory == "All") {
            challenges
        } else {
            challenges.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    var challengeIndex by remember(selectedCategory) { mutableIntStateOf(0) }
    val current = filteredChallenges.getOrNull(challengeIndex % maxOf(1, filteredChallenges.size))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Category Selector Chips Row
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Select Word Category:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalBlue)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allCategories.forEach { cat ->
                    val isSelected = selectedCategory.equals(cat, ignoreCase = true)
                    val emoji = when (cat.lowercase()) {
                        "all" -> "🌟"
                        "animals" -> "🐾"
                        "vegetables" -> "🥦"
                        "fruits" -> "🍎"
                        "general" -> "📚"
                        "objects" -> "📦"
                        "grammar" -> "⚡"
                        else -> "🏷️"
                    }
                    val count = if (cat == "All") challenges.size else challenges.count { it.category.equals(cat, ignoreCase = true) }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                        border = BorderStroke(1.dp, if (isSelected) RoyalBlue else Color.Transparent),
                        modifier = Modifier.clickable {
                            selectedCategory = cat
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(emoji, fontSize = 14.sp)
                            Text(
                                text = "$cat ($count)",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        if (current == null || filteredChallenges.isEmpty()) {
            // Empty State
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🏷️", fontSize = 36.sp)
                    Text("No words in category '$selectedCategory' yet", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Teachers can add new words to this category from the Teacher Panel.", fontSize = 12.sp, color = MutedGray, textAlign = TextAlign.Center)
                    Button(
                        onClick = { selectedCategory = "All" },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("View All Categories")
                    }
                }
            }
        } else {
            val targetWord = remember(current.id) { current.fullWord.trim().uppercase() }
            val missingIndices = remember(current.id) {
                current.missingIndices.filter { it in targetWord.indices }.toSet()
            }

            // Letter choices: missing letters + distractors (normalized uppercase)
            val choices = remember(current.id) {
                val missingChars = missingIndices.map { targetWord[it] }
                val alphabetDistractors = listOf('A', 'E', 'I', 'O', 'U', 'R', 'S', 'T', 'L', 'N', 'D', 'C')
                    .filterNot { missingChars.contains(it) }
                    .shuffled()
                    .take(4)
                val combined = (missingChars + alphabetDistractors).shuffled()
                combined.mapIndexed { idx, ch -> LetterChoice(idx, ch, false) }
            }

            // Slots state: maps slotIndex -> LetterChoice? placed into it
            var placedSlots by remember(current.id) {
                mutableStateOf(missingIndices.associateWith { null as LetterChoice? })
            }
            var choicePool by remember(current.id) { mutableStateOf(choices) }
            var isVerified by remember(current.id) { mutableStateOf<Boolean?>(null) }
            var hasAwarded by remember(current.id) { mutableStateOf(false) }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Complete Spelling:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEFF6FF)
                            ) {
                                Text(
                                    text = current.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            "Word ${(challengeIndex % filteredChallenges.size) + 1}/${filteredChallenges.size}",
                            fontSize = 12.sp,
                            color = RoyalBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Clue Card with High-Contrast Text
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0F1E42),
                        border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("💡 Clue & Meaning:", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF60A5FA))
                            Text(
                                text = current.hint,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }

                    // Word Letter Slots (Adaptive width & wrapping FlowRow so long words never clip)
                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val count = targetWord.length
                        val spacing = if (count > 9) 3.dp else 5.dp
                        val computedSize = ((maxWidth - (spacing * (count - 1))) / count).coerceIn(24.dp, 38.dp)
                        val fontSize = when {
                            computedSize < 28.dp -> 13.sp
                            computedSize < 34.dp -> 15.sp
                            else -> 17.sp
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            targetWord.forEachIndexed { idx, fixedChar ->
                                val isMissing = missingIndices.contains(idx)
                                val placedChoice = placedSlots[idx]

                                Surface(
                                    shape = RoundedCornerShape(if (computedSize < 30.dp) 6.dp else 8.dp),
                                    color = when {
                                        !isMissing -> MaterialTheme.colorScheme.surfaceVariant
                                        placedChoice != null -> Color(0xFFDBEAFE)
                                        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                    },
                                    border = BorderStroke(
                                        1.5.dp,
                                        when {
                                            isVerified == true -> EmeraldSuccess
                                            isVerified == false -> CoralAccent
                                            isMissing -> RoyalBlue
                                            else -> Color.Transparent
                                        }
                                    ),
                                    modifier = Modifier
                                        .size(computedSize)
                                        .clickable(enabled = isMissing && placedChoice != null) {
                                            // Remove placed letter and restore into choice pool
                                            if (placedChoice != null) {
                                                val updatedSlots = placedSlots.toMutableMap()
                                                updatedSlots[idx] = null
                                                placedSlots = updatedSlots

                                                choicePool = choicePool.map {
                                                    if (it.id == placedChoice.id) it.copy(isUsed = false) else it
                                                }
                                                isVerified = null
                                            }
                                        }
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        val charText = when {
                                            !isMissing -> fixedChar.toString()
                                            placedChoice != null -> placedChoice.char.toString()
                                            else -> "_"
                                        }
                                        Text(
                                            text = charText,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = fontSize,
                                            color = when {
                                                isVerified == true -> EmeraldSuccess
                                                isVerified == false && placedChoice != null -> CoralAccent
                                                isMissing && placedChoice != null -> RoyalBlue
                                                isMissing -> MutedGray
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text("Tap letters below to fill blanks (tap a placed letter to undo):", fontSize = 11.sp, color = MutedGray)

                    // Letter Choice Bank
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        choicePool.forEach { choice ->
                            val isUsed = choice.isUsed
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isUsed) MaterialTheme.colorScheme.surfaceVariant else RoyalBlue,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clickable(enabled = !isUsed) {
                                        // Find first empty missing slot
                                        val firstEmptySlot = missingIndices.sorted().firstOrNull { placedSlots[it] == null }
                                        if (firstEmptySlot != null) {
                                            val updatedSlots = placedSlots.toMutableMap()
                                            updatedSlots[firstEmptySlot] = choice
                                            placedSlots = updatedSlots

                                            choicePool = choicePool.map {
                                                if (it.id == choice.id) it.copy(isUsed = true) else it
                                            }
                                            isVerified = null
                                        }
                                    }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = choice.char.toString(),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = if (isUsed) MutedGray else Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Action Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                // Reset all slots and choice pool
                                placedSlots = missingIndices.associateWith { null }
                                choicePool = choicePool.map { it.copy(isUsed = false) }
                                isVerified = null
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Clear All")
                        }

                        Button(
                            onClick = {
                                // Check if all slots are filled
                                val allFilled = missingIndices.all { placedSlots[it] != null }
                                if (!allFilled) {
                                    isVerified = false
                                    return@Button
                                }
                                val assembled = targetWord.mapIndexed { idx, ch ->
                                    if (missingIndices.contains(idx)) {
                                        placedSlots[idx]?.char ?: ' '
                                    } else {
                                        ch
                                    }
                                }.joinToString("")

                                if (assembled.equals(targetWord, ignoreCase = true)) {
                                    isVerified = true
                                    if (!hasAwarded) {
                                        hasAwarded = true
                                        viewModel.rewardGameWin(40, 15)
                                    }
                                    viewModel.speakAudio(targetWord)
                                } else {
                                    isVerified = false
                                }
                            },
                            modifier = Modifier.weight(1.3f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Text("Verify Spelling")
                        }
                    }

                    // Verification Feedback
                    isVerified?.let { correct ->
                        Text(
                            text = if (correct) "🎉 Correct! Perfect spelling." else "❌ Incorrect letter placed. Tap any placed tile to remove and retry!",
                            color = if (correct) EmeraldSuccess else CoralAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (isVerified == true) {
                        Button(
                            onClick = {
                                challengeIndex = (challengeIndex + 1) % filteredChallenges.size
                                isVerified = null
                                hasAwarded = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Next Word Challenge ➡️")
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. SENTENCE BUILDER GAME - Choose a Challenge Page + Pack-Scoped Gameplay
// =========================================================================
private data class SentenceWordChip(val id: Int, val text: String)

private data class SentenceChallengeCardStyle(
    val gradientColors: List<Color>,
    val borderColor: Color,
    val badgeBgColor: Color,
    val starCount: Int
)

private fun resolveChallengeCardStyle(pack: SentenceBuilderPack, index: Int): SentenceChallengeCardStyle {
    return when (pack.difficulty.trim().lowercase()) {
        "easy" -> SentenceChallengeCardStyle(
            gradientColors = listOf(Color(0xFF159957), Color(0xFF0A5C36)),
            borderColor = Color(0xFF4ADE80),
            badgeBgColor = Color(0xFF0B4629),
            starCount = 1
        )
        "medium" -> SentenceChallengeCardStyle(
            gradientColors = listOf(Color(0xFF0284C7), Color(0xFF0A3D82)),
            borderColor = Color(0xFF38BDF8),
            badgeBgColor = Color(0xFF082F63),
            starCount = 2
        )
        "hard" -> SentenceChallengeCardStyle(
            gradientColors = listOf(Color(0xFF8B2CF5), Color(0xFF4C1D95)),
            borderColor = Color(0xFFD8B4FE),
            badgeBgColor = Color(0xFF3B1373),
            starCount = 3
        )
        else -> when (index % 3) {
            0 -> SentenceChallengeCardStyle(
                gradientColors = listOf(Color(0xFF0D9488), Color(0xFF115E59)),
                borderColor = Color(0xFF5EEAD4),
                badgeBgColor = Color(0xFF083D3A),
                starCount = 1
            )
            1 -> SentenceChallengeCardStyle(
                gradientColors = listOf(Color(0xFFD97706), Color(0xFF78350F)),
                borderColor = Color(0xFFFCD34D),
                badgeBgColor = Color(0xFF59280B),
                starCount = 2
            )
            else -> SentenceChallengeCardStyle(
                gradientColors = listOf(Color(0xFFDB2777), Color(0xFF831843)),
                borderColor = Color(0xFFF9A8D4),
                badgeBgColor = Color(0xFF5E1032),
                starCount = 3
            )
        }
    }
}

@Composable
fun SentenceBuilderGame(
    viewModel: MainViewModel,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val packs by viewModel.sentenceBuilderPacks.collectAsState()
    val allChallenges by viewModel.sentenceBuilderChallenges.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()

    var activePlayingPackId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(packs) {
        if (activePlayingPackId != null && packs.none { it.id == activePlayingPackId }) {
            activePlayingPackId = null
        }
    }

    BackHandler {
        if (activePlayingPackId != null) {
            activePlayingPackId = null
        } else {
            onClose()
        }
    }

    val activePack = remember(packs, activePlayingPackId) {
        packs.firstOrNull { it.id == activePlayingPackId }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF10042B),
                        Color(0xFF1D0B4F),
                        Color(0xFF130638),
                        Color(0xFF0A021D)
                    )
                )
            )
    ) {
        // Ambient cosmic wave & orb backdrop
        ArcadeBackgroundWaves()

        if (activePack == null) {
            // =========================================================
            // PAGE 1: CHOOSE A CHALLENGE SELECTION SCREEN
            // =========================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Bar: Back Button, Title, XP Pill
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
                            color = Color(0xFF28175A),
                            border = BorderStroke(1.2.dp, Color(0xFF7C3AED).copy(alpha = 0.7f)),
                            modifier = Modifier
                                .size(48.dp)
                                .clickable { onClose() }
                                .testTag("sb_back_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                    contentDescription = "Back to Games Hub",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Extension,
                                contentDescription = null,
                                tint = Color(0xFFFACC15),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Sentence Builder",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                color = Color.White
                            )
                        }
                    }

                    // XP Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1F1147),
                        border = BorderStroke(1.2.dp, Color(0xFFFACC15).copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Text("⭐", fontSize = 13.sp)
                            Text(
                                text = "${studentProfile.xpPoints} XP",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                // Hero Banner: Mascot, Trophy, Ribbon & Choose a Challenge Pill
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = Color(0xFF281259),
                            border = BorderStroke(1.5.dp, Color(0xFFA855F7).copy(alpha = 0.7f)),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.app_logo),
                                contentDescription = "Speakly Owl Mascot",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Image(
                            painter = painterResource(R.drawable.img_arcade_trophy),
                            contentDescription = "Challenge Trophy",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(76.dp)
                        )
                    }

                    Spacer(Modifier.height(6.dp))

                    // Ribbon Banner
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF9333EA),
                                        Color(0xFFC026D3),
                                        Color(0xFF7E22CE)
                                    )
                                )
                            )
                            .border(1.5.dp, Color(0xFFF0ABFC).copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sentence Builder",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }

                    // Overlapping Golden Pill "Choose a Challenge"
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFACC15),
                        border = BorderStroke(1.5.dp, Color(0xFFFEF08A)),
                        modifier = Modifier
                            .offset(y = (-8).dp)
                            .testTag("sb_choose_challenge_badge")
                    ) {
                        Text(
                            text = "Choose a Challenge",
                            color = Color(0xFF1E103C),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 5.dp)
                        )
                    }

                    Text(
                        text = "Tap any challenge to start building sentences!",
                        fontSize = 13.sp,
                        color = Color(0xFFE2E8F0),
                        textAlign = TextAlign.Center
                    )
                }

                // Challenge Selection Cards (Direct 1-tap launch into Sentence Builder)
                if (packs.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A103D)),
                        border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🧩", fontSize = 36.sp)
                            Text(
                                "No Sentence Challenges Created Yet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                "Teachers can add challenges and sentences from the Teacher Panel.",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .testTag("sb_challenge_packs_list")
                    ) {
                        packs.forEachIndexed { index, pack ->
                            val sentencesCount = allChallenges.count { it.packId == pack.id }
                            val style = resolveChallengeCardStyle(pack, index)

                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = Color.Transparent,
                                border = BorderStroke(
                                    width = 1.6.dp,
                                    color = style.borderColor.copy(alpha = 0.85f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable { activePlayingPackId = pack.id }
                                    .testTag("sb_pack_card_${pack.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            Brush.horizontalGradient(colors = style.gradientColors)
                                        )
                                        .padding(horizontal = 14.dp, vertical = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Left Star Badge
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = style.badgeBgColor,
                                        border = BorderStroke(1.2.dp, style.borderColor.copy(alpha = 0.75f)),
                                        modifier = Modifier.size(width = 76.dp, height = 58.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                repeat(style.starCount) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Star,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFACC15),
                                                        modifier = Modifier.size(if (style.starCount == 3) 18.dp else 21.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    // Center Title, Description, Badges
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Text(
                                            text = pack.title,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = pack.description,
                                            fontSize = 12.sp,
                                            color = Color.White.copy(alpha = 0.88f),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            lineHeight = 16.sp
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(top = 2.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFF0A1026).copy(alpha = 0.55f),
                                                border = BorderStroke(0.8.dp, Color.White.copy(alpha = 0.25f))
                                            ) {
                                                Text(
                                                    text = "🎯 $sentencesCount ${if (sentencesCount == 1) "Sentence" else "Sentences"}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = Color(0xFF0A1026).copy(alpha = 0.55f),
                                                border = BorderStroke(0.8.dp, Color(0xFFFACC15).copy(alpha = 0.45f))
                                            ) {
                                                Text(
                                                    text = "⚡ +${pack.xpReward} XP",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFDE047),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Right Direct Launch Arrow Badge
                                    Surface(
                                        shape = CircleShape,
                                        color = style.badgeBgColor.copy(alpha = 0.9f),
                                        border = BorderStroke(1.2.dp, style.borderColor.copy(alpha = 0.8f)),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                                contentDescription = "Start ${pack.title}",
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
            }
        } else {
            // =========================================================
            // PAGE 2: PLAYING SENTENCES INSIDE SELECTED CHALLENGE
            // =========================================================
            val packChallenges = remember(allChallenges, activePack.id) {
                allChallenges.filter { it.packId == activePack.id }
            }
            var challengeIndex by remember(activePack.id) { mutableIntStateOf(0) }
            var completedSentenceIds by remember(activePack.id) { mutableStateOf(setOf<String>()) }
            val safeIndex = if (packChallenges.isNotEmpty()) challengeIndex.coerceIn(0, packChallenges.lastIndex) else 0
            val current = packChallenges.getOrNull(safeIndex)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header with Back to Challenges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF28175A),
                        border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.7f)),
                        modifier = Modifier
                            .defaultMinSize(minHeight = 48.dp)
                            .clickable { activePlayingPackId = null }
                            .testTag("sb_back_to_challenges_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back to Challenges",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Challenges",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f).padding(horizontal = 8.dp)
                    ) {
                        Text(
                            text = activePack.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${packChallenges.size} ${if (packChallenges.size == 1) "Sentence" else "Sentences"} • ${activePack.difficulty}",
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1F1147),
                        border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.55f))
                    ) {
                        Text(
                            text = "⚡ +${activePack.xpReward} XP",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFDE047),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                if (current == null) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("📝 No Sentences in This Challenge", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(
                                "Ask your teacher to add sentences to '${activePack.title}', or select another challenge.",
                                fontSize = 12.sp,
                                color = MutedGray,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { activePlayingPackId = null },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Choose Another Challenge")
                            }
                        }
                    }
                } else {
                    // Tokenize target sentence with unique IDs to handle duplicate words cleanly
                    val initialTokens = remember(current.id) {
                        current.targetSentence.trim().split(Regex("\\s+")).mapIndexed { index, word ->
                            SentenceWordChip(index, word)
                        }
                    }
                    val shuffledChips = remember(current.id) { initialTokens.shuffled() }

                    var selectedChips by remember(current.id) { mutableStateOf(listOf<SentenceWordChip>()) }
                    var isCorrect by remember(current.id) { mutableStateOf<Boolean?>(null) }
                    var hasAwarded by remember(current.id) { mutableStateOf(false) }

                    val remainingChips = remember(shuffledChips, selectedChips) {
                        val selectedIds = selectedChips.map { it.id }.toSet()
                        shuffledChips.filterNot { selectedIds.contains(it.id) }
                    }

                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Assemble Sentence Syntax:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        "Sentence ${safeIndex + 1}/${packChallenges.size}",
                                        fontSize = 12.sp,
                                        color = RoyalBlue,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (current.createdBy.isNotBlank() && current.createdBy != "System") {
                                        Text(
                                            "By ${current.createdBy}",
                                            fontSize = 10.sp,
                                            color = CoralAccent,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }

                            // Hint Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F1E42),
                                border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    "💡 Hint: ${current.hint}",
                                    fontSize = 12.sp,
                                    color = Color(0xFF93C5FD),
                                    modifier = Modifier.padding(12.dp)
                                )
                            }

                            // Assembled Sentence Tray
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Your Assembled Sentence:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MutedGray)
                                    if (selectedChips.isNotEmpty()) {
                                        TextButton(
                                            onClick = {
                                                selectedChips = emptyList()
                                                isCorrect = null
                                            },
                                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("Clear All", fontSize = 11.sp, color = CoralAccent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                    border = BorderStroke(
                                        1.5.dp,
                                        when {
                                            isCorrect == true -> EmeraldSuccess
                                            isCorrect == false -> CoralAccent
                                            selectedChips.isNotEmpty() -> RoyalBlue
                                            else -> MaterialTheme.colorScheme.outlineVariant
                                        }
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 76.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        if (selectedChips.isEmpty()) {
                                            Text("Tap the word chips below in grammatical order...", fontSize = 13.sp, color = MutedGray)
                                        } else {
                                            FlowRow(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                selectedChips.forEachIndexed { index, chip ->
                                                    Surface(
                                                        shape = RoundedCornerShape(10.dp),
                                                        color = RoyalBlue,
                                                        modifier = Modifier.clickable {
                                                            selectedChips = selectedChips.filterIndexed { i, _ -> i != index }
                                                            isCorrect = null
                                                        }
                                                    ) {
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                        ) {
                                                            Text(
                                                                text = chip.text,
                                                                color = Color.White,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 13.sp
                                                            )
                                                            Spacer(Modifier.width(4.dp))
                                                            Icon(
                                                                Icons.Filled.Close,
                                                                contentDescription = "Remove",
                                                                tint = Color.White.copy(alpha = 0.8f),
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Available Word Chips
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Available Words:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                if (remainingChips.isEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            "✓ All words placed! Tap 'Verify Sentence Syntax' below.",
                                            fontSize = 12.sp,
                                            color = MutedGray,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(10.dp)
                                        )
                                    }
                                } else {
                                    FlowRow(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        remainingChips.forEach { chip ->
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = Color(0xFF0F1E42),
                                                border = BorderStroke(1.2.dp, RoyalBlue),
                                                modifier = Modifier.clickable {
                                                    selectedChips = selectedChips + chip
                                                    isCorrect = null
                                                    viewModel.speakAudio(chip.text)
                                                }
                                            ) {
                                                Text(
                                                    text = chip.text,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Verify Button
                            Button(
                                onClick = {
                                    val constructed = selectedChips.joinToString(" ") { it.text }
                                    fun normalize(s: String) = s.replace(Regex("[.,!?;:]"), "").trim().lowercase()
                                    if (normalize(constructed) == normalize(current.targetSentence)) {
                                        isCorrect = true
                                        completedSentenceIds = completedSentenceIds + current.id
                                        if (!hasAwarded) {
                                            hasAwarded = true
                                            viewModel.rewardGameWin(activePack.xpReward, 20)
                                        }
                                        viewModel.speakAudio(current.targetSentence)
                                    } else {
                                        isCorrect = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                            ) {
                                Text("Verify Sentence Syntax")
                            }

                            isCorrect?.let { correct ->
                                if (!correct) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFEE2E2),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "❌ Not quite right yet. Tap words to remove from tray and re-arrange into correct syntax.",
                                            color = CoralAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }
                            }

                            // Assamese Translation & Navigation Buttons
                            if (isCorrect == true) {
                                val translation = remember(current.id) {
                                    AssameseTranslator.translate(current.targetSentence, current.assameseTranslation)
                                }

                                Card(
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C241E)),
                                    border = BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.5f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text(
                                            "অসমীয়া অনুবাদ (Assamese Translation):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF86EFAC)
                                        )
                                        Text(
                                            text = translation.assameseText,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )

                                        Text(
                                            "শব্দার্থ (Word Meanings):",
                                            fontSize = 11.sp,
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
                                                    border = BorderStroke(0.8.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
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

                                val isLastSentence = safeIndex >= packChallenges.lastIndex
                                if (isLastSentence && completedSentenceIds.size >= packChallenges.size) {
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color(0xFF1E1B4B),
                                        border = BorderStroke(1.2.dp, Color(0xFFFACC15).copy(alpha = 0.65f)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(14.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                "🏆 ${activePack.title} Completed!",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 15.sp,
                                                color = Color(0xFFFDE047)
                                            )
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        challengeIndex = 0
                                                        completedSentenceIds = emptySet()
                                                        selectedChips = emptyList()
                                                        isCorrect = null
                                                        hasAwarded = false
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Play Again")
                                                }
                                                Button(
                                                    onClick = { activePlayingPackId = null },
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text("Other Challenges")
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            challengeIndex = (safeIndex + 1) % packChallenges.size
                                            selectedChips = emptyList()
                                            isCorrect = null
                                            hasAwarded = false
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text("Next Sentence ➡️", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 4. LISTEN & CHOOSE GAME - Guarded single-award with answer reveal
// =========================================================================
@Composable
fun ListenAndChooseGame(viewModel: MainViewModel) {
    val challenges by viewModel.listenChooseChallenges.collectAsState()
    var challengeIndex by remember { mutableIntStateOf(0) }
    val current = challenges.getOrNull(challengeIndex) ?: return

    var selectedOption by remember(current.id) { mutableStateOf<Int?>(null) }
    var hasAwarded by remember(current.id) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Listening Comprehension Drill", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    "${challengeIndex + 1}/${challenges.size}",
                    fontSize = 12.sp,
                    color = RoyalBlue,
                    fontWeight = FontWeight.Bold
                )
            }

            // Audio Player Controls
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { viewModel.speakAudio(current.audioScript, slow = false) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Icon(Icons.Rounded.VolumeUp, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Play Audio 🔊", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.speakAudio(current.audioScript, slow = true) },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("🐢 Slow 0.75x", fontSize = 12.sp)
                }
            }

            Text(
                text = current.question,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Multiple Choice Options
            current.options.forEachIndexed { index, opt ->
                val isSelected = selectedOption == index
                val isCorrectAnswer = index == current.correctIndex
                val hasGuessed = selectedOption != null

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        hasGuessed && isCorrectAnswer -> Color(0xFFDCFCE7)
                        isSelected && !isCorrectAnswer -> Color(0xFFFEE2E2)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.5.dp,
                        when {
                            hasGuessed && isCorrectAnswer -> EmeraldSuccess
                            isSelected && !isCorrectAnswer -> CoralAccent
                            else -> Color.Transparent
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = selectedOption == null) {
                            selectedOption = index
                            if (index == current.correctIndex && !hasAwarded) {
                                hasAwarded = true
                                viewModel.rewardGameWin(40, 15)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = opt,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = when {
                                hasGuessed && isCorrectAnswer -> Color(0xFF166534)
                                isSelected && !isCorrectAnswer -> Color(0xFF991B1B)
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (hasGuessed && isCorrectAnswer) {
                            Text("✓ Correct", color = Color(0xFF166534), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else if (isSelected && !isCorrectAnswer) {
                            Text("✗ Incorrect", color = CoralAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            if (selectedOption != null) {
                Button(
                    onClick = {
                        challengeIndex = (challengeIndex + 1) % challenges.size
                        selectedOption = null
                        hasAwarded = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Next Question ➡️")
                }
            }
        }
    }
}

// =========================================================================
// 5. PRONUNCIATION BATTLE GAME - Evaluates & rewards speech with score reset
// =========================================================================
@Composable
fun PronunciationBattleGame(
    viewModel: MainViewModel,
    onClose: () -> Unit = {}
) {
    val challenges by viewModel.pronunciationBattleChallenges.collectAsState()
    var challengeIndex by remember { mutableIntStateOf(0) }
    val current = challenges.getOrNull(challengeIndex) ?: return

    val speechState by viewModel.speechManager.speechState.collectAsState()
    val isListening = speechState is com.example.service.SpeechState.Listening
    val evaluation by viewModel.lastEvaluation.collectAsState()
    var hasAwarded by remember(current.id) { mutableStateOf(false) }

    // Award when evaluation meets passing score
    LaunchedEffect(evaluation) {
        val eval = evaluation
        if (eval != null && eval.pronunciationScore >= current.passingScore && !hasAwarded) {
            hasAwarded = true
            viewModel.rewardGameWin(60, 20)
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // -------------------------------------------------------------
        // TOP HERO BANNER CARD (Deep Purple Gradient with Speakly Mascot & Close)
        // -------------------------------------------------------------
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF1E0E45),
            border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF2E1065),
                                Color(0xFF1E1045),
                                Color(0xFF11082E)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                // Top Right Circular Close Button
                Surface(
                    shape = CircleShape,
                    color = Color(0x33FFFFFF),
                    border = BorderStroke(1.dp, Color(0x44FFFFFF)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(32.dp)
                        .clickable { onClose() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Close Game",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Crest, Title, Pill
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Shield Crest & Soundwave Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF3B1578),
                                border = BorderStroke(1.5.dp, Color(0xFFA855F7)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("⚔️", fontSize = 16.sp)
                                }
                            }

                            // Small cyan soundwave badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF17133C),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.GraphicEq,
                                        contentDescription = null,
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("✨", fontSize = 10.sp)
                                }
                            }
                        }

                        // Title with Golden Laurels
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🌿", fontSize = 16.sp)
                            Text(
                                "Pronunciation",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text("🌿", fontSize = 16.sp)
                        }

                        // Big Golden "Battle"
                        Text(
                            text = "Battle",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFACC15)
                        )

                        // Purple Ribbon Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF7C3AED)
                        ) {
                            Text(
                                text = "Interactive Learning Challenge",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Right Mascot (Speakly Owl)
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .padding(top = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.app_logo),
                            contentDescription = "Speakly Mascot",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(22.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // MAIN ARENA CARD (Glowing Neon Gradient Border + Concentric Orb)
        // -------------------------------------------------------------
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF090C28),
            border = BorderStroke(
                1.5.dp,
                Brush.linearGradient(
                    listOf(
                        Color(0xFFA855F7),
                        Color(0xFF38BDF8),
                        Color(0xFF2563EB)
                    )
                )
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Arena Header Row: Shield + Title + Step Counter Pill
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
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF1E1045),
                            border = BorderStroke(1.5.dp, Color(0xFFA855F7)),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("⚔️", fontSize = 18.sp)
                            }
                        }
                        Text(
                            "Pronunciation Battle Arena",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1A1A46),
                        border = BorderStroke(1.dp, Color(0xFF4338CA))
                    ) {
                        Text(
                            text = "${challengeIndex + 1} / ${challenges.size}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                        )
                    }
                }

                // Phoneme Focus & Passing Target Bar
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF12183A),
                    border = BorderStroke(1.dp, Color(0xFF2A3462)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Left: Phoneme Focus
                        Row(
                            modifier = Modifier.weight(1.3f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFF67E8F9),
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    "Phoneme Focus",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    current.targetPhonemeFocus,
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        // Divider
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(32.dp)
                                .background(Color(0xFF2A3462))
                        )

                        // Right: Passing Target
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Rounded.TrackChanges,
                                contentDescription = null,
                                tint = Color(0xFFA855F7),
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    "Passing Target",
                                    color = Color(0xFF94A3B8),
                                    fontSize = 11.sp
                                )
                                Text(
                                    "${current.passingScore}%",
                                    color = Color(0xFF4ADE80),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }

                // Sentence Box with Golden Quotes
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF10173F).copy(alpha = 0.85f),
                    border = BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF38BDF8),
                                Color(0xFFA855F7)
                            )
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 20.dp)
                    ) {
                        Text(
                            text = "“",
                            color = Color(0xFFFACC15),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = 0.dp, y = (-12).dp)
                        )

                        Text(
                            text = "\"${current.challengeSentence}\"",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            lineHeight = 24.sp,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        Text(
                            text = "”",
                            color = Color(0xFFFACC15),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = 0.dp, y = 12.dp)
                        )
                    }
                }

                // Listen to Native Guide (Flanked by soundwaves)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Left Soundwave bars
                    Canvas(modifier = Modifier.width(38.dp).height(20.dp)) {
                        val barWidth = 2.5.dp.toPx()
                        val spacing = 3.dp.toPx()
                        val heights = listOf(0.4f, 0.75f, 1f, 0.6f, 0.9f, 0.45f)
                        heights.forEachIndexed { i, h ->
                            val x = i * (barWidth + spacing)
                            val barH = size.height * h
                            drawLine(
                                color = Color(0xFF6366F1).copy(alpha = 0.65f),
                                start = Offset(x, (size.height - barH) / 2f),
                                end = Offset(x, (size.height + barH) / 2f),
                                strokeWidth = barWidth
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF6366F1), Color(0xFF9333EA))
                                ),
                                RoundedCornerShape(24.dp)
                            )
                            .clickable { viewModel.speakAudio(current.challengeSentence) }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                Icons.Rounded.VolumeUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Listen to Native Guide",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Right Soundwave bars
                    Canvas(modifier = Modifier.width(38.dp).height(20.dp)) {
                        val barWidth = 2.5.dp.toPx()
                        val spacing = 3.dp.toPx()
                        val heights = listOf(0.45f, 0.9f, 0.6f, 1f, 0.75f, 0.4f)
                        heights.forEachIndexed { i, h ->
                            val x = i * (barWidth + spacing)
                            val barH = size.height * h
                            drawLine(
                                color = Color(0xFF6366F1).copy(alpha = 0.65f),
                                start = Offset(x, (size.height - barH) / 2f),
                                end = Offset(x, (size.height + barH) / 2f),
                                strokeWidth = barWidth
                            )
                        }
                    }
                }

                // Concentric Glowing Microphone Orb
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Left sound ripples
                        Canvas(modifier = Modifier.width(30.dp).height(50.dp)) {
                            drawArc(
                                color = Color(0xFF38BDF8).copy(alpha = 0.6f),
                                startAngle = 120f,
                                sweepAngle = 120f,
                                useCenter = false,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawArc(
                                color = Color(0xFF818CF8).copy(alpha = 0.4f),
                                startAngle = 130f,
                                sweepAngle = 100f,
                                useCenter = false,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }

                        Spacer(Modifier.width(10.dp))

                        // 3-Tier Concentric Circle Orb
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0C1033),
                            border = BorderStroke(
                                3.dp,
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF00E5FF),
                                        Color(0xFF3B82F6),
                                        Color(0xFFA855F7),
                                        Color(0xFFEC4899),
                                        Color(0xFF00E5FF)
                                    )
                                )
                            ),
                            modifier = Modifier.size(126.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                // Middle Circle
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF151940),
                                    border = BorderStroke(1.dp, Color(0xFF3B487A)),
                                    modifier = Modifier.size(96.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        // Inner Clickable Orb
                                        Surface(
                                            shape = CircleShape,
                                            color = Color.Transparent,
                                            modifier = Modifier
                                                .size(74.dp)
                                                .background(
                                                    Brush.verticalGradient(
                                                        listOf(
                                                            Color(0xFF00E5FF),
                                                            Color(0xFF2563EB),
                                                            Color(0xFF7C3AED)
                                                        )
                                                    ),
                                                    CircleShape
                                                )
                                                .clickable {
                                                    if (isListening) {
                                                        viewModel.stopSpeakingPractice()
                                                    } else {
                                                        viewModel.startSpeakingPractice()
                                                    }
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = if (isListening) Icons.Filled.Stop else Icons.Filled.Mic,
                                                    contentDescription = "Microphone",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(34.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.width(10.dp))

                        // Right sound ripples
                        Canvas(modifier = Modifier.width(30.dp).height(50.dp)) {
                            drawArc(
                                color = Color(0xFF38BDF8).copy(alpha = 0.6f),
                                startAngle = -60f,
                                sweepAngle = 120f,
                                useCenter = false,
                                style = Stroke(width = 2.dp.toPx())
                            )
                            drawArc(
                                color = Color(0xFF818CF8).copy(alpha = 0.4f),
                                startAngle = -50f,
                                sweepAngle = 100f,
                                useCenter = false,
                                style = Stroke(width = 1.5.dp.toPx())
                            )
                        }
                    }

                    // Text prompt below orb
                    Text(
                        text = if (isListening) "Listening... Speak Now" else "Tap to Speak",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )

                    // Test with Sample Voice pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF13183B),
                        border = BorderStroke(1.dp, Color(0xFF3B487A)),
                        modifier = Modifier.clickable {
                            viewModel.simulateVoiceInput(current.challengeSentence)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                Icons.Rounded.VolumeUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Test with Sample Voice",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Live Diagnostic Evaluation Feedback (if available)
                evaluation?.let { eval ->
                    val passed = eval.pronunciationScore >= current.passingScore
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (passed) Color(0xFF0F2E1E) else Color(0xFF351A08),
                        border = BorderStroke(1.dp, if (passed) Color(0xFF4ADE80) else Color(0xFFFBBF24)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (passed) "🏆 Battle Won! Accuracy: ${eval.pronunciationScore}%" else "⚡ Score: ${eval.pronunciationScore}% (Need ${current.passingScore}%)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (passed) Color(0xFF4ADE80) else Color(0xFFFBBF24)
                            )
                            Text(
                                text = eval.feedback,
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Next Battle Phrase Button
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF8B5CF6),
                                    Color(0xFF3B82F6),
                                    Color(0xFF06B6D4)
                                )
                            ),
                            RoundedCornerShape(26.dp)
                        )
                        .clickable {
                            challengeIndex = (challengeIndex + 1) % challenges.size
                            viewModel.clearEvaluation()
                            hasAwarded = false
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Next Battle Phrase",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.width(12.dp))
                        Surface(
                            shape = CircleShape,
                            color = Color.White,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.ArrowForward,
                                    contentDescription = null,
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 6. FAST VOCABULARY GAME - Real 30-Second Rapid Blitz with streak and score
// =========================================================================
@Composable
fun FastVocabularyGame(viewModel: MainViewModel) {
    val challenges by viewModel.fastVocabularyChallenges.collectAsState()
    var isBlitzActive by remember { mutableStateOf(false) }
    var isBlitzFinished by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableIntStateOf(30) }
    var currentIdx by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var correctCount by remember { mutableIntStateOf(0) }
    var streak by remember { mutableIntStateOf(1) }
    var selectedOption by remember { mutableStateOf<Int?>(null) }

    // 30-Second Countdown Timer
    LaunchedEffect(isBlitzActive, timeLeft) {
        if (isBlitzActive && timeLeft > 0) {
            delay(1000)
            timeLeft--
        } else if (isBlitzActive && timeLeft == 0) {
            isBlitzActive = false
            isBlitzFinished = true
            // Reward blitz completion
            if (score > 0) {
                viewModel.rewardGameWin(maxOf(30, score), maxOf(10, score / 4))
            }
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("⏱️ Rapid Vocabulary Blitz", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = OrangeVibrant)
                if (isBlitzActive) {
                    Text(
                        "⏳ ${timeLeft}s",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (timeLeft <= 5) CoralAccent else RoyalBlue
                    )
                }
            }

            if (!isBlitzActive && !isBlitzFinished) {
                // Intro Screen
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F1E42),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("⚡ 30-Second Word Speed Test", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Text(
                            "Recognize words as fast as possible! Keep up a combo streak for multiplied bonus points.",
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Button(
                    onClick = {
                        isBlitzActive = true
                        isBlitzFinished = false
                        timeLeft = 30
                        score = 0
                        correctCount = 0
                        streak = 1
                        currentIdx = 0
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeVibrant)
                ) {
                    Text("Start Blitz Now 🚀", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            } else if (isBlitzFinished) {
                // Results Screen
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.5.dp, EmeraldSuccess),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🎉 Blitz Complete!", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color(0xFF166534))
                        Text(
                            "Score: $score Points • $correctCount Correct Words",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF15803D)
                        )
                        Text(
                            "+${maxOf(30, score)} XP & +${maxOf(10, score / 4)} Coins Earned!",
                            fontSize = 12.sp,
                            color = Color(0xFF166534)
                        )
                    }
                }

                Button(
                    onClick = {
                        isBlitzActive = true
                        isBlitzFinished = false
                        timeLeft = 30
                        score = 0
                        correctCount = 0
                        streak = 1
                        currentIdx = 0
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Play Blitz Again 🔄")
                }
            } else {
                // Active Blitz Running
                val current = challenges.getOrNull(currentIdx % maxOf(1, challenges.size))
                if (current != null) {
                    LinearProgressIndicator(
                        progress = { timeLeft / 30f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (timeLeft <= 5) CoralAccent else OrangeVibrant,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Score: $score pts", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalBlue)
                        if (streak > 1) {
                            Text("🔥 ${streak}x Combo!", fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = AmberGold)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF0F1E42),
                        border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Target Word:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                            Text(current.word, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text(current.prompt, fontSize = 12.sp, color = Color(0xFFCBD5E1))
                        }
                    }

                    // Options
                    current.options.forEachIndexed { idx, opt ->
                        val isSelected = selectedOption == idx
                        val isCorrect = idx == current.correctIndex

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when {
                                isSelected && isCorrect -> Color(0xFFDCFCE7)
                                isSelected && !isCorrect -> Color(0xFFFEE2E2)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            border = BorderStroke(
                                1.dp,
                                when {
                                    isSelected && isCorrect -> EmeraldSuccess
                                    isSelected && !isCorrect -> CoralAccent
                                    else -> Color.Transparent
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = selectedOption == null) {
                                    selectedOption = idx
                                    if (isCorrect) {
                                        score += 20 * streak
                                        correctCount++
                                        streak = minOf(4, streak + 1)
                                        viewModel.speakAudio(current.word)
                                    } else {
                                        streak = 1
                                    }
                                }
                        ) {
                            Text(
                                text = opt,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = when {
                                    isSelected && isCorrect -> Color(0xFF166534)
                                    isSelected && !isCorrect -> Color(0xFF991B1B)
                                    else -> MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Auto-advance quickly after selection
                    LaunchedEffect(selectedOption) {
                        if (selectedOption != null) {
                            delay(450)
                            selectedOption = null
                            currentIdx++
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 7. GRAMMAR CHALLENGE GAME - High contrast with explanation breakdown
// =========================================================================
@Composable
fun GrammarChallengeGame(viewModel: MainViewModel) {
    val challenges by viewModel.grammarChallenges.collectAsState()
    var challengeIndex by remember { mutableIntStateOf(0) }
    val current = challenges.getOrNull(challengeIndex) ?: return

    var selectedIndex by remember(current.id) { mutableStateOf<Int?>(null) }
    var hasAwarded by remember(current.id) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("⚡ Grammar Master Challenge", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalBlue)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Text(
                        current.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = current.question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(14.dp)
                )
            }

            current.options.forEachIndexed { idx, opt ->
                val isSelected = selectedIndex == idx
                val isCorrect = idx == current.correctIndex
                val hasGuessed = selectedIndex != null

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        hasGuessed && isCorrect -> Color(0xFFDCFCE7)
                        isSelected && !isCorrect -> Color(0xFFFEE2E2)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            hasGuessed && isCorrect -> EmeraldSuccess
                            isSelected && !isCorrect -> CoralAccent
                            else -> Color.Transparent
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = selectedIndex == null) {
                            selectedIndex = idx
                            if (isCorrect && !hasAwarded) {
                                hasAwarded = true
                                viewModel.rewardGameWin(50, 15)
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = opt,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = when {
                                hasGuessed && isCorrect -> Color(0xFF166534)
                                isSelected && !isCorrect -> Color(0xFF991B1B)
                                else -> MaterialTheme.colorScheme.onSurface
                            },
                            modifier = Modifier.weight(1f)
                        )
                        if (hasGuessed && isCorrect) {
                            Text("✓ Correct", color = Color(0xFF166534), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        } else if (isSelected && !isCorrect) {
                            Text("✗", color = CoralAccent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            selectedIndex?.let {
                // High contrast explanation card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F1E42),
                    border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📖 Grammar Explanation:", fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = Color(0xFF60A5FA))
                        Text(
                            text = current.explanation,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }

                Button(
                    onClick = {
                        challengeIndex = (challengeIndex + 1) % challenges.size
                        selectedIndex = null
                        hasAwarded = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Next Grammar Question ➡️")
                }
            }
        }
    }
}

// =========================================================================
// 8. PICTURE VOCABULARY GAME - Visual object recognition with phonetic guide
// =========================================================================
private data class PictureItem(
    val id: String,
    val emoji: String,
    val correctWord: String,
    val phonetic: String,
    val clue: String,
    val options: List<String>
)

@Composable
fun PictureVocabGame(viewModel: MainViewModel) {
    val dynamicChallenges by viewModel.pictureVocabChallenges.collectAsState()
    val items = remember(dynamicChallenges) {
        if (dynamicChallenges.isNotEmpty()) {
            dynamicChallenges.map {
                PictureItem(
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
                PictureItem("pv_1", "🩺", "Stethoscope", "/ˈsteθ.ə.skəʊp/", "An instrument used by medical doctors to listen to heart and lungs", listOf("Stethoscope", "Microscope", "Thermometer", "Telescope")),
                PictureItem("pv_2", "✈️", "Airplane", "/ˈeə.pleɪn/", "A powered flying vehicle with fixed wings", listOf("Airplane", "Helicopter", "Submarine", "Spaceship")),
                PictureItem("pv_3", "📚", "Library", "/ˈlaɪ.brər.i/", "A quiet room or building containing books for reading or study", listOf("Library", "Bakery", "Auditorium", "Laboratory")),
                PictureItem("pv_4", "🍳", "Cooking", "/ˈkʊk.ɪŋ/", "The practice or skill of preparing food by combining ingredients", listOf("Cooking", "Gardening", "Sewing", "Painting")),
                PictureItem("pv_5", "🎸", "Guitar", "/ɡɪˈtɑːr/", "A musical instrument having six strings played with fingers or pick", listOf("Guitar", "Violin", "Drum", "Saxophone")),
                PictureItem("pv_6", "🏃", "Sprinting", "/ˈsprɪn.tɪŋ/", "Running at full speed over a short distance", listOf("Sprinting", "Swimming", "Jumping", "Climbing"))
            )
        }
    }

    var itemIndex by remember { mutableIntStateOf(0) }
    if (items.isEmpty()) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("No Picture Vocabulary challenges found.", modifier = Modifier.padding(24.dp), color = MutedGray)
        }
        return
    }
    val current = items[itemIndex % items.size]
    var selectedOption by remember(current.id) { mutableStateOf<String?>(null) }
    var hasAwarded by remember(current.id) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🖼️ Picture Vocabulary Quiz", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalBlue)
                Text("${itemIndex + 1}/${items.size}", fontSize = 12.sp, color = MutedGray)
            }

            // Big Emoji Visual Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F1E42),
                border = BorderStroke(1.2.dp, RoyalBlue.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(current.emoji, fontSize = 54.sp)
                    Text("Phonetic: ${current.phonetic}", fontSize = 12.sp, color = Color(0xFF93C5FD), fontWeight = FontWeight.Medium)
                    Text(current.clue, fontSize = 12.sp, color = Color.White, textAlign = TextAlign.Center)
                }
            }

            // 4 Options
            current.options.forEach { opt ->
                val isSelected = selectedOption == opt
                val isCorrect = opt == current.correctWord
                val hasGuessed = selectedOption != null

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        hasGuessed && isCorrect -> Color(0xFFDCFCE7)
                        isSelected && !isCorrect -> Color(0xFFFEE2E2)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            hasGuessed && isCorrect -> EmeraldSuccess
                            isSelected && !isCorrect -> CoralAccent
                            else -> Color.Transparent
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = selectedOption == null) {
                            selectedOption = opt
                            if (opt == current.correctWord && !hasAwarded) {
                                hasAwarded = true
                                viewModel.rewardGameWin(40, 15)
                                viewModel.speakAudio(current.correctWord)
                            }
                        }
                ) {
                    Text(
                        text = opt,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = when {
                            hasGuessed && isCorrect -> Color(0xFF166534)
                            isSelected && !isCorrect -> Color(0xFF991B1B)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(14.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            if (selectedOption != null) {
                Button(
                    onClick = {
                        itemIndex = (itemIndex + 1) % items.size
                        selectedOption = null
                        hasAwarded = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Next Visual Item ➡️")
                }
            }
        }
    }
}

// =========================================================================
// 9. SPEAK 30 SECONDS GAME - Impromptu speaking with countdown & evaluation
// =========================================================================
@Composable
fun Speak30SecondsGame(viewModel: MainViewModel) {
    val prompts by viewModel.speakingPrompts.collectAsState()
    var promptIndex by remember { mutableIntStateOf(0) }
    val currentPrompt = prompts.getOrNull(promptIndex % maxOf(1, prompts.size))

    val speechState by viewModel.speechManager.speechState.collectAsState()
    val evaluation by viewModel.lastEvaluation.collectAsState()
    var hasAwarded by remember(promptIndex) { mutableStateOf(false) }

    LaunchedEffect(evaluation) {
        val eval = evaluation
        if (eval != null && eval.pronunciationScore >= 70 && !hasAwarded) {
            hasAwarded = true
            viewModel.rewardGameWin(70, 25)
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("🗣️ Speak 30s Challenge", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalBlue)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Text(
                        currentPrompt?.category ?: "Impromptu",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F1E42),
                border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your Surprise Topic:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                    Text(
                        text = currentPrompt?.question ?: "Describe your favorite hobby and why you enjoy spending time doing it.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text("Try speaking smoothly for 30 seconds. Focus on rhythm and complete sentences.", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                }
            }

            MicrophoneButton(
                speechState = speechState,
                onStart = { viewModel.startSpeakingPractice() },
                onStop = { viewModel.stopSpeakingPractice() },
                onSimulate = { viewModel.simulateVoiceInput(it) },
                samplePrompt = currentPrompt?.question ?: "I really enjoy practicing English because it connects me with the entire world."
            )

            evaluation?.let { eval ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Accuracy: ${eval.pronunciationScore}% • Fluency: ${eval.fluencyScore}%", fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                        Text(eval.feedback, fontSize = 12.sp, color = Color(0xFF15803D), textAlign = TextAlign.Center)
                    }
                }
            }

            Button(
                onClick = {
                    promptIndex++
                    viewModel.clearEvaluation()
                    hasAwarded = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text("Next Surprise Topic ➡️")
            }
        }
    }
}

// =========================================================================
// 10. STORY BUILDER GAME - Interactive narrative completion with target keywords
// =========================================================================
@Composable
fun StoryBuilderGame(viewModel: MainViewModel) {
    var step by remember { mutableIntStateOf(1) }
    var selectedSentence1 by remember { mutableStateOf<String?>(null) }
    var selectedSentence2 by remember { mutableStateOf<String?>(null) }
    var selectedSentence3 by remember { mutableStateOf<String?>(null) }
    var hasAwarded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("📖 Story Narrative Builder", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalBlue)
                Text("Step $step/3", fontSize = 12.sp, color = MutedGray, fontWeight = FontWeight.Bold)
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0F1E42),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Target Vocabulary Keywords:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                    Text("1. Mysterious  •  2. Discovered  •  3. Courageous", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            when (step) {
                1 -> {
                    Text("Choose the best beginning sentence using 'Mysterious':", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    listOf(
                        "On a chilly autumn morning, Sarah noticed a mysterious locked wooden chest in the attic.",
                        "Sarah woke up and bought some groceries from the supermarket.",
                        "It is good to have nice weather outside during springtime."
                    ).forEach { sentence ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedSentence1 == sentence) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selectedSentence1 == sentence) BorderStroke(1.5.dp, EmeraldSuccess) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSentence1 = sentence }
                        ) {
                            Text(sentence, fontSize = 13.sp, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Medium)
                        }
                    }
                    if (selectedSentence1 != null) {
                        Button(
                            onClick = { step = 2 },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Continue Story (Step 2) ➡️")
                        }
                    }
                }
                2 -> {
                    Text("Choose the middle climax using 'Discovered':", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    listOf(
                        "Carefully turning the golden key, she discovered an ancient map marking a lost city.",
                        "She opened the chest and found ordinary dust and old newspapers.",
                        "She decided to close the window and drink a glass of water."
                    ).forEach { sentence ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedSentence2 == sentence) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selectedSentence2 == sentence) BorderStroke(1.5.dp, EmeraldSuccess) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSentence2 = sentence }
                        ) {
                            Text(sentence, fontSize = 13.sp, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Medium)
                        }
                    }
                    if (selectedSentence2 != null) {
                        Button(
                            onClick = { step = 3 },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Finish Story (Step 3) ➡️")
                        }
                    }
                }
                3 -> {
                    Text("Choose the triumphant resolution using 'Courageous':", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    listOf(
                        "With a courageous heart, she embarked on the great journey that changed her life forever.",
                        "She packed her school bag and went to bed early.",
                        "The chest was heavy, so she left it in the corner."
                    ).forEach { sentence ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (selectedSentence3 == sentence) Color(0xFFDCFCE7) else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (selectedSentence3 == sentence) BorderStroke(1.5.dp, EmeraldSuccess) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedSentence3 = sentence
                                    if (!hasAwarded) {
                                        hasAwarded = true
                                        viewModel.rewardGameWin(80, 25)
                                    }
                                }
                        ) {
                            Text(sentence, fontSize = 13.sp, modifier = Modifier.padding(12.dp), fontWeight = FontWeight.Medium)
                        }
                    }

                    if (selectedSentence3 != null) {
                        val fullStory = "${selectedSentence1 ?: ""} ${selectedSentence2 ?: ""} ${selectedSentence3 ?: ""}"
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, EmeraldSuccess),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("🎉 Full Story Completed! (+80 XP)", fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                Text(fullStory, fontSize = 13.sp, color = Color(0xFF15803D))
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = { viewModel.speakAudio(fullStory) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Listen to Story 🔊")
                            }
                            OutlinedButton(
                                onClick = {
                                    step = 1
                                    selectedSentence1 = null
                                    selectedSentence2 = null
                                    selectedSentence3 = null
                                    hasAwarded = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Create Another Story 🔄")
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 11. CONVERSATION CHALLENGE GAME - Contextual natural dialogue responses
// =========================================================================
private data class ConversationScenario(
    val id: String,
    val situation: String,
    val partnerLine: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

@Composable
fun ConversationChallengeGame(viewModel: MainViewModel) {
    val scenarios = remember {
        listOf(
            ConversationScenario(
                id = "cs_1",
                situation = "At Airport Check-in Counter",
                partnerLine = "Good morning! May I please see your passport and flight confirmation number?",
                options = listOf(
                    "Here they are. Could you please check if my luggage goes straight to London?",
                    "Give me my ticket quickly, I am in a hurry.",
                    "Why do you need my passport? I bought the ticket."
                ),
                correctIndex = 0,
                explanation = "Politely providing documents and asking a courteous question matches formal airport etiquette."
            ),
            ConversationScenario(
                id = "cs_2",
                situation = "Ordering Coffee at a Cafe",
                partnerLine = "Hi there! What can I get started for you today?",
                options = listOf(
                    "Coffee now.",
                    "Could I please get a medium oat latte with no sugar?",
                    "I want what the other customer had."
                ),
                correctIndex = 1,
                explanation = "Starting with 'Could I please get...' is natural, polite, and clearly communicates your custom order."
            ),
            ConversationScenario(
                id = "cs_3",
                situation = "Job Interview Discussion",
                partnerLine = "Could you tell me how you handle constructive feedback from your team lead?",
                options = listOf(
                    "I don't like feedback because I rarely make mistakes.",
                    "I welcome feedback as a learning opportunity to improve and deliver better results.",
                    "I just ignore it if I disagree."
                ),
                correctIndex = 1,
                explanation = "Emphasizing growth mindset, receptivity, and teamwork creates a professional impression."
            )
        )
    }

    var scenarioIndex by remember { mutableIntStateOf(0) }
    val current = scenarios[scenarioIndex % scenarios.size]
    var selectedIndex by remember(current.id) { mutableStateOf<Int?>(null) }
    var hasAwarded by remember(current.id) { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("💬 Dialogue Master Challenge", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalBlue)
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEFF6FF)) {
                    Text(current.situation, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            // Partner Speech Bubble
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F1E42),
                border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👤", fontSize = 24.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Partner says:", fontSize = 11.sp, color = Color(0xFF60A5FA), fontWeight = FontWeight.Bold)
                        Text(current.partnerLine, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    IconButton(onClick = { viewModel.speakAudio(current.partnerLine) }) {
                        Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen", tint = RoyalBlue)
                    }
                }
            }

            Text("Choose the most polite and natural conversational reply:", fontSize = 12.sp, color = MutedGray)

            current.options.forEachIndexed { idx, opt ->
                val isSelected = selectedIndex == idx
                val isCorrect = idx == current.correctIndex
                val hasGuessed = selectedIndex != null

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        hasGuessed && isCorrect -> Color(0xFFDCFCE7)
                        isSelected && !isCorrect -> Color(0xFFFEE2E2)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = BorderStroke(
                        1.dp,
                        when {
                            hasGuessed && isCorrect -> EmeraldSuccess
                            isSelected && !isCorrect -> CoralAccent
                            else -> Color.Transparent
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = selectedIndex == null) {
                            selectedIndex = idx
                            if (isCorrect && !hasAwarded) {
                                hasAwarded = true
                                viewModel.rewardGameWin(50, 15)
                                viewModel.speakAudio(opt)
                            }
                        }
                ) {
                    Text(
                        text = opt,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = when {
                            hasGuessed && isCorrect -> Color(0xFF166534)
                            isSelected && !isCorrect -> Color(0xFF991B1B)
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            selectedIndex?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF0F1E42),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("💡 Etiquette Note:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                        Text(current.explanation, fontSize = 12.sp, color = Color.White)
                    }
                }

                Button(
                    onClick = {
                        scenarioIndex = (scenarioIndex + 1) % scenarios.size
                        selectedIndex = null
                        hasAwarded = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Next Dialogue Scenario ➡️")
                }
            }
        }
    }
}

// =========================================================================
// 12. SPELLING BEE GAME - Audio listening and letter-by-letter spelling check
// =========================================================================
@Composable
fun SpellingBeeGame(viewModel: MainViewModel) {
    val words = remember {
        listOf(
            Triple("FLUENCY", "Ability to speak and write a language easily and smoothly", "/ˈfluː.ən.si/"),
            Triple("PERSISTENCE", "Continuing firmly in a course of action despite difficulty", "/pəˈsɪs.təns/"),
            Triple("CONFIDENCE", "Belief in oneself and one's powers or abilities", "/ˈkɒn.fɪ.dəns/"),
            Triple("EXPRESSION", "The action of making known one's thoughts or feelings", "/ɪkˈspreʃ.ən/"),
            Triple("APPRECIATE", "Recognize the full worth of something", "/əˈpriː.ʃi.eɪt/")
        )
    }

    var wordIndex by remember { mutableIntStateOf(0) }
    val (targetWord, definition, phonetic) = words[wordIndex % words.size]
    var enteredText by remember(wordIndex) { mutableStateOf("") }
    var isSubmitted by remember(wordIndex) { mutableStateOf(false) }
    var hasAwarded by remember(wordIndex) { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("✍️ Audio Spelling Bee", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RoyalBlue)
                Text("Word ${wordIndex + 1}/${words.size}", fontSize = 12.sp, color = MutedGray)
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F1E42),
                border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Listen carefully and spell the word:", fontSize = 12.sp, color = Color(0xFF93C5FD))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.speakAudio(targetWord, slow = false) },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.Rounded.VolumeUp, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text("Play Audio 🔊")
                        }
                        OutlinedButton(
                            onClick = { viewModel.speakAudio(targetWord, slow = true) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("🐢 Slow")
                        }
                    }
                    Text("Clue: $definition", fontSize = 12.sp, color = Color.White, textAlign = TextAlign.Center)
                    Text("Phonetic: $phonetic", fontSize = 11.sp, color = Color(0xFF60A5FA))
                }
            }

            // Word length indicator slots (Adaptive width & wrapping FlowRow)
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val count = targetWord.length
                val spacing = if (count > 9) 3.dp else 4.dp
                val computedSize = ((maxWidth - (spacing * (count - 1))) / count).coerceIn(24.dp, 34.dp)
                val fontSize = when {
                    computedSize < 28.dp -> 13.sp
                    else -> 15.sp
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    targetWord.forEachIndexed { idx, _ ->
                        val enteredChar = enteredText.getOrNull(idx)
                        Surface(
                            shape = RoundedCornerShape(if (computedSize < 28.dp) 4.dp else 6.dp),
                            color = when {
                                isSubmitted && enteredText.equals(targetWord, ignoreCase = true) -> Color(0xFFDCFCE7)
                                isSubmitted -> Color(0xFFFEE2E2)
                                enteredChar != null -> Color(0xFFDBEAFE)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.size(computedSize)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = enteredChar?.uppercase() ?: "_",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = fontSize,
                                    color = if (enteredChar != null) RoyalBlue else MutedGray
                                )
                            }
                        }
                    }
                }
            }

            // Input TextField
            OutlinedTextField(
                value = enteredText,
                onValueChange = {
                    if (it.length <= targetWord.length) {
                        enteredText = it.uppercase()
                        isSubmitted = false
                    }
                },
                label = { Text("Type spelling here") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )

            // Submit Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    isSubmitted = true
                    if (enteredText.trim().equals(targetWord, ignoreCase = true)) {
                        if (!hasAwarded) {
                            hasAwarded = true
                            viewModel.rewardGameWin(50, 15)
                        }
                        viewModel.speakAudio(targetWord)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text("Check Spelling")
            }

            if (isSubmitted) {
                val isCorrect = enteredText.trim().equals(targetWord, ignoreCase = true)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isCorrect) Color(0xFFDCFCE7) else Color(0xFFFEE2E2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isCorrect) "🎉 Correct! Perfect spelling: $targetWord" else "❌ Not quite right. Correct spelling is: $targetWord",
                        fontWeight = FontWeight.Bold,
                        color = if (isCorrect) Color(0xFF166534) else Color(0xFF991B1B),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                Button(
                    onClick = {
                        wordIndex++
                        enteredText = ""
                        isSubmitted = false
                        hasAwarded = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("Next Spelling Word ➡️")
                }
            }
        }
    }
}
