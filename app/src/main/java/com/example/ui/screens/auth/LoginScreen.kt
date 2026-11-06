package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
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

enum class AuthMode {
    SIGN_IN,
    CREATE_ACCOUNT
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }

    // Unified Sign In States
    var email by remember { mutableStateOf("") }
    var securityPin by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Student Registration States
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regClass by remember { mutableStateOf(com.example.data.model.AcademicClasses.DEFAULT_CLASS) }
    var regPin by remember { mutableStateOf("") }
    var regConfirmPin by remember { mutableStateOf("") }
    var regPinVisible by remember { mutableStateOf(false) }
    var regError by remember { mutableStateOf<String?>(null) }

    val allowStudentReg by viewModel.allowStudentRegistration.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .premiumAppBackground()
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
            verticalArrangement = Arrangement.spacedBy(18.dp)
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
                    text = "Spoken English Learning & Academic Portal",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MutedGray,
                    textAlign = TextAlign.Center
                )
            }

            // Mode Selector: Sign In vs Create Account
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val isSignIn = authMode == AuthMode.SIGN_IN
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSignIn) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (isSignIn) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                authMode = AuthMode.SIGN_IN
                                loginError = null
                            }
                            .testTag("tab_sign_in")
                    ) {
                        Text(
                            text = "Sign In",
                            fontSize = 13.sp,
                            fontWeight = if (isSignIn) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSignIn) RoyalBlue else MutedGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    val isRegister = authMode == AuthMode.CREATE_ACCOUNT
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isRegister) MaterialTheme.colorScheme.surface else Color.Transparent,
                        shadowElevation = if (isRegister) 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                authMode = AuthMode.CREATE_ACCOUNT
                                regError = null
                            }
                            .testTag("tab_create_account")
                    ) {
                        Text(
                            text = "Create Student Account",
                            fontSize = 13.sp,
                            fontWeight = if (isRegister) FontWeight.Bold else FontWeight.Medium,
                            color = if (isRegister) RoyalBlue else MutedGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }

            // UNIFIED SIGN-IN FORM
            if (authMode == AuthMode.SIGN_IN) {
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
                        Column {
                            Text(
                                text = "Sign In to Your Account",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enter your email and security PIN to access your portal",
                                fontSize = 12.sp,
                                color = MutedGray
                            )
                        }

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                loginError = null
                            },
                            label = { Text("Email or User ID") },
                            placeholder = { Text("e.g. name@linguaquest.edu") },
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

                        // Security PIN Field (Masked by default, never shown on screen)
                        OutlinedTextField(
                            value = securityPin,
                            onValueChange = {
                                securityPin = it
                                loginError = null
                            },
                            label = { Text("Security PIN") },
                            placeholder = { Text("Enter your security PIN") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Lock, contentDescription = null, tint = RoyalBlue)
                            },
                            trailingIcon = {
                                IconButton(onClick = { pinVisible = !pinVisible }) {
                                    Icon(
                                        imageVector = if (pinVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                        contentDescription = if (pinVisible) "Hide PIN" else "Show PIN"
                                    )
                                }
                            },
                            visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
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

                        // Demo Accounts Quick Fill
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Quick Demo Accounts (Tap to auto-fill):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MutedGray
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            email = "alex.morgan@linguaquest.edu"
                                            securityPin = "1234"
                                            loginError = null
                                        }
                                        .testTag("demo_student_btn")
                                ) {
                                    Text(
                                        text = "👨‍🎓 Student",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = RoyalBlue,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            email = "sarah.jenkins@linguaquest.edu"
                                            securityPin = "7321"
                                            loginError = null
                                        }
                                        .testTag("demo_teacher_btn")
                                ) {
                                    Text(
                                        text = "👩‍🏫 Faculty",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldSuccess,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFFEF3C7),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            email = "admin@linguaquest.edu"
                                            securityPin = "9842"
                                            loginError = null
                                        }
                                        .testTag("demo_admin_btn")
                                ) {
                                    Text(
                                        text = "🛡️ Admin",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        // Single Unified Login Button
                        Button(
                            onClick = {
                                val result = viewModel.unifiedLogin(email, securityPin)
                                if (result is MainViewModel.UnifiedLoginResult.Error) {
                                    loginError = result.message
                                } else {
                                    loginError = null
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "Sign In",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Link to Create Student Account
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = {
                                    authMode = AuthMode.CREATE_ACCOUNT
                                    regError = null
                                }
                            ) {
                                Text(
                                    text = "New Student? Create an account now",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoyalBlue
                                )
                            }
                        }
                    }
                }

                // Security & Auto-Detection Information Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Filled.Shield, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                            Text(
                                text = "Universal Single Sign-On",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = "Speakly automatically routes you to the Administrator, Faculty, or Student portal upon verifying your registered email and PIN.",
                            fontSize = 11.sp,
                            color = MutedGray,
                            lineHeight = 15.sp
                        )
                    }
                }
            } else {
                // CREATE STUDENT ACCOUNT FORM (ONLY FOR STUDENTS)
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
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("🦉", fontSize = 22.sp)
                                Text(
                                    text = "Create Student Account",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "Register your learner profile to access spoken English roleplays and games",
                                fontSize = 12.sp,
                                color = MutedGray
                            )
                        }

                        // Check Permission
                        if (!allowStudentReg) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFFEF2F2),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CoralAccent),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Filled.Lock, contentDescription = null, tint = CoralAccent, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "Student Registration Restricted",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = CoralAccent
                                        )
                                    }
                                    Text(
                                        text = "Self-registration for students is currently closed by the Institution Administrator. Please contact academic staff to be enrolled.",
                                        fontSize = 12.sp,
                                        color = DeepNavy
                                    )
                                }
                            }
                        } else {
                            // Full Name Field
                            OutlinedTextField(
                                value = regName,
                                onValueChange = {
                                    regName = it
                                    regError = null
                                },
                                label = { Text("Full Name") },
                                placeholder = { Text("e.g. Jordan Smith") },
                                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = RoyalBlue) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_student_name"),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true
                            )

                            // Email Address Field
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = {
                                    regEmail = it
                                    regError = null
                                },
                                label = { Text("Email Address") },
                                placeholder = { Text("e.g. jordan.s@example.com") },
                                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = RoyalBlue) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_student_email"),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            // Select Class Selector
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Rounded.School, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                                    Text(
                                        text = "Select Your Class:",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "($regClass)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = RoyalBlue
                                    )
                                }

                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    com.example.data.model.AcademicClasses.ALL_CLASSES.forEach { className ->
                                        val isSelected = regClass == className
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                            border = BorderStroke(
                                                1.2.dp,
                                                if (isSelected) RoyalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                                            ),
                                            modifier = Modifier
                                                .clickable {
                                                    regClass = className
                                                    regError = null
                                                }
                                                .testTag("select_class_${className.lowercase().replace(" ", "_")}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (isSelected) {
                                                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                }
                                                Text(
                                                    text = className,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Set Security PIN Field
                            OutlinedTextField(
                                value = regPin,
                                onValueChange = {
                                    regPin = it
                                    regError = null
                                },
                                label = { Text("Set Security PIN (4-6 digits)") },
                                placeholder = { Text("Choose a private PIN") },
                                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = RoyalBlue) },
                                trailingIcon = {
                                    IconButton(onClick = { regPinVisible = !regPinVisible }) {
                                        Icon(
                                            imageVector = if (regPinVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                            contentDescription = if (regPinVisible) "Hide PIN" else "Show PIN"
                                        )
                                    }
                                },
                                visualTransformation = if (regPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )

                            // Confirm Security PIN Field
                            OutlinedTextField(
                                value = regConfirmPin,
                                onValueChange = {
                                    regConfirmPin = it
                                    regError = null
                                },
                                label = { Text("Confirm Security PIN") },
                                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = RoyalBlue) },
                                visualTransformation = if (regPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )

                            // Error Banner
                            AnimatedVisibility(visible = regError != null) {
                                regError?.let { err ->
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
                                            Icon(Icons.Filled.Error, contentDescription = null, tint = CoralAccent, modifier = Modifier.size(16.dp))
                                            Text(err, color = CoralAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Register Button
                            Button(
                                onClick = {
                                    if (regName.isBlank() || regEmail.isBlank() || regPin.isBlank()) {
                                        regError = "Please fill in all required fields."
                                    } else if (regPin.trim().length < 4) {
                                        regError = "Security PIN must be at least 4 digits."
                                    } else if (regPin.trim() != regConfirmPin.trim()) {
                                        regError = "Security PINs do not match. Please re-enter."
                                    } else {
                                        val result = viewModel.registerStudent(regName, regEmail, regClass, regPin)
                                        if (result is MainViewModel.UnifiedLoginResult.Error) {
                                            regError = result.message
                                        } else {
                                            regError = null
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("register_student_button"),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Create Student Account & Enter",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Faculty note
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Rounded.Info, contentDescription = null, tint = MutedGray, modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Faculty instructors are added directly by the Administrator with their assigned security PIN.",
                                    fontSize = 11.sp,
                                    color = MutedGray,
                                    lineHeight = 14.sp
                                )
                            }
                        }

                        // Switch back to Sign In
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            TextButton(
                                onClick = {
                                    authMode = AuthMode.SIGN_IN
                                    loginError = null
                                }
                            ) {
                                Text(
                                    text = "Already have an account? Sign In",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = RoyalBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
