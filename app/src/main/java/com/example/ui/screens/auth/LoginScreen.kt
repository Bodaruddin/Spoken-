package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Role
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf(Role.STUDENT) }
    var email by remember { mutableStateOf("alex.morgan@linguaquest.edu") }
    var password by remember { mutableStateOf("student123") }
    var passwordVisible by remember { mutableStateOf(false) }

    var loginError by remember { mutableStateOf<String?>(null) }

    // Update default prefilled email & password when switching role
    fun updateRoleCredentials(role: Role) {
        selectedRole = role
        loginError = null
        when (role) {
            Role.STUDENT -> {
                email = "alex.morgan@linguaquest.edu"
                password = "student123"
            }
            Role.TEACHER -> {
                email = "e.vance@linguaquest.edu"
                password = ""
            }
            Role.ADMIN -> {
                email = "admin@linguaquest.edu"
                password = ""
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("login_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // App Branding Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            Brush.linearGradient(listOf(RoyalBlue, PurpleAccent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_mascot),
                        contentDescription = "Speakly Mascot",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Text(
                    text = "Speakly",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepNavy,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = "Spoken English Learning & Gamified Education",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MutedGray,
                    textAlign = TextAlign.Center
                )
            }

            // Role Selection Tabs Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Select Portal to Log In:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RoleTabButton(
                            title = "Student",
                            emoji = "🦉",
                            isSelected = selectedRole == Role.STUDENT,
                            modifier = Modifier.weight(1f),
                            onClick = { updateRoleCredentials(Role.STUDENT) }
                        )

                        RoleTabButton(
                            title = "Teacher",
                            emoji = "👩‍🏫",
                            isSelected = selectedRole == Role.TEACHER,
                            modifier = Modifier.weight(1f),
                            onClick = { updateRoleCredentials(Role.TEACHER) }
                        )

                        RoleTabButton(
                            title = "Admin",
                            emoji = "🛡️",
                            isSelected = selectedRole == Role.ADMIN,
                            modifier = Modifier.weight(1f),
                            onClick = { updateRoleCredentials(Role.ADMIN) }
                        )
                    }

                    // Role Description Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (selectedRole) {
                            Role.STUDENT -> Color(0xFFEFF6FF)
                            Role.TEACHER -> Color(0xFFF3E8FF)
                            Role.ADMIN -> Color(0xFFFEF3C7)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = when (selectedRole) {
                                    Role.STUDENT -> "🦉"
                                    Role.TEACHER -> "👩‍🏫"
                                    Role.ADMIN -> "🛡️"
                                },
                                fontSize = 20.sp
                            )
                            Column {
                                Text(
                                    text = when (selectedRole) {
                                        Role.STUDENT -> "Student Learning Portal"
                                        Role.TEACHER -> "Teacher & Faculty Portal"
                                        Role.ADMIN -> "Global Administration Portal"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = when (selectedRole) {
                                        Role.STUDENT -> RoyalBlueDark
                                        Role.TEACHER -> Color(0xFF7E22CE)
                                        Role.ADMIN -> Color(0xFFB45309)
                                    }
                                )
                                Text(
                                    text = when (selectedRole) {
                                        Role.STUDENT -> "AI speaking partner, journey map, roleplays & 12 mini games"
                                        Role.TEACHER -> "Manage batches, review voice drills, publish game challenges & assignments"
                                        Role.ADMIN -> "Full institutional control, curriculum, database connection & faculty"
                                    },
                                    fontSize = 11.sp,
                                    color = DeepNavy
                                )
                            }
                        }
                    }
                }
            }

            // Login Form Card
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Sign in to ${selectedRole.name.lowercase().replaceFirstChar { it.uppercase() }} Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    // Email Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email or ID") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Email, contentDescription = null, tint = RoyalBlue)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_email_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lock, contentDescription = null, tint = RoyalBlue)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    // Error Banner
                    AnimatedVisibility(visible = loginError != null) {
                        loginError?.let { err ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEE2E2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CoralAccent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(Icons.Filled.Lock, contentDescription = null, tint = CoralAccent, modifier = Modifier.size(16.dp))
                                    Text(err, color = CoralAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Primary Login Button
                    Button(
                        onClick = {
                            val success = viewModel.authenticateAndLogin(selectedRole, password)
                            if (!success) {
                                loginError = when (selectedRole) {
                                    Role.ADMIN -> "Access Denied: Incorrect Admin password or PIN."
                                    Role.TEACHER -> "Access Denied: Incorrect Teacher password or PIN."
                                    else -> "Invalid credentials."
                                }
                            } else {
                                loginError = null
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (selectedRole) {
                                Role.STUDENT -> RoyalBlue
                                else -> DeepNavy
                            }
                        )
                    ) {
                        Text(
                            text = "Sign In as ${selectedRole.name.lowercase().replaceFirstChar { it.uppercase() }}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // 1-Click Fast Demo Login (STUDENTS ONLY)
                    if (selectedRole == Role.STUDENT) {
                        OutlinedButton(
                            onClick = {
                                viewModel.authenticateAndLogin(Role.STUDENT, "student123")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("one_click_demo_login_button"),
                            shape = RoundedCornerShape(14.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, RoyalBlue)
                        ) {
                            Icon(
                                Icons.Rounded.FlashOn,
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Quick 1-Click Enter as Student",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        // Staff Protected Notice
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF1F5F9),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Shield, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                                Text(
                                    text = if (selectedRole == Role.ADMIN)
                                        "Admin Access Protected: Password or PIN 9842 required."
                                    else
                                        "Faculty Access Protected: Password or PIN 7321 required.",
                                    fontSize = 11.sp,
                                    color = MutedGray,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Demo Credentials Information Box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Access & Security Policy:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    DemoCredentialRow("Student:", "alex.morgan@linguaquest.edu (Public access for all learners)") {
                        updateRoleCredentials(Role.STUDENT)
                    }
                    Text(
                        text = "🔐 Teacher & Admin portals are secured against unauthorized access. Faculty and Administrator credentials or security PINs are strictly required.",
                        fontSize = 11.sp,
                        color = MutedGray,
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}

@Composable
fun RoleTabButton(
    title: String,
    emoji: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        modifier = modifier
            .clickable { onClick() }
            .testTag("role_tab_${title.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(emoji, fontSize = 20.sp)
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DemoCredentialRow(roleLabel: String, credentialText: String, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onSelect() }
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(roleLabel, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
        Text(credentialText, fontSize = 11.sp, color = MutedGray)
    }
}
