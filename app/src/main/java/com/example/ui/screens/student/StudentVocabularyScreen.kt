package com.example.ui.screens.student

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VocabularyWord
import com.example.ui.MainViewModel
import com.example.ui.SpeakingSubTab

@Composable
fun StudentVocabularyExplorer(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val vocabularyWords by viewModel.vocabularyWords.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = remember(vocabularyWords) {
        val cats = vocabularyWords.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
        listOf("All", "Teacher Added", "Favorites ❤️") + cats
    }

    val filteredWords = remember(vocabularyWords, searchQuery, selectedCategory) {
        vocabularyWords.filter { w ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Teacher Added" -> w.addedByTeacher.isNotBlank()
                "Favorites ❤️" -> w.isFavorite
                else -> w.category.equals(selectedCategory, ignoreCase = true)
            }
            val q = searchQuery.trim()
            val matchesSearch = q.isBlank() ||
                w.word.contains(q, ignoreCase = true) ||
                w.assameseMeaning.contains(q, ignoreCase = true) ||
                w.assameseTransliteration.contains(q, ignoreCase = true) ||
                w.meaning.contains(q, ignoreCase = true) ||
                w.category.contains(q, ignoreCase = true) ||
                w.addedByTeacher.contains(q, ignoreCase = true)

            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_vocabulary_explorer"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Header Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = BorderStroke(1.5.dp, Color(0xFFF59E0B).copy(alpha = 0.8f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF1E1138).copy(alpha = 0.95f),
                                Color(0xFF0F1B4E).copy(alpha = 0.95f),
                                Color(0xFF062A45).copy(alpha = 0.98f)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                            border = BorderStroke(1.5.dp, Color(0xFFFBBF24)),
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("📖", fontSize = 26.sp)
                            }
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF065F46).copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, Color(0xFF34D399).copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "অসমীয়া দ্বিভাষিক শব্দকোষ",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFA7F3D0),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF7C2D12).copy(alpha = 0.6f),
                                    border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "${vocabularyWords.size} Active Words",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFDBA74),
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "English & Assamese Vocabulary",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Color.White
                            )

                            Text(
                                text = "Master words added by faculty with Assamese script meanings, phonetics, audio & tips.",
                                fontSize = 11.5.sp,
                                lineHeight = 15.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }
        }

        // 2. Search Input Field
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0C1738).copy(alpha = 0.95f),
            border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search English term or অসমীয়া অৰ্থ...",
                            fontSize = 12.5.sp,
                            color = Color(0xFF94A3B8),
                            maxLines = 1
                        )
                    }
                    androidx.compose.foundation.text.BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("student_vocab_search_input")
                    )
                }

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Close,
                            contentDescription = "Clear",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // 3. Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { cat ->
                val isSel = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSel) Color(0xFF7C3AED) else Color(0xFF131A3E).copy(alpha = 0.85f),
                    border = BorderStroke(
                        1.2.dp,
                        if (isSel) Color(0xFFC084FC) else Color(0xFF263366)
                    ),
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        fontSize = 12.sp,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // 4. Word Count Summary
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${filteredWords.size} of ${vocabularyWords.size} words",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.Medium
            )
            if (searchQuery.isNotBlank() || selectedCategory != "All") {
                Text(
                    text = "Reset Filters",
                    fontSize = 12.sp,
                    color = Color(0xFF60A5FA),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        searchQuery = ""
                        selectedCategory = "All"
                    }
                )
            }
        }

        // 5. Vocabulary Word Cards List
        if (filteredWords.isEmpty()) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🔍", fontSize = 32.sp)
                    Text(
                        text = "No words found",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Try searching for a different English word or Assamese keyword.",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredWords.forEach { word ->
                    StudentVocabularyCard(
                        word = word,
                        onSpeak = { text, slow -> viewModel.speakAudio(text, slow) },
                        onToggleFavorite = { viewModel.toggleWordFavorite(word.id) },
                        onPractice = {
                            viewModel.searchPhonetics(word.word)
                            viewModel.selectSpeakingSubTab(SpeakingSubTab.PRONUNCIATION_COACH)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun StudentVocabularyCard(
    word: VocabularyWord,
    onSpeak: (String, Boolean) -> Unit,
    onToggleFavorite: () -> Unit,
    onPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1433).copy(alpha = 0.95f)),
        border = BorderStroke(1.2.dp, Color(0xFF1E3A8A).copy(alpha = 0.8f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("student_vocab_card_${word.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Word, Syllables pill, POS, Favorite button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = word.word,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )

                    if (word.syllables.isNotBlank() && !word.syllables.equals(word.word, ignoreCase = true)) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B),
                            border = BorderStroke(0.8.dp, Color(0xFF475569))
                        ) {
                            Text(
                                text = word.syllables,
                                fontSize = 11.sp,
                                color = Color(0xFFE2E8F0),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF312E81).copy(alpha = 0.8f),
                        border = BorderStroke(0.8.dp, Color(0xFF6366F1).copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = word.partOfSpeech,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA5B4FC),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (word.isFavorite) Icons.Rounded.Favorite else Icons.Rounded.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (word.isFavorite) Color(0xFFF43F5E) else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Pronunciation & Audio Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🗣️", fontSize = 12.sp)
                    Text(
                        text = word.phonetic,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF38BDF8)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Normal Speech
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.25f),
                        border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                        modifier = Modifier.clickable { onSpeak(word.word, false) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen", tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                            Text("Listen", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                        }
                    }

                    // Slow Speech
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF4C1D95).copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, Color(0xFFA855F7).copy(alpha = 0.6f)),
                        modifier = Modifier.clickable { onSpeak(word.word, true) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🐢", fontSize = 11.sp)
                            Text("Slow", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC084FC))
                        }
                    }
                }
            }

            // Assamese Meaning & Transliteration Card
            if (word.assameseMeaning.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF063327).copy(alpha = 0.85f),
                    border = BorderStroke(1.2.dp, Color(0xFF10B981).copy(alpha = 0.7f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text("🌿", fontSize = 12.sp)
                            Text(
                                text = "অসমীয়া অৰ্থ (Assamese Meaning):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6EE7B7)
                            )
                        }

                        Text(
                            text = word.assameseMeaning,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFFFEF08A),
                            lineHeight = 20.sp
                        )

                        if (word.assameseTransliteration.isNotBlank()) {
                            Text(
                                text = "উচ্চাৰণ: ${word.assameseTransliteration}",
                                fontSize = 11.5.sp,
                                fontStyle = FontStyle.Italic,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }
            }

            // English Meaning
            if (word.meaning.isNotBlank()) {
                Text(
                    text = word.meaning,
                    fontSize = 12.5.sp,
                    color = Color(0xFFE2E8F0),
                    lineHeight = 17.sp
                )
            }

            // Example Sentence
            if (word.exampleSentence.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF1E1B4B).copy(alpha = 0.5f),
                    border = BorderStroke(0.8.dp, Color(0xFF4338CA).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("💬", fontSize = 12.sp)
                        Text(
                            text = "\"${word.exampleSentence}\"",
                            fontSize = 11.5.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFFE0E7FF),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onSpeak(word.exampleSentence, false) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Rounded.VolumeUp, contentDescription = "Listen sentence", tint = Color(0xFF818CF8), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }

            // Coaching Tip
            if (word.coachingTip.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("💡", fontSize = 12.sp)
                    Text(
                        text = word.coachingTip,
                        fontSize = 11.sp,
                        color = Color(0xFFFDE68A),
                        lineHeight = 15.sp
                    )
                }
            }

            // Bottom Actions & Attribution Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Attribution Pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (word.addedByTeacher.isNotBlank()) Color(0xFF78350F).copy(alpha = 0.4f) else Color(0xFF1E293B),
                    border = BorderStroke(0.8.dp, if (word.addedByTeacher.isNotBlank()) Color(0xFFF59E0B).copy(alpha = 0.5f) else Color(0xFF475569))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(if (word.addedByTeacher.isNotBlank()) "🧑‍🏫" else "🎓", fontSize = 10.sp)
                        Text(
                            text = if (word.addedByTeacher.isNotBlank()) "Added by ${word.addedByTeacher}" else "Core Curriculum",
                            fontSize = 10.sp,
                            color = if (word.addedByTeacher.isNotBlank()) Color(0xFFFDE68A) else Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Practice Button
                Button(
                    onClick = onPractice,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Rounded.Mic, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Practice", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
