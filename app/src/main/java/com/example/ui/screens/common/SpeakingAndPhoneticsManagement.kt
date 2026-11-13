package com.example.ui.screens.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DialogueTurn
import com.example.data.model.PronunciationDrill
import com.example.data.model.Role
import com.example.data.model.RoleplayScenario
import com.example.data.model.SpeakingPrompt
import com.example.data.model.VocabularyWord
import com.example.service.PhoneticsAiEngine
import com.example.ui.MainViewModel
import com.example.ui.theme.*

enum class SpeakingManageTab(val title: String, val emoji: String) {
    SPEAKING_PROMPTS("Conversation Topics", "💬"),
    PHONETICS_DRILLS("Phonetics & Drills", "🎯"),
    VOCAB_ASSAMESE("Assamese Words", "📖"),
    ENGLISH_ADVENTURE("Adventure Worlds", "🗺️")
}

@Composable
fun SpeakingAndPhoneticsManagement(
    viewModel: MainViewModel,
    userRole: Role,
    modifier: Modifier = Modifier,
    initialTab: SpeakingManageTab = SpeakingManageTab.SPEAKING_PROMPTS
) {
    var currentTab by remember { mutableStateOf(initialTab) }
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Dialog States
    var showCreatePromptDialog by remember { mutableStateOf(false) }
    var editingPrompt by remember { mutableStateOf<SpeakingPrompt?>(null) }
    var promptToDelete by remember { mutableStateOf<SpeakingPrompt?>(null) }

    var showCreateDrillDialog by remember { mutableStateOf(false) }
    var editingDrill by remember { mutableStateOf<PronunciationDrill?>(null) }
    var drillToDelete by remember { mutableStateOf<PronunciationDrill?>(null) }

    var showCreateRoleplayDialog by remember { mutableStateOf(false) }
    var roleplayToDelete by remember { mutableStateOf<RoleplayScenario?>(null) }

    var showCreateVocabDialog by remember { mutableStateOf(false) }
    var showImportExcelDialog by remember { mutableStateOf(false) }
    var vocabToDelete by remember { mutableStateOf<VocabularyWord?>(null) }

    val prompts by viewModel.speakingPrompts.collectAsState()
    val drills by viewModel.pronunciationDrills.collectAsState()
    val roleplays by viewModel.roleplays.collectAsState()
    val adventureWorlds by viewModel.adventureWorlds.collectAsState()
    val vocabularyWords by viewModel.vocabularyWords.collectAsState()
    val milestoneTarget by viewModel.novaMilestoneTarget.collectAsState()
    val cloudNotice by viewModel.cloudSyncNotice.collectAsState()

    LaunchedEffect(cloudNotice) {
        cloudNotice?.let {
            toastMessage = it
            viewModel.clearCloudSyncNotice()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("speaking_phonetics_management_screen")
    ) {
        // Management Header Banner
        Card(
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(RoyalBlue, PurpleAccent))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎙️", fontSize = 20.sp)
                        }
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                "AI Speaking & Phonetics Hub",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                if (userRole == Role.ADMIN) "Global Curriculum & Speech Admin"
                                else "Teacher Speech Course Management",
                                fontSize = 11.sp,
                                color = MutedGray,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Contextual New Button (Single line, fully visible)
                    Button(
                        onClick = {
                            when (currentTab) {
                                SpeakingManageTab.SPEAKING_PROMPTS -> showCreatePromptDialog = true
                                SpeakingManageTab.PHONETICS_DRILLS -> showCreateDrillDialog = true
                                SpeakingManageTab.VOCAB_ASSAMESE -> showCreateVocabDialog = true
                                SpeakingManageTab.ENGLISH_ADVENTURE -> { /* Handled in AdventureWorldsManagementList */ }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        modifier = Modifier.testTag("create_item_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Text(
                                text = when (currentTab) {
                                    SpeakingManageTab.SPEAKING_PROMPTS -> "New Topic"
                                    SpeakingManageTab.PHONETICS_DRILLS -> "New Drill"
                                    SpeakingManageTab.VOCAB_ASSAMESE -> "Add Word"
                                    SpeakingManageTab.ENGLISH_ADVENTURE -> "Adventure"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                // Segmented Tab Selector (Single line, horizontally scrollable, no text wrapping)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .horizontalScroll(rememberScrollState())
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SpeakingManageTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        val count = when (tab) {
                            SpeakingManageTab.SPEAKING_PROMPTS -> prompts.size
                            SpeakingManageTab.PHONETICS_DRILLS -> drills.size
                            SpeakingManageTab.VOCAB_ASSAMESE -> vocabularyWords.size
                            SpeakingManageTab.ENGLISH_ADVENTURE -> adventureWorlds.size
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) RoyalBlue else Color.Transparent,
                            modifier = Modifier
                                .clickable { currentTab = tab }
                                .testTag("manage_tab_${tab.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tab.emoji, fontSize = 13.sp)
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color.White.copy(alpha = 0.25f)
                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Action Toast Feedback
        AnimatedVisibility(visible = toastMessage != null) {
            toastMessage?.let { msg ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDCFCE7),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(16.dp))
                            Text(msg, color = EmeraldSuccess, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { toastMessage = null }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        // Tab Content
        when (currentTab) {
            SpeakingManageTab.SPEAKING_PROMPTS -> {
                PromptsManagementList(
                    prompts = prompts,
                    milestoneTarget = milestoneTarget,
                    canManageMilestone = userRole == Role.TEACHER || userRole == Role.ADMIN,
                    onIncreaseMilestone = { viewModel.increaseNovaMilestone() },
                    onDecreaseMilestone = { viewModel.decreaseNovaMilestone() },
                    onSetMilestoneTarget = { viewModel.setNovaMilestoneTarget(it) },
                    onEdit = { editingPrompt = it },
                    onDelete = { promptToDelete = it },
                    onSpeakAudio = { viewModel.speakAudio(it) }
                )
            }
            SpeakingManageTab.PHONETICS_DRILLS -> {
                DrillsManagementList(
                    drills = drills,
                    onEdit = { editingDrill = it },
                    onDelete = { drillToDelete = it },
                    onSpeakAudio = { text, slow -> viewModel.speakAudio(text, slow) },
                    onQuickSearchAdd = { word ->
                        val result = PhoneticsAiEngine.analyzeWord(word)
                        viewModel.addSearchedWordToDrills(result)
                        toastMessage = "Added '$word' with AI IPA to phonetics drills!"
                    }
                )
            }
            SpeakingManageTab.VOCAB_ASSAMESE -> {
                TeacherVocabularyManagementList(
                    vocabularyWords = vocabularyWords,
                    onAddNew = { showCreateVocabDialog = true },
                    onOpenExcelImport = { showImportExcelDialog = true },
                    onDelete = { vocabToDelete = it },
                    onSpeakAudio = { word, slow -> viewModel.speakAudio(word, slow) }
                )
            }
            SpeakingManageTab.ENGLISH_ADVENTURE -> {
                AdventureWorldsManagementList(
                    viewModel = viewModel
                )
            }
        }
    }

    // -------------------------------------------------------------
    // DIALOGS
    // -------------------------------------------------------------

    // Create Teacher Vocabulary Word Dialog (with Assamese Meaning!)
    if (showCreateVocabDialog) {
        CreateTeacherVocabularyWordDialog(
            onDismiss = { showCreateVocabDialog = false },
            onOpenExcelImport = {
                showCreateVocabDialog = false
                showImportExcelDialog = true
            },
            onCreate = { word, assamese, transliteration, ipa, syllables, pos, cat, tip, ex, meaning ->
                viewModel.addTeacherVocabularyWord(
                    word = word,
                    assameseMeaning = assamese,
                    assameseTransliteration = transliteration,
                    phonetic = ipa,
                    syllables = syllables,
                    partOfSpeech = pos,
                    category = cat,
                    coachingTip = tip,
                    exampleSentence = ex,
                    meaning = meaning
                )
                showCreateVocabDialog = false
                toastMessage = "Added '$word' ($assamese) to English-Assamese dictionary!"
            }
        )
    }

    // Import English to Assamese Words Excel / CSV Dialog
    if (showImportExcelDialog) {
        ImportAssameseWordsExcelDialog(
            onDismiss = { showImportExcelDialog = false },
            onImportWords = { parsedRows ->
                val count = viewModel.batchImportVocabularyWords(parsedRows)
                showImportExcelDialog = false
                toastMessage = "Successfully imported $count words into English-Assamese dictionary!"
            }
        )
    }

    // Delete Vocabulary Confirmation
    vocabToDelete?.let { vocab ->
        AlertDialog(
            onDismissRequest = { vocabToDelete = null },
            title = { Text("Delete Vocabulary Word?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove '${vocab.word}' (${vocab.assameseMeaning}) from the academy dictionary?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTeacherVocabularyWord(vocab.id)
                        vocabToDelete = null
                        toastMessage = "Word deleted from dictionary."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { vocabToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create Speaking Prompt Dialog
    if (showCreatePromptDialog) {
        CreateSpeakingPromptDialog(
            defaultMilestone = milestoneTarget,
            onDismiss = { showCreatePromptDialog = false },
            onCreate = { title, cat, q, tip, kw, sec, emoji, diff, milestone ->
                viewModel.addSpeakingPrompt(
                    title = title,
                    category = cat,
                    question = q,
                    followUpTip = tip,
                    keywords = kw,
                    minSeconds = sec,
                    emoji = emoji,
                    difficultyLevel = diff,
                    milestoneTurns = milestone
                )
                showCreatePromptDialog = false
                toastMessage = "New Conversation Topic '$title' published for students!"
            }
        )
    }

    // Edit Speaking Prompt Dialog
    editingPrompt?.let { prompt ->
        EditSpeakingPromptDialog(
            prompt = prompt,
            defaultMilestone = milestoneTarget,
            onDismiss = { editingPrompt = null },
            onSave = { updated ->
                viewModel.updateSpeakingPrompt(updated)
                editingPrompt = null
                toastMessage = "Speaking prompt updated successfully!"
            }
        )
    }

    // Delete Prompt Confirmation
    promptToDelete?.let { prompt ->
        AlertDialog(
            onDismissRequest = { promptToDelete = null },
            title = { Text("Delete Speaking Prompt?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to remove '${prompt.title}'? Students will no longer see this prompt in daily challenges.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSpeakingPrompt(prompt.id)
                        promptToDelete = null
                        toastMessage = "Speaking prompt deleted."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { promptToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create Phonetic Drill Dialog (with AI Auto-Fill!)
    if (showCreateDrillDialog) {
        CreatePhoneticDrillDialog(
            onDismiss = { showCreateDrillDialog = false },
            onCreate = { word, ipa, focus, diff, tip ->
                viewModel.addPronunciationDrill(word, ipa, focus, diff, tip)
                showCreateDrillDialog = false
                toastMessage = "Phonetic drill for '$word' created!"
            },
            onSpeak = { text, slow -> viewModel.speakAudio(text, slow) }
        )
    }

    // Edit Phonetic Drill Dialog
    editingDrill?.let { drill ->
        EditPhoneticDrillDialog(
            drill = drill,
            onDismiss = { editingDrill = null },
            onSave = { updated ->
                viewModel.updatePronunciationDrill(updated)
                editingDrill = null
                toastMessage = "Phonetic drill updated!"
            },
            onSpeak = { text, slow -> viewModel.speakAudio(text, slow) }
        )
    }

    // Delete Drill Confirmation
    drillToDelete?.let { drill ->
        AlertDialog(
            onDismissRequest = { drillToDelete = null },
            title = { Text("Delete Phonetic Drill?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${drill.wordOrSentence}' from the pronunciation curriculum?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePronunciationDrill(drill.id)
                        drillToDelete = null
                        toastMessage = "Phonetic drill deleted."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { drillToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Create Roleplay Dialog
    if (showCreateRoleplayDialog) {
        CreateRoleplayScenarioDialog(
            onDismiss = { showCreateRoleplayDialog = false },
            onCreate = { title, sit, part, user, diff, emoji, turns ->
                viewModel.addRoleplayScenario(title, sit, part, user, diff, emoji, turns)
                showCreateRoleplayDialog = false
                toastMessage = "Roleplay '$title' created!"
            }
        )
    }

    // Delete Roleplay Confirmation
    roleplayToDelete?.let { rp ->
        AlertDialog(
            onDismissRequest = { roleplayToDelete = null },
            title = { Text("Delete Roleplay Scenario?", fontWeight = FontWeight.Bold) },
            text = { Text("Remove scenario '${rp.title}' from students' speaking hub?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteRoleplayScenario(rp.id)
                        roleplayToDelete = null
                        toastMessage = "Roleplay deleted."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { roleplayToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// PROMPTS LIST
// -------------------------------------------------------------
@Composable
fun PromptsManagementList(
    prompts: List<SpeakingPrompt>,
    milestoneTarget: Int = 5,
    canManageMilestone: Boolean = true,
    onIncreaseMilestone: () -> Unit = {},
    onDecreaseMilestone: () -> Unit = {},
    onSetMilestoneTarget: (Int) -> Unit = {},
    onEdit: (SpeakingPrompt) -> Unit,
    onDelete: (SpeakingPrompt) -> Unit,
    onSpeakAudio: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filtered = remember(prompts, searchQuery) {
        if (searchQuery.isBlank()) prompts
        else prompts.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.category.contains(searchQuery, ignoreCase = true) ||
                    it.question.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Teacher Milestone Target Management Section (hidden from students)
        if (canManageMilestone) {
            item {
                Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1038)),
                border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)))),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_nova_milestone_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
                            Text("🏆", fontSize = 24.sp)
                            Column {
                                Text(
                                    "Nova AI Chatbot Milestone Goal",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    "Required conversation turns for students to achieve their milestone trophy",
                                    fontSize = 11.sp,
                                    color = Color(0xFFC4B5FD)
                                )
                            }
                        }

                        // Stepper: [-] [ Target ] [+]
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            IconButton(
                                onClick = onDecreaseMilestone,
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFF78350F), shape = RoundedCornerShape(8.dp))
                                    .testTag("teacher_decrease_milestone_btn")
                            ) {
                                Text("-", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF2E1B5B),
                                border = BorderStroke(1.dp, Color(0xFFA855F7))
                            ) {
                                Text(
                                    text = "$milestoneTarget Turns",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFE0E7FF),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }

                            IconButton(
                                onClick = onIncreaseMilestone,
                                modifier = Modifier
                                    .size(34.dp)
                                    .background(Color(0xFF047857), shape = RoundedCornerShape(8.dp))
                                    .testTag("teacher_increase_milestone_btn")
                            ) {
                                Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Quick presets row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            3 to "3 Turns (Quick)",
                            5 to "5 Turns (Standard)",
                            8 to "8 Turns (Advanced)",
                            10 to "10 Turns (Mastery)"
                        ).forEach { (preset, label) ->
                            val isSel = milestoneTarget == preset
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSel) Color(0xFF7C3AED) else Color(0xFF281845),
                                border = BorderStroke(1.dp, if (isSel) Color.White else Color(0xFF4C1D95)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onSetMilestoneTarget(preset) }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else Color(0xFFDDD6FE),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search speaking prompts...", fontSize = 13.sp, color = MutedGray, maxLines = 1) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(20.dp)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                maxLines = 1,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("prompt_search_input")
            )
        }

        if (filtered.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No speaking prompts found. Tap '+ New Prompt' to add one.", color = MutedGray, fontSize = 13.sp)
                }
            }
        }

        items(filtered, key = { it.id }) { prompt ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("prompt_card_${prompt.id}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = RoyalBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    prompt.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            val levelColor = when (prompt.difficultyLevel) {
                                "Beginner" -> EmeraldSuccess
                                "Advanced" -> CoralAccent
                                else -> PurpleAccent
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = levelColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    prompt.difficultyLevel,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = levelColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val effectiveMilestone = if (prompt.milestoneTurns > 0) prompt.milestoneTurns else milestoneTarget
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = PurpleAccent.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "🏆 $effectiveMilestone turns",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleAccent,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Rounded.Timer, contentDescription = null, modifier = Modifier.size(13.dp), tint = MutedGray)
                                    Spacer(Modifier.width(3.dp))
                                    Text("Min ${prompt.minSpeakingSeconds}s", fontSize = 10.sp, color = MutedGray)
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(prompt.emoji, fontSize = 22.sp)
                        Text(prompt.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Text(prompt.question, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    if (prompt.recommendedKeywords.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Keywords:", fontSize = 11.sp, color = MutedGray, fontWeight = FontWeight.SemiBold)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                items(prompt.recommendedKeywords) { kw ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = EmeraldSuccess.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            kw,
                                            fontSize = 10.sp,
                                            color = EmeraldSuccess,
                                            fontWeight = FontWeight.Medium,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (prompt.followUpTip.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("💡", fontSize = 12.sp)
                                Text("Tip: ${prompt.followUpTip}", fontSize = 11.sp, color = MutedGray)
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { onSpeakAudio(prompt.question) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Listen TTS", fontSize = 12.sp)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilledTonalButton(
                                onClick = { onEdit(prompt) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Edit", fontSize = 11.sp)
                            }

                            IconButton(
                                onClick = { onDelete(prompt) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// PHONETICS DRILLS LIST
// -------------------------------------------------------------
@Composable
fun DrillsManagementList(
    drills: List<PronunciationDrill>,
    onEdit: (PronunciationDrill) -> Unit,
    onDelete: (PronunciationDrill) -> Unit,
    onSpeakAudio: (String, Boolean) -> Unit,
    onQuickSearchAdd: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedDiff by remember { mutableStateOf("All") }

    val filtered = remember(drills, searchQuery, selectedDiff) {
        drills.filter { drill ->
            val matchQuery = searchQuery.isBlank() ||
                    drill.wordOrSentence.contains(searchQuery, ignoreCase = true) ||
                    drill.ipaPhonetic.contains(searchQuery, ignoreCase = true) ||
                    drill.audioFocus.contains(searchQuery, ignoreCase = true)
            val matchDiff = selectedDiff == "All" || drill.difficulty.equals(selectedDiff, ignoreCase = true)
            matchQuery && matchDiff
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Quick AI Generator Pill suggestions
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✨", fontSize = 16.sp)
                        Text("1-Tap AI Phonetic Word Importer:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = RoyalBlue)
                    }
                    Text("Tap any commonly mispronounced word to immediately auto-generate its IPA transcription and curriculum coaching tips:", fontSize = 11.sp, color = MutedGray)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val suggestions = listOf("Phenomenon", "Worcestershire", "Entrepreneur", "Specifically", "Hierarchy", "Comfortable", "Resilience")
                        items(suggestions) { word ->
                            val alreadyAdded = drills.any { it.wordOrSentence.equals(word, ignoreCase = true) }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (alreadyAdded) MaterialTheme.colorScheme.surfaceVariant else RoyalBlue,
                                modifier = Modifier.clickable(enabled = !alreadyAdded) { onQuickSearchAdd(word) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        word,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (alreadyAdded) MutedGray else Color.White
                                    )
                                    if (alreadyAdded) {
                                        Spacer(Modifier.width(4.dp))
                                        Text("✓", fontSize = 10.sp, color = EmeraldSuccess)
                                    } else {
                                        Spacer(Modifier.width(4.dp))
                                        Text("+", fontSize = 11.sp, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Search & Filter Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter drills by word, sound...", fontSize = 13.sp, color = MutedGray, maxLines = 1) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(20.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    maxLines = 1,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("drill_search_input")
                )

                // Difficulty pills
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Easy", "Medium", "Hard").forEach { diff ->
                        val isSel = selectedDiff == diff
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surface,
                            border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.clickable { selectedDiff = diff }
                        ) {
                            Text(
                                diff,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        items(filtered, key = { it.id }) { drill ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("drill_card_${drill.id}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(drill.wordOrSentence, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = RoyalBlue)
                            Text(drill.ipaPhonetic, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = PurpleAccent)
                        }

                        val diffColor = when (drill.difficulty.lowercase()) {
                            "easy" -> EmeraldSuccess
                            "hard" -> OrangeVibrant
                            else -> AmberGold
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = diffColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                drill.difficulty,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = diffColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            "Focus: ${drill.audioFocus}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    if (drill.tips.isNotBlank()) {
                        Text("Coaching tip: ${drill.tips}", fontSize = 12.sp, color = MutedGray)
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilledTonalButton(
                                onClick = { onSpeakAudio(drill.wordOrSentence, true) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("0.7x Slow", fontSize = 11.sp)
                            }

                            FilledTonalButton(
                                onClick = { onSpeakAudio(drill.wordOrSentence, false) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Normal", fontSize = 11.sp)
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(onClick = { onEdit(drill) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = RoyalBlue, modifier = Modifier.size(16.dp))
                            }
                            IconButton(onClick = { onDelete(drill) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ROLEPLAY LIST
// -------------------------------------------------------------
@Composable
fun RoleplayManagementList(
    scenarios: List<RoleplayScenario>,
    onDelete: (RoleplayScenario) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(scenarios, key = { it.id }) { rp ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(rp.emoji, fontSize = 24.sp)
                            Column {
                                Text(rp.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("${rp.difficulty} • ${rp.partnerRole}", fontSize = 11.sp, color = MutedGray)
                            }
                        }

                        IconButton(onClick = { onDelete(rp) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        }
                    }

                    Text("Situation: ${rp.situation}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            "${rp.turns.size} conversation turns configured",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MutedGray
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// DIALOG IMPLEMENTATIONS
// -------------------------------------------------------------

@Composable
fun CreateSpeakingPromptDialog(
    defaultMilestone: Int = 5,
    onDismiss: () -> Unit,
    onCreate: (title: String, category: String, question: String, tip: String, keywords: List<String>, minSeconds: Int, emoji: String, difficultyLevel: String, milestoneTurns: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Personal Life") }
    var question by remember { mutableStateOf("") }
    var tip by remember { mutableStateOf("") }
    var keywordsInput by remember { mutableStateOf("") }
    var minSeconds by remember { mutableIntStateOf(25) }
    var milestoneTurns by remember(defaultMilestone) { mutableIntStateOf(defaultMilestone.coerceIn(1, 30)) }
    var emoji by remember { mutableStateOf("💬") }
    var difficultyLevel by remember { mutableStateOf("Intermediate") }

    val quickEmojis = listOf("💬", "💖", "💼", "✈️", "🍕", "🤖", "🎬", "⏰", "🎆", "🌿", "⚽", "💡", "📚", "☕", "🌟")
    val difficultyLevels = listOf("Beginner", "Intermediate", "Advanced")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(emoji, fontSize = 22.sp)
                Text("New Conversation Topic", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Emoji Selector
                Text("Select Topic Icon / Emoji:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MutedGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickEmojis) { emo ->
                        val isSelected = emoji == emo
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) Color.White else Color.Transparent),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { emoji = emo }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emo, fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Difficulty Level Chips
                Text("Difficulty Level:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MutedGray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    difficultyLevels.forEach { lvl ->
                        val isSelected = difficultyLevel == lvl
                        val color = when (lvl) {
                            "Beginner" -> EmeraldSuccess
                            "Advanced" -> CoralAccent
                            else -> PurpleAccent
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) color else Color.Transparent),
                            modifier = Modifier.clickable { difficultyLevel = lvl }
                        ) {
                            Text(
                                lvl,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Topic Title * (e.g. Dream Travel)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Travel, Career, Tech)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Starter Question for Nova AI *") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = keywordsInput,
                    onValueChange = { keywordsInput = it },
                    label = { Text("Keywords to Encourage (comma separated)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tip,
                    onValueChange = { tip = it },
                    label = { Text("Follow-up Coaching Tip for Student") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Target Duration: $minSeconds seconds", fontSize = 12.sp, color = MutedGray)
                    Row {
                        IconButton(onClick = { if (minSeconds > 10) minSeconds -= 5 }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                        }
                        IconButton(onClick = { if (minSeconds < 120) minSeconds += 5 }) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Milestone Goal: $milestoneTurns turns", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    Row {
                        IconButton(onClick = { if (milestoneTurns > 1) milestoneTurns -= 1 }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease Milestone")
                        }
                        IconButton(onClick = { if (milestoneTurns < 30) milestoneTurns += 1 }) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase Milestone")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && question.isNotBlank()) {
                        val kwList = keywordsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onCreate(title, category, question, tip, kwList, minSeconds, emoji, difficultyLevel, milestoneTurns)
                    }
                },
                enabled = title.isNotBlank() && question.isNotBlank()
            ) {
                Text("Publish Topic")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditSpeakingPromptDialog(
    prompt: SpeakingPrompt,
    defaultMilestone: Int = 5,
    onDismiss: () -> Unit,
    onSave: (SpeakingPrompt) -> Unit
) {
    var title by remember { mutableStateOf(prompt.title) }
    var category by remember { mutableStateOf(prompt.category) }
    var question by remember { mutableStateOf(prompt.question) }
    var tip by remember { mutableStateOf(prompt.followUpTip) }
    var keywordsInput by remember { mutableStateOf(prompt.recommendedKeywords.joinToString(", ")) }
    var minSeconds by remember { mutableIntStateOf(prompt.minSpeakingSeconds) }
    var milestoneTurns by remember(prompt.id) {
        mutableIntStateOf((if (prompt.milestoneTurns > 0) prompt.milestoneTurns else defaultMilestone).coerceIn(1, 30))
    }
    var emoji by remember { mutableStateOf(prompt.emoji) }
    var difficultyLevel by remember { mutableStateOf(prompt.difficultyLevel) }

    val quickEmojis = listOf("💬", "💖", "💼", "✈️", "🍕", "🤖", "🎬", "⏰", "🎆", "🌿", "⚽", "💡", "📚", "☕", "🌟")
    val difficultyLevels = listOf("Beginner", "Intermediate", "Advanced")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(emoji, fontSize = 22.sp)
                Text("Edit Conversation Topic", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Emoji Selector
                Text("Topic Icon / Emoji:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MutedGray)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(quickEmojis) { emo ->
                        val isSelected = emoji == emo
                        Surface(
                            shape = CircleShape,
                            color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) Color.White else Color.Transparent),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { emoji = emo }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(emo, fontSize = 16.sp)
                            }
                        }
                    }
                }

                // Difficulty Level Chips
                Text("Difficulty Level:", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MutedGray)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    difficultyLevels.forEach { lvl ->
                        val isSelected = difficultyLevel == lvl
                        val color = when (lvl) {
                            "Beginner" -> EmeraldSuccess
                            "Advanced" -> CoralAccent
                            else -> PurpleAccent
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, if (isSelected) color else Color.Transparent),
                            modifier = Modifier.clickable { difficultyLevel = lvl }
                        ) {
                            Text(
                                lvl,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Topic Title") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Speaking Question / Starter") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = keywordsInput,
                    onValueChange = { keywordsInput = it },
                    label = { Text("Keywords (comma separated)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tip,
                    onValueChange = { tip = it },
                    label = { Text("Coaching Tip") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Target Duration: $minSeconds seconds", fontSize = 12.sp, color = MutedGray)
                    Row {
                        IconButton(onClick = { if (minSeconds > 10) minSeconds -= 5 }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease")
                        }
                        IconButton(onClick = { if (minSeconds < 120) minSeconds += 5 }) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase")
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Milestone Goal: $milestoneTurns turns", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    Row {
                        IconButton(onClick = { if (milestoneTurns > 1) milestoneTurns -= 1 }) {
                            Icon(Icons.Filled.Remove, contentDescription = "Decrease Milestone")
                        }
                        IconButton(onClick = { if (milestoneTurns < 30) milestoneTurns += 1 }) {
                            Icon(Icons.Filled.Add, contentDescription = "Increase Milestone")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val kwList = keywordsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                    onSave(prompt.copy(
                        title = title,
                        category = category,
                        question = question,
                        followUpTip = tip,
                        recommendedKeywords = kwList,
                        minSpeakingSeconds = minSeconds,
                        emoji = emoji,
                        difficultyLevel = difficultyLevel,
                        milestoneTurns = milestoneTurns
                    ))
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreatePhoneticDrillDialog(
    onDismiss: () -> Unit,
    onCreate: (word: String, ipa: String, focus: String, difficulty: String, tips: String) -> Unit,
    onSpeak: (String, Boolean) -> Unit
) {
    var word by remember { mutableStateOf("") }
    var ipa by remember { mutableStateOf("") }
    var audioFocus by remember { mutableStateOf("") }
    var difficulty by remember { mutableStateOf("Medium") }
    var tips by remember { mutableStateOf("") }
    var isAutoFilling by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("New Phonetic Drill", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = word,
                        onValueChange = { word = it },
                        label = { Text("Target Word / Sentence *") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    // AI Auto-Fill Button!
                    Button(
                        onClick = {
                            if (word.isNotBlank()) {
                                isAutoFilling = true
                                val analysis = PhoneticsAiEngine.analyzeWord(word)
                                ipa = analysis.ipa
                                audioFocus = analysis.audioFocus
                                difficulty = analysis.difficulty
                                tips = analysis.coachingTip
                                isAutoFilling = false
                            }
                        },
                        enabled = word.isNotBlank(),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Text("✨ Auto-Fill", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                OutlinedTextField(
                    value = ipa,
                    onValueChange = { ipa = it },
                    label = { Text("IPA Phonetic Notation (e.g. /ˈkʌmftəbl/)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = audioFocus,
                    onValueChange = { audioFocus = it },
                    label = { Text("Sound Focus (e.g. 'Silent letters', 'Th-sound')") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Difficulty:", fontSize = 12.sp, color = MutedGray)
                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                        val isSel = difficulty == diff
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { difficulty = diff }
                        ) {
                            Text(
                                diff,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = tips,
                    onValueChange = { tips = it },
                    label = { Text("Pronunciation Coaching Tip") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                if (word.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = { onSpeak(word, true) }) {
                            Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Test Slow", fontSize = 11.sp)
                        }
                        TextButton(onClick = { onSpeak(word, false) }) {
                            Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Test Normal", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (word.isNotBlank()) {
                        onCreate(word, ipa, audioFocus, difficulty, tips)
                    }
                },
                enabled = word.isNotBlank()
            ) {
                Text("Create Drill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditPhoneticDrillDialog(
    drill: PronunciationDrill,
    onDismiss: () -> Unit,
    onSave: (PronunciationDrill) -> Unit,
    onSpeak: (String, Boolean) -> Unit
) {
    var word by remember { mutableStateOf(drill.wordOrSentence) }
    var ipa by remember { mutableStateOf(drill.ipaPhonetic) }
    var audioFocus by remember { mutableStateOf(drill.audioFocus) }
    var difficulty by remember { mutableStateOf(drill.difficulty) }
    var tips by remember { mutableStateOf(drill.tips) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Phonetic Drill", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = word,
                    onValueChange = { word = it },
                    label = { Text("Word / Sentence") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = ipa,
                    onValueChange = { ipa = it },
                    label = { Text("IPA Phonetic") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = audioFocus,
                    onValueChange = { audioFocus = it },
                    label = { Text("Sound Focus") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = tips,
                    onValueChange = { tips = it },
                    label = { Text("Coaching Tip") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(drill.copy(
                        wordOrSentence = word,
                        ipaPhonetic = ipa,
                        audioFocus = audioFocus,
                        difficulty = difficulty,
                        tips = tips
                    ))
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CreateRoleplayScenarioDialog(
    onDismiss: () -> Unit,
    onCreate: (title: String, situation: String, partner: String, user: String, difficulty: String, emoji: String, turns: List<DialogueTurn>) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var situation by remember { mutableStateOf("") }
    var partnerRole by remember { mutableStateOf("Barista") }
    var userRole by remember { mutableStateOf("Customer") }
    var difficulty by remember { mutableStateOf("Beginner") }
    var emoji by remember { mutableStateOf("☕") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Roleplay Scenario", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title *") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = situation,
                    onValueChange = { situation = it },
                    label = { Text("Context & Situation *") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = partnerRole,
                        onValueChange = { partnerRole = it },
                        label = { Text("AI Partner Role") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = userRole,
                        onValueChange = { userRole = it },
                        label = { Text("User Role") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it },
                        label = { Text("Emoji") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = difficulty,
                        onValueChange = { difficulty = it },
                        label = { Text("Level") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && situation.isNotBlank()) {
                        val sampleTurns = listOf(
                            DialogueTurn("AI", "Hello! Welcome, how may I help you today?"),
                            DialogueTurn("USER", "Hi! I am looking for assistance.", listOf("help", "looking")),
                            DialogueTurn("AI", "Certainly, I'd be glad to walk you through everything."),
                            DialogueTurn("USER", "Thank you so much, I appreciate it.", listOf("thank you", "appreciate"))
                        )
                        onCreate(title, situation, partnerRole, userRole, difficulty, emoji, sampleTurns)
                    }
                },
                enabled = title.isNotBlank() && situation.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// -------------------------------------------------------------
// TEACHER VOCABULARY & ASSAMESE TRANSLATION MANAGEMENT
// -------------------------------------------------------------
@Composable
fun TeacherVocabularyManagementList(
    vocabularyWords: List<VocabularyWord>,
    onAddNew: () -> Unit,
    onOpenExcelImport: () -> Unit = {},
    onDelete: (VocabularyWord) -> Unit,
    onSpeakAudio: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredWords = remember(vocabularyWords, searchQuery, selectedCategory) {
        vocabularyWords.filter { word ->
            val matchesCategory = selectedCategory == "All" || word.category.equals(selectedCategory, ignoreCase = true)
            val matchesSearch = searchQuery.isBlank() ||
                word.word.contains(searchQuery.trim(), ignoreCase = true) ||
                word.assameseMeaning.contains(searchQuery.trim(), ignoreCase = true) ||
                word.assameseTransliteration.contains(searchQuery.trim(), ignoreCase = true) ||
                word.meaning.contains(searchQuery.trim(), ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("teacher_vocabulary_management_list"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary & Metric Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, AmberGold.copy(alpha = 0.5f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AmberGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📖", fontSize = 20.sp)
                            }
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "English & Assamese Dictionary",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val totalWordCount = com.example.util.AssameseTranslator.getTotalWordCount()
                                Text(
                                    text = "$totalWordCount+ Active Words with Meaning",
                                    fontSize = 11.sp,
                                    color = AmberGold,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = onOpenExcelImport,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = EmeraldSuccess),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("teacher_import_excel_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("📊", fontSize = 13.sp)
                                    Text(
                                        text = "Import Excel",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }

                            Button(
                                onClick = onAddNew,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("teacher_add_word_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                                    Text(
                                        text = "Add Word",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = "Every word you add here is instantly made searchable for students in the Speaking Hub, complete with Assamese script meaning, Romanized phonetics, and speech coaching tips.",
                        fontSize = 12.sp,
                        color = MutedGray
                    )
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search English or Assamese words...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vocab_search_input")
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                val categories = listOf("All", "Communication", "Mindset", "Workplace", "Academic", "General")
                items(categories) { cat ->
                    val isSel = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Word Count Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Vocabulary Entries (${filteredWords.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Vocabulary List
        items(filteredWords) { word ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth().testTag("vocab_word_${word.id}")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header Row: Word, phonetic, category, delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = word.word,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = RoyalBlue
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PurpleAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = word.phonetic,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PurpleAccent,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = word.partOfSpeech,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { onDelete(word) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        }
                    }

                    // Assamese Meaning Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF1E1B4B).copy(alpha = 0.7f),
                        border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("অসমীয়া অৰ্থ:", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = AmberGold)
                            val displayAssamese = when {
                                word.assameseMeaning.isBlank() -> "অসমীয়া অনুবাদ উপলভ্য"
                                word.assameseTransliteration.isBlank() -> word.assameseMeaning
                                word.assameseMeaning.contains("(") -> word.assameseMeaning
                                else -> "${word.assameseMeaning} (${word.assameseTransliteration})"
                            }
                            Text(
                                text = displayAssamese,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Meaning and Example Sentence
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = word.meaning,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (word.exampleSentence.isNotBlank()) {
                            Text(
                                text = "\"${word.exampleSentence}\"",
                                fontSize = 11.sp,
                                color = MutedGray,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        }
                    }

                    // Audio Playback Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = { onSpeakAudio(word.word, true) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.Speed, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("0.7x Slow", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { onSpeakAudio(word.word, false) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Normal Audio", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateTeacherVocabularyWordDialog(
    onDismiss: () -> Unit,
    onOpenExcelImport: (() -> Unit)? = null,
    onCreate: (
        word: String,
        assameseMeaning: String,
        assameseTransliteration: String,
        phonetic: String,
        syllables: String,
        partOfSpeech: String,
        category: String,
        coachingTip: String,
        exampleSentence: String,
        meaning: String
    ) -> Unit
) {
    var word by remember { mutableStateOf("") }
    var assameseMeaning by remember { mutableStateOf("") }
    var assameseTransliteration by remember { mutableStateOf("") }
    var phonetic by remember { mutableStateOf("") }
    var syllables by remember { mutableStateOf("") }
    var partOfSpeech by remember { mutableStateOf("noun") }
    var category by remember { mutableStateOf("General") }
    var coachingTip by remember { mutableStateOf("") }
    var exampleSentence by remember { mutableStateOf("") }
    var meaning by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("📖", fontSize = 20.sp)
                Text("Add Word with Assamese Meaning", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Quick Excel / CSV Sheet Import Banner Option
                if (onOpenExcelImport != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmeraldSuccess.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenExcelImport() }
                            .testTag("dialog_open_excel_import_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldSuccess,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("📊", fontSize = 16.sp)
                                    }
                                }
                                Column {
                                    Text(
                                        text = "Import from Excel / CSV Sheet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = EmeraldSuccess
                                    )
                                    Text(
                                        text = "Bulk upload English-to-Assamese words table (.xlsx / .csv)",
                                        fontSize = 10.5.sp,
                                        color = MutedGray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // English Word + Auto-detect button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = word,
                        onValueChange = {
                            word = it
                            if (it.isNotBlank()) {
                                val analysis = com.example.service.PhoneticsAiEngine.analyzeWord(it)
                                if (phonetic.isBlank()) phonetic = analysis.ipa
                                if (syllables.isBlank()) syllables = analysis.syllables
                                if (coachingTip.isBlank()) coachingTip = analysis.coachingTip
                                if (assameseMeaning.isBlank() && analysis.assameseMeaning.isNotBlank()) {
                                    assameseMeaning = analysis.assameseMeaning
                                }
                                if (assameseTransliteration.isBlank() && analysis.assameseTransliteration.isNotBlank()) {
                                    assameseTransliteration = analysis.assameseTransliteration
                                }
                            }
                        },
                        label = { Text("English Word *") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f).testTag("teacher_vocab_word_input")
                    )

                    FilledTonalButton(
                        onClick = {
                            if (word.isNotBlank()) {
                                val analysis = com.example.service.PhoneticsAiEngine.analyzeWord(word)
                                phonetic = analysis.ipa
                                syllables = analysis.syllables
                                coachingTip = analysis.coachingTip
                                assameseMeaning = analysis.assameseMeaning
                                assameseTransliteration = analysis.assameseTransliteration
                                if (meaning.isBlank()) meaning = analysis.meaning
                                if (exampleSentence.isBlank()) exampleSentence = analysis.exampleSentence
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("✨ Auto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Assamese Script Meaning
                OutlinedTextField(
                    value = assameseMeaning,
                    onValueChange = { assameseMeaning = it },
                    label = { Text("অসমীয়া অৰ্থ (Assamese Meaning) *") },
                    placeholder = { Text("e.g. আৰামদায়ক / ধুনীয়া") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("teacher_vocab_assamese_input")
                )

                // Assamese Transliteration (Romanized)
                OutlinedTextField(
                    value = assameseTransliteration,
                    onValueChange = { assameseTransliteration = it },
                    label = { Text("Assamese Transliteration (Pronunciation in English)") },
                    placeholder = { Text("e.g. Aaramdayok / Dhuniya") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // IPA Phonetic & Syllables
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = phonetic,
                        onValueChange = { phonetic = it },
                        label = { Text("IPA Phonetic") },
                        placeholder = { Text("e.g. /ˈkʌmftəbl/") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = syllables,
                        onValueChange = { syllables = it },
                        label = { Text("Syllables") },
                        placeholder = { Text("e.g. com · fort · a · ble") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Part of Speech & Category
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = partOfSpeech,
                        onValueChange = { partOfSpeech = it },
                        label = { Text("Part of Speech") },
                        placeholder = { Text("noun, verb, adj") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("Category") },
                        placeholder = { Text("Academic, General") },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Example Sentence
                OutlinedTextField(
                    value = exampleSentence,
                    onValueChange = { exampleSentence = it },
                    label = { Text("Example Sentence") },
                    placeholder = { Text("e.g. We use this word in daily conversations.") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Speech Coaching Tip
                OutlinedTextField(
                    value = coachingTip,
                    onValueChange = { coachingTip = it },
                    label = { Text("Speech Coach Pronunciation Tip") },
                    placeholder = { Text("e.g. Stress the first syllable clearly.") },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (word.isNotBlank() && assameseMeaning.isNotBlank()) {
                        onCreate(
                            word,
                            assameseMeaning,
                            assameseTransliteration,
                            phonetic,
                            syllables,
                            partOfSpeech,
                            category,
                            coachingTip,
                            exampleSentence,
                            meaning
                        )
                    }
                },
                enabled = word.isNotBlank() && assameseMeaning.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
            ) {
                Text("Save to Dictionary", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

// -------------------------------------------------------------
// IMPORT ENGLISH TO ASSAMESE WORDS EXCEL / CSV DIALOG
// -------------------------------------------------------------
@Composable
fun ImportAssameseWordsExcelDialog(
    onDismiss: () -> Unit,
    onImportWords: (List<com.example.util.ParsedVocabRow>) -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) } // 0 = File Upload (.xlsx / .csv), 1 = Paste Spreadsheet Rows
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var rawInputText by remember { mutableStateOf("") }
    var parsedWords by remember { mutableStateOf<List<com.example.util.ParsedVocabRow>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isParsing by remember { mutableStateOf(false) }

    // Launcher for Android Document Picker (.xlsx and .csv files)
    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                isParsing = true
                errorMessage = null
                // Query display name
                var name: String? = null
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1 && cursor.moveToFirst()) {
                        name = cursor.getString(nameIdx)
                    }
                }
                val fileName = name ?: uri.lastPathSegment ?: "spreadsheet.xlsx"
                selectedFileName = fileName

                val rows = com.example.util.ExcelWordSheetParser.parseFromUri(context, uri, fileName)
                if (rows.isEmpty()) {
                    errorMessage = "No valid English words found in '$fileName'. Please check column headers."
                    parsedWords = emptyList()
                } else {
                    parsedWords = rows
                    errorMessage = null
                }
            } catch (e: Exception) {
                errorMessage = "Failed to parse file: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isParsing = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("import_assamese_excel_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldSuccess),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📊", fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "Import Excel Sheet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "English to Assamese Vocabulary",
                            fontSize = 11.sp,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Tab Selection: Upload File vs Paste Rows
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    contentColor = EmeraldSuccess,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("📁 File (.xlsx / .csv)", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("📋 Paste Rows", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                if (selectedTab == 0) {
                    // Option 1: File Picker Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                documentPickerLauncher.launch(
                                    arrayOf(
                                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                        "application/vnd.ms-excel",
                                        "text/csv",
                                        "text/comma-separated-values",
                                        "text/plain",
                                        "*/*"
                                    )
                                )
                            }
                            .testTag("select_excel_file_picker_btn")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSuccess.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.UploadFile,
                                    contentDescription = null,
                                    tint = EmeraldSuccess,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Text(
                                text = selectedFileName ?: "Tap to select .xlsx or .csv file",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedFileName != null) EmeraldSuccess else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Supports Excel sheets with English Word & Assamese Meaning columns",
                                fontSize = 10.5.sp,
                                color = MutedGray
                            )
                        }
                    }

                    // Quick Sample Template Loader Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilledTonalButton(
                            onClick = {
                                val sampleCsv = com.example.util.ExcelWordSheetParser.getSampleExcelTemplateCsv()
                                parsedWords = com.example.util.ExcelWordSheetParser.parseRawText(sampleCsv)
                                selectedFileName = "Sample_Assamese_Vocab.xlsx"
                                errorMessage = null
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("load_sample_excel_template_btn")
                        ) {
                            Text("✨ Load Sample Excel Words", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Text(
                            text = "Auto-fills IPA & coaching tips",
                            fontSize = 10.sp,
                            color = MutedGray
                        )
                    }
                } else {
                    // Option 2: Paste Rows Text Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Paste spreadsheet rows (CSV or Tab-separated):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MutedGray
                        )
                        OutlinedTextField(
                            value = rawInputText,
                            onValueChange = {
                                rawInputText = it
                                if (it.isNotBlank()) {
                                    val rows = com.example.util.ExcelWordSheetParser.parseRawText(it)
                                    parsedWords = rows
                                    if (rows.isEmpty()) {
                                        errorMessage = "Could not parse rows. Use: Word, Assamese Meaning"
                                    } else {
                                        errorMessage = null
                                    }
                                } else {
                                    parsedWords = emptyList()
                                }
                            },
                            placeholder = {
                                Text(
                                    "Word, অসমীয়া অৰ্থ, Transliteration, IPA\nConfidence, আত্মবিশ্বাস, Aatmabixwaas, /ˈkɒnfɪdəns/\nOpportunity, সুযোগ, Xujog, /ˌɒpəˈtjuːnɪti/",
                                    fontSize = 11.sp
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            minLines = 4,
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("paste_excel_rows_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    rawInputText = com.example.util.ExcelWordSheetParser.getSampleExcelTemplateCsv()
                                    parsedWords = com.example.util.ExcelWordSheetParser.parseRawText(rawInputText)
                                    errorMessage = null
                                }
                            ) {
                                Text("Paste Sample Template", fontSize = 11.sp, color = RoyalBlue)
                            }

                            if (rawInputText.isNotBlank()) {
                                TextButton(onClick = { rawInputText = ""; parsedWords = emptyList() }) {
                                    Text("Clear", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }

                // Error Message if any
                errorMessage?.let { err ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Parsed Preview List
                if (parsedWords.isNotEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Preview Words (${parsedWords.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = EmeraldSuccess
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Ready to Import",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 180.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(parsedWords) { row ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surface)
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = row.englishWord,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = RoyalBlue
                                                )
                                                if (row.ipa.isNotBlank()) {
                                                    Text(
                                                        text = row.ipa,
                                                        fontSize = 10.sp,
                                                        color = PurpleAccent
                                                    )
                                                }
                                            }
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = row.assameseMeaning,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = AmberGold
                                                )
                                                if (row.assameseTransliteration.isNotBlank()) {
                                                    Text(
                                                        text = "(${row.assameseTransliteration})",
                                                        fontSize = 10.sp,
                                                        color = MutedGray
                                                    )
                                                }
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = RoyalBlue.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = row.category,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = RoyalBlue,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (parsedWords.isNotEmpty()) {
                        onImportWords(parsedWords)
                    }
                },
                enabled = parsedWords.isNotEmpty() && !isParsing,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("confirm_import_excel_words_btn")
            ) {
                Text(
                    text = if (parsedWords.isEmpty()) "Import 0 Words" else "Import ${parsedWords.size} Words",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
