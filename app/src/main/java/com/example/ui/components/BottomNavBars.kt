package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F123C).copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        border = BorderStroke(1.2.dp, Color(0xFF4C3FA8).copy(alpha = 0.65f)),
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("student_bottom_bar")
    ) {
        val visibleTabs = listOf(
            StudentTabItem(StudentTab.HOME, "Home", Icons.Rounded.Home),
            StudentTabItem(StudentTab.TASKS, "Tasks", Icons.Rounded.Task),
            StudentTabItem(StudentTab.GAMES, "Games", Icons.Rounded.Gamepad),
            StudentTabItem(StudentTab.LEADERBOARD, "Ranks", Icons.Rounded.EmojiEvents),
            StudentTabItem(StudentTab.PROFILE, "Profile", Icons.Rounded.Person)
        )

        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            visibleTabs.forEach { tabItem ->
                val isSelected = currentTab == tabItem.tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tabItem.tab) },
                    icon = {
                        Icon(
                            imageVector = tabItem.icon,
                            contentDescription = tabItem.label,
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    label = {
                        Text(
                            tabItem.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = Color(0xFF7C3AED), // Vibrant Purple active pill from screenshot
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("student_tab_${tabItem.tab.name.lowercase()}")
                )
            }
        }
    }
}

private data class StudentTabItem(
    val tab: StudentTab,
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
        color = Color(0xFF0F173C),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = BorderStroke(1.dp, Color(0xFF283B70).copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("teacher_bottom_bar")
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            TeacherTab.values().forEach { tab ->
                val isSelected = currentTab == tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Text(
                            tab.iconEmoji,
                            fontSize = if (isSelected) 22.sp else 18.sp
                        )
                    },
                    label = {
                        Text(
                            tab.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = Color(0xFF7C3AED),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("teacher_tab_${tab.name.lowercase()}")
                )
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

