@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens.student

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    if (activeGame != null) {
        ActiveGameRunner(
            gameType = activeGame!!,
            viewModel = viewModel,
            onClose = { viewModel.closeGame() },
            modifier = modifier
        )
    } else {
        // Games Catalog Grid
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(AmberGold, OrangeVibrant))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎮", fontSize = 28.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("English Learning Arcade", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text(
                            "Interactive games crafted by teachers & academy curriculum to accelerate spoken mastery!",
                            fontSize = 12.sp,
                            color = MutedGray
                        )
                    }
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(ActiveGameType.values()) { game ->
                    GameCard(
                        game = game,
                        onClick = { viewModel.launchGame(game) }
                    )
                }
            }
        }
    }
}

@Composable
fun GameCard(
    game: ActiveGameType,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(138.dp)
            .clickable { onClick() }
            .testTag("game_card_${game.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(game.emoji, fontSize = 28.sp)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Text(
                        "+40 XP",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = RoyalBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    game.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    game.desc,
                    fontSize = 11.sp,
                    color = MutedGray,
                    maxLines = 2
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
    BackHandler {
        onClose()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Game Header Bar
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

        // Dedicated games with real, validated logic
        when (gameType) {
            ActiveGameType.WORD_MATCH -> WordMatchGame(viewModel)
            ActiveGameType.MISSING_LETTERS -> MissingLettersGame(viewModel)
            ActiveGameType.SENTENCE_BUILDER -> SentenceBuilderGame(viewModel)
            ActiveGameType.LISTEN_CHOOSE -> ListenAndChooseGame(viewModel)
            ActiveGameType.PRONUNCIATION_BATTLE -> PronunciationBattleGame(viewModel)
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
// 3. SENTENCE BUILDER GAME - Robust tokenization and punctuation handling
// =========================================================================
private data class SentenceWordChip(val id: Int, val text: String)

@Composable
fun SentenceBuilderGame(viewModel: MainViewModel) {
    val challenges by viewModel.sentenceBuilderChallenges.collectAsState()
    var challengeIndex by remember { mutableIntStateOf(0) }
    val current = challenges.getOrNull(challengeIndex) ?: return

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
                Text("Assemble Sentence Syntax:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    "Challenge ${challengeIndex + 1}/${challenges.size}",
                    fontSize = 12.sp,
                    color = RoyalBlue,
                    fontWeight = FontWeight.Bold
                )
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
                    // Normalize comparison: strip common trailing punctuation and compare lowercased
                    fun normalize(s: String) = s.replace(Regex("[.,!?;:]"), "").trim().lowercase()
                    if (normalize(constructed) == normalize(current.targetSentence)) {
                        isCorrect = true
                        if (!hasAwarded) {
                            hasAwarded = true
                            viewModel.rewardGameWin(60, 20)
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

            // Assamese Translation & Next Button
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

                Button(
                    onClick = {
                        challengeIndex = (challengeIndex + 1) % challenges.size
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
fun PronunciationBattleGame(viewModel: MainViewModel) {
    val challenges by viewModel.pronunciationBattleChallenges.collectAsState()
    var challengeIndex by remember { mutableIntStateOf(0) }
    val current = challenges.getOrNull(challengeIndex) ?: return

    val speechState by viewModel.speechManager.speechState.collectAsState()
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
                Text("⚔️ Pronunciation Battle Arena", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = RoyalBlue)
                Text("${challengeIndex + 1}/${challenges.size}", fontSize = 12.sp, color = MutedGray)
            }

            Text(
                "Phoneme Focus: ${current.targetPhonemeFocus} • Passing Target: ${current.passingScore}%",
                fontSize = 12.sp,
                color = MutedGray,
                textAlign = TextAlign.Center
            )

            // Sentence Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF0F1E42),
                border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "\"${current.challengeSentence}\"",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Audio Guide
            Button(
                onClick = { viewModel.speakAudio(current.challengeSentence) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen", tint = RoyalBlue)
                Spacer(Modifier.width(6.dp))
                Text("Listen to Native Guide", color = RoyalBlue, fontSize = 12.sp)
            }

            // Microphone Recording
            MicrophoneButton(
                speechState = speechState,
                onStart = { viewModel.startSpeakingPractice() },
                onStop = { viewModel.stopSpeakingPractice() },
                onSimulate = { viewModel.simulateVoiceInput(it) },
                samplePrompt = current.challengeSentence
            )

            // Live Evaluation Feedback
            evaluation?.let { eval ->
                val passed = eval.pronunciationScore >= current.passingScore
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (passed) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, if (passed) EmeraldSuccess else AmberGold),
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
                            color = if (passed) Color(0xFF166534) else Color(0xFF92400E)
                        )
                        Text(
                            text = eval.feedback,
                            fontSize = 12.sp,
                            color = if (passed) Color(0xFF15803D) else Color(0xFF78350F),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Button(
                onClick = {
                    challengeIndex = (challengeIndex + 1) % challenges.size
                    viewModel.clearEvaluation()
                    hasAwarded = false
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text("Next Battle Phrase ➡️")
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
    val items = remember {
        listOf(
            PictureItem("pv_1", "🩺", "Stethoscope", "/ˈsteθ.ə.skəʊp/", "An instrument used by medical doctors to listen to heart and lungs", listOf("Stethoscope", "Microscope", "Thermometer", "Telescope")),
            PictureItem("pv_2", "✈️", "Airplane", "/ˈeə.pleɪn/", "A powered flying vehicle with fixed wings", listOf("Airplane", "Helicopter", "Submarine", "Spaceship")),
            PictureItem("pv_3", "📚", "Library", "/ˈlaɪ.brər.i/", "A quiet room or building containing books for reading or study", listOf("Library", "Bakery", "Auditorium", "Laboratory")),
            PictureItem("pv_4", "🍳", "Cooking", "/ˈkʊk.ɪŋ/", "The practice or skill of preparing food by combining ingredients", listOf("Cooking", "Gardening", "Sewing", "Painting")),
            PictureItem("pv_5", "🎸", "Guitar", "/ɡɪˈtɑːr/", "A musical instrument having six strings played with fingers or pick", listOf("Guitar", "Violin", "Drum", "Saxophone")),
            PictureItem("pv_6", "🏃", "Sprinting", "/ˈsprɪn.tɪŋ/", "Running at full speed over a short distance", listOf("Sprinting", "Swimming", "Jumping", "Climbing"))
        )
    }

    var itemIndex by remember { mutableIntStateOf(0) }
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
