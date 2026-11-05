package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Role
import com.example.ui.MainViewModel
import com.example.ui.StudentTab
import com.example.ui.components.*
import com.example.ui.screens.admin.AdminMainScreen
import com.example.ui.screens.student.*
import com.example.ui.screens.teacher.TeacherMainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                LinguaQuestApp()
            }
        }
    }
}

@Composable
fun LinguaQuestApp(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val studentProfile by viewModel.studentProfile.collectAsState()
    val currentStudentTab by viewModel.currentStudentTab.collectAsState()
    val currentTeacherTab by viewModel.currentTeacherTab.collectAsState()
    val currentAdminTab by viewModel.currentAdminTab.collectAsState()
    val showRoleSwitcher by viewModel.showRoleSwitcher.collectAsState()
    val showSupabaseModal by viewModel.showSupabaseModal.collectAsState()
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val celebrationReward by viewModel.celebrationReward.collectAsState()

    // Runtime Permission for Microphone Audio Recording
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasAudioPermission) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    if (!isLoggedIn) {
        // Dedicated Login Screen with Student, Teacher, and Admin options
        com.example.ui.screens.auth.LoginScreen(viewModel = viewModel)
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppTopBar(
                    role = currentUser.role,
                    streak = studentProfile.streakDays,
                    xp = studentProfile.xpPoints,
                    coins = studentProfile.coins,
                    onRoleClick = { viewModel.toggleRoleSwitcher(true) },
                    onCloudClick = { viewModel.toggleSupabaseModal(true) },
                    isCloudConnected = supabaseStatus is com.example.data.remote.SupabaseConnectionState.Connected,
                    onLogoutClick = { viewModel.logout() }
                )
            },
            bottomBar = {
                when (currentUser.role) {
                    Role.STUDENT -> {
                        StudentBottomBar(
                            currentTab = currentStudentTab,
                            onTabSelected = { viewModel.selectStudentTab(it) }
                        )
                    }
                    Role.TEACHER -> {
                        TeacherBottomBar(
                            currentTab = currentTeacherTab,
                            onTabSelected = { viewModel.selectTeacherTab(it) }
                        )
                    }
                    Role.ADMIN -> {
                        AdminBottomBar(
                            currentTab = currentAdminTab,
                            onTabSelected = { viewModel.selectAdminTab(it) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentUser.role) {
                    Role.STUDENT -> {
                        when (currentStudentTab) {
                            StudentTab.HOME -> StudentHomeScreen(viewModel = viewModel)
                            StudentTab.LEARN -> LearningJourneyScreen(viewModel = viewModel)
                            StudentTab.PRACTICE -> SpeakingHubScreen(viewModel = viewModel)
                            StudentTab.GAMES -> GamesHubScreen(viewModel = viewModel)
                            StudentTab.PROFILE -> StudentProfileScreen(viewModel = viewModel)
                        }
                    }
                    Role.TEACHER -> {
                        TeacherMainScreen(viewModel = viewModel)
                    }
                    Role.ADMIN -> {
                        AdminMainScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }

    // Supabase Cloud Connect Modal
    if (showSupabaseModal) {
        SupabaseConnectDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.toggleSupabaseModal(false) }
        )
    }

    // Role Switcher Dialog Modal
    if (showRoleSwitcher) {
        RoleSwitcherDialog(
            currentRole = currentUser.role,
            onRoleSelected = { viewModel.switchRole(it) },
            onDismiss = { viewModel.toggleRoleSwitcher(false) },
            onLogoutClick = { viewModel.logout() }
        )
    }

    // Celebration & Gamification Reward Modal
    celebrationReward?.let { (xp, coins) ->
        CelebrationDialog(
            xp = xp,
            coins = coins,
            onDismiss = { viewModel.dismissCelebration() }
        )
    }
}
