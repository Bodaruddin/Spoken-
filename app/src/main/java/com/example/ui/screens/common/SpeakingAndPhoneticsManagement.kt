package com.example.ui.screens.common

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DialogueTurn
import com.example.data.model.PronunciationDrill
import com.example.data.model.Role
import com.example.data.model.RoleplayScenario
import com.example.data.model.SpeakingPrompt
import com.example.service.PhoneticsAiEngine
import com.example.ui.MainViewModel
import com.example.ui.theme.*

enum class SpeakingManageTab(val title: String, val emoji: String) {
    SPEAKING_PROMPTS("AI Speaking", "🎙️"),
    PHONETICS_DRILLS("Phonetics & Drills", "🎯"),
    ROLEPLAY_SCENARIOS("Roleplays", "🎭")
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

    val prompts by viewModel.speakingPrompts.collectAsState()
    val drills by viewModel.pronunciationDrills.collectAsState()
    val roleplays by viewModel.roleplays.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(listOf(RoyalBlue, PurpleAccent))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎙️", fontSize = 22.sp)
                        }
                        Column {
                            Text(
                                "AI Speaking & Phonetics Hub",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (userRole == Role.ADMIN) "Global Curriculum & Speech Engine Admin"
                                else "Teacher Speech Course Management",
                                fontSize = 11.sp,
                                color = MutedGray
                            )
                        }
                    }

                    // Contextual New Button
                    Button(
                        onClick = {
                            when (currentTab) {
                                SpeakingManageTab.SPEAKING_PROMPTS -> showCreatePromptDialog = true
                                SpeakingManageTab.PHONETICS_DRILLS -> showCreateDrillDialog = true
                                SpeakingManageTab.ROLEPLAY_SCENARIOS -> showCreateRoleplayDialog = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("create_item_button")
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            when (currentTab) {
                                SpeakingManageTab.SPEAKING_PROMPTS -> "New Prompt"
                                SpeakingManageTab.PHONETICS_DRILLS -> "New Drill"
                                SpeakingManageTab.ROLEPLAY_SCENARIOS -> "New Roleplay"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Segmented Tab Selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    SpeakingManageTab.values().forEach { tab ->
                        val isSelected = currentTab == tab
                        val count = when (tab) {
                            SpeakingManageTab.SPEAKING_PROMPTS -> prompts.size
                            SpeakingManageTab.PHONETICS_DRILLS -> drills.size
                            SpeakingManageTab.ROLEPLAY_SCENARIOS -> roleplays.size
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) RoyalBlue else Color.Transparent,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currentTab = tab }
                                .testTag("manage_tab_${tab.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(tab.emoji, fontSize = 13.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        "$count",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
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
            SpeakingManageTab.ROLEPLAY_SCENARIOS -> {
                RoleplayManagementList(
                    scenarios = roleplays,
                    onDelete = { roleplayToDelete = it }
                )
            }
        }
    }

    // -------------------------------------------------------------
    // DIALOGS
    // -------------------------------------------------------------

    // Create Speaking Prompt Dialog
    if (showCreatePromptDialog) {
        CreateSpeakingPromptDialog(
            onDismiss = { showCreatePromptDialog = false },
            onCreate = { title, cat, q, tip, kw, sec ->
                viewModel.addSpeakingPrompt(title, cat, q, tip, kw, sec)
                showCreatePromptDialog = false
                toastMessage = "New Speaking Prompt '$title' published!"
            }
        )
    }

    // Edit Speaking Prompt Dialog
    editingPrompt?.let { prompt ->
        EditSpeakingPromptDialog(
            prompt = prompt,
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

                    Text(prompt.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
    onDismiss: () -> Unit,
    onCreate: (title: String, category: String, question: String, tip: String, keywords: List<String>, minSeconds: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Daily Life") }
    var question by remember { mutableStateOf("") }
    var tip by remember { mutableStateOf("") }
    var keywordsInput by remember { mutableStateOf("") }
    var minSeconds by remember { mutableIntStateOf(20) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create AI Speaking Prompt", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Prompt Title *") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (e.g. Daily Life, Business, Travel)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Speaking Question / Prompt *") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = keywordsInput,
                    onValueChange = { keywordsInput = it },
                    label = { Text("Recommended Keywords (comma separated)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tip,
                    onValueChange = { tip = it },
                    label = { Text("Coaching Tip / Connectors") },
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && question.isNotBlank()) {
                        val kwList = keywordsInput.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        onCreate(title, category, question, tip, kwList, minSeconds)
                    }
                },
                enabled = title.isNotBlank() && question.isNotBlank()
            ) {
                Text("Publish")
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
    onDismiss: () -> Unit,
    onSave: (SpeakingPrompt) -> Unit
) {
    var title by remember { mutableStateOf(prompt.title) }
    var category by remember { mutableStateOf(prompt.category) }
    var question by remember { mutableStateOf(prompt.question) }
    var tip by remember { mutableStateOf(prompt.followUpTip) }
    var keywordsInput by remember { mutableStateOf(prompt.recommendedKeywords.joinToString(", ")) }
    var minSeconds by remember { mutableIntStateOf(prompt.minSpeakingSeconds) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Speaking Prompt", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Prompt Title") },
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
                    label = { Text("Speaking Question") },
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
                        minSpeakingSeconds = minSeconds
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
