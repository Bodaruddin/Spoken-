@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ActiveGameType
import com.example.ui.MainViewModel
import com.example.ui.components.MicrophoneButton
import com.example.ui.theme.*
import com.example.util.AssameseTranslator

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
                        Text("Interactive games crafted by teachers & academy curriculum to accelerate spoken mastery!", fontSize = 12.sp, color = MutedGray)
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
            .height(135.dp)
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
                    Text("+35 XP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(game.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(game.desc, fontSize = 11.sp, color = MutedGray, maxLines = 2)
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Game Header
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
            IconButton(onClick = onClose, modifier = Modifier.testTag("close_game_button")) {
                Icon(Icons.Filled.Close, contentDescription = "Close Game")
            }
        }

        when (gameType) {
            ActiveGameType.WORD_MATCH -> WordMatchGame(viewModel)
            ActiveGameType.MISSING_LETTERS -> MissingLettersGame(viewModel)
            ActiveGameType.SENTENCE_BUILDER -> SentenceBuilderGame(viewModel)
            ActiveGameType.LISTEN_CHOOSE -> ListenAndChooseGame(viewModel)
            ActiveGameType.PRONUNCIATION_BATTLE -> PronunciationBattleGame(viewModel)
            ActiveGameType.FAST_VOCABULARY -> FastVocabularyGame(viewModel)
            ActiveGameType.GRAMMAR_CHALLENGE -> GrammarChallengeGame(viewModel)
            else -> GenericQuizGame(gameType, viewModel)
        }
    }
}

// 1. WORD MATCH GAME
@Composable
fun WordMatchGame(viewModel: MainViewModel) {
    val challenges by viewModel.wordMatchChallenges.collectAsState()
    var selectedWord by remember { mutableStateOf<String?>(null) }
    var matchedPairs by remember { mutableStateOf(setOf<String>()) }
    var roundOffset by remember { mutableIntStateOf(0) }

    val activeBatch = remember(challenges, roundOffset) {
        val list = challenges.drop(roundOffset * 4).take(4)
        if (list.isEmpty() && challenges.isNotEmpty()) challenges.take(4) else list
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Match words with correct definitions:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("${matchedPairs.size / 2}/${activeBatch.size} Matched", fontSize = 12.sp, color = RoyalBlue, fontWeight = FontWeight.Bold)
            }

            activeBatch.forEach { challenge ->
                val isMatched = matchedPairs.contains(challenge.word)
                val isSel = selectedWord == challenge.word

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isMatched -> Color(0xFFDCFCE7)
                        isSel -> Color(0xFFDBEAFE)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = if (isSel) BorderStroke(1.5.dp, RoyalBlue) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isMatched) {
                            if (selectedWord == null) {
                                selectedWord = challenge.word
                                viewModel.speakAudio(challenge.word)
                            } else if (selectedWord == challenge.word) {
                                selectedWord = null
                            } else {
                                // Match check
                                matchedPairs = matchedPairs + challenge.word + selectedWord!!
                                selectedWord = null
                                if (matchedPairs.size >= activeBatch.size * 2) {
                                    viewModel.rewardGameWin(50, 15)
                                }
                            }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(challenge.word, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(challenge.definition, fontSize = 12.sp, color = MutedGray)
                        }
                        if (isMatched) {
                            Text("✅", fontSize = 16.sp)
                        } else {
                            IconButton(onClick = { viewModel.speakAudio(challenge.word) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Rounded.VolumeUp, contentDescription = "Pronounce", tint = RoyalBlue, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            if (matchedPairs.size >= activeBatch.size * 2 && activeBatch.isNotEmpty()) {
                Button(
                    onClick = {
                        matchedPairs = emptySet()
                        selectedWord = null
                        roundOffset = (roundOffset + 1) % maxOf(1, (challenges.size + 3) / 4)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Play Next Round 🎉")
                }
            }
        }
    }
}

// 2. MISSING LETTERS GAME
@Composable
fun MissingLettersGame(viewModel: MainViewModel) {
    val challenges by viewModel.missingLettersChallenges.collectAsState()
    var challengeIndex by remember { mutableIntStateOf(0) }
    val current = challenges.getOrNull(challengeIndex) ?: return

    var filledLetters by remember(current.id) {
        mutableStateOf(current.fullWord.mapIndexed { idx, ch ->
            if (current.missingIndices.contains(idx)) ' ' else ch
        }.toMutableList())
    }
    var isVerified by remember(current.id) { mutableStateOf<Boolean?>(null) }

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
            Text("Complete the missing letters:", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Clue / Meaning:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    Text(current.hint, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }

            // Word tiles with blanks
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                current.fullWord.forEachIndexed { idx, expectedChar ->
                    val isMissing = current.missingIndices.contains(idx)
                    val displayedChar = filledLetters[idx]

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when {
                            !isMissing -> MaterialTheme.colorScheme.surfaceVariant
                            displayedChar != ' ' -> Color(0xFFDBEAFE)
                            else -> MaterialTheme.colorScheme.outlineVariant
                        },
                        border = if (isMissing) BorderStroke(1.5.dp, RoyalBlue) else null,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = if (displayedChar == ' ') "_" else displayedChar.toString(),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = if (isMissing) RoyalBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Quick letter choices
            val choices = remember(current.id) {
                val missingChars = current.missingIndices.map { current.fullWord[it] }
                val distractors = listOf('A', 'E', 'I', 'O', 'U', 'R', 'S', 'T', 'N', 'L').filterNot { missingChars.contains(it) }.take(4)
                (missingChars + distractors).shuffled()
            }

            Text("Tap letters to fill blanks in order:", fontSize = 12.sp, color = MutedGray)
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                choices.forEach { ch ->
                    Button(
                        onClick = {
                            val nextBlank = filledLetters.indexOfFirst { it == ' ' }
                            if (nextBlank != -1) {
                                val updated = filledLetters.toMutableList()
                                updated[nextBlank] = ch
                                filledLetters = updated
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.size(42.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(ch.toString(), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        filledLetters = current.fullWord.mapIndexed { idx, ch ->
                            if (current.missingIndices.contains(idx)) ' ' else ch
                        }.toMutableList()
                        isVerified = null
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Clear")
                }

                Button(
                    onClick = {
                        val word = filledLetters.joinToString("")
                        if (word.equals(current.fullWord, ignoreCase = true)) {
                            isVerified = true
                            viewModel.speakAudio(current.fullWord)
                            viewModel.rewardGameWin(40, 15)
                        } else {
                            isVerified = false
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Verify Spelling")
                }
            }

            isVerified?.let { correct ->
                Text(
                    text = if (correct) "🎉 Correct! Perfect spelling." else "❌ Incorrect letter placed. Try again!",
                    color = if (correct) EmeraldSuccess else CoralAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            if (isVerified == true) {
                Button(
                    onClick = {
                        challengeIndex = (challengeIndex + 1) % challenges.size
                        isVerified = null
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

// 3. SENTENCE BUILDER GAME
@Composable
fun SentenceBuilderGame(viewModel: MainViewModel) {
    val challenges by viewModel.sentenceBuilderChallenges.collectAsState()
    var challengeIndex by remember { mutableIntStateOf(0) }
    val current = challenges.getOrNull(challengeIndex) ?: return

    val words = remember(current.id) { current.targetSentence.split(" ").shuffled() }
    var selectedWords by remember(current.id) { mutableStateOf(listOf<String>()) }
    var isCorrect by remember(current.id) { mutableStateOf<Boolean?>(null) }

    val remainingWords = remember(words, selectedWords) {
        val sel = selectedWords.toMutableList()
        val rem = mutableListOf<String>()
        for (w in words) {
            if (sel.contains(w)) {
                sel.remove(w)
            } else {
                rem.add(w)
            }
        }
        rem
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
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Assemble the sentence in correct syntax:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("Challenge ${challengeIndex + 1}/${challenges.size}", fontSize = 11.sp, color = RoyalBlue, fontWeight = FontWeight.Bold)
            }

            // Clue Hint
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("💡 Hint: ${current.hint}", fontSize = 12.sp, color = DeepNavy, modifier = Modifier.padding(10.dp))
            }

            // Assembled sentence tray (fixed height expansion issue)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Your Assembled Sentence:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MutedGray)
                    if (selectedWords.isNotEmpty()) {
                        TextButton(
                            onClick = {
                                selectedWords = emptyList()
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
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.5.dp, if (selectedWords.isEmpty()) Color(0xFFE2E8F0) else RoyalBlue.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 76.dp)
                        .wrapContentHeight()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (selectedWords.isEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Outlined.Edit, contentDescription = null, tint = MutedGray, modifier = Modifier.size(16.dp))
                                Text("Tap the words below in order...", fontSize = 13.sp, color = MutedGray)
                            }
                        } else {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                selectedWords.forEachIndexed { index, w ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = RoyalBlue,
                                        modifier = Modifier.clickable {
                                            val updated = selectedWords.toMutableList()
                                            updated.removeAt(index)
                                            selectedWords = updated
                                            isCorrect = null
                                        }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = w,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Icon(
                                                Icons.Filled.Close,
                                                contentDescription = "Remove $w",
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

            // Available word chips (using FlowRow to prevent vertical squishing)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Available Words:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (remainingWords.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF1F5F9),
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
                        remainingWords.forEach { w ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFEFF6FF),
                                border = BorderStroke(1.2.dp, RoyalBlue),
                                modifier = Modifier.clickable {
                                    selectedWords = selectedWords + w
                                    isCorrect = null
                                    viewModel.speakAudio(w)
                                }
                            ) {
                                Text(
                                    text = w,
                                    color = DeepNavy,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            Button(
                onClick = {
                    val constructed = selectedWords.joinToString(" ")
                    if (constructed.equals(current.targetSentence, ignoreCase = true)) {
                        isCorrect = true
                        viewModel.rewardGameWin(60, 20)
                        viewModel.speakAudio(current.targetSentence)
                    } else {
                        isCorrect = false
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
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
                            text = "❌ Not quite right yet. Tap words to rearrange or remove words in the tray.",
                            color = CoralAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }

            // Assamese Translation Section when sentence correctly assembled
            if (isCorrect == true) {
                val translation = remember(current.id) {
                    AssameseTranslator.translate(current.targetSentence, current.assameseTranslation)
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.5.dp, Color(0xFF86EFAC)),
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🇮🇳", fontSize = 20.sp)
                                Column {
                                    Text(
                                        "অসমীয়া অনুবাদ (Assamese Meaning)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF166534)
                                    )
                                    Text("Correct Syntax Translation", fontSize = 10.sp, color = EmeraldSuccess)
                                }
                            }
                            IconButton(
                                onClick = { viewModel.speakAudio(current.targetSentence) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Filled.VolumeUp,
                                    contentDescription = "Listen to English Sentence",
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Assamese Script in prominent typography
                        Text(
                            text = translation.assameseText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepNavy,
                            lineHeight = 26.sp
                        )

                        // Phonetic Romanization pronunciation guide
                        if (translation.transliteration.isNotBlank()) {
                            Text(
                                text = "🗣️ ${translation.transliteration}",
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFF15803D)
                            )
                        }

                        // Bilingual word-by-word bridge
                        if (translation.wordBreakdown.isNotEmpty()) {
                            HorizontalDivider(color = Color(0xFFDCFCE7), thickness = 1.dp)
                            Text(
                                "শব্দার্থ (Word Meanings):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MutedGray
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                translation.wordBreakdown.forEach { (en, asWord) ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = BorderStroke(0.8.dp, Color(0xFFBBF7D0))
                                    ) {
                                        Text(
                                            text = "$en: $asWord",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = DeepNavy,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = {
                        challengeIndex = (challengeIndex + 1) % challenges.size
                        selectedWords = emptyList()
                        isCorrect = null
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

// 4. LISTEN & CHOOSE GAME
@Composable
fun ListenAndChooseGame(viewModel: MainViewModel) {
    val challenges by viewModel.listenChooseChallenges.collectAsState()
    var challengeIndex by remember { mutableIntStateOf(0) }
    val current = challenges.getOrNull(challengeIndex) ?: return

    var selectedOption by remember(current.id) { mutableStateOf<Int?>(null) }

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
                Text("Listening Comprehension Drill", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text("${challengeIndex + 1}/${challenges.size}", fontSize = 12.sp, color = RoyalBlue, fontWeight = FontWeight.Bold)
            }

            // Listen button
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { viewModel.speakAudio(current.audioScript, slow = false) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.VolumeUp, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("Play Audio 🔊", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = { viewModel.speakAudio(current.audioScript, slow = true) },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("🐢 Slow", fontSize = 12.sp)
                }
            }

            Text(current.question, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            current.options.forEachIndexed { index, opt ->
                val isSel = selectedOption == index
                val isCorrect = index == current.correctIndex

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isSel && isCorrect -> Color(0xFFDCFCE7)
                        isSel && !isCorrect -> Color(0xFFFEE2E2)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = if (isSel) BorderStroke(1.5.dp, if (isCorrect) EmeraldSuccess else CoralAccent) else null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedOption = index
                            if (isCorrect) {
                                viewModel.rewardGameWin(40, 15)
                            }
                        }
                ) {
                    Text(opt, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                }
            }

            if (selectedOption != null) {
                Button(
                    onClick = {
                        challengeIndex = (challengeIndex + 1) % challenges.size
                        selectedOption = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next Question ➡️")
                }
            }
        }
    }
}

// 5. PRONUNCIATION BATTLE GAME
@Composable
fun PronunciationBattleGame(viewModel: MainViewModel) {
    val challenges by viewModel.pronunciationBattleChallenges.collectAsState()
    var challengeIndex by remember { mutableIntStateOf(0) }
    val current = challenges.getOrNull(challengeIndex) ?: return

    val speechState by viewModel.speechManager.speechState.collectAsState()
    val evaluation by viewModel.lastEvaluation.collectAsState()

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("⚔️ Pronunciation Battle Arena", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = RoyalBlue)
                Text("${challengeIndex + 1}/${challenges.size}", fontSize = 12.sp, color = MutedGray)
            }
            Text("Focus: ${current.targetPhonemeFocus} • Target: ${current.passingScore}%", fontSize = 12.sp, color = MutedGray, textAlign = TextAlign.Center)

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "\"${current.challengeSentence}\"",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            IconButton(onClick = { viewModel.speakAudio(current.challengeSentence) }) {
                Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen to Guide", tint = RoyalBlue)
            }

            MicrophoneButton(
                speechState = speechState,
                onStart = { viewModel.startSpeakingPractice() },
                onStop = { viewModel.stopSpeakingPractice() },
                onSimulate = { viewModel.simulateVoiceInput(it) },
                samplePrompt = current.challengeSentence
            )

            evaluation?.let { eval ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Accuracy: ${eval.pronunciationScore}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (eval.pronunciationScore >= current.passingScore) EmeraldSuccess else OrangeVibrant
                    )
                    Text(eval.feedback, fontSize = 12.sp, color = MutedGray, textAlign = TextAlign.Center)
                }
            }

            Button(
                onClick = {
                    challengeIndex = (challengeIndex + 1) % challenges.size
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Next Battle Phrase ➡️")
            }
        }
    }
}

// 6. FAST VOCABULARY GAME
@Composable
fun FastVocabularyGame(viewModel: MainViewModel) {
    val challenges by viewModel.fastVocabularyChallenges.collectAsState()
    var challengeIndex by remember { mutableIntStateOf(0) }
    val current = challenges.getOrNull(challengeIndex) ?: return

    var selectedIndex by remember(current.id) { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }

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
                Text("Score: $score XP", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalBlue)
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Target Word:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    Text(current.word, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DeepNavy)
                    Text(current.prompt, fontSize = 13.sp, color = MutedGray)
                }
            }

            current.options.forEachIndexed { idx, opt ->
                val isSel = selectedIndex == idx
                val isCorrect = idx == current.correctIndex

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isSel && isCorrect -> Color(0xFFDCFCE7)
                        isSel && !isCorrect -> Color(0xFFFEE2E2)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (selectedIndex == null) {
                                selectedIndex = idx
                                if (isCorrect) {
                                    score += 30
                                    viewModel.rewardGameWin(30, 10)
                                    viewModel.speakAudio(current.word)
                                }
                            }
                        }
                ) {
                    Text(opt, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                }
            }

            if (selectedIndex != null) {
                Button(
                    onClick = {
                        challengeIndex = (challengeIndex + 1) % challenges.size
                        selectedIndex = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next Word ➡️")
                }
            }
        }
    }
}

// 7. GRAMMAR CHALLENGE GAME
@Composable
fun GrammarChallengeGame(viewModel: MainViewModel) {
    val challenges by viewModel.grammarChallenges.collectAsState()
    var challengeIndex by remember { mutableIntStateOf(0) }
    val current = challenges.getOrNull(challengeIndex) ?: return

    var selectedIndex by remember(current.id) { mutableStateOf<Int?>(null) }

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
                    Text(current.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    current.question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(14.dp)
                )
            }

            current.options.forEachIndexed { idx, opt ->
                val isSel = selectedIndex == idx
                val isCorrect = idx == current.correctIndex

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when {
                        isSel && isCorrect -> Color(0xFFDCFCE7)
                        isSel && !isCorrect -> Color(0xFFFEE2E2)
                        else -> MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(1.dp, if (isSel && isCorrect) EmeraldSuccess else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (selectedIndex == null) {
                                selectedIndex = idx
                                if (isCorrect) viewModel.rewardGameWin(50, 15)
                            }
                        }
                ) {
                    Text(opt, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                }
            }

            selectedIndex?.let {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("📖 Grammar Explanation:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalBlue)
                        Text(current.explanation, fontSize = 12.sp)
                    }
                }

                Button(
                    onClick = {
                        challengeIndex = (challengeIndex + 1) % challenges.size
                        selectedIndex = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Next Grammar Question ➡️")
                }
            }
        }
    }
}

@Composable
fun GenericQuizGame(gameType: ActiveGameType, viewModel: MainViewModel) {
    var answered by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(gameType.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(gameType.desc, fontSize = 13.sp, color = MutedGray)

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Sample Drill Challenge:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    Text("Select the most suitable formal greeting for an international business client:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                }
            }

            listOf(
                "Good morning Mr. Henderson, thank you for your time today.",
                "Hey dude, what's up with the project?",
                "Yo! How's everything going?"
            ).forEachIndexed { idx, opt ->
                Button(
                    onClick = {
                        answered = true
                        if (idx == 0) viewModel.rewardGameWin(40, 10)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = if (idx == 0 && answered) ButtonDefaults.buttonColors(containerColor = EmeraldSuccess) else ButtonDefaults.buttonColors()
                ) {
                    Text(opt, fontSize = 12.sp, textAlign = TextAlign.Center)
                }
            }
        }
    }
}
