package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AdminTab
import com.example.ui.StudentTab
import com.example.ui.TeacherTab
import com.example.ui.theme.*

@Composable
fun StudentBottomBar(
    currentTab: StudentTab,
    onTabSelected: (StudentTab) -> Unit,
    modifier: Modifier = Modifier,
    pendingTasksCount: Int = 0
) {
    Surface(
        color = Color(0xFF090D26).copy(alpha = 0.97f),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(
            1.2.dp,
            Brush.horizontalGradient(
                listOf(
                    Color(0xFF00D2FF).copy(alpha = 0.60f),
                    Color(0xFF8B5CF6).copy(alpha = 0.85f),
                    Color(0xFFEC4899).copy(alpha = 0.60f)
                )
            )
        ),
        shadowElevation = 12.dp,
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("student_bottom_bar")
    ) {
        val studentTabs = listOf(
            StudentTabItem(StudentTab.HOME, "Home", Icons.Rounded.Home),
            StudentTabItem(StudentTab.EXAMS, "Exams", Icons.Rounded.Quiz),
            StudentTabItem(StudentTab.PRACTICE, "Speak", Icons.Rounded.Mic),
            StudentTabItem(StudentTab.GAMES, "Games", Icons.Rounded.SportsEsports),
            StudentTabItem(StudentTab.LEADERBOARD, "Ranks", Icons.Rounded.EmojiEvents),
            StudentTabItem(StudentTab.PROFILE, "Profile", Icons.Rounded.Person)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            studentTabs.forEach { item ->
                val isSelected = currentTab == item.tab
                val isCenterPractice = item.tab == StudentTab.PRACTICE

                if (isCenterPractice) {
                    // Center Hero Action: Elevated Radiant Mic Button for Speaking Hub
                    Column(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onTabSelected(item.tab) }
                            .padding(horizontal = 2.dp, vertical = 2.dp)
                            .testTag("student_tab_${item.tab.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            if (isSelected) Color(0xFF00E5FF) else Color(0xFF0072FF),
                                            if (isSelected) Color(0xFFD946EF) else Color(0xFF8B5CF6)
                                        )
                                    )
                                )
                                .border(
                                    1.5.dp,
                                    if (isSelected) Color(0xFFFDE047) else Color.White.copy(alpha = 0.7f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSelected) Color(0xFF38BDF8) else Color(0xFFC084FC),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    // Standard Tab Item with dynamic active capsule pill and notification badge
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onTabSelected(item.tab) }
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                            .testTag("student_tab_${item.tab.name.lowercase()}"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier.size(width = 44.dp, height = 30.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(15.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color(0xFF8B5CF6).copy(alpha = 0.45f),
                                                    Color(0xFF6366F1).copy(alpha = 0.60f)
                                                )
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            Color(0xFFA855F7).copy(alpha = 0.6f),
                                            RoundedCornerShape(15.dp)
                                        )
                                )
                            }

                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(20.dp)
                            )

                            // Pending exams notification badge dot
                            if (item.tab == StudentTab.EXAMS && pendingTasksCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-4).dp, y = 2.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF4444))
                                        .border(1.dp, Color.White, CircleShape)
                                )
                            }
                        }

                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = item.label,
                            fontSize = 9.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private data class StudentTabItem(
    val tab: StudentTab,
    val label: String,
    val icon: ImageVector
)

data class TeacherTabItem(
    val tab: TeacherTab,
    val label: String,
    val icon: ImageVector
)

@Composable
fun TeacherBottomBar(
    currentTab: TeacherTab,
    onTabSelected: (TeacherTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF070A24).copy(alpha = 0.96f),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.2.dp, Color(0xFF6366F1).copy(alpha = 0.60f)),
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("teacher_bottom_bar")
    ) {
        val teacherTabs = listOf(
            TeacherTabItem(TeacherTab.DASHBOARD, "Overview", Icons.Rounded.BarChart),
            TeacherTabItem(TeacherTab.STUDENTS, "Students", Icons.Rounded.People),
            TeacherTabItem(TeacherTab.GAMES, "Games Hub", Icons.Rounded.SportsEsports),
            TeacherTabItem(TeacherTab.SPEAKING_REVIEW, "Speaking &\nReview", Icons.Rounded.Mic),
            TeacherTabItem(TeacherTab.EXAMS, "Exams", Icons.Rounded.Quiz)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            teacherTabs.forEach { item ->
                val isSelected = currentTab == item.tab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(item.tab) }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .testTag("teacher_tab_${item.tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(width = 60.dp, height = 34.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(width = 60.dp, height = 34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = item.label,
                        fontSize = if (item.tab == TeacherTab.SPEAKING_REVIEW) 8.5.sp else 9.5.sp,
                        lineHeight = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

data class AdminTabItem(
    val tab: AdminTab,
    val label: String,
    val icon: ImageVector
)

@Composable
fun AdminBottomBar(
    currentTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF070A24).copy(alpha = 0.96f),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.2.dp, Color(0xFF6366F1).copy(alpha = 0.60f)),
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("admin_bottom_bar")
    ) {
        val adminTabs = listOf(
            AdminTabItem(AdminTab.DASHBOARD, "Dashboard", Icons.Rounded.Home),
            AdminTabItem(AdminTab.STUDENTS, "Students", Icons.Rounded.School),
            AdminTabItem(AdminTab.GAMES, "Games Hub", Icons.Rounded.SportsEsports),
            AdminTabItem(AdminTab.TEACHERS, "Faculty", Icons.Rounded.RecordVoiceOver),
            AdminTabItem(AdminTab.CURRICULUM, "Courses", Icons.Rounded.MenuBook)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            adminTabs.forEach { item ->
                val isSelected = currentTab == item.tab
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onTabSelected(item.tab) }
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                        .testTag("admin_tab_${item.tab.name.lowercase()}"),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(width = 60.dp, height = 34.dp)
                                .clip(RoundedCornerShape(17.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(width = 60.dp, height = 34.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        text = item.label,
                        fontSize = 9.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

