package com.example.ui.screens.teacher

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AdventureMissionType
import com.example.data.model.VocabularyActivityType
import com.example.ui.MainViewModel
import com.example.ui.theme.CoralAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.MutedGray
import com.example.ui.theme.RoyalBlue
import com.example.util.BulkImportGameType
import com.example.util.GameExcelImportParser
import com.example.util.ParsedGameImportRow

/**
 * Reusable emerald-accented toolbar banner displayed on every Teacher Game tab
 * so teachers can import .xlsx / .csv spreadsheets directly with one tap.
 */
@Composable
fun BulkExcelImportBannerCard(
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.95f)),
        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.55f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(EmeraldSuccess.copy(alpha = 0.18f))
                        .border(1.dp, EmeraldSuccess.copy(alpha = 0.55f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TableChart,
                        contentDescription = null,
                        tint = EmeraldSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("${testTag}_btn")
            ) {
                Icon(
                    imageVector = Icons.Rounded.UploadFile,
                    contentDescription = "Import Excel Sheet",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = "Import Sheet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.White,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Compact callout displayed inside single-item "Add Challenge / Add Question" dialogs
 * to switch smoothly into Bulk Excel Sheet Import.
 */
@Composable
fun BulkExcelInlineCallout(
    title: String = "Bulk Excel Sheet Import",
    subtitle: String = "Upload .xlsx / .csv or paste spreadsheet rows",
    testTag: String = "dialog_open_bulk_import_btn",
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF059669).copy(alpha = 0.12f),
        border = BorderStroke(1.dp, Color(0xFF059669).copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.TableChart,
                    contentDescription = null,
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Color(0xFF059669)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 10.5.sp,
                        color = MutedGray
                    )
                }
            }
            Text(
                text = "Open Import →",
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF059669)
            )
        }
    }
}

/**
 * Universal Bulk Excel / CSV Importer Dialog supporting:
 * Vocabulary Challenge, Listen & Choose, Pronunciation Battle, Fast Vocabulary,
 * Picture Vocabulary, Grammar Challenge, and English Adventure.
 */
@Composable
fun BulkGameExcelImportDialog(
    category: GameCategoryFilter,
    viewModel: MainViewModel,
    authorName: String,
    initialVocabMode: VocabularyActivityType? = null,
    initialGrammarPackId: String? = null,
    initialFastVocabPackId: String? = null,
    onDismiss: () -> Unit,
    onImported: (count: Int, destinationLabel: String) -> Unit
) {
    val gameType = remember(category) { category.toBulkImportGameType() } ?: return
    val context = LocalContext.current

    val grammarPacks by viewModel.grammarChallengePacks.collectAsState()
    val fastVocabPacks by viewModel.fastVocabularyPacks.collectAsState()
    val adventureWorlds by viewModel.adventureWorlds.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Upload .xlsx/.csv, 1 = Paste / Template
    var selectedVocabMode by remember {
        mutableStateOf(initialVocabMode ?: VocabularyActivityType.MEANING_MATCHING)
    }
    var selectedGrammarPackId by remember(grammarPacks, initialGrammarPackId) {
        mutableStateOf(
            initialGrammarPackId
                ?: grammarPacks.firstOrNull()?.id
                ?: "gc_pack_tenses"
        )
    }
    var selectedFastVocabPackId by remember(fastVocabPacks, initialFastVocabPackId) {
        mutableStateOf(
            initialFastVocabPackId
                ?: fastVocabPacks.firstOrNull()?.id
                ?: "fv_pack_easy"
        )
    }
    var selectedAdventureWorldId by remember(adventureWorlds) {
        mutableStateOf(
            adventureWorlds.firstOrNull { it.id == "world_vocab" }?.id
                ?: adventureWorlds.firstOrNull()?.id
                ?: "world_vocab"
        )
    }

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var rawInputText by remember { mutableStateOf("") }
    var parsedRows by remember { mutableStateOf<List<ParsedGameImportRow>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val selectedGrammarPack = remember(grammarPacks, selectedGrammarPackId) {
        grammarPacks.firstOrNull { it.id == selectedGrammarPackId }
    }
    val selectedFastVocabPack = remember(fastVocabPacks, selectedFastVocabPackId) {
        fastVocabPacks.firstOrNull { it.id == selectedFastVocabPackId }
    }
    val selectedAdventureWorld = remember(adventureWorlds, selectedAdventureWorldId) {
        adventureWorlds.firstOrNull { it.id == selectedAdventureWorldId }
    }

    val currentDefaultCategory = when (gameType) {
        BulkImportGameType.GRAMMAR_CHALLENGE -> selectedGrammarPack?.themeStyle ?: "Grammar"
        else -> "General"
    }

    val columnGuide = remember(gameType) { GameExcelImportParser.getColumnGuide(gameType) }

    fun reparseRawText(text: String, mode: VocabularyActivityType = selectedVocabMode, cat: String = currentDefaultCategory) {
        if (text.isBlank()) {
            parsedRows = emptyList()
            errorMessage = null
            return
        }
        val rows = GameExcelImportParser.parseFromText(
            rawText = text,
            gameType = gameType,
            defaultVocabMode = mode,
            defaultCategory = cat
        )
        parsedRows = rows
        errorMessage = if (rows.isEmpty()) {
            "No valid rows detected. Check the expected columns below or tap Load Sample."
        } else {
            null
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            errorMessage = null
            try {
                var fileName: String? = null
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIdx != -1) {
                            fileName = cursor.getString(nameIdx)
                        }
                    }
                }
                val resolvedName = fileName ?: uri.lastPathSegment ?: "sheet.xlsx"
                selectedFileName = resolvedName

                val rows = GameExcelImportParser.parseFromUri(
                    context = context,
                    uri = uri,
                    fileName = resolvedName,
                    gameType = gameType,
                    defaultVocabMode = selectedVocabMode,
                    defaultCategory = currentDefaultCategory
                )
                if (rows.isEmpty()) {
                    parsedRows = emptyList()
                    errorMessage = "No valid rows found in '$resolvedName'. Expected columns: ${columnGuide.joinToString(", ")}."
                } else {
                    parsedRows = rows
                    errorMessage = null
                }
            } catch (e: Exception) {
                parsedRows = emptyList()
                errorMessage = "Failed to read file: ${e.localizedMessage ?: "Unsupported file format"}"
            } finally {
                isProcessing = false
            }
        }
    }

    val destinationSubtitle = when (gameType) {
        BulkImportGameType.WORD_MATCH -> "Default Mode: ${selectedVocabMode.title}"
        BulkImportGameType.GRAMMAR_CHALLENGE -> "Target Pack: ${selectedGrammarPack?.title ?: "Grammar Challenge"}"
        BulkImportGameType.FAST_VOCABULARY -> "Target Pack: ${selectedFastVocabPack?.title ?: "Fast Vocabulary"}"
        BulkImportGameType.ENGLISH_ADVENTURE -> "Target World: ${selectedAdventureWorld?.name ?: "English Adventure"}"
        else -> "Syncs automatically to Student Panel"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bulk_game_excel_dialog"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(EmeraldSuccess),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(category.emoji, fontSize = 18.sp)
                    }
                    Column {
                        Text(
                            text = "Bulk ${category.title} Excel Import",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = destinationSubtitle,
                            fontSize = 11.sp,
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("bulk_excel_close_btn")
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close bulk import", tint = MutedGray)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    contentColor = RoyalBlue
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("bulk_excel_upload_tab"),
                        text = { Text("📁 Upload .xlsx / .csv", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("bulk_excel_paste_tab"),
                        text = { Text("📋 Paste / Template", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) }
                    )
                }

                // Game-specific target selector chips
                when (gameType) {
                    BulkImportGameType.WORD_MATCH -> {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Default Activity Mode (when Mode column is blank):",
                                fontSize = 11.sp,
                                color = MutedGray,
                                fontWeight = FontWeight.SemiBold
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                VocabularyActivityType.values().forEach { mode ->
                                    val isSel = selectedVocabMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                                        modifier = Modifier
                                            .clickable {
                                                selectedVocabMode = mode
                                                if (selectedTab == 1 && rawInputText.isNotBlank()) {
                                                    reparseRawText(rawInputText, mode = mode)
                                                }
                                            }
                                            .testTag("bulk_vocab_mode_${mode.name.lowercase()}")
                                    ) {
                                        Text(
                                            text = "${mode.emoji} ${mode.title}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    BulkImportGameType.GRAMMAR_CHALLENGE -> {
                        if (grammarPacks.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Import into Grammar Challenge Pack:",
                                    fontSize = 11.sp,
                                    color = MutedGray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    grammarPacks.forEach { pack ->
                                        val isSel = selectedGrammarPackId == pack.id
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .clickable {
                                                    selectedGrammarPackId = pack.id
                                                    if (selectedTab == 1 && rawInputText.isNotBlank()) {
                                                        reparseRawText(rawInputText, cat = pack.themeStyle)
                                                    }
                                                }
                                                .testTag("bulk_grammar_pack_${pack.id}")
                                        ) {
                                            Text(
                                                text = pack.title,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BulkImportGameType.FAST_VOCABULARY -> {
                        if (fastVocabPacks.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Import into Fast Vocabulary Pack:",
                                    fontSize = 11.sp,
                                    color = MutedGray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    fastVocabPacks.forEach { pack ->
                                        val isSel = selectedFastVocabPackId == pack.id
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .clickable { selectedFastVocabPackId = pack.id }
                                                .testTag("bulk_fast_vocab_pack_${pack.id}")
                                        ) {
                                            Text(
                                                text = pack.title,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    BulkImportGameType.ENGLISH_ADVENTURE -> {
                        if (adventureWorlds.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Import Levels into Adventure World:",
                                    fontSize = 11.sp,
                                    color = MutedGray,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    adventureWorlds.forEach { world ->
                                        val isSel = selectedAdventureWorldId == world.id
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = if (isSel) RoyalBlue else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .clickable { selectedAdventureWorldId = world.id }
                                                .testTag("bulk_adventure_world_${world.id}")
                                        ) {
                                            Text(
                                                text = "${world.emoji} ${world.name}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    else -> Unit
                }

                // Tab content
                if (selectedTab == 0) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = EmeraldSuccess.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, EmeraldSuccess.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.UploadFile,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = selectedFileName ?: "Choose an Excel (.xlsx) or CSV (.csv) sheet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Columns: ${columnGuide.joinToString(" | ")}",
                                fontSize = 10.5.sp,
                                color = MutedGray
                            )
                            Button(
                                onClick = {
                                    filePickerLauncher.launch(
                                        arrayOf(
                                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                            "text/csv",
                                            "text/comma-separated-values",
                                            "text/tab-separated-values",
                                            "text/plain",
                                            "*/*"
                                        )
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("bulk_excel_pick_file_btn")
                            ) {
                                Icon(Icons.Rounded.UploadFile, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (selectedFileName == null) "Select Excel / CSV File" else "Choose Another File",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Paste Excel / CSV rows:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MutedGray
                            )
                            OutlinedButton(
                                onClick = {
                                    val sample = GameExcelImportParser.getSampleCsv(gameType)
                                    rawInputText = sample
                                    reparseRawText(sample)
                                },
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .heightIn(min = 40.dp)
                                    .testTag("bulk_excel_load_sample_btn")
                            ) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Load Sample", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedTextField(
                            value = rawInputText,
                            onValueChange = {
                                rawInputText = it
                                reparseRawText(it)
                            },
                            placeholder = {
                                Text(
                                    text = columnGuide.joinToString(","),
                                    fontSize = 11.sp
                                )
                            },
                            minLines = 4,
                            maxLines = 6,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bulk_excel_paste_input")
                        )
                    }
                }

                if (isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Parsing spreadsheet...", fontSize = 12.sp, color = MutedGray)
                    }
                }

                errorMessage?.let { err ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CoralAccent.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, CoralAccent.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = err,
                            color = CoralAccent,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Live Preview List
                if (parsedRows.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Preview Ready (${parsedRows.size} ${if (parsedRows.size == 1) "row" else "rows"})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = EmeraldSuccess
                        )
                        TextButton(
                            onClick = {
                                parsedRows = emptyList()
                                rawInputText = ""
                                selectedFileName = null
                                errorMessage = null
                            }
                        ) {
                            Text("Clear All", fontSize = 11.5.sp, color = CoralAccent)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 210.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        itemsIndexed(parsedRows) { idx, row ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = "${idx + 1}. ${row.previewTitle}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f, fill = false)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = RoyalBlue.copy(alpha = 0.14f)
                                            ) {
                                                Text(
                                                    text = row.previewBadge,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = RoyalBlue,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = row.previewSubtitle,
                                            fontSize = 10.5.sp,
                                            color = MutedGray,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            parsedRows = parsedRows.filterIndexed { i, _ -> i != idx }
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.DeleteOutline,
                                            contentDescription = "Remove row ${idx + 1}",
                                            tint = CoralAccent,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val count = commitParsedGameRows(
                        rows = parsedRows,
                        viewModel = viewModel,
                        authorName = authorName,
                        grammarPackId = selectedGrammarPackId,
                        fastVocabPackId = selectedFastVocabPackId,
                        adventureWorldId = selectedAdventureWorldId
                    )
                    val destLabel = when (gameType) {
                        BulkImportGameType.GRAMMAR_CHALLENGE -> selectedGrammarPack?.title ?: category.title
                        BulkImportGameType.FAST_VOCABULARY -> selectedFastVocabPack?.title ?: category.title
                        BulkImportGameType.ENGLISH_ADVENTURE -> selectedAdventureWorld?.name ?: category.title
                        else -> category.title
                    }
                    onImported(count, destLabel)
                },
                enabled = parsedRows.isNotEmpty() && !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("bulk_excel_import_confirm_btn")
            ) {
                Text(
                    text = "Import All (${parsedRows.size}) 🚀",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp)
            ) {
                Text("Cancel")
            }
        }
    )
}

private fun GameCategoryFilter.toBulkImportGameType(): BulkImportGameType? = when (this) {
    GameCategoryFilter.WORD_MATCH -> BulkImportGameType.WORD_MATCH
    GameCategoryFilter.LISTEN_CHOOSE -> BulkImportGameType.LISTEN_CHOOSE
    GameCategoryFilter.PRONUNCIATION_BATTLE -> BulkImportGameType.PRONUNCIATION_BATTLE
    GameCategoryFilter.FAST_VOCABULARY -> BulkImportGameType.FAST_VOCABULARY
    GameCategoryFilter.PICTURE_VOCAB -> BulkImportGameType.PICTURE_VOCAB
    GameCategoryFilter.GRAMMAR_CHALLENGE -> BulkImportGameType.GRAMMAR_CHALLENGE
    GameCategoryFilter.ENGLISH_ADVENTURE -> BulkImportGameType.ENGLISH_ADVENTURE
    GameCategoryFilter.MISSING_LETTERS,
    GameCategoryFilter.SENTENCE_BUILDER -> null
}

private fun commitParsedGameRows(
    rows: List<ParsedGameImportRow>,
    viewModel: MainViewModel,
    authorName: String,
    grammarPackId: String,
    fastVocabPackId: String,
    adventureWorldId: String
): Int {
    return viewModel.bulkImportParsedGames(
        rows = rows,
        authorName = authorName,
        grammarPackId = grammarPackId,
        fastVocabPackId = fastVocabPackId,
        adventureWorldId = adventureWorldId
    )
}
