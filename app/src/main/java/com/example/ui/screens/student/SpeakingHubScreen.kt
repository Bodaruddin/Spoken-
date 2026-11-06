package com.example.ui.screens.student

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PronunciationDrill
import com.example.data.model.RoleplayScenario
import com.example.data.model.SpeakingPrompt
import com.example.service.PhoneticsAiEngine
import com.example.service.PhoneticSearchResult
import com.example.service.SpeechState
import com.example.ui.MainViewModel
import com.example.ui.SpeakingSubTab
import com.example.ui.components.MicrophoneButton
import com.example.ui.components.SpeakingEvaluationCard
import com.example.ui.theme.*

@Composable
fun SpeakingHubScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentSubTab by viewModel.speakingSubTab.collectAsState()
    val speechState by viewModel.speechManager.speechState.collectAsState()
    val lastEvaluation by viewModel.lastEvaluation.collectAsState()
    val roleplays by viewModel.roleplays.collectAsState()
    val activeRoleplay by viewModel.activeRoleplay.collectAsState()
    val roleplayTurnIndex by viewModel.roleplayTurnIndex.collectAsState()
    val pronunciationDrills by viewModel.pronunciationDrills.collectAsState()
    val activeDrill by viewModel.activePronunciationDrill.collectAsState()
    val speakingPrompts by viewModel.speakingPrompts.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("speaking_hub_screen")
    ) {
        // Sub-tabs Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SpeakingTabChip("AI Partner", "🤖", currentSubTab == SpeakingSubTab.AI_PARTNER) {
                    viewModel.selectSpeakingSubTab(SpeakingSubTab.AI_PARTNER)
                }
            }
            item {
                SpeakingTabChip("Daily Challenge", "⚡", currentSubTab == SpeakingSubTab.DAILY_CHALLENGE) {
                    viewModel.selectSpeakingSubTab(SpeakingSubTab.DAILY_CHALLENGE)
                }
            }
            item {
                SpeakingTabChip("Roleplay", "🎭", currentSubTab == SpeakingSubTab.ROLEPLAY) {
                    viewModel.selectSpeakingSubTab(SpeakingSubTab.ROLEPLAY)
                }
            }
            item {
                SpeakingTabChip("Pronunciation", "🎯", currentSubTab == SpeakingSubTab.PRONUNCIATION_COACH) {
                    viewModel.selectSpeakingSubTab(SpeakingSubTab.PRONUNCIATION_COACH)
                }
            }
            item {
                SpeakingTabChip("Picture Describe", "🖼️", currentSubTab == SpeakingSubTab.PICTURE_DESCRIPTION) {
                    viewModel.selectSpeakingSubTab(SpeakingSubTab.PICTURE_DESCRIPTION)
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (currentSubTab) {
                SpeakingSubTab.AI_PARTNER -> {
                    item {
                        AiPartnerSection(
                            viewModel = viewModel,
                            speechState = speechState,
                            evaluation = lastEvaluation
                        )
                    }
                }
                SpeakingSubTab.DAILY_CHALLENGE -> {
                    item {
                        DailyChallengeSection(
                            prompts = speakingPrompts,
                            viewModel = viewModel,
                            speechState = speechState,
                            evaluation = lastEvaluation
                        )
                    }
                }
                SpeakingSubTab.ROLEPLAY -> {
                    item {
                        RoleplaySection(
                            roleplays = roleplays,
                            activeRoleplay = activeRoleplay,
                            turnIndex = roleplayTurnIndex,
                            viewModel = viewModel,
                            speechState = speechState,
                            evaluation = lastEvaluation
                        )
                    }
                }
                SpeakingSubTab.PRONUNCIATION_COACH -> {
                    item {
                        PronunciationSection(
                            drills = pronunciationDrills,
                            activeDrill = activeDrill,
                            viewModel = viewModel,
                            speechState = speechState,
                            evaluation = lastEvaluation
                        )
                    }
                }
                SpeakingSubTab.PICTURE_DESCRIPTION -> {
                    item {
                        PictureDescriptionSection(
                            viewModel = viewModel,
                            speechState = speechState,
                            evaluation = lastEvaluation
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpeakingTabChip(
    title: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .clickable { onClick() }
            .testTag("speaking_tab_${title.lowercase().replace(" ", "_")}")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(emoji, fontSize = 14.sp)
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AiPartnerSection(
    viewModel: MainViewModel,
    speechState: SpeechState,
    evaluation: com.example.data.model.SpeakingEvaluation?
) {
    var aiQuestion by remember { mutableStateOf("Hi Alex! How was your weekend? Did you do anything fun or exciting?") }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // AI Chat Bubble Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(RoyalBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 20.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Nova • AI Speaking Coach", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Real-time voice conversation partner", fontSize = 11.sp, color = MutedGray)
                    }
                    IconButton(
                        onClick = { viewModel.speakAudio(aiQuestion) },
                        modifier = Modifier.testTag("ai_listen_button")
                    ) {
                        Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen to AI", tint = RoyalBlue)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"$aiQuestion\"",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = DeepNavy,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                // Sample prompt suggestions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SuggestionChip(
                        onClick = {
                            aiQuestion = "Could you tell me about your favorite hobbies and why you enjoy them?"
                            viewModel.speakAudio(aiQuestion)
                        },
                        label = { Text("Hobbies Topic", fontSize = 11.sp) }
                    )
                    SuggestionChip(
                        onClick = {
                            aiQuestion = "What kind of job do you do, and what do you like most about your work?"
                            viewModel.speakAudio(aiQuestion)
                        },
                        label = { Text("Career Topic", fontSize = 11.sp) }
                    )
                }
            }
        }

        // Microphone Action & Speech Recognition
        MicrophoneButton(
            speechState = speechState,
            onStart = { viewModel.startSpeakingPractice() },
            onStop = { viewModel.stopSpeakingPractice() },
            onSimulate = { viewModel.simulateVoiceInput(it) },
            samplePrompt = "My weekend was really relaxing. I went hiking in the national park with my friends and cooked dinner."
        )

        // Live Evaluation Display
        evaluation?.let { eval ->
            SpeakingEvaluationCard(
                evaluation = eval,
                onAudioPlayExpected = {
                    viewModel.speakAudio("My weekend was very relaxing. I went hiking with my friends and we cooked a delicious meal together.")
                }
            )
        }
    }
}

@Composable
fun DailyChallengeSection(
    prompts: List<SpeakingPrompt>,
    viewModel: MainViewModel,
    speechState: SpeechState,
    evaluation: com.example.data.model.SpeakingEvaluation?
) {
    var selectedPrompt by remember { mutableStateOf(prompts.first()) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Prompt Selector
        Text("Select Speaking Topic", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(prompts) { p ->
                val isSel = p.id == selectedPrompt.id
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { selectedPrompt = p }
                ) {
                    Text(
                        p.title,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Active Prompt Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedPrompt.category, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    IconButton(onClick = { viewModel.speakAudio(selectedPrompt.question) }) {
                        Icon(Icons.Rounded.VolumeUp, contentDescription = "Play Prompt", tint = RoyalBlue)
                    }
                }

                Text(selectedPrompt.question, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("💡 Tip: ${selectedPrompt.followUpTip}", fontSize = 12.sp, color = MutedGray)

                // Recommended keywords pills
                Text("Recommended Vocabulary:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    selectedPrompt.recommendedKeywords.forEach { kw ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(kw, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), fontSize = 11.sp, color = AmberGold, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        MicrophoneButton(
            speechState = speechState,
            onStart = { viewModel.startSpeakingPractice() },
            onStop = { viewModel.stopSpeakingPractice() },
            onSimulate = { viewModel.simulateVoiceInput(it) },
            samplePrompt = "I am very passionate about learning English. In the mornings I usually prepare a nutritious breakfast and review my flashcards."
        )

        evaluation?.let { eval ->
            SpeakingEvaluationCard(evaluation = eval)
        }
    }
}

@Composable
fun RoleplaySection(
    roleplays: List<RoleplayScenario>,
    activeRoleplay: RoleplayScenario?,
    turnIndex: Int,
    viewModel: MainViewModel,
    speechState: SpeechState,
    evaluation: com.example.data.model.SpeakingEvaluation?
) {
    if (activeRoleplay == null) {
        // List roleplay scenarios
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Interactive Roleplay Scenarios", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            roleplays.forEach { scenario ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.startRoleplay(scenario) }
                        .testTag("roleplay_card_${scenario.id}")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFEDE9FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(scenario.emoji, fontSize = 24.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(scenario.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Text(scenario.situation, fontSize = 12.sp, color = MutedGray)
                            Spacer(Modifier.height(4.dp))
                            Text("Partner: ${scenario.partnerRole} • Level: ${scenario.difficulty}", fontSize = 11.sp, color = RoyalBlue)
                        }
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MutedGray)
                    }
                }
            }
        }
    } else {
        // Active Roleplay Runner
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(activeRoleplay.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Partner: ${activeRoleplay.partnerRole}", fontSize = 12.sp, color = MutedGray)
                }
                IconButton(onClick = { viewModel.exitRoleplay() }) {
                    Icon(Icons.Filled.Close, contentDescription = "Exit")
                }
            }

            // Current AI Line
            val aiTurn = activeRoleplay.turns.getOrNull(turnIndex)
            val userTurn = activeRoleplay.turns.getOrNull(turnIndex + 1)

            aiTurn?.let { turn ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🤖", fontSize = 24.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(activeRoleplay.partnerRole, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                            Text(turn.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        }
                        IconButton(onClick = { viewModel.speakAudio(turn.text) }) {
                            Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen", tint = RoyalBlue)
                        }
                    }
                }
            }

            // User Turn Target
            userTurn?.let { uTurn ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("YOUR TURN TO SPEAK:", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = RoyalBlue)
                        Text("\"${uTurn.text}\"", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Keywords to use: ${uTurn.expectedKeywords.joinToString(", ")}", fontSize = 11.sp, color = MutedGray)
                    }
                }

                MicrophoneButton(
                    speechState = speechState,
                    onStart = { viewModel.startSpeakingPractice() },
                    onStop = { viewModel.stopSpeakingPractice() },
                    onSimulate = { viewModel.simulateVoiceInput(it) },
                    samplePrompt = uTurn.text
                )
            }

            evaluation?.let { eval ->
                SpeakingEvaluationCard(
                    evaluation = eval,
                    onAudioPlayExpected = { userTurn?.text?.let { viewModel.speakAudio(it) } }
                )

                if (eval.overallScore >= 70) {
                    Button(
                        onClick = { viewModel.nextRoleplayTurn() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("next_roleplay_turn_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                    ) {
                        Text("Next Dialogue Turn ➔", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun PronunciationSection(
    drills: List<PronunciationDrill>,
    activeDrill: PronunciationDrill?,
    viewModel: MainViewModel,
    speechState: SpeechState,
    evaluation: com.example.data.model.SpeakingEvaluation?
) {
    var queryInput by remember { mutableStateOf("") }
    val searchedResult by viewModel.searchedPhoneticResult.collectAsState()
    val isSearching by viewModel.isSearchingPhonetics.collectAsState()
    var selectedCategory by remember { mutableStateOf("🔥 Trending") }
    var actionToast by remember { mutableStateOf<String?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // AI Phonetics Word Search Card
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("ai_phonetics_search_card")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.linearGradient(listOf(PurpleAccent, RoyalBlue))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔍", fontSize = 20.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "AI Phonetics Word Search",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "Type ANY word to look up IPA, syllables & native speech tips",
                            fontSize = 11.sp,
                            color = MutedGray
                        )
                    }
                }

                // Search Input Field
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = queryInput,
                        onValueChange = {
                            queryInput = it
                            viewModel.updatePhoneticSearchQuery(it)
                        },
                        placeholder = {
                            Text(
                                "Search word (e.g. thorough)...",
                                fontSize = 13.sp,
                                color = MutedGray,
                                maxLines = 1
                            )
                        },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            if (queryInput.isNotEmpty()) {
                                IconButton(
                                    onClick = {
                                        queryInput = ""
                                        viewModel.clearPhoneticsSearch()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("phonetic_search_text_field")
                    )

                    Button(
                        onClick = {
                            if (queryInput.isNotBlank()) {
                                viewModel.searchPhonetics(queryInput)
                            }
                        },
                        enabled = queryInput.isNotBlank() && !isSearching,
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("phonetic_search_button")
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("✨ Analyze", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Quick Category Selectors
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val categories = listOf("🔥 Trending", "🤫 Silent Letters", "👅 Tricky 'TH'", "💼 Business & Tech", "🌀 Tongue Twisters")
                    items(categories) { cat ->
                        val isSel = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) RoyalBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSel) BorderStroke(1.dp, RoyalBlue) else null,
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Text(
                                cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Category Word Suggestions Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val cleanCat = selectedCategory.replace(Regex("[^a-zA-Z ']+"), "").trim()
                    val suggestions = PhoneticsAiEngine.getWordsByCategory(cleanCat)
                    items(suggestions) { word ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable {
                                queryInput = word
                                viewModel.searchPhonetics(word)
                            }
                        ) {
                            Text(
                                word,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = RoyalBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // AI Word Search Result Card
                searchedResult?.let { res ->
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                        border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("searched_phonetic_result_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        res.word,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 24.sp,
                                        color = RoyalBlue
                                    )
                                    Text(
                                        res.syllables,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = PurpleAccent.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        res.ipa,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = PurpleAccent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Sound Tags
                            if (res.soundTags.isNotEmpty()) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    res.soundTags.forEach { tag ->
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = EmeraldSuccess.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                tag,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldSuccess,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Focus: ${res.audioFocus} • Level: ${res.difficulty}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                                    Spacer(Modifier.height(3.dp))
                                    Text("Speech Coach: ${res.coachingTip}", fontSize = 12.sp, color = Color(0xFFE2E8F0))
                                }
                            }

                            if (res.assameseMeaning.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E1B4B).copy(alpha = 0.8f),
                                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.6f))
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text("অসমীয়া অৰ্থ (Assamese Meaning):", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AmberGold)
                                        val displayAssamese = when {
                                            res.assameseTransliteration.isBlank() -> res.assameseMeaning
                                            res.assameseMeaning.contains("(") -> res.assameseMeaning
                                            res.assameseMeaning.contains(res.assameseTransliteration, ignoreCase = true) -> res.assameseMeaning
                                            else -> "${res.assameseMeaning} (${res.assameseTransliteration})"
                                        }
                                        Text(
                                            displayAssamese,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            // Row 1: Audio Playback Speed Options
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledTonalButton(
                                    onClick = { viewModel.speakAudio(res.word, slow = true) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("0.7x Slow", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = { viewModel.speakAudio(res.word, slow = false) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Normal Speed", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // Row 2: Practice & Drill Actions (Spacious and Responsive)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.addSearchedWordToDrills(res)
                                        actionToast = "Saved '${res.word}' to practice drills!"
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.2.dp, RoyalBlue.copy(alpha = 0.85f)),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("Add to Drills", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                }

                                Button(
                                    onClick = {
                                        val drill = PronunciationDrill(
                                            id = "custom_${res.word}",
                                            wordOrSentence = res.word,
                                            ipaPhonetic = res.ipa,
                                            audioFocus = res.audioFocus,
                                            difficulty = res.difficulty,
                                            tips = res.coachingTip
                                        )
                                        viewModel.startPronunciationDrill(drill)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                    modifier = Modifier.weight(1f),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp)
                                ) {
                                    Icon(Icons.Filled.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(5.dp))
                                    Text("Practice Now", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }

                // Toast Feedback
                AnimatedVisibility(visible = actionToast != null) {
                    actionToast?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                msg,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Curated Practice Drills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📚 Practice Drills & Accent Coach", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    "${drills.size} drills available",
                    fontSize = 11.sp,
                    color = MutedGray,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Drills Grid / List
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(drills) { drill ->
                val isSel = activeDrill?.id == drill.id
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isSel) 4.dp else 1.dp),
                    modifier = Modifier
                        .clickable { viewModel.startPronunciationDrill(drill) }
                        .testTag("drill_card_${drill.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(drill.wordOrSentence, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(drill.ipaPhonetic, fontSize = 12.sp, color = RoyalBlue)
                        Text("Focus: ${drill.audioFocus}", fontSize = 10.sp, color = MutedGray)
                    }
                }
            }
        }

        // Active Drill Interaction
        activeDrill?.let { drill ->
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RoyalBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "Active Practice",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.closePronunciationDrill() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Close", tint = MutedGray, modifier = Modifier.size(16.dp))
                        }
                    }

                    Text(drill.wordOrSentence, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp, color = RoyalBlue)
                    Text(drill.ipaPhonetic, fontSize = 16.sp, color = PurpleAccent, fontWeight = FontWeight.SemiBold)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { viewModel.speakAudio(drill.wordOrSentence, slow = true) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Slow (0.7x)")
                        }

                        Button(
                            onClick = { viewModel.speakAudio(drill.wordOrSentence, slow = false) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Normal")
                        }
                    }

                    Text("Coaching Tip: ${drill.tips}", fontSize = 12.sp, color = MutedGray, textAlign = TextAlign.Center)
                }
            }

            MicrophoneButton(
                speechState = speechState,
                onStart = { viewModel.startSpeakingPractice() },
                onStop = { viewModel.stopSpeakingPractice() },
                onSimulate = { viewModel.simulateVoiceInput(it) },
                samplePrompt = drill.wordOrSentence
            )

            evaluation?.let { eval ->
                SpeakingEvaluationCard(
                    evaluation = eval,
                    onAudioPlayExpected = { viewModel.speakAudio(drill.wordOrSentence) }
                )
            }
        }
    }
}

@Composable
fun PictureDescriptionSection(
    viewModel: MainViewModel,
    speechState: SpeechState,
    evaluation: com.example.data.model.SpeakingEvaluation?
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("🖼️ Picture Description Challenge", fontWeight = FontWeight.Bold, fontSize = 16.sp)

                // Whimsical illustration box representing the picture prompt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color(0xFF38BDF8), Color(0xFF6366F1)))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("☕ 🥐 🌳 🚲", fontSize = 32.sp)
                        Text(
                            "A sunny Sunday morning at a bustling Parisian sidewalk café",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Text(
                    "Describe what you see in the scene. Who is there? What are they doing? What is the atmosphere like?",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        MicrophoneButton(
            speechState = speechState,
            onStart = { viewModel.startSpeakingPractice() },
            onStop = { viewModel.stopSpeakingPractice() },
            onSimulate = { viewModel.simulateVoiceInput(it) },
            samplePrompt = "In this picture, I can see people enjoying warm coffee and fresh croissants at a charming outdoor café on a bright sunny day."
        )

        evaluation?.let { eval ->
            SpeakingEvaluationCard(evaluation = eval)
        }
    }
}
