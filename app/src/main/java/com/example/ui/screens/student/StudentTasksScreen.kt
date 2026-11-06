package com.example.ui.screens.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Assignment
import com.example.data.model.LiveClassSession
import com.example.data.model.StudentSubmission
import com.example.ui.MainViewModel
import com.example.ui.SpeakingSubTab
import com.example.ui.StudentTab
import com.example.ui.theme.*

@Composable
fun StudentTasksScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val assignments by viewModel.assignments.collectAsState()
    val submissions by viewModel.submissions.collectAsState()
    val liveClasses by viewModel.liveClasses.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedBatchFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") } // "All", "Pending", "Submitted"
    var searchQuery by remember { mutableStateOf("") }
    var taskToSubmit by remember { mutableStateOf<Assignment?>(null) }

    val studentClass = currentUser.studentClass

    // Map of submitted assignment IDs by this student
    val studentSubmissionsMap = remember(submissions, currentUser) {
        submissions.filter { it.studentName.equals(currentUser.name, ignoreCase = true) || it.studentName == "Alex Morgan" }
            .associateBy { it.assignmentId }
    }

    val filteredAssignments = remember(assignments, selectedBatchFilter, selectedStatusFilter, searchQuery, studentSubmissionsMap) {
        assignments.filter { asg ->
            val matchesBatch = when (selectedBatchFilter) {
                "All" -> true
                "My Class" -> asg.batch.equals(studentClass, ignoreCase = true) || asg.batch.equals("All Classes", ignoreCase = true)
                else -> asg.batch.contains(selectedBatchFilter, ignoreCase = true) || asg.batch.equals("All Classes", ignoreCase = true)
            }

            val hasSubmitted = studentSubmissionsMap.containsKey(asg.id)
            val matchesStatus = when (selectedStatusFilter) {
                "Pending" -> !hasSubmitted
                "Submitted" -> hasSubmitted
                else -> true
            }

            val matchesSearch = searchQuery.isBlank() ||
                asg.title.contains(searchQuery, ignoreCase = true) ||
                asg.instructions.contains(searchQuery, ignoreCase = true) ||
                asg.teacherName.contains(searchQuery, ignoreCase = true)

            matchesBatch && matchesStatus && matchesSearch
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberMidnightBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // --- Header Section ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth().testTag("student_tasks_header_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = CircleShape,
                                color = RoyalBlue.copy(alpha = 0.2f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("📝", fontSize = 18.sp)
                                }
                            }
                            Column {
                                Text(
                                    "Tasks from Teachers",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Speaking homework & assignments",
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "Enrolled: $studentClass",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF60A5FA),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Urgent summary banner
                    val pendingCount = assignments.count { !studentSubmissionsMap.containsKey(it.id) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0F172A))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (pendingCount > 0) "📌 $pendingCount Speaking tasks pending submission" else "🎉 All teacher tasks completed!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (pendingCount > 0) Color(0xFFFBBF24) else EmeraldSuccess
                        )
                        Text(
                            text = "Class Active",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8)
                        )
                    }
                }
            }
        }

        // --- Search & Filter Row ---
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search assignments by topic or teacher...", fontSize = 12.sp, color = Color(0xFF94A3B8)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = Color(0xFF38BDF8)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear", tint = Color.White)
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = CyberCardBg,
                    unfocusedContainerColor = CyberCardBg,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth().testTag("tasks_search_input"),
                singleLine = true
            )
        }

        // --- Class Batch Filter Chips ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Filter by Class / Batch:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFCBD5E1))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val batchOptions = listOf("All", "My Class", "Class 10", "Class 9", "Class 8", "Class 7", "Class 6")
                    batchOptions.forEach { opt ->
                        val isSelected = selectedBatchFilter == opt
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedBatchFilter = opt },
                            label = { Text(opt, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2563EB),
                                selectedLabelColor = Color.White,
                                containerColor = CyberCardBg,
                                labelColor = Color(0xFF94A3B8)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color(0xFF60A5FA) else Color(0xFF334155)
                            )
                        )
                    }
                }
            }
        }

        // --- Status Tabs: All, Pending, Submitted ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val statuses = listOf("All" to "All Tasks", "Pending" to "Pending ⏳", "Submitted" to "Completed ✅")
                statuses.forEach { (key, label) ->
                    val isSel = selectedStatusFilter == key
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSel) Color(0xFF1E3A8A) else CyberCardBg,
                        border = BorderStroke(1.dp, if (isSel) Color(0xFF60A5FA) else Color(0xFF334155)),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedStatusFilter = key }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSel) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }

        // --- Assignment Cards List ---
        if (filteredAssignments.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("✨", fontSize = 32.sp)
                        Text(
                            "No tasks found for the selected filter",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            "Try switching to 'All' or clearing your search term to see other classroom homework.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(filteredAssignments, key = { it.id }) { task ->
                val submission = studentSubmissionsMap[task.id]
                val isSubmitted = submission != null

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                    border = BorderStroke(
                        1.2.dp,
                        if (isSubmitted) EmeraldSuccess.copy(alpha = 0.5f) else Color(0xFF38BDF8).copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth().testTag("student_task_card_${task.id}")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Top badges row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF1D4ED8).copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        task.topicType,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF60A5FA),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF334155).copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        "Target: ${task.batch}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFCBD5E1),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Text(
                                "Due: ${task.dueDate}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrangeVibrant
                            )
                        }

                        // Task Title
                        Text(
                            text = task.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )

                        // Instructions
                        Text(
                            text = task.instructions,
                            fontSize = 12.5.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )

                        // Teacher info & Points
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Assigned by ${task.teacherName}",
                                fontSize = 11.sp,
                                color = Color(0xFF93C5FD),
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Max: ${task.maxPoints} pts",
                                fontSize = 11.sp,
                                color = AmberGold,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // If already submitted, show feedback/score
                        if (submission != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0F172A),
                                border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.4f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "✅ Submitted & Graded",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = EmeraldSuccess
                                        )
                                        submission.score?.let {
                                            Text(
                                                "Score: $it/100",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.sp,
                                                color = Color(0xFFFBBF24)
                                            )
                                        }
                                    }
                                    submission.teacherFeedback?.let { feedback ->
                                        Text(
                                            "Feedback: \"$feedback\"",
                                            fontSize = 11.sp,
                                            color = Color(0xFFCBD5E1),
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.selectSpeakingSubTab(SpeakingSubTab.AI_PARTNER)
                                    viewModel.selectStudentTab(StudentTab.PRACTICE)
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Practice Task", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { taskToSubmit = task },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSubmitted) Color(0xFF1E293B) else RoyalBlue
                                ),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    if (isSubmitted) Icons.Filled.CheckCircle else Icons.Filled.Send,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isSubmitted) EmeraldSuccess else Color.White
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    if (isSubmitted) "Resubmit" else "Submit Task",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Live Classroom Sessions Section ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("📹", fontSize = 16.sp)
                        Text(
                            "Live Teacher Sessions",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                    Text("Interactive Video", fontSize = 11.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.SemiBold)
                }

                liveClasses.forEach { lc ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                        border = BorderStroke(1.dp, Color(0xFF2563EB).copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    lc.scheduledTime,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (lc.status == "UPCOMING") Color(0xFF38BDF8) else Color(0xFF94A3B8)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (lc.status == "UPCOMING") EmeraldSuccess.copy(alpha = 0.2f) else Color(0xFF334155)
                                ) {
                                    Text(
                                        lc.status,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (lc.status == "UPCOMING") EmeraldSuccess else Color(0xFF94A3B8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(lc.title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                            Text("Instructor: ${lc.instructorName} • ${lc.durationMin} mins", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                        }
                    }
                }
            }
        }
    }

    // --- Submit Task Dialog Modal ---
    taskToSubmit?.let { task ->
        var studentAudioTranscript by remember {
            mutableStateOf(
                "Good day Professor. Here is my response to the assignment regarding ${task.title}. In my daily routine, I consistently practice speaking English with confidence, focusing on correct syntax, clear pronunciation, and natural conversational cadence."
            )
        }
        var isSimulatingRecording by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { taskToSubmit = null },
            containerColor = Color(0xFF131D3F),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFE2E8F0),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🎙️", fontSize = 24.sp)
                    Text("Submit Spoken Assignment", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        task.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp,
                        color = Color(0xFF38BDF8)
                    )
                    Text(
                        "Assigned by: ${task.teacherName} (Target: ${task.batch})",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Speech Transcript / Recording", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Button(
                                    onClick = { isSimulatingRecording = !isSimulatingRecording },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSimulatingRecording) Color(0xFFEF4444) else RoyalBlue
                                    ),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        if (isSimulatingRecording) Icons.Filled.Stop else Icons.Filled.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        if (isSimulatingRecording) "Recording..." else "Record Audio",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = studentAudioTranscript,
                                onValueChange = { studentAudioTranscript = it },
                                modifier = Modifier.fillMaxWidth().height(110.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp, color = Color.White),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF38BDF8),
                                    unfocusedBorderColor = Color(0xFF334155),
                                    focusedContainerColor = Color(0xFF0D152F),
                                    unfocusedContainerColor = Color(0xFF0D152F)
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Reward: +50 XP, +15 Coins", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AmberGold)
                        Text("Due: ${task.dueDate}", fontSize = 11.sp, color = OrangeVibrant)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitAssignment(
                            assignmentId = task.id,
                            transcript = studentAudioTranscript,
                            audioDurationSecs = 75
                        )
                        taskToSubmit = null
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("Turn In Assignment 🚀", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToSubmit = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}
