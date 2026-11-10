package com.example.ui.screens.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.MainViewModel
import com.example.ui.theme.*

// -------------------------------------------------------------
// ADMIN WORLD & MISSION MANAGEMENT LIST
// -------------------------------------------------------------
@Composable
fun AdventureWorldsManagementList(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(14.dp)
) {
    val worlds by viewModel.adventureWorlds.collectAsState()

    var showCreateWorldDialog by remember { mutableStateOf(false) }
    var worldToEdit by remember { mutableStateOf<AdventureWorld?>(null) }
    var worldForNewLevel by remember { mutableStateOf<AdventureWorld?>(null) }
    var levelToEdit by remember { mutableStateOf<Pair<String, AdventureLevel>?>(null) }
    var levelForNewMission by remember { mutableStateOf<Pair<String, AdventureLevel>?>(null) } // Pair(worldId, level)
    var missionToEdit by remember { mutableStateOf<Triple<String, String, AdventureMission>?>(null) } // worldId, levelId, mission
    var worldToDelete by remember { mutableStateOf<AdventureWorld?>(null) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    var expandedWorldId by remember { mutableStateOf<String?>(worlds.firstOrNull()?.id) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Header Action Banner
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF131842),
                border = BorderStroke(1.dp, Color(0xFF38437D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            "English Adventure Worlds (${worlds.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            "Manage worlds, levels, questions & interactive missions",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = { showResetConfirmDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF64748B)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFCBD5E1)),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = "Reset Defaults", modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Reset", fontSize = 11.sp)
                        }

                        Button(
                            onClick = { showCreateWorldDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            modifier = Modifier.testTag("admin_add_world_btn"),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Add World", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(worlds.sortedBy { it.order }) { world ->
            val isExpanded = expandedWorldId == world.id

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF101438),
                border = BorderStroke(1.2.dp, Color(world.themeColorHex).copy(alpha = 0.8f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // World Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                expandedWorldId = if (isExpanded) null else world.id
                            }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(world.themeColorHex).copy(alpha = 0.2f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(world.emoji, fontSize = 22.sp)
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f).padding(end = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "World ${world.order}: ${world.name}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "(${world.levels.size})",
                                    fontSize = 11.sp,
                                    color = Color(world.themeColorHex)
                                )
                            }
                            Text(
                                text = "Unlocks at ${world.requiredTotalStars} ⭐ • ${world.tagline}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { worldForNewLevel = world },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.AddCircle, contentDescription = "Add Level", tint = Color(0xFF38BDF8), modifier = Modifier.size(19.dp))
                        }

                        IconButton(
                            onClick = { worldToEdit = world },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit World", tint = Color(0xFFFBBF24), modifier = Modifier.size(17.dp))
                        }

                        IconButton(
                            onClick = { worldToDelete = world },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete World", tint = Color(0xFFF87171), modifier = Modifier.size(17.dp))
                        }

                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Expanded Levels Section
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF0C0F2B))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "Levels inside ${world.name}:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1)
                            )

                            if (world.levels.isEmpty()) {
                                Text(
                                    "No levels created yet. Click '+' to add the first level.",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            } else {
                                world.levels.forEach { level ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF141940),
                                        border = BorderStroke(1.dp, Color(0xFF283262)),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .padding(end = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(level.iconEmoji, fontSize = 15.sp)
                                                    Text(
                                                        "Level ${level.levelNumber}: ${level.title}",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.5.sp,
                                                        color = Color.White,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f, fill = false)
                                                    )
                                                }

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = Color(0xFF0369A1).copy(alpha = 0.35f),
                                                        modifier = Modifier.padding(end = 2.dp)
                                                    ) {
                                                        Text(
                                                            "+${level.xpReward} XP",
                                                            fontSize = 10.sp,
                                                            color = Color(0xFF38BDF8),
                                                            fontWeight = FontWeight.Bold,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = { levelForNewMission = Pair(world.id, level) },
                                                        modifier = Modifier.size(26.dp)
                                                    ) {
                                                        Icon(Icons.Filled.Add, contentDescription = "Add Question", tint = Color(0xFF4ADE80), modifier = Modifier.size(15.dp))
                                                    }
                                                    IconButton(
                                                        onClick = { levelToEdit = Pair(world.id, level) },
                                                        modifier = Modifier.size(26.dp)
                                                    ) {
                                                        Icon(Icons.Filled.Edit, contentDescription = "Edit Level", tint = Color(0xFFFBBF24), modifier = Modifier.size(14.dp))
                                                    }
                                                    IconButton(
                                                        onClick = { viewModel.deleteAdventureLevel(world.id, level.id) },
                                                        modifier = Modifier.size(26.dp)
                                                    ) {
                                                        Icon(Icons.Filled.DeleteOutline, contentDescription = "Delete Level", tint = Color(0xFFF87171), modifier = Modifier.size(15.dp))
                                                    }
                                                }
                                            }

                                            // Missions list
                                            level.missions.forEach { mission ->
                                                Surface(
                                                    shape = RoundedCornerShape(8.dp),
                                                    color = Color(0xFF1C2250),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 8.dp, vertical = 5.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Row(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .padding(end = 4.dp),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                                        ) {
                                                            Text(mission.type.emoji, fontSize = 13.sp)
                                                            Text(
                                                                mission.title,
                                                                color = Color.White,
                                                                fontSize = 11.5.sp,
                                                                fontWeight = FontWeight.Medium,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis,
                                                                modifier = Modifier.weight(1f, fill = false)
                                                            )
                                                            Text(
                                                                "(${mission.type.displayName})",
                                                                color = Color(0xFF94A3B8),
                                                                fontSize = 10.sp,
                                                                maxLines = 1
                                                            )
                                                        }

                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            IconButton(
                                                                onClick = { missionToEdit = Triple(world.id, level.id, mission) },
                                                                modifier = Modifier.size(24.dp)
                                                            ) {
                                                                Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = Color(0xFFFBBF24), modifier = Modifier.size(13.dp))
                                                            }
                                                            IconButton(
                                                                onClick = { viewModel.deleteAdventureMission(world.id, level.id, mission.id) },
                                                                modifier = Modifier.size(24.dp)
                                                            ) {
                                                                Icon(Icons.Filled.Close, contentDescription = "Delete", tint = Color(0xFFF87171), modifier = Modifier.size(14.dp))
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // -------------------------------------------------------------
    // DIALOGS: ADD WORLD, LEVEL, MISSION
    // -------------------------------------------------------------
    if (showCreateWorldDialog) {
        var name by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var tagline by remember { mutableStateOf("") }
        var emoji by remember { mutableStateOf("🏝️") }
        var reqStars by remember { mutableStateOf("0") }

        AlertDialog(
            onDismissRequest = { showCreateWorldDialog = false },
            title = { Text("Create Adventure World", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("World Name") },
                        placeholder = { Text("e.g. Vocabulary Island") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("Tagline") },
                        placeholder = { Text("e.g. Realm of Lexical Discovery") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = emoji,
                            onValueChange = { emoji = it },
                            label = { Text("Emoji") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reqStars,
                            onValueChange = { reqStars = it },
                            label = { Text("Required Stars") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.addAdventureWorld(
                                name = name,
                                description = description,
                                tagline = tagline,
                                emoji = emoji,
                                requiredStars = reqStars.toIntOrNull() ?: 0
                            )
                            showCreateWorldDialog = false
                        }
                    }
                ) {
                    Text("Create World")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateWorldDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    worldForNewLevel?.let { world ->
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var emoji by remember { mutableStateOf("⭐") }
        var xp by remember { mutableStateOf("70") }
        var coins by remember { mutableStateOf("30") }

        AlertDialog(
            onDismissRequest = { worldForNewLevel = null },
            title = { Text("Add Level to ${world.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Level Title") },
                        placeholder = { Text("e.g. Coral Reef Sanctuary") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = emoji,
                            onValueChange = { emoji = it },
                            label = { Text("Emoji") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = xp,
                            onValueChange = { xp = it },
                            label = { Text("XP Reward") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = coins,
                        onValueChange = { coins = it },
                        label = { Text("Coins Reward") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addAdventureLevel(
                                worldId = world.id,
                                title = title,
                                description = description,
                                emoji = emoji,
                                xp = xp.toIntOrNull() ?: 70,
                                coins = coins.toIntOrNull() ?: 30
                            )
                            worldForNewLevel = null
                        }
                    }
                ) {
                    Text("Add Level")
                }
            },
            dismissButton = {
                TextButton(onClick = { worldForNewLevel = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    levelForNewMission?.let { (worldId, level) ->
        var missionType by remember { mutableStateOf(AdventureMissionType.QUIZ) }
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var targetSentence by remember { mutableStateOf("") }
        var optionsText by remember { mutableStateOf("") }
        var correctAnswer by remember { mutableStateOf("") }
        var clue by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { levelForNewMission = null },
            title = { Text("Add Mission / Question", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("Select Mission Type:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            AdventureMissionType.values().forEach { type ->
                                val isSel = missionType == type
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clickable { missionType = type }
                                ) {
                                    Text(
                                        text = "${type.emoji} ${type.displayName.take(7)}",
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Mission Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Question / Instructions") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = targetSentence,
                            onValueChange = { targetSentence = it },
                            label = { Text("Target Sentence / Audio Text") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = optionsText,
                            onValueChange = { optionsText = it },
                            label = { Text("Options (comma-separated)") },
                            placeholder = { Text("Option A, Option B, Option C, Option D") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = correctAnswer,
                            onValueChange = { correctAnswer = it },
                            label = { Text("Correct Answer") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = clue,
                            onValueChange = { clue = it },
                            label = { Text("Clue / Hint (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val opts = optionsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            viewModel.addAdventureMission(
                                worldId = worldId,
                                levelId = level.id,
                                title = title,
                                description = description,
                                type = missionType,
                                targetSentence = targetSentence,
                                options = opts,
                                correctAnswer = correctAnswer,
                                clue = clue,
                                xp = 40,
                                coins = 20
                            )
                            levelForNewMission = null
                        }
                    }
                ) {
                    Text("Add Mission")
                }
            },
            dismissButton = {
                TextButton(onClick = { levelForNewMission = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    worldToDelete?.let { world ->
        AlertDialog(
            onDismissRequest = { worldToDelete = null },
            title = { Text("Delete World ${world.name}?") },
            text = { Text("This will permanently remove this adventure world and all its missions.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAdventureWorld(world.id)
                        worldToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { worldToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    worldToEdit?.let { world ->
        var name by remember(world.id) { mutableStateOf(world.name) }
        var description by remember(world.id) { mutableStateOf(world.description) }
        var tagline by remember(world.id) { mutableStateOf(world.tagline) }
        var emoji by remember(world.id) { mutableStateOf(world.emoji) }
        var reqStars by remember(world.id) { mutableStateOf(world.requiredTotalStars.toString()) }

        AlertDialog(
            onDismissRequest = { worldToEdit = null },
            title = { Text("Edit Adventure World", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("World Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tagline,
                        onValueChange = { tagline = it },
                        label = { Text("Tagline") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = emoji,
                            onValueChange = { emoji = it },
                            label = { Text("Emoji") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = reqStars,
                            onValueChange = { reqStars = it },
                            label = { Text("Required Stars") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            viewModel.updateAdventureWorld(
                                world.copy(
                                    name = name.trim(),
                                    tagline = tagline.trim(),
                                    description = description.trim(),
                                    emoji = emoji.ifBlank { "🗺️" },
                                    requiredTotalStars = reqStars.toIntOrNull() ?: 0
                                )
                            )
                            worldToEdit = null
                        }
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { worldToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    levelToEdit?.let { (worldId, level) ->
        var title by remember(level.id) { mutableStateOf(level.title) }
        var description by remember(level.id) { mutableStateOf(level.description) }
        var emoji by remember(level.id) { mutableStateOf(level.iconEmoji) }
        var xp by remember(level.id) { mutableStateOf(level.xpReward.toString()) }
        var coins by remember(level.id) { mutableStateOf(level.coinsReward.toString()) }

        AlertDialog(
            onDismissRequest = { levelToEdit = null },
            title = { Text("Edit Level: ${level.title}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Level Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = emoji,
                            onValueChange = { emoji = it },
                            label = { Text("Emoji") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = xp,
                            onValueChange = { xp = it },
                            label = { Text("XP Reward") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                    OutlinedTextField(
                        value = coins,
                        onValueChange = { coins = it },
                        label = { Text("Coins Reward") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.updateAdventureLevel(
                                worldId = worldId,
                                levelId = level.id,
                                title = title,
                                description = description,
                                emoji = emoji,
                                xp = xp.toIntOrNull() ?: 70,
                                coins = coins.toIntOrNull() ?: 30
                            )
                            levelToEdit = null
                        }
                    }
                ) {
                    Text("Save Level")
                }
            },
            dismissButton = {
                TextButton(onClick = { levelToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    missionToEdit?.let { (worldId, levelId, mission) ->
        var missionType by remember(mission.id) { mutableStateOf(mission.type) }
        var title by remember(mission.id) { mutableStateOf(mission.title) }
        var description by remember(mission.id) { mutableStateOf(mission.description) }
        var targetSentence by remember(mission.id) { mutableStateOf(mission.targetSentence) }
        var optionsText by remember(mission.id) { mutableStateOf(mission.options.joinToString(", ")) }
        var correctAnswer by remember(mission.id) { mutableStateOf(mission.correctAnswer) }
        var clue by remember(mission.id) { mutableStateOf(mission.clue) }

        AlertDialog(
            onDismissRequest = { missionToEdit = null },
            title = { Text("Edit Mission / Question", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("Mission Type:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth()) {
                            AdventureMissionType.values().forEach { type ->
                                val isSel = missionType == type
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .padding(2.dp)
                                        .clickable { missionType = type }
                                ) {
                                    Text(
                                        text = "${type.emoji} ${type.displayName.take(7)}",
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                        fontSize = 10.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Mission Title") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Question / Instructions") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = targetSentence,
                            onValueChange = { targetSentence = it },
                            label = { Text("Target Sentence / Audio Text") },
                            maxLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = optionsText,
                            onValueChange = { optionsText = it },
                            label = { Text("Options (comma-separated)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = correctAnswer,
                            onValueChange = { correctAnswer = it },
                            label = { Text("Correct Answer") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = clue,
                            onValueChange = { clue = it },
                            label = { Text("Clue / Hint (Optional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val opts = optionsText.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            viewModel.updateAdventureMission(
                                worldId = worldId,
                                levelId = levelId,
                                missionId = mission.id,
                                title = title,
                                description = description,
                                type = missionType,
                                targetSentence = targetSentence,
                                options = opts,
                                correctAnswer = correctAnswer,
                                clue = clue,
                                xp = mission.xp,
                                coins = mission.coins
                            )
                            missionToEdit = null
                        }
                    }
                ) {
                    Text("Save Mission")
                }
            },
            dismissButton = {
                TextButton(onClick = { missionToEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            title = { Text("Restore Official Adventure Worlds?", fontWeight = FontWeight.Bold) },
            text = { Text("This will reset the English Adventure worlds, levels, and missions back to the official default curriculum (Vocabulary Island, Ocean Reef, Pirate Harbor, Dragon Mountains, Castle of Wisdom).") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.resetAdventureWorldsToDefault()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                ) {
                    Text("Restore Defaults")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// -------------------------------------------------------------
// TEACHER PROGRESS MONITORING TRACKER
// -------------------------------------------------------------
@Composable
fun TeacherAdventureProgressTracker(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var trackerTab by remember { mutableStateOf("PROGRESS") } // "PROGRESS" or "MANAGE"
    val reports = remember { viewModel.getTeacherAdventureReports() }
    val worlds by viewModel.adventureWorlds.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterClass by remember { mutableStateOf("All") }

    val filtered = reports.filter { report ->
        (selectedFilterClass == "All" || report.className.equals(selectedFilterClass, ignoreCase = true)) &&
                (searchQuery.isBlank() || report.studentName.contains(searchQuery, ignoreCase = true))
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Mode Selector: Student Progress vs Manage Worlds & Games
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF0F1538),
            border = BorderStroke(1.dp, Color(0xFF2B3466)),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (trackerTab == "PROGRESS") Color(0xFF7C3AED) else Color.Transparent,
                    modifier = Modifier.weight(1f).clickable { trackerTab = "PROGRESS" }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📊", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Student Progress",
                            fontWeight = if (trackerTab == "PROGRESS") FontWeight.Bold else FontWeight.Medium,
                            color = if (trackerTab == "PROGRESS") Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (trackerTab == "MANAGE") Color(0xFF7C3AED) else Color.Transparent,
                    modifier = Modifier.weight(1f).clickable { trackerTab = "MANAGE" }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🗺️", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Manage Games & Worlds",
                            fontWeight = if (trackerTab == "MANAGE") FontWeight.Bold else FontWeight.Medium,
                            color = if (trackerTab == "MANAGE") Color.White else Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (trackerTab == "MANAGE") {
            AdventureWorldsManagementList(
                viewModel = viewModel,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
        item {
            // Summary Hero Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF131A44),
                border = BorderStroke(1.dp, Color(0xFF38437D)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("🗺️", fontSize = 24.sp)
                            Column {
                                Text(
                                    "English Adventure Student Tracker",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    "Live Supabase sync across 5 Adventure Worlds",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF166534)
                        ) {
                            Text(
                                "SUPABASE SYNCED",
                                color = Color(0xFF4ADE80),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Stat summary pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1B2252),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total Students", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text("${reports.size}", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.White)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1B2252),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Worlds Active", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text("${worlds.size} Worlds", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF38BDF8))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1B2252),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Top World", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                Text("Castle", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFFFACC15))
                            }
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search student name...", fontSize = 12.sp) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Student Progress Cards
        items(filtered) { report ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF101538),
                border = BorderStroke(1.dp, Color(0xFF283262)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 6.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF1E2652),
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(report.avatar, fontSize = 20.sp)
                                }
                            }

                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = report.studentName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${report.className} • Current: ${report.currentWorld}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E2652),
                            border = BorderStroke(1.dp, Color(0xFFFACC15).copy(alpha = 0.5f))
                        ) {
                            Text(
                                "⭐ ${report.totalStars} Stars",
                                color = Color(0xFFFACC15),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                maxLines = 1
                            )
                        }
                    }

                    // Progress bar
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Levels Cleared: ${report.completedLevelsCount} / ${report.totalLevelsCount}",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                            Text(
                                "${(report.completionPercent * 100).toInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4ADE80)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { report.completionPercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF7C3AED),
                            trackColor = Color(0xFF1E293B)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "XP: ${report.xpEarned} • Coins: ${report.coinsEarned}",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            report.lastActiveMission,
                            fontSize = 11.sp,
                            color = Color(0xFF4ADE80),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
}
}


