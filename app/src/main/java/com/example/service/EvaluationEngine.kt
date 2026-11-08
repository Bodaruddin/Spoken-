package com.example.service

import com.example.data.model.SpeakingEvaluation
import kotlin.math.max

object EvaluationEngine {

    fun evaluateSpeech(
        spokenText: String,
        targetPhrase: String? = null,
        requiredKeywords: List<String> = emptyList()
    ): SpeakingEvaluation {
        val cleanSpoken = spokenText.trim()
        val words = cleanSpoken.split("\\s+".toRegex()).filter { it.isNotBlank() }

        if (words.isEmpty()) {
            return SpeakingEvaluation(
                spokenText = spokenText,
                expectedText = targetPhrase,
                overallScore = 0,
                pronunciationScore = 0,
                fluencyScore = 0,
                grammarScore = 0,
                vocabularyScore = 0,
                feedback = "No speech detected. Please speak louder into the microphone.",
                suggestedImprovement = "Try saying: \"${targetPhrase ?: "Hello, nice to meet you."}\""
            )
        }

        // 1. Pronunciation Score
        val pronunciationScore: Int
        val phonemeMatches = mutableListOf<Pair<String, Boolean>>()

        if (!targetPhrase.isNullOrBlank()) {
            val targetWords = targetPhrase.lowercase().replace("[^a-z0-9 ]".toRegex(), "").split("\\s+".toRegex())
            val spokenLowerWords = cleanSpoken.lowercase().replace("[^a-z0-9 ]".toRegex(), "").split("\\s+".toRegex())

            var matchedCount = 0
            for (tWord in targetWords) {
                val found = spokenLowerWords.any { sWord ->
                    levenshteinSimilarity(tWord, sWord) >= 0.70
                }
                phonemeMatches.add(tWord to found)
                if (found) matchedCount++
            }

            val matchRatio = matchedCount.toFloat() / max(1, targetWords.size)
            pronunciationScore = (matchRatio * 70 + 30).toInt().coerceIn(35, 98)
        } else {
            // Open speech pronunciation based on word length & phoneme clarity
            pronunciationScore = (75 + (words.size * 2).coerceAtMost(20)).coerceIn(60, 95)
        }

        // 2. Fluency Score
        val wordCount = words.size
        val fluencyScore = when {
            wordCount >= 15 -> 92
            wordCount >= 10 -> 86
            wordCount >= 6 -> 80
            wordCount >= 3 -> 72
            else -> 65
        }

        // 3. Vocabulary Score
        val uniqueWords = words.map { it.lowercase() }.toSet().size
        val lexicalDiversity = uniqueWords.toFloat() / max(1, wordCount)
        val keywordMatches = requiredKeywords.count { kw ->
            cleanSpoken.contains(kw, ignoreCase = true)
        }
        val keywordBonus = if (requiredKeywords.isNotEmpty()) {
            (keywordMatches.toFloat() / requiredKeywords.size * 20).toInt()
        } else 10

        val vocabularyScore = ((lexicalDiversity * 70) + keywordBonus).toInt().coerceIn(60, 98)

        // 4. Grammar Score
        var grammarIssues = 0
        if (!cleanSpoken.first().isUpperCase()) grammarIssues++
        val commonErrors = listOf("i is", "they is", "she have", "he have", "we is", "much people", "more better")
        for (err in commonErrors) {
            if (cleanSpoken.lowercase().contains(err)) grammarIssues += 2
        }
        val grammarScore = (95 - (grammarIssues * 10)).coerceIn(55, 96)

        // Overall Weighted Score
        val overallScore = ((pronunciationScore * 0.35) + (fluencyScore * 0.25) + (grammarScore * 0.20) + (vocabularyScore * 0.20)).toInt()

        // Detailed Feedback and Suggestions
        val feedbackBuilder = StringBuilder()
        when {
            overallScore >= 90 -> feedbackBuilder.append("Outstanding clarity and natural rhythm! You spoke with great confidence.")
            overallScore >= 80 -> feedbackBuilder.append("Great job! Your pronunciation was clear and easy to understand.")
            overallScore >= 70 -> feedbackBuilder.append("Good attempt! Keep working on your speech flow and intonation.")
            else -> feedbackBuilder.append("Nice try! Practice speaking each word distinctly with the audio guide.")
        }

        val suggestedImprovement = when {
            !targetPhrase.isNullOrBlank() -> "Focus on stressed syllables: \"$targetPhrase\""
            cleanSpoken.split(" ").size < 6 -> "Try elaborating with linking words like 'because', 'furthermore', or 'in addition'."
            grammarIssues > 0 -> "Watch your subject-verb agreement for smoother natural communication."
            else -> "Add descriptive adjectives to expand your speaking richness."
        }

        return SpeakingEvaluation(
            spokenText = cleanSpoken,
            expectedText = targetPhrase,
            overallScore = overallScore,
            pronunciationScore = pronunciationScore,
            fluencyScore = fluencyScore,
            grammarScore = grammarScore,
            vocabularyScore = vocabularyScore,
            feedback = feedbackBuilder.toString(),
            suggestedImprovement = suggestedImprovement,
            phonemeAccuracy = phonemeMatches
        )
    }

    private fun levenshteinSimilarity(s1: String, s2: String): Double {
        if (s1 == s2) return 1.0
        val maxLen = max(s1.length, s2.length)
        if (maxLen == 0) return 1.0
        val dist = levenshteinDistance(s1, s2)
        return 1.0 - (dist.toDouble() / maxLen)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
