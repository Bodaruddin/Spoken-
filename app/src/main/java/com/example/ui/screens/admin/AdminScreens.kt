package com.example.ui.screens.admin

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningLevel
import com.example.data.model.Role
import com.example.data.model.StudentManagementInfo
import com.example.data.model.User
import com.example.ui.AdminSpeakingSection
import com.example.ui.AdminTab
import com.example.ui.MainViewModel
import com.example.ui.screens.common.SpeakingAndPhoneticsManagement
import com.example.ui.screens.common.SpeakingManageTab
import com.example.ui.screens.teacher.ActiveEditingChallenge
import com.example.ui.screens.teacher.CreateGameChallengeDialog
import com.example.ui.screens.teacher.EditGameChallengeDialog
import com.example.ui.screens.teacher.GameCategoryFilter
import com.example.ui.screens.teacher.GameItemCard
import com.example.ui.theme.*

@Composable
fun AdminMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentAdminTab.collectAsState()

    when (currentTab) {
        AdminTab.DASHBOARD -> AdminDashboard(viewModel, modifier)
        AdminTab.STUDENTS -> AdminStudentsManagement(viewModel, modifier)
        AdminTab.GAMES -> AdminGamesManagement(viewModel, modifier)
        AdminTab.TEACHERS -> AdminTeachersManagement(viewModel, modifier)
        AdminTab.CURRICULUM -> AdminCurriculumManagement(viewModel, modifier)
    }
}

// -------------------------------------------------------------
// ADMIN DASHBOARD
// -------------------------------------------------------------
@Composable
fun AdminDashboard(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val students by viewModel.adminStudents.collectAsState()
    val teachers by viewModel.adminTeachers.collectAsState()
    val wordMatches by viewModel.wordMatchChallenges.collectAsState()
    val sentences by viewModel.sentenceBuilderChallenges.collectAsState()
    val totalGames = wordMatches.size + sentences.size + 15

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("admin_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
                                Brush.linearGradient(listOf(AmberGold, Color(0xFFD97706)))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛡️", fontSize = 32.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Dean Arthur Pendelton", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Global Academic Administrator • Speakly University", fontSize = 12.sp, color = MutedGray)
                        Spacer(Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text("● Academic Institution Online", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RoyalBlue, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                }
            }
        }

        // Executive KPI Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Institution KPI Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Total Students", "${students.size}", "🎓", RoyalBlue, Modifier.weight(1f))
                    AdminKpiCard("Faculty Teachers", "${teachers.size}", "👨‍🏫", PurpleAccent, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Daily Active Users", "${students.count { it.isActive }}", "⚡", EmeraldSuccess, Modifier.weight(1f))
                    AdminKpiCard("Avg Speaking Score", "84.2%", "🎙️", AmberGold, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Arcade Challenges", "$totalGames", "🎮", CyanAccent, Modifier.weight(1f))
                    AdminKpiCard("Voice Practice Hours", "3,240h", "⏱️", CoralAccent, Modifier.weight(1f))
                }
            }
        }

        // Rapid Operations
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Executive Rapid Operations", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.selectAdminTab(AdminTab.STUDENTS) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Students", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.selectAdminTab(AdminTab.GAMES) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Games Hub", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.selectAdminTab(AdminTab.CURRICULUM) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Curriculum", fontSize = 11.sp)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                viewModel.selectAdminSpeakingSection(AdminSpeakingSection.SPEAKING_PROMPTS)
                                viewModel.selectAdminTab(AdminTab.CURRICULUM)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            Text("🎙️ AI Speaking", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                viewModel.selectAdminSpeakingSection(AdminSpeakingSection.PHONETICS_DRILLS)
                                viewModel.selectAdminTab(AdminTab.CURRICULUM)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold)
                        ) {
                            Text("🎯 Phonetics", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Account Permissions & Registration Controls
        item {
            val allowStudentReg by viewModel.allowStudentRegistration.collectAsState()
            val allowTeacherReg by viewModel.allowTeacherRegistration.collectAsState()

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Rounded.Security, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(20.dp))
                        Text("Account Registration & Access Permissions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Text(
                        "Manage student onboarding permissions. Faculty instructors are added directly by the Administrator in the Faculty tab.",
                        fontSize = 12.sp,
                        color = MutedGray
                    )

                    // Student Registration Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow Students to Create New Account", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("When enabled, learners can self-register and set their PIN on the sign-in screen", fontSize = 11.sp, color = MutedGray)
                        }
                        Switch(
                            checked = allowStudentReg,
                            onCheckedChange = { viewModel.setAllowStudentRegistration(it) }
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("👨‍🏫", fontSize = 16.sp)
                            Text(
                                "Faculty members cannot self-register; they are added by Admin in the Faculty tab with their assigned security PIN.",
                                fontSize = 11.sp,
                                color = RoyalBlueDark,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }
        }

        // Learning Activity Feed
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Campus Live Activity Stream", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    AdminActivityRow("Alex Morgan scored 92% in Cafe Dialogue Roleplay", "5 mins ago", "🎙️")
                    AdminActivityRow("Prof. Eleanor Vance graded 3 audio submissions", "18 mins ago", "✍️")
                    AdminActivityRow("New Word Match challenges added to Arcade Hub", "1 hour ago", "🎮")
                    AdminActivityRow("Weekly speaking tournament started for Batch Alpha", "2 hours ago", "🏆")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ADMIN STUDENTS MANAGEMENT (FULL CRUD & DIAGNOSTICS)
// -------------------------------------------------------------
@Composable
fun AdminStudentsManagement(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val students by viewModel.adminStudents.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedStudentForEdit by remember { mutableStateOf<StudentManagementInfo?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedBatchFilter by remember { mutableStateOf("All") }
    var statusMsg by remember { mutableStateOf<String?>(null) }

    val filteredStudents = remember(students, searchQuery, selectedBatchFilter) {
        students.filter { s ->
            val matchesQuery = s.name.contains(searchQuery, ignoreCase = true) ||
                    s.email.contains(searchQuery, ignoreCase = true) ||
                    s.batch.contains(searchQuery, ignoreCase = true)
            val matchesBatch = when (selectedBatchFilter) {
                "All" -> true
                "Batch Alpha" -> s.batch.contains("Alpha")
                "Batch Beta" -> s.batch.contains("Beta")
                "Batch Gamma" -> s.batch.contains("Gamma")
                "Active" -> s.isActive
                "Needs Attention" -> s.speakingScore < 80
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
            .testTag("admin_students_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            .padding(end = 8.dp),
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
                            Text("🎓", fontSize = 20.sp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Student Roster",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                "${students.size} enrolled learners",
                                fontSize = 11.sp,
                                color = MutedGray,
                                maxLines = 1
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = {
                                viewModel.repository.bulkImportStudents(3)
                                statusMsg = "Successfully imported 3 student records via CSV spreadsheet!"
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }

                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text("Add", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Filter by student name, email, or batch...", fontSize = 13.sp) },
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
        }

        // Filter Pills
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf("All", "Batch Alpha", "Batch Beta", "Batch Gamma", "Active", "Needs Attention")
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

        statusMsg?.let { msg ->
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFDCFCE7),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(msg, color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }

        items(filteredStudents) { s ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(s.avatarEmoji, fontSize = 30.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(s.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Surface(shape = RoundedCornerShape(6.dp), color = RoyalBlue) {
                                    Text(s.level.cefr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Text("${s.email} • ${s.batch}", fontSize = 11.sp, color = MutedGray)
                            Text("ID: ${s.id} • Joined: ${s.joinedDate}", fontSize = 10.sp, color = RoyalBlue)
                        }

                        Switch(
                            checked = s.isActive,
                            onCheckedChange = { viewModel.repository.toggleStudentActive(s.id) }
                        )
                    }

                    // Score summary bar
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Speaking Proficiency: ${s.speakingScore}%", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Text("Pronunciation: ${s.pronunciationScore}% • Fluency: ${s.fluencyScore}%", fontSize = 11.sp, color = MutedGray)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { selectedStudentForEdit = s },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Profile & Levels", fontSize = 11.sp)
                        }

                        IconButton(onClick = { viewModel.deleteStudent(s.id) }) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = CoralAccent, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }

    // Add Student Dialog
    if (showAddDialog) {
        var newStudentName by remember { mutableStateOf("") }
        var newStudentEmail by remember { mutableStateOf("") }
        var newStudentBatch by remember { mutableStateOf("Batch Alpha") }
        var newStudentLevel by remember { mutableStateOf(LearningLevel.BEGINNER) }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Create New Student Account") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newStudentName,
                        onValueChange = { newStudentName = it },
                        label = { Text("Student Full Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newStudentEmail,
                        onValueChange = { newStudentEmail = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newStudentBatch,
                        onValueChange = { newStudentBatch = it },
                        label = { Text("Class / Batch") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Starting CEFR Level:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LearningLevel.values()) { lvl ->
                            FilterChip(
                                selected = newStudentLevel == lvl,
                                onClick = { newStudentLevel = lvl },
                                label = { Text(lvl.cefr, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newStudentName.isNotBlank() && newStudentEmail.isNotBlank()) {
                        viewModel.repository.addStudent(newStudentName, newStudentEmail, newStudentBatch, newStudentLevel)
                        statusMsg = "Created student account for $newStudentName!"
                        showAddDialog = false
                    }
                }) {
                    Text("Create Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Edit Student Dialog
    selectedStudentForEdit?.let { s ->
        com.example.ui.screens.teacher.StudentDetailDialog(
            student = s,
            onDismiss = { selectedStudentForEdit = null },
            onSave = { updated ->
                viewModel.updateStudent(updated)
                selectedStudentForEdit = null
                statusMsg = "Updated student ${updated.name} successfully!"
            }
        )
    }
}

// -------------------------------------------------------------
// ADMIN GAMES & CURRICULUM MANAGEMENT
// -------------------------------------------------------------
@Composable
fun AdminGamesManagement(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    var selectedCategory by remember { mutableStateOf(GameCategoryFilter.WORD_MATCH) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var activeEditingChallenge by remember { mutableStateOf<ActiveEditingChallenge?>(null) }
    var statusMsg by remember { mutableStateOf<String?>(null) }

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
            .testTag("admin_games_screen"),
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
                                "Curriculum Games Hub Manager",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                "Institutional repository of arcade games",
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

        // Category Switcher
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

        // Prominent "+ Add New Question" banner card
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
                                "Author questions and immediately deploy to student arcade",
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

        // Category Content with Edit & Delete support
        when (selectedCategory) {
            GameCategoryFilter.WORD_MATCH -> {
                item { Text("Word Match Challenges (${wordMatches.size} pairs registered)", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
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
                                    statusMsg = "Dean Arthur Pendelton updated '${f1}' challenge!"
                                }
                            )
                        },
                        onDelete = { viewModel.deleteWordMatchChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.MISSING_LETTERS -> {
                item { Text("Missing Letters Challenges (${missingLetters.size} active)", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
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
                                    statusMsg = "Dean Arthur Pendelton updated '${f1.uppercase()}' missing letters challenge!"
                                }
                            )
                        },
                        onDelete = { viewModel.deleteMissingLettersChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.SENTENCE_BUILDER -> {
                item { Text("Sentence Builder Challenges (${sentenceBuilders.size} active)", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                items(sentenceBuilders) { item ->
                    GameItemCard(
                        title = item.targetSentence,
                        subtitle = "Hint: ${item.hint}" + if (item.assameseTranslation.isNotBlank()) "\n🇮🇳 অসমীয়া: ${item.assameseTranslation}" else "",
                        tag = "Syntax Assembly",
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
                                    statusMsg = "Dean Arthur Pendelton updated sentence builder challenge!"
                                }
                            )
                        },
                        onDelete = { viewModel.deleteSentenceBuilderChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.LISTEN_CHOOSE -> {
                item { Text("Listen & Choose Challenges (${listenChooses.size} items)", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                items(listenChooses) { item ->
                    GameItemCard(
                        title = item.question,
                        subtitle = "Audio: \"${item.audioScript}\"",
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
                                    statusMsg = "Dean Arthur Pendelton updated Listen & Choose challenge!"
                                }
                            )
                        },
                        onDelete = { viewModel.deleteListenChooseChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.PRONUNCIATION_BATTLE -> {
                item { Text("Pronunciation Battles (${pronunciationBattles.size} challenges)", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                items(pronunciationBattles) { item ->
                    GameItemCard(
                        title = item.challengeSentence,
                        subtitle = "Phoneme Focus: ${item.targetPhonemeFocus}",
                        tag = "Target: ${item.passingScore}% (${item.difficulty})",
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
                                    statusMsg = "Dean Arthur Pendelton updated pronunciation battle challenge!"
                                }
                            )
                        },
                        onDelete = { viewModel.deletePronunciationBattleChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.FAST_VOCABULARY -> {
                item { Text("Fast Vocabulary Blitzes (${fastVocabs.size} drills)", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
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
                                    statusMsg = "Dean Arthur Pendelton updated fast vocabulary item '${f1}'!"
                                }
                            )
                        },
                        onDelete = { viewModel.deleteFastVocabularyChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.GRAMMAR_CHALLENGE -> {
                item { Text("Grammar Master Challenges (${grammarChallenges.size} questions)", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
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
                                    statusMsg = "Dean Arthur Pendelton updated grammar challenge question!"
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
            authorName = "Dean Arthur Pendelton",
            onDismiss = { showCreateDialog = false },
            onCreated = {
                statusMsg = "Dean Arthur Pendelton published new challenge to ${selectedCategory.title}!"
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

// -------------------------------------------------------------
// ADMIN TEACHERS MANAGEMENT
// -------------------------------------------------------------
@Composable
fun AdminTeachersManagement(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val teachers by viewModel.adminTeachers.collectAsState()
    var showAddTeacherDialog by remember { mutableStateOf(false) }
    var statusMsg by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            Text("👨‍🏫", fontSize = 20.sp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Faculty & Instructors",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                "Manage teaching staff & course assignments",
                                fontSize = 11.sp,
                                color = MutedGray,
                                maxLines = 1
                            )
                        }
                    }

                    Button(
                        onClick = { showAddTeacherDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }

        statusMsg?.let { msg ->
            item {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Text(msg, color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }

        items(teachers) { t ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(t.avatarEmoji, fontSize = 28.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(t.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(t.email, fontSize = 12.sp, color = MutedGray)
                        Text("Assigned Department: ${t.batch}", fontSize = 11.sp, color = RoyalBlue)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFDCFCE7)) {
                        Text("Active Faculty", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }

    if (showAddTeacherDialog) {
        var name by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }
        var department by remember { mutableStateOf("Spoken English Dept") }
        var securityPin by remember { mutableStateOf("") }
        var pinVisible by remember { mutableStateOf(false) }
        var errorMsg by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showAddTeacherDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("👨‍🏫", fontSize = 22.sp)
                    Text("Add Faculty Instructor", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                            errorMsg = null
                        },
                        label = { Text("Teacher Name") },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = RoyalBlue) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            errorMsg = null
                        },
                        label = { Text("Institutional Email") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = RoyalBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = department,
                        onValueChange = {
                            department = it
                            errorMsg = null
                        },
                        label = { Text("Department / Cohort") },
                        leadingIcon = { Icon(Icons.Rounded.School, contentDescription = null, tint = RoyalBlue) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = securityPin,
                        onValueChange = {
                            securityPin = it
                            errorMsg = null
                        },
                        label = { Text("Set Security PIN") },
                        placeholder = { Text("e.g. 4-6 digit numeric PIN") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = RoyalBlue) },
                        trailingIcon = {
                            IconButton(onClick = { pinVisible = !pinVisible }) {
                                Icon(
                                    imageVector = if (pinVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (pinVisible) "Hide PIN" else "Show PIN"
                                )
                            }
                        },
                        visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        supportingText = {
                            Text("Assigned PIN for teacher to sign into teacher panel.", fontSize = 11.sp, color = MutedGray)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (errorMsg != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMsg ?: "",
                                color = CoralAccent,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isBlank() || email.isBlank()) {
                        errorMsg = "Please enter both teacher name and institutional email."
                    } else if (securityPin.trim().length < 4) {
                        errorMsg = "Security PIN must be at least 4 digits."
                    } else {
                        viewModel.repository.addTeacher(name, email, department, securityPin)
                        statusMsg = "Added $name to Academic Faculty with custom Security PIN!"
                        showAddTeacherDialog = false
                    }
                }) {
                    Text("Register Teacher")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddTeacherDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// -------------------------------------------------------------
// ADMIN CURRICULUM MANAGEMENT
// -------------------------------------------------------------
enum class AdminCurriculumSection(val title: String, val emoji: String) {
    COURSES("CEFR Framework", "📚"),
    SPEAKING("AI Speaking", "🎙️"),
    PHONETICS("Phonetics & Drills", "🎯")
}

@Composable
fun AdminCurriculumManagement(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val adminSpeakingSection by viewModel.adminSpeakingSection.collectAsState()
    var selectedSection by remember { mutableStateOf(AdminCurriculumSection.COURSES) }

    LaunchedEffect(adminSpeakingSection) {
        selectedSection = when (adminSpeakingSection) {
            AdminSpeakingSection.SPEAKING_PROMPTS -> AdminCurriculumSection.SPEAKING
            AdminSpeakingSection.PHONETICS_DRILLS -> AdminCurriculumSection.PHONETICS
            AdminSpeakingSection.ROLEPLAY_SCENARIOS -> AdminCurriculumSection.SPEAKING
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Section Selector for Admin
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
                AdminCurriculumSection.values().forEach { sec ->
                    val isSel = selectedSection == sec
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedSection = sec }
                            .testTag("admin_curriculum_section_${sec.name.lowercase()}")
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

        when (selectedSection) {
            AdminCurriculumSection.COURSES -> {
                AdminCurriculumFrameworkView(viewModel = viewModel, modifier = Modifier.weight(1f))
            }
            AdminCurriculumSection.SPEAKING -> {
                SpeakingAndPhoneticsManagement(
                    viewModel = viewModel,
                    userRole = Role.ADMIN,
                    modifier = Modifier.weight(1f),
                    initialTab = SpeakingManageTab.SPEAKING_PROMPTS
                )
            }
            AdminCurriculumSection.PHONETICS -> {
                SpeakingAndPhoneticsManagement(
                    viewModel = viewModel,
                    userRole = Role.ADMIN,
                    modifier = Modifier.weight(1f),
                    initialTab = SpeakingManageTab.PHONETICS_DRILLS
                )
            }
        }
    }
}

@Composable
fun AdminCurriculumFrameworkView(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val levels = LearningLevel.values()
    var exportToast by remember { mutableStateOf<String?>(null) }
    var notificationsEnabled by remember { mutableStateOf(true) }
    var offlineSyncEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
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
                            Text("📚", fontSize = 20.sp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Course Curriculum",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                "CEFR framework & speaking levels",
                                fontSize = 11.sp,
                                color = MutedGray,
                                maxLines = 1
                            )
                        }
                    }

                    Button(
                        onClick = { exportToast = "Report exported to Speakly_Academic_Audit.pdf" },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Export", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                    }
                }
            }
        }

        exportToast?.let { msg ->
            item {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFEFF6FF), modifier = Modifier.fillMaxWidth()) {
                    Text(msg, color = RoyalBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }

        items(levels) { lvl ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
                            .padding(end = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = RoyalBlue
                        ) {
                            Text(
                                lvl.cefr,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                lvl.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1
                            )
                            Text(
                                "18 Lessons • 42 Speaking Tasks",
                                fontSize = 11.sp,
                                color = MutedGray,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text(
                            "Published",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldSuccess,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        item {
            Text("Institutional Governance & Synchronization", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text("Automated Daily Reminders", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Push notification reminders for student streaks", fontSize = 11.sp, color = MutedGray)
                        }
                        Switch(checked = notificationsEnabled, onCheckedChange = { notificationsEnabled = it })
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text("Offline Data Synchronization", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Automatic background sync when device reconnects", fontSize = 11.sp, color = MutedGray)
                        }
                        Switch(checked = offlineSyncEnabled, onCheckedChange = { offlineSyncEnabled = it })
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                            Text("Supabase Cloud Database", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("External PostgreSQL tables & student metrics sync", fontSize = 11.sp, color = MutedGray)
                        }
                        Button(
                            onClick = { viewModel.toggleSupabaseModal(true) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Connect DB", fontSize = 11.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// REUSABLE DASHBOARD CARDS
// -------------------------------------------------------------
@Composable
fun AdminKpiCard(title: String, value: String, emoji: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 22.sp)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
            Text(title, fontSize = 11.sp, color = MutedGray)
        }
    }
}

@Composable
fun AdminActivityRow(activity: String, time: String, icon: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon, fontSize = 16.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(activity, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(time, fontSize = 10.sp, color = MutedGray)
        }
    }
}
