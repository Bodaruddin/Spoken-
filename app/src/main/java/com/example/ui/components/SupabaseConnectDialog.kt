package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.SupabaseConnectionState
import com.example.data.remote.SupabaseService
import com.example.ui.MainViewModel
import com.example.ui.theme.*

@Composable
fun SupabaseConnectDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val supabaseStatus by viewModel.supabaseStatus.collectAsState()
    var inputUrl by remember { mutableStateOf(viewModel.supabaseService.supabaseUrl) }
    var inputKey by remember { mutableStateOf(viewModel.supabaseService.supabaseAnonKey) }
    var showSqlSchema by remember { mutableStateOf(false) }
    var copiedFeedback by remember { mutableStateOf(false) }

    val currentUser by viewModel.currentUser.collectAsState()
    val isAdmin = currentUser.role == com.example.data.model.Role.ADMIN

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF3ECF8E)), // Official Supabase Green
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚡", fontSize = 20.sp)
                }
                Column {
                    Text("Supabase Cloud Database", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    Text("PostgreSQL Backend & Data Sync", fontSize = 11.sp, color = MutedGray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Admin Restriction Notice for Students
                if (!isAdmin) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("🔒", fontSize = 20.sp)
                            Column {
                                Text(
                                    text = "Administrator Access Required",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFFB45309)
                                )
                                Text(
                                    text = "Only Administrators can configure credentials and connect the database. Student accounts have read-only status access.",
                                    fontSize = 11.sp,
                                    color = DeepNavy
                                )
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalBlue.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🛡️", fontSize = 14.sp)
                            Text(
                                text = "Admin Authorized: Dean Arthur Pendelton",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RoyalBlueDark
                            )
                        }
                    }
                }

                // Connection Status Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = when (supabaseStatus) {
                        is SupabaseConnectionState.Connected -> Color(0xFFDCFCE7)
                        is SupabaseConnectionState.Connecting -> Color(0xFFFEF3C7)
                        is SupabaseConnectionState.Error -> Color(0xFFFEE2E2)
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (supabaseStatus) {
                                        is SupabaseConnectionState.Connected -> EmeraldSuccess
                                        is SupabaseConnectionState.Connecting -> AmberGold
                                        is SupabaseConnectionState.Error -> CoralAccent
                                        else -> MutedGray
                                    }
                                )
                        )
                        Text(
                            text = when (supabaseStatus) {
                                is SupabaseConnectionState.Connected -> "Connected: ${(supabaseStatus as SupabaseConnectionState.Connected).message}"
                                is SupabaseConnectionState.Connecting -> "Testing connection to Supabase..."
                                is SupabaseConnectionState.Error -> "Connection failed: ${(supabaseStatus as SupabaseConnectionState.Error).message}"
                                is SupabaseConnectionState.Disconnected -> "Offline / Local Mode (Room SQLite active)"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepNavy
                        )
                    }
                }

                // Input URL
                OutlinedTextField(
                    value = inputUrl,
                    onValueChange = { if (isAdmin) inputUrl = it },
                    enabled = isAdmin,
                    readOnly = !isAdmin,
                    label = { Text("Supabase Project URL" + if (!isAdmin) " (Admin Locked)" else "") },
                    placeholder = { Text("https://your-project.supabase.co") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Input Anon Key
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { if (isAdmin) inputKey = it },
                    enabled = isAdmin,
                    readOnly = !isAdmin,
                    label = { Text("Supabase Anon Key (Public)" + if (!isAdmin) " (Admin Locked)" else "") },
                    placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (isAdmin) {
                    Button(
                        onClick = {
                            viewModel.connectSupabase(inputUrl, inputKey)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_connect_supabase_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3ECF8E))
                    ) {
                        Icon(Icons.Filled.CloudDone, contentDescription = null, tint = Color.Black)
                        Spacer(Modifier.width(6.dp))
                        Text("Test & Connect Supabase", fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                } else {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_connect_supabase_locked_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            disabledContainerColor = Color(0xFFE2E8F0),
                            disabledContentColor = MutedGray
                        )
                    ) {
                        Icon(Icons.Filled.Lock, contentDescription = null, tint = MutedGray, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Only Admin Can Connect Database", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MutedGray)
                    }

                    OutlinedButton(
                        onClick = {
                            onDismiss()
                            viewModel.toggleRoleSwitcher(true)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoyalBlue)
                    ) {
                        Icon(Icons.Rounded.Shield, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Switch to Admin Account to Connect", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoyalBlue)
                    }
                }

                HorizontalDivider()

                // Toggle Schema Blueprint
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { showSqlSchema = !showSqlSchema }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SQL Schema Blueprint", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RoyalBlue)
                    Icon(if (showSqlSchema) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore, contentDescription = null, tint = RoyalBlue)
                }

                if (showSqlSchema) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = DeepNavy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = SupabaseService.SQL_SCHEMA_BLUEPRINT,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFFE2E8F0),
                                lineHeight = 14.sp
                            )

                            Button(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Supabase Schema", SupabaseService.SQL_SCHEMA_BLUEPRINT)
                                    clipboard.setPrimaryClip(clip)
                                    copiedFeedback = true
                                },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                            ) {
                                Text(if (copiedFeedback) "Copied to Clipboard!" else "Copy SQL Schema", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Text(
                    "Tip: You can also set SUPABASE_URL and SUPABASE_ANON_KEY permanently in the Secrets panel in AI Studio so it connects automatically on every build.",
                    fontSize = 11.sp,
                    color = MutedGray,
                    lineHeight = 14.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}
