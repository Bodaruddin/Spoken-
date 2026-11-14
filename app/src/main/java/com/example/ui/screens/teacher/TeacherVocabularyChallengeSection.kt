@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.ui.screens.teacher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.VocabularyActivityType
import com.example.data.model.VocabularyModeConfig
import com.example.data.model.WordMatchChallenge
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.EmeraldSuccess

fun formatVocabularyChallengeCardContent(item: WordMatchChallenge): Triple<String, String, String> {
    val type = VocabularyActivityType.fromName(item.activityType)
    val title = when (type) {
        VocabularyActivityType.PICTURE_MATCHING -> "${item.pictureEmoji.ifBlank { "🖼️" }} ${item.word}"
        VocabularyActivityType.WORD_SCRAMBLE -> "🔀 ${item.word}"
        else -> item.word
    }
    val subtitle = when (type) {
        VocabularyActivityType.PICTURE_MATCHING ->
            item.definition.ifBlank { "Picture Matching item" }
        VocabularyActivityType.MEANING_MATCHING ->
            item.definition.ifBlank { "Meaning Matching item" }
        VocabularyActivityType.SYNONYMS ->
            "Synonym: ${item.synonym.ifBlank { "—" }}" +
                if (item.definition.isNotBlank()) " • ${item.definition}" else ""
        VocabularyActivityType.OPPOSITE_WORDS ->
            "Opposite: ${item.antonym.ifBlank { "—" }}" +
                if (item.definition.isNotBlank()) " • ${item.definition}" else ""
        VocabularyActivityType.WORD_SCRAMBLE ->
            "Hint: ${item.scrambleHint.ifBlank { item.definition }.ifBlank { "Unscramble tiles" }}"
    }
    val tag = "${type.emoji} ${type.title} • ${item.category.ifBlank { "General" }}"
    return Triple(title, subtitle, tag)
}

@Composable
fun TeacherVocabularyChallengeManagementHeader(
    modeConfigs: List<VocabularyModeConfig>,
    allChallenges: List<WordMatchChallenge>,
    selectedFilter: VocabularyActivityType?,
    onSelectFilter: (VocabularyActivityType?) -> Unit,
    onToggleModeEnabled: (VocabularyModeConfig, Boolean) -> Unit,
    onEditModeConfig: (VocabularyModeConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    var modesExpanded by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("teacher_vocab_management_header"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Expandable 5 Activity Modes Control Card
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)),
            border = BorderStroke(1.2.dp, Color(0xFFA855F7).copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .clickable { modesExpanded = !modesExpanded }
                        .testTag("teacher_vocab_modes_expand_toggle"),
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
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Tune,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Manage 5 Activity Modes",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            val activeCount = modeConfigs.count { it.isEnabled }
                            Text(
                                text = "$activeCount of ${modeConfigs.size} modes active on Student Portal",
                                fontSize = 11.sp,
                                color = Color(0xFFC4B5FD)
                            )
                        }
                    }
                    Icon(
                        imageVector = if (modesExpanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (modesExpanded) "Collapse modes" else "Expand modes",
                        tint = Color(0xFFE9D5FF)
                    )
                }

                AnimatedVisibility(visible = modesExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        modeConfigs.forEach { cfg ->
                            val itemCount = allChallenges.count {
                                VocabularyActivityType.fromName(it.activityType) == cfg.activityType
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (cfg.isEnabled) Color(0xFF17133B) else Color(0xFF0B1120),
                                border = BorderStroke(
                                    1.dp,
                                    if (cfg.isEnabled) Color(0xFF6366F1).copy(alpha = 0.6f)
                                    else Color(0xFF334155)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("teacher_vocab_mode_row_${cfg.activityType.name.lowercase()}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(cfg.activityType.emoji, fontSize = 22.sp)
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Text(
                                            text = cfg.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (cfg.isEnabled) Color.White else Color(0xFF94A3B8),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "$itemCount items • ${cfg.questionsPerRound} Qs/round • +${cfg.xpReward} XP",
                                            fontSize = 11.sp,
                                            color = if (cfg.isEnabled) Color(0xFF93C5FD) else Color(0xFF64748B)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onEditModeConfig(cfg) },
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("vocab_mode_edit_${cfg.activityType.name.lowercase()}")
                                    ) {
                                        Icon(
                                            Icons.Filled.Edit,
                                            contentDescription = "Edit ${cfg.title} settings",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }

                                    Switch(
                                        checked = cfg.isEnabled,
                                        onCheckedChange = { checked -> onToggleModeEnabled(cfg, checked) },
                                        modifier = Modifier.testTag("vocab_mode_toggle_${cfg.activityType.name.lowercase()}")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Activity Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 2.dp)
        ) {
            item {
                val isAll = selectedFilter == null
                FilterChip(
                    selected = isAll,
                    onClick = { onSelectFilter(null) },
                    label = {
                        Text(
                            text = "All (${allChallenges.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.testTag("teacher_vocab_filter_all")
                )
            }
            items(VocabularyActivityType.values()) { type ->
                val count = allChallenges.count {
                    VocabularyActivityType.fromName(it.activityType) == type
                }
                val isSel = selectedFilter == type
                FilterChip(
                    selected = isSel,
                    onClick = { onSelectFilter(if (isSel) null else type) },
                    label = {
                        Text(
                            text = "${type.emoji} ${type.title} ($count)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF7C3AED),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF0F172A),
                        labelColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.testTag("teacher_vocab_filter_${type.name.lowercase()}")
                )
            }
        }
    }
}

@Composable
fun VocabularyChallengeModeConfigDialog(
    config: VocabularyModeConfig,
    onDismiss: () -> Unit,
    onSave: (VocabularyModeConfig) -> Unit
) {
    var title by remember(config) { mutableStateOf(config.title) }
    var subtitle by remember(config) { mutableStateOf(config.subtitle) }
    var questionsPerRoundText by remember(config) { mutableStateOf(config.questionsPerRound.toString()) }
    var xpRewardText by remember(config) { mutableStateOf(config.xpReward.toString()) }
    var isEnabled by remember(config) { mutableStateOf(config.isEnabled) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vocab_mode_config_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(config.activityType.emoji, fontSize = 24.sp)
                    Column {
                        Text(
                            text = "Configure ${config.activityType.title}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "Customize round length, XP reward, and visibility",
                            fontSize = 11.5.sp,
                            color = Color.Gray
                        )
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        errorMsg = null
                    },
                    label = { Text("Activity Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vocab_mode_title_input")
                )

                OutlinedTextField(
                    value = subtitle,
                    onValueChange = { subtitle = it },
                    label = { Text("Student Subtitle / Description") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = questionsPerRoundText,
                        onValueChange = {
                            questionsPerRoundText = it.filter { ch -> ch.isDigit() }
                            errorMsg = null
                        },
                        label = { Text("Questions/Round") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vocab_mode_questions_input")
                    )

                    OutlinedTextField(
                        value = xpRewardText,
                        onValueChange = {
                            xpRewardText = it.filter { ch -> ch.isDigit() }
                            errorMsg = null
                        },
                        label = { Text("XP Reward") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("vocab_mode_xp_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Visible in Student Portal",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it },
                        modifier = Modifier.testTag("vocab_mode_dialog_enabled_switch")
                    )
                }

                errorMsg?.let { err ->
                    Text(err, color = CoralAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            val cleanTitle = title.trim()
                            val qs = questionsPerRoundText.toIntOrNull()
                            val xp = xpRewardText.toIntOrNull()
                            when {
                                cleanTitle.isBlank() -> errorMsg = "Activity title is required."
                                qs == null || qs !in 1..30 -> errorMsg = "Questions per round must be 1 to 30."
                                xp == null || xp !in 10..500 -> errorMsg = "XP reward must be 10 to 500."
                                else -> onSave(
                                    config.copy(
                                        title = cleanTitle,
                                        subtitle = subtitle.trim().ifBlank { config.activityType.subtitle },
                                        questionsPerRound = qs,
                                        xpReward = xp,
                                        isEnabled = isEnabled
                                    )
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("vocab_mode_save_btn")
                    ) {
                        Text("Save Mode", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun VocabularyChallengeEditorDialog(
    initialItem: WordMatchChallenge? = null,
    defaultActivityType: VocabularyActivityType = VocabularyActivityType.PICTURE_MATCHING,
    onDismiss: () -> Unit,
    onSave: (
        word: String,
        definition: String,
        category: String,
        activityType: VocabularyActivityType,
        pictureEmoji: String,
        synonym: String,
        antonym: String,
        distractors: List<String>,
        scrambleHint: String,
        keepOpenForAnother: Boolean
    ) -> Unit
) {
    val startType = remember(initialItem, defaultActivityType) {
        initialItem?.let { VocabularyActivityType.fromName(it.activityType) } ?: defaultActivityType
    }

    var activityType by remember(initialItem) { mutableStateOf(startType) }
    var word by remember(initialItem) { mutableStateOf(initialItem?.word ?: "") }
    var definition by remember(initialItem) { mutableStateOf(initialItem?.definition ?: "") }
    var category by remember(initialItem) { mutableStateOf(initialItem?.category ?: "General") }
    var pictureEmoji by remember(initialItem) { mutableStateOf(initialItem?.pictureEmoji ?: "✨") }
    var synonym by remember(initialItem) { mutableStateOf(initialItem?.synonym ?: "") }
    var antonym by remember(initialItem) { mutableStateOf(initialItem?.antonym ?: "") }
    var scrambleHint by remember(initialItem) { mutableStateOf(initialItem?.scrambleHint ?: "") }
    var distractorsText by remember(initialItem) {
        mutableStateOf(initialItem?.distractors?.joinToString(", ") ?: "")
    }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val quickEmojis = listOf("🍎", "🔭", "🧭", "🌋", "🏆", "🗼", "🦋", "📚", "🚀", "🎨", "🌍", "🎸")

    fun resetFieldsForNext() {
        word = ""
        definition = ""
        synonym = ""
        antonym = ""
        scrambleHint = ""
        distractorsText = ""
        errorMsg = null
    }

    fun trySubmit(keepOpen: Boolean) {
        val cleanWord = word.trim()
        val cleanDef = definition.trim()
        val cleanSyn = synonym.trim()
        val cleanAnt = antonym.trim()
        val cleanHint = scrambleHint.trim()
        val cleanEmoji = pictureEmoji.trim().ifBlank { "✨" }
        val cleanCategory = category.trim().ifBlank { "General" }
        val distractorList = distractorsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

        when {
            cleanWord.isBlank() -> {
                errorMsg = "Please enter the English word."
            }
            activityType == VocabularyActivityType.MEANING_MATCHING && cleanDef.isBlank() -> {
                errorMsg = "Please enter the meaning / definition."
            }
            activityType == VocabularyActivityType.SYNONYMS && cleanSyn.isBlank() -> {
                errorMsg = "Please enter the correct synonym word."
            }
            activityType == VocabularyActivityType.OPPOSITE_WORDS && cleanAnt.isBlank() -> {
                errorMsg = "Please enter the correct opposite word (antonym)."
            }
            activityType == VocabularyActivityType.WORD_SCRAMBLE && cleanHint.isBlank() && cleanDef.isBlank() -> {
                errorMsg = "Please provide a scramble hint or definition clue."
            }
            else -> {
                val resolvedDef = cleanDef.ifBlank {
                    when (activityType) {
                        VocabularyActivityType.PICTURE_MATCHING -> "Visual vocabulary item ($cleanWord)"
                        VocabularyActivityType.SYNONYMS -> "Similar in meaning to $cleanSyn"
                        VocabularyActivityType.OPPOSITE_WORDS -> "Opposite in meaning to $cleanAnt"
                        VocabularyActivityType.WORD_SCRAMBLE -> cleanHint
                        VocabularyActivityType.MEANING_MATCHING -> cleanDef
                    }
                }
                onSave(
                    cleanWord,
                    resolvedDef,
                    cleanCategory,
                    activityType,
                    cleanEmoji,
                    cleanSyn,
                    cleanAnt,
                    distractorList,
                    cleanHint,
                    keepOpen
                )
                if (keepOpen) {
                    resetFieldsForNext()
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vocab_challenge_editor_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (initialItem == null) "Add Vocabulary Challenge Item" else "Edit Vocabulary Challenge Item",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )

                Text(
                    text = "Select Activity Mode",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VocabularyActivityType.values().forEach { type ->
                        val isSel = activityType == type
                        FilterChip(
                            selected = isSel,
                            onClick = {
                                activityType = type
                                errorMsg = null
                            },
                            label = {
                                Text("${type.emoji} ${type.title}", fontSize = 11.5.sp)
                            },
                            modifier = Modifier.testTag("vocab_editor_type_${type.name.lowercase()}")
                        )
                    }
                }

                OutlinedTextField(
                    value = word,
                    onValueChange = {
                        word = it
                        errorMsg = null
                    },
                    label = { Text("English Word *") },
                    placeholder = { Text("e.g., Telescope, Eloquent, Ancient") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vocab_editor_word_input")
                )

                if (activityType == VocabularyActivityType.PICTURE_MATCHING ||
                    activityType == VocabularyActivityType.WORD_SCRAMBLE
                ) {
                    OutlinedTextField(
                        value = pictureEmoji,
                        onValueChange = { pictureEmoji = it },
                        label = { Text("Picture / Visual Emoji") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vocab_editor_emoji_input")
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(quickEmojis) { em ->
                            Surface(
                                shape = CircleShape,
                                color = if (pictureEmoji == em) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(
                                    1.dp,
                                    if (pictureEmoji == em) MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                modifier = Modifier
                                    .size(44.dp)
                                    .clickable { pictureEmoji = em }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(em, fontSize = 20.sp)
                                }
                            }
                        }
                    }
                }

                if (activityType == VocabularyActivityType.SYNONYMS) {
                    OutlinedTextField(
                        value = synonym,
                        onValueChange = {
                            synonym = it
                            errorMsg = null
                        },
                        label = { Text("Correct Synonym *") },
                        placeholder = { Text("Word with same/similar meaning") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vocab_editor_synonym_input")
                    )
                }

                if (activityType == VocabularyActivityType.OPPOSITE_WORDS) {
                    OutlinedTextField(
                        value = antonym,
                        onValueChange = {
                            antonym = it
                            errorMsg = null
                        },
                        label = { Text("Correct Opposite Word (Antonym) *") },
                        placeholder = { Text("Word with opposite meaning") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vocab_editor_antonym_input")
                    )
                }

                if (activityType == VocabularyActivityType.WORD_SCRAMBLE) {
                    OutlinedTextField(
                        value = scrambleHint,
                        onValueChange = {
                            scrambleHint = it
                            errorMsg = null
                        },
                        label = { Text("Scramble Clue / Hint *") },
                        placeholder = { Text("Clue shown while unscrambling tiles") },
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vocab_editor_scramble_hint_input")
                    )
                }

                OutlinedTextField(
                    value = definition,
                    onValueChange = {
                        definition = it
                        errorMsg = null
                    },
                    label = {
                        Text(
                            if (activityType == VocabularyActivityType.MEANING_MATCHING) "Meaning / Definition *"
                            else "Meaning / Extra Clue (Optional)"
                        )
                    },
                    placeholder = { Text("Clear definition shown as feedback or clue") },
                    maxLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vocab_editor_definition_input")
                )

                if (activityType != VocabularyActivityType.WORD_SCRAMBLE) {
                    OutlinedTextField(
                        value = distractorsText,
                        onValueChange = { distractorsText = it },
                        label = { Text("Wrong Options (Comma-separated, optional)") },
                        placeholder = { Text("Auto-filled if left blank (up to 3)") },
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vocab_editor_distractors_input")
                    )
                }

                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category") },
                    placeholder = { Text("e.g., Science, Adjectives, Communication") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vocab_editor_category_input")
                )

                errorMsg?.let { err ->
                    Text(err, color = CoralAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                if (initialItem == null) {
                    OutlinedButton(
                        onClick = { trySubmit(keepOpen = true) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("vocab_editor_save_add_another_btn")
                    ) {
                        Text("Save & Add Another", fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { trySubmit(keepOpen = false) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("vocab_editor_save_btn")
                    ) {
                        Text(
                            text = if (initialItem == null) "Publish" else "Save Changes",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
