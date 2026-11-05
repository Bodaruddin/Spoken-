package com.example.ui.screens.teacher

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
import com.example.data.model.Assignment
import com.example.data.model.LiveClassSession
import com.example.data.model.StudentSubmission
import com.example.ui.MainViewModel
import com.example.ui.TeacherTab
import com.example.ui.theme.*

@Composable
fun TeacherMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTeacherTab.collectAsState()

    when (currentTab) {
        TeacherTab.DASHBOARD -> TeacherDashboard(viewModel, modifier)
        TeacherTab.STUDENTS -> TeacherStudentsList(viewModel, modifier)
        TeacherTab.SPEAKING_REVIEW -> TeacherSpeakingReview(viewModel, modifier)
        TeacherTab.ASSIGNMENTS -> TeacherAssignmentsList(viewModel, modifier)
        TeacherTab.LIVE_CLASSES -> TeacherLiveClasses(viewModel, modifier)
    }
}

@Composable
fun TeacherDashboard(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val submissions by viewModel.submissions.collectAsState()
    val liveClasses by viewModel.liveClasses.collectAsState()

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
                            .background(Color(0xFFEDE9FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👩‍🏫", fontSize = 32.sp)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Prof. Eleanor Vance", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        Text("Lead Spoken English Instructor • Batch Alpha", fontSize = 12.sp, color = MutedGray)
                    }
                }
            }
        }

        // Metrics Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TeacherMetricCard("Assigned Students", "24", "👥", RoyalBlue, Modifier.weight(1f))
                TeacherMetricCard("Pending Audio", "${submissions.count { it.status == "PENDING" }}", "🎧", OrangeVibrant, Modifier.weight(1f))
                TeacherMetricCard("Avg Speaking", "84%", "🎙️", EmeraldSuccess, Modifier.weight(1f))
            }
        }

        // Action Quick Access
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
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
                        Text("3 students waiting for pronunciation & fluency grading", fontSize = 12.sp, color = MutedGray)
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
                    Text("⚠️ Cohort Attention Areas", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    WeakAreaRow("Pronunciation: Voiced 'th' sounds (this, weather)", "6 students need drill")
                    WeakAreaRow("Grammar: Irregular past tense agreement", "4 students need review")
                    WeakAreaRow("Fluency: Extended pauses in impromptu speaking", "Average response pause: 3.4s")
                }
            }
        }
    }
}

@Composable
fun TeacherSpeakingReview(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val submissions by viewModel.submissions.collectAsState()
    var selectedSubmission by remember { mutableStateOf<StudentSubmission?>(submissions.firstOrNull()) }
    var fluencyScore by remember { mutableStateOf(85f) }
    var grammarScore by remember { mutableStateOf(88f) }
    var pronunciationScore by remember { mutableStateOf(82f) }
    var teacherFeedbackInput by remember { mutableStateOf("Excellent clarity and pace. Keep practicing the sentence rhythm!") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .testTag("teacher_speaking_review_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Student Voice Recordings to Grade", fontWeight = FontWeight.Bold, fontSize = 17.sp)
        }

        // Submission selector list
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                submissions.forEach { sub ->
                    val isSel = selectedSubmission?.id == sub.id
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surface,
                        border = if (isSel) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.clickable { selectedSubmission = sub }
                    ) {
                        Text(
                            text = "${sub.studentAvatar} ${sub.studentName}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
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
                                Text("Recorded: ${sub.submittedDate} • ${sub.audioDurationSeconds}s", fontSize = 11.sp, color = MutedGray)
                            }
                            Button(
                                onClick = { viewModel.speakAudio(sub.studentTranscript) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Play Audio")
                            }
                        }

                        // Speech Transcript
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Student Transcript:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                                Text("\"${sub.studentTranscript}\"", fontSize = 13.sp)
                            }
                        }

                        // Grading Sliders
                        Text("Teacher Evaluation Scores:", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                        GradingSlider("Fluency & Flow", fluencyScore) { fluencyScore = it }
                        GradingSlider("Pronunciation & Accent", pronunciationScore) { pronunciationScore = it }
                        GradingSlider("Grammar & Vocabulary", grammarScore) { grammarScore = it }

                        // Feedback Input
                        OutlinedTextField(
                            value = teacherFeedbackInput,
                            onValueChange = { teacherFeedbackInput = it },
                            label = { Text("Personalized Improvement Feedback") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = {
                                val overall = ((fluencyScore + pronunciationScore + grammarScore) / 3).toInt()
                                viewModel.repository.gradeSubmission(sub.id, overall, teacherFeedbackInput)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            Text("Submit Grade & Send Feedback ($ {((fluencyScore + pronunciationScore + grammarScore) / 3).toInt()}%)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
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

@Composable
fun TeacherStudentsList(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val students by viewModel.adminStudents.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Assigned Students Cohort", fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                        Text(s.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("${s.email} • ${s.batch}", fontSize = 12.sp, color = MutedGray)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7)
                    ) {
                        Text("Active", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherAssignmentsList(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val assignments by viewModel.assignments.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Course Speaking Assignments", fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                                .width(100.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = RoyalBlue
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create Speaking Assignment") },
            text = { Text("Configure new audio speaking challenge for Batch Alpha students.") },
            confirmButton = {
                Button(onClick = { showCreateDialog = false }) { Text("Publish Assignment") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun TeacherLiveClasses(viewModel: MainViewModel, modifier: Modifier = Modifier) {
    val liveClasses by viewModel.liveClasses.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Virtual Classroom Schedules", fontWeight = FontWeight.Bold, fontSize = 17.sp)
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
                        onClick = { viewModel.speakAudio("Starting live virtual classroom for ${cls.title}") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Host Session Now")
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherMetricCard(title: String, value: String, emoji: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = color)
            Text(title, fontSize = 10.sp, color = MutedGray)
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
