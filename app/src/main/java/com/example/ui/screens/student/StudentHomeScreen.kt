package com.example.ui.screens.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.SpeakingSubTab
import com.example.ui.StudentTab
import com.example.ui.theme.*

@Composable
fun StudentHomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val user by viewModel.currentUser.collectAsState()
    val profile by viewModel.studentProfile.collectAsState()
    val announcements by viewModel.announcements.collectAsState()
    val assignments by viewModel.assignments.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("student_home_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Welcome Card (Glowing Purple Rounded Container with Owl Mascot)
        item {
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.5.dp, HeroBannerBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_mascot_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HeroBannerGrad)
                ) {
                    // Soft decorative sparkle in top-right
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.25f),
                        modifier = Modifier
                            .size(72.dp)
                            .align(Alignment.TopEnd)
                            .offset(x = (-10).dp, y = 14.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Owl mascot in warm glow squircle container
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color(0xFFFED7AA))
                                .border(2.5.dp, Color(0xFFFDE68A), RoundedCornerShape(22.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_hero_mascot),
                                contentDescription = "Echo Mascot",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            // Top Tag Pill
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = Color(0xFF0F1A3F).copy(alpha = 0.85f),
                                border = BorderStroke(0.8.dp, Color(0xFF22C55E).copy(alpha = 0.6f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF22C55E))
                                    )
                                    Text(
                                        text = "Daily English Adventure • ${user.studentClass}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Text(
                                text = "Hello, ${user.name}! 👋",
                                fontSize = 21.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.2.sp
                            )

                            Text(
                                text = "Ready to boost your speaking fluency today? Your ${profile.streakDays}-day streak is blazing hot!",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color.White.copy(alpha = 0.92f)
                            )
                        }
                    }
                }
            }
        }

        // 2. Tasks from Teachers Hub Container (Navy Rounded Card with nested task row)
        item {
            val studentClass = user.studentClass
            val classTasks = remember(assignments, studentClass) {
                assignments.filter {
                    it.batch.equals(studentClass, ignoreCase = true) ||
                    it.batch.equals("All Classes", ignoreCase = true) ||
                    it.batch.contains(studentClass, ignoreCase = true)
                }
            }

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.5.dp, TasksContainerBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectStudentTab(StudentTab.TASKS) }
                    .testTag("home_tasks_portal_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TasksContainerGrad)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Header row with "View All ->" button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF1E293B)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📝", fontSize = 20.sp)
                                }

                                Column {
                                    Text(
                                        text = "Tasks from Teachers",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Class ${studentClass} • ${classTasks.size} Assignments",
                                        fontSize = 12.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.selectStudentTab(StudentTab.TASKS) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2563EB)
                                ),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "View All →",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Nested Deep Navy Task Card
                        val nextUrgent = classTasks.firstOrNull()
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF0B1437).copy(alpha = 0.95f),
                            border = BorderStroke(1.2.dp, Color(0xFF1E3A8A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            nextUrgent?.topicType ?: "Speaking",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8)
                                        )
                                        Text(
                                            "• Due: ${nextUrgent?.dueDate ?: "Tomorrow, 6 PM"}",
                                            fontSize = 11.sp,
                                            color = Color(0xFFF97316),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Text(
                                        nextUrgent?.title ?: "3-Minute Impromptu Speech: Clim...",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(Modifier.width(8.dp))

                                OutlinedButton(
                                    onClick = {
                                        viewModel.selectStudentTab(StudentTab.TASKS)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.2.dp, Color(0xFF0284C7)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color(0xFF07214A).copy(alpha = 0.8f)
                                    ),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        Icons.Rounded.Mic,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp),
                                        tint = Color(0xFF38BDF8)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        "Practice",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Section Title: "Practice Arena & Learning Hub" with vertical accent bar
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(4.5.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFF8B5CF6))
                )
                Text(
                    text = "Practice Arena & Learning Hub",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp,
                    color = Color.White,
                    letterSpacing = 0.2.sp
                )
            }
        }

        // 4. Exact Screenshot 4-Quadrant Cards (2x2 Grid)
        // Row 1: AI Speaking (Azure Blue) + Roleplays (Vibrant Magenta-Purple)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ScreenshotActionCard(
                    title = "AI Speaking",
                    subtitle = "Voice Partner",
                    iconEmoji = "🎙️",
                    isMicIcon = true,
                    gradient = AISpeakingCardGrad,
                    borderColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectSpeakingSubTab(SpeakingSubTab.AI_PARTNER)
                        viewModel.selectStudentTab(StudentTab.PRACTICE)
                    }
                )

                ScreenshotActionCard(
                    title = "Roleplays",
                    subtitle = "Real Life Dialogues",
                    iconEmoji = "🎭",
                    gradient = RoleplaysCardGrad,
                    borderColor = Color(0xFFC084FC),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectSpeakingSubTab(SpeakingSubTab.ROLEPLAY)
                        viewModel.selectStudentTab(StudentTab.PRACTICE)
                    }
                )
            }
        }

        // Row 2: Phonetics (Vibrant Amber-Orange) + Mini Games (Luminous Teal Green)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ScreenshotActionCard(
                    title = "Phonetics",
                    subtitle = "Pronunciation Coach",
                    iconEmoji = "🎯",
                    gradient = PhoneticsCardGrad,
                    borderColor = Color(0xFFFDBA74),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectSpeakingSubTab(SpeakingSubTab.PRONUNCIATION_COACH)
                        viewModel.selectStudentTab(StudentTab.PRACTICE)
                    }
                )

                ScreenshotActionCard(
                    title = "Mini Games",
                    subtitle = "12 Gamified Drills",
                    iconEmoji = "🎮",
                    gradient = MiniGamesCardGrad,
                    borderColor = Color(0xFF5EEAD4),
                    modifier = Modifier.weight(1f),
                    onClick = {
                        viewModel.selectStudentTab(StudentTab.GAMES)
                    }
                )
            }
        }

        // 5. Daily Fluency Pro Tip Card (Matching EXACT screenshot)
        item {
            val tipText = "Focus on syllable stress rather than speed. Clear intonation makes English sound natural."
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.5.dp, Color(0xFFB34DFF).copy(alpha = 0.85f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_fluency_pro_tip_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF2E085E).copy(alpha = 0.95f),
                                    Color(0xFF1B0748).copy(alpha = 0.92f),
                                    Color(0xFF0D0A35).copy(alpha = 0.95f)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Glowing Squircle Lightbulb Icon badge
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF3B1578).copy(alpha = 0.75f),
                            border = BorderStroke(1.5.dp, Color(0xFFC084FC).copy(alpha = 0.9f)),
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("💡", fontSize = 34.sp)
                            }
                        }

                        // Middle Content
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pill tag: "⭐ Daily Fluency Pro Tip ✨"
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF6B21A8),
                                border = BorderStroke(1.dp, Color(0xFFD8B4FE).copy(alpha = 0.7f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text("⭐", fontSize = 12.sp)
                                    Text(
                                        text = "Daily Fluency Pro Tip",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text("✨", fontSize = 11.sp)
                                }
                            }

                            // Quoted Tip text
                            Text(
                                text = "“$tipText”",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Italic,
                                lineHeight = 18.sp,
                                color = Color.White
                            )
                        }

                        // Right: Circular Speaker Playback Button
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF2563EB).copy(alpha = 0.9f),
                            border = BorderStroke(2.dp, Color(0xFF93C5FD)),
                            modifier = Modifier
                                .size(52.dp)
                                .clickable {
                                    viewModel.speakAudio(tipText)
                                }
                                .testTag("fluency_tip_audio_btn")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.VolumeUp,
                                    contentDescription = "Listen to Pro Tip",
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Campus Announcements Card (Matching EXACT screenshot)
        item {
            Card(
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.5.dp, Color(0xFF0284C7).copy(alpha = 0.9f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("campus_announcement_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF06336B).copy(alpha = 0.95f),
                                    Color(0xFF0B2154).copy(alpha = 0.92f),
                                    Color(0xFF2E0858).copy(alpha = 0.95f)
                                )
                            )
                        )
                ) {
                    // Golden Bookmark Ribbon in Top-Right
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                        color = Color(0xFFF59E0B),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(end = 22.dp)
                            .width(34.dp)
                            .height(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🔔", fontSize = 18.sp)
                        }
                    }

                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Header Row: Loudspeaker Icon + "Campus Announcements"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("📢", fontSize = 22.sp)
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(16.dp)
                                    .background(Color.White.copy(alpha = 0.4f))
                            )
                            Text(
                                text = "Campus Announcements",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }

                        // Content Row: Golden Trophy Left + Announcement details Right
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Glowing Trophy on Pedestal Badge
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = Color(0xFF1E1B4B).copy(alpha = 0.7f),
                                border = BorderStroke(1.5.dp, Color(0xFFFACC15).copy(alpha = 0.6f)),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🏆", fontSize = 38.sp)
                                }
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Weekly Spoken English Tournament Begins!",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp,
                                    color = Color.White,
                                    lineHeight = 18.sp
                                )

                                val annotatedText = buildAnnotatedString {
                                    append("Compete in the 30-Second Speaking Challenge to win ")
                                    withStyle(
                                        style = SpanStyle(
                                            color = Color(0xFFFBBF24),
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    ) {
                                        append("500 bonus XP")
                                    }
                                    append(" and the ")
                                }

                                Text(
                                    text = annotatedText,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp,
                                    color = Color(0xFFE2E8F0)
                                )

                                // Diamond League Crown Pill
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF4C1D95).copy(alpha = 0.85f),
                                    border = BorderStroke(1.dp, Color(0xFFA855F7)),
                                    modifier = Modifier.padding(top = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Diamond League crown. 👑",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.5.sp,
                                            color = Color(0xFFE9D5FF)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom space so nothing is occluded by bottom bar
        item {
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

/**
 * Custom 4-quadrant action card exactly matching user's screenshot.
 * Features curved corner card, vibrant vertical gradient, round icon badge,
 * circular chevron button in top right, bold title, and clean subtitle.
 */
@Composable
fun ScreenshotActionCard(
    title: String,
    subtitle: String,
    iconEmoji: String,
    gradient: Brush,
    borderColor: Color,
    modifier: Modifier = Modifier,
    isMicIcon: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.2.dp, borderColor.copy(alpha = 0.55f)),
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .clickable { onClick() }
            .testTag("screenshot_card_${title.lowercase().replace(" ", "_")}")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top row with Icon Badge & Arrow circle button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Elevated icon badge
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        modifier = Modifier.size(50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isMicIcon) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            } else {
                                Text(iconEmoji, fontSize = 26.sp)
                            }
                        }
                    }

                    // Circle arrow button
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.32f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Text labels
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White,
                        letterSpacing = 0.2.sp
                    )
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
