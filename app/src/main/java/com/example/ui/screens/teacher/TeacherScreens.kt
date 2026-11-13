package com.example.ui.screens.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.util.CertificatePdfGenerator
import com.example.ui.components.SpeaklyCertificatePreviewDialog
import com.example.util.ExcelWordSheetParser
import com.example.util.ParsedMissingLetterRow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.components.SectionHeader
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.TeacherSpeakingSection
import com.example.ui.TeacherTab
import com.example.ui.screens.common.SpeakingAndPhoneticsManagement
import com.example.ui.screens.common.SpeakingManageTab
import com.example.ui.theme.*
import com.example.util.ExamTimeFormatter
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun TeacherMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTeacherTab.collectAsState()
    val showAiKeyDialog by viewModel.showAiKeyDialog.collectAsState()

    if (showAiKeyDialog) {
        com.example.ui.components.AiApiKeyIntegrationDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeAiKeyDialog() }
        )
    }

    when (currentTab) {
        TeacherTab.DASHBOARD -> TeacherDashboard(viewModel, modifier)
        TeacherTab.STUDENTS -> TeacherStudentsManagement(viewModel, modifier)
        TeacherTab.GAMES -> TeacherGameCreator(viewModel, modifier)
        TeacherTab.SPEAKING_REVIEW -> TeacherSpeakingReview(viewModel, modifier)
        TeacherTab.EXAMS -> TeacherExamsManagementScreen(viewModel, modifier)
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
    val isAdminSession by viewModel.isAdminSession.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("teacher_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Admin Session Banner if viewing as Teacher
        if (isAdminSession) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF0F172A),
                    border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("teacher_admin_preview_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("👑", fontSize = 18.sp)
                            Column {
                                Text(
                                    "Admin Session Active",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFDE68A)
                                )
                                Text(
                                    "You are viewing Teacher Portal • Direct return without PIN",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                        Button(
                            onClick = { viewModel.returnToAdminPanel() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF59E0B),
                                contentColor = Color(0xFF0F172A)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("teacher_dashboard_back_to_admin_button")
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Back to Admin", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Teacher Banner Card (Exact match to screenshot)
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
                        TeacherPortraitAvatar(
                            modifier = Modifier.size(68.dp)
                        )

                        Spacer(Modifier.width(10.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = currentUser.name.ifBlank { "Prof. Eleanor Vance" },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            val departmentOrBatch = currentUser.batch.ifBlank { "Class 10 & 9" }
                            Text(
                                text = "Faculty Instructor • $departmentOrBatch",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFA5B4FC),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(3.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF04261B).copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.5.dp),
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
                                        text = "Faculty Active Today",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        TeacherGraduationBooksIllustration(
                            modifier = Modifier.size(width = 88.dp, height = 76.dp)
                        )
                    }
                }
            }
        }

        // Compact Premium AI Speech & Cloud Connection Card for Faculty
        item {
            val isGeminiConfigured by viewModel.isGeminiConfigured.collectAsState()
            val apiKeySource by viewModel.apiKeySource.collectAsState()
            val activeModel by viewModel.activeAiModel.collectAsState()

            TeacherAiConnectionCard(
                isConfigured = isGeminiConfigured,
                activeModel = activeModel,
                apiKeySource = apiKeySource,
                onClick = { viewModel.openAiKeyDialog() }
            )
        }

        // Section: Classroom Overview & Metrics (Matching Screenshot 2x2 Grid)
        item {

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        text = "Classroom Overview & Metrics",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }

                // Row 1: Enrolled Students & Pending Voice
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TeacherMetricCard(
                        title = "Enrolled Students",
                        value = "${students.size.coerceAtLeast(6)}",
                        icon = Icons.Rounded.Groups,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f),
                        tag = "Active Cohort",
                        growth = "+0% vs last week",
                        watermarkIcon = Icons.Rounded.Groups
                    )
                    TeacherMetricCard(
                        title = "Pending Voice",
                        value = "${submissions.count { it.status == "PENDING" }.coerceAtLeast(1)}",
                        icon = Icons.Rounded.Headphones,
                        color = Color(0xFFF59E0B),
                        modifier = Modifier.weight(1f),
                        tag = "Needs Review",
                        growth = "-50% vs last week",
                        watermarkIcon = Icons.Rounded.Mic
                    )
                }

                // Row 2: Avg Speaking Fluency & Active Practice Drills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TeacherMetricCard(
                        title = "Avg Speaking Fluency",
                        value = "84.2%",
                        icon = Icons.Rounded.Mic,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f),
                        tag = "+3.2% This Wk",
                        growth = "+3.2% vs last week",
                        watermarkIcon = Icons.Rounded.TrendingUp
                    )
                    TeacherMetricCard(
                        title = "Active Practice Drills",
                        value = "${totalGames.coerceAtLeast(25)}",
                        icon = Icons.Rounded.SportsEsports,
                        color = Color(0xFFA855F7),
                        modifier = Modifier.weight(1f),
                        tag = "12 Categories",
                        growth = "+25% vs last week",
                        watermarkIcon = Icons.Rounded.SportsEsports
                    )
                }
            }
        }

        // Section: Faculty Quick Operations (Matching Screenshot 2-card row)
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🚀", fontSize = 18.sp)
                Text(
                    text = "Faculty Quick Operations",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TeacherQuickOperationCard(
                    title = "Manage Students",
                    subtitle = "Roster & CEFR Levels",
                    icon = Icons.Rounded.ManageAccounts,
                    color = Color(0xFF00D2FF),
                    gradient = listOf(Color(0xFF0C2454), Color(0xFF061430)),
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.selectTeacherTab(TeacherTab.STUDENTS) }
                )
                TeacherQuickOperationCard(
                    title = "Add Game Challenge",
                    subtitle = "Word match, sentence, etc.",
                    icon = Icons.Rounded.SportsEsports,
                    color = Color(0xFFA855F7),
                    gradient = listOf(Color(0xFF2E0854), Color(0xFF17042C)),
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

        // Quick Access: English & Assamese Vocabulary Words Management
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, AmberGold.copy(alpha = 0.6f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        viewModel.selectTeacherSpeakingSection(TeacherSpeakingSection.MANAGE_SPEAKING)
                        viewModel.selectTeacherTab(TeacherTab.SPEAKING_REVIEW)
                    }
                    .testTag("teacher_assamese_vocab_shortcut_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AmberGold.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📖", fontSize = 24.sp)
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "English & Assamese Vocabulary",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Add words with Assamese meanings, IPA & syllables so students can search and practice them.",
                                fontSize = 11.sp,
                                color = MutedGray
                            )
                        }
                    }

                    Button(
                        onClick = {
                            viewModel.selectTeacherSpeakingSection(TeacherSpeakingSection.MANAGE_SPEAKING)
                            viewModel.selectTeacherTab(TeacherTab.SPEAKING_REVIEW)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Word", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    }
                }
            }
        }

        // Action Quick Access for Speaking Review
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.45f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                        Text("Review Spoken Submissions", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Text("${submissions.count { it.status == "PENDING" }} voice audio recordings waiting for grading", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = RoyalBlue)
                }
            }
        }

        // Weak Areas Alert Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.2.dp, AmberGold.copy(alpha = 0.45f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("⚠️ Cohort Attention & Phoneme Alerts", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                    WeakAreaRow("Pronunciation: Voiced 'th' sounds (this, weather)", "4 students need drill")
                    WeakAreaRow("Grammar: Irregular past tense agreement", "3 students need review")
                    WeakAreaRow("Fluency: Response hesitation in impromptu talks", "Avg pause: 3.2s")
                }
            }
        }

        item {
            Spacer(Modifier.height(56.dp))
        }
    }
}

// -------------------------------------------------------------
// TEACHER STUDENTS MANAGEMENT (COMPREHENSIVE)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TeacherStudentsManagement(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val students by viewModel.adminStudents.collectAsState()
    val academicClasses by viewModel.academicClasses.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedBatchFilter by remember { mutableStateOf("All") }
    var selectedStudentForDetail by remember { mutableStateOf<StudentManagementInfo?>(null) }
    var actionToast by remember { mutableStateOf<String?>(null) }

    // Dialog State Controllers
    var showManageClassesDialog by remember { mutableStateOf(false) }
    var showCreateStudentDialog by remember { mutableStateOf(false) }

    val filteredStudents = remember(students, searchQuery, selectedBatchFilter) {
        students.filter { student ->
            val matchesQuery = student.name.contains(searchQuery, ignoreCase = true) ||
                    student.email.contains(searchQuery, ignoreCase = true) ||
                    student.batch.contains(searchQuery, ignoreCase = true)
            val matchesBatch = when (selectedBatchFilter) {
                "All" -> true
                "Needs Help (<80%)" -> student.speakingScore < 80
                "Top Fluency (≥85%)" -> student.speakingScore >= 85
                else -> student.batch.equals(selectedBatchFilter, ignoreCase = true)
            }
            matchesQuery && matchesBatch
        }
    }

    var activeStudentSubTab by remember { mutableStateOf("ROSTER") }

    Column(modifier = modifier.fillMaxSize()) {
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
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (activeStudentSubTab == "ROSTER") RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f).clickable { activeStudentSubTab = "ROSTER" }
                ) {
                    Text(
                        "👥 Cohort Roster",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (activeStudentSubTab == "ADVENTURE") Color(0xFF7C3AED) else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.weight(1f).clickable { activeStudentSubTab = "ADVENTURE" }
                ) {
                    Text(
                        "🗺️ Adventure Progress",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        if (activeStudentSubTab == "ADVENTURE") {
            com.example.ui.screens.common.TeacherAdventureProgressTracker(
                viewModel = viewModel,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
                    .testTag("teacher_students_screen"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Assigned Students Cohort", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.White)
                        Text("Manage student levels, class groups, and enrollments", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    }
                }

                // Action Controls: Manage Classes (Add/Delete) & Create New Student Account
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { showManageClassesDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, RoyalBlue.copy(alpha = 0.8f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF0F1A3E),
                            contentColor = Color(0xFF60A5FA)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("teacher_manage_classes_btn"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Rounded.Class, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF60A5FA))
                        Spacer(Modifier.width(6.dp))
                        Text("Manage Classes", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                    }

                    Button(
                        onClick = { showCreateStudentDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("teacher_create_student_btn"),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Create Account", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, email, or class...", fontSize = 13.sp) },
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

                // Dynamic Filter Pills (using dynamic academicClasses)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    val filters = listOf("All") + academicClasses + listOf("Needs Help (<80%)", "Top Fluency (≥85%)")
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
                    val teacher = viewModel.currentUser.value.name.ifBlank { "Faculty" }
                    viewModel.createAssignment(
                        title = "Target Pronunciation Drill - ${student.name}",
                        instructions = "Complete 15-minute voice challenge and pronunciation assessment assigned by $teacher.",
                        targetClass = student.batch.ifBlank { student.studentClass.ifBlank { "All Classes" } },
                        dueDate = "In 3 Days",
                        topicType = "Speaking Drill"
                    )
                    actionToast = "Assigned 15-minute voice challenge to ${student.name}!"
                    viewModel.speakAudio("Target pronunciation drill assigned to ${student.name}")
                }
            )
        }
    }
        }
    }

    // Dialog 1: Manage Academic Classes (Add Class & Delete Class)
    if (showManageClassesDialog) {
        ManageClassesDialog(
            classes = academicClasses,
            students = students,
            onAddClass = { newClass ->
                val success = viewModel.addClass(newClass)
                if (success) {
                    actionToast = "Added new class: $newClass"
                } else {
                    actionToast = "Class '$newClass' already exists or is empty."
                }
            },
            onDeleteClass = { classToDelete ->
                val success = viewModel.deleteClass(classToDelete)
                if (success) {
                    actionToast = "Deleted class '$classToDelete'"
                    if (selectedBatchFilter == classToDelete) {
                        selectedBatchFilter = "All"
                    }
                }
            },
            onDismiss = { showManageClassesDialog = false }
        )
    }

    // Dialog 2: Create Student Account with Drop-down Class Option
    if (showCreateStudentDialog) {
        TeacherCreateStudentAccountDialog(
            classes = academicClasses,
            onDismiss = { showCreateStudentDialog = false },
            onCreate = { name, email, selectedClass, level, pin, notes ->
                viewModel.addStudent(
                    name = name,
                    email = email,
                    batch = selectedClass,
                    level = level,
                    notes = notes
                )
                actionToast = "Created student account for $name in $selectedClass!"
                showCreateStudentDialog = false
            }
        )
    }

    // Comprehensive Student Detail & Management Dialog
    selectedStudentForDetail?.let { student ->
        StudentDetailDialog(
            student = student,
            classes = academicClasses,
            onDismiss = { selectedStudentForDetail = null },
            onSave = { updated ->
                viewModel.updateStudent(updated)
                selectedStudentForDetail = null
                actionToast = "Updated profile & level for ${updated.name}!"
            }
        )
    }
}

// -------------------------------------------------------------
// MANAGE CLASSES DIALOG (ADD & DELETE CLASS)
// -------------------------------------------------------------
@Composable
fun ManageClassesDialog(
    classes: List<String>,
    students: List<StudentManagementInfo>,
    onAddClass: (String) -> Unit,
    onDeleteClass: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var newClassName by remember { mutableStateOf("") }
    var classToDeleteConfirmation by remember { mutableStateOf<String?>(null) }
    var addError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Class, contentDescription = null, tint = RoyalBlue)
                Text("Manage Academic Classes", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Add new classes or remove existing cohorts. Changes will instantly reflect across student registration, assignment targets, and class filters.",
                    fontSize = 12.sp,
                    color = MutedGray,
                    lineHeight = 16.sp
                )

                // Add Class Input Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.AddCircle, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                            Text("Add New Class", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newClassName,
                                onValueChange = {
                                    newClassName = it
                                    addError = null
                                },
                                placeholder = { Text("e.g. Class 5 or Spoken Batch A", fontSize = 12.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("add_class_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Button(
                                onClick = {
                                    val trimmed = newClassName.trim()
                                    if (trimmed.isBlank()) {
                                        addError = "Please enter a class name"
                                    } else if (classes.any { it.equals(trimmed, ignoreCase = true) }) {
                                        addError = "Class '$trimmed' already exists"
                                    } else {
                                        onAddClass(trimmed)
                                        newClassName = ""
                                        addError = null
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("submit_add_class_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                            ) {
                                Text("Add", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (addError != null) {
                            Text(addError!!, color = CoralAccent, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // Existing Classes List
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Existing Classes (${classes.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    if (classes.isEmpty()) {
                        Text("No classes created yet. Add your first class above.", fontSize = 12.sp, color = MutedGray)
                    } else {
                        classes.forEach { cls ->
                            val count = students.count { it.batch.equals(cls, ignoreCase = true) }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Rounded.School, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                                        Text(cls, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFEFF6FF)
                                        ) {
                                            Text(
                                                "$count students",
                                                fontSize = 11.sp,
                                                color = RoyalBlue,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { classToDeleteConfirmation = cls },
                                        modifier = Modifier.testTag("delete_class_${cls.lowercase().replace(" ", "_")}")
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete Class $cls",
                                            tint = CoralAccent,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )

    // Delete Confirmation Sub-Dialog
    classToDeleteConfirmation?.let { cls ->
        val count = students.count { it.batch.equals(cls, ignoreCase = true) }
        AlertDialog(
            onDismissRequest = { classToDeleteConfirmation = null },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = CoralAccent) },
            title = { Text("Delete $cls?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    if (count > 0)
                        "Are you sure you want to delete '$cls'? There are currently $count students enrolled in this class. They will remain in the system, but this class option will be deleted."
                    else
                        "Are you sure you want to delete '$cls'?",
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClass(cls)
                        classToDeleteConfirmation = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralAccent),
                    modifier = Modifier.testTag("confirm_delete_class_btn")
                ) {
                    Text("Delete Class", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { classToDeleteConfirmation = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// TEACHER CREATE STUDENT ACCOUNT DIALOG (WITH CLASS DROPDOWN)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherCreateStudentAccountDialog(
    classes: List<String>,
    onDismiss: () -> Unit,
    onCreate: (name: String, email: String, selectedClass: String, level: LearningLevel, pin: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedClass by remember(classes) {
        mutableStateOf(classes.firstOrNull() ?: AcademicClasses.DEFAULT_CLASS)
    }
    var classDropdownExpanded by remember { mutableStateOf(false) }
    var selectedLevel by remember { mutableStateOf(LearningLevel.BEGINNER) }
    var securityPin by remember { mutableStateOf("1234") }
    var notes by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = RoyalBlue)
                Text("Create Student Account", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Enroll a student into your cohort. Select their academic class from the dropdown menu and set initial CEFR proficiency.",
                    fontSize = 12.sp,
                    color = MutedGray,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        errorMessage = null
                    },
                    label = { Text("Full Name") },
                    placeholder = { Text("e.g. Maya Sharma") },
                    leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = RoyalBlue) },
                    modifier = Modifier.fillMaxWidth().testTag("teacher_new_student_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        errorMessage = null
                    },
                    label = { Text("Email Address") },
                    placeholder = { Text("e.g. maya.s@linguaquest.edu") },
                    leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = RoyalBlue) },
                    modifier = Modifier.fillMaxWidth().testTag("teacher_new_student_email"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                // Dropdown Option to Select Class
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Select Academic Class", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    ExposedDropdownMenuBox(
                        expanded = classDropdownExpanded,
                        onExpandedChange = { classDropdownExpanded = !classDropdownExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("teacher_class_dropdown_box")
                    ) {
                        OutlinedTextField(
                            value = selectedClass,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assigned Class") },
                            leadingIcon = { Icon(Icons.Rounded.School, contentDescription = null, tint = RoyalBlue) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("teacher_class_dropdown_field"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                        )

                        ExposedDropdownMenu(
                            expanded = classDropdownExpanded,
                            onDismissRequest = { classDropdownExpanded = false }
                        ) {
                            classes.forEach { cls ->
                                val isSelected = selectedClass == cls
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            cls,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.onSurface
                                        )
                                    },
                                    leadingIcon = {
                                        if (isSelected) {
                                            Icon(Icons.Filled.Check, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                                        }
                                    },
                                    onClick = {
                                        selectedClass = cls
                                        classDropdownExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }
                }

                // Learning Level Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Starting Learning Level", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LearningLevel.values()) { lvl ->
                            FilterChip(
                                selected = selectedLevel == lvl,
                                onClick = { selectedLevel = lvl },
                                label = { Text("${lvl.cefr} ${lvl.displayName}", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = securityPin,
                    onValueChange = {
                        securityPin = it
                        errorMessage = null
                    },
                    label = { Text("Security PIN (4-6 digits)") },
                    placeholder = { Text("Default 1234") },
                    leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = RoyalBlue) },
                    modifier = Modifier.fillMaxWidth().testTag("teacher_new_student_pin"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Teacher Notes (Optional)") },
                    placeholder = { Text("e.g. Focus on pronunciation...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, color = CoralAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || email.isBlank()) {
                        errorMessage = "Student name and email are required."
                    } else if (securityPin.trim().length < 4) {
                        errorMessage = "Security PIN must be at least 4 digits."
                    } else {
                        onCreate(name.trim(), email.trim(), selectedClass, selectedLevel, securityPin.trim(), notes.trim())
                    }
                },
                modifier = Modifier.testTag("submit_create_student_account_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
            ) {
                Text("Create Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TeacherStudentCard(
    student: StudentManagementInfo,
    onManageClick: () -> Unit,
    onAssignDrill: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                        Text(student.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.White)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = RoyalBlue
                        ) {
                            Text(student.level.cefr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    Text("${student.email} • Class: ${student.studentClass}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (student.isActive) Color(0xFF0F2E22) else Color(0xFF33151D),
                    border = BorderStroke(1.dp, if (student.isActive) EmeraldSuccess.copy(alpha = 0.5f) else CoralAccent.copy(alpha = 0.5f))
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
                Text("Speaking Score", fontSize = 12.sp, color = Color(0xFFCBD5E1))
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

            // Diagnostic Badges (Sleek dark glass tiles)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0C1638),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.30f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Pronunciation", fontSize = 9.sp, color = Color(0xFF94A3B8))
                        Text("${student.pronunciationScore}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0C1638),
                    border = BorderStroke(1.dp, Color(0xFF60A5FA).copy(alpha = 0.30f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Fluency", fontSize = 9.sp, color = Color(0xFF94A3B8))
                        Text("${student.fluencyScore}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0C1638),
                    border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.35f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Streak", fontSize = 9.sp, color = Color(0xFF94A3B8))
                        Text("🔥 ${student.streakDays}d", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                    }
                }
            }

            // Action Buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onAssignDrill,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.8f)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFF0E1A3C),
                        contentColor = Color(0xFF60A5FA)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF60A5FA))
                    Spacer(Modifier.width(4.dp))
                    Text("Assign Drill", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onManageClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text("Diagnostics & Edit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
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
    PICTURE_VOCAB("Picture Vocabulary", "🖼️"),
    GRAMMAR_CHALLENGE("Grammar Challenge", "⚡"),
    ENGLISH_ADVENTURE("English Adventure", "🗺️")
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
    var showBulkImportMissingLettersDialog by remember { mutableStateOf(false) }
    var showManageCategoriesDialog by remember { mutableStateOf(false) }
    var activeEditingChallenge by remember { mutableStateOf<ActiveEditingChallenge?>(null) }
    var statusMsg by remember { mutableStateOf<String?>(null) }
    val cloudNotice by viewModel.cloudSyncNotice.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.refreshGameChallengesFromCloud()
    }
    LaunchedEffect(cloudNotice) {
        cloudNotice?.let {
            statusMsg = it
            viewModel.clearCloudSyncNotice()
        }
    }
    var isSearchVisible by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val currentUser by viewModel.currentUser.collectAsState()

    val wordMatches by viewModel.wordMatchChallenges.collectAsState()
    val missingLetters by viewModel.missingLettersChallenges.collectAsState()
    val missingLettersCategories by viewModel.missingLettersCategories.collectAsState()
    var selectedMissingLettersCategory by remember { mutableStateOf("All") }
    val sentenceBuilders by viewModel.sentenceBuilderChallenges.collectAsState()
    val listenChooses by viewModel.listenChooseChallenges.collectAsState()
    val pronunciationBattles by viewModel.pronunciationBattleChallenges.collectAsState()
    val fastVocabs by viewModel.fastVocabularyChallenges.collectAsState()
    val pictureVocabs by viewModel.pictureVocabChallenges.collectAsState()
    val grammarChallenges by viewModel.grammarChallenges.collectAsState()

    val filteredWordMatches = remember(wordMatches, searchQuery) {
        if (searchQuery.isBlank()) wordMatches
        else wordMatches.filter { it.word.contains(searchQuery.trim(), ignoreCase = true) || it.definition.contains(searchQuery.trim(), ignoreCase = true) || it.category.contains(searchQuery.trim(), ignoreCase = true) }
    }
    val filteredMissingLetters = remember(missingLetters, searchQuery, selectedMissingLettersCategory) {
        missingLetters.filter { item ->
            val matchesCat = selectedMissingLettersCategory == "All" || item.category.equals(selectedMissingLettersCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() || (
                item.fullWord.contains(searchQuery.trim(), ignoreCase = true) ||
                item.hint.contains(searchQuery.trim(), ignoreCase = true) ||
                item.category.contains(searchQuery.trim(), ignoreCase = true)
            )
            matchesCat && matchesQuery
        }
    }
    val filteredSentenceBuilders = remember(sentenceBuilders, searchQuery) {
        if (searchQuery.isBlank()) sentenceBuilders
        else sentenceBuilders.filter { it.targetSentence.contains(searchQuery.trim(), ignoreCase = true) || it.hint.contains(searchQuery.trim(), ignoreCase = true) }
    }
    val filteredListenChooses = remember(listenChooses, searchQuery) {
        if (searchQuery.isBlank()) listenChooses
        else listenChooses.filter { it.question.contains(searchQuery.trim(), ignoreCase = true) || it.audioScript.contains(searchQuery.trim(), ignoreCase = true) }
    }
    val filteredPronunciationBattles = remember(pronunciationBattles, searchQuery) {
        if (searchQuery.isBlank()) pronunciationBattles
        else pronunciationBattles.filter { it.challengeSentence.contains(searchQuery.trim(), ignoreCase = true) || it.targetPhonemeFocus.contains(searchQuery.trim(), ignoreCase = true) }
    }
    val filteredFastVocabs = remember(fastVocabs, searchQuery) {
        if (searchQuery.isBlank()) fastVocabs
        else fastVocabs.filter { it.word.contains(searchQuery.trim(), ignoreCase = true) || it.prompt.contains(searchQuery.trim(), ignoreCase = true) }
    }
    val filteredPictureVocabs = remember(pictureVocabs, searchQuery) {
        if (searchQuery.isBlank()) pictureVocabs
        else pictureVocabs.filter {
            it.word.contains(searchQuery.trim(), ignoreCase = true) ||
            it.clue.contains(searchQuery.trim(), ignoreCase = true) ||
            it.phonetic.contains(searchQuery.trim(), ignoreCase = true)
        }
    }
    val filteredGrammarChallenges = remember(grammarChallenges, searchQuery) {
        if (searchQuery.isBlank()) grammarChallenges
        else grammarChallenges.filter { it.question.contains(searchQuery.trim(), ignoreCase = true) || it.explanation.contains(searchQuery.trim(), ignoreCase = true) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF080D26),
                        Color(0xFF090E2A),
                        Color(0xFF060919)
                    )
                )
            )
    ) {
        // Ambient neon glow top left
        Box(
            modifier = Modifier
                .size(240.dp)
                .offset(x = (-80).dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFD946EF).copy(alpha = 0.25f), Color.Transparent)
                    )
                )
        )
        // Ambient neon glow top right
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.TopEnd)
                .offset(x = 80.dp, y = 40.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.2f), Color.Transparent)
                    )
                )
        )
        // Ambient neon glow bottom
        Box(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF00D2FF).copy(alpha = 0.15f), Color.Transparent)
                    )
                )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("teacher_game_creator_screen"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        // 1. Hero Banner: Game & Challenge Creator with cyber wave and neon border
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F24).copy(alpha = 0.92f)),
                border = BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFEC4899),
                            Color(0xFF8B5CF6),
                            Color(0xFF00E5FF)
                        )
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // Fluid wave accent at bottom
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(55.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        KpiCardFluidWave(color = Color(0xFF8B5CF6))
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Glowing 3D Gamepad badge
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF9333EA), Color(0xFF6366F1), Color(0xFF3B82F6))
                                        )
                                    )
                                    .border(1.dp, Color(0xFFD8B4FE).copy(alpha = 0.7f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🎮", fontSize = 22.sp)
                            }

                            // Vertical divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(34.dp)
                                    .background(Color(0xFF334155).copy(alpha = 0.8f))
                            )

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    "Game & Challenge Creator",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White,
                                    maxLines = 1
                                )
                                Text(
                                    "Publish & edit interactive learning",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8),
                                    maxLines = 1
                                )
                            }
                        }

                        // Electric cyan "+ New" button
                        Button(
                            onClick = { showCreateDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                            modifier = Modifier
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF00D2FF), Color(0xFF0072FF))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .border(1.dp, Color(0xFF7DD3FC).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "New",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 2. Horizontal Category Tabs Carousel
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(GameCategoryFilter.values()) { cat ->
                    val isSel = selectedCategory == cat
                    val tabShape = RoundedCornerShape(16.dp)

                    Box(
                        modifier = Modifier
                            .clip(tabShape)
                            .then(
                                if (isSel) {
                                    Modifier
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFF00D2FF), Color(0xFF0088FF))
                                            )
                                        )
                                        .border(1.dp, Color(0xFF7DD3FC).copy(alpha = 0.8f), tabShape)
                                } else {
                                    Modifier
                                        .background(Color(0xFF0C1326).copy(alpha = 0.85f))
                                        .border(1.dp, Color(0xFF1E293B), tabShape)
                                }
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            when (cat) {
                                GameCategoryFilter.WORD_MATCH -> {
                                    Text("🧩", fontSize = 15.sp)
                                }
                                GameCategoryFilter.MISSING_LETTERS -> {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) Color.White.copy(alpha = 0.25f) else Color(0xFF1E3A5F)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "abc",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSel) Color.White else Color(0xFF38BDF8)
                                        )
                                    }
                                }
                                GameCategoryFilter.SENTENCE_BUILDER -> {
                                    Text("🏗️", fontSize = 15.sp)
                                }
                                GameCategoryFilter.LISTEN_CHOOSE -> {
                                    Text("🎧", fontSize = 15.sp)
                                }
                                GameCategoryFilter.PRONUNCIATION_BATTLE -> {
                                    Text("🎙️", fontSize = 15.sp)
                                }
                                GameCategoryFilter.FAST_VOCABULARY -> {
                                    Text("⚡", fontSize = 15.sp)
                                }
                                GameCategoryFilter.PICTURE_VOCAB -> {
                                    Text("🖼️", fontSize = 15.sp)
                                }
                                GameCategoryFilter.GRAMMAR_CHALLENGE -> {
                                    Text("📚", fontSize = 15.sp)
                                }
                                GameCategoryFilter.ENGLISH_ADVENTURE -> {
                                    Text("🗺️", fontSize = 15.sp)
                                }
                            }
                            Text(
                                cat.title,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSel) Color.White else Color(0xFFCBD5E1),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // 3. Action Banner: "+ Add New Challenge"
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF09122C).copy(alpha = 0.92f)),
                border = BorderStroke(1.5.dp, Color(0xFF0077B6).copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCreateDialog = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
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
                        // Glowing cyan circle with white plus
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(Color(0xFF00F0FF), Color(0xFF0072FF))
                                    )
                                )
                                .border(1.dp, Color(0xFF7DD3FC).copy(alpha = 0.8f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.Add,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "+ Add New ${selectedCategory.title}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                "Create a question or drill to publish",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1
                            )
                        }
                    }

                    // Magenta-violet pill button: "Add New ->"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFD946EF), Color(0xFF8B5CF6))
                                )
                            )
                            .border(1.dp, Color(0xFFF0ABFC).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                            .clickable { showCreateDialog = true }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Add New",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }

        if (selectedCategory == GameCategoryFilter.MISSING_LETTERS) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)),
                    border = BorderStroke(1.dp, Color(0xFF00D2FF).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth().testTag("missing_letters_management_toolbar")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Action buttons row: Bulk Excel Import & Manage Categories
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showBulkImportMissingLettersDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("missing_letters_bulk_import_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF059669)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("📊", fontSize = 16.sp)
                                    Text(
                                        "Bulk Excel Import",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { showManageCategoriesDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("missing_letters_manage_categories_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF67E8F9)
                                ),
                                border = BorderStroke(1.2.dp, Color(0xFF06B6D4)),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🏷️", fontSize = 16.sp)
                                    Text(
                                        "Categories (${missingLettersCategories.size})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Category filter chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Filter by Category:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    "${filteredMissingLetters.size} showing",
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // "All" Chip
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (selectedMissingLettersCategory == "All") Color(0xFF2563EB) else Color(0xFF1E293B),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selectedMissingLettersCategory == "All") Color(0xFF60A5FA) else Color(0xFF334155)
                                    ),
                                    modifier = Modifier
                                        .clickable { selectedMissingLettersCategory = "All" }
                                        .testTag("category_filter_all")
                                ) {
                                    Text(
                                        text = "All (${missingLetters.size})",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedMissingLettersCategory == "All") Color.White else Color(0xFF94A3B8),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }

                                missingLettersCategories.forEach { cat ->
                                    val count = missingLetters.count { it.category.equals(cat, ignoreCase = true) }
                                    val isSelected = selectedMissingLettersCategory.equals(cat, ignoreCase = true)
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (isSelected) Color(0xFF8B5CF6) else Color(0xFF1E293B),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) Color(0xFFC084FC) else Color(0xFF334155)
                                        ),
                                        modifier = Modifier
                                            .clickable { selectedMissingLettersCategory = cat }
                                            .testTag("category_filter_${cat.lowercase()}")
                                    ) {
                                        Text(
                                            text = "$cat ($count)",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }

                                // Quick Add Category Chip
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF0F172A),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .clickable { showManageCategoriesDialog = true }
                                        .testTag("add_category_chip_btn")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Icon(Icons.Filled.Add, contentDescription = "Add Category", tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                        Text("Add Category", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                        }
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

        // 4. Section Header with Layers Icon, Title, Count, and Search Button
        val (categoryHeaderLabel, currentCount) = when (selectedCategory) {
            GameCategoryFilter.WORD_MATCH -> "Existing Word Match Pairs" to filteredWordMatches.size
            GameCategoryFilter.MISSING_LETTERS -> "Existing Missing Letters Challenges" to filteredMissingLetters.size
            GameCategoryFilter.SENTENCE_BUILDER -> "Existing Sentence Builder Challenges" to filteredSentenceBuilders.size
            GameCategoryFilter.LISTEN_CHOOSE -> "Existing Listen & Choose Drills" to filteredListenChooses.size
            GameCategoryFilter.PRONUNCIATION_BATTLE -> "Existing Pronunciation Battles" to filteredPronunciationBattles.size
            GameCategoryFilter.FAST_VOCABULARY -> "Existing Fast Vocabulary Blitzes" to filteredFastVocabs.size
            GameCategoryFilter.GRAMMAR_CHALLENGE -> "Existing Grammar Challenges" to filteredGrammarChallenges.size
            GameCategoryFilter.PICTURE_VOCAB -> "Existing Picture Vocabulary Items" to filteredPictureVocabs.size
            GameCategoryFilter.ENGLISH_ADVENTURE -> "English Adventure Worlds" to viewModel.adventureWorlds.value.size
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Rounded.Layers,
                        contentDescription = null,
                        tint = Color(0xFF00D2FF),
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        "$categoryHeaderLabel ($currentCount)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }

                // Circular search toggle button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0A152E))
                        .border(1.dp, Color(0xFF0077B6).copy(alpha = 0.7f), CircleShape)
                        .clickable { isSearchVisible = !isSearchVisible },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isSearchVisible) Icons.Filled.Close else Icons.Filled.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF00D2FF),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (isSearchVisible) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter ${selectedCategory.title} challenges...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF00D2FF), modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0B1428),
                        unfocusedContainerColor = Color(0xFF0B1428),
                        focusedBorderColor = Color(0xFF00D2FF),
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Items based on selected category with Edit & Delete support and alternating cyber neon themes
        when (selectedCategory) {
            GameCategoryFilter.WORD_MATCH -> {
                itemsIndexed(filteredWordMatches) { index, item ->
                    GameItemCard(
                        title = item.word,
                        subtitle = item.definition,
                        tag = item.category,
                        createdBy = item.createdBy,
                        index = index,
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
                itemsIndexed(filteredMissingLetters) { index, item ->
                    GameItemCard(
                        title = item.fullWord,
                        subtitle = "Clue: ${item.hint}",
                        tag = item.category,
                        createdBy = item.createdBy,
                        index = index,
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
                                    statusMsg = "Updated '${f1.uppercase()}' in category '${f3.ifBlank { "General" }}'!"
                                }
                            )
                        },
                        onDelete = { viewModel.deleteMissingLettersChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.SENTENCE_BUILDER -> {
                itemsIndexed(filteredSentenceBuilders) { index, item ->
                    GameItemCard(
                        title = item.targetSentence,
                        subtitle = "Hint: ${item.hint}" + if (item.assameseTranslation.isNotBlank()) "\n🇮🇳 অসমীয়া: ${item.assameseTranslation}" else "",
                        tag = "Sentence Syntax",
                        createdBy = item.createdBy,
                        index = index,
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
                itemsIndexed(filteredListenChooses) { index, item ->
                    GameItemCard(
                        title = item.question,
                        subtitle = "Audio Speech: \"${item.audioScript}\"",
                        tag = "Answer: ${item.options.getOrNull(item.correctIndex) ?: ""}",
                        createdBy = item.createdBy,
                        index = index,
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
                itemsIndexed(filteredPronunciationBattles) { index, item ->
                    GameItemCard(
                        title = item.challengeSentence,
                        subtitle = "Phoneme Focus: ${item.targetPhonemeFocus}",
                        tag = "Passing: ${item.passingScore}% (${item.difficulty})",
                        createdBy = item.createdBy,
                        index = index,
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
                itemsIndexed(filteredFastVocabs) { index, item ->
                    GameItemCard(
                        title = item.word,
                        subtitle = item.prompt,
                        tag = "Answer: ${item.options.getOrNull(item.correctIndex) ?: ""}",
                        createdBy = item.createdBy,
                        index = index,
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
                itemsIndexed(filteredGrammarChallenges) { index, item ->
                    GameItemCard(
                        title = item.question,
                        subtitle = "Rule: ${item.explanation}",
                        tag = item.category,
                        createdBy = item.createdBy,
                        index = index,
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
            GameCategoryFilter.PICTURE_VOCAB -> {
                itemsIndexed(filteredPictureVocabs) { index, item ->
                    GameItemCard(
                        title = "${item.emoji} ${item.word}",
                        subtitle = "${item.phonetic} • ${item.clue}",
                        tag = "Options: ${item.options.joinToString(", ")}",
                        createdBy = item.createdBy,
                        index = index,
                        onPlayAudio = { viewModel.speakAudio(item.word) },
                        onEdit = {
                            activeEditingChallenge = ActiveEditingChallenge(
                                category = GameCategoryFilter.PICTURE_VOCAB,
                                id = item.id,
                                field1 = item.word,
                                field2 = item.emoji,
                                field3 = item.phonetic,
                                field4 = item.clue,
                                field5 = item.options.joinToString(", "),
                                onSave = { f1, f2, f3, f4, f5 ->
                                    val opts = f5.split(",").map { it.trim() }.filter { it.isNotEmpty() }.ifEmpty { listOf(f1) }
                                    viewModel.updatePictureVocabChallenge(item.copy(word = f1, emoji = f2, phonetic = f3, clue = f4, options = opts))
                                    statusMsg = "Updated picture vocabulary item '${f1}'!"
                                }
                            )
                        },
                        onDelete = { viewModel.deletePictureVocabChallenge(item.id) }
                    )
                }
            }
            GameCategoryFilter.ENGLISH_ADVENTURE -> {
                item {
                    com.example.ui.screens.common.AdventureWorldsManagementList(
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 450.dp, max = 750.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp)
                    )
                }
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
                statusMsg = "Successfully published and synced new challenge for ${selectedCategory.title} to Supabase & student panel! 🚀"
                showCreateDialog = false
            },
            viewModel = viewModel,
            onOpenBulkImport = {
                showCreateDialog = false
                showBulkImportMissingLettersDialog = true
            }
        )
    }

    if (showBulkImportMissingLettersDialog) {
        BulkImportMissingLettersDialog(
            onDismiss = { showBulkImportMissingLettersDialog = false },
            onImported = { count ->
                statusMsg = "Successfully imported $count challenges from spreadsheet!"
                showBulkImportMissingLettersDialog = false
            },
            viewModel = viewModel
        )
    }

    if (showManageCategoriesDialog) {
        ManageMissingLettersCategoriesDialog(
            onDismiss = { showManageCategoriesDialog = false },
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

// Data class and theme selector for radiant GameItemCards
data class GameCardTheme(
    val borderColor: Color,
    val bgGrad: List<Color>,
    val iconGrad: List<Color>,
    val iconType: String,
    val pillBg: Color,
    val pillBorder: Color,
    val pillText: Color,
    val accentColor: Color
)

fun getGameCardTheme(title: String, tag: String, index: Int): GameCardTheme {
    return when {
        title.equals("Eloquence", ignoreCase = true) || (tag.equals("Communication", ignoreCase = true) && index % 3 == 0) -> {
            GameCardTheme(
                borderColor = Color(0xFF00D2FF),
                bgGrad = listOf(Color(0xFF0B1C48), Color(0xFF071133)),
                iconGrad = listOf(Color(0xFF00D2FF), Color(0xFF1D4ED8)),
                iconType = "megaphone",
                pillBg = Color(0xFF0A224E),
                pillBorder = Color(0xFF00D2FF).copy(alpha = 0.7f),
                pillText = Color(0xFF38BDF8),
                accentColor = Color(0xFF00D2FF)
            )
        }
        title.equals("Resilience", ignoreCase = true) || tag.equals("Mindset", ignoreCase = true) || (index % 3 == 1) -> {
            GameCardTheme(
                borderColor = Color(0xFF00E676),
                bgGrad = listOf(Color(0xFF082728), Color(0xFF05171B)),
                iconGrad = listOf(Color(0xFF00E676), Color(0xFF059669)),
                iconType = "mountain",
                pillBg = Color(0xFF063327),
                pillBorder = Color(0xFF00E676).copy(alpha = 0.7f),
                pillText = Color(0xFF34D399),
                accentColor = Color(0xFF34D399)
            )
        }
        title.equals("Articulation", ignoreCase = true) || (index % 3 == 2) -> {
            GameCardTheme(
                borderColor = Color(0xFFB026FF),
                bgGrad = listOf(Color(0xFF1E0E3D), Color(0xFF120726)),
                iconGrad = listOf(Color(0xFFC084FC), Color(0xFF7C3AED)),
                iconType = "microphone",
                pillBg = Color(0xFF28104D),
                pillBorder = Color(0xFFC084FC).copy(alpha = 0.7f),
                pillText = Color(0xFFC084FC),
                accentColor = Color(0xFF38BDF8)
            )
        }
        else -> {
            GameCardTheme(
                borderColor = Color(0xFF00D2FF),
                bgGrad = listOf(Color(0xFF0B1C48), Color(0xFF071133)),
                iconGrad = listOf(Color(0xFF00D2FF), Color(0xFF1D4ED8)),
                iconType = "megaphone",
                pillBg = Color(0xFF0A224E),
                pillBorder = Color(0xFF00D2FF).copy(alpha = 0.7f),
                pillText = Color(0xFF38BDF8),
                accentColor = Color(0xFF00D2FF)
            )
        }
    }
}

@Composable
fun ChallengeBadgeIcon(iconType: String, modifier: Modifier = Modifier) {
    when (iconType) {
        "megaphone" -> {
            Icon(
                Icons.Filled.Campaign,
                contentDescription = null,
                tint = Color.White,
                modifier = modifier
            )
        }
        "mountain" -> {
            Canvas(modifier = modifier) {
                val w = size.width
                val h = size.height
                // Mountain body (Forest Green)
                val mtnPath = Path().apply {
                    moveTo(w * 0.12f, h * 0.88f)
                    lineTo(w * 0.50f, h * 0.28f)
                    lineTo(w * 0.88f, h * 0.88f)
                    close()
                }
                drawPath(mtnPath, Color(0xFF047857))

                // Left darker shading
                val shadePath = Path().apply {
                    moveTo(w * 0.12f, h * 0.88f)
                    lineTo(w * 0.50f, h * 0.28f)
                    lineTo(w * 0.50f, h * 0.88f)
                    close()
                }
                drawPath(shadePath, Color(0xFF065F46))

                // Snow cap
                val snowPath = Path().apply {
                    moveTo(w * 0.35f, h * 0.52f)
                    lineTo(w * 0.50f, h * 0.28f)
                    lineTo(w * 0.65f, h * 0.52f)
                    lineTo(w * 0.55f, h * 0.46f)
                    lineTo(w * 0.45f, h * 0.50f)
                    close()
                }
                drawPath(snowPath, Color.White)

                // Yellow Flagpole on peak
                drawLine(
                    color = Color(0xFFFEF08A),
                    start = Offset(w * 0.50f, h * 0.30f),
                    end = Offset(w * 0.50f, h * 0.10f),
                    strokeWidth = 2.5.dp.toPx()
                )
                // Yellow triangular flag waving to the right
                val flagPath = Path().apply {
                    moveTo(w * 0.50f, h * 0.10f)
                    lineTo(w * 0.76f, h * 0.18f)
                    lineTo(w * 0.50f, h * 0.26f)
                    close()
                }
                drawPath(flagPath, Color(0xFFFACC15))
            }
        }
        "microphone" -> {
            Icon(
                Icons.Filled.Mic,
                contentDescription = null,
                tint = Color.White,
                modifier = modifier
            )
        }
        else -> {
            Icon(
                Icons.Filled.SportsEsports,
                contentDescription = null,
                tint = Color.White,
                modifier = modifier
            )
        }
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
    onDelete: () -> Unit,
    index: Int = 0
) {
    val theme = getGameCardTheme(title = title, tag = tag, index = index)

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, theme.borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("game_item_card_$title")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(theme.bgGrad)
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left 3D Icon Badge (Megaphone, Mountain with Yellow Flag, Microphone)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(theme.iconGrad)
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    ChallengeBadgeIcon(iconType = theme.iconType, modifier = Modifier.size(34.dp))
                }

                // Middle Info Column
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Category Tag Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = theme.pillBg,
                        border = BorderStroke(1.dp, theme.pillBorder)
                    ) {
                        Text(
                            text = tag,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.pillText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.5.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.5.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )

                    Text(
                        text = "Published by: $createdBy",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.accentColor
                    )
                }

                // Right Actions Column (3 square buttons at top + Edit pill at bottom)
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.height(104.dp)
                ) {
                    // Top Row: Volume, Edit, Delete
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0D2556),
                            border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onPlayAudio() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = "Play Audio",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0D2556),
                            border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onEdit() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = "Edit Question",
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF3B1525),
                            border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.6f)),
                            modifier = Modifier
                                .size(32.dp)
                                .clickable { onDelete() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = Color(0xFFF43F5E),
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    // Bottom: Edit Pill Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Transparent,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                )
                            )
                            .clickable { onEdit() }
                            .testTag("edit_pill_btn_$title")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Edit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
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
                        OutlinedTextField(value = f3, onValueChange = { f3 = it }, label = { Text("Category (e.g. Animals, Vegetables, Fruits)") }, modifier = Modifier.fillMaxWidth())
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
                    GameCategoryFilter.PICTURE_VOCAB -> {
                        OutlinedTextField(value = f1, onValueChange = { f1 = it }, label = { Text("Target Word (e.g. Telescope)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f2, onValueChange = { f2 = it }, label = { Text("Emoji / Visual (e.g. 🔭)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f3, onValueChange = { f3 = it }, label = { Text("Phonetic Guide (e.g. /ˈtel.ɪ.skəʊp/)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f4, onValueChange = { f4 = it }, label = { Text("Clue / Definition") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = f5, onValueChange = { f5 = it }, label = { Text("Options (comma separated, must include target word)") }, modifier = Modifier.fillMaxWidth())
                    }
                    GameCategoryFilter.ENGLISH_ADVENTURE -> {
                        Text("Manage English Adventure missions directly from the world cards.", color = Color.White)
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
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TeacherSpeakingSection.values().forEach { sec ->
                    val isSel = currentSection == sec
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSel) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .clickable { viewModel.selectTeacherSpeakingSection(sec) }
                            .testTag("teacher_section_${sec.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(sec.emoji, fontSize = 13.sp)
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = sec.title,
                                fontSize = 12.sp,
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
            TeacherSpeakingSection.ADVENTURE_WORLDS -> {
                com.example.ui.screens.common.AdventureWorldsManagementList(
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
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
            .padding(16.dp)
            .testTag("teacher_speaking_review_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Student Voice Recordings to Grade",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
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
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(sub.studentAvatar, fontSize = 16.sp)
                            Text(
                                text = sub.studentName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                softWrap = false
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
                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                    border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sub.studentName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Recorded: ${sub.submittedDate} • ${sub.audioDurationSeconds}s audio",
                                    fontSize = 11.sp,
                                    color = MutedGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Button(
                                onClick = { viewModel.speakAudio(sub.studentTranscript) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                                modifier = Modifier.testTag("play_recording_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Rounded.PlayArrow, contentDescription = "Play", modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Play Recording",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }

                        // Speech Transcript
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0F1E42),
                            border = BorderStroke(1.dp, Color(0xFF283B70)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Student Audio Transcript:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                                Text("\"${sub.studentTranscript}\"", fontSize = 13.sp, color = Color.White)
                            }
                        }

                        // Grading Sliders
                        Text("Teacher Evaluation Criteria:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)

                        GradingSlider("Fluency & Conversational Flow", fluencyScore) { fluencyScore = it }
                        GradingSlider("Pronunciation Accuracy & Accent", pronunciationScore) { pronunciationScore = it }
                        GradingSlider("Grammar & Lexical Resource", grammarScore) { grammarScore = it }

                        // Quick Feedback Chips
                        Text("Suggested Feedback Templates:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Terrific rhythm!", "Work on voiced TH", "Natural intonation").forEach { chip ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF131F46),
                                    border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.5f)),
                                    modifier = Modifier.clickable { teacherFeedbackInput = "$teacherFeedbackInput $chip" }
                                ) {
                                    Text(chip, fontSize = 10.sp, color = Color(0xFF93C5FD), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
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
// -------------------------------------------------------------
// TEACHER MCQ EXAMINATIONS & STUDENT CERTIFICATES
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TeacherExamsManagementScreen(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exams by viewModel.exams.collectAsState()
    val examSubmissions by viewModel.examSubmissions.collectAsState()
    val academicClasses by viewModel.academicClasses.collectAsState()

    var showCreateExamDialog by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<Exam?>(null) }
    var selectedExamForSubmissions by remember { mutableStateOf<Exam?>(null) }
    var selectedSubmissionForCert by remember { mutableStateOf<ExamSubmission?>(null) }
    var toastMsg by remember { mutableStateOf<String?>(null) }
    var selectedClassFilter by remember { mutableStateOf("All Classes") }
    var currentTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        viewModel.refreshExamsFromCloud()
        while (true) {
            delay(1000L)
            currentTimeMillis = System.currentTimeMillis()
        }
    }

    val filteredExams = remember(exams, selectedClassFilter) {
        if (selectedClassFilter == "All Classes") exams
        else exams.filter { it.targetClass == selectedClassFilter || it.targetClass == "All Classes" }
    }

    val totalSubmissions = examSubmissions.size
    val totalPassed = examSubmissions.count { it.isPassed }
    val avgScore = if (examSubmissions.isNotEmpty()) examSubmissions.map { it.percentage }.average().toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- Top Header ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                ) {
                    Text(
                        text = "MCQ Examination Management",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Create timed tests, assign marks & award certificates",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = { showCreateExamDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Create Exam",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 12.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }

        // --- Status Banner if any ---
        toastMsg?.let { msg ->
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F2E22),
                    border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(msg, color = EmeraldSuccess, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }

        // --- Stats Row ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TeacherExamStatBox(
                    label = "Active Exams",
                    value = "${exams.size}",
                    icon = Icons.Rounded.Quiz,
                    color = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                TeacherExamStatBox(
                    label = "Submissions",
                    value = "$totalSubmissions",
                    icon = Icons.Rounded.People,
                    color = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
                TeacherExamStatBox(
                    label = "Certificates",
                    value = "$totalPassed",
                    icon = Icons.Rounded.WorkspacePremium,
                    color = AmberGold,
                    modifier = Modifier.weight(1f)
                )
                TeacherExamStatBox(
                    label = "Avg Score",
                    value = "$avgScore%",
                    icon = Icons.Rounded.Speed,
                    color = EmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // --- Class Filter Chips ---
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp)
            ) {
                val targets = listOf("All Classes") + academicClasses
                items(targets) { cls ->
                    val isSel = selectedClassFilter == cls
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedClassFilter = cls },
                        label = {
                            Text(
                                text = cls,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else Color(0xFFCBD5E1)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = Color(0xFF0D173C),
                            labelColor = Color(0xFFCBD5E1),
                            selectedContainerColor = RoyalBlue,
                            selectedLabelColor = Color.White
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSel,
                            borderColor = Color(0xFF263366),
                            selectedBorderColor = RoyalBlue
                        )
                    )
                }
            }
        }

        if (filteredExams.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0D173C),
                    border = BorderStroke(1.dp, Color(0xFF1E2B52)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("📋", fontSize = 28.sp)
                            Text("No exams scheduled for $selectedClassFilter yet.", fontSize = 13.sp, color = Color(0xFF94A3B8))
                            TextButton(onClick = { showCreateExamDialog = true }) {
                                Text("+ Click to Create First MCQ Exam", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- Exam Cards List ---
        items(filteredExams) { exam ->
            val submissionsForThisExam = examSubmissions.filter { it.examId == exam.id }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg, contentColor = Color.White),
                border = BorderStroke(1.2.dp, Color(0xFF818CF8).copy(alpha = 0.45f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category & Class Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF131F46),
                                border = BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Rounded.School, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(12.dp))
                                    Text(
                                        "Target: ${exam.targetClass}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF60A5FA)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF1E1B4B)
                            ) {
                                Text(
                                    text = exam.subjectCategory,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA78BFA),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Edit & Delete buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0C2444),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { editingExam = exam }
                                    .testTag("teacher_edit_exam_btn_${exam.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Edit,
                                        contentDescription = "Edit Exam",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Edit",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7DD3FC)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    viewModel.deleteExam(exam.id)
                                    toastMsg = "Deleted exam: ${exam.title}"
                                },
                                modifier = Modifier
                                    .size(30.dp)
                                    .testTag("teacher_delete_exam_btn_${exam.id}")
                            ) {
                                Icon(Icons.Rounded.DeleteOutline, contentDescription = "Delete Exam", tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Text(exam.description, fontSize = 12.sp, color = Color(0xFFCBD5E1), maxLines = 2, overflow = TextOverflow.Ellipsis)

                    // Specs Row (Duration, Questions, Total Marks, Pass %)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF090D26), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏱️ ${exam.durationMinutes} Mins", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        Text("📝 ${exam.questionsCount} MCQs", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        Text("🎯 ${exam.totalMarks} Marks", fontSize = 11.sp, color = AmberGold, fontWeight = FontWeight.Bold)
                        Text("🏆 Pass: ${exam.passingPercentage}%", fontSize = 11.sp, color = EmeraldSuccess)
                    }

                    // Student Access Window Status Banner
                    if (exam.hasTimeWindow) {
                        val isExpired = exam.isExpiredAt(currentTimeMillis)
                        val isBeforeStart = exam.isBeforeStartAt(currentTimeMillis)
                        val statusBg = when {
                            isExpired -> Color(0xFF3B0D16)
                            isBeforeStart -> Color(0xFF17204A)
                            else -> Color(0xFF063325)
                        }
                        val statusBorder = when {
                            isExpired -> Color(0xFFF43F5E)
                            isBeforeStart -> Color(0xFF38BDF8)
                            else -> EmeraldSuccess
                        }
                        val statusText = when {
                            isExpired -> "🔒 Time Over • Hidden from Student Panel (Ended ${ExamTimeFormatter.formatShortDateTime(exam.availableUntilMillis)})"
                            isBeforeStart -> "🕒 Scheduled • ${ExamTimeFormatter.formatShortDateTime(exam.availableFromMillis)} → ${ExamTimeFormatter.formatShortDateTime(exam.availableUntilMillis)}"
                            exam.availableUntilMillis > 0L -> "🟢 Live Window • Closes in ${ExamTimeFormatter.formatCountdown(exam.availableUntilMillis - currentTimeMillis)} (${ExamTimeFormatter.formatTimeOnly(exam.availableUntilMillis)})"
                            else -> "🟢 Open Since ${ExamTimeFormatter.formatShortDateTime(exam.availableFromMillis)}"
                        }
                        val statusColor = when {
                            isExpired -> Color(0xFFFDA4AF)
                            isBeforeStart -> Color(0xFF93C5FD)
                            else -> Color(0xFF6EE7B7)
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = statusBg,
                            border = BorderStroke(1.dp, statusBorder.copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = statusText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Submissions bar & Review Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Student Submissions: ${submissionsForThisExam.size}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF93C5FD)
                            )
                            val passedForExam = submissionsForThisExam.count { it.isPassed }
                            Text(
                                "$passedForExam / ${submissionsForThisExam.size} Earned Certificate",
                                fontSize = 10.5.sp,
                                color = if (passedForExam > 0) EmeraldSuccess else Color(0xFF94A3B8)
                            )
                        }

                        Button(
                            onClick = { selectedExamForSubmissions = exam },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Rounded.Assessment, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("View Results & Certs", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }

    // --- Create MCQ Exam Dialog ---
    if (showCreateExamDialog) {
        TeacherCreateOrEditExamDialog(
            academicClasses = academicClasses,
            existingExam = null,
            onDismiss = { showCreateExamDialog = false },
            onExamSaved = { savedExam ->
                showCreateExamDialog = false
                toastMsg = "Published exam '${savedExam.title}' (${savedExam.questionsCount} MCQs, ${savedExam.durationMinutes} mins)!"
            },
            viewModel = viewModel
        )
    }

    // --- Edit Existing MCQ Exam Dialog ---
    editingExam?.let { examToEdit ->
        TeacherCreateOrEditExamDialog(
            academicClasses = academicClasses,
            existingExam = examToEdit,
            onDismiss = { editingExam = null },
            onExamSaved = { updatedExam ->
                editingExam = null
                toastMsg = "Updated exam '${updatedExam.title}'!"
            },
            viewModel = viewModel
        )
    }

    // --- Submissions & Results Dialog ---
    selectedExamForSubmissions?.let { exam ->
        val submissionsForExam = examSubmissions.filter { it.examId == exam.id }

        AlertDialog(
            onDismissRequest = { selectedExamForSubmissions = null },
            title = {
                Column {
                    Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Submissions & Earned Certificates (${submissionsForExam.size})", fontSize = 11.5.sp, color = Color(0xFF94A3B8))
                }
            },
            text = {
                if (submissionsForExam.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("No students have submitted this exam yet.", fontSize = 13.sp, color = Color(0xFF94A3B8))
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(submissionsForExam) { sub ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.dp, if (sub.isPassed) EmeraldSuccess.copy(alpha = 0.5f) else Color(0xFF334155)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(sub.studentAvatar, fontSize = 20.sp)
                                        Column {
                                            Text(sub.studentName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                            Text(
                                                "Class ${sub.studentClass} • Score: ${sub.scoreObtained}/${sub.totalMarks} (${sub.percentage}%) • Grade ${sub.grade}",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    }

                                    if (sub.isPassed) {
                                        Button(
                                            onClick = { selectedSubmissionForCert = sub },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = Color(0xFF78350F), modifier = Modifier.size(13.dp))
                                            Spacer(Modifier.width(3.dp))
                                            Text("Certificate", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedExamForSubmissions = null }) {
                    Text("Close")
                }
            }
        )
    }

    // --- Teacher Certificate Preview Dialog with PDF Download ---
    selectedSubmissionForCert?.let { sub ->
        SpeaklyCertificatePreviewDialog(
            initialSubmission = sub,
            allSubmissions = examSubmissions,
            onDismiss = { selectedSubmissionForCert = null },
            onDownloadPdf = { submissionToDownload ->
                CertificatePdfGenerator.generateCertificatePdf(
                    context = context,
                    submission = submissionToDownload,
                    onSuccess = { file ->
                        Toast.makeText(context, "Certificate PDF downloaded to Downloads!", Toast.LENGTH_LONG).show()
                        CertificatePdfGenerator.openOrSharePdf(context, file)
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

// -------------------------------------------------------------
// TEACHER CREATE / EDIT EXAM DIALOG WITH TIME WINDOW & MCQ BUILDER
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TeacherCreateOrEditExamDialog(
    academicClasses: List<String>,
    existingExam: Exam? = null,
    onDismiss: () -> Unit,
    onExamSaved: (Exam) -> Unit,
    viewModel: MainViewModel
) {
    val isEditing = existingExam != null
    var title by remember(existingExam?.id) { mutableStateOf(existingExam?.title ?: "") }
    var description by remember(existingExam?.id) { mutableStateOf(existingExam?.description ?: "") }
    var selectedClass by remember(existingExam?.id) { mutableStateOf(existingExam?.targetClass ?: AcademicClasses.DEFAULT_CLASS) }
    var selectedCategory by remember(existingExam?.id) { mutableStateOf(existingExam?.subjectCategory ?: "English Grammar") }
    var selectedDurationMinutes by remember(existingExam?.id) { mutableIntStateOf(existingExam?.durationMinutes ?: 15) }
    var passingPercentage by remember(existingExam?.id) { mutableIntStateOf(existingExam?.passingPercentage ?: 60) }

    // Availability Window State (Students can take exam within selected time; auto-hides after End time)
    val initialNow = remember { System.currentTimeMillis() }
    var useTimeWindow by remember(existingExam?.id) {
        mutableStateOf(existingExam?.hasTimeWindow == true)
    }
    var windowStartMillis by remember(existingExam?.id) {
        mutableLongStateOf(
            if (existingExam != null && existingExam.availableFromMillis > 0L) existingExam.availableFromMillis
            else initialNow
        )
    }
    var windowEndMillis by remember(existingExam?.id) {
        mutableLongStateOf(
            if (existingExam != null && existingExam.availableUntilMillis > 0L) existingExam.availableUntilMillis
            else initialNow + 30 * 60_000L
        )
    }
    var activePickerTarget by remember { mutableStateOf<String?>(null) } // "START" or "END"

    // Dynamic list of questions being composed
    val questionsList = remember(existingExam?.id) {
        mutableStateListOf<ExamQuestion>().apply {
            if (existingExam != null) {
                addAll(existingExam.questions)
            }
        }
    }

    // Form inputs for adding or editing a single question
    var editingQuestionIndex by remember { mutableStateOf<Int?>(null) }
    var currentQuestionText by remember { mutableStateOf("") }
    var optA by remember { mutableStateOf("") }
    var optB by remember { mutableStateOf("") }
    var optC by remember { mutableStateOf("") }
    var optD by remember { mutableStateOf("") }
    var correctOptIndex by remember { mutableIntStateOf(0) }
    var marksForQuestion by remember { mutableIntStateOf(5) }
    var questionExplanation by remember { mutableStateOf("") }

    val totalCalculatedMarks = questionsList.sumOf { it.marks }
    val isWindowInvalid = useTimeWindow && windowEndMillis <= windowStartMillis

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (isEditing) "✏️" else "📝", fontSize = 20.sp)
                Column {
                    Text(
                        text = if (isEditing) "Edit MCQ Examination" else "Create MCQ Examination",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Define duration, student access time & questions",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Exam Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Exam Title") },
                    placeholder = { Text("e.g. Class 10 Mid-Term Grammar & Phonetics") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_dialog_title_input")
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Instructions / Description") },
                    placeholder = { Text("e.g. Answer all multiple choice questions before time runs out.") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )

                // Target Class Selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Target Student Class:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (listOf("All Classes") + academicClasses).forEach { cls ->
                            FilterChip(
                                selected = selectedClass == cls,
                                onClick = { selectedClass = cls },
                                label = { Text(cls, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Subject Category
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Subject / Category:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "English Grammar",
                            "Phonetics & Pronunciation",
                            "Vocabulary & Idioms",
                            "Spoken English & Fluency"
                        ).forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // Time Duration Selector (Exam Timer)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Selected Exam Time (Timer):", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            "$selectedDurationMinutes Minutes",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(5, 10, 15, 20, 30, 45, 60).forEach { mins ->
                            FilterChip(
                                selected = selectedDurationMinutes == mins,
                                onClick = { selectedDurationMinutes = mins },
                                label = { Text("$mins Mins", fontSize = 11.sp) }
                            )
                        }
                    }
                }

                // --- Student Access Window (Schedule & Auto-Hide After Time Over) ---
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF090D26),
                    border = BorderStroke(
                        1.dp,
                        if (useTimeWindow) Color(0xFF38BDF8).copy(alpha = 0.6f) else Color(0xFF1E293B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Student Access Time Window",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    "Auto-hides from Student Panel after end time",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Switch(
                                checked = useTimeWindow,
                                onCheckedChange = { enabled ->
                                    useTimeWindow = enabled
                                    if (enabled && windowEndMillis <= windowStartMillis) {
                                        val now = System.currentTimeMillis()
                                        windowStartMillis = now
                                        windowEndMillis = now + 30 * 60_000L
                                    }
                                },
                                modifier = Modifier.testTag("exam_time_window_switch")
                            )
                        }

                        if (!useTimeWindow) {
                            Text(
                                "Currently set to Always Open — students can take this exam anytime.",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        } else {
                            // Quick Window Presets starting from Now
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "Quick Window (Starts Now):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFCBD5E1)
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(
                                        2 to "2 Mins",
                                        5 to "5 Mins",
                                        15 to "15 Mins",
                                        30 to "30 Mins",
                                        60 to "1 Hour",
                                        120 to "2 Hours",
                                        1440 to "24 Hours"
                                    ).forEach { (mins, label) ->
                                        val diffMins = ((windowEndMillis - windowStartMillis) / 60_000L).toInt()
                                        FilterChip(
                                            selected = diffMins == mins,
                                            onClick = {
                                                val now = System.currentTimeMillis()
                                                windowStartMillis = now
                                                windowEndMillis = now + mins * 60_000L
                                            },
                                            label = { Text(label, fontSize = 10.5.sp) }
                                        )
                                    }
                                }
                            }

                            // Custom Start Time Selector
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Start Time", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                                        Text(
                                            ExamTimeFormatter.formatShortDateTime(windowStartMillis),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        TextButton(
                                            onClick = {
                                                val now = System.currentTimeMillis()
                                                val prevDuration = (windowEndMillis - windowStartMillis).coerceAtLeast(15 * 60_000L)
                                                windowStartMillis = now
                                                windowEndMillis = now + prevDuration
                                            },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text("Now", fontSize = 11.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = { activePickerTarget = "START" },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text("Pick", fontSize = 11.sp)
                                        }
                                    }
                                }
                            }

                            // Custom End Time Selector
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.dp, if (isWindowInvalid) Color(0xFFEF4444) else Color(0xFF1E293B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("End Time (Auto-Hide)", fontSize = 10.5.sp, color = Color(0xFF94A3B8))
                                        Text(
                                            ExamTimeFormatter.formatShortDateTime(windowEndMillis),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFBBF24)
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { activePickerTarget = "END" },
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("Pick", fontSize = 11.sp)
                                    }
                                }
                            }

                            if (isWindowInvalid) {
                                Text(
                                    "⚠️ End time must be after Start time.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFF87171),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Passing Percentage Selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Passing Score to Earn Certificate:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("$passingPercentage%", fontWeight = FontWeight.ExtraBold, color = EmeraldSuccess, fontSize = 12.sp)
                    }
                    Slider(
                        value = passingPercentage.toFloat(),
                        onValueChange = { passingPercentage = it.toInt() },
                        valueRange = 40f..90f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = EmeraldSuccess,
                            activeTrackColor = EmeraldSuccess
                        )
                    )
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                // --- MCQ Questions Builder Section ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Questions (${questionsList.size} Added)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Total Marks: $totalCalculatedMarks",
                            fontSize = 11.sp,
                            color = AmberGold,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Quick Helper to load a question pack
                    OutlinedButton(
                        onClick = {
                            val sampleQuestions = when (selectedCategory) {
                                "Phonetics & Pronunciation" -> listOf(
                                    ExamQuestion("sq1", "Which sound does the IPA symbol /ʃ/ represent in English?", listOf("The 'ch' sound in 'chair'", "The 'sh' sound in 'shine'", "The 's' sound in 'sun'", "The 'j' sound in 'jump'"), 1, 5, "/ʃ/ is the voiceless postalveolar fricative."),
                                    ExamQuestion("sq2", "In which word is the letter 'p' silent?", listOf("Pencil", "Psychology", "Purple", "Paper"), 1, 5, "Initial 'p' before 's' in Greek roots is silent."),
                                    ExamQuestion("sq3", "Which word contains the diphthong /eɪ/?", listOf("Play", "See", "Car", "Door"), 0, 5, "'Play' /pleɪ/ features the closing diphthong /eɪ/."),
                                    ExamQuestion("sq4", "Where is stress placed in 'IMPOSSIBLE'?", listOf("First syllable (IM-)", "Second syllable (-POS-)", "Third syllable (-si-)", "Fourth syllable (-ble)"), 1, 5, "Stress falls on second syllable /ɪmˈpɒs.ə.bəl/."),
                                    ExamQuestion("sq5", "Which IPA vowel is the short open front unrounded vowel?", listOf("/æ/ as in 'cat'", "/uː/ as in 'goose'", "/ɑː/ as in 'car'", "/ɔː/ as in 'thought'"), 0, 5, "/æ/ is the short vowel in cat, trap, hat.")
                                )
                                "Vocabulary & Idioms" -> listOf(
                                    ExamQuestion("sq1", "What does 'A piece of cake' signify?", listOf("An expensive meal", "Something very easy to do", "A bakery item", "A sudden surprise"), 1, 5, "Signifies an easy task."),
                                    ExamQuestion("sq2", "Select the synonym of 'PRAGMATIC':", listOf("Idealistic", "Practical", "Careless", "Hesitant"), 1, 5, "Pragmatic means practical."),
                                    ExamQuestion("sq3", "Complete the idiom: 'Beat around the ______'", listOf("table", "bush", "corner", "street"), 1, 5, "'Beat around the bush' means avoiding the main topic."),
                                    ExamQuestion("sq4", "Which word describes someone who speaks many languages?", listOf("Monoglot", "Polyglot", "Diplomat", "Novelist"), 1, 5, "Polyglot speaks multiple languages."),
                                    ExamQuestion("sq5", "Choose the antonym of 'BENEVOLENT':", listOf("Kind", "Generous", "Malevolent", "Sympathetic"), 2, 5, "Malevolent means wishing evil.")
                                )
                                else -> listOf(
                                    ExamQuestion("sq1", "Choose the correct past participle form of the irregular verb 'SWIM':", listOf("Swam", "Swum", "Swimming", "Swimmed"), 1, 5, "Past participle is 'swum' (swim, swam, swum)."),
                                    ExamQuestion("sq2", "Select the correct pronoun: 'Neither of the boys brought ______ lunch.'", listOf("their", "his", "they're", "its"), 1, 5, "'Neither' is grammatically singular and takes 'his'."),
                                    ExamQuestion("sq3", "Identify the passive form: 'She writes insightful essays.'", listOf("Insightful essays are written by her.", "Insightful essays were written by her.", "She has written essays.", "Essays had been written."), 0, 5, "Present simple active becomes 'are written' in passive."),
                                    ExamQuestion("sq4", "Which conjunction expresses condition?", listOf("Although", "Unless", "Because", "Therefore"), 1, 5, "'Unless' means 'if not'."),
                                    ExamQuestion("sq5", "Select the correct sentence with proper subject-verb agreement:", listOf("The flock of sheep were grazing peacefully.", "The flock of sheep was grazing peacefully.", "The flock of sheeps is grazing.", "The flock are grazing."), 1, 5, "Collective noun 'flock' is singular.")
                                )
                            }
                            questionsList.clear()
                            questionsList.addAll(sampleQuestions)
                            editingQuestionIndex = null
                        },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("⚡ Load Sample MCQs", fontSize = 11.sp, color = RoyalBlue, fontWeight = FontWeight.Bold)
                    }
                }

                // Added Questions List View (with Edit & Remove controls)
                questionsList.forEachIndexed { qIndex, q ->
                    val isBeingEdited = editingQuestionIndex == qIndex
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isBeingEdited) Color(0xFF172554) else Color(0xFF0F172A),
                        border = BorderStroke(1.dp, if (isBeingEdited) Color(0xFF38BDF8) else Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Q${qIndex + 1}: ${q.question}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "Correct: Option ${listOf("A", "B", "C", "D").getOrElse(q.correctOptionIndex) { "" }} • ${q.marks} Marks",
                                    fontSize = 10.5.sp,
                                    color = EmeraldSuccess
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        editingQuestionIndex = qIndex
                                        currentQuestionText = q.question
                                        optA = q.options.getOrElse(0) { "" }
                                        optB = q.options.getOrElse(1) { "" }
                                        optC = q.options.getOrElse(2) { "" }
                                        optD = q.options.getOrElse(3) { "" }
                                        correctOptIndex = q.correctOptionIndex
                                        marksForQuestion = q.marks
                                        questionExplanation = q.explanation
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit Question", tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                                }
                                IconButton(
                                    onClick = {
                                        if (editingQuestionIndex == qIndex) editingQuestionIndex = null
                                        questionsList.removeAt(qIndex)
                                    },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(Icons.Rounded.Close, contentDescription = "Remove", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Add / Edit Question Sub-Form
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF090D26),
                    border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (editingQuestionIndex != null) "Editing Question Q${editingQuestionIndex!! + 1}" else "Add a Question",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp,
                                color = Color(0xFF38BDF8)
                            )
                            if (editingQuestionIndex != null) {
                                TextButton(
                                    onClick = {
                                        editingQuestionIndex = null
                                        currentQuestionText = ""
                                        optA = ""
                                        optB = ""
                                        optC = ""
                                        optD = ""
                                        questionExplanation = ""
                                    },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                                ) {
                                    Text("Cancel Edit", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                            }
                        }

                        OutlinedTextField(
                            value = currentQuestionText,
                            onValueChange = { currentQuestionText = it },
                            placeholder = { Text("Enter question prompt...", fontSize = 12.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )

                        // 4 Option Inputs
                        OutlinedTextField(
                            value = optA,
                            onValueChange = { optA = it },
                            label = { Text("Option A") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = optB,
                            onValueChange = { optB = it },
                            label = { Text("Option B") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = optC,
                            onValueChange = { optC = it },
                            label = { Text("Option C") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = optD,
                            onValueChange = { optD = it },
                            label = { Text("Option D") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Correct Option Selector
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Which option is correct?", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("A", "B", "C", "D").forEachIndexed { idx, label ->
                                    FilterChip(
                                        selected = correctOptIndex == idx,
                                        onClick = { correctOptIndex = idx },
                                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = EmeraldSuccess,
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Marks for this question
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Marks:", fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(2, 5, 10).forEach { m ->
                                    FilterChip(
                                        selected = marksForQuestion == m,
                                        onClick = { marksForQuestion = m },
                                        label = { Text("$m pts", fontSize = 10.sp) }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = questionExplanation,
                            onValueChange = { questionExplanation = it },
                            label = { Text("Explanation (Optional)") },
                            placeholder = { Text("Rationale displayed in review", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = {
                                if (currentQuestionText.isNotBlank() && optA.isNotBlank() && optB.isNotBlank()) {
                                    val editIdx = editingQuestionIndex
                                    val newQuestion = ExamQuestion(
                                        id = if (editIdx != null) questionsList[editIdx].id else "q_${System.currentTimeMillis()}_${questionsList.size}",
                                        question = currentQuestionText.trim(),
                                        options = listOf(
                                            optA.trim(),
                                            optB.trim(),
                                            optC.ifBlank { "None of the above" }.trim(),
                                            optD.ifBlank { "All of the above" }.trim()
                                        ),
                                        correctOptionIndex = correctOptIndex,
                                        marks = marksForQuestion,
                                        explanation = questionExplanation.trim()
                                    )
                                    if (editIdx != null && editIdx in questionsList.indices) {
                                        questionsList[editIdx] = newQuestion
                                        editingQuestionIndex = null
                                    } else {
                                        questionsList.add(newQuestion)
                                    }
                                    // Reset question form
                                    currentQuestionText = ""
                                    optA = ""
                                    optB = ""
                                    optC = ""
                                    optD = ""
                                    questionExplanation = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(if (editingQuestionIndex != null) Icons.Rounded.Check else Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (editingQuestionIndex != null) "Update Question Q${editingQuestionIndex!! + 1}" else "Add Question to Exam",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = title.isNotBlank() && !isWindowInvalid,
                onClick = {
                    val finalQuestions = if (questionsList.isEmpty()) {
                        listOf(
                            ExamQuestion("def_1", "Choose the correct past participle form of 'WRITE':", listOf("Wrote", "Written", "Writing", "Writted"), 1, 5, "Past participle is 'written'."),
                            ExamQuestion("def_2", "Identify the correct preposition: 'She is interested ______ learning French.'", listOf("on", "in", "with", "about"), 1, 5, "Interested takes 'in'."),
                            ExamQuestion("def_3", "Which sentence is grammatically correct?", listOf("Neither he nor I are going.", "Neither he nor I am going.", "Neither he nor I is going.", "Neither him nor me is going."), 1, 5, "In 'neither... nor', the verb agrees with the subject closest to it ('I am')."),
                            ExamQuestion("def_4", "What does 'break the ice' mean?", listOf("To smash frozen water", "To make people feel more comfortable in a social setting", "To stop talking", "To cancel a plan"), 1, 5, "Idiom meaning initiating conversation."),
                            ExamQuestion("def_5", "Select the silent letter in 'SUBTLE':", listOf("s", "b", "t", "l"), 1, 5, "The letter 'b' is silent in subtle.")
                        )
                    } else {
                        questionsList.toList()
                    }

                    val fromMillis = if (useTimeWindow) windowStartMillis else 0L
                    val untilMillis = if (useTimeWindow) windowEndMillis else 0L
                    val dueLabel = if (useTimeWindow) "Closes ${ExamTimeFormatter.formatShortDateTime(untilMillis)}" else "Always Open"

                    val saved = if (existingExam != null) {
                        viewModel.updateExam(
                            examId = existingExam.id,
                            title = title,
                            description = description.ifBlank { "Complete all multiple choice questions within the allotted time." },
                            targetClass = selectedClass,
                            subjectCategory = selectedCategory,
                            durationMinutes = selectedDurationMinutes,
                            passingPercentage = passingPercentage,
                            questions = finalQuestions,
                            availableFromMillis = fromMillis,
                            availableUntilMillis = untilMillis,
                            dueDate = dueLabel
                        ) ?: existingExam
                    } else {
                        viewModel.createExam(
                            title = title,
                            description = description.ifBlank { "Complete all multiple choice questions within the allotted time." },
                            targetClass = selectedClass,
                            subjectCategory = selectedCategory,
                            durationMinutes = selectedDurationMinutes,
                            passingPercentage = passingPercentage,
                            questions = finalQuestions,
                            availableFromMillis = fromMillis,
                            availableUntilMillis = untilMillis,
                            dueDate = dueLabel
                        )
                    }
                    onExamSaved(saved)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("exam_dialog_save_btn")
            ) {
                Text(
                    text = if (isEditing) "Save Changes ✓" else "Publish Exam 🚀",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )

    // Date & Time Picker Modal for Start or End of Student Access Window
    activePickerTarget?.let { target ->
        val initialMillis = if (target == "START") windowStartMillis else windowEndMillis
        ExamScheduleDateTimePickerDialog(
            title = if (target == "START") "Select Exam Start Time" else "Select Exam End Time (Auto-Hide)",
            initialMillis = initialMillis,
            onDismiss = { activePickerTarget = null },
            onConfirm = { pickedMillis ->
                if (target == "START") {
                    windowStartMillis = pickedMillis
                    if (windowEndMillis <= pickedMillis) {
                        windowEndMillis = pickedMillis + 30 * 60_000L
                    }
                } else {
                    windowEndMillis = pickedMillis
                }
                activePickerTarget = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamScheduleDateTimePickerDialog(
    title: String,
    initialMillis: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    val initCal = remember(initialMillis) {
        Calendar.getInstance().apply { timeInMillis = if (initialMillis > 0L) initialMillis else System.currentTimeMillis() }
    }
    var dayOffset by remember(initialMillis) {
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val initStart = Calendar.getInstance().apply {
            timeInMillis = initCal.timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val diffDays = ((initStart.timeInMillis - todayStart.timeInMillis) / 86_400_000L).toInt().coerceIn(0, 30)
        mutableIntStateOf(diffDays)
    }
    var customDateMillis by remember(initialMillis) { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val timeState = rememberTimePickerState(
        initialHour = initCal.get(Calendar.HOUR_OF_DAY),
        initialMinute = initCal.get(Calendar.MINUTE),
        is24Hour = false
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF0F172A),
            border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = Color.White)

                // Quick Day Selection + Custom Date Picker trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = customDateMillis == null && dayOffset == 0,
                        onClick = { customDateMillis = null; dayOffset = 0 },
                        label = { Text("Today", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = customDateMillis == null && dayOffset == 1,
                        onClick = { customDateMillis = null; dayOffset = 1 },
                        label = { Text("Tomorrow", fontSize = 11.sp) }
                    )
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Pick Date", fontSize = 11.sp)
                    }
                }

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timeState)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val resultCal = Calendar.getInstance()
                            val pickedDate = customDateMillis
                            if (pickedDate != null) {
                                val utcCal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                                    timeInMillis = pickedDate
                                }
                                resultCal.set(Calendar.YEAR, utcCal.get(Calendar.YEAR))
                                resultCal.set(Calendar.MONTH, utcCal.get(Calendar.MONTH))
                                resultCal.set(Calendar.DAY_OF_MONTH, utcCal.get(Calendar.DAY_OF_MONTH))
                            } else {
                                resultCal.add(Calendar.DAY_OF_YEAR, dayOffset)
                            }
                            resultCal.set(Calendar.HOUR_OF_DAY, timeState.hour)
                            resultCal.set(Calendar.MINUTE, timeState.minute)
                            resultCal.set(Calendar.SECOND, 0)
                            resultCal.set(Calendar.MILLISECOND, 0)
                            onConfirm(resultCal.timeInMillis)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                    ) {
                        Text("Apply Time", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initCal.timeInMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        customDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun TeacherExamStatBox(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0D173C),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.White)
            Text(label, fontSize = 9.sp, color = Color(0xFF94A3B8), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}


// -------------------------------------------------------------
// HELPER COMPOSABLES & DIALOGS
// -------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailDialog(
    student: StudentManagementInfo,
    classes: List<String> = AcademicClasses.DEFAULT_CLASSES,
    onDismiss: () -> Unit,
    onSave: (StudentManagementInfo) -> Unit
) {
    var name by remember { mutableStateOf(student.name) }
    var email by remember { mutableStateOf(student.email) }
    var batch by remember { mutableStateOf(student.batch) }
    var classDropdownExpanded by remember { mutableStateOf(false) }
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
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Assigned Class", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                        val classList = if (classes.isNotEmpty()) classes else AcademicClasses.DEFAULT_CLASSES
                        ExposedDropdownMenuBox(
                            expanded = classDropdownExpanded,
                            onExpandedChange = { classDropdownExpanded = !classDropdownExpanded },
                            modifier = Modifier.fillMaxWidth().testTag("edit_student_class_dropdown_box")
                        ) {
                            OutlinedTextField(
                                value = batch,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Class") },
                                leadingIcon = { Icon(Icons.Rounded.School, contentDescription = null, tint = RoyalBlue) },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classDropdownExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )

                            ExposedDropdownMenu(
                                expanded = classDropdownExpanded,
                                onDismissRequest = { classDropdownExpanded = false }
                            ) {
                                classList.forEach { cls ->
                                    val isSelected = batch == cls
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                cls,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.onSurface
                                            )
                                        },
                                        leadingIcon = {
                                            if (isSelected) {
                                                Icon(Icons.Filled.Check, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                                            }
                                        },
                                        onClick = {
                                            batch = cls
                                            classDropdownExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }
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
// MANAGE MISSING LETTERS CATEGORIES DIALOG
// -------------------------------------------------------------
@Composable
fun ManageMissingLettersCategoriesDialog(
    onDismiss: () -> Unit,
    viewModel: MainViewModel
) {
    val categories by viewModel.missingLettersCategories.collectAsState()
    val challenges by viewModel.missingLettersChallenges.collectAsState()
    var newCategoryInput by remember { mutableStateOf("") }
    var actionMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🏷️", fontSize = 22.sp)
                Text("Manage Categories", fontWeight = FontWeight.Bold, fontSize = 17.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    "Organize your Missing Letters challenges by category or topic. Deleting a category automatically moves its challenges to 'General'.",
                    fontSize = 12.sp,
                    color = MutedGray
                )

                // Add Category Input Box
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Add New Category",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newCategoryInput,
                                onValueChange = { newCategoryInput = it },
                                placeholder = { Text("e.g. Science, Space, Food", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_category_input"),
                                shape = RoundedCornerShape(10.dp)
                            )
                            Button(
                                onClick = {
                                    val trimmed = newCategoryInput.trim()
                                    if (trimmed.isNotBlank()) {
                                        val added = viewModel.addMissingLettersCategory(trimmed)
                                        if (added) {
                                            actionMessage = "Category '$trimmed' added successfully!"
                                            isError = false
                                            newCategoryInput = ""
                                        } else {
                                            actionMessage = "Category '$trimmed' already exists."
                                            isError = true
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                enabled = newCategoryInput.isNotBlank(),
                                modifier = Modifier.testTag("add_category_confirm_button")
                            ) {
                                Text("+ Add", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                actionMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isError) Color(0xFFFEE2E2) else Color(0xFFDCFCE7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = msg,
                            color = if (isError) Color(0xFFDC2626) else EmeraldSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Current Categories List
                Text("Existing Categories (${categories.size}):", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                categories.forEach { cat ->
                    val count = challenges.count { it.category.equals(cat, ignoreCase = true) }
                    val isGeneral = cat.equals("General", ignoreCase = true)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("category_card_${cat.lowercase()}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🏷️", fontSize = 14.sp)
                                Column {
                                    Text(cat, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("$count challenge${if (count != 1) "s" else ""}", fontSize = 11.sp, color = MutedGray)
                                }
                            }

                            if (!isGeneral) {
                                IconButton(
                                    onClick = {
                                        val removed = viewModel.deleteMissingLettersCategory(cat)
                                        if (removed) {
                                            actionMessage = "Deleted '$cat'. Challenges moved to 'General'."
                                            isError = false
                                        }
                                    },
                                    modifier = Modifier.testTag("delete_category_${cat.lowercase()}")
                                ) {
                                    Icon(
                                        Icons.Filled.Delete,
                                        contentDescription = "Delete $cat",
                                        tint = CoralAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        "Default",
                                        fontSize = 10.sp,
                                        color = MutedGray,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss, modifier = Modifier.testTag("close_categories_dialog")) {
                Text("Done")
            }
        }
    )
}

// -------------------------------------------------------------
// BULK IMPORT MISSING LETTERS DIALOG
// -------------------------------------------------------------
@Composable
fun BulkImportMissingLettersDialog(
    onDismiss: () -> Unit,
    onImported: (count: Int) -> Unit,
    viewModel: MainViewModel
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val availableCategories by viewModel.missingLettersCategories.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = File Upload (.xlsx/.csv), 1 = Paste Sheet Data
    var selectedDefaultCategory by remember { mutableStateOf(availableCategories.firstOrNull() ?: "General") }
    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var rawInputText by remember { mutableStateOf("") }
    var parsedRows by remember { mutableStateOf<List<ParsedMissingLetterRow>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            errorMessage = null
            successMessage = null
            try {
                var fileName: String? = null
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            fileName = cursor.getString(nameIndex)
                        }
                    }
                }
                val resolvedName = fileName ?: uri.lastPathSegment ?: "missing_letters.xlsx"
                selectedFileName = resolvedName

                val rows = ExcelWordSheetParser.parseMissingLettersFromUri(
                    context = context,
                    uri = uri,
                    fileName = resolvedName,
                    defaultCategory = selectedDefaultCategory
                )

                if (rows.isEmpty()) {
                    errorMessage = "No valid word challenges found in '$resolvedName'. Format required: Word, Meaning/Clue Hint, Category"
                    parsedRows = emptyList()
                } else {
                    parsedRows = rows
                    errorMessage = null
                }
            } catch (e: Exception) {
                errorMessage = "Failed to parse file: ${e.localizedMessage ?: "Unknown error"}"
            } finally {
                isProcessing = false
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bulk_import_missing_letters_dialog"),
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
                            text = "Bulk Excel Sheet Import",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Missing Letters Challenges",
                            fontSize = 11.sp,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = MutedGray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tab Selector: Upload File vs Paste Sheet Data
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = RoyalBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("📁 Upload .xlsx / .csv", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("📋 Paste / Template", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                // Default Category Selector
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Target Category (if unspecified in sheet):", fontSize = 11.sp, color = MutedGray)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        availableCategories.forEach { cat ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedDefaultCategory.equals(cat, true)) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    selectedDefaultCategory = cat
                                    if (parsedRows.isNotEmpty()) {
                                        parsedRows = parsedRows.map { row ->
                                            if (row.category.isBlank() || row.category == "General") row.copy(category = cat) else row
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedDefaultCategory.equals(cat, true)) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                if (selectedTab == 0) {
                    // File Upload Area
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.5.dp, if (selectedFileName != null) EmeraldSuccess else RoyalBlue.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { filePickerLauncher.launch("*/*") }
                            .testTag("choose_excel_file_card")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(if (selectedFileName != null) "📄" else "📥", fontSize = 32.sp)
                            Text(
                                text = selectedFileName ?: "Tap to Select Excel (.xlsx) or CSV File",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Columns: Word, Meaning/Clue Hint, Category",
                                fontSize = 11.sp,
                                color = MutedGray,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { filePickerLauncher.launch("*/*") },
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text("Browse Device Files", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    // Paste Raw Text or Load Sample Template
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Paste CSV / Excel rows:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            TextButton(
                                onClick = {
                                    val sample = ExcelWordSheetParser.getSampleMissingLettersCsv()
                                    rawInputText = sample
                                    parsedRows = ExcelWordSheetParser.parseMissingLettersFromText(sample, selectedDefaultCategory)
                                },
                                modifier = Modifier.testTag("load_sample_template_button")
                            ) {
                                Text("💡 Load Sample Template", fontSize = 11.sp, color = RoyalBlue)
                            }
                        }

                        OutlinedTextField(
                            value = rawInputText,
                            onValueChange = {
                                rawInputText = it
                                if (it.isNotBlank()) {
                                    parsedRows = ExcelWordSheetParser.parseMissingLettersFromText(it, selectedDefaultCategory)
                                }
                            },
                            placeholder = {
                                Text(
                                    "Word,Meaning / Clue Hint,Category\nELEPHANT,The largest land animal,Animals\nBROCCOLI,Green cabbage vegetable,Vegetables",
                                    fontSize = 11.sp,
                                    color = MutedGray
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .testTag("paste_excel_text_field"),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                        )
                    }
                }

                if (isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Parsing spreadsheet rows...", fontSize = 12.sp, color = MutedGray)
                    }
                }

                errorMessage?.let { err ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEE2E2),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "⚠️ $err",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                successMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✅ $msg",
                            color = EmeraldSuccess,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Parsed preview
                if (parsedRows.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Previewing ${parsedRows.size} Challenges:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = EmeraldSuccess
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = EmeraldSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "${parsedRows.size} ready",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldSuccess,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                itemsIndexed(parsedRows) { idx, row ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        "${idx + 1}. ${row.word}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Surface(
                                                        shape = RoundedCornerShape(4.dp),
                                                        color = RoyalBlue.copy(alpha = 0.12f)
                                                    ) {
                                                        Text(
                                                            row.category,
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = RoyalBlue,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    row.hint,
                                                    fontSize = 11.sp,
                                                    color = MutedGray,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
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
                    if (parsedRows.isNotEmpty()) {
                        val author = currentUser.name.ifBlank { "Faculty Instructor" }
                        val rowsToImport = parsedRows.map { Triple(it.word, it.hint, it.category) }
                        val count = viewModel.bulkImportMissingLetters(rowsToImport, author)
                        successMessage = "Successfully imported $count challenges!"
                        onImported(count)
                    }
                },
                enabled = parsedRows.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("execute_bulk_import_button")
            ) {
                Text(
                    text = if (parsedRows.isNotEmpty()) "Import All (${parsedRows.size}) 🚀" else "Import Challenges",
                    fontWeight = FontWeight.Bold
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

// -------------------------------------------------------------
// UNIVERSAL CREATE CHALLENGE DIALOG
// -------------------------------------------------------------
@Composable
fun CreateGameChallengeDialog(
    category: GameCategoryFilter,
    authorName: String,
    onDismiss: () -> Unit,
    onCreated: () -> Unit,
    viewModel: MainViewModel,
    onOpenBulkImport: (() -> Unit)? = null
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
                        val missingLettersCategories by viewModel.missingLettersCategories.collectAsState()
                        var isAddingCategory by remember { mutableStateOf(false) }
                        var newCatName by remember { mutableStateOf("") }

                        OutlinedTextField(
                            value = field1,
                            onValueChange = { field1 = it },
                            label = { Text("Full Word (e.g. ELEPHANT, BROCCOLI)") },
                            modifier = Modifier.fillMaxWidth().testTag("missing_letters_word_input")
                        )
                        OutlinedTextField(
                            value = field2,
                            onValueChange = { field2 = it },
                            label = { Text("Meaning / Clue Hint") },
                            modifier = Modifier.fillMaxWidth().testTag("missing_letters_hint_input")
                        )
                        OutlinedTextField(
                            value = field3,
                            onValueChange = { field3 = it },
                            label = { Text("Category (e.g. Animals, Vegetables, Fruits)") },
                            modifier = Modifier.fillMaxWidth().testTag("missing_letters_category_input")
                        )

                        // Categories management header with Add Category button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Categories:", fontSize = 11.sp, color = MutedGray, fontWeight = FontWeight.SemiBold)
                            TextButton(
                                onClick = { isAddingCategory = !isAddingCategory },
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                modifier = Modifier.testTag("toggle_add_category_btn")
                            ) {
                                Text(if (isAddingCategory) "✕ Close" else "+ Add Category", fontSize = 11.sp, color = RoyalBlue)
                            }
                        }

                        // Inline add category input
                        if (isAddingCategory) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedTextField(
                                    value = newCatName,
                                    onValueChange = { newCatName = it },
                                    placeholder = { Text("Category name...", fontSize = 11.sp) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f).testTag("inline_category_input"),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Button(
                                    onClick = {
                                        val trimmed = newCatName.trim()
                                        if (trimmed.isNotBlank()) {
                                            viewModel.addMissingLettersCategory(trimmed)
                                            field3 = trimmed
                                            newCatName = ""
                                            isAddingCategory = false
                                        }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    enabled = newCatName.isNotBlank(),
                                    modifier = Modifier.testTag("inline_save_category_btn")
                                ) {
                                    Text("Add", fontSize = 11.sp)
                                }
                            }
                        }

                        // Categories horizontal chips with active selection and delete icon
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            missingLettersCategories.forEach { cat ->
                                val isSelected = field3.equals(cat, ignoreCase = true)
                                val isGeneral = cat.equals("General", ignoreCase = true)

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clickable { field3 = cat }
                                        .testTag("suggested_category_${cat.lowercase()}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 8.dp, end = if (!isGeneral) 4.dp else 8.dp, top = 4.dp, bottom = 4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = cat,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                        if (!isGeneral) {
                                            Icon(
                                                Icons.Filled.Close,
                                                contentDescription = "Delete category $cat",
                                                tint = if (isSelected) Color.White.copy(alpha = 0.8f) else CoralAccent,
                                                modifier = Modifier
                                                    .size(14.dp)
                                                    .clickable {
                                                        viewModel.deleteMissingLettersCategory(cat)
                                                        if (field3.equals(cat, ignoreCase = true)) {
                                                            field3 = "General"
                                                        }
                                                    }
                                                    .testTag("delete_category_chip_${cat.lowercase()}")
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Direct Bulk Excel Sheet Import callout
                        if (onOpenBulkImport != null) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF059669).copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenBulkImport() }
                                    .testTag("dialog_open_bulk_import_btn")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text("📊", fontSize = 16.sp)
                                        Column {
                                            Text(
                                                "Bulk Excel Sheet Import",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = Color(0xFF059669)
                                            )
                                            Text(
                                                "Upload .xlsx / .csv or paste rows",
                                                fontSize = 10.sp,
                                                color = MutedGray
                                            )
                                        }
                                    }
                                    Text(
                                        "Open Import →",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF059669)
                                    )
                                }
                            }
                        }
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
                    GameCategoryFilter.PICTURE_VOCAB -> {
                        OutlinedTextField(
                            value = field1,
                            onValueChange = { field1 = it },
                            label = { Text("Target Word (e.g. Microscope, Satellite)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = field2,
                                onValueChange = { field2 = it },
                                label = { Text("Visual Emoji (e.g. 🔬, 🛰️)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = field3,
                                onValueChange = { field3 = it },
                                label = { Text("Phonetic (e.g. /ˈmaɪ.krə.skəʊp/)") },
                                modifier = Modifier.weight(1.5f)
                            )
                        }
                        OutlinedTextField(
                            value = field4,
                            onValueChange = { field4 = it },
                            label = { Text("Clue / Hint (Definition for student)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = field5,
                            onValueChange = { field5 = it },
                            label = { Text("Quiz Options (comma-separated, e.g. Microscope, Telescope, Lens, Camera)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    GameCategoryFilter.ENGLISH_ADVENTURE -> {
                        OutlinedTextField(
                            value = field1,
                            onValueChange = { field1 = it },
                            label = { Text("Game / Level Title (e.g. Sunken Temple Explorer)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = field2,
                            onValueChange = { field2 = it },
                            label = { Text("Adventure Description (e.g. Master marine adjectives)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = field3,
                            onValueChange = { field3 = it },
                            label = { Text("Target Speaking Sentence / Phrase") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = field4,
                                onValueChange = { field4 = it },
                                label = { Text("Icon Emoji (e.g. 🏛️, ⚓, 🗼)") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = field5,
                                onValueChange = { field5 = it },
                                label = { Text("XP Reward (e.g. 80)") },
                                modifier = Modifier.weight(1f)
                            )
                        }
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
                            viewModel.addMissingLettersChallenge(
                                fullWord = field1,
                                hint = field2.ifBlank { "English Vocabulary Word" },
                                category = field3.ifBlank { "General" },
                                createdBy = authorName
                            )
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
                    GameCategoryFilter.PICTURE_VOCAB -> {
                        if (field1.isNotBlank()) {
                            val opts = field5.split(",")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                                .let { list ->
                                    if (!list.contains(field1.trim())) (list + field1.trim()) else list
                                }
                                .ifEmpty { listOf(field1.trim(), "Option B", "Option C", "Option D") }
                            viewModel.addPictureVocabChallenge(
                                word = field1.trim(),
                                emoji = field2.ifBlank { "🖼️" }.trim(),
                                phonetic = field3.ifBlank { "/${field1.trim().lowercase()}/" }.trim(),
                                clue = field4.ifBlank { "Visual object recognition challenge" }.trim(),
                                options = opts,
                                createdBy = authorName
                            )
                            onCreated()
                        }
                    }
                    GameCategoryFilter.ENGLISH_ADVENTURE -> {
                        if (field1.isNotBlank()) {
                            val targetWorldId = "world_vocab"
                            val xp = field5.toIntOrNull() ?: 80
                            val newLvl = viewModel.addAdventureLevel(
                                worldId = targetWorldId,
                                title = field1.trim(),
                                description = field2.ifBlank { "Explore and conquer this spoken English quest." },
                                emoji = field4.ifBlank { "⭐" },
                                xp = xp,
                                coins = (xp / 2).coerceAtLeast(20)
                            )
                            if (newLvl != null && field3.isNotBlank()) {
                                viewModel.addAdventureMission(
                                    worldId = targetWorldId,
                                    levelId = newLvl.id,
                                    title = "${field1.trim()} Speaking Challenge",
                                    description = "Speak this sentence with clear pronunciation.",
                                    type = AdventureMissionType.SPEAKING_CHALLENGE,
                                    targetSentence = field3.trim(),
                                    options = emptyList(),
                                    correctAnswer = field3.trim(),
                                    clue = "Listen closely and enunciate clearly.",
                                    xp = xp / 2,
                                    coins = 20
                                )
                            }
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
fun TeacherPortraitAvatar(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.sweepGradient(
                    listOf(
                        Color(0xFFEC4899),
                        Color(0xFF818CF8),
                        Color(0xFF06B6D4),
                        Color(0xFFEC4899)
                    )
                )
            )
            .padding(2.5.dp)
            .clip(CircleShape)
            .background(Color(0xFF1E1B4B)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(2.dp)) {
            val w = size.width
            val h = size.height

            // Soft circular vignette background
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(Color(0xFF3B82F6), Color(0xFF1E1B4B)),
                    center = Offset(w * 0.5f, h * 0.4f),
                    radius = w * 0.5f
                )
            )

            // Inner golden rim
            drawCircle(
                color = Color(0xFFFBBF24).copy(alpha = 0.65f),
                radius = w * 0.48f,
                style = Stroke(width = 1.5f)
            )

            // Body / Blue blazer
            val bodyPath = Path().apply {
                moveTo(w * 0.20f, h)
                cubicTo(w * 0.20f, h * 0.72f, w * 0.80f, h * 0.72f, w * 0.80f, h)
                close()
            }
            drawPath(bodyPath, Color(0xFF2563EB))

            // White shirt collar
            val shirtPath = Path().apply {
                moveTo(w * 0.42f, h * 0.68f)
                lineTo(w * 0.50f, h * 0.82f)
                lineTo(w * 0.58f, h * 0.68f)
                close()
            }
            drawPath(shirtPath, Color.White)

            // Red folder/book held in left arm
            val bookPath = Path().apply {
                moveTo(w * 0.58f, h * 0.82f)
                lineTo(w * 0.84f, h * 0.82f)
                lineTo(w * 0.84f, h)
                lineTo(w * 0.58f, h)
                close()
            }
            drawPath(bookPath, Color(0xFFE11D48))

            // Neck
            drawRect(
                color = Color(0xFFFED7AA),
                topLeft = Offset(w * 0.44f, h * 0.58f),
                size = Size(w * 0.12f, h * 0.14f)
            )

            // Hair (back)
            drawCircle(
                color = Color(0xFF5A2A18),
                radius = w * 0.28f,
                center = Offset(w * 0.5f, h * 0.44f)
            )

            // Face
            drawCircle(
                color = Color(0xFFFED7AA),
                radius = w * 0.21f,
                center = Offset(w * 0.5f, h * 0.44f)
            )

            // Hair bangs/sides
            val hairLeft = Path().apply {
                moveTo(w * 0.30f, h * 0.46f)
                cubicTo(w * 0.28f, h * 0.30f, w * 0.44f, h * 0.24f, w * 0.50f, h * 0.30f)
                cubicTo(w * 0.40f, h * 0.34f, w * 0.34f, h * 0.40f, w * 0.30f, h * 0.46f)
                close()
            }
            drawPath(hairLeft, Color(0xFF5A2A18))

            val hairRight = Path().apply {
                moveTo(w * 0.70f, h * 0.46f)
                cubicTo(w * 0.72f, h * 0.30f, w * 0.56f, h * 0.24f, w * 0.50f, h * 0.30f)
                cubicTo(w * 0.60f, h * 0.34f, w * 0.66f, h * 0.40f, w * 0.70f, h * 0.46f)
                close()
            }
            drawPath(hairRight, Color(0xFF5A2A18))

            // Spectacles / Glasses (round frames)
            val glassY = h * 0.43f
            val glassR = w * 0.08f
            drawCircle(
                color = Color(0xFF1E293B),
                radius = glassR,
                center = Offset(w * 0.41f, glassY),
                style = Stroke(width = 2f)
            )
            drawCircle(
                color = Color(0xFF1E293B),
                radius = glassR,
                center = Offset(w * 0.59f, glassY),
                style = Stroke(width = 2f)
            )
            drawLine(
                color = Color(0xFF1E293B),
                start = Offset(w * 0.49f, glassY),
                end = Offset(w * 0.51f, glassY),
                strokeWidth = 2f
            )

            // Friendly smile
            val smilePath = Path().apply {
                moveTo(w * 0.44f, h * 0.55f)
                cubicTo(w * 0.47f, h * 0.58f, w * 0.53f, h * 0.58f, w * 0.56f, h * 0.55f)
            }
            drawPath(smilePath, Color(0xFFE11D48), style = Stroke(width = 2f))
        }
    }
}

@Composable
fun TeacherGraduationBooksIllustration(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Book Stack at bottom
        // Book 1 (Bottom Book - Cyan & Light Gray)
        val b1Y = h * 0.72f
        val b1H = h * 0.12f
        val spine1 = Path().apply {
            moveTo(w * 0.18f, b1Y)
            lineTo(w * 0.52f, b1Y + b1H * 0.75f)
            lineTo(w * 0.52f, b1Y + b1H * 1.75f)
            lineTo(w * 0.18f, b1Y + b1H)
            close()
        }
        drawPath(spine1, Color(0xFF0284C7))

        val pages1 = Path().apply {
            moveTo(w * 0.52f, b1Y + b1H * 0.75f)
            lineTo(w * 0.88f, b1Y + b1H * 0.2f)
            lineTo(w * 0.88f, b1Y + b1H * 1.2f)
            lineTo(w * 0.52f, b1Y + b1H * 1.75f)
            close()
        }
        drawPath(pages1, Color(0xFFF1F5F9))

        // Book 2 (Middle Book - Vibrant Magenta/Pink & White)
        val b2Y = h * 0.58f
        val b2H = h * 0.11f
        val spine2 = Path().apply {
            moveTo(w * 0.22f, b2Y)
            lineTo(w * 0.56f, b2Y + b2H * 0.75f)
            lineTo(w * 0.56f, b2Y + b2H * 1.75f)
            lineTo(w * 0.22f, b2Y + b2H)
            close()
        }
        drawPath(spine2, Color(0xFFD946EF))

        val pages2 = Path().apply {
            moveTo(w * 0.56f, b2Y + b2H * 0.75f)
            lineTo(w * 0.90f, b2Y + b2H * 0.2f)
            lineTo(w * 0.90f, b2Y + b2H * 1.2f)
            lineTo(w * 0.56f, b2Y + b2H * 1.75f)
            close()
        }
        drawPath(pages2, Color(0xFFEDE9FE))

        // Book 3 (Top Book - Vibrant Indigo/Blue & White)
        val b3Y = h * 0.46f
        val b3H = h * 0.10f
        val spine3 = Path().apply {
            moveTo(w * 0.20f, b3Y)
            lineTo(w * 0.54f, b3Y + b3H * 0.75f)
            lineTo(w * 0.54f, b3Y + b3H * 1.75f)
            lineTo(w * 0.20f, b3Y + b3H)
            close()
        }
        drawPath(spine3, Color(0xFF4F46E5))

        val pages3 = Path().apply {
            moveTo(w * 0.54f, b3Y + b3H * 0.75f)
            lineTo(w * 0.86f, b3Y + b3H * 0.2f)
            lineTo(w * 0.86f, b3Y + b3H * 1.2f)
            lineTo(w * 0.54f, b3Y + b3H * 1.75f)
            close()
        }
        drawPath(pages3, Color(0xFFE2E8F0))

        // Mortarboard Cap (Graduation Hat) resting on top book
        val capBand = Path().apply {
            moveTo(w * 0.40f, h * 0.38f)
            lineTo(w * 0.68f, h * 0.38f)
            lineTo(w * 0.65f, h * 0.48f)
            lineTo(w * 0.43f, h * 0.48f)
            close()
        }
        drawPath(capBand, Color(0xFF1E1B4B))

        val diamondTop = Path().apply {
            moveTo(w * 0.54f, h * 0.12f)
            lineTo(w * 0.92f, h * 0.25f)
            lineTo(w * 0.52f, h * 0.38f)
            lineTo(w * 0.14f, h * 0.25f)
            close()
        }
        drawPath(
            diamondTop,
            Brush.linearGradient(
                listOf(Color(0xFF38BDF8), Color(0xFF4F46E5), Color(0xFF1E1B4B)),
                start = Offset(w * 0.14f, h * 0.12f),
                end = Offset(w * 0.92f, h * 0.38f)
            )
        )
        drawPath(
            diamondTop,
            Color(0xFF93C5FD).copy(alpha = 0.8f),
            style = Stroke(width = 1.8f)
        )

        drawCircle(
            color = Color(0xFFFBBF24),
            radius = 3.5f,
            center = Offset(w * 0.53f, h * 0.25f)
        )

        val tasselCord = Path().apply {
            moveTo(w * 0.53f, h * 0.25f)
            cubicTo(
                w * 0.70f, h * 0.22f,
                w * 0.86f, h * 0.24f,
                w * 0.92f, h * 0.26f
            )
            lineTo(w * 0.93f, h * 0.46f)
        }
        drawPath(
            tasselCord,
            Color(0xFFFBBF24),
            style = Stroke(width = 2.2f)
        )

        val tasselFringe = Path().apply {
            moveTo(w * 0.90f, h * 0.44f)
            lineTo(w * 0.96f, h * 0.44f)
            lineTo(w * 0.95f, h * 0.54f)
            lineTo(w * 0.89f, h * 0.54f)
            close()
        }
        drawPath(tasselFringe, Color(0xFFF59E0B))
    }
}

@Composable
fun TeacherMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    tag: String = "Active",
    growth: String = "+0% vs last week",
    watermarkIcon: ImageVector? = null
) {
    val cardBg = when (color) {
        Color(0xFF38BDF8) -> listOf(Color(0xFF071A3C), Color(0xFF030D20))
        Color(0xFFF59E0B) -> listOf(Color(0xFF2E1303), Color(0xFF180A02))
        Color(0xFF10B981) -> listOf(Color(0xFF032619), Color(0xFF02160E))
        Color(0xFFA855F7) -> listOf(Color(0xFF1E0A3C), Color(0xFF100524))
        else -> listOf(Color(0xFF0F1535), Color(0xFF070B1F))
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.85f)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(cardBg))
        ) {
            KpiCardFluidWave(
                color = color,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .align(Alignment.BottomCenter)
            )

            Icon(
                imageVector = watermarkIcon ?: icon,
                contentDescription = null,
                tint = color.copy(alpha = 0.28f),
                modifier = Modifier
                    .size(38.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-10).dp, y = (-10).dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Top Row: Circular Icon on Left, Pill on Right
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
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF070B1F).copy(alpha = 0.85f),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.75f))
                    ) {
                        Text(
                            text = tag,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.5.dp),
                            maxLines = 1
                        )
                    }
                }

                // Middle Content: Big Number on top, Label below it (matching screenshot!)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = value,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFCBD5E1),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Bottom row: growth trend
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
fun TeacherQuickOperationCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    gradient: List<Color>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.85f)),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradient))
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(color, color.copy(alpha = 0.7f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.25f))
                            .border(1.dp, color.copy(alpha = 0.6f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.5.sp,
                        color = color.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
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
    val cardBorder = when (color) {
        RoyalBlue -> NeonBlueBorder
        PurpleAccent -> NeonPurpleBorder
        EmeraldSuccess -> NeonGreenBorder
        AmberGold -> NeonAmberBorder
        else -> color
    }
    val iconGrad = when (color) {
        RoyalBlue -> NeonBlueGrad
        PurpleAccent -> NeonPurpleGrad
        EmeraldSuccess -> NeonGreenGrad
        AmberGold -> NeonAmberGrad
        else -> Brush.linearGradient(listOf(color, color))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.2.dp, cardBorder.copy(alpha = 0.45f)),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Text(icon, fontSize = 22.sp)
                }

                Surface(
                    shape = CircleShape,
                    color = Color(0xFF070B1F),
                    border = BorderStroke(1.dp, cardBorder.copy(alpha = 0.4f)),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            tint = cardBorder,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                Text(subtitle, fontSize = 11.sp, color = Color(0xFF94A3B8), maxLines = 1)
            }
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
        Text(area, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.White, modifier = Modifier.weight(1f))
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
            Text(label, fontSize = 12.sp, color = Color.White)
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

@Composable
fun TeacherAiConnectionCard(
    isConfigured: Boolean,
    activeModel: String,
    apiKeySource: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderBrush = if (isConfigured) {
        Brush.linearGradient(
            listOf(Color(0xFF38BDF8), Color(0xFF818CF8), Color(0xFF34D399))
        )
    } else {
        Brush.linearGradient(
            listOf(Color(0xFFFBBF24), Color(0xFFF97316), Color(0xFFEC4899))
        )
    }

    val bgBrush = if (isConfigured) {
        Brush.linearGradient(
            listOf(Color(0xFF0B1C40), Color(0xFF121847), Color(0xFF08122E))
        )
    } else {
        Brush.linearGradient(
            listOf(Color(0xFF2E1608), Color(0xFF211026), Color(0xFF120A1E))
        )
    }

    val medallionBrush = if (isConfigured) {
        Brush.linearGradient(listOf(Color(0xFF0EA5E9), Color(0xFF6366F1)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C)))
    }

    val actionBrush = if (isConfigured) {
        Brush.horizontalGradient(listOf(Color(0xFF0284C7), Color(0xFF4F46E5)))
    } else {
        Brush.horizontalGradient(listOf(Color(0xFFD97706), Color(0xFFEA580C)))
    }

    val glowPrimary = if (isConfigured) Color(0xFF38BDF8) else Color(0xFFF59E0B)
    val glowSecondary = if (isConfigured) Color(0xFF34D399) else Color(0xFFEC4899)

    val compactSource = when {
        apiKeySource.contains("Secrets", ignoreCase = true) -> "Secrets Key"
        apiKeySource.contains("Saved", ignoreCase = true) -> "Saved Key"
        apiKeySource.contains("Proxy", ignoreCase = true) || apiKeySource.contains("Custom", ignoreCase = true) -> "Custom Endpoint"
        else -> apiKeySource
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.3.dp, borderBrush),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("teacher_gemini_api_card")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgBrush)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val w = size.width
                val h = size.height
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowPrimary.copy(alpha = 0.24f), Color.Transparent),
                        center = Offset(w * 0.12f, h * 0.5f),
                        radius = h * 1.35f
                    ),
                    center = Offset(w * 0.12f, h * 0.5f),
                    radius = h * 1.35f
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glowSecondary.copy(alpha = 0.16f), Color.Transparent),
                        center = Offset(w * 0.92f, h * 0.15f),
                        radius = h * 1.2f
                    ),
                    center = Offset(w * 0.92f, h * 0.15f),
                    radius = h * 1.2f
                )
                val sheen = Path().apply {
                    moveTo(w * 0.36f, 0f)
                    lineTo(w * 0.54f, 0f)
                    lineTo(w * 0.42f, h)
                    lineTo(w * 0.24f, h)
                    close()
                }
                drawPath(sheen, Color.White.copy(alpha = 0.03f))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(medallionBrush)
                        .border(1.dp, Color.White.copy(alpha = 0.28f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isConfigured) Icons.Rounded.Bolt else Icons.Rounded.VpnKey,
                        contentDescription = if (isConfigured) "AI Speech Connected" else "Connect AI Key",
                        tint = Color.White,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Gemini Speech API",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isConfigured) Color(0xFF064E3B).copy(alpha = 0.85f) else Color(0xFF78350F).copy(alpha = 0.85f),
                            border = BorderStroke(
                                0.8.dp,
                                if (isConfigured) Color(0xFF34D399) else Color(0xFFFBBF24)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.5.dp)
                                        .clip(CircleShape)
                                        .background(if (isConfigured) Color(0xFF34D399) else Color(0xFFFBBF24))
                                )
                                Text(
                                    text = if (isConfigured) "LIVE" else "SETUP",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = if (isConfigured) Color(0xFFA7F3D0) else Color(0xFFFEF3C7),
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Text(
                        text = if (isConfigured) "$activeModel • $compactSource" else "Link API key for Nova AI voice",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isConfigured) Color(0xFF93C5FD) else Color(0xFFFDE68A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .background(actionBrush)
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isConfigured) Icons.Rounded.Tune else Icons.Rounded.Key,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isConfigured) "Manage" else "Connect",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

