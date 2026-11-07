package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
        color = Color(0xFF0F173C),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = BorderStroke(1.dp, Color(0xFF283B70).copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("student_bottom_bar")
    ) {
        val visibleTabs = listOf(
            StudentTab.HOME,
            StudentTab.TASKS,
            StudentTab.GAMES,
            StudentTab.LEADERBOARD,
            StudentTab.PROFILE
        )

        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            visibleTabs.forEach { tab ->
                val isSelected = currentTab == tab
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onTabSelected(tab) },
                    icon = {
                        Text(
                            tab.iconEmoji,
                            fontSize = if (isSelected) 20.sp else 16.sp
                        )
                    },
                    label = {
                        Text(
                            tab.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            maxLines = 1,
                            softWrap = false
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = Color(0xFF4F46E5),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("student_tab_${tab.name.lowercase()}")
                )
            }
        }
    }
}

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

@Composable
fun AdminBottomBar(
    currentTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF0F173C),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = BorderStroke(1.dp, Color(0xFF283B70).copy(alpha = 0.6f)),
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .testTag("admin_bottom_bar")
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            AdminTab.values().forEach { tab ->
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
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color.White,
                        indicatorColor = Color(0xFF4F46E5),
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    ),
                    modifier = Modifier.testTag("admin_tab_${tab.name.lowercase()}")
                )
            }
        }
    }
}
