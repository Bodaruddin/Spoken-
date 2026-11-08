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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.SectionHeader
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

    var selectedRange by remember { mutableStateOf("This Month") }
    var showRangeDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("admin_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card: Dean Arthur Pendelton (Exact match to screenshot)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(
                    1.5.dp,
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFEC4899),
                            Color(0xFF818CF8),
                            Color(0xFF06B6D4)
                        )
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF13113F),
                                    Color(0xFF0B1438),
                                    Color(0xFF081232)
                                )
                            )
                        )
                ) {
                    // Fluid bottom wave in hero card
                    KpiCardFluidWave(
                        color = Color(0xFF818CF8),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                            .align(Alignment.BottomCenter)
                    )

                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Golden double-ringed shield emblem with open book
                        GoldenAcademicShieldEmblem(
                            modifier = Modifier.size(54.dp)
                        )

                        Spacer(Modifier.width(10.dp))

                        // Admin Info
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = currentUser.name.ifBlank { "Dean Arthur Pendelton" },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Global Academic Administrator",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFA5B4FC),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = Color(0xFFA5B4FC),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = "Speakly University",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFA5B4FC),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.height(3.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF04261B).copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
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
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Greek/Roman university building with pediment, pillars, dome, flag, and green trees
                        UniversityBuildingIllustration(
                            modifier = Modifier.size(width = 80.dp, height = 72.dp)
                        )
                    }
                }
            }
        }

        // Section 1: Institution KPI Overview (Matching Screenshot 2x3 Grid)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Header with Dropdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BarChart,
                            contentDescription = null,
                            tint = Color(0xFF818CF8),
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Institution KPI Overview",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }

                    // Dropdown Range Selector
                    Box {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1E1742),
                            border = BorderStroke(1.dp, Color(0xFF6366F1).copy(alpha = 0.55f)),
                            modifier = Modifier.clickable { showRangeDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = selectedRange,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFFC084FC),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showRangeDropdown,
                            onDismissRequest = { showRangeDropdown = false }
                        ) {
                            listOf("This Month", "Last Month", "This Semester", "All Time").forEach { range ->
                                DropdownMenuItem(
                                    text = { Text(range, fontSize = 12.sp) },
                                    onClick = {
                                        selectedRange = range
                                        showRangeDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Row 1: Total Students & Faculty Teachers
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard(
                        title = "Total Students",
                        value = "${students.size.coerceAtLeast(6)}",
                        tag = "Enrolled",
                        growth = "+20% vs last month",
                        color = Color(0xFF38BDF8),
                        watermarkIcon = Icons.Rounded.Groups,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    AdminKpiCard(
                        title = "Faculty Teachers",
                        value = "${teachers.size.coerceAtLeast(3)}",
                        tag = "Faculty",
                        growth = "+0% vs last month",
                        color = Color(0xFFA855F7),
                        watermarkIcon = Icons.Rounded.Groups,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("👩‍🏫", fontSize = 22.sp)
                    }
                }

                // Row 2: Daily Active Users & Avg Speaking Score
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard(
                        title = "Daily Active Users",
                        value = "${students.count { it.isActive }.coerceAtLeast(5)}",
                        tag = "Live Today",
                        growth = "+25% vs last month",
                        color = Color(0xFF10B981),
                        watermarkIcon = Icons.Rounded.BarChart,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Bolt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    AdminKpiCard(
                        title = "Avg Speaking Score",
                        value = "84.2%",
                        tag = "+4.1% MoM",
                        growth = "+4.1% vs last month",
                        color = Color(0xFFF59E0B),
                        watermarkIcon = Icons.Rounded.TrendingUp,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WorkspacePremium,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Row 3: Arcade Challenges & Voice Practice Hours
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard(
                        title = "Arcade Challenges",
                        value = "$totalGames",
                        tag = "12 Modes",
                        growth = "+15% vs last month",
                        color = Color(0xFF06B6D4),
                        watermarkIcon = Icons.Rounded.SportsEsports,
                        tagIcon = Icons.Rounded.GridView,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SportsEsports,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    AdminKpiCard(
                        title = "Voice Practice Hours",
                        value = "3,240h",
                        tag = "Total Hours",
                        growth = "+12% vs last month",
                        color = Color(0xFFFB7185),
                        watermarkIcon = Icons.Rounded.Mic,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Timer,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // Section 2: Executive Rapid Operations (Matching Screenshot 4-card gradient row)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🚀", fontSize = 18.sp)
                    Text(
                        "Executive Rapid Operations",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Card 1: Add Student (Purple-violet gradient)
                    RapidOperationCard(
                        title = "Add\nStudent",
                        icon = Icons.Rounded.GroupAdd,
                        gradient = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
                        borderColor = Color(0xFFC084FC),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectAdminTab(AdminTab.STUDENTS) }
                    )

                    // Card 2: Manage Faculty (Ocean-blue gradient)
                    RapidOperationCard(
                        title = "Manage\nFaculty",
                        icon = Icons.Rounded.CoPresent,
                        gradient = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
                        borderColor = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectAdminTab(AdminTab.TEACHERS) }
                    )

                    // Card 3: Create Challenge (Emerald-green gradient)
                    RapidOperationCard(
                        title = "Create\nChallenge",
                        icon = Icons.Rounded.GridView,
                        gradient = listOf(Color(0xFF10B981), Color(0xFF047857)),
                        borderColor = Color(0xFF34D399),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectAdminTab(AdminTab.GAMES) }
                    )

                    // Card 4: View Reports (Warm amber-gold gradient)
                    RapidOperationCard(
                        title = "View\nReports",
                        icon = Icons.Rounded.TrendingUp,
                        gradient = listOf(Color(0xFFF59E0B), Color(0xFFD97706)),
                        borderColor = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.selectAdminTab(AdminTab.CURRICULUM) }
                    )
                }

                // Secondary Rapid Actions (Preserving existing actions)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                    border = BorderStroke(1.dp, CyberCardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                            Text(
                                "👩‍🏫 Switch to Teacher Panel (Direct Access • No PIN)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
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
                colors = CardDefaults.cardColors(containerColor = CyberCardBg, contentColor = Color.White),
                border = BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
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
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                "Author questions and immediately deploy to student arcade",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1),
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
                        tag = item.category,
                        createdBy = item.createdBy,
                        onPlayAudio = { viewModel.speakAudio(item.fullWord) },
                        onEdit = {
                            activeEditingChallenge = ActiveEditingChallenge(
                                category = GameCategoryFilter.MISSING_LETTERS,
                                id = item.id,
                                field1 = item.fullWord,
                                field2 = item.hint,
                                field3 = item.category,
                                onSave = { f1, f2, f3, _, _ ->
                                    viewModel.updateMissingLettersChallenge(item.copy(fullWord = f1.uppercase(), hint = f2, category = f3.ifBlank { "General" }))
                                    statusMsg = "Dean Arthur Pendelton updated '${f1.uppercase()}' in category '${f3.ifBlank { "General" }}'!"
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
                                fontSize = 10.5.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
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
fun GoldenAcademicShieldEmblem(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFBBF24),
                        Color(0xFFD97706),
                        Color(0xFF92400E)
                    )
                )
            )
            .border(2.5.dp, Color(0xFFFDE68A), CircleShape)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // Inner golden ring
            drawCircle(
                color = Color(0xFFFDE68A).copy(alpha = 0.5f),
                radius = w * 0.44f,
                style = Stroke(width = 1.5f)
            )

            // Shield Path
            val shieldW = w * 0.58f
            val shieldH = h * 0.65f
            val sx = (w - shieldW) / 2f
            val sy = (h - shieldH) / 2f + 2f

            val shieldPath = Path().apply {
                moveTo(sx, sy)
                lineTo(sx + shieldW, sy)
                lineTo(sx + shieldW, sy + shieldH * 0.55f)
                cubicTo(
                    sx + shieldW, sy + shieldH * 0.85f,
                    cx, sy + shieldH,
                    cx, sy + shieldH
                )
                cubicTo(
                    cx, sy + shieldH,
                    sx, sy + shieldH * 0.85f,
                    sx, sy + shieldH * 0.55f
                )
                close()
            }

            // Fill Shield with Royal Blue Gradient
            drawPath(
                path = shieldPath,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF2563EB), Color(0xFF1E3A8A)),
                    startY = sy,
                    endY = sy + shieldH
                )
            )

            // Shield Golden Border
            drawPath(
                path = shieldPath,
                color = Color(0xFFFDE68A),
                style = Stroke(width = 2f)
            )

            // Laurel dots along outer rim
            val laurelColor = Color(0xFFFEF08A)
            for (angle in listOf(-140f, -115f, -90f, -65f, -40f, 40f, 65f, 90f, 115f, 140f)) {
                val rad = Math.toRadians(angle.toDouble())
                val r = w * 0.40f
                val lx = cx + (r * Math.cos(rad)).toFloat()
                val ly = cy + (r * Math.sin(rad)).toFloat()
                drawCircle(
                    color = laurelColor,
                    radius = 2.2f,
                    center = Offset(lx, ly)
                )
            }
        }

        // Open book icon inside the shield
        Icon(
            imageVector = Icons.Rounded.MenuBook,
            contentDescription = null,
            tint = Color(0xFFF0F9FF),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun UniversityBuildingIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val shrubColorDark = Color(0xFF059669)
        val shrubColorLight = Color(0xFF10B981)
        val buildingBaseColor = Color(0xFF818CF8)
        val buildingLightColor = Color(0xFFE0E7FF)
        val columnHighlight = Color(0xFFFFFFFF)
        val columnShadow = Color(0xFF6366F1)
        val roofColor = Color(0xFFC7D2FE)
        val pedimentBorder = Color(0xFFE0E7FF)
        val flagPoleColor = Color(0xFFE2E8F0)

        // 1. Shrubbery mounds at left and right base
        drawCircle(
            color = shrubColorDark,
            radius = w * 0.12f,
            center = Offset(w * 0.12f, h * 0.82f)
        )
        drawCircle(
            color = shrubColorLight,
            radius = w * 0.09f,
            center = Offset(w * 0.18f, h * 0.85f)
        )
        drawCircle(
            color = shrubColorDark,
            radius = w * 0.12f,
            center = Offset(w * 0.88f, h * 0.82f)
        )
        drawCircle(
            color = shrubColorLight,
            radius = w * 0.09f,
            center = Offset(w * 0.82f, h * 0.85f)
        )

        // 2. Front stairs (3 tiers)
        val stepBottom = h * 0.88f
        val stepH = h * 0.035f
        drawRoundRect(
            color = buildingBaseColor.copy(alpha = 0.8f),
            topLeft = Offset(w * 0.10f, stepBottom),
            size = Size(w * 0.80f, stepH),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = buildingLightColor.copy(alpha = 0.9f),
            topLeft = Offset(w * 0.14f, stepBottom - stepH),
            size = Size(w * 0.72f, stepH),
            cornerRadius = CornerRadius(2f, 2f)
        )
        drawRoundRect(
            color = columnHighlight,
            topLeft = Offset(w * 0.18f, stepBottom - stepH * 2),
            size = Size(w * 0.64f, stepH),
            cornerRadius = CornerRadius(2f, 2f)
        )

        // 3. Portico back wall & entrance arch
        val porticoTop = h * 0.45f
        val porticoBottom = stepBottom - stepH * 2
        val porticoHeight = porticoBottom - porticoTop
        drawRect(
            color = Color(0xFF1E1B4B).copy(alpha = 0.75f),
            topLeft = Offset(w * 0.20f, porticoTop),
            size = Size(w * 0.60f, porticoHeight)
        )
        val doorW = w * 0.16f
        val doorH = porticoHeight * 0.65f
        val doorX = (w - doorW) / 2f
        val doorY = porticoBottom - doorH
        drawRoundRect(
            color = Color(0xFF0F172A),
            topLeft = Offset(doorX, doorY),
            size = Size(doorW, doorH),
            cornerRadius = CornerRadius(doorW / 2, doorW / 2)
        )

        // 4. Columns (4 classical pillars)
        val numColumns = 4
        val colWidth = w * 0.055f
        val colLeft = w * 0.22f
        val colRight = w * 0.78f - colWidth
        val colSpacing = (colRight - colLeft) / (numColumns - 1)

        for (i in 0 until numColumns) {
            val cx = colLeft + i * colSpacing
            drawRect(
                color = columnHighlight,
                topLeft = Offset(cx - 2f, porticoBottom - 4f),
                size = Size(colWidth + 4f, 4f)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(columnHighlight, buildingLightColor, columnShadow.copy(alpha = 0.7f)),
                    startX = cx,
                    endX = cx + colWidth
                ),
                topLeft = Offset(cx, porticoTop + 5f),
                size = Size(colWidth, porticoHeight - 9f)
            )
            drawRect(
                color = columnHighlight,
                topLeft = Offset(cx - 3f, porticoTop),
                size = Size(colWidth + 6f, 5f)
            )
        }

        // 5. Entablature (horizontal beam above columns)
        val entablatureTop = porticoTop - h * 0.055f
        val entablatureHeight = h * 0.055f
        drawRect(
            color = buildingLightColor,
            topLeft = Offset(w * 0.16f, entablatureTop),
            size = Size(w * 0.68f, entablatureHeight)
        )
        drawLine(
            color = columnShadow,
            start = Offset(w * 0.16f, entablatureTop + entablatureHeight),
            end = Offset(w * 0.84f, entablatureTop + entablatureHeight),
            strokeWidth = 2f
        )

        // 6. Triangular Pediment
        val pedimentTop = h * 0.22f
        val pedimentPath = Path().apply {
            moveTo(w * 0.50f, pedimentTop)
            lineTo(w * 0.86f, entablatureTop)
            lineTo(w * 0.14f, entablatureTop)
            close()
        }
        drawPath(
            path = pedimentPath,
            brush = Brush.verticalGradient(
                colors = listOf(buildingLightColor, roofColor),
                startY = pedimentTop,
                endY = entablatureTop
            )
        )
        drawPath(
            path = pedimentPath,
            color = pedimentBorder,
            style = Stroke(width = 2.5f)
        )

        // 7. Dome / cupola & Flagpole on roof
        val domeW = w * 0.14f
        val domeH = h * 0.08f
        val domeX = (w - domeW) / 2f
        val domeY = pedimentTop - domeH * 0.75f
        drawArc(
            color = columnHighlight,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = true,
            topLeft = Offset(domeX, domeY),
            size = Size(domeW, domeH)
        )

        val poleX = w * 0.50f
        val poleTop = h * 0.04f
        drawLine(
            color = flagPoleColor,
            start = Offset(poleX, domeY),
            end = Offset(poleX, poleTop),
            strokeWidth = 2.5f
        )

        // Waving Flag
        val flagPath = Path().apply {
            moveTo(poleX, poleTop)
            cubicTo(
                poleX + w * 0.06f, poleTop - 3f,
                poleX + w * 0.09f, poleTop + 6f,
                poleX + w * 0.14f, poleTop + 2f
            )
            lineTo(poleX + w * 0.14f, poleTop + h * 0.055f)
            cubicTo(
                poleX + w * 0.09f, poleTop + h * 0.055f + 4f,
                poleX + w * 0.06f, poleTop + h * 0.055f - 3f,
                poleX, poleTop + h * 0.055f
            )
            close()
        }
        drawPath(
            path = flagPath,
            color = Color(0xFFF8FAFC)
        )
    }
}

@Composable
fun KpiCardFluidWave(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val wavePath = Path().apply {
            moveTo(0f, h)
            lineTo(0f, h * 0.70f)
            cubicTo(
                w * 0.35f, h * 0.90f,
                w * 0.60f, h * 0.35f,
                w, h * 0.50f
            )
            lineTo(w, h)
            close()
        }

        drawPath(
            path = wavePath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    color.copy(alpha = 0.45f),
                    color.copy(alpha = 0.10f)
                ),
                startY = h * 0.3f,
                endY = h
            )
        )
    }
}

@Composable
fun AdminKpiCard(
    title: String,
    value: String,
    tag: String,
    growth: String,
    color: Color,
    watermarkIcon: ImageVector,
    modifier: Modifier = Modifier,
    tagIcon: ImageVector? = null,
    iconContent: @Composable () -> Unit
) {
    val cardBg = when (color) {
        Color(0xFF38BDF8) -> listOf(Color(0xFF071A3C), Color(0xFF030D20))
        Color(0xFFA855F7) -> listOf(Color(0xFF1E0A3C), Color(0xFF100524))
        Color(0xFF10B981) -> listOf(Color(0xFF032619), Color(0xFF02160E))
        Color(0xFFF59E0B) -> listOf(Color(0xFF2E1303), Color(0xFF180A02))
        Color(0xFF06B6D4) -> listOf(Color(0xFF051C30), Color(0xFF020E1A))
        Color(0xFFFB7185) -> listOf(Color(0xFF2B0517), Color(0xFF16020C))
        else -> listOf(Color(0xFF0F1535), Color(0xFF070B1F))
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.75f)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(cardBg))
        ) {
            // Fluid wave at bottom
            KpiCardFluidWave(
                color = color,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .align(Alignment.BottomCenter)
            )

            // Subtle watermark icon in bottom-right corner
            Icon(
                imageVector = watermarkIcon,
                contentDescription = null,
                tint = color.copy(alpha = 0.28f),
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-12).dp, y = (-10).dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header Row: Left Icon Circle + Right Tag Pill (Full space for pill on right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(color, color.copy(alpha = 0.7f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        iconContent()
                    }

                    // Tag Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF070B1F).copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.70f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            if (tagIcon != null) {
                                Icon(
                                    imageVector = tagIcon,
                                    contentDescription = null,
                                    tint = color,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                            Text(
                                text = tag,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = color,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Middle Content: Title (full card width!) + Value (full card width!)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = title,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = value,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                // Bottom row: growth indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = null,
                        tint = Color(0xFF4ADE80),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = growth,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF4ADE80),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun RapidOperationCard(
    title: String,
    icon: ImageVector,
    gradient: List<Color>,
    borderColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, borderColor.copy(alpha = 0.85f)),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradient))
                .padding(vertical = 14.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
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
