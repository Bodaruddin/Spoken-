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
                    onValueChange = { inputUrl = it },
                    label = { Text("Supabase Project URL") },
                    placeholder = { Text("https://your-project.supabase.co") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Input Anon Key
                OutlinedTextField(
                    value = inputKey,
                    onValueChange = { inputKey = it },
                    label = { Text("Supabase Anon Key (Public)") },
                    placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

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
