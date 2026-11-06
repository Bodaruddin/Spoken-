package com.example.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.components.SectionHeader
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
    val currentUser by viewModel.currentUser.collectAsState()
    val students by viewModel.adminStudents.collectAsState()
    val teachers by viewModel.adminTeachers.collectAsState()
    val wordMatches by viewModel.wordMatchChallenges.collectAsState()
    val sentences by viewModel.sentenceBuilderChallenges.collectAsState()
    val totalGames = wordMatches.size + sentences.size + 15

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.2.dp, Color(0xFF60A5FA).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HeroBannerGrad)
                ) {
                    // Translucent university pillars watermark on right side (from screenshot)
                    Icon(
                        imageVector = Icons.Rounded.AccountBalance,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier
                            .size(120.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 12.dp, y = 14.dp)
                    )
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Golden circular badge with shield
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C))))
                                .border(2.dp, Color(0xFFFDE68A).copy(alpha = 0.85f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛡️", fontSize = 34.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentUser.name.ifBlank { "Dean Arthur Pendelton" },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Global Academic Administrator • Speakly University",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.82f)
                            )
                            Spacer(Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF070B24).copy(alpha = 0.65f),
                                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.45f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF10B981))
                                        )
                                        Text(
                                            text = "Institution Online",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF86EFAC)
                                        )
                                    }
                                }
                        }
                    }
                }
            }
        }

        // Executive KPI Cards (Matching Screenshot)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Institution KPI Overview")
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Total Students", "${students.size}", "🎓", RoyalBlue, Modifier.weight(1f), "Enrolled", Icons.Rounded.People)
                    AdminKpiCard("Faculty Teachers", "${teachers.size}", "👨‍🏫", PurpleAccent, Modifier.weight(1f), "Faculty", Icons.Rounded.Groups)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Daily Active Users", "${students.count { it.isActive }}", "⚡", EmeraldSuccess, Modifier.weight(1f), "Live Today", Icons.Rounded.BarChart)
                    AdminKpiCard("Avg Speaking Score", "84.2%", "🎖️", AmberGold, Modifier.weight(1f), "+4.1% MoM", Icons.Rounded.TrendingUp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Arcade Challenges", "$totalGames", "🎮", CyanAccent, Modifier.weight(1f), "12 Modes", Icons.Rounded.SportsEsports)
                    AdminKpiCard("Voice Practice Hours", "3,240h", "⏱️", CoralAccent, Modifier.weight(1f), "Total Hours", Icons.Rounded.Mic)
                }
            }
        }

        // Rapid Operations (Matching Screenshot 3-card gradient row)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Executive Rapid Operations")
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    // Card 1: Add Student (Purple-indigo gradient)
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.2.dp, Color(0xFF818CF8).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.selectAdminTab(AdminTab.STUDENTS) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.linearGradient(listOf(Color(0xFF4338CA), Color(0xFF6366F1))))
                                .padding(vertical = 16.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.PersonAdd, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                Text("Add Student", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Card 2: Manage Users (Cyan-teal gradient)
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.2.dp, Color(0xFF22D3EE).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.selectAdminTab(AdminTab.TEACHERS) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.linearGradient(listOf(Color(0xFF0E7490), Color(0xFF06B6D4))))
                                .padding(vertical = 16.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.Groups, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                Text("Manage Users", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Card 3: View Reports (Pink-magenta gradient)
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        border = BorderStroke(1.2.dp, Color(0xFFF472B6).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.selectAdminTab(AdminTab.CURRICULUM) }
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Brush.linearGradient(listOf(Color(0xFFBE185D), Color(0xFFEC4899))))
                                .padding(vertical = 16.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Rounded.BarChart, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                Text("View Reports", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                // Secondary Rapid Actions
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                    border = BorderStroke(1.dp, CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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

                        // Direct switch to Teacher Panel for Admin without Security PIN
                        OutlinedButton(
                            onClick = { viewModel.switchRole(Role.TEACHER) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_switch_to_teacher_panel_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleAccent),
                            border = BorderStroke(1.5.dp, PurpleAccent)
                        ) {
                            Icon(Icons.Rounded.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp), tint = PurpleAccent)
                            Spacer(Modifier.width(6.dp))
                            Text("👩‍🏫 Switch to Teacher Panel (Direct Access • No PIN)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    AdminActivityRow("Weekly speaking tournament started for Class 10", "2 hours ago", "🏆")
                }
            }
        }
    }
}

// -------------------------------------------------------------
// ADMIN STUDENTS MANAGEMENT (FULL CRUD & DIAGNOSTICS)
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminStudentsManagement(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val students by viewModel.adminStudents.collectAsState()
    val academicClasses by viewModel.academicClasses.collectAsState()
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
                "Active" -> s.isActive
                "Needs Attention" -> s.speakingScore < 80
                else -> s.batch.equals(selectedBatchFilter, ignoreCase = true)
            }
            matchesQuery && matchesBatch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
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
                placeholder = { Text("Filter by student name, email, or class...", fontSize = 13.sp) },
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
                val filters = listOf("All") + academicClasses + listOf("Active", "Needs Attention")
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
                            Text("${s.email} • Class: ${s.studentClass}", fontSize = 11.sp, color = MutedGray)
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
        var newStudentClass by remember { mutableStateOf(com.example.data.model.AcademicClasses.DEFAULT_CLASS) }
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

                    // Select Class
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Rounded.School, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                            Text("Select Class:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text("($newStudentClass)", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = RoyalBlue)
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            academicClasses.forEach { cls ->
                                FilterChip(
                                    selected = newStudentClass == cls,
                                    onClick = { newStudentClass = cls },
                                    label = { Text(cls, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

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
                        viewModel.repository.addStudent(newStudentName, newStudentEmail, newStudentClass, newStudentLevel)
                        statusMsg = "Created student account for $newStudentName in $newStudentClass!"
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
            classes = academicClasses,
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
    var selectedTeacherForEdit by remember { mutableStateOf<User?>(null) }
    var teacherToDelete by remember { mutableStateOf<User?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var statusMsg by remember { mutableStateOf<String?>(null) }

    val filteredTeachers = remember(teachers, searchQuery) {
        if (searchQuery.isBlank()) teachers
        else teachers.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true) ||
                    it.batch.contains(searchQuery, ignoreCase = true) ||
                    it.id.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_teachers_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Header Card with Add and Switch options
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
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
                                Text("👨‍🏫", fontSize = 20.sp)
                            }
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "Faculty & Instructors (${teachers.size})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    "Edit, delete & configure instructor permissions",
                                    fontSize = 11.sp,
                                    color = MutedGray,
                                    maxLines = 1
                                )
                            }
                        }

                        Button(
                            onClick = { showAddTeacherDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("add_teacher_button")
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        }
                    }

                    // Direct Quick Switch to Teacher Panel without PIN
                    OutlinedButton(
                        onClick = { viewModel.switchRole(Role.TEACHER) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_switch_to_teacher_portal_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PurpleAccent),
                        border = BorderStroke(1.dp, PurpleAccent.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Rounded.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp), tint = PurpleAccent)
                        Spacer(Modifier.width(6.dp))
                        Text("Switch to Teacher Panel (Direct Access • No PIN)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search faculty by name, email, or department...", fontSize = 13.sp) },
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

        statusMsg?.let { msg ->
            item {
                Surface(shape = RoundedCornerShape(10.dp), color = Color(0xFFDCFCE7), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(msg, color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                        IconButton(onClick = { statusMsg = null }, modifier = Modifier.size(18.dp)) {
                            Icon(Icons.Filled.Clear, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }

        if (filteredTeachers.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("👨‍🏫", fontSize = 36.sp)
                        Text("No faculty members found", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Try a different search term or add a new teacher above.", fontSize = 12.sp, color = MutedGray)
                    }
                }
            }
        }

        items(filteredTeachers, key = { it.id }) { t ->
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("teacher_card_${t.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(t.avatarEmoji, fontSize = 30.sp)
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(t.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (t.isActive) Color(0xFFDCFCE7) else Color(0xFFF3F4F6)
                                ) {
                                    Text(
                                        if (t.isActive) "Active Faculty" else "Inactive",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (t.isActive) EmeraldSuccess else MutedGray,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(t.email, fontSize = 12.sp, color = MutedGray)
                            Text("Assigned Department: ${t.batch}", fontSize = 11.sp, color = RoyalBlue)
                            Text("ID: ${t.id}", fontSize = 10.sp, color = MutedGray)
                        }

                        // Toggle Active Status
                        Switch(
                            checked = t.isActive,
                            onCheckedChange = { viewModel.toggleTeacherActive(t.id) }
                        )
                    }

                    // Security PIN Info Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(Icons.Rounded.Lock, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(14.dp))
                                Text(
                                    "Security PIN: ",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    t.securityPin.ifBlank { "7321" },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlue
                                )
                            }
                            Text("Used for teacher login", fontSize = 10.sp, color = MutedGray)
                        }
                    }

                    // Action Buttons: Edit and Delete
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { selectedTeacherForEdit = t },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("edit_teacher_button_${t.id}"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Edit Teacher", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        IconButton(
                            onClick = { teacherToDelete = t },
                            modifier = Modifier.testTag("delete_teacher_button_${t.id}")
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete Teacher",
                                tint = CoralAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Teacher Dialog
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
                        viewModel.addTeacher(name, email, department, securityPin)
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

    // Edit Teacher Dialog
    selectedTeacherForEdit?.let { teacher ->
        var editName by remember { mutableStateOf(teacher.name) }
        var editEmail by remember { mutableStateOf(teacher.email) }
        var editDept by remember { mutableStateOf(teacher.batch) }
        var editPin by remember { mutableStateOf(teacher.securityPin.ifBlank { "7321" }) }
        var editPinVisible by remember { mutableStateOf(false) }
        var editActive by remember { mutableStateOf(teacher.isActive) }
        var editAvatar by remember { mutableStateOf(teacher.avatarEmoji) }
        var editErrorMsg by remember { mutableStateOf<String?>(null) }

        val emojiOptions = listOf("👩‍🏫", "👨‍🏫", "👩‍💼", "🧑‍🏫", "🎓", "🦉")

        AlertDialog(
            onDismissRequest = { selectedTeacherForEdit = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(editAvatar, fontSize = 22.sp)
                    Text("Edit Teacher Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Emoji Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        emojiOptions.forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (editAvatar == emoji) RoyalBlue.copy(alpha = 0.2f) else Color.Transparent
                                    )
                                    .clickable { editAvatar = emoji },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editName,
                        onValueChange = {
                            editName = it
                            editErrorMsg = null
                        },
                        label = { Text("Teacher Full Name") },
                        leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = RoyalBlue) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = {
                            editEmail = it
                            editErrorMsg = null
                        },
                        label = { Text("Institutional Email") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = RoyalBlue) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editDept,
                        onValueChange = {
                            editDept = it
                            editErrorMsg = null
                        },
                        label = { Text("Department / Cohort") },
                        leadingIcon = { Icon(Icons.Rounded.School, contentDescription = null, tint = RoyalBlue) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = editPin,
                        onValueChange = {
                            editPin = it
                            editErrorMsg = null
                        },
                        label = { Text("Security PIN") },
                        leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = RoyalBlue) },
                        trailingIcon = {
                            IconButton(onClick = { editPinVisible = !editPinVisible }) {
                                Icon(
                                    imageVector = if (editPinVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (editPinVisible) "Hide PIN" else "Show PIN"
                                )
                            }
                        },
                        visualTransformation = if (editPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        supportingText = {
                            Text("Security PIN used to unlock Teacher panel.", fontSize = 11.sp, color = MutedGray)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Active Faculty Status", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Switch(
                            checked = editActive,
                            onCheckedChange = { editActive = it }
                        )
                    }

                    if (editErrorMsg != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFEE2E2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = editErrorMsg ?: "",
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
                Button(
                    onClick = {
                        if (editName.isBlank() || editEmail.isBlank()) {
                            editErrorMsg = "Please enter both teacher name and email."
                        } else if (editPin.trim().length < 4) {
                            editErrorMsg = "Security PIN must be at least 4 digits."
                        } else {
                            val updated = teacher.copy(
                                name = editName.trim(),
                                email = editEmail.trim(),
                                batch = editDept.trim().ifBlank { "Spoken English Dept" },
                                securityPin = editPin.trim(),
                                avatarEmoji = editAvatar,
                                isActive = editActive
                            )
                            viewModel.updateTeacher(updated)
                            statusMsg = "Updated ${updated.name} successfully!"
                            selectedTeacherForEdit = null
                        }
                    },
                    modifier = Modifier.testTag("save_teacher_edits_button")
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTeacherForEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Teacher Confirmation Dialog
    teacherToDelete?.let { teacher ->
        AlertDialog(
            onDismissRequest = { teacherToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Delete, contentDescription = null, tint = CoralAccent)
                    Text("Delete Faculty Teacher", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Are you sure you want to remove ${teacher.name} (${teacher.email}) from faculty?",
                        fontSize = 14.sp
                    )
                    Text(
                        "This will revoke their access to the Teacher Panel and assignments. This action cannot be undone.",
                        fontSize = 12.sp,
                        color = MutedGray
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTeacher(teacher.id)
                        statusMsg = "Removed ${teacher.name} from faculty."
                        teacherToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralAccent),
                    modifier = Modifier.testTag("confirm_delete_teacher_button")
                ) {
                    Text("Delete Teacher", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { teacherToDelete = null }) {
                    Text("Cancel")
                }
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
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false
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
fun AdminKpiCard(
    title: String,
    value: String,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier,
    tag: String = "Live",
    watermarkIcon: ImageVector? = null
) {
    val cardBg: Color
    val cardBorder: Color
    val iconGrad: Brush
    val defaultWatermark: ImageVector

    when (color) {
        RoyalBlue -> {
            cardBg = NeonBlueCardBg
            cardBorder = NeonBlueBorder
            iconGrad = NeonBlueGrad
            defaultWatermark = Icons.Rounded.People
        }
        PurpleAccent -> {
            cardBg = NeonPurpleCardBg
            cardBorder = NeonPurpleBorder
            iconGrad = NeonPurpleGrad
            defaultWatermark = Icons.Rounded.Groups
        }
        EmeraldSuccess -> {
            cardBg = NeonGreenCardBg
            cardBorder = NeonGreenBorder
            iconGrad = NeonGreenGrad
            defaultWatermark = Icons.Rounded.BarChart
        }
        AmberGold -> {
            cardBg = NeonAmberCardBg
            cardBorder = NeonAmberBorder
            iconGrad = NeonAmberGrad
            defaultWatermark = Icons.Rounded.TrendingUp
        }
        CyanAccent -> {
            cardBg = NeonCyanCardBg
            cardBorder = NeonCyanBorder
            iconGrad = NeonCyanGrad
            defaultWatermark = Icons.Rounded.SportsEsports
        }
        CoralAccent, SoftPink -> {
            cardBg = NeonPinkCardBg
            cardBorder = NeonPinkBorder
            iconGrad = NeonPinkGrad
            defaultWatermark = Icons.Rounded.Mic
        }
        else -> {
            cardBg = Color(0xFF0F1535)
            cardBorder = color
            iconGrad = Brush.linearGradient(listOf(color, color))
            defaultWatermark = Icons.Rounded.Analytics
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.2.dp, cardBorder.copy(alpha = 0.55f)),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Subtle watermark icon in bottom-right corner as seen in screenshot
            Icon(
                imageVector = watermarkIcon ?: defaultWatermark,
                contentDescription = null,
                tint = cardBorder.copy(alpha = 0.22f),
                modifier = Modifier
                    .size(38.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-10).dp, y = (-10).dp)
            )

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(iconGrad),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 22.sp)
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF070B1F).copy(alpha = 0.70f),
                        border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.50f))
                    ) {
                        Text(
                            text = tag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = value,
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = Color.White
                    )
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
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
