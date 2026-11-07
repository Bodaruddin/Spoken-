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
import androidx.compose.material.icons.automirrored.rounded.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.MainViewModel
import com.example.ui.SpeakingSubTab
import com.example.ui.StudentTab
import com.example.ui.components.SectionHeader
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
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Mascot Welcome Card (Cyber-Glass matching screenshot)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.2.dp, Color(0xFF60A5FA).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_mascot_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HeroBannerGrad)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier
                            .size(110.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = 10.dp, y = 14.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Mascot in glowing squircle ring
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(RoundedCornerShape(22.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C))))
                                .border(2.dp, Color(0xFFFDE68A).copy(alpha = 0.85f), RoundedCornerShape(22.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_hero_mascot),
                                contentDescription = "Echo the Mascot",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(20.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
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
                                        text = "Daily English Adventure • ${user.studentClass}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF86EFAC)
                                    )
                                }
                            }

                            Text(
                                text = "Hello, ${user.name}! 👋",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )

                            Text(
                                text = "Ready to boost your speaking fluency today? Your ${profile.streakDays}-day streak is blazing hot!",
                                fontSize = 12.sp,
                                lineHeight = 16.sp,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }

        // 2. Tasks from Teachers Hub Banner (Links to dedicated Tasks tab)
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
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.selectStudentTab(StudentTab.TASKS) }
                    .testTag("home_tasks_portal_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("📝", fontSize = 20.sp)
                            Column {
                                Text(
                                    "Tasks from Teachers",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Class ${studentClass} • ${classTasks.size} Assignments",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.selectStudentTab(StudentTab.TASKS) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("View All", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color.White)
                        }
                    }

                    val nextUrgent = classTasks.firstOrNull()
                    if (nextUrgent != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            nextUrgent.topicType,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF60A5FA)
                                        )
                                        Text(
                                            "• Due: ${nextUrgent.dueDate}",
                                            fontSize = 10.sp,
                                            color = OrangeVibrant,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        nextUrgent.title,
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
                                        viewModel.selectSpeakingSubTab(SpeakingSubTab.AI_PARTNER)
                                        viewModel.selectStudentTab(StudentTab.PRACTICE)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f)),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(12.dp), tint = Color(0xFF38BDF8))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Practice", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. Quick Practice Hub Launchers
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("Practice Arena & Learning Hub")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickHubCard(
                        title = "AI Speaking",
                        subtitle = "Voice Partner",
                        emoji = "🎙️",
                        color = Color(0xFFEFF6FF),
                        accent = RoyalBlue,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectSpeakingSubTab(SpeakingSubTab.AI_PARTNER)
                            viewModel.selectStudentTab(StudentTab.PRACTICE)
                        }
                    )

                    QuickHubCard(
                        title = "Roleplays",
                        subtitle = "Real Life Dialogues",
                        emoji = "🎭",
                        color = Color(0xFFFDF4FF),
                        accent = PurpleAccent,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectSpeakingSubTab(SpeakingSubTab.ROLEPLAY)
                            viewModel.selectStudentTab(StudentTab.PRACTICE)
                        }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickHubCard(
                        title = "Phonetics",
                        subtitle = "Pronunciation Coach",
                        emoji = "🎯",
                        color = Color(0xFFFFFBEB),
                        accent = AmberGold,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectSpeakingSubTab(SpeakingSubTab.PRONUNCIATION_COACH)
                            viewModel.selectStudentTab(StudentTab.PRACTICE)
                        }
                    )

                    QuickHubCard(
                        title = "Mini Games",
                        subtitle = "12 Gamified Drills",
                        emoji = "🎮",
                        color = Color(0xFFECFDF5),
                        accent = EmeraldSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.selectStudentTab(StudentTab.GAMES)
                        }
                    )
                }
            }
        }

        // 5. Daily Spoken Fluency Tip
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C241E)),
                border = BorderStroke(1.2.dp, EmeraldSuccess.copy(alpha = 0.45f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0A3D2E)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("💡", fontSize = 22.sp)
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "Daily Fluency Pro Tip",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                        Text(
                            text = "\"Focus on syllable stress rather than speed. Clear intonation makes English sound natural.\"",
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }

                    IconButton(
                        onClick = {
                            viewModel.speakAudio("Focus on syllable stress rather than speed. Clear intonation makes English sound natural.")
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            Icons.Rounded.VolumeUp,
                            contentDescription = "Listen to Tip",
                            tint = EmeraldSuccess
                        )
                    }
                }
            }
        }

        // 6. Recent Campus Announcements
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CyberCardBg),
                border = BorderStroke(1.2.dp, Color(0xFF38BDF8).copy(alpha = 0.35f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📢", fontSize = 16.sp)
                        Text("Campus Announcements", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    announcements.firstOrNull()?.let { note ->
                        Text(
                            text = note.title,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = note.message,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}



@Composable
fun QuickHubCard(
    title: String,
    subtitle: String,
    emoji: String,
    color: Color,
    accent: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val cardBg = when (accent) {
        RoyalBlue -> NeonBlueCardBg
        PurpleAccent -> NeonPurpleCardBg
        EmeraldSuccess -> NeonGreenCardBg
        AmberGold -> NeonAmberCardBg
        CyanAccent -> NeonCyanCardBg
        CoralAccent -> NeonPinkCardBg
        else -> CyberCardBg
    }
    val cardBorder = when (accent) {
        RoyalBlue -> NeonBlueBorder
        PurpleAccent -> NeonPurpleBorder
        EmeraldSuccess -> NeonGreenBorder
        AmberGold -> NeonAmberBorder
        CyanAccent -> NeonCyanBorder
        CoralAccent -> NeonPinkBorder
        else -> accent
    }
    val iconGrad = when (accent) {
        RoyalBlue -> NeonBlueGrad
        PurpleAccent -> NeonPurpleGrad
        EmeraldSuccess -> NeonGreenGrad
        AmberGold -> NeonAmberGrad
        CyanAccent -> NeonCyanGrad
        CoralAccent -> NeonPinkGrad
        else -> Brush.linearGradient(listOf(accent, accent))
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.2.dp, cardBorder.copy(alpha = 0.55f)),
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("quick_hub_${title.lowercase().replace(" ", "_")}")
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
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(iconGrad),
                    contentAlignment = Alignment.Center
                ) {
                    Text(emoji, fontSize = 22.sp)
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
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                )
            }
        }
    }
}
