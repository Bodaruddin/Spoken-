package com.example.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.ActiveGameType
import com.example.ui.MainViewModel
import com.example.ui.components.MicrophoneButton
import com.example.ui.theme.*

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
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(AmberGold, OrangeVibrant))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎮", fontSize = 26.sp)
                    }
                    Column {
                        Text("English Learning Arcade", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Play 12 thrilling games to sharpen your speaking, vocabulary, and grammar!", fontSize = 12.sp, color = MutedGray)
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
            .height(130.dp)
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
                    Text("+30 XP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
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
            .background(MaterialTheme.colorScheme.background)
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(gameType.emoji, fontSize = 24.sp)
                Column {
                    Text(gameType.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Educational Challenge", fontSize = 11.sp, color = MutedGray)
                }
            }
            IconButton(onClick = onClose, modifier = Modifier.testTag("close_game_button")) {
                Icon(Icons.Filled.Close, contentDescription = "Close Game")
            }
        }

        when (gameType) {
            ActiveGameType.WORD_MATCH -> WordMatchGame(viewModel)
            ActiveGameType.SENTENCE_BUILDER -> SentenceBuilderGame(viewModel)
            ActiveGameType.LISTEN_CHOOSE -> ListenAndChooseGame(viewModel)
            ActiveGameType.PRONUNCIATION_BATTLE -> PronunciationBattleGame(viewModel)
            else -> GenericQuizGame(gameType, viewModel)
        }
    }
}

@Composable
fun WordMatchGame(viewModel: MainViewModel) {
    var selectedWord by remember { mutableStateOf<String?>(null) }
    var matchedPairs by remember { mutableStateOf(setOf<String>()) }

    val pairs = remember {
        listOf(
            "Eloquence" to "Fluent & persuasive speech",
            "Resilience" to "Ability to recover quickly",
            "Articulate" to "Clear & expressive language",
            "Pragmatic" to "Sensible & practical"
        )
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
            Text("Match each word to its definition:", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            pairs.forEach { (word, def) ->
                val isMatched = matchedPairs.contains(word)
                val isSel = selectedWord == word

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when {
                                isMatched -> Color(0xFFDCFCE7)
                                isSel -> Color(0xFFDBEAFE)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .clickable(enabled = !isMatched) {
                            if (selectedWord == null) {
                                selectedWord = word
                                viewModel.speakAudio(word)
                            } else if (selectedWord == word) {
                                selectedWord = null
                            } else {
                                // Match check
                                matchedPairs = matchedPairs + word + selectedWord!!
                                selectedWord = null
                                if (matchedPairs.size >= pairs.size) {
                                    viewModel.rewardGameWin(50, 15)
                                }
                            }
                        }
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(word, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(def, fontSize = 12.sp, color = MutedGray)
                }
            }

            if (matchedPairs.size >= pairs.size) {
                Button(
                    onClick = {
                        matchedPairs = emptySet()
                        selectedWord = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Play Next Round")
                }
            }
        }
    }
}

@Composable
fun SentenceBuilderGame(viewModel: MainViewModel) {
    val targetSentence = "She speaks English with natural confidence"
    val words = remember { listOf("English", "confidence", "speaks", "with", "She", "natural") }
    var selectedWords by remember { mutableStateOf(listOf<String>()) }
    var isCorrect by remember { mutableStateOf<Boolean?>(null) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Assemble the sentence in correct syntax:", fontWeight = FontWeight.Bold, fontSize = 15.sp)

            // Assembled sentence tray
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 60.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedWords.isEmpty()) {
                        Text("Tap the words below in order...", fontSize = 13.sp, color = MutedGray)
                    } else {
                        selectedWords.forEach { w ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = RoyalBlue,
                                modifier = Modifier.clickable {
                                    selectedWords = selectedWords - w
                                }
                            ) {
                                Text(w, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            // Available word chips
            Text("Available Words:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                words.filter { !selectedWords.contains(it) }.forEach { w ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalBlue),
                        modifier = Modifier.clickable {
                            selectedWords = selectedWords + w
                            viewModel.speakAudio(w)
                        }
                    ) {
                        Text(w, color = DeepNavy, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                    }
                }
            }

            Button(
                onClick = {
                    val constructed = selectedWords.joinToString(" ")
                    if (constructed.equals(targetSentence, ignoreCase = true)) {
                        isCorrect = true
                        viewModel.rewardGameWin(60, 20)
                        viewModel.speakAudio(targetSentence)
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
                Text(
                    text = if (correct) "🎉 Perfect Grammar! Target sentence matched." else "❌ Not quite right yet. Try rearranging the chips.",
                    color = if (correct) EmeraldSuccess else CoralAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun ListenAndChooseGame(viewModel: MainViewModel) {
    val question = "Listen to the phrase and identify what is being ordered:"
    val targetAudio = "I would like a large cappuccino with cinnamon sprinkle."
    val options = listOf("Hot chocolate with cream", "Large cappuccino with cinnamon", "Iced latte with caramel", "Black Americano coffee")
    var selectedOption by remember { mutableStateOf<Int?>(null) }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(question, fontWeight = FontWeight.Bold, fontSize = 15.sp)

            Button(
                onClick = { viewModel.speakAudio(targetAudio) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.VolumeUp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Play Listening Audio 🔊")
            }

            options.forEachIndexed { index, opt ->
                val isSel = selectedOption == index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSel) Color(0xFFDBEAFE) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedOption = index
                            if (index == 1) {
                                viewModel.rewardGameWin(40, 15)
                            }
                        }
                ) {
                    Text(opt, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(14.dp))
                }
            }
        }
    }
}

@Composable
fun PronunciationBattleGame(viewModel: MainViewModel) {
    val challengeSentence = "The courageous astronaut explored uncharted territory"
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
            Text("⚔️ Pronunciation Battle Arena", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = RoyalBlue)
            Text("Score above 80% pronunciation accuracy to conquer this battle!", fontSize = 12.sp, color = MutedGray, textAlign = TextAlign.Center)

            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEFF6FF),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "\"$challengeSentence\"",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepNavy,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }

            IconButton(onClick = { viewModel.speakAudio(challengeSentence) }) {
                Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen to Guide", tint = RoyalBlue)
            }

            MicrophoneButton(
                speechState = speechState,
                onStart = { viewModel.startSpeakingPractice() },
                onStop = { viewModel.stopSpeakingPractice() },
                onSimulate = { viewModel.simulateVoiceInput(it) },
                samplePrompt = challengeSentence
            )

            evaluation?.let { eval ->
                Text(
                    "Accuracy: ${eval.pronunciationScore}%",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (eval.pronunciationScore >= 80) EmeraldSuccess else OrangeVibrant
                )
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
                    Text("Select the most suitable formal greeting for a business client:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
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
