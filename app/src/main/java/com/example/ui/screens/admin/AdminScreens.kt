package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LearningLevel
import com.example.data.model.User
import com.example.ui.AdminTab
import com.example.ui.MainViewModel
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
        AdminTab.TEACHERS -> AdminTeachersManagement(viewModel, modifier)
        AdminTab.CURRICULUM -> AdminCurriculumManagement(viewModel, modifier)
        AdminTab.ANALYTICS -> AdminAnalytics(viewModel, modifier)
        AdminTab.SETTINGS -> AdminSettings(viewModel, modifier)
    }
}

@Composable
fun AdminDashboard(viewModel: MainViewModel, modifier: Modifier = Modifier) {
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
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEF3C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛡️", fontSize = 32.sp)
                    }
                    Column {
                        Text("Dean Arthur Pendelton", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Global Academic Administrator • Speakly", fontSize = 12.sp, color = MutedGray)
                    }
                }
            }
        }

        // Executive KPI Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Institution KPI Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Total Students", "128", "🎓", RoyalBlue, Modifier.weight(1f))
                    AdminKpiCard("Faculty Teachers", "12", "👨‍🏫", PurpleAccent, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Daily Active Users", "94", "⚡", EmeraldSuccess, Modifier.weight(1f))
                    AdminKpiCard("Avg Speaking Score", "84.2%", "🎙️", AmberGold, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    AdminKpiCard("Completed Lessons", "1,480", "📚", CyanAccent, Modifier.weight(1f))
                    AdminKpiCard("Voice Practice Min", "3,240m", "⏱️", CoralAccent, Modifier.weight(1f))
                }
            }
        }

        // Quick System Actions
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Administration Quick Actions", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { viewModel.selectAdminTab(AdminTab.STUDENTS) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Import CSV", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.selectAdminTab(AdminTab.CURRICULUM) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Edit Courses", fontSize = 11.sp)
                        }
                        Button(
                            onClick = { viewModel.selectAdminTab(AdminTab.ANALYTICS) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Export PDF", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStudentsManagement(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val students by viewModel.adminStudents.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newStudentName by remember { mutableStateOf("") }
    var newStudentEmail by remember { mutableStateOf("") }
    var newStudentBatch by remember { mutableStateOf("Batch Alpha") }
    var importStatusMsg by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("admin_students_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Student Roster Management", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("${students.size} enrolled students", fontSize = 12.sp, color = MutedGray)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.repository.bulkImportStudents(3)
                            importStatusMsg = "Successfully imported 3 students from CSV spreadsheet!"
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.FileUpload, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("CSV Bulk", fontSize = 11.sp)
                    }

                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add", fontSize = 11.sp)
                    }
                }
            }
        }

        importStatusMsg?.let { msg ->
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

        items(students) { s ->
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
                    Text(s.avatarEmoji, fontSize = 28.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(s.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${s.email} • ${s.batch}", fontSize = 11.sp, color = MutedGray)
                        Text("ID: ${s.id} • Joined: ${s.joinedDate}", fontSize = 10.sp, color = RoyalBlue)
                    }

                    Switch(
                        checked = s.isActive,
                        onCheckedChange = { viewModel.repository.toggleStudentActive(s.id) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Individual Student") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newStudentName,
                        onValueChange = { newStudentName = it },
                        label = { Text("Full Name") }
                    )
                    OutlinedTextField(
                        value = newStudentEmail,
                        onValueChange = { newStudentEmail = it },
                        label = { Text("Email Address") }
                    )
                    OutlinedTextField(
                        value = newStudentBatch,
                        onValueChange = { newStudentBatch = it },
                        label = { Text("Class / Batch") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newStudentName.isNotBlank() && newStudentEmail.isNotBlank()) {
                        viewModel.repository.addStudent(newStudentName, newStudentEmail, newStudentBatch)
                        newStudentName = ""
                        newStudentEmail = ""
                        showAddDialog = false
                    }
                }) {
                    Text("Create Student Account")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun AdminTeachersManagement(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val teachers by viewModel.adminTeachers.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Faculty & Instructor Staff", fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                        Text("Assigned: ${t.batch}", fontSize = 11.sp, color = RoyalBlue)
                    }
                    IconButton(onClick = { }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Options")
                    }
                }
            }
        }
    }
}

@Composable
fun AdminCurriculumManagement(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val levels = LearningLevel.values()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Course Curriculum & Levels (A1 to C2)", fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = RoyalBlue
                        ) {
                            Text(lvl.cefr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                        Column {
                            Text(lvl.displayName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("18 Lessons • 42 Speaking Activities", fontSize = 12.sp, color = MutedGray)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text("Published", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminAnalytics(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    var exportToast by remember { mutableStateOf<String?>(null) }

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
                Text("Institutional Performance Reports", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Button(
                    onClick = { exportToast = "Report exported to Speakly_Report_Nov2026.pdf" },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Export", fontSize = 11.sp)
                }
            }
        }

        exportToast?.let { msg ->
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(msg, color = RoyalBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(10.dp))
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Speaking Proficiency Progression Trend", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    // Visual progress breakdown
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Month 1 (Baseline)", fontSize = 12.sp, color = MutedGray)
                        Text("64.5%", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    LinearProgressIndicator(progress = { 0.645f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = MutedGray)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Month 2 (Current)", fontSize = 12.sp, color = MutedGray)
                        Text("84.2% (+19.7%)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldSuccess)
                    }
                    LinearProgressIndicator(progress = { 0.842f }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = EmeraldSuccess)
                }
            }
        }
    }
}

@Composable
fun AdminSettings(viewModel: MainViewModel, modifier: Modifier = Modifier) {
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
            Text("Platform Configuration & Governance", fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                        Column {
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
                        Column {
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
                        Column {
                            Text("Supabase Cloud Database", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Connect external PostgreSQL tables & sync student data", fontSize = 11.sp, color = MutedGray)
                        }
                        Button(
                            onClick = { viewModel.toggleSupabaseModal(true) },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Connect", fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

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
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = color)
            Text(title, fontSize = 11.sp, color = MutedGray)
        }
    }
}
