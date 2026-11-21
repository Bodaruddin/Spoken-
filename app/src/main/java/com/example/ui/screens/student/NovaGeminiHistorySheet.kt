package com.example.ui.screens.student

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NovaGeminiChatSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Gemini-style left-side sliding Chat History drawer for Nova AI.
 */
@Composable
fun NovaGeminiHistoryDrawerOverlay(
    visible: Boolean,
    history: List<NovaGeminiChatSession>,
    activeSessionId: String,
    onSelectSession: (String) -> Unit,
    onStartNewChat: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onClearAllHistory: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember(visible) { mutableStateOf("") }
    var showClearConfirm by remember { mutableStateOf(false) }

    val filteredHistory = remember(history, searchQuery) {
        val q = searchQuery.trim()
        if (q.isBlank()) history
        else history.filter { session ->
            session.title.contains(q, ignoreCase = true) ||
                session.previewText.contains(q, ignoreCase = true) ||
                session.mode.contains(q, ignoreCase = true)
        }
    }

    BackHandler(enabled = visible) {
        onDismiss()
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Dimmed backdrop scrim — tap outside to close drawer
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(220)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.60f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
                    .testTag("nova_gemini_history_scrim")
            )
        }

        // Left-side sliding Gemini-style History Drawer
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(
                animationSpec = tween(260),
                initialOffsetX = { -it }
            ) + fadeIn(animationSpec = tween(180)),
            exit = slideOutHorizontally(
                animationSpec = tween(220),
                targetOffsetX = { -it }
            ) + fadeOut(animationSpec = tween(160)),
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Surface(
                shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
                color = Color(0xFF0D0826),
                contentColor = Color.White,
                border = BorderStroke(
                    1.2.dp,
                    Brush.verticalGradient(
                        listOf(
                            Color(0xFF38BDF8).copy(alpha = 0.55f),
                            Color(0xFF6366F1).copy(alpha = 0.65f),
                            Color(0xFFEC4899).copy(alpha = 0.45f)
                        )
                    )
                ),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.84f)
                    .widthIn(max = 340.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .testTag("nova_gemini_history_sheet")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Drawer Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF06B6D4), Color(0xFF6366F1))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.History,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Nova AI History",
                                    fontSize = 16.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${history.size} saved ${if (history.size == 1) "chat" else "chats"}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("nova_history_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close chat history",
                                tint = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    // Gemini-style prominent "+ New Chat" pill button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E1648),
                        border = BorderStroke(
                            1.dp,
                            Brush.horizontalGradient(
                                listOf(Color(0xFF38BDF8), Color(0xFF8B5CF6))
                            )
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable {
                                onStartNewChat()
                                onDismiss()
                            }
                            .testTag("nova_history_new_chat_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AddComment,
                                contentDescription = null,
                                tint = Color(0xFF67E8F9),
                                modifier = Modifier.size(19.dp)
                            )
                            Text(
                                text = "New Chat",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Search box (shown when there are 2+ saved chats)
                    if (history.size >= 2) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "Search chats...",
                                    fontSize = 12.5.sp,
                                    color = Color(0xFF64748B)
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "Clear search",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF38BDF8),
                                unfocusedBorderColor = Color(0xFF312E81),
                                focusedContainerColor = Color(0xFF140D32),
                                unfocusedContainerColor = Color(0xFF140D32),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("nova_history_search_input")
                        )
                    }

                    // "Recent" section header + Clear All button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Recent",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF94A3B8)
                        )
                        if (history.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { showClearConfirm = true }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag("nova_history_clear_all_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteSweep,
                                    contentDescription = "Clear all chat history",
                                    tint = Color(0xFFF87171),
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = "Clear All",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFFCA5A5)
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = Color(0xFF312E81).copy(alpha = 0.55f),
                        thickness = 1.dp
                    )

                    // Scrollable History List
                    if (filteredHistory.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = Color(0xFF67E8F9).copy(alpha = 0.7f),
                                    modifier = Modifier.size(34.dp)
                                )
                                Text(
                                    text = if (history.isEmpty()) "No recent chats yet" else "No matching chats",
                                    fontSize = 14.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = if (history.isEmpty()) {
                                        "Start chatting with Nova AI. When you open a new chat, your previous chats stay saved right here."
                                    } else {
                                        "Try searching with a different word."
                                    },
                                    fontSize = 12.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(bottom = 12.dp)
                        ) {
                            items(filteredHistory, key = { it.id }) { session ->
                                val isActive = session.id == activeSessionId
                                NovaHistoryDrawerItem(
                                    session = session,
                                    isActive = isActive,
                                    onClick = {
                                        onSelectSession(session.id)
                                        onDismiss()
                                    },
                                    onDelete = { onDeleteSession(session.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = Color(0xFF140D32),
            titleContentColor = Color.White,
            textContentColor = Color(0xFFCBD5E1),
            title = {
                Text("Clear All Chat History?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will permanently remove all ${history.size} saved Nova AI conversations from your history.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearConfirm = false
                        onClearAllHistory()
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                    modifier = Modifier.testTag("nova_history_confirm_clear_btn")
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
private fun NovaHistoryDrawerItem(
    session: NovaGeminiChatSession,
    isActive: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isActive) Color(0xFF1F1650) else Color(0xFF140D32).copy(alpha = 0.85f),
        border = BorderStroke(
            width = if (isActive) 1.3.dp else 0.8.dp,
            color = if (isActive) Color(0xFF38BDF8) else Color(0xFF312E81).copy(alpha = 0.65f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("nova_history_item_${session.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 10.dp, bottom = 10.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = session.title,
                    fontSize = 13.5.sp,
                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (session.previewText.isNotBlank()) {
                    Text(
                        text = session.previewText,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "${modeEmojiFor(session.mode)} ${session.mode}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC4B5FD),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = formatRelativeChatTime(session.updatedAt),
                        fontSize = 10.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1
                    )
                    if (isActive) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF064E3B)
                        ) {
                            Text(
                                text = "Active",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6EE7B7),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("nova_history_delete_${session.id}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "Delete chat ${session.title}",
                    tint = Color(0xFFF87171).copy(alpha = 0.85f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun modeEmojiFor(mode: String): String = when (mode) {
    "Roleplay" -> "🎭"
    "Speaking Tutor" -> "🗣️"
    "Grammar Fix" -> "✍️"
    "Translator" -> "🔤"
    "Writing Coach" -> "📝"
    else -> "✨"
}

private fun formatRelativeChatTime(timestamp: Long): String {
    val diffMs = (System.currentTimeMillis() - timestamp).coerceAtLeast(0L)
    val mins = diffMs / 60_000L
    val hours = mins / 60L
    val days = hours / 24L
    return when {
        mins < 1L -> "Just now"
        mins < 60L -> "${mins}m ago"
        hours < 24L -> SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timestamp))
        days == 1L -> "Yesterday"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
