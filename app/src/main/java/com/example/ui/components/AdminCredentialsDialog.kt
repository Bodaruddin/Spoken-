package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun AdminCredentialsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()

    var adminName by remember { mutableStateOf(currentUser.name) }
    var adminEmail by remember { mutableStateOf(currentUser.email) }
    var adminPassword by remember { mutableStateOf(viewModel.adminPassword) }
    var adminPin by remember { mutableStateOf(viewModel.adminPin) }

    var passwordVisible by remember { mutableStateOf(false) }
    var pinVisible by remember { mutableStateOf(false) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = Color(0xFF070B1E),
            border = BorderStroke(1.2.dp, Color(0xFF3B82F6).copy(alpha = 0.6f)),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(vertical = 24.dp)
                .testTag("admin_credentials_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1E3A8A)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Security, contentDescription = null, tint = Color(0xFF60A5FA), modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "Admin Credentials",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Manage administrator login & security PIN",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                HorizontalDivider(color = Color(0xFF1E293B))

                // Input: Admin Full Name
                OutlinedTextField(
                    value = adminName,
                    onValueChange = { adminName = it },
                    label = { Text("Administrator Name") },
                    leadingIcon = {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_name_field")
                )

                // Input: Admin Email
                OutlinedTextField(
                    value = adminEmail,
                    onValueChange = { adminEmail = it },
                    label = { Text("Admin Email / Login Identifier") },
                    leadingIcon = {
                        Icon(Icons.Filled.Email, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(18.dp))
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_email_field")
                )

                // Input: Admin Password
                OutlinedTextField(
                    value = adminPassword,
                    onValueChange = { adminPassword = it },
                    label = { Text("Admin Master Password") },
                    leadingIcon = {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_password_field")
                )

                // Input: Admin Security PIN (for fast PIN unlock & role switching)
                OutlinedTextField(
                    value = adminPin,
                    onValueChange = { if (it.length <= 6 && it.all { char -> char.isDigit() }) adminPin = it },
                    label = { Text("4-Digit Security PIN (Quick Unlock)") },
                    leadingIcon = {
                        Icon(Icons.Filled.Key, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        IconButton(onClick = { pinVisible = !pinVisible }) {
                            Icon(
                                if (pinVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    visualTransformation = if (pinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RoyalBlue,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("admin_pin_field")
                )

                // Feedback Banner
                AnimatedVisibility(visible = feedbackMessage != null) {
                    feedbackMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSuccess) Color(0xFF064E3B) else Color(0xFF7F1D1D),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = msg,
                                color = if (isSuccess) Color(0xFF6EE7B7) else Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = Color(0xFF94A3B8))
                    }

                    Button(
                        onClick = {
                            if (adminName.isBlank()) {
                                feedbackMessage = "Administrator name cannot be empty."
                                isSuccess = false
                                return@Button
                            }
                            if (adminEmail.isBlank()) {
                                feedbackMessage = "Email cannot be empty."
                                isSuccess = false
                                return@Button
                            }
                            if (adminPassword.length < 4) {
                                feedbackMessage = "Password must be at least 4 characters."
                                isSuccess = false
                                return@Button
                            }
                            if (adminPin.length < 4) {
                                feedbackMessage = "Security PIN must be at least 4 digits."
                                isSuccess = false
                                return@Button
                            }

                            val success = viewModel.updateAdminCredentials(
                                name = adminName,
                                email = adminEmail,
                                newPassword = adminPassword,
                                newPin = adminPin
                            )
                            if (success) {
                                feedbackMessage = "Admin credentials updated successfully!"
                                isSuccess = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                        modifier = Modifier.weight(1.2f).testTag("save_admin_credentials_btn")
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save Credentials", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
