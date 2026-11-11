package com.example.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
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
import com.example.ui.MainViewModel
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class AuthMode {
    SIGN_IN,
    CREATE_ACCOUNT
}

/**
 * Laurel branch wreath illustration flanking the Speakly owl mascot.
 */
@Composable
fun LaurelBranch(
    isLeft: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(width = 38.dp, height = 58.dp)) {
        val w = size.width
        val h = size.height
        val leafColor = Color(0xFFA78BFA) // Soft lavender/purple

        // Stem curve
        val stemPath = Path().apply {
            if (isLeft) {
                moveTo(w * 0.85f, h * 0.95f)
                cubicTo(w * 0.20f, h * 0.70f, w * 0.15f, h * 0.30f, w * 0.80f, h * 0.05f)
            } else {
                moveTo(w * 0.15f, h * 0.95f)
                cubicTo(w * 0.80f, h * 0.70f, w * 0.85f, h * 0.30f, w * 0.20f, h * 0.05f)
            }
        }
        drawPath(
            path = stemPath,
            color = leafColor.copy(alpha = 0.85f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        // Leaf pairs along the stem
        val progressPoints = listOf(0.18f, 0.38f, 0.58f, 0.78f, 0.95f)
        for (p in progressPoints) {
            val cx = if (isLeft) w * (0.85f - p * 0.55f) else w * (0.15f + p * 0.55f)
            val cy = h * (0.95f - p * 0.88f)

            // Leaf 1
            drawOval(
                color = leafColor,
                topLeft = Offset(cx - 5.dp.toPx(), cy - 3.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(10.dp.toPx(), 6.dp.toPx())
            )
            // Leaf 2
            drawOval(
                color = leafColor.copy(alpha = 0.85f),
                topLeft = Offset(cx - 2.dp.toPx(), cy - 6.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(9.dp.toPx(), 5.dp.toPx())
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }

    // Unified Sign In States
    val coroutineScope = rememberCoroutineScope()
    var isAuthenticating by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf<com.example.data.model.Role?>(null) }
    var email by remember { mutableStateOf("") }
    var securityPin by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Academic Classes from Repository
    val academicClasses by viewModel.academicClasses.collectAsState()

    // Student Registration States
    var regName by remember { mutableStateOf("") }
    var regEmail by remember { mutableStateOf("") }
    var regClass by remember(academicClasses) {
        mutableStateOf(academicClasses.firstOrNull() ?: com.example.data.model.AcademicClasses.DEFAULT_CLASS)
    }
    var regClassDropdownExpanded by remember { mutableStateOf(false) }
    var regPin by remember { mutableStateOf("") }
    var regConfirmPin by remember { mutableStateOf("") }
    var regPinVisible by remember { mutableStateOf(false) }
    var regError by remember { mutableStateOf<String?>(null) }

    val allowStudentReg by viewModel.allowStudentRegistration.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF070921), // Midnight dark blue
                        Color(0xFF0F0E38), // Royal purple navy
                        Color(0xFF090620)  // Obsidian navy
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("login_screen")
    ) {
        // Fluid neon background glow ribbons matching screenshot
        Canvas(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val w = size.width
            val h = size.height

            // Top-left magenta ribbon
            val pTopLeft = Path().apply {
                moveTo(0f, h * 0.16f)
                cubicTo(w * 0.35f, h * 0.08f, w * 0.60f, h * 0.02f, w * 0.85f, 0f)
                lineTo(0f, 0f)
                close()
            }
            drawPath(
                pTopLeft,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFFEC4899).copy(alpha = 0.22f),
                        Color(0xFF8B5CF6).copy(alpha = 0.14f),
                        Color.Transparent
                    )
                )
            )

            // Bottom-right cyan/magenta wave ribbon
            val pBottom = Path().apply {
                moveTo(0f, h * 0.90f)
                cubicTo(w * 0.30f, h * 0.84f, w * 0.65f, h * 0.96f, w, h * 0.86f)
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(
                pBottom,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF00D2FF).copy(alpha = 0.18f),
                        Color(0xFF8B5CF6).copy(alpha = 0.20f),
                        Color(0xFFEC4899).copy(alpha = 0.16f)
                    )
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. App Mascot with Laurel Wreath Branches & Title (Matching Screenshot)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    LaurelBranch(isLeft = true)

                    Spacer(Modifier.width(8.dp))

                    // Mascot container with neon gradient border
                    Surface(
                        shape = RoundedCornerShape(26.dp),
                        color = Color.Transparent,
                        border = BorderStroke(
                            2.dp,
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF00D2FF),
                                    Color(0xFF818CF8),
                                    Color(0xFFEC4899),
                                    Color(0xFF00D2FF)
                                )
                            )
                        ),
                        modifier = Modifier.size(94.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_mascot),
                            contentDescription = "Speakly Mascot",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(24.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(Modifier.width(8.dp))

                    LaurelBranch(isLeft = false)
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Speakly",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )

                Text(
                    text = "Spoken English Learning & Academic Portal",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.95f),
                    textAlign = TextAlign.Center
                )
            }

            // 2. Mode Selector: Sign In vs Create Student Account (Matching Screenshot)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF080D2C).copy(alpha = 0.92f),
                border = BorderStroke(1.2.dp, Color(0xFF2E2368).copy(alpha = 0.75f)),
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
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSignIn) Color.Transparent else Color.Transparent,
                        border = if (isSignIn) BorderStroke(1.2.dp, Color(0xFFC084FC).copy(alpha = 0.85f)) else null,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isSignIn) {
                                    Modifier.background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
                                        )
                                    )
                                } else Modifier
                            )
                            .clickable {
                                authMode = AuthMode.SIGN_IN
                                loginError = null
                            }
                            .testTag("tab_sign_in")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Login,
                                contentDescription = null,
                                tint = if (isSignIn) Color.White else Color(0xFF94A3B8),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Sign In",
                                fontSize = 13.5.sp,
                                fontWeight = if (isSignIn) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSignIn) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }

                    val isRegister = authMode == AuthMode.CREATE_ACCOUNT
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isRegister) Color.Transparent else Color.Transparent,
                        border = if (isRegister) BorderStroke(1.2.dp, Color(0xFFC084FC).copy(alpha = 0.85f)) else null,
                        modifier = Modifier
                            .weight(1.3f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .then(
                                if (isRegister) {
                                    Modifier.background(
                                        Brush.horizontalGradient(
                                            listOf(Color(0xFF7C3AED), Color(0xFF6D28D9))
                                        )
                                    )
                                } else Modifier
                            )
                            .clickable {
                                authMode = AuthMode.CREATE_ACCOUNT
                                regError = null
                            }
                            .testTag("tab_create_account")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PersonAdd,
                                contentDescription = null,
                                tint = if (isRegister) Color.White else Color(0xFF38BDF8),
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = "Create Student Account",
                                fontSize = 12.5.sp,
                                fontWeight = if (isRegister) FontWeight.Bold else FontWeight.Medium,
                                color = if (isRegister) Color.White else Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            // 3. PRIMARY SIGN-IN CARD (Matching Screenshot)
            if (authMode == AuthMode.SIGN_IN) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070D30).copy(alpha = 0.95f)),
                    border = BorderStroke(
                        1.8.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF00D2FF),
                                Color(0xFF818CF8),
                                Color(0xFFEC4899),
                                Color(0xFF00D2FF)
                            )
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(15.dp)
                    ) {
                        // Header with Shield icon in glowing squircle
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.2.dp, Color(0xFF93C5FD)),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF3B82F6), Color(0xFF6366F1))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Security,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "Sign In to Your Account",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Enter your email and security PIN to access your portal",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Portal Target Selector Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Login Portal Target",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFFA5B4FC)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                val roles = listOf(
                                    null to "Auto-Detect",
                                    com.example.data.model.Role.STUDENT to "Student",
                                    com.example.data.model.Role.TEACHER to "Faculty",
                                    com.example.data.model.Role.ADMIN to "Admin"
                                )
                                roles.forEach { (role, label) ->
                                    val isSelected = selectedRole == role
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) Color(0xFF7C3AED) else Color(0xFF070F35),
                                        border = BorderStroke(1.2.dp, if (isSelected) Color(0xFF00D2FF) else Color(0xFF2E3B70)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { selectedRole = role }
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp),
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Quick Demo Fill Chips
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚡ Quick Demo One-Tap",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                                Text(
                                    text = "Tap to Fill & Test",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF0F1E4A),
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            email = "alex.morgan@linguaquest.edu"
                                            securityPin = "1234"
                                            selectedRole = com.example.data.model.Role.STUDENT
                                            loginError = null
                                        }
                                        .testTag("quick_fill_student")
                                ) {
                                    Text(
                                        "🎓 Student",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF1E1446),
                                    border = BorderStroke(1.dp, Color(0xFFA855F7)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            email = "e.vance@linguaquest.edu"
                                            securityPin = "7321"
                                            selectedRole = com.example.data.model.Role.TEACHER
                                            loginError = null
                                        }
                                        .testTag("quick_fill_teacher")
                                ) {
                                    Text(
                                        "👩‍🏫 Faculty",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF2E1C0A),
                                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            email = "admin@linguaquest.edu"
                                            securityPin = "1234"
                                            selectedRole = com.example.data.model.Role.ADMIN
                                            loginError = null
                                        }
                                        .testTag("quick_fill_admin")
                                ) {
                                    Text(
                                        "🛡️ Admin",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp)
                                    )
                                }
                            }
                        }

                        // Email or User ID Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = {
                                email = it
                                loginError = null
                            },
                            placeholder = { Text("Email or User ID", color = Color(0xFF64748B), fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Email,
                                    contentDescription = null,
                                    tint = Color(0xFF00D2FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_email_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF06143A),
                                unfocusedContainerColor = Color(0xFF06143A),
                                focusedBorderColor = Color(0xFF00D2FF),
                                unfocusedBorderColor = Color(0xFF00D2FF).copy(alpha = 0.65f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF00D2FF)
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )

                        // Security PIN Field
                        OutlinedTextField(
                            value = securityPin,
                            onValueChange = {
                                securityPin = it
                                loginError = null
                            },
                            placeholder = { Text("Security PIN", color = Color(0xFF64748B), fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFF00D2FF),
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { pinVisible = !pinVisible }) {
                                    Icon(
                                        imageVector = if (pinVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                        contentDescription = if (pinVisible) "Hide PIN" else "Show PIN",
                                        tint = Color(0xFF94A3B8)
                                    )
                                }
                            },
                            visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("login_password_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF06143A),
                                unfocusedContainerColor = Color(0xFF06143A),
                                focusedBorderColor = Color(0xFF00D2FF),
                                unfocusedBorderColor = Color(0xFF00D2FF).copy(alpha = 0.65f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF00D2FF)
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                        )

                        // Error Banner
                        AnimatedVisibility(visible = loginError != null) {
                            loginError?.let { err ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFF3F1318),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(16.dp))
                                        Text(err, color = Color(0xFFFEE2E2), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Gradient Sign In Button with Supabase remote authentication
                        Button(
                            onClick = {
                                if (!isAuthenticating) {
                                    isAuthenticating = true
                                    loginError = null
                                    coroutineScope.launch {
                                        val result = viewModel.unifiedLoginAsync(email, securityPin, selectedRole)
                                        isAuthenticating = false
                                        if (result is MainViewModel.UnifiedLoginResult.Error) {
                                            loginError = result.message
                                        } else {
                                            loginError = null
                                        }
                                    }
                                }
                            },
                            enabled = !isAuthenticating,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_submit_button"),
                            shape = RoundedCornerShape(25.dp),
                            contentPadding = PaddingValues(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFFA855F7), // Purple/Magenta
                                                Color(0xFF7C3AED), // Violet
                                                Color(0xFF00D2FF)  // Electric Cyan
                                            )
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAuthenticating) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Text(
                                            text = "Connecting to Supabase Database...",
                                            fontSize = 13.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Rounded.Login,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = if (selectedRole != null) "Sign In as ${selectedRole?.name}" else "Sign In",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Link to Create Student Account
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "New Student? ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFCBD5E1)
                            )
                            Text(
                                text = "Create an account now ➔",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                modifier = Modifier
                                    .clickable {
                                        authMode = AuthMode.CREATE_ACCOUNT
                                        regError = null
                                    }
                                    .testTag("link_create_account")
                            )
                        }
                    }
                }

                // 4. Universal Single Sign-On Information Card (Matching Screenshot)
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F38).copy(alpha = 0.95f)),
                    border = BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(
                            listOf(Color(0xFF6366F1), Color(0xFFA855F7), Color(0xFF00D2FF))
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.2.dp, Color(0xFF60A5FA)),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF2563EB), Color(0xFF3B82F6))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.VerifiedUser,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Box(
                            modifier = Modifier
                                .height(44.dp)
                                .width(2.dp)
                                .clip(RoundedCornerShape(1.dp))
                                .background(Color(0xFF00D2FF).copy(alpha = 0.7f))
                        )

                        Spacer(Modifier.width(12.dp))

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = "Universal Single Sign-On",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "Speakly automatically routes you to the Administrator, Faculty, or Student portal upon verifying your registered email and PIN.",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFCBD5E1),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                // 5. Bottom Academic Illustration (3D Stack of books, graduation cap, and glowing laptop)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_academic_laptop_books),
                        contentDescription = "Academic Learning Portal",
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(160.dp)
                            .clip(RoundedCornerShape(18.dp)),
                        contentScale = ContentScale.Fit
                    )
                }
            } else {
                // CREATE STUDENT ACCOUNT FORM (STYLED MATCHING CYBER NEON THEME)
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF070D30).copy(alpha = 0.95f)),
                    border = BorderStroke(
                        1.8.dp,
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF00D2FF),
                                Color(0xFF818CF8),
                                Color(0xFFEC4899),
                                Color(0xFF00D2FF)
                            )
                        )
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.Transparent,
                                border = BorderStroke(1.2.dp, Color(0xFF93C5FD)),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                listOf(Color(0xFF3B82F6), Color(0xFF6366F1))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PersonAdd,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = "Create Student Account",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Register your profile for English roleplays & drills",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        // Check Permission
                        if (!allowStudentReg) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF2E1318),
                                border = BorderStroke(1.dp, CoralAccent),
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
                                        color = Color(0xFFFCA5A5)
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
                                placeholder = { Text("Full Name (e.g. Jordan Smith)", color = Color(0xFF64748B), fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null, tint = Color(0xFF00D2FF)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_student_name"),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF06143A),
                                    unfocusedContainerColor = Color(0xFF06143A),
                                    focusedBorderColor = Color(0xFF00D2FF),
                                    unfocusedBorderColor = Color(0xFF00D2FF).copy(alpha = 0.65f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFF00D2FF)
                                ),
                                singleLine = true
                            )

                            // Email Address Field
                            OutlinedTextField(
                                value = regEmail,
                                onValueChange = {
                                    regEmail = it
                                    regError = null
                                },
                                placeholder = { Text("Email Address", color = Color(0xFF64748B), fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null, tint = Color(0xFF00D2FF)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("register_student_email"),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF06143A),
                                    unfocusedContainerColor = Color(0xFF06143A),
                                    focusedBorderColor = Color(0xFF00D2FF),
                                    unfocusedBorderColor = Color(0xFF00D2FF).copy(alpha = 0.65f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFF00D2FF)
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )

                            // Academic Class Selection
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Academic Class / Batch",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFA5B4FC)
                                )
                                ExposedDropdownMenuBox(
                                    expanded = regClassDropdownExpanded,
                                    onExpandedChange = { regClassDropdownExpanded = !regClassDropdownExpanded },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = regClass,
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regClassDropdownExpanded) },
                                        leadingIcon = { Icon(Icons.Rounded.School, contentDescription = null, tint = Color(0xFF00D2FF)) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFF06143A),
                                            unfocusedContainerColor = Color(0xFF06143A),
                                            focusedBorderColor = Color(0xFF00D2FF),
                                            unfocusedBorderColor = Color(0xFF00D2FF).copy(alpha = 0.65f),
                                            focusedTextColor = Color.White,
                                            unfocusedTextColor = Color.White
                                        )
                                    )

                                    ExposedDropdownMenu(
                                        expanded = regClassDropdownExpanded,
                                        onDismissRequest = { regClassDropdownExpanded = false }
                                    ) {
                                        academicClasses.forEach { className ->
                                            val isSelected = regClass == className
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = className,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) Color(0xFF00D2FF) else MaterialTheme.colorScheme.onSurface
                                                    )
                                                },
                                                leadingIcon = {
                                                    if (isSelected) {
                                                        Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFF00D2FF), modifier = Modifier.size(16.dp))
                                                    }
                                                },
                                                onClick = {
                                                    regClass = className
                                                    regClassDropdownExpanded = false
                                                    regError = null
                                                },
                                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                            )
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
                                placeholder = { Text("Set Security PIN (4-6 digits)", color = Color(0xFF64748B), fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFF00D2FF)) },
                                trailingIcon = {
                                    IconButton(onClick = { regPinVisible = !regPinVisible }) {
                                        Icon(
                                            imageVector = if (regPinVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                                            contentDescription = if (regPinVisible) "Hide PIN" else "Show PIN",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                },
                                visualTransformation = if (regPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF06143A),
                                    unfocusedContainerColor = Color(0xFF06143A),
                                    focusedBorderColor = Color(0xFF00D2FF),
                                    unfocusedBorderColor = Color(0xFF00D2FF).copy(alpha = 0.65f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFF00D2FF)
                                ),
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
                                placeholder = { Text("Confirm Security PIN", color = Color(0xFF64748B), fontSize = 14.sp) },
                                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, tint = Color(0xFF00D2FF)) },
                                visualTransformation = if (regPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF06143A),
                                    unfocusedContainerColor = Color(0xFF06143A),
                                    focusedBorderColor = Color(0xFF00D2FF),
                                    unfocusedBorderColor = Color(0xFF00D2FF).copy(alpha = 0.65f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color(0xFF00D2FF)
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                            )

                            // Error Banner
                            AnimatedVisibility(visible = regError != null) {
                                regError?.let { err ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF3F1318),
                                        border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(Icons.Filled.Error, contentDescription = null, tint = Color(0xFFFCA5A5), modifier = Modifier.size(16.dp))
                                            Text(err, color = Color(0xFFFEE2E2), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                shape = RoundedCornerShape(25.dp),
                                contentPadding = PaddingValues(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(Color(0xFFA855F7), Color(0xFF7C3AED), Color(0xFF00D2FF))
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.AutoMirrored.Rounded.Login, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                        Text(
                                            text = "Create Student Account & Enter",
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Switch back to Sign In
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Already have an account? ",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFCBD5E1)
                            )
                            Text(
                                text = "Sign In ➔",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.clickable {
                                    authMode = AuthMode.SIGN_IN
                                    loginError = null
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
