package com.example.ui.screens.teacher

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
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.TeacherSpeakingSection
import com.example.ui.TeacherTab
import com.example.ui.screens.common.SpeakingAndPhoneticsManagement
import com.example.ui.screens.common.SpeakingManageTab
import com.example.ui.theme.*

@Composable
fun TeacherMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTeacherTab.collectAsState()

    when (currentTab) {
        TeacherTab.DASHBOARD -> TeacherDashboard(viewModel, modifier)
        TeacherTab.STUDENTS -> TeacherStudentsManagement(viewModel, modifier)
        TeacherTab.GAMES -> TeacherGameCreator(viewModel, modifier)
        TeacherTab.SPEAKING_REVIEW -> TeacherSpeakingReview(viewModel, modifier)
        TeacherTab.ASSIGNMENTS -> TeacherAssignmentsList(viewModel, modifier)
    }
}

// -------------------------------------------------------------
// TEACHER DASHBOARD
// -------------------------------------------------------------
@Composable
fun TeacherDashboard(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val currentUser by viewModel.currentUser.collectAsState()
    val submissions by viewModel.submissions.collectAsState()
    val students by viewModel.adminStudents.collectAsState()
    val wordMatches by viewModel.wordMatchChallenges.collectAsState()
    val sentences by viewModel.sentenceBuilderChallenges.collectAsState()
    val totalGames = wordMatches.size + sentences.size + 15

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("teacher_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Teacher Banner Card
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF8B5CF6), RoyalBlue))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(currentUser.avatarEmoji.ifBlank { "👩‍🏫" }, fontSize = 32.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentUser.name.ifBlank { "Faculty Instructor" },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        val departmentOrBatch = currentUser.batch.ifBlank { "Batch Alpha & Beta" }
                        Text(
                            text = "Faculty Instructor • $departmentOrBatch",
                            fontSize = 12.sp,
                            color = MutedGray
                        )
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text("● Faculty Active Today", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TeacherMetricCard("Enrolled Students", "${students.size}", "👥", RoyalBlue, Modifier.weight(1f))
                TeacherMetricCard("Pending Voice", "${submissions.count { it.status == "PENDING" }}", "🎧", OrangeVibrant, Modifier.weight(1f))
                TeacherMetricCard("Avg Cohort", "84%", "🎙️", EmeraldSuccess, Modifier.weight(1f))
                TeacherMetricCard("Games Active", "$totalGames", "🎮", PurpleAccent, Modifier.weight(1f))
            }
        }

        // Quick Action Cards
        item {
            Text("Faculty Quick Operations", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TeacherQuickActionCard(
                    title = "Manage Students",
                    subtitle = "Roster & CEFR Levels",
                    icon = "👥",
                    color = RoyalBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTeacherTab(TeacherTab.STUDENTS) }
                )
                TeacherQuickActionCard(
                    title = "Add Game Challenge",
                    subtitle = "Word match, sentence, etc.",
                    icon = "🎮",
                    color = PurpleAccent,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTeacherTab(TeacherTab.GAMES) }
                )
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TeacherQuickActionCard(
                    title = "AI Speaking",
                    subtitle = "Speech Prompts & Tasks",
                    icon = "🎙️",
                    color = EmeraldSuccess,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectTeacherSpeakingSection(TeacherSpeakingSection.MANAGE_SPEAKING)
                        viewModel.selectTeacherTab(TeacherTab.SPEAKING_REVIEW)
                    }
                )
                TeacherQuickActionCard(
                    title = "Phonetics & Drills",
                    subtitle = "IPA & Accent Coach",
                    icon = "🎯",
                    color = AmberGold,
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectTeacherSpeakingSection(TeacherSpeakingSection.MANAGE_PHONETICS)
                        viewModel.selectTeacherTab(TeacherTab.SPEAKING_REVIEW)
                    }
                )
            }
        }

        // Action Quick Access for Speaking Review
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectTeacherTab(TeacherTab.SPEAKING_REVIEW) }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Review Spoken Submissions", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = DeepNavy)
                        Text("${submissions.count { it.status == "PENDING" }} voice audio recordings waiting for grading", fontSize = 12.sp, color = MutedGray)
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = RoyalBlue)
                }
            }
        }

        // Weak Areas Alert Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("⚠️ Cohort Attention & Phoneme Alerts", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    WeakAreaRow("Pronunciation: Voiced 'th' sounds (this, weather)", "4 students need drill")
                    WeakAreaRow("Grammar: Irregular past tense agreement", "3 students need review")
                    WeakAreaRow("Fluency: Response hesitation in impromptu talks", "Avg pause: 3.2s")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TEACHER STUDENTS MANAGEMENT (COMPREHENSIVE)
// -------------------------------------------------------------
@Composable
fun TeacherStudentsManagement(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val students by viewModel.adminStudents.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedBatchFilter by remember { mutableStateOf("All") }
    var selectedStudentForDetail by remember { mutableStateOf<StudentManagementInfo?>(null) }
    var actionToast by remember { mutableStateOf<String?>(null) }

    val filteredStudents = remember(students, searchQuery, selectedBatchFilter) {
        students.filter { student ->
            val matchesQuery = student.name.contains(searchQuery, ignoreCase = true) ||
                    student.email.contains(searchQuery, ignoreCase = true) ||
                    student.batch.contains(searchQuery, ignoreCase = true)
            val matchesBatch = when (selectedBatchFilter) {
                "All" -> true
                "Batch Alpha" -> student.batch.contains("Alpha")
                "Batch Beta" -> student.batch.contains("Beta")
                "Needs Help (<80%)" -> student.speakingScore < 80
                "Top Fluency (≥85%)" -> student.speakingScore >= 85
                else -> true
            }
            matchesQuery && matchesBatch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("teacher_students_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Assigned Students Cohort", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Manage student levels, view speaking diagnostics, and assign target drills", fontSize = 12.sp, color = MutedGray)

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, email, or batch...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = RoyalBlue) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )

                // Filter Pills
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    val filters = listOf("All", "Batch Alpha", "Batch Beta", "Needs Help (<80%)", "Top Fluency (≥85%)")
                    items(filters) { f ->
                        val isSel = selectedBatchFilter == f
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedBatchFilter = f },
                            label = { Text(f, fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        actionToast?.let { msg ->
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(msg, color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }

        if (filteredStudents.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No students match the criteria", fontSize = 14.sp, color = MutedGray)
                }
            }
        }

        items(filteredStudents) { student ->
            TeacherStudentCard(
                student = student,
                onManageClick = { selectedStudentForDetail = student },
                onAssignDrill = {
                    actionToast = "Assigned 15-minute voice challenge to ${student.name}!"
                    viewModel.speakAudio("Target pronunciation drill assigned to ${student.name}")
                }
            )
        }
    }

    // Comprehensive Student Detail & Management Dialog
    selectedStudentForDetail?.let { student ->
        StudentDetailDialog(
            student = student,
            onDismiss = { selectedStudentForDetail = null },
            onSave = { updated ->
                viewModel.updateStudent(updated)
                selectedStudentForDetail = null
                actionToast = "Updated profile & level for ${updated.name}!"
            }
        )
    }
}

@Composable
fun TeacherStudentCard(
    student: StudentManagementInfo,
    onManageClick: () -> Unit,
    onAssignDrill: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(student.avatarEmoji, fontSize = 32.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(student.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RoyalBlue
                        ) {
                            Text(student.level.cefr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text("${student.email} • ${student.batch}", fontSize = 11.sp, color = MutedGray)
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (student.isActive) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                ) {
                    Text(
                        if (student.isActive) "Active" else "Paused",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (student.isActive) EmeraldSuccess else CoralAccent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Speaking Proficiency Progress
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Speaking Score", fontSize = 12.sp, color = MutedGray)
                Text("${student.speakingScore}%", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (student.speakingScore >= 80) EmeraldSuccess else OrangeVibrant)
            }
            LinearProgressIndicator(
                progress = { student.speakingScore / 100f },
                color = if (student.speakingScore >= 80) EmeraldSuccess else OrangeVibrant,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )

            // Diagnostic Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEFF6FF), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pronunciation", fontSize = 9.sp, color = MutedGray)
                        Text("${student.pronunciationScore}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEFF6FF), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Fluency", fontSize = 9.sp, color = MutedGray)
                        Text("${student.fluencyScore}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    }
                }
                Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFEFF6FF), modifier = Modifier.weight(1f)) {
                    Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Streak", fontSize = 9.sp, color = MutedGray)
                        Text("🔥 ${student.streakDays}d", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                    }
                }
            }

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onAssignDrill,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Assign Drill", fontSize = 11.sp)
                }
                Button(
                    onClick = onManageClick,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Diagnostics & Edit", fontSize = 11.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TEACHER GAME & CHALLENGE CREATOR
// -------------------------------------------------------------
enum class GameCategoryFilter(val title: String, val emoji: String) {
    WORD_MATCH("Word Match", "🧩"),
    MISSING_LETTERS("Missing Letters", "🔤"),
    SENTENCE_BUILDER("Sentence Builder", "🏗️"),
    LISTEN_CHOOSE("Listen & Choose", "🎧"),
    PRONUNCIATION_BATTLE("Pronunciation Battle", "⚔️"),
    FAST_VOCABULARY("Fast Vocabulary", "⏱️"),
    GRAMMAR_CHALLENGE("Grammar Challenge", "⚡")
}

// Active editing state for game challenges
data class ActiveEditingChallenge(
    val category: GameCategoryFilter,
    val id: String,
    val field1: String,
    val field2: String,
    val field3: String = "",
    val field4: String = "",
    val field5: String = "",
    val onSave: (f1: String, f2: String, f3: String, f4: String, f5: String) -> Unit
)

@Composable
fun TeacherGameCreator(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    var selectedCategory by remember { mutableStateOf(GameCategoryFilter.WORD_MATCH) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var activeEditingChallenge by remember { mutableStateOf<ActiveEditingChallenge?>(null) }
    var statusMsg by remember { mutableStateOf<String?>(null) }
    val currentUser by viewModel.currentUser.collectAsState()

    val wordMatches by viewModel.wordMatchChallenges.collectAsState()
    val missingLetters by viewModel.missingLettersChallenges.collectAsState()
    val sentenceBuilders by viewModel.sentenceBuilderChallenges.collectAsState()
    val listenChooses by viewModel.listenChooseChallenges.collectAsState()
    val pronunciationBattles by viewModel.pronunciationBattleChallenges.collectAsState()
    val fastVocabs by viewModel.fastVocabularyChallenges.collectAsState()
    val grammarChallenges by viewModel.grammarChallenges.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("teacher_game_creator_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(RoyalBlue, PurpleAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎮", fontSize = 20.sp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Game & Challenge Creator",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                "Publish & edit interactive learning challenges for students",
                                fontSize = 11.sp,
                                color = MutedGray,
                                maxLines = 1
                            )
                        }
                    }

                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("New", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }

        // Horizontal Category Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(GameCategoryFilter.values()) { cat ->
                    val isSel = selectedCategory == cat
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surface,
                        border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.clickable { selectedCategory = cat }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(cat.emoji, fontSize = 16.sp)
                            Text(
                                cat.title,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Prominent "Add New Question" banner card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                border = BorderStroke(1.5.dp, Color(0xFFBFDBFE)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCreateDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(RoyalBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "+ Add New ${selectedCategory.title} Question",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = DeepNavy,
                                maxLines = 1
                            )
                            Text(
                                "Create a question or drill to publish directly to students",
                                fontSize = 11.sp,
                                color = MutedGray,
                                maxLines = 1
                            )
                        }
                    }
                    Button(
                        onClick = { showCreateDialog = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text("Add New", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }

        statusMsg?.let { msg ->
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(msg, color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }

        // Items based on selected category with Edit & Delete support
        when (selectedCategory) {
            GameCategoryFilter.WORD_MATCH -> {
                item { Text("Existing Word Match Pairs (${wordMatches.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                items(wordMatches) { item ->
                    GameItemCard(
                        title = item.word,
                        subtitle = item.definition,
                        tag = item.category,
                        createdBy = item.createdBy,
                        onPlayAudio = { viewModel.speakAudio(item.word) },
                        onEdit = {
                            activeEditingChallenge = ActiveEditingChallenge(
                                category = GameCategoryFilter.WORD_MATCH,
                                id = item.id,
                                field1 = item.word,
                                field2 = item.definition,
                                field3 = item.category,
                                onSave = { f1, f2, f3, _, _ ->
                                    viewModel.updateWordMatchChallenge(item.copy(word = f1, definition = f2, category = f3))
                                    statusMsg = "Updated '${f1}' challenge successfully!"
                                }
                            )
                        },
                        onDelete = { viewModel.deleteWordMatchChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.MISSING_LETTERS -> {
                item { Text("Existing Missing Letters Challenges (${missingLetters.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                items(missingLetters) { item ->
                    GameItemCard(
                        title = item.fullWord,
                        subtitle = "Clue: ${item.hint}",
                        tag = "Masked indices: ${item.missingIndices.joinToString(", ")}",
                        createdBy = item.createdBy,
                        onPlayAudio = { viewModel.speakAudio(item.fullWord) },
                        onEdit = {
                            activeEditingChallenge = ActiveEditingChallenge(
                                category = GameCategoryFilter.MISSING_LETTERS,
                                id = item.id,
                                field1 = item.fullWord,
                                field2 = item.hint,
                                onSave = { f1, f2, _, _, _ ->
                                    viewModel.updateMissingLettersChallenge(item.copy(fullWord = f1.uppercase(), hint = f2))
                                    statusMsg = "Updated '${f1.uppercase()}' missing letters challenge!"
                                }
                            )
                        },
                        onDelete = { viewModel.deleteMissingLettersChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.SENTENCE_BUILDER -> {
                item { Text("Existing Sentence Builder Challenges (${sentenceBuilders.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                items(sentenceBuilders) { item ->
                    GameItemCard(
                        title = item.targetSentence,
                        subtitle = "Hint: ${item.hint}" + if (item.assameseTranslation.isNotBlank()) "\n🇮🇳 অসমীয়া: ${item.assameseTranslation}" else "",
                        tag = "Sentence Syntax",
                        createdBy = item.createdBy,
                        onPlayAudio = { viewModel.speakAudio(item.targetSentence) },
                        onEdit = {
                            activeEditingChallenge = ActiveEditingChallenge(
                                category = GameCategoryFilter.SENTENCE_BUILDER,
                                id = item.id,
                                field1 = item.targetSentence,
                                field2 = item.hint,
                                field3 = item.assameseTranslation,
                                onSave = { f1, f2, f3, _, _ ->
                                    viewModel.updateSentenceBuilderChallenge(item.copy(targetSentence = f1, hint = f2, assameseTranslation = f3))
                                    statusMsg = "Updated sentence builder challenge!"
                                }
                            )
                        },
                        onDelete = { viewModel.deleteSentenceBuilderChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.LISTEN_CHOOSE -> {
                item { Text("Existing Listen & Choose Drills (${listenChooses.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                items(listenChooses) { item ->
                    GameItemCard(
                        title = item.question,
                        subtitle = "Audio Speech: \"${item.audioScript}\"",
                        tag = "Answer: ${item.options.getOrNull(item.correctIndex) ?: ""}",
                        createdBy = item.createdBy,
                        onPlayAudio = { viewModel.speakAudio(item.audioScript) },
                        onEdit = {
                            activeEditingChallenge = ActiveEditingChallenge(
                                category = GameCategoryFilter.LISTEN_CHOOSE,
                                id = item.id,
                                field1 = item.audioScript,
                                field2 = item.question,
                                field3 = item.options.getOrNull(0) ?: "",
                                field4 = item.options.getOrNull(1) ?: "",
                                field5 = item.options.getOrNull(2) ?: "",
                                onSave = { f1, f2, f3, f4, f5 ->
                                    val opts = listOf(f3.ifBlank { "Option 1" }, f4.ifBlank { "Option 2" }, f5.ifBlank { "Option 3" })
                                    viewModel.updateListenChooseChallenge(item.copy(audioScript = f1, question = f2, options = opts))
                                    statusMsg = "Updated Listen & Choose challenge!"
                                }
                            )
                        },
                        onDelete = { viewModel.deleteListenChooseChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.PRONUNCIATION_BATTLE -> {
                item { Text("Existing Pronunciation Battles (${pronunciationBattles.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                items(pronunciationBattles) { item ->
                    GameItemCard(
                        title = item.challengeSentence,
                        subtitle = "Phoneme Focus: ${item.targetPhonemeFocus}",
                        tag = "Passing: ${item.passingScore}% (${item.difficulty})",
                        createdBy = item.createdBy,
                        onPlayAudio = { viewModel.speakAudio(item.challengeSentence) },
                        onEdit = {
                            activeEditingChallenge = ActiveEditingChallenge(
                                category = GameCategoryFilter.PRONUNCIATION_BATTLE,
                                id = item.id,
                                field1 = item.challengeSentence,
                                field2 = item.targetPhonemeFocus,
                                field3 = item.difficulty,
                                onSave = { f1, f2, f3, _, _ ->
                                    viewModel.updatePronunciationBattleChallenge(item.copy(challengeSentence = f1, targetPhonemeFocus = f2, difficulty = f3))
                                    statusMsg = "Updated pronunciation battle challenge!"
                                }
                            )
                        },
                        onDelete = { viewModel.deletePronunciationBattleChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.FAST_VOCABULARY -> {
                item { Text("Existing Fast Vocabulary Blitzes (${fastVocabs.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                items(fastVocabs) { item ->
                    GameItemCard(
                        title = item.word,
                        subtitle = item.prompt,
                        tag = "Answer: ${item.options.getOrNull(item.correctIndex) ?: ""}",
                        createdBy = item.createdBy,
                        onPlayAudio = { viewModel.speakAudio(item.word) },
                        onEdit = {
                            activeEditingChallenge = ActiveEditingChallenge(
                                category = GameCategoryFilter.FAST_VOCABULARY,
                                id = item.id,
                                field1 = item.word,
                                field2 = item.prompt,
                                field3 = item.options.getOrNull(0) ?: "",
                                field4 = item.options.getOrNull(1) ?: "",
                                field5 = item.options.getOrNull(2) ?: "",
                                onSave = { f1, f2, f3, f4, f5 ->
                                    val opts = listOf(f3, f4, f5)
                                    viewModel.updateFastVocabularyChallenge(item.copy(word = f1, prompt = f2, options = opts))
                                    statusMsg = "Updated fast vocabulary item '${f1}'!"
                                }
                            )
                        },
                        onDelete = { viewModel.deleteFastVocabularyChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.GRAMMAR_CHALLENGE -> {
                item { Text("Existing Grammar Challenges (${grammarChallenges.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                items(grammarChallenges) { item ->
                    GameItemCard(
                        title = item.question,
                        subtitle = "Rule: ${item.explanation}",
                        tag = item.category,
                        createdBy = item.createdBy,
                        onPlayAudio = { viewModel.speakAudio(item.question) },
                        onEdit = {
                            activeEditingChallenge = ActiveEditingChallenge(
                                category = GameCategoryFilter.GRAMMAR_CHALLENGE,
                                id = item.id,
                                field1 = item.question,
                                field2 = item.options.getOrNull(0) ?: "",
                                field3 = item.options.getOrNull(1) ?: "",
                                field4 = item.options.getOrNull(2) ?: "",
                                field5 = item.explanation,
                                onSave = { f1, f2, f3, f4, f5 ->
                                    val opts = listOf(f2, f3, f4)
                                    viewModel.updateGrammarChallenge(item.copy(question = f1, options = opts, explanation = f5))
                                    statusMsg = "Updated grammar challenge question!"
                                }
                            )
                        },
                        onDelete = { viewModel.deleteGrammarChallenge(item.id) }
                    )
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateGameChallengeDialog(
            category = selectedCategory,
            authorName = currentUser.name.ifBlank { "Faculty Instructor" },
            onDismiss = { showCreateDialog = false },
            onCreated = {
                statusMsg = "Successfully published new challenge for ${selectedCategory.title}!"
                showCreateDialog = false
            },
            viewModel = viewModel
        )
    }

    activeEditingChallenge?.let { editing ->
        EditGameChallengeDialog(
            editing = editing,
            onDismiss = { activeEditingChallenge = null },
            onSaved = { activeEditingChallenge = null }
        )
    }
}

@Composable
fun GameItemCard(
    title: String,
    subtitle: String,
    tag: String,
    createdBy: String,
    onPlayAudio: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEFF6FF)
                ) {
                    Text(tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalBlue, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onPlayAudio, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Rounded.VolumeUp, contentDescription = "Play Audio", tint = RoyalBlue, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit Question", tint = RoyalBlue, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = CoralAccent, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 12.sp, color = MutedGray)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Published by: $createdBy", fontSize = 10.sp, color = RoyalBlue)
                
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onEdit,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Edit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Universal Edit Challenge Dialog
@Composable
fun EditGameChallengeDialog(
    editing: ActiveEditingChallenge,
    onDismiss: () -> Unit,
    onSaved: () -> Unit
) {
    var f1 by remember { mutableStateOf(editing.field1) }
    var f2 by remember { mutableStateOf(editing.field2) }
    var f3 by remember { mutableStateOf(editing.field3) }
    var f4 by remember { mutableStateOf(editing.field4) }
    var f5 by remember { mutableStateOf(editing.field5) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(editing.category.emoji, fontSize = 24.sp)
                Text("Edit ${editing.category.title} Question", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (editing.category) {
                    GameCategoryFilter.WORD_MATCH -> {
                        OutlinedTextField(value = f1, onValueChange = { f1 = it }, label = { Text("Word") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f2, onValueChange = { f2 = it }, label = { Text("Definition") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f3, onValueChange = { f3 = it }, label = { Text("Category") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.MISSING_LETTERS -> {
                        OutlinedTextField(value = f1, onValueChange = { f1 = it }, label = { Text("Full Word") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f2, onValueChange = { f2 = it }, label = { Text("Clue / Hint") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.SENTENCE_BUILDER -> {
                        OutlinedTextField(value = f1, onValueChange = { f1 = it }, label = { Text("Target Sentence") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f2, onValueChange = { f2 = it }, label = { Text("Grammar Hint") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f3, onValueChange = { f3 = it }, label = { Text("Assamese Translation (অসমীয়া অনুবাদ - optional)") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.LISTEN_CHOOSE -> {
                        OutlinedTextField(value = f1, onValueChange = { f1 = it }, label = { Text("Audio Script Spoken (TTS)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f2, onValueChange = { f2 = it }, label = { Text("Question Prompt") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f3, onValueChange = { f3 = it }, label = { Text("Correct Option") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f4, onValueChange = { f4 = it }, label = { Text("Distractor Option 1") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f5, onValueChange = { f5 = it }, label = { Text("Distractor Option 2") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.PRONUNCIATION_BATTLE -> {
                        OutlinedTextField(value = f1, onValueChange = { f1 = it }, label = { Text("Challenge Sentence") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f2, onValueChange = { f2 = it }, label = { Text("Phoneme Focus") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f3, onValueChange = { f3 = it }, label = { Text("Difficulty (Easy, Medium, Hard)") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.FAST_VOCABULARY -> {
                        OutlinedTextField(value = f1, onValueChange = { f1 = it }, label = { Text("Target Word") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f2, onValueChange = { f2 = it }, label = { Text("Prompt / Meaning") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f3, onValueChange = { f3 = it }, label = { Text("Correct Option") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f4, onValueChange = { f4 = it }, label = { Text("Distractor Option 1") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f5, onValueChange = { f5 = it }, label = { Text("Distractor Option 2") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.GRAMMAR_CHALLENGE -> {
                        OutlinedTextField(value = f1, onValueChange = { f1 = it }, label = { Text("Question Sentence with blank ___") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f2, onValueChange = { f2 = it }, label = { Text("Correct Option") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f3, onValueChange = { f3 = it }, label = { Text("Distractor Option 1") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f4, onValueChange = { f4 = it }, label = { Text("Distractor Option 2") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f5, onValueChange = { f5 = it }, label = { Text("Grammar Rule Explanation") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                editing.onSave(f1, f2, f3, f4, f5)
                onSaved()
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


// -------------------------------------------------------------
// TEACHER SPEAKING REVIEW
// -------------------------------------------------------------
@Composable
fun TeacherSpeakingReview(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val currentSection by viewModel.teacherSpeakingSection.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Section Selector for Teacher
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeacherSpeakingSection.values().forEach { sec ->
                    val isSel = currentSection == sec
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.selectTeacherSpeakingSection(sec) }
                            .testTag("teacher_section_${sec.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sec.emoji, fontSize = 13.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                sec.title,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        when (currentSection) {
            TeacherSpeakingSection.SUBMISSIONS -> {
                TeacherSubmissionsGradingView(viewModel = viewModel, modifier = Modifier.weight(1f))
            }
            TeacherSpeakingSection.MANAGE_SPEAKING -> {
                SpeakingAndPhoneticsManagement(
                    viewModel = viewModel,
                    userRole = Role.TEACHER,
                    modifier = Modifier.weight(1f),
                    initialTab = SpeakingManageTab.SPEAKING_PROMPTS
                )
            }
            TeacherSpeakingSection.MANAGE_PHONETICS -> {
                SpeakingAndPhoneticsManagement(
                    viewModel = viewModel,
                    userRole = Role.TEACHER,
                    modifier = Modifier.weight(1f),
                    initialTab = SpeakingManageTab.PHONETICS_DRILLS
                )
            }
        }
    }
}

@Composable
fun TeacherSubmissionsGradingView(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val submissions by viewModel.submissions.collectAsState()
    var selectedSubmission by remember { mutableStateOf<StudentSubmission?>(submissions.firstOrNull()) }
    var fluencyScore by remember { mutableFloatStateOf(85f) }
    var grammarScore by remember { mutableFloatStateOf(88f) }
    var pronunciationScore by remember { mutableFloatStateOf(82f) }
    var teacherFeedbackInput by remember { mutableStateOf("Excellent clarity and pace. Keep practicing the sentence rhythm!") }
    var submitToast by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("teacher_speaking_review_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Student Voice Recordings to Grade", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }

        submitToast?.let { msg ->
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(msg, color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }

        // Submission selector list
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(submissions) { sub ->
                    val isSel = selectedSubmission?.id == sub.id
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surface,
                        border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.clickable { selectedSubmission = sub }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(sub.studentAvatar, fontSize = 16.sp)
                            Text(
                                sub.studentName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            if (sub.status == "PENDING") {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(OrangeVibrant))
                            }
                        }
                    }
                }
            }
        }

        selectedSubmission?.let { sub ->
            item {
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
                            Column {
                                Text(sub.studentName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Recorded: ${sub.submittedDate} • ${sub.audioDurationSeconds}s audio", fontSize = 11.sp, color = MutedGray)
                            }
                            Button(
                                onClick = { viewModel.speakAudio(sub.studentTranscript) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Play Recording")
                            }
                        }

                        // Speech Transcript
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Student Audio Transcript:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                Text("\"${sub.studentTranscript}\"", fontSize = 13.sp)
                            }
                        }

                        // Grading Sliders
                        Text("Teacher Evaluation Criteria:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        GradingSlider("Fluency & Conversational Flow", fluencyScore) { fluencyScore = it }
                        GradingSlider("Pronunciation Accuracy & Accent", pronunciationScore) { pronunciationScore = it }
                        GradingSlider("Grammar & Lexical Resource", grammarScore) { grammarScore = it }

                        // Quick Feedback Chips
                        Text("Suggested Feedback Templates:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MutedGray)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Terrific rhythm!", "Work on voiced TH", "Natural intonation").forEach { chip ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEFF6FF),
                                    modifier = Modifier.clickable { teacherFeedbackInput = "$teacherFeedbackInput $chip" }
                                ) {
                                    Text(chip, fontSize = 10.sp, color = RoyalBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                }
                            }
                        }

                        // Feedback Input
                        OutlinedTextField(
                            value = teacherFeedbackInput,
                            onValueChange = { teacherFeedbackInput = it },
                            label = { Text("Personalized Improvement Feedback") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        val overall = ((fluencyScore + pronunciationScore + grammarScore) / 3).toInt()

                        Button(
                            onClick = {
                                viewModel.repository.gradeSubmission(sub.id, overall, teacherFeedbackInput)
                                submitToast = "Grade of $overall% and feedback sent to ${sub.studentName}!"
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            Text("Publish Grade ($overall%) & Send to Student", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TEACHER ASSIGNMENTS & LIVE CLASSES
// -------------------------------------------------------------
@Composable
fun TeacherAssignmentsList(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val assignments by viewModel.assignments.collectAsState()
    val liveClasses by viewModel.liveClasses.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var toastMsg by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Course Speaking Assignments", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Button(
                    onClick = { showCreateDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("New")
                }
            }
        }

        toastMsg?.let { msg ->
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(msg, color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }

        items(assignments) { asg ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(asg.topicType, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                        Text("Due: ${asg.dueDate}", fontSize = 11.sp, color = OrangeVibrant, fontWeight = FontWeight.SemiBold)
                    }
                    Text(asg.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(asg.instructions, fontSize = 12.sp, color = MutedGray)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Submissions: ${asg.submissionsCount} / ${asg.totalAssigned}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        LinearProgressIndicator(
                            progress = { asg.submissionsCount.toFloat() / asg.totalAssigned },
                            modifier = Modifier
                                .width(120.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = RoyalBlue
                        )
                    }
                }
            }
        }

        // Live Virtual Classes Section
        item {
            Spacer(Modifier.height(8.dp))
            Text("Live Classroom Sessions", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }

        items(liveClasses) { cls ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (cls.status == "LIVE") Color(0xFFFEE2E2) else Color(0xFFEFF6FF)
                        ) {
                            Text(
                                cls.status,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (cls.status == "LIVE") CoralAccent else RoyalBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text("${cls.durationMin} mins • ${cls.participantsCount} enrolled", fontSize = 11.sp, color = MutedGray)
                    }

                    Text(cls.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(cls.topicDescription, fontSize = 12.sp, color = MutedGray)
                    Text("Time: ${cls.scheduledTime}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DeepNavy)

                    Button(
                        onClick = {
                            viewModel.speakAudio("Starting live virtual classroom for ${cls.title}")
                            toastMsg = "Live classroom launched for ${cls.title}!"
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Host Session Now 🎥")
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newInstructions by remember { mutableStateOf("") }
        var newDueDate by remember { mutableStateOf("Tomorrow, 6 PM") }
        var newBatch by remember { mutableStateOf("Batch Alpha") }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Speaking Assignment") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Assignment Title") },
                        placeholder = { Text("e.g. 2-Minute Speech on Technology") }
                    )
                    OutlinedTextField(
                        value = newInstructions,
                        onValueChange = { newInstructions = it },
                        label = { Text("Instructions") },
                        placeholder = { Text("Record audio expressing your opinion...") }
                    )
                    OutlinedTextField(
                        value = newDueDate,
                        onValueChange = { newDueDate = it },
                        label = { Text("Due Date") }
                    )
                    OutlinedTextField(
                        value = newBatch,
                        onValueChange = { newBatch = it },
                        label = { Text("Assigned Batch") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newTitle.isNotBlank()) {
                        showCreateDialog = false
                        toastMsg = "New assignment '$newTitle' published for $newBatch!"
                    }
                }) { Text("Publish Assignment") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// -------------------------------------------------------------
// HELPER COMPOSABLES & DIALOGS
// -------------------------------------------------------------
@Composable
fun StudentDetailDialog(
    student: StudentManagementInfo,
    onDismiss: () -> Unit,
    onSave: (StudentManagementInfo) -> Unit
) {
    var name by remember { mutableStateOf(student.name) }
    var email by remember { mutableStateOf(student.email) }
    var batch by remember { mutableStateOf(student.batch) }
    var selectedLevel by remember { mutableStateOf(student.level) }
    var notes by remember { mutableStateOf(student.teacherNotes) }
    var isActive by remember { mutableStateOf(student.isActive) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(student.avatarEmoji, fontSize = 28.sp)
                Column {
                    Text("Student Profile & Management", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("ID: ${student.id} • Joined: ${student.joinedDate}", fontSize = 11.sp, color = MutedGray)
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text("Proficiency Scores Radar", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ScorePill("Speaking", "${student.speakingScore}%", Modifier.weight(1f))
                        ScorePill("Pronunciation", "${student.pronunciationScore}%", Modifier.weight(1f))
                        ScorePill("Fluency", "${student.fluencyScore}%", Modifier.weight(1f))
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ScorePill("Grammar", "${student.grammarScore}%", Modifier.weight(1f))
                        ScorePill("Vocabulary", "${student.vocabularyScore}%", Modifier.weight(1f))
                        ScorePill("Listening", "${student.listeningScore}%", Modifier.weight(1f))
                    }
                }

                item {
                    Text("CEFR Learning Level:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LearningLevel.values()) { lvl ->
                            val isSel = selectedLevel == lvl
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedLevel = lvl },
                                label = { Text("${lvl.cefr} ${lvl.displayName}", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = batch,
                        onValueChange = { batch = it },
                        label = { Text("Class / Batch") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Teacher Evaluation Notes") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Active Enrollment Status", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Switch(checked = isActive, onCheckedChange = { isActive = it })
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    student.copy(
                        name = name,
                        email = email,
                        batch = batch,
                        level = selectedLevel,
                        teacherNotes = notes,
                        isActive = isActive
                    )
                )
            }) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun ScorePill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFEFF6FF),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 9.sp, color = MutedGray)
            Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
        }
    }
}

// -------------------------------------------------------------
// UNIVERSAL CREATE CHALLENGE DIALOG
// -------------------------------------------------------------
@Composable
fun CreateGameChallengeDialog(
    category: GameCategoryFilter,
    authorName: String,
    onDismiss: () -> Unit,
    onCreated: () -> Unit,
    viewModel: MainViewModel
) {
    var field1 by remember { mutableStateOf("") }
    var field2 by remember { mutableStateOf("") }
    var field3 by remember { mutableStateOf("") }
    var field4 by remember { mutableStateOf("") }
    var field5 by remember { mutableStateOf("") }
    var correctIndex by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(category.emoji, fontSize = 24.sp)
                Text("Add ${category.title} Challenge", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (category) {
                    GameCategoryFilter.WORD_MATCH -> {
                        OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Word (e.g. Eloquence)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Definition (e.g. Fluent speech)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field3, onValueChange = { field3 = it }, label = { Text("Category (e.g. Communication)") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.MISSING_LETTERS -> {
                        OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Full Word (e.g. PRONUNCIATION)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Meaning / Clue Hint") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.SENTENCE_BUILDER -> {
                        OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Target Sentence (e.g. She speaks English)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Grammar Hint / Category") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field3, onValueChange = { field3 = it }, label = { Text("Assamese Translation (অসমীয়া অনুবাদ - optional)") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.LISTEN_CHOOSE -> {
                        OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Audio Script Spoken (TTS)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Question Prompt") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field3, onValueChange = { field3 = it }, label = { Text("Option 1 (Correct by default)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field4, onValueChange = { field4 = it }, label = { Text("Option 2 (Distractor)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field5, onValueChange = { field5 = it }, label = { Text("Option 3 (Distractor)") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.PRONUNCIATION_BATTLE -> {
                        OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Challenge Sentence") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Phoneme Focus (e.g. Voiced TH sound)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field3, onValueChange = { field3 = it }, label = { Text("Difficulty (Easy, Medium, Hard)") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.FAST_VOCABULARY -> {
                        OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Target Word") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Prompt (e.g. Select synonym for)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field3, onValueChange = { field3 = it }, label = { Text("Correct Option") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field4, onValueChange = { field4 = it }, label = { Text("Distractor Option 1") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field5, onValueChange = { field5 = it }, label = { Text("Distractor Option 2") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.GRAMMAR_CHALLENGE -> {
                        OutlinedTextField(value = field1, onValueChange = { field1 = it }, label = { Text("Question Sentence with blank ___") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field2, onValueChange = { field2 = it }, label = { Text("Correct Option") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field3, onValueChange = { field3 = it }, label = { Text("Distractor Option 1") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field4, onValueChange = { field4 = it }, label = { Text("Distractor Option 2") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = field5, onValueChange = { field5 = it }, label = { Text("Grammar Rule Explanation") }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                when (category) {
                    GameCategoryFilter.WORD_MATCH -> {
                        if (field1.isNotBlank() && field2.isNotBlank()) {
                            viewModel.addWordMatchChallenge(field1, field2, field3.ifBlank { "General" }, authorName)
                            onCreated()
                        }
                    }
                    GameCategoryFilter.MISSING_LETTERS -> {
                        if (field1.isNotBlank()) {
                            viewModel.addMissingLettersChallenge(field1, field2.ifBlank { "English Vocabulary Word" }, authorName)
                            onCreated()
                        }
                    }
                    GameCategoryFilter.SENTENCE_BUILDER -> {
                        if (field1.isNotBlank()) {
                            viewModel.addSentenceBuilderChallenge(field1, field2.ifBlank { "Assemble syntax" }, field3.trim(), authorName)
                            onCreated()
                        }
                    }
                    GameCategoryFilter.LISTEN_CHOOSE -> {
                        if (field1.isNotBlank() && field2.isNotBlank()) {
                            val opts = listOf(field3.ifBlank { "Option A" }, field4.ifBlank { "Option B" }, field5.ifBlank { "Option C" })
                            viewModel.addListenChooseChallenge(field1, field2, opts, 0, authorName)
                            onCreated()
                        }
                    }
                    GameCategoryFilter.PRONUNCIATION_BATTLE -> {
                        if (field1.isNotBlank()) {
                            viewModel.addPronunciationBattleChallenge(field1, field2.ifBlank { "Clear pronunciation" }, field3.ifBlank { "Medium" }, 80, authorName)
                            onCreated()
                        }
                    }
                    GameCategoryFilter.FAST_VOCABULARY -> {
                        if (field1.isNotBlank()) {
                            val opts = listOf(field3.ifBlank { "Correct meaning" }, field4.ifBlank { "Wrong meaning 1" }, field5.ifBlank { "Wrong meaning 2" })
                            viewModel.addFastVocabularyChallenge(field1, field2.ifBlank { "Choose the meaning" }, opts, 0, authorName)
                            onCreated()
                        }
                    }
                    GameCategoryFilter.GRAMMAR_CHALLENGE -> {
                        if (field1.isNotBlank()) {
                            val opts = listOf(field2.ifBlank { "Correct form" }, field3.ifBlank { "Incorrect 1" }, field4.ifBlank { "Incorrect 2" })
                            viewModel.addGrammarChallenge(field1, opts, 0, field5.ifBlank { "Standard grammatical syntax rule." }, "Grammar", authorName)
                            onCreated()
                        }
                    }
                }
            }) {
                Text("Publish to Students 🚀")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun TeacherMetricCard(title: String, value: String, emoji: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = color)
            Text(title, fontSize = 9.sp, color = MutedGray, maxLines = 1)
        }
    }
}

@Composable
fun TeacherQuickActionCard(
    title: String,
    subtitle: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier.clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(icon, fontSize = 24.sp)
            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            Text(subtitle, fontSize = 10.sp, color = MutedGray)
        }
    }
}

@Composable
fun WeakAreaRow(area: String, count: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(area, fontSize = 12.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Text(count, fontSize = 11.sp, color = CoralAccent, fontWeight = FontWeight.Bold)
    }
}
@Composable
fun GradingSlider(label: String, value: Float, onValueChange: (Float) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp)
            Text("${value.toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 50f..100f,
            steps = 9
        )
    }
}
