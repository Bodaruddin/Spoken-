package com.example.ui.screens.student

import android.content.Context
import android.os.CountDownTimer
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Exam
import com.example.data.model.ExamQuestion
import com.example.data.model.ExamSubmission
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import com.example.util.CertificatePdfGenerator
import java.io.File

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentExamsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val exams by viewModel.exams.collectAsState()
    val examSubmissions by viewModel.examSubmissions.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf("All") } // "All", "Pending", "Completed"
    var selectedCategoryFilter by remember { mutableStateOf("All Categories") }
    var searchQuery by remember { mutableStateOf("") }

    // Active exam session state
    var activeExamToTake by remember { mutableStateOf<Exam?>(null) }
    // Certificate preview state
    var selectedSubmissionForCert by remember { mutableStateOf<ExamSubmission?>(null) }
    // Review answers state
    var selectedSubmissionForReview by remember { mutableStateOf<Pair<Exam, ExamSubmission>?>(null) }

    val studentClass = currentUser.studentClass

    // Map of completed submissions for this student keyed by examId
    val studentSubmissionsMap = remember(examSubmissions, currentUser) {
        examSubmissions.filter {
            it.studentId == currentUser.id || it.studentName.equals(currentUser.name, ignoreCase = true)
        }.associateBy { it.examId }
    }

    // Filter exams according to class, status, category, and search query
    val filteredExams = remember(exams, selectedStatusFilter, selectedCategoryFilter, searchQuery, studentSubmissionsMap, studentClass) {
        exams.filter { exam ->
            // Class match: Target class or All Classes
            val matchesClass = exam.targetClass.equals("All Classes", ignoreCase = true) ||
                exam.targetClass.equals(studentClass, ignoreCase = true)

            val hasCompleted = studentSubmissionsMap.containsKey(exam.id)
            val matchesStatus = when (selectedStatusFilter) {
                "Pending" -> !hasCompleted
                "Completed" -> hasCompleted
                else -> true
            }

            val matchesCategory = selectedCategoryFilter == "All Categories" ||
                exam.subjectCategory.equals(selectedCategoryFilter, ignoreCase = true)

            val matchesSearch = searchQuery.isBlank() ||
                exam.title.contains(searchQuery, ignoreCase = true) ||
                exam.description.contains(searchQuery, ignoreCase = true) ||
                exam.teacherName.contains(searchQuery, ignoreCase = true)

            matchesClass && matchesStatus && matchesCategory && matchesSearch
        }
    }

    val completedCount = studentSubmissionsMap.size
    val pendingCount = exams.count { exam ->
        (exam.targetClass.equals("All Classes", ignoreCase = true) || exam.targetClass.equals(studentClass, ignoreCase = true)) &&
            !studentSubmissionsMap.containsKey(exam.id)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberMidnightBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
    ) {
        // --- Header Banner Section ---
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                border = BorderStroke(1.2.dp, Brush.horizontalGradient(listOf(Color(0xFF38BDF8), Color(0xFF8B5CF6)))),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("student_exams_header_card")
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("🎓", fontSize = 18.sp)
                                Text(
                                    text = "MCQ Examination Center",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = "Enrolled in $studentClass • Timed assessments with verified certificates",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF0284C7))
                        ) {
                            Text(
                                text = studentClass,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // 3 Metric Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricPill(
                            title = "Available",
                            value = "$pendingCount",
                            icon = Icons.Rounded.PendingActions,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            title = "Completed",
                            value = "$completedCount",
                            icon = Icons.Rounded.TaskAlt,
                            color = EmeraldSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        MetricPill(
                            title = "Certificates",
                            value = "${studentSubmissionsMap.values.count { it.isPassed }}",
                            icon = Icons.Rounded.WorkspacePremium,
                            color = AmberGold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // --- Search & Filter Bar ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search Input
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by exam title, grammar topic...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                    leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Rounded.Close, contentDescription = "Clear", tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0B132B),
                        unfocusedContainerColor = Color(0xFF0B132B),
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF1E293B),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Status Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusOptions = listOf(
                        "All" to "All Exams (${filteredExams.size})",
                        "Pending" to "Pending ($pendingCount ⏳)",
                        "Completed" to "Completed ($completedCount ✅)"
                    )
                    statusOptions.forEach { (statusKey, label) ->
                        val isSelected = selectedStatusFilter == statusKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedStatusFilter = statusKey },
                            label = { Text(label, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(0xFF0F172A),
                                labelColor = Color(0xFF94A3B8),
                                selectedContainerColor = RoyalBlue,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = Color(0xFF1E293B),
                                selectedBorderColor = Color(0xFF38BDF8)
                            )
                        )
                    }
                }

                // Category Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    val categories = listOf(
                        "All Categories",
                        "English Grammar",
                        "Phonetics & Pronunciation",
                        "Vocabulary & Idioms",
                        "Spoken English & Fluency"
                    )
                    items(categories) { cat ->
                        val isSelected = selectedCategoryFilter == cat
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) Color(0xFF8B5CF6).copy(alpha = 0.35f) else Color(0xFF0A0F29),
                            border = BorderStroke(1.dp, if (isSelected) Color(0xFFA78BFA) else Color(0xFF1E293B)),
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { selectedCategoryFilter = cat }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFFE9D5FF) else Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // --- Exams List ---
        if (filteredExams.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                    border = BorderStroke(1.dp, Color(0xFF1E293B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("📋", fontSize = 36.sp)
                        Text("No Exams Found", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No exams match your search query." else "All scheduled exams for this category have been checked.",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        items(filteredExams, key = { it.id }) { exam ->
            val submission = studentSubmissionsMap[exam.id]
            val isCompleted = submission != null

            ExamCard(
                exam = exam,
                submission = submission,
                onStartExam = { activeExamToTake = exam },
                onViewCertificate = { selectedSubmissionForCert = submission },
                onDownloadPdf = {
                    submission?.let { sub ->
                        CertificatePdfGenerator.generateCertificatePdf(
                            context = context,
                            submission = sub,
                            onSuccess = { file ->
                                Toast.makeText(context, "Certificate PDF downloaded: ${file.name}", Toast.LENGTH_LONG).show()
                                CertificatePdfGenerator.openOrSharePdf(context, file)
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                },
                onReviewQuestions = {
                    submission?.let { sub ->
                        selectedSubmissionForReview = Pair(exam, sub)
                    }
                }
            )
        }
    }

    // --- Active Exam Dialog Modal ---
    activeExamToTake?.let { exam ->
        LiveExamModal(
            exam = exam,
            onDismiss = { activeExamToTake = null },
            onSubmit = { answersMap, timeTakenSeconds ->
                val newSub = viewModel.submitStudentExam(
                    examId = exam.id,
                    selectedAnswers = answersMap,
                    timeTakenSeconds = timeTakenSeconds
                )
                activeExamToTake = null
                selectedSubmissionForCert = newSub
            }
        )
    }

    // --- Certificate Preview Dialog Modal ---
    selectedSubmissionForCert?.let { sub ->
        CertificatePreviewDialog(
            submission = sub,
            onDismiss = { selectedSubmissionForCert = null },
            onDownloadPdf = {
                CertificatePdfGenerator.generateCertificatePdf(
                    context = context,
                    submission = sub,
                    onSuccess = { file ->
                        Toast.makeText(context, "Certificate PDF saved to Downloads!", Toast.LENGTH_LONG).show()
                        CertificatePdfGenerator.openOrSharePdf(context, file)
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }

    // --- Review Questions Dialog Modal ---
    selectedSubmissionForReview?.let { (exam, sub) ->
        ExamReviewDialog(
            exam = exam,
            submission = sub,
            onDismiss = { selectedSubmissionForReview = null },
            onViewCertificate = {
                selectedSubmissionForReview = null
                selectedSubmissionForCert = sub
            }
        )
    }
}

// -------------------------------------------------------------
// EXAM CARD COMPOSABLE
// -------------------------------------------------------------
@Composable
private fun ExamCard(
    exam: Exam,
    submission: ExamSubmission?,
    onStartExam: () -> Unit,
    onViewCertificate: () -> Unit,
    onDownloadPdf: () -> Unit,
    onReviewQuestions: () -> Unit
) {
    val isCompleted = submission != null

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(
            1.2.dp,
            if (isCompleted) EmeraldSuccess.copy(alpha = 0.5f) else Color(0xFF38BDF8).copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("exam_card_${exam.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Category & Class Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E1B4B),
                        border = BorderStroke(1.dp, Color(0xFF818CF8).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = exam.subjectCategory,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA78BFA),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F2537),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = exam.targetClass,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = "Instructor: ${exam.teacherName}",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Exam Title & Description
            Text(
                text = exam.title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )

            Text(
                text = exam.description,
                fontSize = 12.5.sp,
                color = Color(0xFFCBD5E1),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Key Exam Parameters Row (Time, Questions, Marks, Pass)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0B132B), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ParamBadge(icon = Icons.Rounded.Timer, label = "${exam.durationMinutes} Mins")
                VerticalDivider(modifier = Modifier.height(18.dp), color = Color(0xFF1E293B))
                ParamBadge(icon = Icons.Rounded.Quiz, label = "${exam.questionsCount} MCQs")
                VerticalDivider(modifier = Modifier.height(18.dp), color = Color(0xFF1E293B))
                ParamBadge(icon = Icons.Rounded.Scoreboard, label = "${exam.totalMarks} Marks")
                VerticalDivider(modifier = Modifier.height(18.dp), color = Color(0xFF1E293B))
                ParamBadge(icon = Icons.Rounded.Verified, label = "Pass: ${exam.passingPercentage}%")
            }

            // Completed State vs Start State
            if (isCompleted && submission != null) {
                // Completed Summary Strip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (submission.isPassed) Color(0xFF0A2E1F) else Color(0xFF2E1313),
                    border = BorderStroke(1.dp, if (submission.isPassed) EmeraldSuccess else CoralAccent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(if (submission.isPassed) "🏆" else "⚠️", fontSize = 16.sp)
                                Text(
                                    text = if (submission.isPassed) "Passed • Grade ${submission.grade}" else "Needs Improvement",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (submission.isPassed) EmeraldSuccess else CoralAccent
                                )
                            }
                            Text(
                                text = "Score: ${submission.scoreObtained}/${submission.totalMarks} (${submission.percentage}%) • Cert ID: ${submission.certificateId}",
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (submission.isPassed) EmeraldSuccess else CoralAccent
                        ) {
                            Text(
                                text = "${submission.percentage}%",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Action buttons for completed exam
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onReviewQuestions,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF64748B)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF94A3B8)),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(Icons.Rounded.Visibility, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    if (submission.isPassed) {
                        Button(
                            onClick = onViewCertificate,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberGold
                            ),
                            modifier = Modifier.weight(1.3f),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            Icon(Icons.Rounded.WorkspacePremium, contentDescription = null, tint = Color(0xFF78350F), modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Certificate", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                        }

                        IconButton(
                            onClick = onDownloadPdf,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Rounded.Download, contentDescription = "Download PDF", tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            } else {
                // Pending: Start Exam CTA
                Button(
                    onClick = onStartExam,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(listOf(Color(0xFF0072FF), Color(0xFF00C6FF))),
                                RoundedCornerShape(14.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Start MCQ Exam", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ParamBadge(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(13.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE2E8F0))
    }
}

@Composable
private fun MetricPill(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF0B132B),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
            }
            Column {
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp, color = Color.White)
                Text(title, fontSize = 9.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}

// -------------------------------------------------------------
// LIVE MCQ EXAM MODAL
// -------------------------------------------------------------
@Composable
private fun LiveExamModal(
    exam: Exam,
    onDismiss: () -> Unit,
    onSubmit: (selectedAnswers: Map<String, Int>, timeTakenSeconds: Int) -> Unit
) {
    var hasStarted by remember { mutableStateOf(false) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    val selectedAnswers = remember { mutableStateMapOf<String, Int>() }
    var timeRemainingSeconds by remember { mutableIntStateOf(exam.durationMinutes * 60) }
    var showSubmitConfirmation by remember { mutableStateOf(false) }

    val totalDurationSeconds = exam.durationMinutes * 60

    // Countdown Timer
    DisposableEffect(hasStarted) {
        if (!hasStarted) return@DisposableEffect onDispose { }

        val timer = object : CountDownTimer((timeRemainingSeconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeRemainingSeconds = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                timeRemainingSeconds = 0
                // Auto-submit exam when timer reaches 0
                val timeTaken = totalDurationSeconds
                onSubmit(selectedAnswers.toMap(), timeTaken)
            }
        }.start()

        onDispose { timer.cancel() }
    }

    Dialog(
        onDismissRequest = {
            if (!hasStarted) onDismiss() else showSubmitConfirmation = true
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF060919)),
            color = Color(0xFF060919)
        ) {
            if (!hasStarted) {
                // Pre-Exam Briefing Screen
                PreExamBriefing(
                    exam = exam,
                    onCancel = onDismiss,
                    onBegin = { hasStarted = true }
                )
            } else {
                // Active Test Taking UI
                val currentQuestion = exam.questions.getOrNull(currentQuestionIndex)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Top App Bar with Title & Countdown Timer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = exam.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Question ${currentQuestionIndex + 1} of ${exam.questionsCount}",
                                fontSize = 11.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }

                        // Live Countdown Timer Badge
                        val isLowTime = timeRemainingSeconds < 120
                        val minutes = timeRemainingSeconds / 60
                        val seconds = timeRemainingSeconds % 60
                        val timeStr = String.format("%02d:%02d", minutes, seconds)

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isLowTime) Color(0xFF450A0A) else Color(0xFF0B1938),
                            border = BorderStroke(1.2.dp, if (isLowTime) Color(0xFFEF4444) else Color(0xFF38BDF8))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Alarm,
                                    contentDescription = null,
                                    tint = if (isLowTime) Color(0xFFEF4444) else Color(0xFF38BDF8),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = timeStr,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.sp,
                                    color = if (isLowTime) Color(0xFFEF4444) else Color.White
                                )
                            }
                        }
                    }

                    // Linear Progress Bar
                    val progress = (currentQuestionIndex + 1).toFloat() / exam.questionsCount.coerceAtLeast(1)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = Color(0xFF38BDF8),
                        trackColor = Color(0xFF1E293B)
                    )

                    // Quick Question Selector Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(exam.questions.size) { idx ->
                            val q = exam.questions[idx]
                            val isCurrent = idx == currentQuestionIndex
                            val isAnswered = selectedAnswers.containsKey(q.id)

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    isCurrent -> Color(0xFF2563EB)
                                    isAnswered -> EmeraldSuccess.copy(alpha = 0.35f)
                                    else -> Color(0xFF0F172A)
                                },
                                border = BorderStroke(
                                    1.dp,
                                    when {
                                        isCurrent -> Color(0xFF60A5FA)
                                        isAnswered -> EmeraldSuccess
                                        else -> Color(0xFF1E293B)
                                    }
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { currentQuestionIndex = idx }
                            ) {
                                Text(
                                    text = "Q${idx + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Question Content Card
                    currentQuestion?.let { q ->
                        val selectedOpt = selectedAnswers[q.id]

                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1432)),
                            border = BorderStroke(1.2.dp, Color(0xFF1E293B)),
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(18.dp)
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Question Number & Points
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Text(
                                            text = "Question ${currentQuestionIndex + 1}",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF93C5FD),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }

                                    Text(
                                        text = "+${q.marks} Marks",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = AmberGold
                                    )
                                }

                                // Question Prompt Text
                                Text(
                                    text = q.question,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    lineHeight = 22.sp
                                )

                                Spacer(Modifier.height(4.dp))

                                // Options A, B, C, D
                                val optionLetters = listOf("A", "B", "C", "D")
                                q.options.forEachIndexed { optIdx, optText ->
                                    val isSelected = selectedOpt == optIdx
                                    val letter = optionLetters.getOrElse(optIdx) { "${optIdx + 1}" }

                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = if (isSelected) Color(0xFF1E3A8A).copy(alpha = 0.6f) else Color(0xFF070B1F),
                                        border = BorderStroke(
                                            1.5.dp,
                                            if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { selectedAnswers[q.id] = optIdx }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            // Letter Circle Badge
                                            Box(
                                                modifier = Modifier
                                                    .size(30.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) Color(0xFF38BDF8) else Color(0xFF1E293B)
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = letter,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color(0xFF090D26) else Color(0xFFCBD5E1)
                                                )
                                            }

                                            Text(
                                                text = optText,
                                                fontSize = 14.sp,
                                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                                modifier = Modifier.weight(1f)
                                            )

                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedAnswers[q.id] = optIdx },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = Color(0xFF38BDF8),
                                                    unselectedColor = Color(0xFF475569)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Bottom Navigation Buttons Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Button
                        OutlinedButton(
                            onClick = { if (currentQuestionIndex > 0) currentQuestionIndex-- },
                            enabled = currentQuestionIndex > 0,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Previous", fontSize = 12.sp)
                        }

                        // Next or Finish Button
                        if (currentQuestionIndex < exam.questionsCount - 1) {
                            Button(
                                onClick = { currentQuestionIndex++ },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Next", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        } else {
                            Button(
                                onClick = { showSubmitConfirmation = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Submit Exam", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    // Submit Confirmation Dialog
    if (showSubmitConfirmation) {
        val answeredCount = selectedAnswers.size
        val unansweredCount = exam.questionsCount - answeredCount

        AlertDialog(
            onDismissRequest = { showSubmitConfirmation = false },
            title = { Text("Submit Examination?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("You have answered $answeredCount of ${exam.questionsCount} questions.")
                    if (unansweredCount > 0) {
                        Text(
                            "Warning: $unansweredCount question(s) remain unanswered.",
                            color = CoralAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text("Once submitted, your final marks and verified certificate will be generated immediately.")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSubmitConfirmation = false
                        val timeTaken = totalDurationSeconds - timeRemainingSeconds
                        onSubmit(selectedAnswers.toMap(), timeTaken)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                ) {
                    Text("Confirm & Submit", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSubmitConfirmation = false }) {
                    Text("Continue Test")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// PRE-EXAM BRIEFING COMPOSABLE
// -------------------------------------------------------------
@Composable
private fun PreExamBriefing(
    exam: Exam,
    onCancel: () -> Unit,
    onBegin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Close / Cancel Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Rounded.Close, contentDescription = "Cancel", tint = Color.White)
                }
                Text("Examination Instructions", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                Spacer(Modifier.width(48.dp))
            }

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E1B4B),
                        border = BorderStroke(1.dp, Color(0xFF818CF8))
                    ) {
                        Text(
                            text = exam.subjectCategory,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA78BFA),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = exam.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    Text(
                        text = exam.description,
                        fontSize = 13.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            }

            // Rules & Parameters Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B)),
                border = BorderStroke(1.dp, Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Examination Guidelines", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)

                    RuleItem(icon = Icons.Rounded.Timer, title = "Time Limit: ${exam.durationMinutes} Minutes", desc = "Timer starts immediately. Assessment auto-submits when time expires.")
                    RuleItem(icon = Icons.Rounded.Quiz, title = "${exam.questionsCount} Multiple Choice Questions", desc = "Each question carries ${exam.questions.firstOrNull()?.marks ?: 5} marks. Total marks: ${exam.totalMarks}.")
                    RuleItem(icon = Icons.Rounded.WorkspacePremium, title = "Pass Criteria: ${exam.passingPercentage}% (${exam.passingMarks} Marks)", desc = "Score ${exam.passingPercentage}% or above to generate your official Verified Certificate.")
                    RuleItem(icon = Icons.Rounded.Download, title = "PDF Certificate Download", desc = "Includes your score, award grade, official seal, and faculty digital signatures.")
                }
            }
        }

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color(0xFF475569)),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("Not Now", color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onBegin,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Begin Exam 🚀", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun RuleItem(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, desc: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp).offset(y = 2.dp))
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = Color.White)
            Text(desc, fontSize = 11.sp, color = Color(0xFF94A3B8))
        }
    }
}

// -------------------------------------------------------------
// PREMIUM CERTIFICATE PREVIEW DIALOG
// -------------------------------------------------------------
@Composable
private fun CertificatePreviewDialog(
    submission: ExamSubmission,
    onDismiss: () -> Unit,
    onDownloadPdf: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF040714)),
            color = Color(0xFF040714)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Text(
                        "Official Verified Certificate",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Button(
                        onClick = onDownloadPdf,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Rounded.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                    }
                }

                // Certificate Canvas Surface (Scrollable container for viewing)
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFBF7)), // Ivory background
                    border = BorderStroke(4.dp, Color(0xFFB8860B)), // Dark gold framing
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(1.5.dp, Color(0xFF090D26), RoundedCornerShape(16.dp))
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Academy Seal & Header
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0F172A))
                                    .border(2.dp, Color(0xFFD4AF37), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("★", fontSize = 24.sp, color = Color(0xFFFFD700))
                            }

                            Text(
                                text = "SPEAKLY ACADEMY OF ENGLISH",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFF090D26),
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "OFFICIAL RECORD OF ACADEMIC EXCELLENCE",
                                fontSize = 9.sp,
                                color = Color(0xFF64748B),
                                letterSpacing = 1.sp
                            )

                            // Gold divider line
                            Box(
                                modifier = Modifier
                                    .width(180.dp)
                                    .height(1.5.dp)
                                    .background(Color(0xFFD4AF37))
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "CERTIFICATE OF EXCELLENCE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF091030),
                                letterSpacing = 1.sp
                            )

                            Text(
                                text = "This credential is proudly awarded to",
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                fontSize = 11.5.sp,
                                color = Color(0xFF475569)
                            )

                            // Student Name
                            Text(
                                text = submission.studentName,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp,
                                color = Color(0xFF090D26),
                                fontFamily = FontFamily.Serif
                            )

                            Box(
                                modifier = Modifier
                                    .width(200.dp)
                                    .height(1.dp)
                                    .background(Color(0xFFB8860B))
                            )

                            Text(
                                text = "Student of ${submission.studentClass} • Speakly Digital Campus",
                                fontSize = 11.sp,
                                color = Color(0xFF334155)
                            )

                            Text(
                                text = "for demonstrating proficiency and successfully passing",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )

                            Text(
                                text = "\"${submission.examTitle}\"",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1E3A8A),
                                textAlign = TextAlign.Center
                            )

                            // Score & Grade Box
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("MARKS", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                        Text("${submission.scoreObtained}/${submission.totalMarks}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF090D26))
                                    }
                                    VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0xFFCBD5E1))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("PERCENTAGE", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                        Text("${submission.percentage}%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF090D26))
                                    }
                                    VerticalDivider(modifier = Modifier.height(24.dp), color = Color(0xFFCBD5E1))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("GRADE", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B))
                                        Text("Grade ${submission.grade}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF15803D))
                                    }
                                }
                            }

                            // Signatures & Gold Seal Row
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Faculty Signature
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Eleanor Vance", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 13.sp, color = Color(0xFF1E293B))
                                    Box(modifier = Modifier.width(90.dp).height(1.dp).background(Color(0xFF94A3B8)))
                                    Text("Prof. Eleanor Vance", fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = Color(0xFF334155))
                                    Text("Head of English Faculty", fontSize = 7.5.sp, color = Color(0xFF64748B))
                                }

                                // Gold Embossed Seal
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFDAA520))
                                        .border(2.dp, Color(0xFFB8860B), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("★ SEAL ★", fontSize = 6.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF78350F))
                                }

                                // Dean Signature
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Marcus Reed", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic, fontSize = 13.sp, color = Color(0xFF1E293B))
                                    Box(modifier = Modifier.width(90.dp).height(1.dp).background(Color(0xFF94A3B8)))
                                    Text("Dr. Marcus Reed", fontWeight = FontWeight.Bold, fontSize = 8.5.sp, color = Color(0xFF334155))
                                    Text("Academic Dean", fontSize = 7.5.sp, color = Color(0xFF64748B))
                                }
                            }

                            // Footer IDs
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ID: ${submission.certificateId}", fontSize = 8.sp, color = Color(0xFF64748B))
                                Text("✓ AUTHENTIC VERIFIED", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            }
                        }
                    }
                }

                // Download Full Resolution PDF Button
                Button(
                    onClick = onDownloadPdf,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.Download, contentDescription = null, tint = Color.White)
                        Text("Download Certificate PDF", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// EXAM REVIEW DIALOG (CORRECT/INCORRECT ANSWERS)
// -------------------------------------------------------------
@Composable
private fun ExamReviewDialog(
    exam: Exam,
    submission: ExamSubmission,
    onDismiss: () -> Unit,
    onViewCertificate: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF060919)),
            color = Color(0xFF060919)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
                    }
                    Text("Exam Answers Review", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    if (submission.isPassed) {
                        TextButton(onClick = onViewCertificate) {
                            Text("Certificate 🏆", color = AmberGold, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Spacer(Modifier.width(48.dp))
                    }
                }

                // Review Score Banner
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(exam.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                            Text("Score: ${submission.scoreObtained}/${submission.totalMarks} (${submission.percentage}%) • Grade ${submission.grade}", fontSize = 12.sp, color = Color(0xFF38BDF8))
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (submission.isPassed) EmeraldSuccess else CoralAccent
                        ) {
                            Text(
                                text = if (submission.isPassed) "PASSED" else "RETAKE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Questions List with Student's Choices vs Correct Answer
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(exam.questions.size) { qIdx ->
                        val q = exam.questions[qIdx]
                        val selectedOptIdx = submission.selectedAnswers[q.id]
                        val isCorrect = selectedOptIdx != null && selectedOptIdx == q.correctOptionIndex

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B132B)),
                            border = BorderStroke(
                                1.dp,
                                if (isCorrect) EmeraldSuccess.copy(alpha = 0.4f) else CoralAccent.copy(alpha = 0.4f)
                            ),
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
                                    Text(
                                        "Question ${qIdx + 1} (${q.marks} Marks)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF93C5FD)
                                    )
                                    Text(
                                        text = if (isCorrect) "✓ Correct (+${q.marks})" else "✗ Incorrect (0)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (isCorrect) EmeraldSuccess else CoralAccent
                                    )
                                }

                                Text(q.question, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)

                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    val letters = listOf("A", "B", "C", "D")
                                    q.options.forEachIndexed { optIdx, optText ->
                                        val isChosen = selectedOptIdx == optIdx
                                        val isTheCorrectOne = q.correctOptionIndex == optIdx

                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = when {
                                                isTheCorrectOne -> EmeraldSuccess.copy(alpha = 0.2f)
                                                isChosen && !isTheCorrectOne -> CoralAccent.copy(alpha = 0.2f)
                                                else -> Color.Transparent
                                            },
                                            border = BorderStroke(
                                                1.dp,
                                                when {
                                                    isTheCorrectOne -> EmeraldSuccess
                                                    isChosen && !isTheCorrectOne -> CoralAccent
                                                    else -> Color(0xFF1E293B)
                                                }
                                            ),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Text(
                                                    "${letters.getOrElse(optIdx) { "" }}.",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp,
                                                    color = when {
                                                        isTheCorrectOne -> EmeraldSuccess
                                                        isChosen && !isTheCorrectOne -> CoralAccent
                                                        else -> Color(0xFF94A3B8)
                                                    }
                                                )
                                                Text(
                                                    optText,
                                                    fontSize = 12.5.sp,
                                                    color = when {
                                                        isTheCorrectOne -> Color.White
                                                        isChosen && !isTheCorrectOne -> Color(0xFFFCA5A5)
                                                        else -> Color(0xFF94A3B8)
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                if (isTheCorrectOne) {
                                                    Text("✓ Correct Answer", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                                                } else if (isChosen) {
                                                    Text("Your Choice", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CoralAccent)
                                                }
                                            }
                                        }
                                    }
                                }

                                if (q.explanation.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "💡 Explanation: ${q.explanation}",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF38BDF8),
                                            modifier = Modifier.padding(8.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Done Button
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Review", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
